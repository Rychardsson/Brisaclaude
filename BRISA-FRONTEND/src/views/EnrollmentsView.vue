<template>
  <div class="enrollments-view">
    <section class="page-card">
      <div class="header">
        <div>
          <h1>Matrículas</h1>
          <p v-if="filterByClass" class="subtitle">
            Turma: {{ currentClass?.code || 'Carregando...' }} - {{ currentClass?.program?.name || 'Programa' }}
          </p>
          <p v-else class="subtitle">
            Acompanhe vínculos de pessoas com turmas, papéis acadêmicos e situação atual.
          </p>
        </div>

        <div class="header-actions">
          <button
            v-if="filterByClass && currentClass?.program?.id && currentClass?.id"
            type="button"
            class="btn-secondary"
            @click="goToClassDetails"
          >
            Ver turma
          </button>
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
          placeholder="Buscar por nome, CPF ou email..."
          class="search-input"
        />

        <select v-model="filterRole" class="filter-select">
          <option value="">Todos os papéis</option>
          <option v-for="role in academicRoles" :key="role.id" :value="role.name">
            {{ formatRoleLabel(role.name) }}
          </option>
        </select>
      </div>

      <div class="summary-row">
        <span class="summary-pill">{{ filteredEnrollments.length }} matrícula(s)</span>
      </div>

      <div v-if="loading" class="state-box">Carregando matrículas...</div>
      <div v-else-if="error" class="state-box state-box-error">{{ error }}</div>

      <div v-else class="table-container">
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>CPF</th>
              <th>Email</th>
              <th>Papel</th>
              <th>Status</th>
              <th>Matrícula</th>
              <th>Conclusão</th>
              <th>Nota</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="enrollment in filteredEnrollments" :key="enrollment.id">
              <td>{{ enrollment.people?.name || '-' }}</td>
              <td>{{ formatCPF(enrollment.people?.cpf) }}</td>
              <td>{{ enrollment.people?.email || '-' }}</td>
              <td>
                <span :class="['role-badge', getRoleClass(enrollment.academicRole?.name)]">
                  {{ formatRoleLabel(enrollment.academicRole?.name) }}
                </span>
              </td>
              <td>
                <span :class="['status-badge', getStatusClass(enrollment.status)]">
                  {{ enrollment.status || 'Pendente' }}
                </span>
              </td>
              <td>{{ formatDate(enrollment.enrollmentDate) }}</td>
              <td>{{ formatDate(enrollment.completionDate) }}</td>
              <td>{{ formatGrade(enrollment.grade) }}</td>
              <td class="actions">
                <button type="button" class="btn-view" @click="openPerson(enrollment)">Pessoa</button>
                <button type="button" class="btn-edit" @click="editEnrollment(enrollment)">Editar</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="filteredEnrollments.length === 0" class="no-data">
          Nenhuma matrícula encontrada.
        </div>
      </div>
    </section>

    <div v-if="showUploadModal" class="modal-overlay" @click="closeUploadModal">
      <div class="modal-content" @click.stop>
        <h2>Importar matrículas via Excel</h2>
        <p class="modal-copy">
          Selecione uma planilha `.xlsx` ou `.xls` para criar ou atualizar matrículas em lote.
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
        <h2>Editar matrícula</h2>
        <p class="modal-copy">
          Ajuste status, papel acadêmico e datas da matrícula selecionada.
        </p>

        <div class="person-summary">
          <strong>{{ editingEnrollmentLabel }}</strong>
          <span>{{ editingEnrollmentMeta }}</span>
        </div>

        <div class="form-grid">
          <label class="field">
            <span>Papel acadêmico</span>
            <select v-model="editForm.roleId">
              <option value="">Selecione</option>
              <option v-for="role in academicRoles" :key="role.id" :value="String(role.id)">
                {{ formatRoleLabel(role.name) }}
              </option>
            </select>
          </label>

          <label class="field">
            <span>Status</span>
            <select v-model="editForm.status">
              <option value="">Selecione</option>
              <option v-for="status in statusOptions" :key="status" :value="status">
                {{ status }}
              </option>
            </select>
          </label>

          <label class="field">
            <span>Data da matrícula</span>
            <input v-model="editForm.enrollmentDate" type="date" />
          </label>

          <label class="field">
            <span>Data de conclusão</span>
            <input v-model="editForm.completionDate" type="date" />
          </label>

          <label class="field field-full">
            <span>Nota final</span>
            <input v-model="editForm.grade" type="number" min="0" max="10" step="0.1" />
          </label>
        </div>

        <div v-if="editError" class="feedback feedback-error">{{ editError }}</div>
        <div v-if="editSuccess" class="feedback feedback-success">{{ editSuccess }}</div>

        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="closeEditModal">Fechar</button>
          <button type="button" class="btn-primary" :disabled="savingEdit" @click="saveEnrollment">
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
import { academicRoleService } from '@/services/academicRoleService';
import { classService } from '@/services/classService';
import { enrollmentService } from '@/services/enrollmentService';

const DEFAULT_STATUS_OPTIONS = ['Pendente', 'Em andamento', 'Realizado', 'Não-realizado'];

export default {
  name: 'EnrollmentsView',
  setup() {
    const route = useRoute();
    const router = useRouter();

    const enrollments = ref([]);
    const academicRoles = ref([]);
    const currentClass = ref(null);
    const loading = ref(false);
    const error = ref('');
    const searchTerm = ref('');
    const filterRole = ref('');

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
    const selectedEnrollmentId = ref('');
    const editForm = ref({
      roleId: '',
      status: '',
      enrollmentDate: '',
      completionDate: '',
      grade: ''
    });

    const filterByClass = computed(() => route.query.classId ? String(route.query.classId) : '');

    const filteredEnrollments = computed(() => {
      const term = searchTerm.value.trim().toLowerCase();

      return enrollments.value.filter((item) => {
        if (filterRole.value && item.academicRole?.name !== filterRole.value) {
          return false;
        }

        if (!term) return true;

        return [
          item.people?.name,
          item.people?.cpf,
          item.people?.email
        ]
          .filter(Boolean)
          .some((value) => String(value).toLowerCase().includes(term));
      });
    });

    const statusOptions = computed(() => {
      const known = new Set(DEFAULT_STATUS_OPTIONS);
      enrollments.value.forEach((item) => {
        if (item?.status) known.add(item.status);
      });
      return Array.from(known.values());
    });

    const editingEnrollment = computed(() =>
      enrollments.value.find((item) => String(item.id) === selectedEnrollmentId.value) || null
    );

    const editingEnrollmentLabel = computed(() =>
      editingEnrollment.value?.people?.name || 'Matrícula selecionada'
    );

    const editingEnrollmentMeta = computed(() => {
      const role = formatRoleLabel(editingEnrollment.value?.academicRole?.name);
      const classCode = editingEnrollment.value?.classModel?.code || currentClass.value?.code || '-';
      return `${role} • ${classCode}`;
    });

    const resetEditMessages = () => {
      editError.value = '';
      editSuccess.value = '';
    };

    const loadEnrollments = async () => {
      loading.value = true;
      error.value = '';

      try {
        const requests = [
          academicRoleService.getAll().catch(() => [])
        ];

        if (filterByClass.value) {
          requests.push(enrollmentService.getByClassId(filterByClass.value));
          requests.push(classService.getById(filterByClass.value));
        } else {
          requests.push(enrollmentService.getAll());
          requests.push(Promise.resolve(null));
        }

        const [rolesData, enrollmentsData, classData] = await Promise.all(requests);

        academicRoles.value = Array.isArray(rolesData) ? rolesData : [];
        enrollments.value = Array.isArray(enrollmentsData) ? enrollmentsData : [];
        currentClass.value = classData;
      } catch (err) {
        error.value = `Erro ao carregar matrículas: ${err.response?.data?.message || err.message}`;
      } finally {
        loading.value = false;
      }
    };

    const reloadData = async () => {
      await loadEnrollments();
    };

    const formatCPF = (cpf) => {
      if (!cpf) return '-';
      const digits = String(cpf).replace(/\D/g, '');
      if (digits.length !== 11) return cpf;
      return digits.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    };

    const formatDate = (date) => {
      if (!date) return '-';
      return new Date(`${date}T00:00:00`).toLocaleDateString('pt-BR');
    };

    const formatDateInput = (date) => {
      if (!date) return '';
      return String(date).slice(0, 10);
    };

    const formatGrade = (grade) => {
      if (grade === null || grade === undefined || grade === '') return '-';
      return Number(grade).toLocaleString('pt-BR', { minimumFractionDigits: 0, maximumFractionDigits: 1 });
    };

    const formatRoleLabel = (role) => {
      if (!role) return '-';
      const normalized = String(role).toUpperCase();
      if (normalized === 'ALUNO') return 'Aluno';
      if (normalized === 'PROFESSOR') return 'Professor';
      if (normalized === 'ORIENTADOR') return 'Orientador';
      if (normalized === 'COORDENADOR') return 'Coordenador';
      return role;
    };

    const getRoleClass = (role) => {
      const normalized = String(role || '').toUpperCase();
      if (normalized === 'ALUNO') return 'role-student';
      if (normalized === 'PROFESSOR') return 'role-teacher';
      if (normalized === 'ORIENTADOR') return 'role-advisor';
      if (normalized === 'COORDENADOR') return 'role-coordinator';
      return 'role-default';
    };

    const getStatusClass = (status) => {
      const normalized = String(status || '').toLowerCase();
      if (normalized === 'realizado') return 'status-completed';
      if (normalized === 'em andamento') return 'status-in-progress';
      if (normalized === 'não-realizado' || normalized === 'nao-realizado') return 'status-not-completed';
      return 'status-pending';
    };

    const openPerson = (enrollment) => {
      if (!enrollment?.people?.id) return;
      router.push({ name: 'PersonDetails', params: { id: String(enrollment.people.id) } });
    };

    const goToClassDetails = () => {
      if (!currentClass.value?.program?.id || !currentClass.value?.id) return;
      router.push({
        name: 'ClassDetails',
        params: {
          programId: String(currentClass.value.program.id),
          classId: String(currentClass.value.id)
        },
        query: {
          tab: 'pessoas'
        }
      });
    };

    const editEnrollment = (enrollment) => {
      selectedEnrollmentId.value = String(enrollment.id);
      editForm.value = {
        roleId: enrollment.academicRole?.id ? String(enrollment.academicRole.id) : '',
        status: enrollment.status || '',
        enrollmentDate: formatDateInput(enrollment.enrollmentDate),
        completionDate: formatDateInput(enrollment.completionDate),
        grade: enrollment.grade ?? ''
      };
      resetEditMessages();
      showEditModal.value = true;
    };

    const saveEnrollment = async () => {
      if (!selectedEnrollmentId.value) return;

      if (!editForm.value.roleId || !editForm.value.enrollmentDate) {
        editError.value = 'Informe pelo menos o papel acadêmico e a data da matrícula.';
        return;
      }

      savingEdit.value = true;
      resetEditMessages();

      try {
        await enrollmentService.update(selectedEnrollmentId.value, {
          academicRole: { id: Number(editForm.value.roleId) },
          enrollmentDate: editForm.value.enrollmentDate,
          status: editForm.value.status || null,
          completionDate: editForm.value.completionDate || null,
          grade: editForm.value.grade === '' ? null : Number(editForm.value.grade)
        });

        editSuccess.value = 'Matrícula atualizada com sucesso.';
        await loadEnrollments();
        setTimeout(() => {
          closeEditModal();
        }, 900);
      } catch (err) {
        editError.value = `Erro ao salvar matrícula: ${err.response?.data?.message || err.message}`;
      } finally {
        savingEdit.value = false;
      }
    };

    const closeEditModal = () => {
      showEditModal.value = false;
      selectedEnrollmentId.value = '';
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
        await enrollmentService.importExcel(selectedFile.value);
        uploadSuccess.value = 'Arquivo importado com sucesso.';
        await loadEnrollments();
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

    onMounted(loadEnrollments);

    return {
      academicRoles,
      currentClass,
      loading,
      error,
      searchTerm,
      filterRole,
      filterByClass,
      filteredEnrollments,
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
      editingEnrollmentLabel,
      editingEnrollmentMeta,
      statusOptions,
      formatCPF,
      formatDate,
      formatGrade,
      formatRoleLabel,
      getRoleClass,
      getStatusClass,
      openPerson,
      goToClassDetails,
      editEnrollment,
      saveEnrollment,
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
.enrollments-view {
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
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  gap: 12px;
  margin-bottom: 14px;
}

.search-input,
.filter-select,
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
.filter-select:focus,
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
  white-space: nowrap;
}

td {
  color: #102a43;
}

tbody tr:hover {
  background: #f8fbff;
}

.role-badge,
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.role-student {
  background: #dbeafe;
  color: #1d4ed8;
}

.role-teacher {
  background: #ede9fe;
  color: #6d28d9;
}

.role-advisor {
  background: #dcfce7;
  color: #15803d;
}

.role-coordinator {
  background: #ffedd5;
  color: #c2410c;
}

.role-default {
  background: #e2e8f0;
  color: #334155;
}

.status-completed {
  background: #dcfce7;
  color: #15803d;
}

.status-in-progress {
  background: #fef3c7;
  color: #a16207;
}

.status-not-completed {
  background: #fee2e2;
  color: #b91c1c;
}

.status-pending {
  background: #e2e8f0;
  color: #334155;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.btn-primary,
.btn-secondary,
.btn-view,
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

.btn-edit {
  background: #fef3c7;
  color: #92400e;
}

.btn-primary:hover,
.btn-secondary:hover,
.btn-view:hover,
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

.person-summary {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px 16px;
  border-radius: 14px;
  background: #f8fbff;
  border: 1px solid #dbeafe;
  margin-bottom: 18px;
}

.person-summary strong {
  color: #102a43;
}

.person-summary span {
  color: #52606d;
  font-size: 14px;
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

.field-full {
  grid-column: 1 / -1;
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
  .enrollments-view {
    padding: 16px;
  }

  .header {
    flex-direction: column;
  }

  .filters {
    grid-template-columns: 1fr;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .field-full {
    grid-column: auto;
  }
}
</style>
