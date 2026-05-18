<template>
  <div class="pessoa-perfil-view">
    <!-- Header do Perfil -->
    <header class="header">
      <div class="header-top">
        <button @click="goBack" class="btn-back">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          Voltar para Pessoas
        </button>
        <div class="header-actions" v-if="person">
          <button class="btn-outline" @click="showRegisterVinculo = true">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="8.5" cy="7" r="4"></circle>
              <line x1="20" y1="8" x2="20" y2="14"></line>
              <line x1="23" y1="11" x2="17" y2="11"></line>
            </svg>
            Registrar novo vínculo
          </button>
          <button class="btn-outline" @click="showUpdateAcompanhamento = true">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="12" y1="19" x2="12" y2="11"></line>
              <polyline points="9 14 12 11 15 14"></polyline>
            </svg>
            Atualizar acompanhamento
          </button>
          <button class="btn-primary" @click="isEditing = true">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
            </svg>
            Editar perfil
          </button>
          <button class="btn-outline" aria-label="Mais ações" title="Mais ações">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="1"></circle>
              <circle cx="19" cy="12" r="1"></circle>
              <circle cx="5" cy="12" r="1"></circle>
            </svg>
          </button>
        </div>
      </div>

      <!-- Informações principais -->
      <div class="person-header" v-if="person && !loading">
        <div class="avatar-circle">
          {{ getInitials(person.name) }}
        </div>
        <div class="header-info">
          <div class="info-top">
            <div>
              <h1>{{ person.name }}</h1>
              <div class="person-meta">
                <span v-if="person.educationLevel">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M22 10v6m0 0l-8.5 4.75a2 2 0 0 1-1.972.224l-.978-.39m.978.39l-8.5-4.75m8.5 4.75v-5.5m0 5.5l8.5-4.75M2 10v6m0 0l8.5 4.75m0-5.5v5.5m0 0l8.5-4.75M2 10l8.5-4.75a2 2 0 0 1 1.972-.224l.978.39m-.978-.39L12 5.25m0 0L3.5 10"></path>
                  </svg>
                  {{ person.educationLevel }}
                </span>
                <span v-if="person.city">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                    <circle cx="12" cy="10" r="3"></circle>
                  </svg>
                  {{ person.city }}
                </span>
              </div>
            </div>
            <div class="status-badge active">Ativa</div>
          </div>

          <div class="info-grid">
            <div class="info-card">
              <div class="info-label">Programa Atual</div>
              <div class="info-value">-</div>
            </div>
            <div class="info-card">
              <div class="info-label">Etapa Atual</div>
              <div class="info-value">-</div>
            </div>
            <div class="info-card">
              <div class="info-label">Desempenho</div>
              <div class="info-value" style="color: #0d9488;">-</div>
            </div>
            <div class="info-card">
              <div class="info-label">Última Atualização</div>
              <div class="info-value">{{ formatDate(person.updatedAt) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="tabs-header">
        <div class="tabs-container" role="tablist" aria-label="Person profile sections">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            @click="activeTab = tab.id"
            :class="['tab-btn', { active: activeTab === tab.id }]"
            role="tab"
            :aria-selected="activeTab === tab.id ? 'true' : 'false'"
            :id="`tab-${tab.id}`"
            :aria-controls="`panel-${tab.id}`"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>
    </header>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Carregando perfil...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      <p>{{ error }}</p>
    </div>

    <!-- Content -->
    <div v-else class="content">
      <!-- Resumo Tab -->
      <div v-if="activeTab === 'resumo'" class="tab-content" id="panel-resumo" role="tabpanel" aria-labelledby="tab-resumo">
        <div class="metrics-grid">
          <div class="metric-card">
            <div class="metric-header">
              <div class="metric-label">Programas Vinculados</div>
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
              </svg>
            </div>
            <div class="metric-value">0</div>
            <div class="metric-note">0 ativo</div>
          </div>

          <div class="metric-card">
            <div class="metric-header">
              <div class="metric-label">Cursos Concluídos</div>
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="22 10 13.5 15.1 2 10"></polyline>
                <polyline points="22 4 13.5 9.1 2 4"></polyline>
                <polyline points="2 16.5v7.5a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-7.5"></polyline>
              </svg>
            </div>
            <div class="metric-value">0</div>
            <div class="metric-note">0h de formação</div>
          </div>

          <div class="metric-card">
            <div class="metric-header">
              <div class="metric-label">Presença</div>
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </div>
            <div class="metric-value">0%</div>
            <div class="metric-note">-</div>
          </div>

          <div class="metric-card">
            <div class="metric-header">
              <div class="metric-label">Situação Profissional</div>
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
              </svg>
            </div>
            <div class="metric-value">-</div>
            <div class="metric-note">-</div>
          </div>
        </div>

        <div class="progress-section">
          <div class="progress-card">
            <h3>Progresso no Programa Atual</h3>
            <p class="no-data">Sem dados de programa ativo</p>
          </div>

          <div class="timeline-card">
            <h3>Eventos Recentes</h3>
            <p class="no-data">Sem eventos registrados</p>
          </div>
        </div>
      </div>

      <!-- Dados Pessoais Tab -->
      <div v-if="activeTab === 'dados-pessoais'" class="tab-content" id="panel-dados-pessoais" role="tabpanel" aria-labelledby="tab-dados-pessoais">
        <div class="data-card">
          <h3>Informações Pessoais</h3>
          <div class="data-grid">
            <div class="data-item">
              <div class="data-label">Nome Completo</div>
              <div class="data-value">{{ person.name }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">CPF</div>
              <div class="data-value">{{ person.cpf || '-' }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">E-mail</div>
              <div class="data-value">{{ person.email }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">Telefone</div>
              <div class="data-value">{{ person.phone || '-' }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">Data de Nascimento</div>
              <div class="data-value">{{ formatDate(person.birthDate) }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">Gênero</div>
              <div class="data-value">{{ person.gender || '-' }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">Estado</div>
              <div class="data-value">{{ person.state || '-' }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">Cidade</div>
              <div class="data-value">{{ person.city || '-' }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Dados Acadêmicos Tab -->
      <div v-if="activeTab === 'dados-academicos'" class="tab-content" id="panel-dados-academicos" role="tabpanel" aria-labelledby="tab-dados-academicos">
        <div class="data-card">
          <h3>Informações Acadêmicas</h3>
          <div class="data-grid">
            <div class="data-item">
              <div class="data-label">Tipo de Formação</div>
              <div class="data-value">{{ person.educationLevel || '-' }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">Instituição</div>
              <div class="data-value">{{ person.institution || '-' }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">Curso</div>
              <div class="data-value">{{ person.course || '-' }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">Status da Formação</div>
              <div class="data-value">{{ person.educationStatus || '-' }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Placeholder para outras abas -->
      <div v-for="tab in tabs.filter(t => !['resumo', 'dados-pessoais', 'dados-academicos'].includes(t.id))" :key="tab.id">
        <div v-if="activeTab === tab.id" class="tab-content" :id="`panel-${tab.id}`" role="tabpanel" :aria-labelledby="`tab-${tab.id}`">
          <div class="empty-state">
            <p>{{ tab.label }} - Em desenvolvimento</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Modals implemented as components -->
    <EditPersonModal v-if="isEditing" :personId="route.params.id" @close="isEditing = false" @saved="loadPerson" />
    <EnrollmentModal v-if="showRegisterVinculo" :personId="route.params.id" @close="showRegisterVinculo = false" @saved="loadPerson" />
    <FollowUpModal v-if="showUpdateAcompanhamento" :personId="route.params.id" @close="showUpdateAcompanhamento = false" @saved="loadPerson" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { peopleService } from '@/services/peopleService';
import EditPersonModal from '@/components/EditPersonModal.vue';
import EnrollmentModal from '@/components/EnrollmentModal.vue';
import FollowUpModal from '@/components/FollowUpModal.vue';

const route = useRoute();
const router = useRouter();

const person = ref(null);
const loading = ref(true);
const error = ref('');
const activeTab = ref('resumo');
const isEditing = ref(false);
const showRegisterVinculo = ref(false);
const showUpdateAcompanhamento = ref(false);

const tabs = [
  { id: 'resumo', label: 'Resumo' },
  { id: 'dados-pessoais', label: 'Dados Pessoais' },
  { id: 'dados-academicos', label: 'Dados Acadêmicos' },
  { id: 'participacoes', label: 'Participações em Programas' },
  { id: 'desempenho', label: 'Desempenho no Programa' },
  { id: 'acompanhamento', label: 'Carreira' },
  { id: 'documentos', label: 'Documentos' },
  { id: 'historico', label: 'Histórico' },
];

const loadPerson = async () => {
  try {
    loading.value = true;
    error.value = '';
    const id = route.params.id;
    const data = await peopleService.getById(id);
    person.value = data;
  } catch (err) {
    error.value = 'Erro ao carregar dados da pessoa';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  router.push('/people');
};

const getInitials = (name) => {
  if (!name) return '?';
  return name
    .split(' ')
    .slice(0, 2)
    .map(n => n[0])
    .join('')
    .toUpperCase();
};

const formatDate = (date) => {
  if (!date) return '-';
  return new Date(date).toLocaleDateString('pt-BR');
};

onMounted(() => {
  loadPerson();
});
</script>

<style scoped>
.pessoa-perfil-view {
  background: #f8fafc;
  min-height: 100vh;
}

.header {
  background: white;
  border-bottom: 1px solid #e2eaf2;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 32px;
  border-bottom: 1px solid #e2eaf2;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  cursor: pointer;
  color: #5f728d;
  font-size: 14px;
  font-weight: 600;
  transition: color 0.2s;
}

.btn-back:hover {
  color: #13233f;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.btn-outline,
.btn-primary,
.btn-close {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  min-height: 48px;
  border-radius: 10px;
  border: 1px solid #d8e1eb;
  background: white;
  color: #13233f;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-outline:hover {
  background: #f8fafc;
  border-color: #cfd9e6;
}

.btn-primary {
  border-color: #14b8a6;
  background: #14b8a6;
  color: white;
}

.btn-primary:hover {
  background: #0d9488;
  border-color: #0d9488;
}

.btn-close {
  padding: 0;
  width: 32px;
  height: 32px;
  justify-content: center;
}

.person-header {
  display: flex;
  gap: 24px;
  padding: 20px 32px;
  border-bottom: 1px solid #e2eaf2;
}

.avatar-circle {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: linear-gradient(135deg, #14b8a6, #0d9488);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
  font-weight: 600;
  flex-shrink: 0;
}

.header-info {
  flex: 1;
}

.info-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.info-top h1 {
  font-size: 24px;
  font-weight: 600;
  color: #13233f;
  margin: 0 0 8px;
}

.person-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #5f728d;
}

.person-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.active {
  background: #d1fae5;
  color: #065f46;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.info-card {
  background: #f8fafc;
  border: 1px solid #e2eaf2;
  border-radius: 10px;
  padding: 12px;
}

.info-label {
  font-size: 11px;
  font-weight: 600;
  color: #5f728d;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #13233f;
}

.tabs-header {
  border-top: 1px solid #e2eaf2;
  overflow-x: auto;
}

.tabs-container {
  display: flex;
  padding: 0 32px;
}

.tab-btn {
  padding: 14px 16px;
  border: none;
  background: none;
  cursor: pointer;
  color: #5f728d;
  font-size: 14px;
  font-weight: 600;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  white-space: nowrap;
}

.tab-btn:hover {
  color: #13233f;
}

.tab-btn.active {
  color: #0f766e;
  border-bottom-color: #14b8a6;
}

.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px 32px;
  text-align: center;
  color: #5f728d;
}

.error-state {
  color: #b42318;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e2eaf2;
  border-top-color: #14b8a6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.content {
  padding: 32px;
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.metric-card {
  background: white;
  border: 1px solid #e2eaf2;
  border-radius: 12px;
  padding: 20px;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.metric-label {
  font-size: 12px;
  color: #5f728d;
  font-weight: 600;
}

.metric-header svg {
  color: #14b8a6;
}

.metric-value {
  font-size: 24px;
  font-weight: 600;
  color: #13233f;
  margin-bottom: 4px;
}

.metric-note {
  font-size: 12px;
  color: #8a98ab;
}

.progress-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.progress-card,
.timeline-card,
.data-card {
  background: white;
  border: 1px solid #e2eaf2;
  border-radius: 12px;
  padding: 24px;
}

.progress-card h3,
.timeline-card h3,
.data-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #13233f;
  margin: 0 0 16px;
}

.no-data {
  color: #8a98ab;
  font-size: 14px;
  text-align: center;
  padding: 32px 0;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.data-item {
  border-bottom: 1px solid #e2eaf2;
  padding-bottom: 16px;
}

.data-item:last-child {
  border-bottom: none;
}

.data-label {
  font-size: 12px;
  color: #5f728d;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 6px;
}

.data-value {
  font-size: 14px;
  font-weight: 500;
  color: #13233f;
}

.empty-state {
  background: white;
  border: 1px solid #e2eaf2;
  border-radius: 12px;
  padding: 60px 32px;
  text-align: center;
  color: #8a98ab;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.modal-card {
  background: white;
  border-radius: 14px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2eaf2;
}

.modal-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #13233f;
  margin: 0;
}

.modal-body {
  padding: 24px;
  color: #5f728d;
}

@media (max-width: 1024px) {
  .info-grid,
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .info-grid,
  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .progress-section {
    grid-template-columns: 1fr;
  }

  .header-top,
  .person-header,
  .tabs-container,
  .content {
    padding-left: 16px;
    padding-right: 16px;
  }

  .header-actions {
    flex-wrap: wrap;
  }
}
</style>
