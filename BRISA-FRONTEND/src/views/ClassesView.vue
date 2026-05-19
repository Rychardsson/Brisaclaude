<template>
  <div class="classes-view">
    <section class="page-card">
      <div class="header">
        <div>
          <h1>Turmas</h1>
          <p v-if="filterByProgram" class="subtitle">
            Programa: {{ currentProgram?.name || 'Carregando...' }}
          </p>
          <p v-else class="subtitle">
            Visualize, ajuste e acompanhe as turmas cadastradas no sistema.
          </p>
        </div>

        <div class="header-actions">
          <button type="button" class="btn-secondary" @click="reloadData" :disabled="loading">
            Atualizar
          </button>
          <button type="button" class="btn-primary" @click="showUploadModal = true">
            Importar Excel
          </button>
        </div>
      </div>

      <div class="filters">
        <input
          v-model="searchTerm"
          type="text"
          placeholder="Buscar por código, programa ou localidade..."
          class="search-input"
        />
      </div>

      <div class="summary-row">
        <span class="summary-pill">{{ filteredClasses.length }} turma(s)</span>
      </div>

      <div v-if="loading" class="state-box">Carregando turmas...</div>
      <div v-else-if="error" class="state-box state-box-error">{{ error }}</div>

      <div v-else class="table-container">
        <table>
          <thead>
            <tr>
              <th>Código</th>
              <th>Programa</th>
              <th>Início</th>
              <th>Fim</th>
              <th>Localidade</th>
              <th>Instituição</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="classItem in filteredClasses" :key="classItem.id">
              <td>{{ classItem.code || '-' }}</td>
              <td>{{ classItem.program?.name || '-' }}</td>
              <td>{{ formatDate(classItem.startDate) }}</td>
              <td>{{ formatDate(classItem.endDate) }}</td>
              <td>{{ classItem.locality || '-' }}</td>
              <td>{{ classItem.location?.name || '-' }}</td>
              <td class="actions">
                <button type="button" class="btn-view" @click="viewEnrollments(classItem)">Matrículas</button>
                <button type="button" class="btn-outline" @click="openClassDetails(classItem)">Detalhes</button>
                <button type="button" class="btn-edit" @click="editClass(classItem)">Editar</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="filteredClasses.length === 0" class="no-data">
          Nenhuma turma encontrada.
        </div>
      </div>
    </section>

    <div v-if="showUploadModal" class="modal-overlay" @click="closeUploadModal">
      <div class="modal-content" @click.stop>
        <h2>Importar turmas via Excel</h2>
        <p class="modal-copy">
          Selecione uma planilha `.xlsx` ou `.xls` para criar ou atualizar turmas em lote.
        </p>

        <div class="upload-area">
          <input
            ref="fileInput"
            type="file"
            accept=".xlsx,.xls"
            @change="handleFileSelect"
          />
          <p v-if="selectedFile">{{ selectedFile.name }}</p>
        </div>

        <div v-if="uploadError" class="feedback feedback-error">{{ uploadError }}</div>
        <div v-if="uploadSuccess" class="feedback feedback-success">{{ uploadSuccess }}</div>

        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="closeUploadModal">Cancelar</button>
          <button type="button" class="btn-primary" :disabled="!selectedFile || uploading" @click="uploadFile">
            {{ uploading ? 'Enviando...' : 'Enviar planilha' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content modal-content-wide" @click.stop>
        <h2>Editar turma</h2>
        <p class="modal-copy">
          Atualize os dados principais da turma e mantenha o cadastro sincronizado com o backend.
        </p>

        <div class="form-grid">
          <label class="field">
            <span>Código</span>
            <input v-model="editForm.code" type="text" />
          </label>

          <label class="field">
            <span>Programa</span>
            <select v-model="editForm.programId">
              <option value="">Selecione</option>
              <option v-for="program in programs" :key="program.id" :value="String(program.id)">
                {{ program.name || `Programa ${program.id}` }}
              </option>
            </select>
          </label>

          <label class="field">
            <span>Instituição</span>
            <select v-model="editForm.locationId">
              <option value="">Selecione</option>
              <option v-for="institution in institutions" :key="institution.id" :value="String(institution.id)">
                {{ institution.name || institution.acronym || `Instituição ${institution.id}` }}
              </option>
            </select>
          </label>

          <label class="field">
            <span>Localidade</span>
            <input v-model="editForm.locality" type="text" />
          </label>

          <label class="field">
            <span>Data de início</span>
            <input v-model="editForm.startDate" type="date" />
          </label>

          <label class="field">
            <span>Data de fim</span>
            <input v-model="editForm.endDate" type="date" />
          </label>
        </div>

        <div v-if="editError" class="feedback feedback-error">{{ editError }}</div>
        <div v-if="editSuccess" class="feedback feedback-success">{{ editSuccess }}</div>

        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="closeEditModal">Fechar</button>
          <button type="button" class="btn-primary" :disabled="savingEdit" @click="saveClass">
            {{ savingEdit ? 'Salvando...' : 'Salvar alterações' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { classService } from '@/services/classService';
import { institutionService } from '@/services/institutionService';
import { programService } from '@/services/programService';

export default {
  name: 'ClassesView',
  setup() {
    const route = useRoute();
    const router = useRouter();

    const classes = ref([]);
    const programs = ref([]);
    const institutions = ref([]);
    const currentProgram = ref(null);
    const loading = ref(false);
    const error = ref('');
    const searchTerm = ref('');

    const showUploadModal = ref(false);
    const selectedFile = ref(null);
    const uploading = ref(false);
    const uploadError = ref('');
    const uploadSuccess = ref('');
    const fileInput = ref(null);

    const showEditModal = ref(false);
    const savingEdit = ref(false);
    const editError = ref('');
    const editSuccess = ref('');
    const selectedClassId = ref('');
    const editForm = ref({
      code: '',
      programId: '',
      locationId: '',
      locality: '',
      startDate: '',
      endDate: ''
    });

    const filterByProgram = computed(() => route.query.programId ? String(route.query.programId) : '');

    const filteredClasses = computed(() => {
      const term = searchTerm.value.trim().toLowerCase();

      return classes.value
        .filter((item) => {
          if (!filterByProgram.value) return true;
          return String(item.program?.id || '') === filterByProgram.value;
        })
        .filter((item) => {
          if (!term) return true;
          return [
            item.code,
            item.program?.name,
            item.locality,
            item.location?.name
          ]
            .filter(Boolean)
            .some((value) => String(value).toLowerCase().includes(term));
        });
    });

    const resetEditMessages = () => {
      editError.value = '';
      editSuccess.value = '';
    };

    const loadClasses = async () => {
      loading.value = true;
      error.value = '';

      try {
        const [classesData, programsData, institutionsData] = await Promise.all([
          classService.getAll(),
          programService.getAll(),
          institutionService.getAll()
        ]);

        classes.value = Array.isArray(classesData) ? classesData : [];
        programs.value = Array.isArray(programsData) ? programsData : [];
        institutions.value = Array.isArray(institutionsData) ? institutionsData : [];

        if (filterByProgram.value) {
          currentProgram.value = await programService.getById(filterByProgram.value);
        } else {
          currentProgram.value = null;
        }
      } catch (err) {
        error.value = `Erro ao carregar turmas: ${err.response?.data?.message || err.message}`;
      } finally {
        loading.value = false;
      }
    };

    const reloadData = async () => {
      await loadClasses();
    };

    const formatDate = (date) => {
      if (!date) return '-';
      return new Date(`${date}T00:00:00`).toLocaleDateString('pt-BR');
    };

    const formatDateInput = (date) => {
      if (!date) return '';
      return String(date).slice(0, 10);
    };

    const viewEnrollments = (classItem) => {
      router.push({ name: 'Enrollments', query: { classId: String(classItem.id) } });
    };

    const openClassDetails = (classItem) => {
      if (!classItem?.program?.id || !classItem?.id) return;
      router.push({
        name: 'ClassDetails',
        params: {
          programId: String(classItem.program.id),
          classId: String(classItem.id)
        }
      });
    };

    const editClass = (classItem) => {
      selectedClassId.value = String(classItem.id);
      editForm.value = {
        code: classItem.code || '',
        programId: classItem.program?.id ? String(classItem.program.id) : '',
        locationId: classItem.location?.id ? String(classItem.location.id) : '',
        locality: classItem.locality || '',
        startDate: formatDateInput(classItem.startDate),
        endDate: formatDateInput(classItem.endDate)
      };
      resetEditMessages();
      showEditModal.value = true;
    };

    const saveClass = async () => {
      if (!selectedClassId.value) return;

      if (!editForm.value.code || !editForm.value.programId || !editForm.value.locationId || !editForm.value.startDate || !editForm.value.endDate) {
        editError.value = 'Preencha código, programa, instituição e datas da turma.';
        return;
      }

      savingEdit.value = true;
      resetEditMessages();

      try {
        await classService.update(selectedClassId.value, {
          code: editForm.value.code.trim(),
          locality: editForm.value.locality.trim() || null,
          startDate: editForm.value.startDate,
          endDate: editForm.value.endDate,
          program: { id: Number(editForm.value.programId) },
          location: { id: Number(editForm.value.locationId) }
        });

        editSuccess.value = 'Turma atualizada com sucesso.';
        await loadClasses();
        setTimeout(() => {
          closeEditModal();
        }, 900);
      } catch (err) {
        editError.value = `Erro ao salvar turma: ${err.response?.data?.message || err.message}`;
      } finally {
        savingEdit.value = false;
      }
    };

    const closeEditModal = () => {
      showEditModal.value = false;
      selectedClassId.value = '';
      resetEditMessages();
    };

    const handleFileSelect = (event) => {
      selectedFile.value = event.target.files?.[0] || null;
      uploadError.value = '';
      uploadSuccess.value = '';
    };

    const uploadFile = async () => {
      if (!selectedFile.value) return;

      uploading.value = true;
      uploadError.value = '';
      uploadSuccess.value = '';

      try {
        await classService.importExcel(selectedFile.value);
        uploadSuccess.value = 'Arquivo importado com sucesso.';
        await loadClasses();
        setTimeout(() => {
          closeUploadModal();
        }, 900);
      } catch (err) {
        uploadError.value = `Erro ao importar arquivo: ${err.response?.data?.message || err.message}`;
      } finally {
        uploading.value = false;
      }
    };

    const closeUploadModal = () => {
      showUploadModal.value = false;
      selectedFile.value = null;
      uploadError.value = '';
      uploadSuccess.value = '';
      if (fileInput.value) {
        fileInput.value.value = '';
      }
    };

    onMounted(loadClasses);

    return {
      classes,
      programs,
      institutions,
      currentProgram,
      loading,
      error,
      searchTerm,
      filterByProgram,
      filteredClasses,
      showUploadModal,
      selectedFile,
      uploading,
      uploadError,
      uploadSuccess,
      fileInput,
      showEditModal,
      savingEdit,
      editError,
      editSuccess,
      editForm,
      formatDate,
      viewEnrollments,
      openClassDetails,
      editClass,
      saveClass,
      closeEditModal,
      handleFileSelect,
      uploadFile,
      closeUploadModal,
      reloadData
    };
  }
};
</script>

<style scoped>
.classes-view {
  padding: 24px;
}

.page-card {
  background: #ffffff;
  border: 1px solid #d9e2ef;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.06);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
  color: #102a43;
  font-size: 30px;
}

.subtitle {
  margin: 8px 0 0;
  color: #52606d;
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filters {
  margin-bottom: 14px;
}

.search-input,
.field input,
.field select {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 14px;
  color: #102a43;
  background: #ffffff;
}

.search-input:focus,
.field input:focus,
.field select:focus {
  outline: none;
  border-color: #2b6cb0;
  box-shadow: 0 0 0 3px rgba(43, 108, 176, 0.16);
}

.summary-row {
  margin-bottom: 18px;
}

.summary-pill {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 999px;
  background: #eef4ff;
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 600;
}

.table-container {
  border: 1px solid #e5edf5;
  border-radius: 18px;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: #f8fbff;
}

th,
td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid #edf2f7;
  vertical-align: middle;
}

th {
  font-size: 13px;
  font-weight: 700;
  color: #486581;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

td {
  color: #102a43;
}

tbody tr:hover {
  background: #f8fbff;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.btn-primary,
.btn-secondary,
.btn-view,
.btn-outline,
.btn-edit {
  border: none;
  border-radius: 12px;
  padding: 10px 14px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.btn-primary {
  background: #1d4ed8;
  color: #ffffff;
}

.btn-secondary {
  background: #e2e8f0;
  color: #1e293b;
}

.btn-view {
  background: #e0f2fe;
  color: #0c4a6e;
}

.btn-outline {
  background: #f8fafc;
  color: #1d4ed8;
  border: 1px solid #bfdbfe;
}

.btn-edit {
  background: #fef3c7;
  color: #92400e;
}

.btn-primary:hover,
.btn-secondary:hover,
.btn-view:hover,
.btn-outline:hover,
.btn-edit:hover {
  transform: translateY(-1px);
}

.btn-primary:disabled,
.btn-secondary:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  transform: none;
}

.state-box,
.no-data {
  padding: 32px 16px;
  text-align: center;
  color: #52606d;
}

.state-box-error {
  color: #b42318;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.54);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  z-index: 1000;
}

.modal-content {
  width: min(520px, 100%);
  background: #ffffff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 30px 60px rgba(15, 23, 42, 0.2);
}

.modal-content-wide {
  width: min(760px, 100%);
}

.modal-content h2 {
  margin: 0 0 8px;
  color: #102a43;
}

.modal-copy {
  margin: 0 0 18px;
  color: #52606d;
  line-height: 1.5;
}

.upload-area {
  border: 1.5px dashed #cbd5e1;
  border-radius: 16px;
  padding: 18px;
  text-align: center;
  margin-bottom: 18px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #334e68;
  font-size: 13px;
  font-weight: 600;
}

.feedback {
  margin-top: 16px;
  padding: 12px 14px;
  border-radius: 12px;
  font-size: 14px;
}

.feedback-error {
  background: #fff1f2;
  color: #b42318;
  border: 1px solid #fecdd3;
}

.feedback-success {
  background: #ecfdf3;
  color: #027a48;
  border: 1px solid #a7f3d0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .classes-view {
    padding: 16px;
  }

  .header {
    flex-direction: column;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
