<template>
  <div class="program-details-view">
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>Carregando programa...</p>
    </div>

    <div v-else-if="error" class="error-box">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
        stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      {{ error }}
    </div>

    <div v-else>
      <!-- NavegaÃ§Ã£o e CabeÃ§alho -->
      <button @click="goBack" class="btn-back">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2">
          <line x1="19" y1="12" x2="5" y2="12"></line>
          <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
        Voltar
      </button>

      <!-- Card do Programa -->
      <div class="program-card">
        <div class="program-header">
          <div class="program-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2">
              <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path>
              <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"></path>
            </svg>
          </div>
          <div class="program-info">
            <h1>{{ program.name }}</h1>
            <div class="program-code-badge">{{ program.code }}</div>
          </div>
        </div>

        <div class="program-details-grid">
          <div class="detail-card" v-if="program.contractNumber">
            <div class="detail-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
              </svg>
            </div>
            <div>
              <div class="detail-label">NÃºmero do Contrato</div>
              <div class="detail-value">{{ program.contractNumber }}</div>
            </div>
          </div>

          <div class="detail-card" v-if="program.executorName">
            <div class="detail-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </div>
            <div>
              <div class="detail-label">Executora</div>
              <div class="detail-value">{{ program.executorName }}</div>
            </div>
          </div>

          <div class="detail-card" v-if="program.fundingEntity">
            <div class="detail-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2">
                <path d="M3 10.5 12 4l9 6.5"></path>
                <path d="M5 10v8h14v-8"></path>
                <path d="M9 22V12h6v10"></path>
              </svg>
            </div>
            <div>
              <div class="detail-label">Entidade de Fomento</div>
              <div class="detail-value">{{ program.fundingEntity }}</div>
            </div>
          </div>

          <div class="detail-card" v-if="program.generalCoordinator">
            <div class="detail-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2">
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
                <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
              </svg>
            </div>
            <div>
              <div class="detail-label">Coordenador Geral</div>
              <div class="detail-value">{{ program.generalCoordinator }}</div>
            </div>
          </div>

          <div class="detail-card" v-if="program.programValue !== null && program.programValue !== undefined && program.programValue !== ''">
            <div class="detail-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <path d="M12 6v12"></path>
                <path d="M16 10c0-1.657-1.79-3-4-3s-4 1.343-4 3 1.79 3 4 3 4 1.343 4 3-1.79 3-4 3-4-1.343-4-3"></path>
              </svg>
            </div>
            <div>
              <div class="detail-label">Valor do Programa</div>
              <div class="detail-value">{{ formatCurrency(program.programValue) }}</div>
            </div>
          </div>

          <div class="detail-card">
            <div class="detail-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                <line x1="16" y1="2" x2="16" y2="6"></line>
                <line x1="8" y1="2" x2="8" y2="6"></line>
                <line x1="3" y1="10" x2="21" y2="10"></line>
              </svg>
            </div>
            <div>
              <div class="detail-label">PerÃ­odo</div>
              <div class="detail-value">{{ formatDate(program.startDate) }} - {{ formatDate(program.endDate) }}</div>
            </div>
          </div>

          <div class="detail-card">
            <div class="detail-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
              </svg>
            </div>
            <div>
              <div class="detail-label">Total de Turmas</div>
              <div class="detail-value">{{ classes.length }}</div>
            </div>
          </div>
        </div>

        <div class="addendum-section">
          <div class="section-header section-header-compact">
            <div>
              <h2>Aditivos do Programa</h2>
              <p class="section-subtitle-small">Controle prazos adicionais e valores complementares do programa.</p>
            </div>
            <button @click="openAddendumModal()" class="btn-create btn-create-small">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              Novo aditivo
            </button>
          </div>

          <div v-if="addendums.length === 0" class="empty-inline-state">
            Nenhum aditivo cadastrado para este programa.
          </div>

          <div v-else class="addendum-grid">
            <article v-for="addendum in addendums" :key="addendum.id" class="addendum-card">
              <div class="addendum-top">
                <div class="addendum-badge">Aditivo {{ addendum.addendumNumber }}</div>
                <div class="addendum-actions">
                  <button type="button" class="btn-icon-small" title="Editar aditivo" @click="openAddendumModal(addendum)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                      stroke="currentColor" stroke-width="2">
                      <path d="M12 20h9"></path>
                      <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z"></path>
                    </svg>
                  </button>
                  <button type="button" class="btn-icon-small btn-icon-danger" title="Excluir aditivo" @click="removeAddendum(addendum)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                      stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"></polyline>
                      <path d="M19 6l-1 14H6L5 6"></path>
                      <path d="M10 11v6"></path>
                      <path d="M14 11v6"></path>
                      <path d="M9 6V4h6v2"></path>
                    </svg>
                  </button>
                </div>
              </div>

              <div class="addendum-meta">
                <div>
                  <span>Vigencia</span>
                  <strong>{{ formatDate(addendum.startDate) }} - {{ formatDate(addendum.endDate) }}</strong>
                </div>
                <div>
                  <span>Valor</span>
                  <strong>{{ formatCurrency(addendum.value) }}</strong>
                </div>
              </div>
            </article>
          </div>
        </div>
      </div>

      <!-- SeÃ§Ã£o de Turmas -->
      <div class="classes-section">
        <div class="section-header">
          <h2>Turmas do Programa</h2>
          <div class="header-actions">
            <button @click="showCreateModal = true" class="btn-create">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              Nova Turma
            </button>
          </div>
        </div>

        <div class="search-container">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" class="search-icon">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
          <input v-model="searchTerm" type="text" placeholder="Buscar turma por cÃ³digo..." class="search-input" />
        </div>

        <div v-if="filteredClasses.length === 0" class="empty-state">
          <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="16" y1="2" x2="16" y2="6"></line>
            <line x1="8" y1="2" x2="8" y2="6"></line>
            <line x1="3" y1="10" x2="21" y2="10"></line>
          </svg>
          <h3>Nenhuma turma encontrada</h3>
          <p>Importe turmas via Excel para comeÃ§ar</p>
        </div>

        <div v-else class="classes-grid">
          <div v-for="classItem in filteredClasses" :key="classItem.id" class="class-card"
            @click="viewClass(classItem)">
            <div class="class-header">
              <div class="class-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
              </div>
              <button @click.stop="editClass(classItem)" class="btn-icon-small" title="Editar">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
            </div>

            <h3 class="class-code">{{ classItem.code }}</h3>

            <div class="class-details">
              <div class="class-detail-item">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                <span>{{ formatDate(classItem.startDate) }} - {{ formatDate(classItem.endDate) }}</span>
              </div>

              <div class="class-detail-item" v-if="classItem.location">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                  <circle cx="12" cy="10" r="3"></circle>
                </svg>
                <span>{{ classItem.location.name }}</span>
              </div>
            </div>

            <div class="class-footer">
              <span class="view-link">Ver detalhes</span>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2">
                <line x1="5" y1="12" x2="19" y2="12"></line>
                <polyline points="12 5 19 12 12 19"></polyline>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de CriaÃ§Ã£o de Turma -->
    <div v-if="showCreateModal" class="modal-overlay" @click="closeCreateModal">
      <div class="modal-content modal-form" @click.stop>
        <h2>{{ editingClass ? 'Editar Turma' : 'Nova Turma' }}</h2>
        <p class="modal-subtitle">Programa: <strong>{{ program.name }}</strong></p>

        <form @submit.prevent="saveClass">
          <div class="form-group">
            <label for="classCode">CÃ³digo da Turma *</label>
            <input id="classCode" v-model="classForm.code" type="text" required placeholder="Ex: T001" />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="classStartDate">Data InÃ­cio</label>
              <input id="classStartDate" v-model="classForm.startDate" type="date" />
            </div>

            <div class="form-group">
              <label for="classEndDate">Data Fim</label>
              <input id="classEndDate" v-model="classForm.endDate" type="date" />
            </div>
          </div>

          <div class="form-group">
            <label for="location">InstituiÃ§Ã£o (Local)</label>
            <select id="location" v-model="classForm.locationId">
              <option value="">Selecione uma instituiÃ§Ã£o</option>
              <option v-for="institution in institutions" :key="institution.id" :value="institution.id">
                {{ institution.name }} {{ institution.acronym ? `(${institution.acronym})` : '' }}
              </option>
            </select>
          </div>

          <div v-if="formError" class="error-box">{{ formError }}</div>

          <div class="modal-actions">
            <button type="button" @click="closeCreateModal" class="btn-secondary">Cancelar</button>
            <button type="submit" :disabled="saving || formError" class="btn-primary">
              {{ saving ? 'Salvando...' : 'Salvar' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showAddendumModal" class="modal-overlay" @click="closeAddendumModal">
      <div class="modal-content modal-form modal-addendum" @click.stop>
        <h2>{{ editingAddendum ? 'Editar Aditivo' : 'Novo Aditivo' }}</h2>
        <p class="modal-subtitle">Programa: <strong>{{ program.name }}</strong></p>

        <form @submit.prevent="saveAddendum">
          <div class="form-row">
            <div class="form-group">
              <label for="addendumNumber">Numero do aditivo</label>
              <input id="addendumNumber" v-model="addendumForm.addendumNumber" type="number" min="1" placeholder="Ex: 1" />
            </div>

            <div class="form-group">
              <label for="addendumValue">Valor do aditivo</label>
              <input id="addendumValue" v-model="addendumForm.value" type="number" min="0" step="0.01" placeholder="Ex: 15000.00" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="addendumStartDate">Data inicial *</label>
              <input id="addendumStartDate" v-model="addendumForm.startDate" type="date" required />
            </div>

            <div class="form-group">
              <label for="addendumEndDate">Data final *</label>
              <input id="addendumEndDate" v-model="addendumForm.endDate" type="date" required />
            </div>
          </div>

          <div v-if="addendumError" class="error-box form-error-box">{{ addendumError }}</div>

          <div class="modal-actions">
            <button type="button" @click="closeAddendumModal" class="btn-secondary">Cancelar</button>
            <button type="submit" :disabled="savingAddendum" class="btn-primary">
              {{ savingAddendum ? 'Salvando...' : 'Salvar aditivo' }}
            </button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { programService } from '@/services/programService';
import { programAddendumService } from '@/services/programAddendumService';
import { classService } from '@/services/classService';
import { institutionService } from '@/services/institutionService';

export default {
  name: 'ProgramDetailsView',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const program = ref({});
    const classes = ref([]);
    const institutions = ref([]);
    const addendums = ref([]);
    const loading = ref(false);
    const error = ref(null);
    const searchTerm = ref('');
    const showUploadModal = ref(false);
    const showCreateModal = ref(false);
    const selectedFile = ref(null);
    const uploading = ref(false);
    const uploadError = ref(null);
    const uploadSuccess = ref(null);
    const fileInput = ref(null);
    const isEditing = ref(false);
    const editingClass = ref(null);
    const saving = ref(false);
    const formError = ref(null);
    const showAddendumModal = ref(false);
    const editingAddendum = ref(null);
    const savingAddendum = ref(false);
    const addendumError = ref(null);
    const classForm = ref({
      code: '',
      startDate: '',
      endDate: '',
      locationId: ''
    });
    const addendumForm = ref({
      addendumNumber: '',
      startDate: '',
      endDate: '',
      value: ''
    });

    watch(
      () => [classForm.value.startDate, classForm.value.endDate],
      ([startDate, endDate]) => {
        if (!startDate || !endDate) {
          formError.value = null;
          return;
        }

        if (endDate < startDate) {
          formError.value = 'Data invÃ¡lida: a data de fim nÃ£o pode ser anterior Ã  data de inÃ­cio';
        } else {
          formError.value = null;
        }
      }
    );

    const programId = computed(() => route.params.id);

    const filteredClasses = computed(() => {
      if (!searchTerm.value) return classes.value;

      const term = searchTerm.value.toLowerCase();
      return classes.value.filter(c => c.code?.toLowerCase().includes(term));
    });

    const loadProgramDetails = async () => {
      loading.value = true;
      error.value = null;
      try {
        const [programData, classesData, institutionsData, addendumsData] = await Promise.all([
          programService.getById(programId.value),
          classService.getByProgramId(programId.value),
          institutionService.getAll(),
          programAddendumService.listByProgram(programId.value).catch(() => [])
        ]);

        program.value = programData;
        classes.value = classesData;
        institutions.value = institutionsData;
        addendums.value = Array.isArray(addendumsData) ? addendumsData : [];
      } catch (err) {
        error.value = 'Erro ao carregar detalhes do programa: ' + (err.response?.data?.message || err.message);
      } finally {
        loading.value = false;
      }
    };

    const formatCurrency = (value) => {
      const numericValue = Number(value || 0);
      return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
      }).format(Number.isFinite(numericValue) ? numericValue : 0);
    };

    const formatDate = (date) => {
      if (!date) return '-';
      return new Date(date).toLocaleDateString('pt-BR');
    };

    const goBack = () => {
      router.back();
    };

    const viewClass = (classItem) => {
      router.push({
        name: 'ClassDetails',
        params: {
          programId: programId.value,
          classId: classItem.id
        }
      });
    };

    const editClass = (classItem) => {
      isEditing.value = true;
      editingClass.value = classItem;
      classForm.value = {
        id: classItem.id,
        code: classItem.code,
        startDate: classItem.startDate || '',
        endDate: classItem.endDate || '',
        locationId: classItem.location?.id || ''
      };
      formError.value = null;
      showCreateModal.value = true;
    };

    const handleFileSelect = (event) => {
      selectedFile.value = event.target.files[0];
      uploadError.value = null;
      uploadSuccess.value = null;
    };

    const uploadFile = async () => {
      if (!selectedFile.value) return;

      uploading.value = true;
      uploadError.value = null;
      uploadSuccess.value = null;

      try {
        await classService.importExcel(selectedFile.value);
        uploadSuccess.value = 'Arquivo importado com sucesso!';
        setTimeout(() => {
          closeUploadModal();
          loadProgramDetails();
        }, 2000);
      } catch (err) {
        uploadError.value = 'Erro ao importar arquivo: ' + (err.response?.data?.message || err.message);
      } finally {
        uploading.value = false;
      }
    };

    const closeUploadModal = () => {
      showUploadModal.value = false;
      selectedFile.value = null;
      uploadError.value = null;
      uploadSuccess.value = null;
      if (fileInput.value) {
        fileInput.value.value = '';
      }
    };

    const openCreateModal = () => {
      isEditing.value = false;
      editingClass.value = null;
      classForm.value = {
        code: '',
        startDate: '',
        endDate: '',
        locationId: ''
      };
      formError.value = null;
      showCreateModal.value = true;
    };

    const closeCreateModal = () => {
      showCreateModal.value = false;
      isEditing.value = false;
      editingClass.value = null;
      classForm.value = {
        code: '',
        startDate: '',
        endDate: '',
        locationId: ''
      };
      formError.value = null;
    };

    const openAddendumModal = (addendum = null) => {
      editingAddendum.value = addendum;
      addendumError.value = null;
      addendumForm.value = {
        addendumNumber: addendum?.addendumNumber ?? '',
        startDate: addendum?.startDate ?? '',
        endDate: addendum?.endDate ?? '',
        value: addendum?.value ?? ''
      };
      showAddendumModal.value = true;
    };

    const closeAddendumModal = () => {
      showAddendumModal.value = false;
      editingAddendum.value = null;
      savingAddendum.value = false;
      addendumError.value = null;
      addendumForm.value = {
        addendumNumber: '',
        startDate: '',
        endDate: '',
        value: ''
      };
    };

    const saveAddendum = async () => {
      savingAddendum.value = true;
      addendumError.value = null;

      try {
        const payload = {
          addendumNumber: addendumForm.value.addendumNumber ? Number(addendumForm.value.addendumNumber) : null,
          startDate: addendumForm.value.startDate,
          endDate: addendumForm.value.endDate,
          value: addendumForm.value.value === '' ? null : Number(addendumForm.value.value)
        };

        if (editingAddendum.value?.id) {
          await programAddendumService.update(programId.value, editingAddendum.value.id, payload);
        } else {
          await programAddendumService.create(programId.value, payload);
        }

        closeAddendumModal();
        await loadProgramDetails();
      } catch (err) {
        addendumError.value = err.response?.data?.message
          || err.response?.data?.errors?.join?.(' ')
          || 'Erro ao salvar aditivo.';
      } finally {
        savingAddendum.value = false;
      }
    };

    const removeAddendum = async (addendum) => {
      const confirmed = window.confirm(`Deseja excluir o aditivo ${addendum.addendumNumber}?`);
      if (!confirmed) return;

      try {
        await programAddendumService.delete(programId.value, addendum.id);
        await loadProgramDetails();
      } catch (err) {
        window.alert(err.response?.data?.message || 'Erro ao excluir aditivo.');
      }
    };

    const saveClass = async () => {
      try {
        formError.value = null;

        if (!classForm.value.code) {
          formError.value = 'CÃ³digo da turma Ã© obrigatÃ³rio';
          return;
        }

        saving.value = true;

        const classData = {
          code: classForm.value.code,
          startDate: classForm.value.startDate || null,
          endDate: classForm.value.endDate || null,
          program: {
            id: programId.value
          }
        };

        // Adiciona a instituiÃ§Ã£o se selecionada
        if (classForm.value.locationId) {
          classData.location = {
            id: classForm.value.locationId
          };
        }

        if (isEditing.value && editingClass.value) {
          await classService.update(editingClass.value.id, classData);
        } else {
          await classService.create(classData);
        }

        closeCreateModal();
        await loadProgramDetails();
      } catch (err) {
        console.error('Erro ao salvar turma:', err);
        formError.value = err.response?.data?.message || 'Erro ao salvar turma. Tente novamente.';
      } finally {
        saving.value = false;
      }
    };

    onMounted(() => {
      loadProgramDetails();
    });

    return {
      program,
      institutions,
      classes,
      addendums,
      loading,
      error,
      searchTerm,
      filteredClasses,
      showUploadModal,
      showCreateModal,
      selectedFile,
      uploading,
      uploadError,
      uploadSuccess,
      fileInput,
      isEditing,
      editingClass,
      saving,
      formError,
      showAddendumModal,
      editingAddendum,
      savingAddendum,
      addendumError,
      classForm,
      addendumForm,
      formatDate,
      formatCurrency,
      goBack,
      viewClass,
      editClass,
      handleFileSelect,
      uploadFile,
      closeUploadModal,
      openCreateModal,
      closeCreateModal,
      saveClass,
      openAddendumModal,
      closeAddendumModal,
      saveAddendum,
      removeAddendum
    };
  }
};
</script>

<style scoped>
.program-details-view {
  padding: 30px;
  max-width: 1400px;
  margin: 0 auto;
}

/* Navigation */
.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: white;
  color: #1F285F;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 24px;
  transition: all 0.2s;
}

.btn-back:hover {
  background: #f5f5f5;
  border-color: #1F285F;
}

/* Program Card */
.program-card {
  background: white;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(31, 40, 95, 0.1);
  margin-bottom: 32px;
  border: 2px solid #f5f5f5;
  position: relative;
  overflow: hidden;
}

.program-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 5px;
  background: linear-gradient(90deg, #1F285F 0%, #0288d1 100%);
}

.program-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.program-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #1F285F 0%, #0288d1 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.program-info h1 {
  margin: 0 0 8px 0;
  color: #1F285F;
  font-size: 28px;
  font-weight: 700;
}

.program-code-badge {
  display: inline-block;
  background: #e3f2fd;
  color: #0277bd;
  padding: 6px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
}

.program-details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.detail-card {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.detail-icon {
  width: 40px;
  height: 40px;
  background: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0288d1;
  flex-shrink: 0;
}

.detail-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  text-transform: uppercase;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.detail-value {
  font-size: 15px;
  color: #1F285F;
  font-weight: 600;
}

.addendum-section {
  margin-top: 28px;
  padding-top: 24px;
  border-top: 1px solid #eef2f7;
}

.section-header-compact {
  margin-bottom: 16px;
}

.section-subtitle-small {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 14px;
}

.btn-create-small {
  padding: 10px 18px;
  font-size: 14px;
}

.empty-inline-state {
  background: #f8fafc;
  color: #64748b;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
  padding: 18px;
  text-align: center;
}

.addendum-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 14px;
}

.addendum-card {
  background: #f8fbff;
  border: 1px solid #dbe5f1;
  border-radius: 14px;
  padding: 16px;
}

.addendum-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.addendum-badge {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e3f2fd;
  color: #0277bd;
  font-size: 13px;
  font-weight: 700;
}

.addendum-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-icon-danger:hover {
  background: #ef4444;
  color: #fff;
}

.addendum-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.addendum-meta span {
  display: block;
  color: #64748b;
  font-size: 12px;
  margin-bottom: 4px;
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 0.03em;
}

.addendum-meta strong {
  color: #1F285F;
  font-size: 15px;
}

.form-error-box {
  margin-top: 0;
}

/* Classes Section */
.classes-section {
  margin-top: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.section-header h2 {
  margin: 0;
  color: #1F285F;
  font-size: 24px;
  font-weight: 700;
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.btn-create {
  background: linear-gradient(135deg, #1F285F 0%, #0288d1 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(31, 40, 95, 0.15);
}

.btn-create:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(31, 40, 95, 0.25);
}

.btn-create:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(31, 40, 95, 0.15);
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

.search-container {
  position: relative;
  margin-bottom: 24px;
  max-width: 500px;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

.search-input {
  width: 100%;
  padding: 14px 16px 14px 48px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 15px;
  transition: all 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #0288d1;
  box-shadow: 0 0 0 3px rgba(2, 136, 209, 0.1);
}

/* Classes Grid */
.classes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.class-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(31, 40, 95, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  border: 2px solid #f0f0f0;
}

.class-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(31, 40, 95, 0.15);
  border-color: #0288d1;
}

.class-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.class-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #1F285F 0%, #0288d1 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(31, 40, 95, 0.2);
}

.btn-icon-small {
  background: #f5f5f5;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
}

.btn-icon-small:hover {
  background: #0288d1;
  color: white;
}

.class-code {
  margin: 0 0 16px 0;
  color: #1F285F;
  font-size: 18px;
  font-weight: 700;
}

.class-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.class-detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.class-detail-item svg {
  color: #999;
  flex-shrink: 0;
}

.class-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.view-link {
  color: #0288d1;
  font-size: 14px;
  font-weight: 600;
}

/* Loading & Empty States */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #666;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #f0f0f0;
  border-top-color: #0288d1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.error-box {
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
  border-left: 5px solid #f44336;
  padding: 16px 20px;
  border-radius: 10px;
  color: #c62828;
  margin-bottom: 20px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(244, 67, 54, 0.15);
  animation: shake 0.4s;
}

@keyframes shake {

  0%,
  100% {
    transform: translateX(0);
  }

  25% {
    transform: translateX(-8px);
  }

  75% {
    transform: translateX(8px);
  }
}

.error-box::before {
  content: 'âš ï¸';
  font-size: 20px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-state svg {
  color: #e0e0e0;
  margin-bottom: 16px;
}

.empty-state h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 20px;
}

.empty-state p {
  margin: 0;
  color: #666;
  font-size: 15px;
}

.success {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #2e7d32;
  border-left: 5px solid #4caf50;
  padding: 16px 20px;
  border-radius: 10px;
  margin-top: 16px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.15);
  display: flex;
  align-items: center;
  gap: 12px;
  animation: slideInRight 0.3s ease-out;
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

.success::before {
  content: 'âœ“';
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #4caf50;
  color: white;
  border-radius: 50%;
  font-weight: bold;
  font-size: 16px;
}

/* Modal Styles */
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
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
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

.modal-form {
  max-width: 700px;
}

.modal-content h2 {
  margin: 0 0 8px 0;
  color: #1F285F;
  font-size: 26px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 12px;
}

.modal-content h2::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 28px;
  background: linear-gradient(135deg, #1F285F 0%, #0288d1 100%);
  border-radius: 2px;
}

.modal-subtitle {
  color: #666;
  font-size: 15px;
  margin-bottom: 32px;
  padding-left: 16px;
}

.modal-subtitle strong {
  color: #1F285F;
  font-weight: 600;
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
  position: relative;
}

.upload-area::before {
  content: 'ðŸ“';
  display: block;
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: #0288d1;
  background: linear-gradient(135deg, #ffffff 0%, #e3f2fd 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(2, 136, 209, 0.15);
}

.upload-area:hover::before {
  opacity: 1;
  transform: scale(1.1);
}

.upload-area input[type="file"] {
  font-size: 14px;
  cursor: pointer;
  color: #666;
}

.upload-area input[type="file"]::file-selector-button {
  background: linear-gradient(135deg, #1F285F 0%, #0288d1 100%);
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  margin-right: 12px;
  transition: all 0.2s;
}

.upload-area input[type="file"]::file-selector-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(31, 40, 95, 0.25);
}

.upload-area p {
  margin-top: 16px;
  color: #0288d1;
  font-weight: 600;
  font-size: 15px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 2px solid #f0f0f0;
}

.modal-actions .btn-primary {
  background: linear-gradient(135deg, #1F285F 0%, #0288d1 100%);
  color: white;
  border: none;
  padding: 14px 32px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(31, 40, 95, 0.2);
  min-width: 120px;
}

.modal-actions .btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(31, 40, 95, 0.3);
}

.modal-actions .btn-primary:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(31, 40, 95, 0.2);
}

.modal-actions .btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 2px 8px rgba(31, 40, 95, 0.15);
}

.modal-actions .btn-secondary {
  background: white;
  color: #666;
  border: 2px solid #e0e0e0;
  padding: 14px 32px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 120px;
}

.modal-actions .btn-secondary:hover {
  background: #f8f8f8;
  border-color: #bbb;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.modal-actions .btn-secondary:active {
  transform: translateY(0);
  box-shadow: none;
}

/* Form Styles */
.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  color: #1F285F;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  letter-spacing: 0.3px;
}

.form-group input[type="text"],
.form-group input[type="date"],
.form-group select {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 15px;
  color: #333;
  transition: all 0.2s;
  background: #fafafa;
  box-sizing: border-box;
  appearance: none;
  cursor: pointer;
}

.form-group select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='%230288d1' stroke-width='2'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 20px;
  padding-right: 40px;
}

.form-group input[type="text"]:focus,
.form-group input[type="date"]:focus,
.form-group select:focus {
  outline: none;
  border-color: #0288d1;
  background-color: white;
  box-shadow: 0 0 0 4px rgba(2, 136, 209, 0.1);
}

.form-group select:hover {
  border-color: #0288d1;
  background-color: white;
}

.form-group input[type="text"]::placeholder {
  color: #999;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

@media (max-width: 640px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .header-actions {
    width: 100%;
  }

  .btn-create,
  .btn-import {
    flex: 1;
    justify-content: center;
  }
}
</style>

