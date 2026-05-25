<template>
  <div class="people-view">
    <div class="header">
      <div class="header-left">
        <h1>Pessoas</h1>
        <p class="subtitle">{{ totalPeople }} pessoa(s) cadastrada(s)</p>
      </div>
      <div class="header-actions">
        <button @click="showCreateModal = true" class="btn-create">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2">
            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="8.5" cy="7" r="4"></circle>
            <line x1="20" y1="8" x2="20" y2="14"></line>
            <line x1="23" y1="11" x2="17" y2="11"></line>
          </svg>
          Cadastrar
        </button>
        <button @click="showUploadModal = true" class="btn-import">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="17 8 12 3 7 8"></polyline>
            <line x1="12" y1="3" x2="12" y2="15"></line>
          </svg>
          Importar
        </button>
      </div>
    </div>

    <div class="search-container">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
        stroke="currentColor" stroke-width="2" class="search-icon">
        <circle cx="11" cy="11" r="8"></circle>
        <path d="m21 21-4.35-4.35"></path>
      </svg>
      <input v-model="searchTerm" type="text" placeholder="Buscar por nome, CPF ou email..." class="search-input" />
    </div>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>Carregando pessoas...</p>
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

    <div v-else-if="paginatedPeople.length === 0" class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none"
        stroke="currentColor" stroke-width="2">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
        <circle cx="12" cy="7" r="4"></circle>
      </svg>
      <h3>Nenhuma pessoa encontrada</h3>
      <p>Comece importando pessoas via Excel</p>
    </div>

    <div v-else>
      <div class="people-grid">
        <div v-for="person in paginatedPeople" :key="person.id" class="person-card">
          <div class="card-header">
            <div class="person-avatar">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </div>
            <div class="card-actions">
              <button @click="viewDetails(person)" class="btn-icon" title="Ver detalhes">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
              </button>
              <button @click="editPerson(person)" class="btn-icon" title="Editar">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
            </div>
          </div>

          <div class="card-body">
            <h3 class="person-name">{{ person.name }}</h3>

            <div class="person-details">
              <div class="detail-item" v-if="person.cpf">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
                <span>{{ formatCPF(person.cpf) }}</span>
              </div>

              <div class="detail-item" v-if="person.email">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                  <polyline points="22,6 12,13 2,6"></polyline>
                </svg>
                <span class="email-text">{{ person.email }}</span>
              </div>

              <div class="detail-item" v-if="person.educationLevel">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2">
                  <path d="M22 10v6M2 10l10-5 10 5-10 5z"></path>
                  <path d="M6 12v5c3 3 9 3 12 0v-5"></path>
                </svg>
                <span>{{ person.educationLevel }}</span>
              </div>

              <div class="detail-item" v-if="person.city">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                  <circle cx="12" cy="10" r="3"></circle>
                </svg>
                <span>{{ person.city }}</span>
              </div>

              <div class="detail-item" v-if="person.birthDate">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                <span>{{ formatDate(person.birthDate) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination" v-if="totalPages > 1">
        <button @click="goToPage(1)" :disabled="currentPage === 1" class="pagination-btn pagination-btn-icon"
          title="Primeira página">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2">
            <polyline points="11 17 6 12 11 7"></polyline>
            <polyline points="18 17 13 12 18 7"></polyline>
          </svg>
        </button>

        <button @click="currentPage--" :disabled="currentPage === 1" class="pagination-btn pagination-btn-icon"
          title="Página anterior">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"></polyline>
          </svg>
        </button>

        <div class="pagination-pages">
          <button v-for="page in visiblePages" :key="page" @click="goToPage(page)"
            :class="['pagination-number', { active: page === currentPage }]">
            {{ page }}
          </button>
        </div>

        <button @click="currentPage++" :disabled="currentPage === totalPages" class="pagination-btn pagination-btn-icon"
          title="Próxima página">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"></polyline>
          </svg>
        </button>

        <button @click="goToPage(totalPages)" :disabled="currentPage === totalPages"
          class="pagination-btn pagination-btn-icon" title="Última página">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2">
            <polyline points="13 17 18 12 13 7"></polyline>
            <polyline points="6 17 11 12 6 7"></polyline>
          </svg>
        </button>
      </div>

      <div class="pagination-info" v-if="totalPages > 1">
        Mostrando {{ ((currentPage - 1) * itemsPerPage) + 1 }} a {{ Math.min(currentPage * itemsPerPage,
          filteredPeople.length) }} de {{ filteredPeople.length }} pessoa(s)
      </div>
    </div>

    <div v-if="showUploadModal" class="modal-overlay" @click="closeUploadModal">
      <div class="modal-content" @click.stop>
        <h2>Importar Pessoas via Excel</h2>
        <div
          class="upload-area"
          :class="{ 'drag-over': isDragging, 'has-file': selectedFile }"
          @dragover.prevent="handleDragOver"
          @dragleave.prevent="handleDragLeave"
          @drop.prevent="handleDrop"
          @click="fileInput.click()"
        >
          <input
            type="file"
            @change="handleFileSelect"
            accept=".xlsx,.xls"
            ref="fileInput"
            class="hidden-input"
          />

          <div class="upload-icon-wrapper">
            <svg v-if="!selectedFile" xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="upload-icon">
              <polyline points="16 16 12 12 8 16"></polyline>
              <line x1="12" y1="12" x2="12" y2="21"></line>
              <path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3"></path>
            </svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="upload-icon file-ok">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <polyline points="9 15 11 17 15 13"></polyline>
            </svg>
          </div>

          <div class="upload-text">
            <p v-if="!selectedFile" class="upload-main-text">
              <span v-if="isDragging">Solte o arquivo aqui</span>
              <span v-else>Solte aqui ou <span class="upload-link">selecione o arquivo</span></span>
            </p>
            <p v-else class="upload-main-text file-name">{{ selectedFile.name }}</p>
            <p class="upload-sub-text">{{ selectedFile ? 'Clique para trocar o arquivo' : 'Formatos aceitos: .xlsx, .xls' }}</p>
          </div>
        </div>
        <div class="modal-actions">
          <button @click="closeUploadModal" class="btn-secondary">Cancelar</button>
          <button @click="uploadFile" :disabled="!selectedFile || uploading" class="btn-primary">
            {{ uploading ? 'Enviando...' : 'Enviar' }}
          </button>
        </div>
        <div v-if="uploadError" class="alert alert-error">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          {{ uploadError }}
        </div>

        <div v-if="uploadSuccess" class="alert alert-success">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22 4 12 14.01 9 11.01"></polyline>
          </svg>
          {{ uploadSuccess }}
        </div>

        <div v-if="uploadResult && (uploadResult.alreadyExists > 0 || (uploadResult.duplicatePersons && uploadResult.duplicatePersons.length > 0))" class="alert alert-warning">
          <div class="alert-header">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
              <line x1="12" y1="9" x2="12" y2="13"></line>
              <line x1="12" y1="17" x2="12.01" y2="17"></line>
            </svg>
            <div>
              <strong>Atenção: {{ uploadResult.duplicatePersons.length }} registro(s) com problema ou duplicados</strong>
              <p>As seguintes pessoas falharam na validação ou já estão cadastradas:</p>
            </div>
          </div>
          <div class="duplicate-list" style="max-height: 150px; overflow-y: auto; margin-top: 10px;">
            <div v-for="(person, index) in uploadResult.duplicatePersons" :key="index" class="duplicate-item" style="padding: 4px 0; border-bottom: 1px solid #eee;">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" style="margin-right: 8px; vertical-align: middle;">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              <span style="vertical-align: middle;">{{ person }}</span>
            </div>
          </div>
          <div class="alert-footer" style="margin-top: 10px; font-size: 0.85em; color: #666;">
            Verifique as mensagens de erro e corrija os dados na planilha se necessário.
          </div>
        </div>
      </div>
    </div>

    <div v-if="showCreateModal" class="modal-overlay" @click="closeCreateModal">
      <div class="modal-content modal-large" @click.stop>
        <h2>Nova Pessoa</h2>
        <form @submit.prevent="handleCreate" class="create-form">
          <div class="form-grid">
            <div class="form-group full-width">
              <label>Nome Completo *</label>
              <input v-model="createForm.name" type="text" placeholder="Digite o nome completo" required />
            </div>

            <div class="form-group">
              <label>E-mail *</label>
              <input v-model="createForm.email" type="email" placeholder="email@exemplo.com" required />
            </div>

            <div class="form-group">
              <label>CPF *</label>
              <input v-model="createForm.cpf" type="text" placeholder="000.000.000-00" @input="formatCPFInput"
                required />
            </div>

            <div class="form-group">
              <label>Data de Nascimento</label>
              <input v-model="createForm.birthDate" type="date" />
            </div>

            <div class="form-group">
              <label>Gênero</label>
              <select v-model="createForm.gender">
                <option value="">Selecione</option>
                <option value="Masculino">Masculino</option>
                <option value="Feminino">Feminino</option>
                <option value="Outro">Outro</option>
                <option value="Prefiro não informar">Prefiro não informar</option>
              </select>
            </div>

            <div class="form-group full-width">
              <label>Nível de Escolaridade</label>
              <input v-model="createForm.educationLevel" type="text" placeholder="Ex: Ensino Superior Completo" />
            </div>

            <div class="form-group full-width">
              <label>Endereço</label>
              <input v-model="createForm.address" type="text" placeholder="Rua, número, bairro" />
            </div>

            <div class="form-group">
              <label>Cidade</label>
              <input v-model="createForm.city" type="text" placeholder="Cidade - UF" />
            </div>
          </div>

          <div v-if="createError" class="alert alert-error">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            {{ createError }}
          </div>

          <div class="modal-actions">
            <button type="button" @click="closeCreateModal" class="btn-secondary">Cancelar</button>
            <button type="submit" :disabled="creating || !createForm.name || !createForm.email || createForm.cpf.replace(/\D/g,'').length !== 11"
              class="btn-primary">
              {{ creating ? 'Criando...' : 'Criar Pessoa' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { peopleService } from '@/services/peopleService';

export default {
  name: 'PeopleView',
  setup() {
    const router = useRouter();
    const people = ref([]);
    const loading = ref(false);
    const error = ref(null);
    const searchTerm = ref('');
    const showUploadModal = ref(false);
    const showCreateModal = ref(false);
    const selectedFile = ref(null);
    const uploading = ref(false);
    const uploadError = ref(null);
    const uploadSuccess = ref(null);
    const uploadResult = ref(null);
    const fileInput = ref(null);
    const isDragging = ref(false);
    const currentPage = ref(1);
    const itemsPerPage = 20;
    const createForm = ref({
      name: '',
      email: '',
      cpf: '',
      birthDate: '',
      gender: '',
      educationLevel: '',
      address: '',
      city: ''
    });
    const creating = ref(false);
    const createError = ref(null);

    const totalPeople = computed(() => people.value.length);

    const isValidCPFLength = (cpf) => {
      const digits = cpf.replace(/\D/g, '');
      return digits.length === 11;
    };

    const filteredPeople = computed(() => {
      if (!searchTerm.value) return people.value;

      const term = searchTerm.value.toLowerCase();
      return people.value.filter(person =>
        person.name?.toLowerCase().includes(term) ||
        person.cpf?.includes(term) ||
        person.email?.toLowerCase().includes(term)
      );
    });

    const totalPages = computed(() => {
      return Math.ceil(filteredPeople.value.length / itemsPerPage);
    });

    const paginatedPeople = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage;
      const end = start + itemsPerPage;
      return filteredPeople.value.slice(start, end);
    });

    const visiblePages = computed(() => {
      const pages = [];
      const maxVisible = 5;

      if (totalPages.value <= maxVisible) {
        for (let i = 1; i <= totalPages.value; i++) {
          pages.push(i);
        }
      } else {
        if (currentPage.value <= 3) {
          for (let i = 1; i <= 4; i++) pages.push(i);
          pages.push('...');
          pages.push(totalPages.value);
        } else if (currentPage.value >= totalPages.value - 2) {
          pages.push(1);
          pages.push('...');
          for (let i = totalPages.value - 3; i <= totalPages.value; i++) pages.push(i);
        } else {
          pages.push(1);
          pages.push('...');
          for (let i = currentPage.value - 1; i <= currentPage.value + 1; i++) pages.push(i);
          pages.push('...');
          pages.push(totalPages.value);
        }
      }

      return pages;
    });

    const goToPage = (page) => {
      if (typeof page === 'number' && page >= 1 && page <= totalPages.value) {
        currentPage.value = page;
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    };

    const loadPeople = async () => {
      loading.value = true;
      error.value = null;
      try {
        people.value = await peopleService.getAll();
      } catch (err) {
        error.value = 'Erro ao carregar pessoas: ' + (err.response?.data?.message || err.message);
      } finally {
        loading.value = false;
      }
    };

    const formatCPF = (cpf) => {
      if (!cpf) return '-';
      return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    };

    const formatDate = (date) => {
      if (!date) return '-';
      return new Date(date).toLocaleDateString('pt-BR');
    };

    const viewDetails = (person) => {
      router.push(`/people/${person.id}`);
    };

    const editPerson = (person) => {
      router.push(`/people/${person.id}?edit=true`);
    };

    const formatCPFInput = (event) => {
      let value = event.target.value.replace(/\D/g, '');
      if (value.length > 11) value = value.slice(0, 11);

      if (value.length > 9) {
        value = value.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
      } else if (value.length > 6) {
        value = value.replace(/(\d{3})(\d{3})(\d{1,3})/, '$1.$2.$3');
      } else if (value.length > 3) {
        value = value.replace(/(\d{3})(\d{1,3})/, '$1.$2');
      } else {
        createError.value = 'CPF deve conter apenas números e no máximo 11 dígitos';
      }
      createForm.value.cpf = value;
    };

    const handleCreate = async () => {
      const cpfDigits = createForm.value.cpf.replace(/\D/g, '');

      if (!createForm.value.name || !createForm.value.email || !createForm.value.cpf) {
        createError.value = 'Nome, CPF e e-mail são obrigatórios';
        return;
      }

      if (cpfDigits.length !== 11) {
        createError.value = 'CPF deve conter exatamente 11 dígitos numéricos';
        return;
      }

      creating.value = true;
      createError.value = null;

      try {
        await peopleService.create({
          ...createForm.value,
          cpf: cpfDigits
        });

        closeCreateModal();
        loadPeople();
      } catch (err) {
        createError.value = err.response?.data?.message || 'Erro ao criar pessoa';
      } finally {
        creating.value = false;
      }
    };

    const closeCreateModal = () => {
      showCreateModal.value = false;
      createForm.value = {
        name: '',
        email: '',
        cpf: '',
        birthDate: '',
        gender: '',
        educationLevel: '',
        address: '',
        city: ''
      };
      createError.value = null;
    };

    const handleFileSelect = (event) => {
      selectedFile.value = event.target.files[0];
      uploadError.value = null;
      uploadSuccess.value = null;
    };

    const handleDragOver = () => {
      isDragging.value = true;
    };

    const handleDragLeave = () => {
      isDragging.value = false;
    };

    const handleDrop = (event) => {
      isDragging.value = false;
      const file = event.dataTransfer.files[0];
      if (file && (file.name.endsWith('.xlsx') || file.name.endsWith('.xls'))) {
        selectedFile.value = file;
        uploadError.value = null;
        uploadSuccess.value = null;
      } else if (file) {
        uploadError.value = 'Formato inválido. Use arquivos .xlsx ou .xls.';
      }
    };

    const uploadFile = async () => {
      if (!selectedFile.value) return;

      uploading.value = true;
      uploadError.value = null;
      uploadSuccess.value = null;
      uploadResult.value = null;

      try {
        const response = await peopleService.importExcel(selectedFile.value);
        uploadResult.value = response;

        if (response.successfullyInserted > 0) {
          uploadSuccess.value = `${response.successfullyInserted} pessoa(s) importada(s) com sucesso!`;
        }

        const hasErrors = response.duplicatePersons && response.duplicatePersons.length > 0;

        // MUDANÇA AQUI: Só fecha o modal automaticamente se tudo deu certo e não teve erros
        if (response.successfullyInserted > 0 && !hasErrors) {
          setTimeout(() => {
            closeUploadModal();
            loadPeople();
          }, 2000);
        } else {
          // Recarrega a lista silenciosamente por trás, mas deixa o modal aberto para o usuário ver o erro
          loadPeople();
        }
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
      uploadResult.value = null;
      if (fileInput.value) {
        fileInput.value.value = '';
      }
    };

    watch(searchTerm, () => {
      currentPage.value = 1;
    });

    onMounted(() => {
      loadPeople();
    });

    return {
      people,
      loading,
      error,
      searchTerm,
      filteredPeople,
      paginatedPeople,
      currentPage,
      totalPages,
      totalPeople,
      visiblePages,
      itemsPerPage,
      showUploadModal,
      showCreateModal,
      selectedFile,
      uploading,
      uploadError,
      uploadSuccess,
      uploadResult,
      fileInput,
      isDragging,
      createForm,
      creating,
      createError,
      formatCPF,
      formatDate,
      viewDetails,
      editPerson,
      handleFileSelect,
      handleDragOver,
      handleDragLeave,
      handleDrop,
      uploadFile,
      closeUploadModal,
      handleCreate,
      closeCreateModal,
      formatCPFInput,
      goToPage
    };
  }
};
</script>

<style scoped>
/* (styles omitted for brevity in backup) */
</style>