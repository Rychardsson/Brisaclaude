<template>
  <div class="stage-details-view">
    <ConfirmDialog ref="confirmDialog" />
    <div v-if="loading" class="loading">Carregando...</div>
    
    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else>
      <!-- Cabeçalho da Etapa -->
      <div class="stage-header">
        <button @click="goBack" class="btn-back">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          Voltar
        </button>
        <div class="stage-info">
          <h1>{{ displayStageName(stageData.name) }}</h1>
          <p v-if="stageData.description" class="stage-description">{{ stageData.description }}</p>
          <div class="stage-meta">
            <span class="meta-item"><strong>Turma:</strong> {{ stageData.className }}</span>
            <span class="meta-item"><strong>Total de Candidatos:</strong> {{ candidates.length }}</span>
          </div>
        </div>
      </div>

      <!-- Candidatos da Etapa -->
      <div class="candidates-section">
        <div class="section-header">
          <h2>Candidatos</h2>
          <div class="header-actions">
            <button v-if="isNivelamento && candidates.length > 0" @click="openCourses" class="btn-primary">
              Abrir Cursos
            </button>
            <button v-if="isNivelamento" @click="openAddCoursesModal" class="btn-secondary">
              Adicionar Cursos
            </button>
            <button @click="showImportModal = true" class="btn-import">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="17 8 12 3 7 8"></polyline>
                <line x1="12" y1="3" x2="12" y2="15"></line>
              </svg>
              Importar
            </button>
            <button @click="showAddCandidateModal = true" class="btn-primary">
              + Adicionar
            </button>
          </div>
        </div>

        <div v-if="candidates.length === 0" class="no-data">
          Nenhum candidato adicionado a esta etapa
        </div>

        <div v-else class="table-container">
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>Email</th>
                <th>CPF</th>
                <th>Status</th>
                <th>Observações</th>
                <th>Data Adição</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="candidate in candidates" :key="candidate.id">
                <td>{{ candidate.peopleName }}</td>
                <td>{{ getPeopleEmail(candidate.peopleId) }}</td>
                <td>{{ getPeopleCPF(candidate.peopleId) }}</td>
                <td>
                  <select 
                    :value="candidate.status" 
                    @change="updateCandidateStatus(candidate.id, $event.target.value)"
                    class="status-select"
                    :class="getStatusClass(candidate.status)"
                  >
                    <option value="APROVADO">Aprovado</option>
                    <option value="REPROVADO">Reprovado</option>
                  </select>
                </td>
                <td>
                  <input 
                    type="text" 
                    :value="candidate.notes || ''" 
                    @blur="updateCandidateNotes(candidate.id, $event.target.value)"
                    class="notes-input"
                    placeholder="Adicionar observação..."
                  />
                </td>
                <td>{{ formatDate(candidate.createdAt) }}</td>
                <td class="actions">
                  <button @click="removeCandidate(candidate.id)" class="btn-delete-small">Remover</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Modal combinado: Importar via Excel ou Adicionar Manualmente -->
    <div v-if="showAddCandidatesModal" class="modal-overlay" @click="closeAddCandidatesModal">
      <div class="modal-content" @click.stop>
        <h2>Adicionar Candidatos (Importar ou Manual)</h2>

        <div class="form-group">
          <label>Importar via Excel</label>
          <div class="upload-area">
            <input type="file" @change="handleFileChange" accept=".xlsx,.xls" ref="fileInput" />
            <p v-if="selectedFile">{{ selectedFile.name }}</p>
          </div>
          <div class="modal-actions">
            <button @click="closeAddCandidatesModal" class="btn-secondary">Fechar</button>
            <button @click="importExcel" :disabled="!selectedFile || importing" class="btn-primary">{{ importing ? 'Enviando...' : 'Importar' }}</button>
          </div>
          <div v-if="importError" class="error">{{ importError }}</div>
          <div v-if="importSuccess" class="alert alert-success">
            <div>{{ importSuccessMessage }}</div>
          </div>
        </div>

        <hr />

        <div class="form-group">
          <label>Adicionar Manualmente</label>
          <div class="search-container" ref="searchContainerRef">
            <input 
              type="text"
              v-model="searchPeopleQuery"
              @input="handleSearchInput"
              @focus="showSearchResults = true"
              class="form-input search-input"
              placeholder="Digite o nome, email ou CPF..."
            />
            <div v-if="showSearchResults && filteredPeople.length > 0" class="search-results">
              <div 
                v-for="person in filteredPeople" 
                :key="person.id"
                @click="selectPerson(person)"
                class="search-result-item"
              >
                <div class="result-name">{{ person.name }}</div>
                <div class="result-details">{{ person.email }} - {{ formatCPF(person.cpf) }}</div>
              </div>
            </div>
            <div v-if="showSearchResults && searchPeopleQuery && filteredPeople.length === 0" class="search-no-results">
              Nenhuma pessoa encontrada
            </div>
          </div>
          <div v-if="selectedPerson" class="selected-person">
            <strong>Selecionado:</strong> {{ selectedPerson.name }} - {{ selectedPerson.email }}
            <button @click="clearSelection" class="btn-clear">×</button>
          </div>
          <div class="form-group">
            <label for="candidateStatus">Status *</label>
            <select id="candidateStatus" v-model="newCandidate.status" class="form-input">
              <option value="APROVADO">Aprovado</option>
              <option value="REPROVADO">Reprovado</option>
            </select>
          </div>
          <div class="form-group">
            <label for="candidateNotes">Observações</label>
            <textarea id="candidateNotes" v-model="newCandidate.notes" class="form-input" rows="3" placeholder="Observações sobre o candidato (opcional)"></textarea>
          </div>
          <div class="modal-actions">
            <button @click="closeAddCandidatesModal" class="btn-secondary">Cancelar</button>
            <button @click="addCandidate" :disabled="!newCandidate.peopleId || !newCandidate.status || addingCandidate" class="btn-primary">{{ addingCandidate ? 'Adicionando...' : 'Adicionar' }}</button>
          </div>
          <div v-if="candidateError" class="error">{{ candidateError }}</div>
        </div>
      </div>
    </div>

    <!-- Modal de Adicionar Candidato -->
    <div v-if="showAddCandidateModal" class="modal-overlay" @click="closeAddCandidateModal">
      <div class="modal-content" @click.stop>
        <h2>Adicionar Candidato</h2>
        <div class="form-group">
          <label for="candidatePeople">Pesquisar Pessoa *</label>
          <div class="search-container" ref="searchContainerRef">
            <input 
              type="text"
              v-model="searchPeopleQuery"
              @input="handleSearchInput"
              @focus="showSearchResults = true"
              class="form-input search-input"
              placeholder="Digite o nome, email ou CPF..."
            />
            <div v-if="showSearchResults && filteredPeople.length > 0" class="search-results">
              <div 
                v-for="person in filteredPeople" 
                :key="person.id"
                @click="selectPerson(person)"
                class="search-result-item"
              >
                <div class="result-name">{{ person.name }}</div>
                <div class="result-details">{{ person.email }} - {{ formatCPF(person.cpf) }}</div>
              </div>
            </div>
            <div v-if="showSearchResults && searchPeopleQuery && filteredPeople.length === 0" class="search-no-results">
              Nenhuma pessoa encontrada
            </div>
          </div>
          <div v-if="selectedPerson" class="selected-person">
            <strong>Selecionado:</strong> {{ selectedPerson.name }} - {{ selectedPerson.email }}
            <button @click="clearSelection" class="btn-clear">×</button>
          </div>
        </div>
        <div class="form-group">
          <label for="candidateStatus">Status *</label>
          <select 
            id="candidateStatus"
            v-model="newCandidate.status" 
            class="form-input"
          >
            <option value="APROVADO">Aprovado</option>
            <option value="REPROVADO">Reprovado</option>
          </select>
        </div>
        <div class="form-group">
          <label for="candidateNotes">Observações</label>
          <textarea 
            id="candidateNotes"
            v-model="newCandidate.notes" 
            class="form-input"
            rows="3"
            placeholder="Observações sobre o candidato (opcional)"
          ></textarea>
        </div>
        <div class="modal-actions">
          <button @click="closeAddCandidateModal" class="btn-secondary">Cancelar</button>
          <button @click="addCandidate" :disabled="!newCandidate.peopleId || !newCandidate.status || addingCandidate" class="btn-primary">
            {{ addingCandidate ? 'Adicionando...' : 'Adicionar' }}
          </button>
        </div>
        <div v-if="candidateError" class="error">{{ candidateError }}</div>
      </div>
    </div>

    <!-- Modal de Importar Excel -->
    <div v-if="showImportModal" class="modal-overlay" @click="closeImportModal">
      <div class="modal-content" @click.stop>
        <h2>Importar Candidatos via Excel</h2>
        <div class="upload-area">
          <input 
            type="file"
            @change="handleFileChange"
            accept=".xlsx,.xls"
            ref="fileInput"
          />
          <p v-if="selectedFile">{{ selectedFile.name }}</p>
        </div>
        <div class="modal-actions">
          <button @click="closeImportModal" class="btn-secondary">Cancelar</button>
          <button @click="importExcel" :disabled="!selectedFile || importing" class="btn-primary">
            {{ importing ? 'Enviando...' : 'Enviar' }}
          </button>
        </div>
        <div v-if="importError || (rowErrors && rowErrors.length > 0)" class="alert alert-error">
          <button @click="importError = null; rowErrors = []" class="btn-close-alert">×</button>
          <div class="alert-scrollable">
            <div class="alert-content">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              <div class="alert-message">
                <div v-if="importError">{{ importError }}</div>
                <div v-if="rowErrors && rowErrors.length > 0" style="margin-top:8px;">
                  <strong>Erros no arquivo (linhas não importadas):</strong>
                  <ul style="margin-top:6px;">
                    <li v-for="(err, index) in rowErrors" :key="index">
                      Linha {{ err.row }}: <span v-for="(m,mi) in err.messages" :key="mi">{{ m }}<span v-if="mi < err.messages.length -1">; </span></span>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="importSuccess" class="alert alert-success">
          <button @click="closeSuccessAlert" class="btn-close-alert">×</button>
          <div class="alert-scrollable">
            <div class="alert-content">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
              <div class="alert-message">
                <div>{{ importSuccessMessage }}</div>
                <div v-if="importedPeopleNames && importedPeopleNames.length > 0" class="imported-people">
                  <strong>Pessoas inseridas no sistema:</strong>
                  <ul>
                    <li v-for="(person, index) in importedPeopleNames" :key="index">{{ person }}</li>
                  </ul>
                </div>
                <div v-if="duplicatePeopleNames && duplicatePeopleNames.length > 0" class="duplicate-people">
                  <strong>Candidatos que já estavam na etapa:</strong>
                  <ul>
                    <li v-for="(person, index) in duplicatePeopleNames" :key="index">{{ person }}</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- Modal: Adicionar Cursos à Turma -->
    <div v-if="showAddCoursesModal" class="modal-overlay" @click="closeAddCoursesModal">
      <div class="modal-content" @click.stop>
        <h2>Adicionar Cursos à Turma</h2>
        <div class="upload-area" style="margin-top:0;margin-bottom:16px;padding:20px;">
          <input type="file" accept=".xlsx,.xls" @change="handleCourseExcelFileChange" />
          <p v-if="selectedCourseExcelFile">{{ selectedCourseExcelFile.name }}</p>
          <div class="modal-actions" style="margin-top:12px;padding-top:0;border-top:none;">
            <button
              @click="importCoursesExcelToClass"
              :disabled="importingCourseExcel || !selectedCourseExcelFile"
              class="btn-primary"
            >{{ importingCourseExcel ? 'Importando...' : 'Importar Cursos por Excel' }}</button>
          </div>
          <div v-if="courseExcelImportError" class="error" style="padding:8px 0 0 0;text-align:left;">{{ courseExcelImportError }}</div>
          <div v-if="courseExcelImportSuccess" class="alert alert-success" style="margin-top:12px;">{{ courseExcelImportSuccess }}</div>
        </div>

        <div v-if="loadingModalCourses" class="loading"> Carregando cursos...</div>
        <div v-else>
          <div class="courses-selection-list">
            <div v-for="course in modalCourses" :key="course.id" :class="['course-row', { assigned: course.assigned }]" style="display:flex;align-items:center;gap:12px;padding:12px 0;border-bottom:1px solid #f0f0f0;">
              <div style="flex:1; display:flex; gap:12px; align-items:center;">
                <input v-if="!course.assigned" type="checkbox" :value="course.id" @change="toggleCourseSelection(course.id, $event.target.checked)" :checked="selectedCourseIds.includes(course.id)" />
                <div>
                  <div style="font-weight:700;color:#1F285F">{{ course.name }}</div>
                  <div style="font-size:13px;color:#666">{{ course.knowledgeArea }}</div>
                </div>
              </div>
              <div style="display:flex;align-items:center;gap:12px;">
                <div v-if="!course.assigned">
                  <label style="font-size:13px;color:#444"><input type="checkbox" :checked="selectedRequired[course.id] !== false" @change="setRequired(course.id, $event.target.checked)" /> Obrigatório</label>
                </div>
                <div v-else style="color:#888;font-size:13px;padding:6px 10px;border-radius:8px;background:#f4f6fb">
                  Adicionado <span v-if="course.required">(Obrigatório)</span><span v-else>(Opcional)</span>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-actions" style="margin-top:16px;">
            <button @click="closeAddCoursesModal" class="btn-secondary">Cancelar</button>
            <button @click="addSelectedCourses" :disabled="addingCourses || selectedCourseIds.length===0" class="btn-primary">{{ addingCourses ? 'Adicionando...' : 'Adicionar Selecionados' }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { stageService } from '@/services/stageService';
import { peopleService } from '@/services/peopleService';
import { classService } from '@/services/classService';
import { courseService } from '@/services/courseService';
import ConfirmDialog from '@/components/ConfirmDialog.vue';

export default {
  name: 'StageDetailsView',
  components: { ConfirmDialog },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const stageData = ref({});
    const candidates = ref([]);
    const allPeople = ref([]);
    const loading = ref(false);
    const error = ref(null);
    const confirmDialog = ref();

    const showAddCandidateModal = ref(false);
    const showAddCandidatesModal = ref(false); // combined modal: import + manual add
    const addingCandidate = ref(false);
    const candidateError = ref(null);

    const searchPeopleQuery = ref('');
    const showSearchResults = ref(false);
    const selectedPerson = ref(null);
    const searchContainerRef = ref(null);

    const showImportModal = ref(false);
    const importing = ref(false);
    const importError = ref(null);
    const importSuccess = ref(null);
    const importSuccessMessage = ref('');
    const importedPeopleNames = ref([]);
    const duplicatePeopleNames = ref([]);
    const rowErrors = ref([]);
    const selectedFile = ref(null);
    const fileInput = ref(null);

    const newCandidate = ref({
      peopleId: '',
      status: 'APROVADO',
      notes: ''
    });

    const programId = computed(() => route.params.programId);
    const classId = computed(() => route.params.classId);
    const stageId = computed(() => route.params.stageId);

    const availablePeople = computed(() => {
      const candidateIds = candidates.value.map(c => c.peopleId);
      return allPeople.value.filter(p => !candidateIds.includes(p.id));
    });

    const filteredPeople = computed(() => {
      if (!searchPeopleQuery.value) return availablePeople.value;
      const query = searchPeopleQuery.value.toLowerCase();
      return availablePeople.value.filter(p => 
        p.name?.toLowerCase().includes(query) ||
        p.email?.toLowerCase().includes(query) ||
        p.cpf?.replace(/\D/g, '').includes(query.replace(/\D/g, ''))
      ).slice(0, 10); // Limitar a 10 resultados
    });

    const loadStageDetails = async () => {
      loading.value = true;
      error.value = null;
      try {
        stageData.value = await stageService.getById(stageId.value);
        candidates.value = await stageService.getCandidatesByStageId(stageId.value);
        allPeople.value = await peopleService.getAll();
      } catch (err) {
        error.value = 'Erro ao carregar detalhes da etapa: ' + (err.response?.data?.message || err.message);
      } finally {
        loading.value = false;
      }
    };

    const getPeopleEmail = (peopleId) => {
      const person = allPeople.value.find(p => p.id === peopleId);
      return person?.email || '-';
    };

    const getPeopleCPF = (peopleId) => {
      const person = allPeople.value.find(p => p.id === peopleId);
      if (!person?.cpf) return '-';
      return person.cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    };

    const handleSearchInput = () => {
      showSearchResults.value = true;
    };

    const selectPerson = (person) => {
      selectedPerson.value = person;
      newCandidate.value.peopleId = person.id;
      searchPeopleQuery.value = person.name;
      showSearchResults.value = false;
    };

    const clearSelection = () => {
      selectedPerson.value = null;
      newCandidate.value.peopleId = '';
      searchPeopleQuery.value = '';
    };

    const formatCPF = (cpf) => {
      if (!cpf) return '';
      return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    };

    const addCandidate = async () => {
      if (!selectedPerson.value) {
        candidateError.value = 'Por favor, selecione uma pessoa';
        return;
      }

      addingCandidate.value = true;
      candidateError.value = null;

      try {
        await stageService.addCandidate({
          stageId: stageId.value,
          peopleId: newCandidate.value.peopleId,
          status: newCandidate.value.status,
          notes: newCandidate.value.notes
        });
        await loadStageDetails();
        // If we were opened via the combined modal, close the combined modal so the header/button becomes visible
        if (showAddCandidatesModal.value) {
          closeAddCandidatesModal();
        } else {
          closeAddCandidateModal();
        }
      } catch (err) {
        candidateError.value = 'Erro ao adicionar candidato: ' + (err.response?.data?.message || err.message);
      } finally {
        addingCandidate.value = false;
      }
    };

    const handleFileChange = (event) => {
      const file = event.target.files[0];
      if (file) {
        selectedFile.value = file;
        importError.value = null;
        importSuccess.value = null;
      }
    };

    const importExcel = async () => {
      if (!selectedFile.value) {
        importError.value = 'Por favor, selecione um arquivo';
        return;
      }

      importing.value = true;
      importError.value = null;
      importSuccess.value = null;
      importSuccessMessage.value = '';
      importedPeopleNames.value = [];
      duplicatePeopleNames.value = [];

      try {
        const formData = new FormData();
        formData.append('file', selectedFile.value);
        
        const response = await stageService.importCandidates(stageId.value, formData);

        // Registra erros por linha, se houver
        rowErrors.value = response.rowErrors || [];

        // Reset previous state
        importError.value = null;
        importSuccess.value = false;
        importSuccessMessage.value = '';
        importedPeopleNames.value = [];
        duplicatePeopleNames.value = [];

        // Monta mensagem de sucesso com detalhes (apenas para itens importados)
        let successMessage = `${response.successfullyInserted} candidato(s) importado(s) com sucesso!`;

        if (response.newPeopleCreated > 0) {
          successMessage += ` ${response.newPeopleCreated} pessoa(s) nova(s) inserida(s) no sistema.`;
          importedPeopleNames.value = response.createdPeople || [];
        }

        if (response.alreadyInStage > 0) {
          successMessage += ` ${response.alreadyInStage} já estava(m) nesta etapa.`;
          duplicatePeopleNames.value = response.duplicateCandidates || [];
        }

        if (response.successfullyInserted > 0) {
          importSuccess.value = true;
          importSuccessMessage.value = successMessage;
        }

        // Se houver erros por linha, converte para erro legível e armazena em importError (mantendo detalhes em rowErrors)
        if (rowErrors.value.length > 0) {
          importError.value = rowErrors.value
            .map(err => `Linha ${err.row}: ${err.messages.join('; ')}`)
            .join(' | ');
        } else {
          importError.value = null;
        }

        // Recarrega a lista de candidatos
        await loadStageDetails();

        // Se houver erros por linha, mantém o modal aberto para correção; caso contrário fecha-o
        if (rowErrors.value.length === 0) {
          if (showAddCandidatesModal.value) {
            closeAddCandidatesModal();
          } else {
            closeImportModal();
          }
        }
      } catch (err) {
        importError.value = 'Erro ao importar arquivo: ' + (err.response?.data?.message || err.message);
      } finally {
        importing.value = false;
      }
    };

    const closeSuccessAlert = () => {
      importSuccess.value = null;
      importSuccessMessage.value = '';
      importedPeopleNames.value = [];
      duplicatePeopleNames.value = [];
    };

    const closeImportModal = () => {
      showImportModal.value = false;
      selectedFile.value = null;
      importError.value = null;
      importSuccess.value = null;
      importSuccessMessage.value = '';
      importedPeopleNames.value = [];
      duplicatePeopleNames.value = [];
      if (fileInput.value) {
        fileInput.value.value = '';
      }
    };

    // ── Modal: Adicionar Cursos (nivelamento) ─────────────────────────────────
    const showAddCoursesModal = ref(false);
    const modalCourses = ref([]);
    const loadingModalCourses = ref(false);
    const addingCourses = ref(false);
    const selectedCourseIds = ref([]);
    const selectedRequired = ref({});
    const selectedCourseExcelFile = ref(null);
    const importingCourseExcel = ref(false);
    const courseExcelImportError = ref(null);
    const courseExcelImportSuccess = ref('');

    const openAddCoursesModal = async () => {
      showAddCoursesModal.value = true;
      await loadModalCourses();
    };

    const loadModalCourses = async () => {
      loadingModalCourses.value = true;
      try {
        const all = await courseService.getAll().catch(() => []);
        const assignments = await courseService.getAssignmentsByClassId(classId.value).catch(() => []);
        modalCourses.value = (Array.isArray(all) ? all : []).map(c => {
          const a = (assignments || []).find(x => x.course?.id === c.id);
          return {
            id: c.id,
            name: c.name,
            description: c.description,
            knowledgeArea: c.knowledgeArea?.name,
            assigned: !!a,
            required: a ? a.required : true
          };
        });
        modalCourses.value.forEach(c => {
          if (c.assigned) selectedRequired.value[c.id] = c.required;
          else if (selectedRequired.value[c.id] === undefined) selectedRequired.value[c.id] = true;
        });
      } catch (err) {
        console.error('Erro ao carregar cursos para modal:', err);
        modalCourses.value = [];
      } finally {
        loadingModalCourses.value = false;
      }
    };

    const toggleCourseSelection = (courseId, checked) => {
      const idx = selectedCourseIds.value.indexOf(courseId);
      if (checked) {
        if (idx === -1) selectedCourseIds.value.push(courseId);
      } else {
        if (idx !== -1) selectedCourseIds.value.splice(idx, 1);
      }
    };

    const setRequired = (courseId, checked) => {
      selectedRequired.value[courseId] = checked;
    };

    const handleCourseExcelFileChange = (event) => {
      const file = event.target.files[0];
      selectedCourseExcelFile.value = file || null;
      courseExcelImportError.value = null;
      courseExcelImportSuccess.value = '';
    };

    const importCoursesExcelToClass = async () => {
      if (!classId.value || !selectedCourseExcelFile.value) return;
      importingCourseExcel.value = true;
      courseExcelImportError.value = null;
      courseExcelImportSuccess.value = '';
      try {
        const formData = new FormData();
        formData.append('file', selectedCourseExcelFile.value);
        const result = await courseService.importCoursesToClassFromExcel(classId.value, formData);
        courseExcelImportSuccess.value =
          `Processadas: ${result.totalProcessed}. ` +
          `Cursos criados: ${result.createdCourses}. ` +
          `Cursos adicionados à turma: ${result.assignedCourses}. ` +
          `Já adicionados: ${result.alreadyAssigned}. ` +
          `Ignoradas: ${result.skippedRows}.`;
        await loadModalCourses();
      } catch (err) {
        courseExcelImportError.value = 'Erro ao importar cursos: ' + (err.response?.data?.message || err.message);
      } finally {
        importingCourseExcel.value = false;
      }
    };

    const addSelectedCourses = async () => {
      if (!classId.value) return;
      if (!selectedCourseIds.value.length) return;
      addingCourses.value = true;
      try {
        await Promise.all(selectedCourseIds.value.map(id => {
          const req = selectedRequired.value[id] === undefined ? true : !!selectedRequired.value[id];
          return courseService.assignCourseToClass(id, classId.value, req);
        }));
        await loadStageDetails();
        await loadModalCourses();
        selectedCourseIds.value = [];
        showAddCoursesModal.value = false;
      } catch (err) {
        console.error('Erro ao adicionar cursos:', err);
        alert('Erro ao adicionar cursos: ' + (err.response?.data?.message || err.message));
      } finally {
        addingCourses.value = false;
      }
    };

    const closeAddCoursesModal = () => {
      showAddCoursesModal.value = false;
      selectedCourseIds.value = [];
      selectedRequired.value = {};
      selectedCourseExcelFile.value = null;
      importingCourseExcel.value = false;
      courseExcelImportError.value = null;
      courseExcelImportSuccess.value = '';
    };

    const updateCandidateStatus = async (candidateId, newStatus) => {
      try {
        await stageService.updateCandidate(candidateId, { status: newStatus });
        await loadStageDetails();
      } catch (err) {
        alert('Erro ao atualizar status: ' + (err.response?.data?.message || err.message));
      }
    };

    const updateCandidateNotes = async (candidateId, notes) => {
      try {
        await stageService.updateCandidate(candidateId, { notes });
      } catch (err) {
        alert('Erro ao atualizar observações: ' + (err.response?.data?.message || err.message));
      }
    };

    const removeCandidate = async (candidateId) => {
      const confirmed = await confirmDialog.value.show('Tem certeza que deseja remover este candidato?', 'Remover');
      if (!confirmed) return;

      try {
        await stageService.deleteCandidate(candidateId);
        await loadStageDetails();
      } catch (err) {
        alert('Erro ao remover candidato: ' + (err.response?.data?.message || err.message));
      }
    };

    const closeAddCandidateModal = () => {
      showAddCandidateModal.value = false;
      newCandidate.value = { peopleId: '', status: 'APROVADO', notes: '' };
      candidateError.value = null;
      searchPeopleQuery.value = '';
      selectedPerson.value = null;
      showSearchResults.value = false;
    };

    const closeAddCandidatesModal = () => {
      showAddCandidatesModal.value = false;
      // reset inner states
      closeAddCandidateModal();
      closeImportModal();
    };

    const getStatusClass = (status) => {
      const statusMap = {
        'APROVADO': 'status-approved',
        'REPROVADO': 'status-rejected'
      };
      return statusMap[status] || 'status-approved';
    };

    const formatDate = (date) => {
      if (!date) return '-';
      return new Date(date).toLocaleDateString('pt-BR');
    };

    const goBack = () => {
      router.back();
    };

    const openCourses = async () => {
      if (!classId.value) return;
      let pid = programId.value;
      if (!pid) {
        try {
          const cls = await classService.getById(classId.value);
          pid = cls?.program?.id ?? cls?.programId;
        } catch (err) {
          console.error('Erro ao obter programId:', err);
        }
      }

      if (!pid) {
        console.warn('programId não encontrado para a turma', classId.value);
        return;
      }

      router.push({
        name: 'ClassDetails',
        params: { programId: String(pid), classId: String(classId.value) },
        query: { tab: 'etapas', subTab: 'nivelamento' },
      });
    };

    const normalizeName = (str) => (str || '').normalize('NFD').replace(/\p{Diacritic}/gu, '').toUpperCase();

    const displayStageName = (name) => {
      if (!name) return '';
      const normalized = normalizeName(name);
      if (normalized === 'SELECAO') return 'SELEÇÃO';
      if (normalized === 'IMERSAO') return 'IMERSÃO';
      return name;
    };

    const isNivelamento = computed(() => normalizeName(stageData.value?.name) === 'NIVELAMENTO');

    const handleDocumentClick = (e) => {
      try {
        // If the click is inside any .search-container, keep results open
        const containers = document.querySelectorAll('.search-container');
        let clickedInside = false;
        containers.forEach(c => {
          if (c.contains(e.target)) clickedInside = true;
        });
        if (!clickedInside) {
          showSearchResults.value = false;
        }
      } catch (err) {
        // ignore
      }
    };

    onMounted(async () => {
      // use pointerdown to catch outside clicks earlier
      document.addEventListener('pointerdown', handleDocumentClick);
      await loadStageDetails();
      const shouldOpenAddFromQuery = route.query && (route.query.openAdd === 'true' || route.query.openAdd === true);
      const hasNoCandidates = !Array.isArray(candidates.value) || candidates.value.length === 0;
      if (shouldOpenAddFromQuery && hasNoCandidates) {
        showAddCandidatesModal.value = true;
      }
    });

    onBeforeUnmount(() => {
      document.removeEventListener('pointerdown', handleDocumentClick);
    });

    return {
      stageData,
      candidates,
      loading,
      error,
      confirmDialog,
      showAddCandidateModal,
      showAddCandidatesModal,
      searchContainerRef,
      addingCandidate,
      candidateError,
      newCandidate,
      availablePeople,
      searchPeopleQuery,
      showSearchResults,
      selectedPerson,
      filteredPeople,
      showImportModal,
      importing,
      importError,
      importSuccess,
      importSuccessMessage,
      importedPeopleNames,
      duplicatePeopleNames,
      rowErrors,
      selectedFile,
      fileInput,
      showAddCoursesModal,
      modalCourses,
      loadingModalCourses,
      selectedCourseIds,
      selectedRequired,
      addingCourses,
      selectedCourseExcelFile,
      importingCourseExcel,
      courseExcelImportError,
      courseExcelImportSuccess,
      handleSearchInput,
      selectPerson,
      clearSelection,
      formatCPF,
      getPeopleEmail,
      getPeopleCPF,
      addCandidate,
      toggleCourseSelection,
      setRequired,
      handleCourseExcelFileChange,
      importCoursesExcelToClass,
      addSelectedCourses,
      closeAddCoursesModal,
      openAddCoursesModal,
      updateCandidateStatus,
      updateCandidateNotes,
      removeCandidate,
      closeAddCandidateModal,
      closeAddCandidatesModal,
      handleFileChange,
      importExcel,
      closeSuccessAlert,
      closeImportModal,
      getStatusClass,
      formatDate,
      openCourses,
      goBack,
      displayStageName,
      isNivelamento
    };
  }
};
</script>

<style scoped>
.stage-details-view {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
}

.stage-header {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.btn-back {
  background: white;
  border: 2px solid #e0e0e0;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #666;
  margin-bottom: 15px;
  transition: all 0.2s;
}

.btn-back:hover {
  border-color: #0288d1;
  color: #0288d1;
}

.stage-info h1 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 24px;
}

.stage-description {
  margin: 0 0 15px 0;
  color: #666;
  font-size: 15px;
  line-height: 1.5;
}

.stage-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  color: #666;
  font-size: 14px;
}

.meta-item {
  display: flex;
  gap: 5px;
}

.candidates-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  color: #333;
  font-size: 20px;
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: #f8f9fa;
}

th {
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #555;
  border-bottom: 2px solid #dee2e6;
  white-space: nowrap;
}

td {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

tbody tr:hover {
  background: #f8f9fa;
}

.status-select {
  padding: 6px 10px;
  border: 2px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  font-weight: 600;
}

.status-select:focus {
  outline: none;
  border-color: #0288d1;
}

.status-pending {
  background: #e2e3e5;
  color: #383d41;
}

.status-approved {
  background: #d4edda;
  color: #155724;
}

.status-rejected {
  background: #f8d7da;
  color: #721c24;
}

.status-in-progress {
  background: #fff3cd;
  color: #856404;
}

.status-completed {
  background: #d1ecf1;
  color: #0c5460;
}

.notes-input {
  width: 100%;
  padding: 6px 10px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
}

.notes-input:focus {
  outline: none;
  border-color: #0288d1;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn-delete-small {
  padding: 6px 12px;
  background: white;
  color: #dc3545;
  border: 2px solid #dc3545;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-delete-small:hover {
  background: #dc3545;
  color: white;
}

.btn-primary {
  background: white;
  color: #0288d1;
  border: 2px solid #0288d1;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(2, 136, 209, 0.1);
}

.btn-primary:hover {
  background: #0288d1;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(2, 136, 209, 0.25);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn-secondary {
  background: white;
  color: #666;
  border: 2px solid #e0e0e0;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-secondary:hover {
  background: #f8f8f8;
  border-color: #bbb;
}

.loading, .error, .no-data {
  text-align: center;
  padding: 40px;
  color: #666;
}

.error {
  color: #dc3545;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(31, 40, 95, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: white;
  padding: 32px;
  border-radius: 16px;
  max-width: 600px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(31, 40, 95, 0.3);
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-content h2 {
  margin: 0 0 24px 0;
  color: #1F285F;
  font-size: 24px;
  font-weight: 700;
}

.upload-area {
  margin: 20px 0;
  padding: 48px 32px;
  border: 3px dashed #d0d0d0;
  border-radius: 16px;
  text-align: center;
  background: linear-gradient(135deg, #fafafa 0%, #f5f9fc 100%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.upload-area:hover {
  border-color: #0288d1;
  background: linear-gradient(135deg, #ffffff 0%, #e3f2fd 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(2, 136, 209, 0.15);
}

.upload-area input[type="file"] {
  font-size: 14px;
  cursor: pointer;
}

.upload-area p {
  margin: 16px 0 0 0;
  color: #0288d1;
  font-weight: 600;
  font-size: 15px;
}

.alert {
  position: relative;
  border-radius: 12px;
  margin-top: 16px;
  font-size: 15px;
  font-weight: 500;
  animation: slideInRight 0.3s ease-out;
  overflow: hidden;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.alert-scrollable {
  max-height: 300px;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 16px 50px 16px 20px;
}

.alert-scrollable::-webkit-scrollbar {
  width: 8px;
}

.alert-scrollable::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.alert-scrollable::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}

.alert-scrollable::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

.alert-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.alert-content svg {
  flex-shrink: 0;
  margin-top: 2px;
}

.alert-message {
  flex: 1;
  min-width: 0;
}

.btn-close-alert {
  position: absolute;
  top: 12px;
  right: 12px;
  background: none;
  border: none;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
  color: inherit;
  opacity: 0.7;
  z-index: 10;
}

.btn-close-alert:hover {
  opacity: 1;
  background: rgba(0, 0, 0, 0.1);
  transform: rotate(90deg);
}

.alert-success {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #2e7d32;
  border-left: 5px solid #4caf50;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
}

.alert-error {
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
  color: #c62828;
  border-left: 5px solid #f44336;
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.2);
}

.imported-people,
.duplicate-people {
  margin-top: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
}

.imported-people strong,
.duplicate-people strong {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
}

.imported-people ul,
.duplicate-people ul {
  margin: 0;
  padding-left: 20px;
  list-style-type: disc;
}

.imported-people li,
.duplicate-people li {
  margin: 4px 0;
  font-size: 14px;
  line-height: 1.4;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #333;
}

.form-input {
  width: 100%;
  padding: 10px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #0288d1;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 2px solid #f0f0f0;
}

.btn-secondary {
  background: white;
  color: #666;
  border: 2px solid #e0e0e0;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-secondary:hover {
  background: #f8f8f8;
  border-color: #bbb;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.btn-secondary:active {
  transform: translateY(0);
  box-shadow: none;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-import {
  background: white;
  color: #0288d1;
  border: 2px solid #0288d1;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(2, 136, 209, 0.1);
}

.btn-import:hover {
  background: #0288d1;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(2, 136, 209, 0.25);
}

.btn-import:active {
  transform: translateY(0);
}

.btn-import svg {
  transition: transform 0.3s;
}

.btn-import:hover svg {
  transform: translateY(-2px);
}

.search-container {
  position: relative;
}

.search-input {
  width: 100%;
}

.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 2px solid #0288d1;
  border-top: none;
  border-radius: 0 0 6px 6px;
  max-height: 300px;
  overflow-y: auto;
  z-index: 100;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.search-result-item {
  padding: 12px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item:hover {
  background: #f8f9fa;
}

.result-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.result-details {
  font-size: 13px;
  color: #666;
}

.search-no-results {
  padding: 12px;
  color: #666;
  text-align: center;
  font-size: 14px;
}

.selected-person {
  margin-top: 10px;
  padding: 12px;
  background: #e3f2fd;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #0277bd;
  font-size: 14px;
}

.btn-clear {
  background: none;
  border: none;
  color: #0277bd;
  font-size: 24px;
  cursor: pointer;
  padding: 0 8px;
  line-height: 1;
  transition: color 0.2s;
}

.btn-clear:hover {
  color: #01579b;
}
</style>
