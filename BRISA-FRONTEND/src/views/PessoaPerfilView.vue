<template>
  <div class="pessoa-perfil-view">
    <ConfirmDialog ref="confirmDialog" />

    <header class="profile-shell">
      <div class="header-bar">
        <button @click="goBack" class="btn-back">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
          Voltar para Pessoas
        </button>

        <div class="header-actions" v-if="person">
          <button class="btn-outline" @click="showRegisterVinculo = true">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
              <circle cx="8.5" cy="7" r="4" />
              <line x1="20" y1="8" x2="20" y2="14" />
              <line x1="23" y1="11" x2="17" y2="11" />
            </svg>
            Registrar novo vínculo
          </button>

          <button class="btn-outline" @click="showUpdateAcompanhamento = true">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
              <line x1="12" y1="19" x2="12" y2="11" />
              <polyline points="9 14 12 11 15 14" />
            </svg>
            Atualizar acompanhamento
          </button>

          <button class="btn-primary" @click="isEditing = true">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
            </svg>
            Editar perfil
          </button>

          <div class="person-actions-menu-wrap">
            <button class="btn-icon" @click.stop="togglePersonActionsMenu" title="Mais ações" aria-label="Mais ações">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="5" r="1" />
                <circle cx="12" cy="12" r="1" />
                <circle cx="12" cy="19" r="1" />
              </svg>
            </button>
            <div v-if="showPersonActionsMenu" class="person-actions-menu">
              <button class="person-actions-menu-item danger" :disabled="deletingPerson" @click="handleSoftDelete">
                {{ deletingPerson ? 'Apagando...' : 'Apagar pessoa' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="person && !loading" class="hero-card">
        <div class="hero-identity">
          <div class="avatar-circle">
            {{ getInitials(person.name) }}
          </div>

          <div class="identity-copy">
            <div class="identity-row">
              <div>
                <h1>{{ person.name }}</h1>
                <p class="identity-subtitle">{{ profileSubtitle }}</p>
              </div>
              <span :class="['status-pill', `status-pill--${profileStatusVariant}`]">
                {{ profileStatusLabel }}
              </span>
            </div>

            <div class="meta-row">
              <span v-if="person.email">
                <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                  <polyline points="22,6 12,13 2,6" />
                </svg>
                {{ person.email }}
              </span>
              <span v-if="person.phone">
                <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" />
                </svg>
                {{ formatPhone(person.phone) }}
              </span>
              <span v-if="locationLabel">
                <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0 1 18 0z" />
                  <circle cx="12" cy="10" r="3" />
                </svg>
                {{ locationLabel }}
              </span>
            </div>

            <div class="tag-row" v-if="profileChips.length">
              <span v-for="chip in profileChips" :key="chip" class="data-chip">
                {{ chip }}
              </span>
            </div>
          </div>
        </div>

        <div class="hero-summary">
          <div v-for="metric in heroMetrics" :key="metric.label" class="summary-card">
            <span class="summary-label">{{ metric.label }}</span>
            <strong class="summary-value">{{ metric.value }}</strong>
            <span class="summary-note">{{ metric.note }}</span>
          </div>
        </div>
      </div>

      <div class="tabs-header" v-if="!loading && person">
        <div class="tabs-scroll">
          <div class="tabs-container" role="tablist" aria-label="Seções do perfil">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              @click="activeTab = tab.id"
              :class="['tab-btn', { active: activeTab === tab.id }]"
              role="tab"
              :aria-selected="activeTab === tab.id ? 'true' : 'false'"
              :id="`profile-tab-${tab.id}`"
              :aria-controls="`profile-panel-${tab.id}`"
            >
              {{ tab.label }}
            </button>
          </div>
        </div>
      </div>
    </header>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Carregando perfil...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10" />
        <line x1="12" y1="8" x2="12" y2="12" />
        <line x1="12" y1="16" x2="12.01" y2="16" />
      </svg>
      <p>{{ error }}</p>
    </div>

    <main v-else-if="person" class="content-shell">
      <section
        v-if="activeTab === 'resumo'"
        class="tab-section"
        id="profile-panel-resumo"
        role="tabpanel"
        aria-labelledby="profile-tab-resumo"
      >
        <div class="overview-grid">
          <article class="panel">
            <div class="panel-header">
              <div>
                <h3>Visão geral</h3>
                <p>Resumo rápido do cadastro, vínculo e desempenho.</p>
              </div>
            </div>
            <div class="keyvalue-grid">
              <div v-for="item in summaryFacts" :key="item.label" class="keyvalue-item">
                <span class="keyvalue-label">{{ item.label }}</span>
                <strong class="keyvalue-value">{{ item.value }}</strong>
              </div>
            </div>
          </article>

          <article class="panel">
            <div class="panel-header">
              <div>
                <h3>Contato e localização</h3>
                <p>Dados de comunicação e endereço principal.</p>
              </div>
            </div>
            <div class="keyvalue-grid">
              <div v-for="item in contactFacts" :key="item.label" class="keyvalue-item">
                <span class="keyvalue-label">{{ item.label }}</span>
                <strong class="keyvalue-value">{{ item.value }}</strong>
              </div>
            </div>
          </article>
        </div>

        <div class="split-grid">
          <article class="panel">
            <div class="panel-header">
              <div>
                <h3>Vínculos recentes</h3>
                <p>{{ sortedEnrollments.length }} vínculo(s) encontrado(s).</p>
              </div>
            </div>

            <div v-if="recentEnrollments.length" class="stack-list">
              <article v-for="enrollment in recentEnrollments" :key="enrollment.id" class="list-card">
                <div class="list-card__top">
                  <div>
                    <h4>{{ resolveProgramName(enrollment) }}</h4>
                    <p>{{ resolveClassName(enrollment) }} • {{ resolveInstitutionName(enrollment) }}</p>
                  </div>
                  <span :class="['inline-pill', `inline-pill--${resolveEnrollmentTone(enrollment)}`]">
                    {{ enrollment.status || 'Sem status' }}
                  </span>
                </div>
                <div class="list-card__meta">
                  <span>{{ resolveRoleName(enrollment) }}</span>
                  <span>Entrada: {{ formatDate(enrollment.enrollmentDate) }}</span>
                  <span>Conclusão: {{ formatDate(enrollment.completionDate) }}</span>
                </div>
              </article>
            </div>

            <div v-else class="empty-state">
              <p>Sem vínculos registrados para esta pessoa.</p>
            </div>
          </article>

          <article class="panel">
            <div class="panel-header">
              <div>
                <h3>Linha do tempo</h3>
                <p>Últimos eventos relevantes do perfil.</p>
              </div>
            </div>

            <div v-if="timelinePreview.length" class="timeline-list">
              <article v-for="item in timelinePreview" :key="item.id" class="timeline-item">
                <div class="timeline-marker"></div>
                <div class="timeline-body">
                  <div class="timeline-top">
                    <strong>{{ item.title }}</strong>
                    <span>{{ formatDateTime(item.date) }}</span>
                  </div>
                  <p>{{ item.description }}</p>
                </div>
              </article>
            </div>

            <div v-else class="empty-state">
              <p>Sem eventos suficientes para montar a linha do tempo.</p>
            </div>
          </article>
        </div>
      </section>

      <section
        v-if="activeTab === 'dados-pessoais'"
        class="tab-section"
        id="profile-panel-dados-pessoais"
        role="tabpanel"
        aria-labelledby="profile-tab-dados-pessoais"
      >
        <div class="detail-grid">
          <article class="panel">
            <div class="panel-header">
              <div>
                <h3>Identificação</h3>
                <p>Dados pessoais e documentação.</p>
              </div>
            </div>
            <div class="keyvalue-grid">
              <div v-for="item in personalFacts" :key="item.label" class="keyvalue-item">
                <span class="keyvalue-label">{{ item.label }}</span>
                <strong class="keyvalue-value">{{ item.value }}</strong>
              </div>
            </div>
          </article>

          <article class="panel">
            <div class="panel-header">
              <div>
                <h3>Contato</h3>
                <p>Como falar com essa pessoa hoje.</p>
              </div>
            </div>
            <div class="keyvalue-grid">
              <div v-for="item in communicationFacts" :key="item.label" class="keyvalue-item">
                <span class="keyvalue-label">{{ item.label }}</span>
                <strong class="keyvalue-value">{{ item.value }}</strong>
              </div>
            </div>
          </article>

          <article class="panel panel--full">
            <div class="panel-header">
              <div>
                <h3>Endereço</h3>
                <p>Localização residencial registrada.</p>
              </div>
            </div>
            <div class="keyvalue-grid">
              <div v-for="item in addressFacts" :key="item.label" class="keyvalue-item">
                <span class="keyvalue-label">{{ item.label }}</span>
                <strong class="keyvalue-value">{{ item.value }}</strong>
              </div>
            </div>
          </article>
        </div>
      </section>

      <section
        v-if="activeTab === 'dados-academicos'"
        class="tab-section"
        id="profile-panel-dados-academicos"
        role="tabpanel"
        aria-labelledby="profile-tab-dados-academicos"
      >
        <div class="detail-grid">
          <article class="panel">
            <div class="panel-header">
              <div>
                <h3>Formação</h3>
                <p>Base acadêmica informada no cadastro.</p>
              </div>
            </div>
            <div class="keyvalue-grid">
              <div v-for="item in academicFacts" :key="item.label" class="keyvalue-item">
                <span class="keyvalue-label">{{ item.label }}</span>
                <strong class="keyvalue-value">{{ item.value }}</strong>
              </div>
            </div>
          </article>

          <article class="panel">
            <div class="panel-header">
              <div>
                <h3>Elegibilidade</h3>
                <p>Informações úteis para seleção e acompanhamento.</p>
              </div>
            </div>
            <div class="keyvalue-grid">
              <div v-for="item in classificationFacts" :key="item.label" class="keyvalue-item">
                <span class="keyvalue-label">{{ item.label }}</span>
                <strong class="keyvalue-value">{{ item.value }}</strong>
              </div>
            </div>
          </article>
        </div>
      </section>

      <section
        v-if="activeTab === 'participacoes'"
        class="tab-section"
        id="profile-panel-participacoes"
        role="tabpanel"
        aria-labelledby="profile-tab-participacoes"
      >
        <article class="panel">
          <div class="panel-header">
            <div>
              <h3>Participações em programas</h3>
              <p>Histórico completo de vínculos desta pessoa.</p>
            </div>
          </div>

          <div v-if="sortedEnrollments.length" class="stack-list">
            <article v-for="enrollment in sortedEnrollments" :key="enrollment.id" class="list-card list-card--full">
              <div class="list-card__top">
                <div>
                  <h4>{{ resolveProgramName(enrollment) }}</h4>
                  <p>{{ resolveClassName(enrollment) }} • {{ resolveInstitutionName(enrollment) }}</p>
                </div>
                <span :class="['inline-pill', `inline-pill--${resolveEnrollmentTone(enrollment)}`]">
                  {{ enrollment.status || 'Sem status' }}
                </span>
              </div>

              <div class="detail-row">
                <div class="detail-pill">
                  <span class="detail-pill__label">Papel</span>
                  <strong>{{ resolveRoleName(enrollment) }}</strong>
                </div>
                <div class="detail-pill">
                  <span class="detail-pill__label">Data de entrada</span>
                  <strong>{{ formatDate(enrollment.enrollmentDate) }}</strong>
                </div>
                <div class="detail-pill">
                  <span class="detail-pill__label">Conclusão</span>
                  <strong>{{ formatDate(enrollment.completionDate) }}</strong>
                </div>
                <div class="detail-pill">
                  <span class="detail-pill__label">Nota final</span>
                  <strong>{{ formatMetricValue(enrollment.grade, 1, '-') }}</strong>
                </div>
                <div class="detail-pill">
                  <span class="detail-pill__label">Frequência</span>
                  <strong>{{ formatPercentValue(enrollment.frequency) }}</strong>
                </div>
              </div>
            </article>
          </div>

          <div v-else class="empty-state">
            <p>Essa pessoa ainda não possui vínculos em programas ou turmas.</p>
          </div>
        </article>
      </section>

      <section
        v-if="activeTab === 'desempenho'"
        class="tab-section"
        id="profile-panel-desempenho"
        role="tabpanel"
        aria-labelledby="profile-tab-desempenho"
      >
        <div class="stats-grid">
          <article v-for="item in performanceMetrics" :key="item.label" class="stats-card">
            <span class="stats-card__label">{{ item.label }}</span>
            <strong class="stats-card__value">{{ item.value }}</strong>
            <span class="stats-card__note">{{ item.note }}</span>
          </article>
        </div>

        <article class="panel">
          <div class="panel-header">
            <div>
              <h3>Progressão por curso</h3>
              <p>Leitura direta do que já foi concluído, está em andamento ou sem avanço.</p>
            </div>
          </div>

          <div v-if="sortedProgressions.length" class="stack-list">
            <article v-for="progression in sortedProgressions" :key="progression.id" class="list-card list-card--full">
              <div class="list-card__top">
                <div>
                  <h4>{{ progression.course?.name || 'Curso sem nome' }}</h4>
                  <p>{{ progression.course?.code || 'Sem código' }} • {{ progression.classModel?.code || 'Turma não informada' }}</p>
                </div>
                <span :class="['inline-pill', `inline-pill--${resolveProgressionTone(progression)}`]">
                  {{ progression.status || 'Sem status' }}
                </span>
              </div>

              <div class="progress-row">
                <div class="progress-bar">
                  <span class="progress-fill" :style="{ width: `${clampProgress(progression.completionPercentage)}%` }"></span>
                </div>
                <strong>{{ clampProgress(progression.completionPercentage) }}%</strong>
              </div>

              <div class="detail-row">
                <div class="detail-pill">
                  <span class="detail-pill__label">Nota</span>
                  <strong>{{ formatMetricValue(progression.grade, 1, '-') }}</strong>
                </div>
                <div class="detail-pill">
                  <span class="detail-pill__label">Frequência</span>
                  <strong>{{ formatPercentValue(progression.frequency) }}</strong>
                </div>
                <div class="detail-pill">
                  <span class="detail-pill__label">Carga horária</span>
                  <strong>{{ formatHours(progression.course?.workload) }}</strong>
                </div>
                <div class="detail-pill">
                  <span class="detail-pill__label">Último acesso</span>
                  <strong>{{ formatDate(progression.lastAccess || progression.date) }}</strong>
                </div>
              </div>
            </article>
          </div>

          <div v-else class="empty-state">
            <p>Sem progressões registradas para os cursos desta pessoa.</p>
          </div>
        </article>
      </section>

      <section
        v-if="activeTab === 'acompanhamento'"
        class="tab-section"
        id="profile-panel-acompanhamento"
        role="tabpanel"
        aria-labelledby="profile-tab-acompanhamento"
      >
        <div class="detail-grid">
          <article class="panel">
            <div class="panel-header">
              <div>
                <h3>Panorama de carreira</h3>
                <p>Último retorno registrado pelo egresso ou pela equipe.</p>
              </div>
            </div>

            <div v-if="latestFollowUp" class="career-summary">
              <div class="career-summary__status">
                <span class="keyvalue-label">Situação atual</span>
                <strong>{{ formatCareerStatus(latestFollowUp.status) }}</strong>
                <small>Atualizado em {{ formatDate(latestFollowUp.surveyDate || latestFollowUp.updatedAt) }}</small>
              </div>
              <div class="career-summary__grid">
                <div class="detail-pill">
                  <span class="detail-pill__label">Empresa</span>
                  <strong>{{ latestFollowUp.company || '-' }}</strong>
                </div>
                <div class="detail-pill">
                  <span class="detail-pill__label">Cargo</span>
                  <strong>{{ latestFollowUp.position || '-' }}</strong>
                </div>
                <div class="detail-pill">
                  <span class="detail-pill__label">Programa</span>
                  <strong>{{ latestFollowUp.programName || currentProgramLabel }}</strong>
                </div>
                <div class="detail-pill">
                  <span class="detail-pill__label">Turma</span>
                  <strong>{{ latestFollowUp.classCode || currentClassLabel }}</strong>
                </div>
              </div>

              <div v-if="latestFollowUp.notes" class="notes-box">
                {{ latestFollowUp.notes }}
              </div>
            </div>

            <div v-else class="empty-state">
              <p>Sem acompanhamento de carreira registrado até o momento.</p>
            </div>
          </article>

          <article class="panel">
            <div class="panel-header">
              <div>
                <h3>Histórico de acompanhamentos</h3>
                <p>{{ sortedFollowUps.length }} registro(s) disponível(is).</p>
              </div>
            </div>

            <div v-if="sortedFollowUps.length" class="timeline-list">
              <article v-for="item in sortedFollowUps" :key="item.id" class="timeline-item">
                <div class="timeline-marker timeline-marker--career"></div>
                <div class="timeline-body">
                  <div class="timeline-top">
                    <strong>{{ formatCareerStatus(item.status) }}</strong>
                    <span>{{ formatDate(item.surveyDate || item.updatedAt) }}</span>
                  </div>
                  <p>{{ buildCareerDescription(item) }}</p>
                </div>
              </article>
            </div>

            <div v-else class="empty-state">
              <p>Nenhum histórico de carreira para exibir.</p>
            </div>
          </article>
        </div>
      </section>

      <section
        v-if="activeTab === 'historico'"
        class="tab-section"
        id="profile-panel-historico"
        role="tabpanel"
        aria-labelledby="profile-tab-historico"
      >
        <article class="panel">
          <div class="panel-header">
            <div>
              <h3>Histórico consolidado</h3>
              <p>Cadastro, vínculos, progressões e acompanhamentos em ordem cronológica.</p>
            </div>
          </div>

          <div v-if="timelineEntries.length" class="timeline-list">
            <article v-for="item in timelineEntries" :key="item.id" class="timeline-item">
              <div :class="['timeline-marker', `timeline-marker--${item.tone}`]"></div>
              <div class="timeline-body">
                <div class="timeline-top">
                  <strong>{{ item.title }}</strong>
                  <span>{{ formatDateTime(item.date) }}</span>
                </div>
                <p>{{ item.description }}</p>
              </div>
            </article>
          </div>

          <div v-else class="empty-state">
            <p>Não há eventos suficientes para montar o histórico.</p>
          </div>
        </article>
      </section>
    </main>

    <EditPersonModal
      v-if="isEditing"
      :personId="route.params.id"
      @close="isEditing = false"
      @saved="handleDataChanged"
    />
    <EnrollmentModal
      v-if="showRegisterVinculo"
      :personId="route.params.id"
      @close="showRegisterVinculo = false"
      @saved="handleDataChanged"
    />
    <FollowUpModal
      v-if="showUpdateAcompanhamento"
      :personId="route.params.id"
      @close="showUpdateAcompanhamento = false"
      @saved="handleDataChanged"
    />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { peopleService } from '@/services/peopleService';
import { enrollmentService } from '@/services/enrollmentService';
import { careerService } from '@/services/careerService';
import { courseService } from '@/services/courseService';
import { logService } from '@/services/logService';
import EditPersonModal from '@/components/EditPersonModal.vue';
import EnrollmentModal from '@/components/EnrollmentModal.vue';
import FollowUpModal from '@/components/FollowUpModal.vue';
import ConfirmDialog from '@/components/ConfirmDialog.vue';

const route = useRoute();
const router = useRouter();

const person = ref(null);
const enrollments = ref([]);
const progressions = ref([]);
const followUps = ref([]);
const historyLogs = ref([]);
const loading = ref(true);
const error = ref('');
const activeTab = ref('resumo');
const isEditing = ref(false);
const showRegisterVinculo = ref(false);
const showUpdateAcompanhamento = ref(false);
const showPersonActionsMenu = ref(false);
const deletingPerson = ref(false);
const confirmDialog = ref(null);

const tabs = [
  { id: 'resumo', label: 'Resumo' },
  { id: 'dados-pessoais', label: 'Dados pessoais' },
  { id: 'dados-academicos', label: 'Dados acadêmicos' },
  { id: 'participacoes', label: 'Vínculos' },
  { id: 'desempenho', label: 'Desempenho' },
  { id: 'acompanhamento', label: 'Carreira' },
  { id: 'historico', label: 'Histórico' }
];

const ACTIVE_ENROLLMENT_STATUSES = new Set(['ativo', 'ativa', 'em andamento', 'matriculado', 'matriculada', 'nivelamento', 'imersao', 'imersão']);
const COMPLETED_ENROLLMENT_STATUSES = new Set(['concluido', 'concluida', 'concluído', 'concluída', 'finalizado', 'finalizada', 'egresso', 'formado']);
const COMPLETED_PROGRESSION_STATUSES = new Set(['concluido', 'concluida', 'concluído', 'concluída', 'finalizado', 'finalizada', 'realizado']);

function normalizeText(value) {
  return (value || '')
    .toString()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .trim()
    .toLowerCase();
}

function hasValue(value) {
  return value !== null && value !== undefined && String(value).trim() !== '';
}

function asDate(value) {
  if (!value) return null;
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}

function formatDate(value) {
  const parsed = asDate(value);
  return parsed ? parsed.toLocaleDateString('pt-BR') : '-';
}

function formatDateTime(value) {
  const parsed = asDate(value);
  return parsed ? parsed.toLocaleString('pt-BR') : '-';
}

function formatMetricValue(value, digits = 0, fallback = '0') {
  const numeric = Number(value);
  if (!Number.isFinite(numeric)) return fallback;
  return numeric.toLocaleString('pt-BR', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits
  });
}

function formatPercentValue(value) {
  const numeric = Number(value);
  return Number.isFinite(numeric) ? `${formatMetricValue(numeric, 0)}%` : '-';
}

function formatHours(value) {
  const numeric = Number(value);
  return Number.isFinite(numeric) && numeric > 0 ? `${formatMetricValue(numeric, 0)}h` : '-';
}

function formatCpf(value) {
  const digits = (value || '').replace(/\D/g, '');
  if (digits.length !== 11) return value || '-';
  return digits.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
}

function formatPhone(value) {
  const digits = (value || '').replace(/\D/g, '');
  if (digits.length === 11) return digits.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
  if (digits.length === 10) return digits.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
  return value || '-';
}

function getInitials(name) {
  if (!name) return '?';
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase();
}

function clampProgress(value) {
  const numeric = Number(value);
  if (!Number.isFinite(numeric)) return 0;
  return Math.max(0, Math.min(100, Math.round(numeric)));
}

function resolveProgramName(enrollment) {
  return enrollment?.classModel?.program?.name || enrollment?.classModel?.program?.code || 'Sem programa';
}

function resolveClassName(enrollment) {
  return enrollment?.classModel?.code || 'Turma não informada';
}

function resolveInstitutionName(enrollment) {
  return enrollment?.classModel?.location?.acronym || enrollment?.classModel?.location?.name || enrollment?.classModel?.locality || 'Sem escola';
}

function resolveRoleName(enrollment) {
  return enrollment?.academicRole?.name || 'Aluno';
}

function isActiveEnrollment(enrollment) {
  const normalized = normalizeText(enrollment?.status);
  if (!normalized) return false;
  return ACTIVE_ENROLLMENT_STATUSES.has(normalized);
}

function isCompletedProgression(progression) {
  const normalized = normalizeText(progression?.status);
  return clampProgress(progression?.completionPercentage) >= 100 || COMPLETED_PROGRESSION_STATUSES.has(normalized);
}

function isInProgressProgression(progression) {
  const completion = clampProgress(progression?.completionPercentage);
  if (completion > 0 && completion < 100) return true;
  const normalized = normalizeText(progression?.status);
  return normalized === 'em andamento';
}

function resolveEnrollmentTone(enrollment) {
  if (isActiveEnrollment(enrollment)) return 'success';
  if (COMPLETED_ENROLLMENT_STATUSES.has(normalizeText(enrollment?.status))) return 'neutral';
  return 'warning';
}

function resolveProgressionTone(progression) {
  if (isCompletedProgression(progression)) return 'success';
  if (isInProgressProgression(progression)) return 'info';
  return 'warning';
}

function formatCareerStatus(value) {
  const normalized = normalizeText(value);
  if (!normalized) return 'Sem status';
  if (normalized === 'empregado') return 'Empregado';
  if (normalized === 'desempregado') return 'Desempregado';
  if (normalized === 'empreendendo') return 'Empreendendo';
  if (normalized === 'estudando') return 'Estudando';
  if (normalized === 'sem_resposta' || normalized === 'sem resposta') return 'Sem resposta';
  return value;
}

function buildCareerDescription(item) {
  const parts = [formatCareerStatus(item?.status)];
  if (item?.company) parts.push(item.company);
  if (item?.position) parts.push(item.position);
  if (item?.notes) parts.push(item.notes);
  return parts.join(' • ');
}

function mapLogActionLabel(log) {
  const action = normalizeText(log?.action);
  if (action.includes('create')) return 'Cadastro atualizado';
  if (action.includes('update')) return 'Cadastro atualizado';
  if (action.includes('delete')) return 'Registro removido';
  if (action.includes('import')) return 'Importação relacionada';
  return 'Movimentação registrada';
}

const sortedEnrollments = computed(() =>
  [...enrollments.value].sort((left, right) => {
    const leftDate = asDate(left?.completionDate || left?.enrollmentDate || left?.createdAt)?.getTime() || 0;
    const rightDate = asDate(right?.completionDate || right?.enrollmentDate || right?.createdAt)?.getTime() || 0;
    return rightDate - leftDate;
  })
);

const activeEnrollments = computed(() => sortedEnrollments.value.filter(isActiveEnrollment));

const currentEnrollment = computed(() => activeEnrollments.value[0] || sortedEnrollments.value[0] || null);

const sortedProgressions = computed(() =>
  [...progressions.value].sort((left, right) => {
    const leftDate = asDate(left?.lastAccess || left?.date)?.getTime() || 0;
    const rightDate = asDate(right?.lastAccess || right?.date)?.getTime() || 0;
    return rightDate - leftDate;
  })
);

const sortedFollowUps = computed(() =>
  [...followUps.value].sort((left, right) => {
    const leftDate = asDate(left?.surveyDate || left?.updatedAt || left?.createdAt)?.getTime() || 0;
    const rightDate = asDate(right?.surveyDate || right?.updatedAt || right?.createdAt)?.getTime() || 0;
    return rightDate - leftDate;
  })
);

const latestFollowUp = computed(() => sortedFollowUps.value[0] || null);

const uniquePrograms = computed(() => {
  const seen = new Set();
  return sortedEnrollments.value.reduce((accumulator, enrollment) => {
    const id = enrollment?.classModel?.program?.id || resolveProgramName(enrollment);
    if (!id || seen.has(id)) return accumulator;
    seen.add(id);
    accumulator.push(resolveProgramName(enrollment));
    return accumulator;
  }, []);
});

const uniqueClasses = computed(() => {
  const seen = new Set();
  return sortedEnrollments.value.reduce((accumulator, enrollment) => {
    const id = enrollment?.classModel?.id || resolveClassName(enrollment);
    if (!id || seen.has(id)) return accumulator;
    seen.add(id);
    accumulator.push(resolveClassName(enrollment));
    return accumulator;
  }, []);
});

const completedProgressions = computed(() => sortedProgressions.value.filter(isCompletedProgression));
const inProgressProgressions = computed(() => sortedProgressions.value.filter(isInProgressProgression));

const totalWorkload = computed(() => {
  const seen = new Set();
  return completedProgressions.value.reduce((total, progression) => {
    const key = `${progression?.classModel?.id || 'class'}:${progression?.course?.id || progression?.id}`;
    if (seen.has(key)) return total;
    seen.add(key);
    const workload = Number(progression?.course?.workload);
    return Number.isFinite(workload) ? total + workload : total;
  }, 0);
});

const averageGrade = computed(() => {
  const gradeValues = sortedProgressions.value
    .map((item) => Number(item?.grade))
    .filter((value) => Number.isFinite(value));
  if (gradeValues.length) return gradeValues.reduce((sum, item) => sum + item, 0) / gradeValues.length;

  const enrollmentGrades = sortedEnrollments.value
    .map((item) => Number(item?.grade))
    .filter((value) => Number.isFinite(value));
  if (!enrollmentGrades.length) return null;
  return enrollmentGrades.reduce((sum, item) => sum + item, 0) / enrollmentGrades.length;
});

const averageFrequency = computed(() => {
  const frequencyValues = sortedProgressions.value
    .map((item) => Number(item?.frequency))
    .filter((value) => Number.isFinite(value));
  if (frequencyValues.length) return frequencyValues.reduce((sum, item) => sum + item, 0) / frequencyValues.length;

  const enrollmentFrequencies = sortedEnrollments.value
    .map((item) => Number(item?.frequency))
    .filter((value) => Number.isFinite(value));
  if (!enrollmentFrequencies.length) return null;
  return enrollmentFrequencies.reduce((sum, item) => sum + item, 0) / enrollmentFrequencies.length;
});

const averageCompletion = computed(() => {
  const completionValues = sortedProgressions.value
    .map((item) => clampProgress(item?.completionPercentage))
    .filter((value) => Number.isFinite(value));
  if (!completionValues.length) return null;
  return completionValues.reduce((sum, item) => sum + item, 0) / completionValues.length;
});

const currentProgramLabel = computed(() => resolveProgramName(currentEnrollment.value));
const currentClassLabel = computed(() => resolveClassName(currentEnrollment.value));
const currentInstitutionLabel = computed(() => resolveInstitutionName(currentEnrollment.value));
const currentRoleLabel = computed(() => resolveRoleName(currentEnrollment.value));

const currentJourneyLabel = computed(() => {
  const enrollment = currentEnrollment.value;
  if (!enrollment) return 'Sem vínculo';
  if (isActiveEnrollment(enrollment)) return enrollment.status || 'Em andamento';
  if (hasValue(enrollment?.completionDate)) return 'Concluído';
  return enrollment.status || 'Sem status';
});

const profileStatusLabel = computed(() => {
  if (activeEnrollments.value.length) return 'Em programa';
  if (latestFollowUp.value) return 'Egresso';
  if (sortedEnrollments.value.length) return 'Concluído';
  return 'Cadastro base';
});

const profileStatusVariant = computed(() => {
  if (activeEnrollments.value.length) return 'success';
  if (latestFollowUp.value) return 'info';
  if (sortedEnrollments.value.length) return 'neutral';
  return 'muted';
});

const locationLabel = computed(() => {
  const parts = [person.value?.city, person.value?.state].filter(hasValue);
  return parts.length ? parts.join(' • ') : '';
});

const profileSubtitle = computed(() => {
  const parts = [];
  if (hasValue(person.value?.courseName)) parts.push(person.value.courseName);
  if (hasValue(person.value?.institutionName)) parts.push(person.value.institutionName);
  if (!parts.length && hasValue(person.value?.educationLevel)) parts.push(person.value.educationLevel);
  return parts.length ? parts.join(' • ') : 'Perfil acadêmico e de carreira';
});

const profileChips = computed(() =>
  [person.value?.educationLevel, person.value?.quotaCategory, currentRoleLabel.value, currentInstitutionLabel.value]
    .filter(hasValue)
    .slice(0, 4)
);

const lastRelevantUpdate = computed(() => {
  const dates = [
    person.value?.updatedAt,
    person.value?.createdAt,
    currentEnrollment.value?.completionDate,
    currentEnrollment.value?.enrollmentDate,
    latestFollowUp.value?.updatedAt,
    latestFollowUp.value?.surveyDate,
    sortedProgressions.value[0]?.lastAccess,
    sortedProgressions.value[0]?.date
  ]
    .map(asDate)
    .filter(Boolean)
    .sort((left, right) => right.getTime() - left.getTime());
  return dates[0] || null;
});

const lastRelevantUpdateSource = computed(() => {
  if (latestFollowUp.value && formatDate(latestFollowUp.value.surveyDate || latestFollowUp.value.updatedAt) === formatDate(lastRelevantUpdate.value)) {
    return 'Carreira';
  }
  if (sortedProgressions.value.length && formatDate(sortedProgressions.value[0].lastAccess || sortedProgressions.value[0].date) === formatDate(lastRelevantUpdate.value)) {
    return 'Curso';
  }
  if (currentEnrollment.value && formatDate(currentEnrollment.value.completionDate || currentEnrollment.value.enrollmentDate) === formatDate(lastRelevantUpdate.value)) {
    return 'Vínculo';
  }
  return 'Cadastro';
});

const heroMetrics = computed(() => [
  {
    label: 'Programa atual',
    value: currentProgramLabel.value,
    note: currentClassLabel.value
  },
  {
    label: 'Jornada atual',
    value: currentJourneyLabel.value,
    note: currentRoleLabel.value
  },
  {
    label: 'Cursos concluídos',
    value: String(completedProgressions.value.length),
    note: `${formatHours(totalWorkload.value)} de carga horária`
  },
  {
    label: 'Última atualização',
    value: formatDate(lastRelevantUpdate.value),
    note: lastRelevantUpdateSource.value
  }
]);

const summaryFacts = computed(() => [
  { label: 'Programas vinculados', value: String(uniquePrograms.value.length) },
  { label: 'Turmas vinculadas', value: String(uniqueClasses.value.length) },
  { label: 'Vínculos ativos', value: String(activeEnrollments.value.length) },
  { label: 'Situação do vínculo', value: currentJourneyLabel.value },
  { label: 'Nota média', value: averageGrade.value !== null ? formatMetricValue(averageGrade.value, 1) : '-' },
  { label: 'Frequência média', value: averageFrequency.value !== null ? formatPercentValue(averageFrequency.value) : '-' },
  { label: 'Cursos concluídos', value: String(completedProgressions.value.length) },
  { label: 'Acompanhamentos de carreira', value: String(sortedFollowUps.value.length) }
]);

const contactFacts = computed(() => [
  { label: 'E-mail', value: person.value?.email || '-' },
  { label: 'Telefone', value: formatPhone(person.value?.phone) },
  { label: 'LinkedIn', value: person.value?.linkedin || '-' },
  { label: 'Cidade / UF', value: locationLabel.value || '-' },
  { label: 'CEP', value: person.value?.zipCode || '-' },
  { label: 'Endereço resumido', value: [person.value?.address, person.value?.addressExtra].filter(hasValue).join(', ') || '-' }
]);

const personalFacts = computed(() => [
  { label: 'Nome completo', value: person.value?.name || '-' },
  { label: 'CPF', value: formatCpf(person.value?.cpf) },
  { label: 'Data de nascimento', value: formatDate(person.value?.birthDate) },
  { label: 'Gênero', value: person.value?.gender || '-' },
  { label: 'Cota', value: person.value?.quotaCategory || '-' },
  { label: 'Última edição', value: formatDateTime(person.value?.updatedAt) }
]);

const communicationFacts = computed(() => [
  { label: 'E-mail principal', value: person.value?.email || '-' },
  { label: 'Telefone principal', value: formatPhone(person.value?.phone) },
  { label: 'LinkedIn', value: person.value?.linkedin || '-' },
  { label: 'Canal preferencial', value: person.value?.email || person.value?.phone || '-' }
]);

const addressFacts = computed(() => [
  { label: 'Estado', value: person.value?.state || '-' },
  { label: 'Cidade', value: person.value?.city || '-' },
  { label: 'Endereço', value: person.value?.address || '-' },
  { label: 'Complemento', value: person.value?.addressExtra || '-' },
  { label: 'CEP', value: person.value?.zipCode || '-' }
]);

const academicFacts = computed(() => [
  { label: 'Tipo de formação', value: person.value?.educationLevel || '-' },
  { label: 'Instituição', value: person.value?.institutionName || '-' },
  { label: 'Curso', value: person.value?.courseName || '-' },
  { label: 'Status da formação', value: person.value?.educationStatus || '-' },
  { label: 'Conclusão da formação', value: formatDate(person.value?.educationCompletionDate) }
]);

const classificationFacts = computed(() => [
  { label: 'Programa atual', value: currentProgramLabel.value },
  { label: 'Escola / parceiro', value: currentInstitutionLabel.value },
  { label: 'Turma principal', value: currentClassLabel.value },
  { label: 'Papel atual', value: currentRoleLabel.value },
  { label: 'Situação do perfil', value: profileStatusLabel.value }
]);

const performanceMetrics = computed(() => [
  {
    label: 'Cursos concluídos',
    value: String(completedProgressions.value.length),
    note: `${formatHours(totalWorkload.value)} acumulados`
  },
  {
    label: 'Cursos em andamento',
    value: String(inProgressProgressions.value.length),
    note: `${sortedProgressions.value.length} progressão(ões) no total`
  },
  {
    label: 'Conclusão média',
    value: averageCompletion.value !== null ? `${formatMetricValue(averageCompletion.value, 0)}%` : '-',
    note: 'Média geral de avanço'
  },
  {
    label: 'Nota / frequência',
    value: averageGrade.value !== null ? formatMetricValue(averageGrade.value, 1) : '-',
    note: averageFrequency.value !== null ? `Frequência ${formatPercentValue(averageFrequency.value)}` : 'Sem frequência consolidada'
  }
]);

const recentEnrollments = computed(() => sortedEnrollments.value.slice(0, 3));

const timelineEntries = computed(() => {
  const items = [];

  if (person.value) {
    items.push({
      id: `person-created-${person.value.id}`,
      date: person.value.createdAt,
      title: 'Cadastro criado',
      description: `${person.value.name} foi incluída na base.`,
      tone: 'neutral'
    });

    if (person.value.updatedAt && person.value.updatedAt !== person.value.createdAt) {
      items.push({
        id: `person-updated-${person.value.id}`,
        date: person.value.updatedAt,
        title: 'Perfil atualizado',
        description: 'Dados cadastrais revisados ou complementados.',
        tone: 'info'
      });
    }
  }

  sortedEnrollments.value.forEach((enrollment) => {
    items.push({
      id: `enrollment-start-${enrollment.id}`,
      date: enrollment.enrollmentDate || enrollment.classModel?.startDate,
      title: 'Ingresso em turma',
      description: `${resolveProgramName(enrollment)} • ${resolveClassName(enrollment)} • ${resolveRoleName(enrollment)}`,
      tone: 'success'
    });

    if (enrollment.completionDate) {
      items.push({
        id: `enrollment-end-${enrollment.id}`,
        date: enrollment.completionDate,
        title: 'Encerramento de vínculo',
        description: `${resolveProgramName(enrollment)} • ${resolveClassName(enrollment)}`,
        tone: 'warning'
      });
    }
  });

  completedProgressions.value.slice(0, 10).forEach((progression) => {
    items.push({
      id: `progression-${progression.id}`,
      date: progression.lastAccess || progression.date,
      title: 'Curso concluído',
      description: `${progression.course?.name || 'Curso'} • ${progression.classModel?.code || 'Turma'}`,
      tone: 'success'
    });
  });

  sortedFollowUps.value.forEach((item) => {
    items.push({
      id: `career-${item.id}`,
      date: item.surveyDate || item.updatedAt || item.createdAt,
      title: 'Acompanhamento de carreira',
      description: buildCareerDescription(item),
      tone: 'career'
    });
  });

  historyLogs.value.forEach((log) => {
    items.push({
      id: `log-${log.id}`,
      date: log.createdAt,
      title: mapLogActionLabel(log),
      description: log.description || 'Movimentação registrada em sistema.',
      tone: 'info'
    });
  });

  return items
    .filter((item) => item.date)
    .sort((left, right) => (asDate(right.date)?.getTime() || 0) - (asDate(left.date)?.getTime() || 0));
});

const timelinePreview = computed(() => timelineEntries.value.slice(0, 6));

async function loadProfile() {
  loading.value = true;
  error.value = '';

  try {
    const peopleId = Number(route.params.id);
    const personData = await peopleService.getById(peopleId);
    person.value = personData;

    const [enrollmentData, followUpData, logsPage] = await Promise.all([
      enrollmentService.getByPeopleId(peopleId).catch(() => []),
      careerService.getFollowUps({ peopleId }).catch(() => []),
      logService.getLogs({
        entityType: 'People',
        entityId: String(peopleId),
        page: 0,
        size: 50,
        sortBy: 'createdAt',
        sortDirection: 'DESC'
      }).catch(() => null)
    ]);

    enrollments.value = Array.isArray(enrollmentData) ? enrollmentData : [];
    followUps.value = Array.isArray(followUpData) ? followUpData : [];
    historyLogs.value = Array.isArray(logsPage?.content) ? logsPage.content : [];

    const classIds = [...new Set(
      enrollments.value
        .map((enrollment) => enrollment?.classModel?.id)
        .filter(Boolean)
    )];

    if (!classIds.length) {
      progressions.value = [];
      return;
    }

    const progressionsByClass = await Promise.all(
      classIds.map((classId) => courseService.getProgressionsByClassId(classId).catch(() => []))
    );

    progressions.value = progressionsByClass
      .flat()
      .filter((item) => String(item?.people?.id || item?.peopleId || '') === String(peopleId));
  } catch (err) {
    error.value = err.response?.data?.message || 'Erro ao carregar dados da pessoa';
    console.error(err);
  } finally {
    loading.value = false;
  }
}

async function handleDataChanged() {
  isEditing.value = false;
  showRegisterVinculo.value = false;
  showUpdateAcompanhamento.value = false;
  await loadProfile();
}

function goBack() {
  router.push('/people');
}

function togglePersonActionsMenu() {
  showPersonActionsMenu.value = !showPersonActionsMenu.value;
}

async function handleSoftDelete() {
  showPersonActionsMenu.value = false;
  const confirmed = await confirmDialog.value?.show(
    `Tem certeza que deseja apagar ${person.value?.name || 'esta pessoa'}? Você poderá recuperar depois.`,
    'Apagar'
  );
  if (!confirmed) return;

  deletingPerson.value = true;
  error.value = '';
  try {
    await peopleService.delete(route.params.id);
    router.push('/people');
  } catch (err) {
    error.value = err.response?.data?.message || 'Erro ao apagar pessoa';
  } finally {
    deletingPerson.value = false;
  }
}

function handleDocumentClick(event) {
  if (!(event.target instanceof HTMLElement)) return;
  if (!event.target.closest('.person-actions-menu-wrap')) {
    showPersonActionsMenu.value = false;
  }
}

onMounted(() => {
  void loadProfile();
  document.addEventListener('click', handleDocumentClick);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick);
});
</script>

<style scoped>
.pessoa-perfil-view {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(32, 187, 178, 0.08), transparent 28%),
    linear-gradient(180deg, #f5f8fc 0%, #f8fafc 100%);
  color: #13233f;
}

.profile-shell {
  background: #ffffff;
  border-bottom: 1px solid #dce5ef;
  box-shadow: 0 8px 30px rgba(15, 23, 42, 0.05);
}

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 20px 32px 18px;
  border-bottom: 1px solid #e6edf5;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  color: #5f728d;
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
}

.btn-back:hover {
  color: #13233f;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.btn-outline,
.btn-primary,
.btn-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 46px;
  padding: 0 16px;
  border-radius: 12px;
  border: 1px solid #d4deea;
  background: #ffffff;
  color: #13233f;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-outline:hover,
.btn-icon:hover {
  border-color: #bfd0e4;
  background: #f8fbff;
}

.btn-primary {
  border-color: #18b6ac;
  background: linear-gradient(135deg, #1bb8ae 0%, #14a79d 100%);
  color: #ffffff;
}

.btn-primary:hover {
  box-shadow: 0 10px 24px rgba(27, 184, 174, 0.22);
}

.btn-icon {
  width: 46px;
  padding: 0;
}

.person-actions-menu-wrap {
  position: relative;
}

.person-actions-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 170px;
  padding: 6px;
  background: #ffffff;
  border: 1px solid #dbe6f2;
  border-radius: 12px;
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.12);
  z-index: 20;
}

.person-actions-menu-item {
  width: 100%;
  border: none;
  border-radius: 8px;
  background: transparent;
  padding: 10px 12px;
  text-align: left;
  font: inherit;
  cursor: pointer;
}

.person-actions-menu-item.danger {
  color: #b42318;
}

.person-actions-menu-item.danger:hover:not(:disabled) {
  background: #fff2f2;
}

.hero-card {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(320px, 0.95fr);
  gap: 24px;
  padding: 28px 32px 24px;
}

.hero-identity {
  display: flex;
  gap: 20px;
  min-width: 0;
}

.avatar-circle {
  width: 98px;
  height: 98px;
  flex: 0 0 98px;
  border-radius: 28px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #1db9b0 0%, #14958c 100%);
  color: #ffffff;
  font-size: 38px;
  font-weight: 800;
  letter-spacing: 0;
  box-shadow: 0 16px 28px rgba(20, 149, 140, 0.22);
}

.identity-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.identity-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.identity-row h1 {
  margin: 0;
  font-size: 38px;
  line-height: 1.04;
  color: #0f2240;
  overflow-wrap: anywhere;
}

.identity-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  line-height: 1.5;
  color: #5e7390;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}

.status-pill--success {
  background: #e7fbf5;
  color: #117a66;
}

.status-pill--info {
  background: #ebf4ff;
  color: #2956c7;
}

.status-pill--neutral {
  background: #f1f5f9;
  color: #43536b;
}

.status-pill--muted {
  background: #f8fafc;
  color: #6b7d93;
}

.meta-row,
.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.meta-row span,
.data-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}

.meta-row span {
  color: #40536e;
  background: #f6f8fc;
}

.data-chip {
  color: #0f2240;
  background: #eef4fb;
}

.hero-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  padding: 16px;
  border-radius: 18px;
  border: 1px solid #dfe8f2;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  min-height: 112px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.summary-label {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #6a7d95;
}

.summary-value {
  font-size: 23px;
  line-height: 1.1;
  color: #10233f;
  overflow-wrap: anywhere;
}

.summary-note {
  font-size: 13px;
  color: #60738f;
}

.tabs-header {
  padding: 0 32px;
  border-top: 1px solid #edf2f7;
}

.tabs-scroll {
  overflow-x: auto;
}

.tabs-container {
  display: inline-flex;
  min-width: 100%;
  gap: 6px;
}

.tab-btn {
  border: none;
  border-bottom: 3px solid transparent;
  background: transparent;
  color: #4f6280;
  cursor: pointer;
  padding: 16px 14px 14px;
  font-size: 14px;
  font-weight: 800;
  white-space: nowrap;
}

.tab-btn.active {
  color: #0f2240;
  border-bottom-color: #18b6ac;
}

.content-shell {
  padding: 28px 32px 40px;
}

.tab-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.overview-grid,
.split-grid,
.detail-grid {
  display: grid;
  gap: 20px;
}

.overview-grid,
.detail-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.split-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.panel {
  border: 1px solid #dce6f1;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.96);
  padding: 22px;
  box-shadow: 0 18px 34px rgba(15, 23, 42, 0.05);
  min-width: 0;
  overflow: hidden;
}

.panel--full {
  grid-column: 1 / -1;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
  min-width: 0;
}

.panel-header h3 {
  margin: 0;
  font-size: 20px;
  color: #10233f;
}

.panel-header p {
  margin: 6px 0 0;
  color: #667a93;
  font-size: 14px;
  line-height: 1.5;
}

.keyvalue-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.keyvalue-item {
  padding: 16px;
  border-radius: 16px;
  background: #f8fbff;
  border: 1px solid #e5eef8;
  min-height: 88px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.keyvalue-label,
.detail-pill__label,
.stats-card__label {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #6a7d95;
}

.keyvalue-value {
  font-size: 17px;
  line-height: 1.35;
  color: #13233f;
  word-break: break-word;
}

.stack-list,
.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-height: min(58vh, 520px);
  overflow-y: auto;
  padding-right: 4px;
  scrollbar-width: thin;
  scrollbar-color: #b8c6d8 transparent;
}

.overview-grid .stack-list,
.overview-grid .timeline-list,
.split-grid .stack-list,
.split-grid .timeline-list {
  max-height: 360px;
}

.stack-list::-webkit-scrollbar,
.timeline-list::-webkit-scrollbar {
  width: 8px;
}

.stack-list::-webkit-scrollbar-thumb,
.timeline-list::-webkit-scrollbar-thumb {
  background: #b8c6d8;
  border-radius: 999px;
}

.list-card {
  border: 1px solid #e3ebf5;
  border-radius: 18px;
  padding: 16px 18px;
  background: #fbfdff;
  min-width: 0;
}

.list-card--full {
  padding-bottom: 18px;
}

.list-card__top {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.list-card__top h4 {
  margin: 0;
  font-size: 18px;
  overflow-wrap: anywhere;
}

.list-card__top p {
  margin: 5px 0 0;
  color: #667a93;
  font-size: 14px;
  overflow-wrap: anywhere;
}

.list-card__meta,
.detail-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.list-card__meta span {
  padding: 7px 10px;
  border-radius: 999px;
  background: #eef4fb;
  color: #4f6380;
  font-size: 12px;
  font-weight: 700;
}

.detail-pill {
  flex: 1 1 160px;
  min-width: 0;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f7faff;
  border: 1px solid #e6edf7;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-pill strong {
  color: #13233f;
  font-size: 15px;
  overflow-wrap: anywhere;
}

.inline-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}

.inline-pill--success {
  background: #e7fbf5;
  color: #117a66;
}

.inline-pill--info {
  background: #eaf4ff;
  color: #2b58ca;
}

.inline-pill--warning {
  background: #fff3df;
  color: #a05a00;
}

.inline-pill--neutral {
  background: #f1f5f9;
  color: #43536b;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stats-card {
  padding: 18px;
  border-radius: 18px;
  border: 1px solid #dfe7f2;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 8px;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.05);
}

.stats-card__value {
  font-size: 26px;
  line-height: 1.1;
  color: #0f2240;
}

.stats-card__note {
  font-size: 13px;
  color: #62758f;
}

.progress-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 14px;
}

.progress-bar {
  flex: 1;
  height: 10px;
  border-radius: 999px;
  background: #e8eef6;
  overflow: hidden;
}

.progress-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #18b6ac 0%, #2e72d2 100%);
}

.career-summary {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.career-summary__status {
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(24, 182, 172, 0.1) 0%, rgba(46, 114, 210, 0.08) 100%);
  border: 1px solid #dbe8f5;
}

.career-summary__status strong {
  display: block;
  margin-top: 8px;
  font-size: 24px;
  color: #10233f;
}

.career-summary__status small {
  display: block;
  margin-top: 6px;
  color: #60738f;
}

.career-summary__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.notes-box {
  padding: 16px;
  border-radius: 16px;
  background: #f9fbfe;
  border: 1px solid #e7edf6;
  color: #415470;
  line-height: 1.6;
}

.timeline-item {
  display: grid;
  grid-template-columns: 16px minmax(0, 1fr);
  gap: 14px;
  align-items: start;
}

.timeline-marker {
  width: 12px;
  height: 12px;
  margin-top: 7px;
  border-radius: 999px;
  background: #a7b8cd;
  box-shadow: 0 0 0 4px #edf3fa;
}

.timeline-marker--success {
  background: #1aa084;
}

.timeline-marker--info {
  background: #2e72d2;
}

.timeline-marker--warning {
  background: #d98922;
}

.timeline-marker--career {
  background: #18b6ac;
}

.timeline-marker--neutral {
  background: #8295ad;
}

.timeline-body {
  padding-bottom: 14px;
  border-bottom: 1px dashed #dbe5f0;
}

.timeline-top {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.timeline-top strong {
  color: #122440;
  overflow-wrap: anywhere;
}

.timeline-top span {
  flex-shrink: 0;
  color: #6a7d95;
  font-size: 13px;
}

.timeline-body p {
  margin: 6px 0 0;
  color: #51657f;
  line-height: 1.6;
  overflow-wrap: anywhere;
}

.empty-state {
  padding: 24px;
  border-radius: 16px;
  border: 1px dashed #dbe5f0;
  background: #f8fbff;
  text-align: center;
  color: #647892;
}

.loading-state,
.error-state {
  min-height: 40vh;
  display: grid;
  place-items: center;
  gap: 12px;
  text-align: center;
  color: #5f728d;
}

.spinner {
  width: 38px;
  height: 38px;
  border-radius: 999px;
  border: 3px solid #dce6f2;
  border-top-color: #18b6ac;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1200px) {
  .hero-card,
  .overview-grid,
  .split-grid,
  .detail-grid,
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .hero-summary,
  .career-summary__grid,
  .keyvalue-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .header-bar,
  .content-shell,
  .hero-card,
  .tabs-header {
    padding-left: 18px;
    padding-right: 18px;
  }

  .header-bar,
  .identity-row,
  .list-card__top,
  .timeline-top {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-identity {
    flex-direction: column;
  }

  .hero-summary,
  .keyvalue-grid,
  .career-summary__grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .avatar-circle {
    width: 78px;
    height: 78px;
    flex-basis: 78px;
    font-size: 28px;
    border-radius: 22px;
  }

  .identity-row h1 {
    font-size: 28px;
  }

  .summary-value,
  .stats-card__value {
    font-size: 22px;
  }
}
</style>
