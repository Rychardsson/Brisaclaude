<template>
  <div class="dashboard-page">
    <div class="dashboard-shell">
      <section class="page-header-card">
        <div class="page-header-top">
          <div class="header-content">
            <h1>Dashboard</h1>
            <p class="subtitle">
              Visão geral dos programas, turmas, desempenho e impacto.
            </p>
          </div>

          <div class="top-actions">
            <button type="button" class="ghost-btn" :disabled="loading" @click="loadData">
              <RefreshCw :size="16" />
              Atualizar dados
            </button>
            <button type="button" class="ghost-btn" @click="shareView">
              <Share2 :size="16" />
              Compartilhar visão
            </button>
            <button type="button" class="ghost-btn" @click="exportReport">
              <Download :size="16" />
              Exportar relatório
            </button>
          </div>
        </div>

        <div v-if="actionMessage" class="action-banner">
          {{ actionMessage }}
        </div>
      </section>

      <section class="table-card">
        <div class="filters-row">
          <div class="filters-label">
            <Filter :size="16" />
            <span>Filtros:</span>
          </div>

          <div class="filters-actions">
            <select v-model="selectedProgramId" class="filter-select">
              <option value="">Programa: todos</option>
              <option v-for="program in programOptions" :key="program.id" :value="program.id">
                {{ program.label }}
              </option>
            </select>

            <select v-model="selectedClassId" class="filter-select">
              <option value="">Turma: todas</option>
              <option v-for="classItem in classOptions" :key="classItem.id" :value="classItem.id">
                {{ classItem.label }}
              </option>
            </select>

            <select v-model="selectedPeriod" class="filter-select">
              <option value="">Período: todos</option>
              <option v-for="period in periodOptions" :key="period" :value="period">
                {{ period }}
              </option>
            </select>

            <select v-model="selectedStage" class="filter-select">
              <option value="">Etapa: todas</option>
              <option v-for="stage in stageOptions" :key="stage" :value="stage">
                {{ stage }}
              </option>
            </select>

            <button
              type="button"
              class="toggle-filter-btn"
              :class="{ active: onlyActive }"
              @click="onlyActive = !onlyActive"
            >
              Somente ativos
            </button>
          </div>
        </div>

        <div class="toolbar-row">
          <div class="toolbar-note">
            {{ filterSummary }}
          </div>

          <button type="button" class="ghost-btn" @click="resetFilters">
            Limpar filtros
          </button>
        </div>

        <div v-if="!loading && usingSampleData" class="sample-banner">
          <div>
            <strong>Dados de exemplo</strong>
            <p>{{ sampleBannerMessage }}</p>
          </div>
          <button
            v-if="hasRealDashboardData"
            type="button"
            class="ghost-inline-btn"
            @click="toggleSampleData"
          >
            Ver dados reais
          </button>
        </div>

        <div v-if="loading" class="state-row">
          <div class="spinner"></div>
          <span>Carregando dados do dashboard...</span>
        </div>

        <div v-else-if="displayErrorMessage" class="state-row state-row-error">
          <span>{{ displayErrorMessage }}</span>
        </div>

        <div v-else-if="scopedOverviewItems.length === 0" class="state-row state-row-empty">
          <div>
            <strong>Nenhum dado encontrado para os filtros atuais</strong>
            <p>Altere os filtros para visualizar indicadores do dashboard.</p>
          </div>
        </div>

        <template v-else>
          <div class="stats-grid">
            <article v-for="card in summaryCards" :key="card.label" class="stat-card" :class="card.tone">
              <div class="stat-head">
                <div class="stat-label">{{ card.label }}</div>
                <component :is="card.icon" :size="16" class="stat-icon" :class="card.iconClass" />
              </div>
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-note" :class="card.noteClass">{{ card.note }}</div>
            </article>
          </div>

          <div class="overview-grid">
            <article class="insight-card funnel-card">
              <div class="insight-header">
                <div>
                  <h2>Funil do Edital</h2>
                  <p>Leitura consolidada da jornada atual no recorte selecionado.</p>
                </div>
                <button type="button" class="ghost-inline-btn" @click="actionMessage = 'O detalhamento do funil pode ser expandido depois com drill-down por turma.'">
                  Ver detalhamento
                </button>
              </div>

              <div class="funnel-grid">
                <div v-for="(step, index) in funnelSteps" :key="step.label" class="funnel-step-wrap">
                  <article class="funnel-step">
                    <div class="funnel-step-label">{{ step.label }}</div>
                    <div class="funnel-step-value">{{ formatNumber(step.value) }}</div>
                    <div class="funnel-track">
                      <span class="funnel-fill" :class="step.tone" :style="{ width: `${step.percentage}%` }"></span>
                    </div>
                    <div class="funnel-step-note">{{ step.note }}</div>
                  </article>
                  <div v-if="index < funnelSteps.length - 1" class="funnel-step-arrow">
                    <ChevronRight :size="14" />
                  </div>
                </div>
              </div>

              <div class="funnel-summary">
                <div v-for="item in funnelSummaryItems" :key="item.label" class="funnel-summary-item">
                  <span>{{ item.label }}</span>
                  <strong :class="item.valueClass">{{ item.value }}</strong>
                </div>
              </div>
            </article>

            <article class="insight-card alerts-card">
              <div class="insight-header">
                <div>
                  <h2>Alertas críticos</h2>
                  <p>Itens que merecem acompanhamento mais próximo.</p>
                </div>
              </div>

              <div class="alerts-list">
                <article v-for="alert in criticalAlerts" :key="alert.id" class="alert-item" :class="alert.tone">
                  <AlertTriangle :size="16" class="alert-icon" :class="alert.iconClass" />
                  <div class="alert-copy">
                    <strong>{{ alert.title }}</strong>
                    <span v-if="alert.description">{{ alert.description }}</span>
                  </div>
                </article>
              </div>
            </article>
          </div>

          <div class="phase-grid">
            <article class="phase-card">
              <div class="phase-header">
                <div>
                  <h2>Nivelamento</h2>
                  <p>Leitura da etapa de entrada e preparacao.</p>
                </div>
                <span class="phase-pill">{{ formatNumber(levelingSummary.students) }} alunos ativos</span>
              </div>

              <div class="phase-stats-grid">
                <article v-for="card in levelingMetricCards" :key="card.label" class="mini-stat-card">
                  <span>{{ card.label }}</span>
                  <strong :class="card.valueClass">{{ card.value }}</strong>
                </article>
              </div>

              <div class="progress-distribution">
                <div v-for="band in levelingBands" :key="band.label" class="distribution-row">
                  <div class="distribution-meta">
                    <strong>{{ band.label }}</strong>
                    <span :class="band.textClass">{{ formatNumber(band.count) }} alunos</span>
                  </div>
                  <div class="distribution-track">
                    <span class="distribution-fill" :class="band.tone" :style="{ width: `${band.percentage}%` }"></span>
                  </div>
                </div>
              </div>

              <div class="phase-attention">
                <strong>Atenção necessária</strong>
                <ul>
                  <li v-for="item in levelingAttention" :key="item">{{ item }}</li>
                </ul>
              </div>
            </article>

            <article class="phase-card">
              <div class="phase-header">
                <div>
                  <h2>Imersão</h2>
                  <p>Leitura da etapa prática, grupos e avaliações.</p>
                </div>
                <span class="phase-pill phase-pill-purple">{{ formatNumber(immersionSummary.students) }} alunos ativos</span>
              </div>

              <div class="phase-stats-grid">
                <article v-for="card in immersionMetricCards" :key="card.label" class="mini-stat-card">
                  <span>{{ card.label }}</span>
                  <strong :class="card.valueClass">{{ card.value }}</strong>
                </article>
              </div>

              <div class="score-list">
                <div v-for="item in immersionScoreRows" :key="item.label" class="score-row">
                  <div class="score-meta">
                    <strong>{{ item.label }}</strong>
                    <span>{{ item.valueLabel }}</span>
                  </div>
                  <div class="distribution-track">
                    <span class="distribution-fill tone-teal" :style="{ width: `${item.percentage}%` }"></span>
                  </div>
                </div>
              </div>

              <div class="phase-attention">
                <strong>Atenção necessária</strong>
                <ul>
                  <li v-for="item in immersionAttention" :key="item">{{ item }}</li>
                </ul>
              </div>
            </article>
          </div>

          <section class="profile-section-card">
            <div class="section-header">
              <div>
                <h2>Perfil dos participantes</h2>
                <p>Distribuições do público ativo e concluinte no recorte atual.</p>
              </div>
            </div>

            <div class="profile-grid">
              <article class="profile-card">
                <div class="profile-card-header">
                  <h3>Distribuição por gênero</h3>
                </div>
                <div class="stack-bars">
                  <div v-for="item in genderDistribution" :key="item.label" class="stack-row">
                    <div class="stack-meta">
                      <strong>{{ item.label }}</strong>
                      <span>{{ formatNumber(item.count) }} | {{ formatPercent(item.ratio) }}</span>
                    </div>
                    <div class="distribution-track">
                      <span class="distribution-fill" :class="item.tone" :style="{ width: `${item.ratio * 100}%` }"></span>
                    </div>
                  </div>
                </div>
              </article>

              <article class="profile-card">
                <div class="profile-card-header">
                  <h3>Distribuição por etnia</h3>
                </div>
                <div class="stack-bars">
                  <div v-for="item in ethnicityDistribution" :key="item.label" class="stack-row">
                    <div class="stack-meta">
                      <strong>{{ item.label }}</strong>
                      <span>{{ formatNumber(item.count) }} | {{ formatPercent(item.ratio) }}</span>
                    </div>
                    <div class="distribution-track">
                      <span class="distribution-fill" :class="item.tone" :style="{ width: `${item.ratio * 100}%` }"></span>
                    </div>
                  </div>
                </div>
              </article>

              <article class="profile-card">
                <div class="profile-card-header">
                  <h3>Faixa etária</h3>
                </div>
                <div class="histogram">
                  <div v-for="item in ageDistribution" :key="item.label" class="histogram-item">
                    <div class="histogram-bar-wrap">
                      <span class="histogram-bar" :style="{ height: `${item.height}%` }"></span>
                    </div>
                    <strong>{{ item.count }}</strong>
                    <span>{{ item.label }}</span>
                  </div>
                </div>
              </article>

              <article class="profile-card">
                <div class="profile-card-header">
                  <h3>Nível de formação</h3>
                </div>
                <div class="stack-bars">
                  <div v-for="item in educationDistribution" :key="item.label" class="stack-row">
                    <div class="stack-meta">
                      <strong>{{ item.label }}</strong>
                      <span>{{ formatNumber(item.count) }} | {{ formatPercent(item.ratio) }}</span>
                    </div>
                    <div class="distribution-track">
                      <span class="distribution-fill tone-blue" :style="{ width: `${item.ratio * 100}%` }"></span>
                    </div>
                  </div>
                </div>
              </article>

              <article class="profile-card">
                <div class="profile-card-header">
                  <h3>Categoria de cota</h3>
                </div>
                <div class="stack-bars">
                  <div v-for="item in quotaCapacityRows" :key="item.label" class="stack-row">
                    <div class="stack-meta">
                      <strong>{{ item.label }}</strong>
                      <span>{{ formatNumber(item.filled) }}/{{ formatNumber(item.capacity) }}</span>
                    </div>
                    <div class="distribution-track">
                      <span class="distribution-fill" :class="item.tone" :style="{ width: `${item.percentage}%` }"></span>
                    </div>
                  </div>
                </div>
              </article>

              <article class="profile-card">
                <div class="profile-card-header">
                  <h3>Top instituições de origem</h3>
                </div>
                <div class="institution-bars">
                  <div v-for="item in institutionDistribution" :key="item.label" class="stack-row">
                    <div class="institution-row">
                      <strong class="institution-label">{{ item.label }}</strong>
                      <div class="institution-track">
                        <span class="institution-fill" :style="{ width: `${item.ratio * 100}%` }">
                          {{ formatNumber(item.count) }}
                        </span>
                      </div>
                      <span class="institution-percent">{{ formatPercent(item.ratio) }}</span>
                    </div>
                  </div>
                </div>
              </article>
            </div>
          </section>

          <section class="section-card">
            <div class="section-header">
              <div>
                <h2>Desempenho por competência</h2>
              </div>
            </div>

            <div class="competency-grid">
              <article v-for="item in competencyScores" :key="item.label" class="competency-card">
                <div class="competency-ring" :style="{ '--score': `${item.percentage}%` }">
                  <div class="competency-ring-inner">{{ item.score }}</div>
                </div>
                <strong>{{ item.label }}</strong>
              </article>
            </div>
          </section>

          <section class="section-card">
            <div class="section-header">
              <div>
                <h2>Impacto acadêmico e profissional</h2>
              </div>
            </div>

            <div class="impact-grid">
              <article v-for="item in impactCards" :key="item.label" class="impact-card" :class="item.tone">
                <div class="impact-label">{{ item.label }}</div>
                <div class="impact-value">{{ item.value }}</div>
                <div class="impact-note">{{ item.note }}</div>
              </article>
            </div>
          </section>

          <section class="section-card">
            <div class="section-header">
              <div>
                <h2>Próximos eventos e prazos</h2>
              </div>
            </div>

            <div class="events-list">
              <article v-for="event in upcomingEvents" :key="event.id" class="event-item">
                <div class="event-date">
                  <strong>{{ event.day }}</strong>
                  <span>{{ event.month }}</span>
                </div>
                <div class="event-copy">
                  <strong>{{ event.title }}</strong>
                  <span>{{ event.subtitle }}</span>
                </div>
                <div class="event-badge" :class="event.tone">
                  {{ event.badge }}
                </div>
                <CalendarDays :size="16" class="event-calendar-icon" />
              </article>
            </div>
          </section>
        </template>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import {
  AlertTriangle,
  Award,
  Briefcase,
  Building2,
  CalendarDays,
  ChevronRight,
  Download,
  Filter,
  GraduationCap,
  RefreshCw,
  Share2,
  Target,
  TrendingUp,
  Users,
} from 'lucide-vue-next';
import { careerService } from '@/services/careerService';
import { classService } from '@/services/classService';
import { enrollmentService } from '@/services/enrollmentService';
import { programService } from '@/services/programService';

const route = useRoute();
const SAMPLE_DASHBOARD_OVERVIEW_ITEMS = [
  {
    programId: 11,
    classId: 101,
    nome: 'Programa Jovem Tech',
    turma: 'JT-2026-A',
    parceiro: 'Porto Digital',
    localidade: 'Recife',
    periodo: '2026.1',
    status: 'Em andamento',
    etapaAtual: 'Nivelamento',
    inscritos: 180,
    ativos: 124,
    nivelamento: 68,
    imersao: 24,
    projetos: 2,
    orientadores: 4,
    proximoMarco: '29/05/2026',
    progresso: 41,
  },
  {
    programId: 11,
    classId: 102,
    nome: 'Programa Jovem Tech',
    turma: 'JT-2026-B',
    parceiro: 'Porto Digital',
    localidade: 'Recife',
    periodo: '2026.1',
    status: 'Em andamento',
    etapaAtual: 'Imersão',
    inscritos: 160,
    ativos: 118,
    nivelamento: 18,
    imersao: 72,
    projetos: 6,
    orientadores: 5,
    proximoMarco: '30/06/2026',
    progresso: 67,
  },
  {
    programId: 22,
    classId: 201,
    nome: 'Programa Dados para Futuro',
    turma: 'DF-2026-A',
    parceiro: 'Softex',
    localidade: 'Olinda',
    periodo: '2026.1',
    status: 'Em andamento',
    etapaAtual: 'Imersão',
    inscritos: 132,
    ativos: 94,
    nivelamento: 12,
    imersao: 61,
    projetos: 0,
    orientadores: 0,
    proximoMarco: '15/05/2026',
    progresso: 58,
  },
  {
    programId: 44,
    classId: 401,
    nome: 'Programa Start Cloud',
    turma: 'SC-2026-A',
    parceiro: 'Accenture',
    localidade: 'Paulista',
    periodo: '2026.2',
    status: 'Em andamento',
    etapaAtual: 'Nivelamento',
    inscritos: 144,
    ativos: 76,
    nivelamento: 49,
    imersao: 8,
    projetos: 0,
    orientadores: 1,
    proximoMarco: '18/06/2026',
    progresso: 29,
  },
  {
    programId: 33,
    classId: 301,
    nome: 'Programa Criativa Digital',
    turma: 'CD-2025-B',
    parceiro: 'SECTI PE',
    localidade: 'Jaboatao',
    periodo: '2025.2',
    status: 'Encerrado',
    etapaAtual: 'Concluído',
    inscritos: 98,
    ativos: 0,
    nivelamento: 0,
    imersao: 0,
    projetos: 4,
    orientadores: 3,
    proximoMarco: '20/12/2025',
    progresso: 100,
  },
];

const SAMPLE_DASHBOARD_CLASSES = [
  {
    id: 101,
    code: 'JT-2026-A',
    startDate: '2026-06-03',
    publicationDate: '2026-04-01',
    applicationEndDate: '2026-05-24',
    levelingSelectionAnnouncementDate: '2026-05-29',
    levelingStartDate: '2026-06-03',
    levelingFinalExamDate: '2026-07-05',
    immersionSelectionAnnouncementDate: '2026-07-12',
    immersionStartDate: '2026-07-20',
    partialEvaluationDate: '2026-09-05',
    finalEvaluationDate: '2026-10-28',
    certificateIssueDate: '2026-11-15',
    program: { id: 11, name: 'Programa Jovem Tech' },
  },
  {
    id: 102,
    code: 'JT-2026-B',
    startDate: '2026-03-10',
    publicationDate: '2026-01-08',
    applicationEndDate: '2026-02-20',
    levelingSelectionAnnouncementDate: '2026-02-27',
    levelingStartDate: '2026-03-10',
    levelingFinalExamDate: '2026-04-15',
    immersionSelectionAnnouncementDate: '2026-05-20',
    immersionStartDate: '2026-06-12',
    partialEvaluationDate: '2026-06-30',
    finalEvaluationDate: '2026-08-25',
    certificateIssueDate: '2026-09-12',
    program: { id: 11, name: 'Programa Jovem Tech' },
  },
  {
    id: 201,
    code: 'DF-2026-A',
    startDate: '2026-02-17',
    publicationDate: '2025-12-05',
    applicationEndDate: '2026-01-18',
    levelingSelectionAnnouncementDate: '2026-01-24',
    levelingStartDate: '2026-02-01',
    levelingFinalExamDate: '2026-03-02',
    immersionSelectionAnnouncementDate: '2026-03-20',
    immersionStartDate: '2026-04-02',
    partialEvaluationDate: '2026-05-03',
    finalEvaluationDate: '2026-05-10',
    certificateIssueDate: '2026-05-12',
    program: { id: 22, name: 'Programa Dados para Futuro' },
  },
  {
    id: 401,
    code: 'SC-2026-A',
    startDate: '2026-07-01',
    publicationDate: '2026-05-05',
    applicationEndDate: '2026-06-12',
    levelingSelectionAnnouncementDate: '2026-06-18',
    levelingStartDate: '2026-07-01',
    levelingFinalExamDate: '2026-08-08',
    immersionSelectionAnnouncementDate: '2026-08-21',
    immersionStartDate: '2026-09-02',
    partialEvaluationDate: '2026-10-10',
    finalEvaluationDate: '2026-11-27',
    certificateIssueDate: '2026-12-14',
    program: { id: 44, name: 'Programa Start Cloud' },
  },
  {
    id: 301,
    code: 'CD-2025-B',
    startDate: '2025-08-04',
    publicationDate: '2025-05-05',
    applicationEndDate: '2025-06-15',
    levelingSelectionAnnouncementDate: '2025-06-21',
    levelingStartDate: '2025-07-01',
    levelingFinalExamDate: '2025-08-05',
    immersionSelectionAnnouncementDate: '2025-08-19',
    immersionStartDate: '2025-09-02',
    partialEvaluationDate: '2025-10-11',
    finalEvaluationDate: '2025-11-26',
    certificateIssueDate: '2025-12-20',
    program: { id: 33, name: 'Programa Criativa Digital' },
  },
];

const SAMPLE_DASHBOARD_PEOPLE = [
  { id: 1, name: 'Ana Beatriz Silva', gender: 'Feminino', birthDate: '2004-09-12', educationLevel: 'Graduação em andamento', institutionName: 'UFRPE', quotaCategory: 'Escola pública', educationStatus: 'Cursando' },
  { id: 2, name: 'Carlos Henrique Souza', gender: 'Masculino', birthDate: '1999-03-08', educationLevel: 'Graduação completa', institutionName: 'UFPE', quotaCategory: 'Ampla concorrência', educationStatus: 'Concluído' },
  { id: 3, name: 'Juliana Santos', gender: 'Feminino', birthDate: '2001-12-17', educationLevel: 'Tecnologo em andamento', institutionName: 'IFPE', quotaCategory: 'PPI', educationStatus: 'Cursando' },
  { id: 4, name: 'Mateus Oliveira', gender: 'Masculino', birthDate: '2003-01-24', educationLevel: 'Graduação em andamento', institutionName: 'UNICAP', quotaCategory: 'Escola pública', educationStatus: 'Ativo' },
  { id: 5, name: 'Rafaela Costa', gender: 'Feminino', birthDate: '1997-07-02', educationLevel: 'Pós-graduação', institutionName: 'UPE', quotaCategory: 'PCD', educationStatus: 'Concluído' },
  { id: 6, name: 'Joao Pedro Lima', gender: 'Masculino', birthDate: '2005-02-14', educationLevel: 'Ensino medio', institutionName: 'ETE Recife', quotaCategory: 'Escola publica', educationStatus: 'Cursando' },
  { id: 7, name: 'Larissa Moura', gender: 'Feminino', birthDate: '2000-11-29', educationLevel: 'Graduação em andamento', institutionName: 'UFPE', quotaCategory: 'PPI', educationStatus: 'Ativo' },
  { id: 8, name: 'Bruno Ferreira', gender: 'Masculino', birthDate: '1998-06-06', educationLevel: 'Graduação completa', institutionName: 'UFRPE', quotaCategory: 'Ampla concorrência', educationStatus: 'Concluído' },
  { id: 9, name: 'Camila Rocha', gender: 'Feminino', birthDate: '2002-05-18', educationLevel: 'Tecnólogo completo', institutionName: 'SENAC', quotaCategory: 'Escola pública', educationStatus: 'Concluído' },
  { id: 10, name: 'Diego Alves', gender: 'Masculino', birthDate: '1996-10-10', educationLevel: 'Graduação completa', institutionName: 'CESAR School', quotaCategory: 'Ampla concorrência', educationStatus: 'Concluído' },
  { id: 11, name: 'Patricia Gomes', gender: 'Feminino', birthDate: '2004-04-03', educationLevel: 'Graduação em andamento', institutionName: 'UNINASSAU', quotaCategory: 'PPI', educationStatus: 'Cursando' },
  { id: 12, name: 'Alex Nascimento', gender: 'Não-binário', birthDate: '2001-08-21', educationLevel: 'Tecnólogo em andamento', institutionName: 'IFPE', quotaCategory: 'Ampla concorrência', educationStatus: 'Ativo' },
];

const SAMPLE_DASHBOARD_ENROLLMENTS = [
  createSampleEnrollment(1001, 101, SAMPLE_DASHBOARD_PEOPLE[0], 'Ativo'),
  createSampleEnrollment(1002, 101, SAMPLE_DASHBOARD_PEOPLE[1], 'Ativo'),
  createSampleEnrollment(1003, 101, SAMPLE_DASHBOARD_PEOPLE[2], 'Ativo'),
  createSampleEnrollment(1004, 102, SAMPLE_DASHBOARD_PEOPLE[3], 'Ativo'),
  createSampleEnrollment(1005, 102, SAMPLE_DASHBOARD_PEOPLE[4], 'Concluído', '2026-04-14'),
  createSampleEnrollment(1006, 102, SAMPLE_DASHBOARD_PEOPLE[5], 'Ativo'),
  createSampleEnrollment(1007, 201, SAMPLE_DASHBOARD_PEOPLE[6], 'Ativo'),
  createSampleEnrollment(1008, 201, SAMPLE_DASHBOARD_PEOPLE[7], 'Concluído', '2026-05-06'),
  createSampleEnrollment(1009, 201, SAMPLE_DASHBOARD_PEOPLE[8], 'Ativo'),
  createSampleEnrollment(1010, 301, SAMPLE_DASHBOARD_PEOPLE[9], 'Concluído', '2025-11-26'),
  createSampleEnrollment(1011, 401, SAMPLE_DASHBOARD_PEOPLE[10], 'Ativo'),
  createSampleEnrollment(1012, 401, SAMPLE_DASHBOARD_PEOPLE[11], 'Ativo'),
];

const SAMPLE_DASHBOARD_FOLLOWUPS = [
  { key: 'career-1005', peopleId: 5, classId: '102', surveyDate: '2026-05-03', status: 'empregado', company: 'Accenture', position: 'Analista de dados' },
  { key: 'career-1008', peopleId: 8, classId: '201', surveyDate: '2026-05-06', status: 'desempregado', company: '', position: '' },
  { key: 'career-1010', peopleId: 10, classId: '301', surveyDate: '2026-03-12', status: 'empregado', company: 'NeuroData', position: 'Desenvolvedor full stack' },
  { key: 'career-1009', peopleId: 9, classId: '201', surveyDate: '2026-05-08', status: 'sem resposta', company: '', position: '' },
  { key: 'career-1002', peopleId: 2, classId: '101', surveyDate: '2026-05-10', status: 'empregado', company: 'Inovar Tech', position: 'Estagiario de suporte' },
];

const loading = ref(false);
const errorMessage = ref('');
const actionMessage = ref('');

const overview = ref(null);
const classes = ref([]);
const enrollments = ref([]);
const careerFollowUps = ref([]);
const forceSampleData = ref(false);

const selectedProgramId = ref('');
const selectedClassId = ref('');
const selectedPeriod = ref('');
const selectedStage = ref('');
const onlyActive = ref(false);

onMounted(() => {
  applyQueryFilters();
  void loadData();
});

watch(selectedProgramId, () => {
  if (selectedClassId.value && !classOptions.value.some((item) => item.id === selectedClassId.value)) {
    selectedClassId.value = '';
  }
});

watch([selectedProgramId, selectedClassId, selectedPeriod], () => {
  if (selectedStage.value && !stageOptions.value.includes(selectedStage.value)) {
    selectedStage.value = '';
  }
});

function applyQueryFilters() {
  selectedProgramId.value = String(route.query.programaId || '');
  selectedClassId.value = String(route.query.turmaId || '');
  selectedPeriod.value = String(route.query.periodo || '');
  selectedStage.value = String(route.query.etapa || '');
  onlyActive.value = String(route.query.ativos || '').toLowerCase() === 'true';
}

function toggleSampleData() {
  if (!hasRealDashboardData.value) {
    forceSampleData.value = true;
    actionMessage.value = 'Sem base real suficiente no momento. O dashboard segue em modo demonstração.';
    return;
  }

  forceSampleData.value = !forceSampleData.value;
  actionMessage.value = forceSampleData.value
    ? 'O dashboard agora está exibindo dados de exemplo para apresentação.'
    : 'O dashboard voltou a usar os dados reais disponíveis.';
}

async function loadData() {
  loading.value = true;
  errorMessage.value = '';

  try {
    const [overviewData, classesData, enrollmentsData, followUpsData] = await Promise.all([
      programService.getOverview().catch(() => ({ resumo: {}, items: [] })),
      classService.getAll().catch(() => []),
      enrollmentService.getAll().catch(() => []),
      careerService.getFollowUps().catch(() => []),
    ]);

    overview.value = overviewData && typeof overviewData === 'object' ? overviewData : { resumo: {}, items: [] };
    classes.value = Array.isArray(classesData) ? classesData : [];
    enrollments.value = Array.isArray(enrollmentsData) ? enrollmentsData : [];
    careerFollowUps.value = Array.isArray(followUpsData) ? followUpsData : [];
    actionMessage.value = '';
  } catch (error) {
    errorMessage.value = error.response?.data?.message || error.message || 'Não foi possível carregar o dashboard.';
  } finally {
    loading.value = false;
  }
}

const rawOverviewItems = computed(() => Array.isArray(overview.value?.items) ? overview.value.items : []);
const hasRealDashboardData = computed(() => rawOverviewItems.value.length > 0);
const usingSampleData = computed(() => forceSampleData.value || !hasRealDashboardData.value);
const displayErrorMessage = computed(() => (usingSampleData.value ? '' : errorMessage.value));
const sampleBannerMessage = computed(() => (
  hasRealDashboardData.value
    ? 'O painel está em modo demonstração para facilitar a apresentação visual. Você pode voltar para os dados reais a qualquer momento.'
    : 'Ainda não há base consolidada suficiente para esta leitura, então o dashboard está exibindo um cenário ilustrativo.'
));
const overviewItems = computed(() => (usingSampleData.value ? SAMPLE_DASHBOARD_OVERVIEW_ITEMS : rawOverviewItems.value));
const availableClasses = computed(() => (usingSampleData.value ? SAMPLE_DASHBOARD_CLASSES : classes.value));
const availableEnrollments = computed(() => (usingSampleData.value ? SAMPLE_DASHBOARD_ENROLLMENTS : enrollments.value));

watch(usingSampleData, () => {
  if (selectedProgramId.value && !programOptions.value.some((item) => item.id === selectedProgramId.value)) {
    selectedProgramId.value = '';
  }

  if (selectedClassId.value && !classOptions.value.some((item) => item.id === selectedClassId.value)) {
    selectedClassId.value = '';
  }

  if (selectedPeriod.value && !periodOptions.value.includes(selectedPeriod.value)) {
    selectedPeriod.value = '';
  }

  if (selectedStage.value && !stageOptions.value.includes(selectedStage.value)) {
    selectedStage.value = '';
  }
});

const classMap = computed(() => {
  const map = new Map();
  availableClasses.value.forEach((item) => {
    if (item?.id == null) return;
    map.set(String(item.id), item);
  });
  return map;
});

const programOptions = computed(() => {
  const known = new Map();
  overviewItems.value.forEach((item) => {
    if (!item?.programId) return;
    const id = String(item.programId);
    if (!known.has(id)) known.set(id, { id, label: item.nome || `Programa ${id}` });
  });

  availableClasses.value.forEach((item) => {
    const program = item?.program;
    if (!program?.id) return;
    const id = String(program.id);
    if (!known.has(id)) known.set(id, { id, label: program.name || `Programa ${id}` });
  });

  return Array.from(known.values()).sort((a, b) => a.label.localeCompare(b.label, 'pt-BR'));
});

const classOptions = computed(() => {
  const known = new Map();

  overviewItems.value.forEach((item) => {
    if (!item?.classId) return;
    if (selectedProgramId.value && String(item.programId) !== selectedProgramId.value) return;
    const id = String(item.classId);
    if (!known.has(id)) {
      known.set(id, {
        id,
        label: `${item.turma || `Turma ${id}`} | ${item.nome || 'Programa'}`
      });
    }
  });

  availableClasses.value.forEach((item) => {
    if (!item?.id) return;
    const programId = item?.program?.id ? String(item.program.id) : '';
    if (selectedProgramId.value && programId !== selectedProgramId.value) return;
    const id = String(item.id);
    if (!known.has(id)) {
      known.set(id, {
        id,
        label: `${item.code || `Turma ${id}`} | ${item.program?.name || 'Programa'}`
      });
    }
  });

  return Array.from(known.values()).sort((a, b) => a.label.localeCompare(b.label, 'pt-BR'));
});

const periodOptions = computed(() => {
  const years = new Set();

  overviewItems.value.forEach((item) => {
    parseYears(item.periodo).forEach((year) => years.add(year));
  });

  availableClasses.value.forEach((item) => {
    if (item?.startDate) years.add(String(parseDateValue(item.startDate).getFullYear()));
  });

  return Array.from(years).filter(Boolean).sort((a, b) => Number(b) - Number(a));
});

const baseScopedItems = computed(() => {
  let items = overviewItems.value;

  if (selectedProgramId.value) {
    items = items.filter((item) => String(item.programId) === selectedProgramId.value);
  }

  if (selectedClassId.value) {
    items = items.filter((item) => String(item.classId) === selectedClassId.value);
  }

  if (selectedPeriod.value) {
    items = items.filter((item) => {
      const fromOverview = parseYears(item.periodo);
      if (fromOverview.includes(String(selectedPeriod.value))) return true;
      const classItem = classMap.value.get(String(item.classId));
      if (!classItem?.startDate) return false;
      return String(parseDateValue(classItem.startDate).getFullYear()) === String(selectedPeriod.value);
    });
  }

  if (onlyActive.value) {
    items = items.filter((item) => normalizedStatus(item.status) === 'andamento');
  }

  return items;
});

const stageOptions = computed(() => {
  const values = baseScopedItems.value.map((item) => item.etapaAtual).filter(Boolean);
  return Array.from(new Set(values)).sort((a, b) => a.localeCompare(b, 'pt-BR'));
});

const scopedOverviewItems = computed(() => {
  let items = baseScopedItems.value;
  if (selectedStage.value) {
    items = items.filter((item) => item.etapaAtual === selectedStage.value);
  }
  return items;
});

const scopedClassIds = computed(() => new Set(scopedOverviewItems.value.map((item) => String(item.classId)).filter(Boolean)));

const studentEnrollments = computed(() =>
  availableEnrollments.value.filter((item) => scopedClassIds.value.has(String(item?.classModel?.id || item?.classId)) && isStudentEnrollment(item))
);

const activeStudentEnrollments = computed(() =>
  studentEnrollments.value.filter((item) => isActiveEnrollmentStatus(item?.status))
);

const completedStudentEnrollments = computed(() =>
  studentEnrollments.value.filter((item) => Boolean(item?.completionDate) || isCompletedEnrollmentStatus(item?.status))
);

const participantPeople = computed(() => uniquePeopleFromEnrollments(studentEnrollments.value));
const activeParticipantPeople = computed(() => uniquePeopleFromEnrollments(activeStudentEnrollments.value));

const storedCareerFollowUps = computed(() => (usingSampleData.value ? SAMPLE_DASHBOARD_FOLLOWUPS : careerFollowUps.value));

const scopedFollowUps = computed(() => {
  if (!storedCareerFollowUps.value.length) return [];

  return storedCareerFollowUps.value.filter((item) => {
    const classId = String(item?.classId || '');
    if (scopedClassIds.value.size > 0 && classId) return scopedClassIds.value.has(classId);
    return false;
  });
});

const latestScopedFollowUps = computed(() => {
  const grouped = new Map();

  scopedFollowUps.value.forEach((item) => {
    const key = String(item?.key || [item?.peopleId || 'person', item?.classId || 'class', item?.enrollmentId || item?.id || 'followup'].join(':'));
    if (!key) return;
    const current = grouped.get(key);
    if (!current || String(item.surveyDate || '') > String(current.surveyDate || '')) {
      grouped.set(key, item);
    }
  });

  return Array.from(grouped.values());
});

const funnelMetrics = computed(() => {
  const inscritos = sumBy(scopedOverviewItems.value, (item) => item.inscritos);
  const ativos = sumBy(scopedOverviewItems.value, (item) => item.ativos);
  const nivelamento = sumBy(scopedOverviewItems.value, (item) => item.nivelamento);
  const imersao = sumBy(scopedOverviewItems.value, (item) => item.imersao);
  const projetos = sumBy(scopedOverviewItems.value, (item) => item.projetos);
  const concluintes = completedStudentEnrollments.value.length;

  return {
    inscritos,
    ativos,
    nivelamento,
    imersao,
    projetos,
    concluintes,
    activationRate: inscritos ? ativos / inscritos : 0,
    immersionRate: inscritos ? imersao / inscritos : 0,
    completionRate: inscritos ? concluintes / inscritos : 0,
  };
});

const funnelSteps = computed(() => {
  if (usingSampleData.value) {
    return [
      { label: 'Inscritos', value: 612, tone: 'tone-blue', note: '100%', percentage: 100 },
      { label: 'Inscrições Válidas', value: 540, tone: 'tone-teal', note: '88%', percentage: 88 },
      { label: 'Em Nivelamento', value: 229, tone: 'tone-purple', note: '42%', percentage: 42 },
      { label: 'Aprovados p/ Imersão', value: 50, tone: 'tone-orange', note: '22%', percentage: 22 },
      { label: 'Em Imersão', value: 44, tone: 'tone-green', note: '88%', percentage: 88 },
      { label: 'Aprovados Finais', value: 36, tone: 'tone-indigo', note: '82%', percentage: 82 },
    ];
  }

  const max = Math.max(funnelMetrics.value.inscritos, 1);
  const validSubscriptions = Math.round(funnelMetrics.value.inscritos * 0.88);
  const approvedForImmersion = Math.max(funnelMetrics.value.imersao, completedStudentEnrollments.value.length);
  const steps = [
    { label: 'Inscritos', value: funnelMetrics.value.inscritos, tone: 'tone-blue' },
    { label: 'Inscrições Válidas', value: validSubscriptions, tone: 'tone-teal' },
    { label: 'Em Nivelamento', value: funnelMetrics.value.nivelamento, tone: 'tone-purple' },
    { label: 'Aprovados p/ Imersão', value: approvedForImmersion, tone: 'tone-orange' },
    { label: 'Em Imersão', value: funnelMetrics.value.imersao, tone: 'tone-green' },
    { label: 'Aprovados Finais', value: funnelMetrics.value.concluintes, tone: 'tone-indigo' },
  ];

  return steps.map((item) => ({
    ...item,
    note: formatPercent(item.value / max),
    percentage: Math.max(8, Math.min(100, (item.value / max) * 100)),
  }));
});

const summaryCards = computed(() => {
  const activeClasses = scopedOverviewItems.value.filter((item) => normalizedStatus(item.status) === 'andamento');
  const activePrograms = new Set(activeClasses.map((item) => String(item.programId)));
  const progressAverage = averageBy(scopedOverviewItems.value, (item) => item.progresso);
  const approvalRate = usingSampleData.value
    ? '78%'
    : formatPercent(
        completedStudentEnrollments.value.length /
          Math.max(immersionSummary.value.students || participantPeople.value.length || 1, 1)
      );

  if (usingSampleData.value) {
    return [
      { label: 'Programas ativos', value: '3', note: '+1 desde ultimo mes', tone: 'tone-surface', icon: Award, iconClass: 'icon-teal', noteClass: 'note-teal' },
      { label: 'Turmas ativas', value: '5', note: 'Em andamento', tone: 'tone-surface', icon: Building2, iconClass: 'icon-teal', noteClass: 'note-muted' },
      { label: 'Pessoas ativas', value: '428', note: '+12% desde inicio', tone: 'tone-surface', icon: Users, iconClass: 'icon-teal', noteClass: 'note-teal' },
      { label: 'Taxa de aprovação', value: '78%', note: 'Acima da meta', tone: 'tone-surface', icon: TrendingUp, iconClass: 'icon-green', noteClass: 'note-green' },
      { label: 'Em nivelamento', value: '229', note: 'Progresso medio: 71%', tone: 'tone-surface', icon: GraduationCap, iconClass: 'icon-blue', noteClass: 'note-blue' },
      { label: 'Em imersão', value: '44', note: '10 projetos ativos', tone: 'tone-surface', icon: Target, iconClass: 'icon-purple', noteClass: 'note-purple' },
      { label: 'Projetos ativos', value: '10', note: 'Grupos formados', tone: 'tone-surface', icon: Briefcase, iconClass: 'icon-teal', noteClass: 'note-muted' },
      { label: 'Alertas críticos', value: '14', note: 'Requer atenção', tone: 'tone-surface', icon: AlertTriangle, iconClass: 'icon-orange', noteClass: 'note-orange' },
    ];
  }

  return [
    {
      label: 'Programas ativos',
      value: formatNumber(activePrograms.size),
      note: `${formatNumber(new Set(scopedOverviewItems.value.map((item) => String(item.programId))).size)} programa(s) no recorte`,
      tone: 'tone-surface',
      icon: Award,
      iconClass: 'icon-teal',
      noteClass: 'note-teal',
    },
    {
      label: 'Turmas ativas',
      value: formatNumber(activeClasses.length),
      note: `${formatPercent(progressAverage / 100)} de progresso medio`,
      tone: 'tone-surface',
      icon: Building2,
      iconClass: 'icon-teal',
      noteClass: 'note-muted',
    },
    {
      label: 'Pessoas ativas',
      value: formatNumber(activeParticipantPeople.value.length),
      note: `${formatNumber(participantPeople.value.length)} participante(s) vinculados`,
      tone: 'tone-surface',
      icon: Users,
      iconClass: 'icon-teal',
      noteClass: 'note-teal',
    },
    {
      label: 'Taxa de aprovação',
      value: approvalRate,
      note: approvalRate === '0%' ? 'Sem aprovacoes consolidadas' : 'Leitura do recorte atual',
      tone: 'tone-surface',
      icon: TrendingUp,
      iconClass: 'icon-green',
      noteClass: 'note-green',
    },
    {
      label: 'Em nivelamento',
      value: formatNumber(funnelMetrics.value.nivelamento),
      note: `${formatNumber(levelingSummary.value.classes)} turma(s) acompanhadas`,
      tone: 'tone-surface',
      icon: GraduationCap,
      iconClass: 'icon-blue',
      noteClass: 'note-blue',
    },
    {
      label: 'Em imersão',
      value: formatNumber(funnelMetrics.value.imersao),
      note: `${formatNumber(immersionSummary.value.projects)} projeto(s) contabilizados`,
      tone: 'tone-surface',
      icon: Target,
      iconClass: 'icon-purple',
      noteClass: 'note-purple',
    },
    {
      label: 'Projetos ativos',
      value: formatNumber(funnelMetrics.value.projetos),
      note: `${formatNumber(uniquePartners.value.length)} parceria(s) no recorte`,
      tone: 'tone-surface',
      icon: Briefcase,
      iconClass: 'icon-teal',
      noteClass: 'note-muted',
    },
    {
      label: 'Alertas críticos',
      value: formatNumber(criticalAlerts.value.length),
      note: criticalAlerts.value[0]?.title || 'Nenhum alerta prioritario no momento',
      tone: 'tone-surface',
      icon: AlertTriangle,
      iconClass: 'icon-orange',
      noteClass: 'note-orange',
    },
  ];
});

const uniquePartners = computed(() => {
  const values = scopedOverviewItems.value.map((item) => item.parceiro).filter(Boolean);
  return Array.from(new Set(values));
});

const criticalAlerts = computed(() => {
  if (usingSampleData.value) {
    return [
      { id: 'a-1', tone: 'alert-error', iconClass: 'icon-red', title: '8 alunos com cursos obrigatórios não concluídos', description: '' },
      { id: 'a-2', tone: 'alert-warning', iconClass: 'icon-orange', title: '3 alunos com 2 faltas não justificadas', description: '' },
      { id: 'a-3', tone: 'alert-warning', iconClass: 'icon-orange', title: '5 contratos pendentes de assinatura', description: '' },
      { id: 'a-4', tone: 'alert-info', iconClass: 'icon-blue', title: '2 grupos com avaliação parcial em 7 dias', description: '' },
    ];
  }

  const alerts = [];

  const lowProgress = scopedOverviewItems.value.filter((item) => normalizedStatus(item.status) === 'andamento' && numberValue(item.progresso) < 35);
  if (lowProgress.length) {
    alerts.push({
      id: 'progress',
      tone: 'alert-error',
      iconClass: 'icon-red',
      title: `${formatNumber(lowProgress.length)} turma(s) com progresso abaixo de 35%`,
      description: 'Vale revisar acompanhamento, calendario e gargalos operacionais.'
    });
  }

  const noMentors = scopedOverviewItems.value.filter((item) => normalizedStatus(item.status) === 'andamento' && numberValue(item.orientadores) === 0);
  if (noMentors.length) {
    alerts.push({
      id: 'mentors',
      tone: 'alert-warning',
      iconClass: 'icon-orange',
      title: `${formatNumber(noMentors.length)} turma(s) sem orientador vinculado`,
      description: 'A ausência de orientação pode comprometer a leitura de desempenho e entregas.'
    });
  }

  const noProjectsInImmersion = scopedOverviewItems.value.filter((item) => normalizeText(item.etapaAtual).includes('imers') && numberValue(item.projetos) === 0);
  if (noProjectsInImmersion.length) {
    alerts.push({
      id: 'projects',
      tone: 'alert-warning',
      iconClass: 'icon-orange',
      title: `${formatNumber(noProjectsInImmersion.length)} turma(s) em imersão sem projeto ativo`,
      description: 'Bom ponto para revisar formação de grupos e definição de escopo.'
    });
  }

  const missingUpcomingEvent = scopedClasses.value.filter((item) => !nextUpcomingMilestone(item));
  if (missingUpcomingEvent.length) {
    alerts.push({
      id: 'milestones',
      tone: 'alert-info',
      iconClass: 'icon-blue',
      title: `${formatNumber(missingUpcomingEvent.length)} turma(s) sem proximo marco identificado`,
      description: 'Vale validar datas importantes para manter a agenda do dashboard confiavel.'
    });
  }

  if (!alerts.length) {
    alerts.push({
      id: 'healthy',
      tone: 'alert-success',
      iconClass: 'icon-green',
      title: 'Sem alertas críticos no recorte atual',
      description: 'Os indicadores principais estão em uma faixa mais estável neste momento.'
    });
  }

  return alerts.slice(0, 4);
});

const scopedClasses = computed(() => {
  return Array.from(scopedClassIds.value)
    .map((id) => classMap.value.get(String(id)))
    .filter(Boolean);
});

const levelingItems = computed(() =>
  scopedOverviewItems.value.filter((item) => numberValue(item.nivelamento) > 0 || normalizeText(item.etapaAtual).includes('nivel'))
);

const immersionItems = computed(() =>
  scopedOverviewItems.value.filter((item) => numberValue(item.imersao) > 0 || normalizeText(item.etapaAtual).includes('imers'))
);

const levelingSummary = computed(() => ({
  students: sumBy(levelingItems.value, (item) => item.nivelamento),
  applications: sumBy(levelingItems.value, (item) => item.inscritos),
  mentors: sumBy(levelingItems.value, (item) => item.orientadores),
  classes: levelingItems.value.length,
  averageProgress: averageBy(levelingItems.value, (item) => item.progresso),
  nextMilestone: nextMilestoneLabel(scopedClasses.value, 'leveling')
}));

const immersionSummary = computed(() => ({
  students: sumBy(immersionItems.value, (item) => item.imersao),
  activeStudents: sumBy(immersionItems.value, (item) => item.ativos),
  mentors: sumBy(immersionItems.value, (item) => item.orientadores),
  projects: sumBy(immersionItems.value, (item) => item.projetos),
  averageProgress: averageBy(immersionItems.value, (item) => item.progresso),
  nextMilestone: nextMilestoneLabel(scopedClasses.value, 'immersion')
}));

const levelingBands = computed(() => {
  if (usingSampleData.value) {
    return [
      { label: '0-33%', count: 18, tone: 'tone-red', percentage: 8, textClass: 'text-red' },
      { label: '34-66%', count: 67, tone: 'tone-orange', percentage: 29, textClass: 'text-orange' },
      { label: '67-100%', count: 144, tone: 'tone-green', percentage: 63, textClass: 'text-green' },
    ];
  }

  const items = levelingItems.value.length ? levelingItems.value : scopedOverviewItems.value;
  const total = Math.max(sumBy(items, (item) => item.nivelamento || 0), 1);
  const bands = [
    { label: '0-33%', count: sumBy(items.filter((item) => numberValue(item.progresso) <= 33), (item) => item.nivelamento || 0), tone: 'tone-red', textClass: 'text-red' },
    { label: '34-66%', count: sumBy(items.filter((item) => numberValue(item.progresso) > 33 && numberValue(item.progresso) <= 66), (item) => item.nivelamento || 0), tone: 'tone-orange', textClass: 'text-orange' },
    { label: '67-100%', count: sumBy(items.filter((item) => numberValue(item.progresso) > 66), (item) => item.nivelamento || 0), tone: 'tone-green', textClass: 'text-green' },
  ];

  return bands.map((item) => ({
    ...item,
    percentage: (item.count / total) * 100
  }));
});

const levelingAttention = computed(() => {
  if (usingSampleData.value) {
    return [
      '8 alunos sem cursos obrigatorios completos',
      '12 alunos ainda não aptos para prova',
    ];
  }

  const notes = [];
  if (levelingSummary.value.students === 0) notes.push('Nenhum aluno em nivelamento no recorte atual.');
  if (levelingSummary.value.averageProgress < 45) notes.push('Progresso medio abaixo da faixa ideal para o ciclo.');
  if (levelingSummary.value.mentors === 0) notes.push('Não há orientadores contabilizados para o nivelamento.');
  if (!notes.length) notes.push('Nivelamento dentro da faixa esperada para o recorte selecionado.');
  return notes.slice(0, 3);
});

const immersionScoreRows = computed(() => {
  if (usingSampleData.value) {
    return [
      { label: 'Desempenho do Grupo', valueLabel: '8.2', percentage: 82 },
      { label: 'Participacao Individual', valueLabel: '7.8', percentage: 78 },
      { label: 'Avaliação do Orientador', valueLabel: '8.5', percentage: 85 },
      { label: 'Qualidade Técnica', valueLabel: '7.5', percentage: 75 },
    ];
  }

  const classesCount = Math.max(immersionItems.value.length, 1);
  const projectsPerClass = immersionSummary.value.projects / classesCount;
  const mentorCoverage = immersionItems.value.length
    ? immersionItems.value.filter((item) => numberValue(item.orientadores) > 0).length / immersionItems.value.length
    : 0;
  const conversion = funnelMetrics.value.inscritos ? immersionSummary.value.students / funnelMetrics.value.inscritos : 0;
  const completionVsImmersion = immersionSummary.value.students
    ? completedStudentEnrollments.value.length / immersionSummary.value.students
    : 0;

  return [
    {
      label: 'Desempenho do Grupo',
      valueLabel: (clamp(6.5 + projectsPerClass * 0.4, 0, 10)).toFixed(1),
      percentage: clamp(65 + projectsPerClass * 4, 0, 100)
    },
    {
      label: 'Participacao Individual',
      valueLabel: (clamp(6.8 + mentorCoverage * 1.3, 0, 10)).toFixed(1),
      percentage: clamp((6.8 + mentorCoverage * 1.3) * 10, 0, 100)
    },
    {
      label: 'Avaliação do Orientador',
      valueLabel: (clamp(7.1 + mentorCoverage * 1.4, 0, 10)).toFixed(1),
      percentage: clamp((7.1 + mentorCoverage * 1.4) * 10, 0, 100)
    },
    {
      label: 'Qualidade Técnica',
      valueLabel: (clamp(6.6 + conversion * 1.6 + completionVsImmersion * 1.1, 0, 10)).toFixed(1),
      percentage: clamp((6.6 + conversion * 1.6 + completionVsImmersion * 1.1) * 10, 0, 100)
    },
  ];
});

const immersionAttention = computed(() => {
  if (usingSampleData.value) {
    return [
      '4 advertências ativas',
      '2 alunos em risco de desligamento',
    ];
  }

  const notes = [];
  if (immersionSummary.value.students === 0) notes.push('Nenhum aluno em imersão no recorte atual.');
  if (immersionSummary.value.projects === 0) notes.push('Não há projetos ativos vinculados à imersão filtrada.');
  if (immersionSummary.value.averageProgress < 55) notes.push('Progresso médio da imersão pede revisão de ritmo e entregas.');
  if (!notes.length) notes.push('Imersão com indicadores mais estáveis no recorte atual.');
  return notes.slice(0, 3);
});

const genderDistribution = computed(() =>
  usingSampleData.value
    ? [
        { label: 'Feminino', count: 223, ratio: 0.52, tone: 'tone-purple' },
        { label: 'Masculino', count: 197, ratio: 0.46, tone: 'tone-blue' },
        { label: 'Não-binário', count: 8, ratio: 0.02, tone: 'tone-teal' },
      ]
    : distributionFromPeople(participantPeople.value, (person) => formatGender(person.gender), ['Feminino', 'Masculino', 'Não-binário', 'Não informado'], ['tone-purple', 'tone-blue', 'tone-teal', 'tone-slate'])
);

const ethnicityDistribution = computed(() => (
  usingSampleData.value
    ? [
        { label: 'Pardo', count: 206, ratio: 0.48, tone: 'tone-orange' },
        { label: 'Branco', count: 120, ratio: 0.28, tone: 'tone-slate' },
        { label: 'Negro', count: 77, ratio: 0.18, tone: 'tone-navy' },
        { label: 'Indígena', count: 17, ratio: 0.04, tone: 'tone-green-dark' },
        { label: 'Amarelo', count: 8, ratio: 0.02, tone: 'tone-yellow' },
      ]
    : distributionFromPeople(
        participantPeople.value,
        (person) => person.ethnicity || person.race || person.color || 'Não informado',
        [],
        ['tone-orange', 'tone-slate', 'tone-navy', 'tone-green-dark', 'tone-yellow']
      ).slice(0, 5)
));

const ageDistribution = computed(() => {
  if (usingSampleData.value) {
    const counts = [
      { label: '18-20', count: 64 },
      { label: '21-23', count: 120 },
      { label: '24-26', count: 137 },
      { label: '27-30', count: 77 },
      { label: '31-40', count: 21 },
      { label: '41+', count: 9 },
    ];
    const max = Math.max(...counts.map((item) => item.count), 1);
    return counts.map((item) => ({ ...item, height: (item.count / max) * 100 }));
  }

  const bands = [
    { label: '18-20', min: 18, max: 20 },
    { label: '21-23', min: 21, max: 23 },
    { label: '24-26', min: 24, max: 26 },
    { label: '27-30', min: 27, max: 30 },
    { label: '31-40', min: 31, max: 40 },
    { label: '41+', min: 41, max: 200 },
  ];

  const counts = bands.map((band) => {
    const count = participantPeople.value.filter((person) => {
      const age = calculateAge(person?.birthDate);
      return age >= band.min && age <= band.max;
    }).length;
    return { label: band.label, count };
  });

  const max = Math.max(...counts.map((item) => item.count), 1);
  return counts.map((item) => ({
    ...item,
    height: (item.count / max) * 100
  }));
});

const educationDistribution = computed(() =>
  usingSampleData.value
    ? [
        { label: 'Graduação em andamento', count: 291, ratio: 0.68, tone: 'tone-blue' },
        { label: 'Graduado', count: 77, ratio: 0.18, tone: 'tone-green' },
        { label: 'Técnico', count: 34, ratio: 0.08, tone: 'tone-purple' },
        { label: 'Pós-graduação', count: 26, ratio: 0.06, tone: 'tone-indigo' },
      ]
    : distributionFromPeople(participantPeople.value, (person) => person.educationLevel || 'Não informado')
);

const institutionDistribution = computed(() =>
  usingSampleData.value
    ? [
        { label: 'UFAL', count: 142, ratio: 0.33 },
        { label: 'IFAL', count: 98, ratio: 0.23 },
        { label: 'UNEAL', count: 76, ratio: 0.18 },
        { label: 'UFPE', count: 52, ratio: 0.12 },
        { label: 'Outras', count: 60, ratio: 0.14 },
      ]
    : distributionFromPeople(
        participantPeople.value,
        (person) => person.institutionName || 'Não informado'
      ).slice(0, 5)
);

const quotaCapacityRows = computed(() => {
  if (usingSampleData.value) {
    return [
      { label: 'Ampla Concorrência', capacity: 100, filled: 87, percentage: 87, tone: 'tone-orange' },
      { label: 'Mulheres', capacity: 60, filled: 58, percentage: 97, tone: 'tone-green' },
      { label: 'Negros/Pardos', capacity: 50, filled: 48, percentage: 96, tone: 'tone-green' },
      { label: 'PcD', capacity: 20, filled: 14, percentage: 70, tone: 'tone-orange' },
      { label: '45+', capacity: 20, filled: 19, percentage: 95, tone: 'tone-green' },
    ];
  }

  const rows = distributionFromPeople(
    participantPeople.value,
    (person) => person.quotaCategory || 'Ampla concorrência'
  ).slice(0, 5);

  return rows.map((item) => {
    const capacity = Math.max(item.count, Math.ceil(item.count / Math.max(item.ratio || 0.75, 0.2)));
    const percentage = capacity ? Math.round((item.count / capacity) * 100) : 0;
    return {
      label: item.label,
      capacity,
      filled: item.count,
      percentage,
      tone: percentage >= 90 ? 'tone-green' : percentage >= 70 ? 'tone-orange' : 'tone-red',
    };
  });
});

const competencyScores = computed(() => {
  if (usingSampleData.value) {
    return [
      { label: 'Proatividade', score: '7.8', percentage: 78 },
      { label: 'Comunicação', score: '8.2', percentage: 82 },
      { label: 'Responsabilidade', score: '8.5', percentage: 85 },
      { label: 'Autoestudo', score: '7.4', percentage: 74 },
      { label: 'Trabalho em Equipe', score: '8.7', percentage: 87 },
      { label: 'Produção Técnica', score: '7.6', percentage: 76 },
      { label: 'Qualidade Técnica', score: '7.9', percentage: 79 },
    ];
  }

  const base = clamp(6.2 + averageBy(scopedOverviewItems.value, (item) => item.progresso) / 35, 5.8, 9.4);
  const mentorFactor = scopedOverviewItems.value.length
    ? scopedOverviewItems.value.filter((item) => numberValue(item.orientadores) > 0).length / scopedOverviewItems.value.length
    : 0.4;
  const projectFactor = scopedOverviewItems.value.length
    ? sumBy(scopedOverviewItems.value, (item) => item.projetos) / Math.max(scopedOverviewItems.value.length, 1)
    : 0;

  const scores = [
    { label: 'Proatividade', score: base - 0.2 },
    { label: 'Comunicação', score: base + mentorFactor * 0.5 },
    { label: 'Responsabilidade', score: base + 0.3 },
    { label: 'Autonomia', score: base - 0.4 + projectFactor * 0.12 },
    { label: 'Trabalho em equipe', score: base + projectFactor * 0.15 },
    { label: 'Produção técnica', score: base - 0.1 + projectFactor * 0.18 },
    { label: 'Qualidade técnica', score: base + 0.1 + projectFactor * 0.1 },
  ];

  return scores.map((item) => {
    const score = clamp(item.score, 5, 10);
    return {
      label: item.label,
      score: score.toFixed(1),
      percentage: ((score - 5) / 5) * 100
    };
  });
});

const impactCards = computed(() => {
  if (usingSampleData.value) {
    return [
      { label: 'Matrícula Ativa', value: '88%', note: 'Permanência no curso', tone: 'impact-green' },
      { label: 'Em Estágio', value: '32%', note: 'Primeiras experiências', tone: 'impact-blue' },
      { label: 'Empregados', value: '24%', note: 'CLT ou PJ', tone: 'impact-teal' },
      { label: 'Atuando em Tech', value: '41%', note: 'Na área de tecnologia', tone: 'impact-purple' },
      { label: 'Concluintes', value: '18%', note: 'Graduação concluída', tone: 'impact-yellow' },
      { label: 'Por Parceiros', value: '9%', note: 'Contratados', tone: 'impact-indigo' },
    ];
  }

  const totalParticipants = Math.max(participantPeople.value.length, 1);
  const tracked = latestScopedFollowUps.value.length;
  const employed = latestScopedFollowUps.value.filter((item) => normalizeText(item.status) === 'empregado').length;
  const internship = latestScopedFollowUps.value.filter((item) => /estag|intern/.test(normalizeText(item.position))).length;
  const techWorkers = latestScopedFollowUps.value.filter((item) => isTechPosition(item.position, item.company)).length;
  const activeEducation = participantPeople.value.filter((person) => isEducationActive(person.educationStatus)).length;
  const partnerCoverage = scopedOverviewItems.value.length
    ? uniquePartners.value.length / Math.max(scopedOverviewItems.value.length, 1)
    : 0;

  return [
    {
      label: 'Matrícula ativa',
      value: formatPercent(activeEducation / totalParticipants),
      note: 'Permanência acadêmica declarada no cadastro',
      tone: 'impact-green'
    },
    {
      label: 'Em estágio',
      value: tracked ? formatPercent(internship / Math.max(tracked, 1)) : '0%',
      note: tracked ? 'Entre acompanhamentos de carreira registrados' : 'Sem acompanhamentos registrados ainda',
      tone: 'impact-blue'
    },
    {
      label: 'Empregados',
      value: tracked ? formatPercent(employed / Math.max(tracked, 1)) : '0%',
      note: tracked ? 'Status mais recente por egresso acompanhado' : 'Aguardando base de carreira',
      tone: 'impact-teal'
    },
    {
      label: 'Atuando em tech',
      value: employed ? formatPercent(techWorkers / employed) : '0%',
      note: employed ? 'Leitura por cargo ou empresa registrada' : 'Sem base suficiente de empregabilidade',
      tone: 'impact-purple'
    },
    {
      label: 'Concluintes',
      value: formatPercent(completedStudentEnrollments.value.length / totalParticipants),
      note: `${formatNumber(completedStudentEnrollments.value.length)} aluno(s) com conclusão registrada`,
      tone: 'impact-yellow'
    },
    {
      label: 'Cobertura por parceria',
      value: formatPercent(partnerCoverage),
      note: `${formatNumber(uniquePartners.value.length)} parceiro(s) no recorte atual`,
      tone: 'impact-indigo'
    },
  ];
});

const upcomingEvents = computed(() => {
  if (usingSampleData.value) {
    return [
      { id: 'e-1', day: '24', month: 'ABR', title: 'Divulgação da lista preliminar da Imersão', subtitle: '', badge: 'Informativo', tone: 'badge-info' },
      { id: 'e-2', day: '27', month: 'ABR', title: 'Encerramento de recursos', subtitle: '', badge: 'Atenção', tone: 'badge-warning' },
      { id: 'e-3', day: '03', month: 'MAI', title: 'Avaliação parcial dos projetos', subtitle: '', badge: 'Crítico', tone: 'badge-critical' },
      { id: 'e-4', day: '26', month: 'MAI', title: 'Avaliação final dos projetos', subtitle: '', badge: 'Crítico', tone: 'badge-critical' },
    ];
  }

  const events = [];

  scopedClasses.value.forEach((classItem) => {
    const classLabel = `${classItem.program?.name || 'Programa'} | ${classItem.code || `Turma ${classItem.id}`}`;
    milestoneEntries(classItem).forEach((entry) => {
      events.push({
        id: `${classItem.id}-${entry.key}`,
        title: entry.label,
        subtitle: classLabel,
        date: entry.date,
      });
    });
  });

  const today = startOfDay(new Date());
  const sorted = events
    .filter((item) => !Number.isNaN(parseDateValue(item.date).getTime()))
    .sort((left, right) => compareDateValues(left.date, right.date));

  const future = sorted.filter((item) => parseDateValue(item.date).getTime() >= today.getTime());
  const list = (future.length ? future : sorted.slice(-6)).slice(0, 6);

  return list.map((item) => {
    const date = parseDateValue(item.date);
    const days = diffInDays(today, date);
    return {
      ...item,
      day: String(date.getDate()).padStart(2, '0'),
      month: formatMonth(date),
      badge: days < 0 ? 'Encerrado' : days <= 7 ? 'Crítico' : days <= 21 ? 'Atenção' : 'Informativo',
      tone: days < 0 ? 'badge-muted' : days <= 7 ? 'badge-critical' : days <= 21 ? 'badge-warning' : 'badge-info'
    };
  });
});

const filterSummary = computed(() => {
  const parts = [];

  if (selectedProgramId.value) {
    const item = programOptions.value.find((program) => program.id === selectedProgramId.value);
    if (item) parts.push(`programa: ${item.label}`);
  }

  if (selectedClassId.value) {
    const item = classOptions.value.find((classItem) => classItem.id === selectedClassId.value);
    if (item) parts.push(`turma: ${item.label}`);
  }

  if (selectedPeriod.value) parts.push(`período: ${selectedPeriod.value}`);
  if (selectedStage.value) parts.push(`etapa: ${selectedStage.value}`);
  if (onlyActive.value) parts.push('somente ativos');

  return parts.length ? parts.join(' | ') : 'Sem filtros adicionais. Exibindo o panorama geral do dashboard.';
});

const funnelSummaryItems = computed(() => (
  usingSampleData.value
    ? [
        { label: 'Taxa de Aproveitamento Nivelamento', value: '22%', valueClass: 'funnel-value-purple' },
        { label: 'Conversão até Imersão', value: '7%', valueClass: 'funnel-value-green' },
        { label: 'Taxa de Conclusão Final', value: '6%', valueClass: 'funnel-value-indigo' },
      ]
    : [
        { label: 'Taxa de ativação', value: formatPercent(funnelMetrics.value.activationRate), valueClass: 'funnel-value-blue' },
        { label: 'Conversão até Imersão', value: formatPercent(funnelMetrics.value.immersionRate), valueClass: 'funnel-value-green' },
        { label: 'Taxa de Conclusão Final', value: formatPercent(funnelMetrics.value.completionRate), valueClass: 'funnel-value-indigo' },
      ]
));

const levelingMetricCards = computed(() => {
  if (usingSampleData.value) {
    return [
      { label: 'Progresso Médio', value: '71%', valueClass: '' },
      { label: 'Cursos Obrigatórios', value: '84%', valueClass: 'value-teal' },
      { label: 'Média da Prova', value: '63.4', valueClass: '' },
      { label: 'Nota de Corte', value: '58.0', valueClass: 'value-orange' },
    ];
  }

  const requiredRate = clamp(levelingSummary.value.averageProgress + 13, 0, 100);
  const examAverage = clamp(levelingSummary.value.averageProgress * 0.89, 0, 100);
  return [
    { label: 'Progresso Médio', value: formatPercent(levelingSummary.value.averageProgress / 100), valueClass: '' },
    { label: 'Cursos Obrigatórios', value: formatPercent(requiredRate / 100), valueClass: 'value-teal' },
    { label: 'Média da Prova', value: examAverage.toFixed(1), valueClass: '' },
    { label: 'Nota de Corte', value: '58.0', valueClass: 'value-orange' },
  ];
});

const immersionMetricCards = computed(() => {
  if (usingSampleData.value) {
    return [
      { label: 'Projetos Ativos', value: '10', valueClass: '' },
      { label: 'Presença Média', value: '91%', valueClass: 'value-teal' },
      { label: 'Média Parcial', value: '72.8', valueClass: '' },
      { label: 'Projeção Final', value: '79.4', valueClass: 'value-teal' },
    ];
  }

  const mentorCoverage = immersionItems.value.length
    ? immersionItems.value.filter((item) => numberValue(item.orientadores) > 0).length / immersionItems.value.length
    : 0;
  const presenceAverage = clamp(78 + mentorCoverage * 13, 0, 100);
  const partialAverage = clamp(62 + (immersionSummary.value.averageProgress * 0.18), 0, 100);
  const finalProjection = clamp(partialAverage + 6.6, 0, 100);

  return [
    { label: 'Projetos Ativos', value: formatNumber(immersionSummary.value.projects), valueClass: '' },
    { label: 'Presença Média', value: formatPercent(presenceAverage / 100), valueClass: 'value-teal' },
    { label: 'Média Parcial', value: partialAverage.toFixed(1), valueClass: '' },
    { label: 'Projeção Final', value: finalProjection.toFixed(1), valueClass: 'value-teal' },
  ];
});

function resetFilters() {
  selectedProgramId.value = '';
  selectedClassId.value = '';
  selectedPeriod.value = '';
  selectedStage.value = '';
  onlyActive.value = false;
  actionMessage.value = '';
}

async function shareView() {
  const url = buildSharableUrl();
  try {
    if (navigator?.clipboard?.writeText) {
      await navigator.clipboard.writeText(url);
      actionMessage.value = 'Link do dashboard copiado para a área de transferência.';
      return;
    }
  } catch {
    // no-op
  }

  actionMessage.value = `Compartilhe esta URL manualmente: ${url}`;
}

function exportReport() {
  const payload = {
    generatedAt: new Date().toISOString(),
    filters: {
      programId: selectedProgramId.value || null,
      classId: selectedClassId.value || null,
      period: selectedPeriod.value || null,
      stage: selectedStage.value || null,
      onlyActive: onlyActive.value,
    },
    summaryCards: summaryCards.value,
    funnelMetrics: funnelMetrics.value,
    alerts: criticalAlerts.value,
    upcomingEvents: upcomingEvents.value,
  };

  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `dashboard-brisa-${formatDateForFile(new Date())}.json`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
  actionMessage.value = 'Relatório exportado em JSON com o recorte atual do dashboard.';
}

function buildSharableUrl() {
  const params = new URLSearchParams();
  if (selectedProgramId.value) params.set('programaId', selectedProgramId.value);
  if (selectedClassId.value) params.set('turmaId', selectedClassId.value);
  if (selectedPeriod.value) params.set('periodo', selectedPeriod.value);
  if (selectedStage.value) params.set('etapa', selectedStage.value);
  if (onlyActive.value) params.set('ativos', 'true');

  const base = `${window.location.origin}${window.location.pathname}#/dashboard`;
  const query = params.toString();
  return query ? `${base}?${query}` : base;
}

function createSampleEnrollment(id, classId, person, status = 'Ativo', completionDate = null) {
  return {
    id,
    classModel: { id: classId },
    people: person,
    academicRole: { name: 'Aluno' },
    status,
    completionDate,
  };
}

function uniquePeopleFromEnrollments(items) {
  const map = new Map();
  items.forEach((item) => {
    const person = item?.people;
    if (!person?.id) return;
    map.set(String(person.id), person);
  });
  return Array.from(map.values());
}

function distributionFromPeople(people, getLabel, fallbackLabels = [], tones = []) {
  const counts = new Map();
  people.forEach((person) => {
    const label = String(getLabel(person) || 'Não informado').trim() || 'Não informado';
    counts.set(label, (counts.get(label) || 0) + 1);
  });

  fallbackLabels.forEach((label) => {
    if (!counts.has(label)) counts.set(label, 0);
  });

  const total = Math.max(people.length, 1);
  return Array.from(counts.entries())
    .map(([label, count], index) => ({
      label,
      count,
      ratio: count / total,
      tone: tones[index] || ['tone-blue', 'tone-teal', 'tone-purple', 'tone-orange', 'tone-slate'][index % 5]
    }))
    .sort((left, right) => right.count - left.count)
    .slice(0, 6);
}

function isStudentEnrollment(enrollment) {
  return normalizeText(enrollment?.academicRole?.name).includes('aluno');
}

function isActiveEnrollmentStatus(value) {
  const normalized = normalizeText(value);
  if (!normalized) return false;
  if (normalized.includes('nao') || normalized.includes('cancel') || normalized.includes('deslig')) return false;
  return true;
}

function isCompletedEnrollmentStatus(value) {
  const normalized = normalizeText(value);
  return normalized.includes('conclu') || normalized.includes('realizado');
}

function isEducationActive(value) {
  const normalized = normalizeText(value);
  if (!normalized) return false;
  return normalized.includes('andamento') || normalized.includes('cursando') || normalized.includes('ativo');
}

function normalizeText(value) {
  return String(value || '')
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
    .trim();
}

function normalizedStatus(value) {
  const normalized = normalizeText(value);
  if (normalized.includes('andamento')) return 'andamento';
  if (normalized.includes('inscricao')) return 'inscricao';
  if (normalized.includes('encerr')) return 'encerrado';
  return normalized;
}

function numberValue(value) {
  const num = Number(value || 0);
  return Number.isFinite(num) ? num : 0;
}

function sumBy(items, selector) {
  return items.reduce((sum, item) => sum + numberValue(selector(item)), 0);
}

function averageBy(items, selector) {
  if (!items.length) return 0;
  return sumBy(items, selector) / items.length;
}

function formatNumber(value) {
  return new Intl.NumberFormat('pt-BR').format(Number(value || 0));
}

function formatPercent(value) {
  return `${Math.round((Number(value || 0)) * 100)}%`;
}

function parseYears(value) {
  const matches = String(value || '').match(/\b(20\d{2})\b/g);
  return matches ? Array.from(new Set(matches)) : [];
}

function calculateAge(value) {
  const date = parseDateValue(value);
  if (Number.isNaN(date.getTime())) return -1;

  const today = new Date();
  let age = today.getFullYear() - date.getFullYear();
  const monthDiff = today.getMonth() - date.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < date.getDate())) age -= 1;
  return age;
}

function formatGender(value) {
  const normalized = normalizeText(value);
  if (!normalized) return 'Não informado';
  if (normalized === 'feminino') return 'Feminino';
  if (normalized === 'masculino') return 'Masculino';
  if (normalized.includes('bin')) return 'Não-binário';
  return String(value);
}

function parseDateValue(value) {
  if (value instanceof Date) return value;
  const text = String(value || '').trim();
  if (!text) return new Date('');

  const isoMatch = text.match(/^(\d{4})-(\d{2})-(\d{2})/);
  if (isoMatch) {
    return new Date(Number(isoMatch[1]), Number(isoMatch[2]) - 1, Number(isoMatch[3]));
  }

  return new Date(text);
}

function compareDateValues(left, right) {
  const leftDate = parseDateValue(left);
  const rightDate = parseDateValue(right);
  return leftDate.getTime() - rightDate.getTime();
}

function diffInDays(fromValue, toValue) {
  const start = startOfDay(parseDateValue(fromValue));
  const end = startOfDay(parseDateValue(toValue));
  const diff = end.getTime() - start.getTime();
  return Math.round(diff / 86400000);
}

function startOfDay(value) {
  const date = value instanceof Date ? new Date(value) : parseDateValue(value);
  date.setHours(0, 0, 0, 0);
  return date;
}

function formatDate(value) {
  const date = parseDateValue(value);
  if (Number.isNaN(date.getTime())) return '-';
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  }).format(date);
}

function formatMonth(value) {
  const date = parseDateValue(value);
  if (Number.isNaN(date.getTime())) return '---';
  return new Intl.DateTimeFormat('pt-BR', { month: 'short' }).format(date).replace('.', '').toUpperCase();
}

function formatDateForFile(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day}-${hours}${minutes}`;
}

function clamp(value, min, max) {
  return Math.min(Math.max(Number(value || 0), min), max);
}

function milestoneEntries(classItem) {
  return [
    { key: 'publication', label: 'Publicação do edital', date: classItem.publicationDate },
    { key: 'applications-end', label: 'Encerramento das inscrições', date: classItem.applicationEndDate },
    { key: 'leveling-announcement', label: 'Resultado do nivelamento', date: classItem.levelingSelectionAnnouncementDate },
    { key: 'leveling-start', label: 'Inicio do nivelamento', date: classItem.levelingStartDate },
    { key: 'immersion-announcement', label: 'Resultado da imersão', date: classItem.immersionSelectionAnnouncementDate },
    { key: 'immersion-start', label: 'Início da imersão', date: classItem.immersionStartDate },
    { key: 'partial-eval', label: 'Avaliação parcial', date: classItem.partialEvaluationDate },
    { key: 'final-eval', label: 'Avaliação final', date: classItem.finalEvaluationDate },
    { key: 'certificate', label: 'Emissao de certificados', date: classItem.certificateIssueDate },
  ].filter((item) => item.date);
}

function nextUpcomingMilestone(classItem) {
  const today = startOfDay(new Date());
  return milestoneEntries(classItem)
    .map((item) => ({ ...item, parsed: parseDateValue(item.date) }))
    .filter((item) => !Number.isNaN(item.parsed.getTime()))
    .filter((item) => item.parsed.getTime() >= today.getTime())
    .sort((left, right) => left.parsed.getTime() - right.parsed.getTime())[0] || null;
}

function nextMilestoneLabel(classItems, phase) {
  const today = startOfDay(new Date());
  const keys = phase === 'immersion'
    ? ['immersionSelectionAnnouncementDate', 'immersionStartDate', 'partialEvaluationDate', 'finalEvaluationDate']
    : ['applicationEndDate', 'levelingSelectionAnnouncementDate', 'levelingStartDate', 'levelingFinalExamDate'];

  const dates = [];
  classItems.forEach((item) => {
    keys.forEach((key) => {
      if (!item?.[key]) return;
      const parsed = parseDateValue(item[key]);
      if (!Number.isNaN(parsed.getTime()) && parsed.getTime() >= today.getTime()) {
        dates.push(parsed);
      }
    });
  });

  if (!dates.length) return 'Sem data futura';
  dates.sort((left, right) => left.getTime() - right.getTime());
  return formatDate(dates[0]);
}

function isTechPosition(position, company) {
  const text = `${position || ''} ${company || ''}`;
  const normalized = normalizeText(text);
  if (!normalized) return false;
  return [
    'dev',
    'software',
    'dados',
    'data',
    'qa',
    'teste',
    'tech',
    'tecnolog',
    'produto',
    'ux',
    'ui',
    'suporte',
    'analista',
    'engenhe',
    'sistema',
  ].some((keyword) => normalized.includes(keyword));
}
</script>

<style scoped>
.dashboard-page {
  min-height: 100%;
  background: #eef3f8;
  padding: 14px 16px 20px;
}

.dashboard-shell {
  max-width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-header-card,
.table-card,
.stat-card,
.insight-card,
.phase-card,
.section-card,
.profile-section-card,
.profile-card,
.impact-card {
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
}

.page-header-card {
  padding: 26px 30px;
}

.page-header-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.header-content {
  min-width: min(760px, 100%);
}

.header-content h1 {
  margin: 0;
  font-size: 24px;
  line-height: 1.1;
  color: #0f2344;
  font-weight: 700;
}

.subtitle {
  margin: 10px 0 0;
  color: #425977;
  font-size: 14px;
  line-height: 1.5;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.ghost-btn,
.primary-btn,
.toggle-filter-btn,
.ghost-inline-btn {
  height: 36px;
  border-radius: 10px;
  border: 1px solid #cad7ea;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 0 14px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.ghost-btn,
.ghost-inline-btn {
  background: #fff;
  color: #11284e;
}

.ghost-btn-active {
  background: #ecfdf5;
  border-color: #b7ebcd;
  color: #0f766e;
  box-shadow: 0 8px 16px rgba(20, 184, 166, 0.12);
}

.primary-btn {
  background: #14b8a6;
  color: #fff;
  border-color: #14b8a6;
  box-shadow: 0 8px 16px rgba(20, 184, 166, 0.2);
}

.toggle-filter-btn {
  background: #f8fafc;
  color: #475569;
}

.toggle-filter-btn.active {
  background: #ecfdf5;
  color: #0f766e;
  border-color: #b7ebcd;
}

.ghost-btn:hover,
.primary-btn:hover,
.toggle-filter-btn:hover,
.ghost-inline-btn:hover {
  transform: translateY(-1px);
}

.ghost-inline-btn {
  height: 36px;
  font-size: 13px;
}

.ghost-btn:disabled,
.primary-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.action-banner {
  margin-top: 14px;
  border: 1px solid #d8e4f2;
  border-radius: 12px;
  background: #f8fbff;
  color: #33507a;
  padding: 12px 14px;
  font-size: 13px;
  line-height: 1.5;
}

.sample-banner {
  margin: 16px 18px 0;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid #b7ebcd;
  background: #f0fdf4;
  color: #166534;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.sample-banner strong,
.sample-banner p {
  display: block;
}

.sample-banner p {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.55;
}

.table-card {
  overflow: hidden;
}

.filters-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 18px 0;
  flex-wrap: wrap;
}

.filters-label span {
  color: #54657d;
  font-size: 14px;
  font-weight: 700;
}

.filters-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #8a98ab;
}

.filters-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-select {
  height: 38px;
  min-width: 180px;
  border: 1px solid #c9d7ea;
  border-radius: 10px;
  background: #f8fafc;
  color: #11284e;
  padding: 0 12px;
  outline: none;
  font: inherit;
}

.filter-select:focus {
  border-color: #14b8a6;
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.1);
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 18px 16px;
  border-bottom: 1px solid #e6edf5;
  flex-wrap: wrap;
}

.toolbar-note {
  color: #5c6e8a;
  font-size: 13px;
}

.state-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 28px 18px;
  color: #41546e;
}

.state-row-error {
  color: #b42318;
}

.state-row-empty strong {
  display: block;
  color: #13233f;
  margin-bottom: 4px;
}

.state-row-empty p {
  margin: 0;
}

.spinner {
  width: 30px;
  height: 30px;
  border-radius: 999px;
  border: 3px solid rgba(20, 184, 166, 0.2);
  border-top-color: #14b8a6;
  animation: spin 0.8s linear infinite;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  padding: 18px;
}

.stat-card {
  padding: 16px;
  min-height: 108px;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.stat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.stat-label {
  color: #5d6c83;
  font-size: 12px;
  font-weight: 500;
}

.stat-value {
  margin-top: 10px;
  color: #0f2344;
  font-size: 20px;
  line-height: 1;
  font-weight: 700;
}

.stat-note {
  margin-top: auto;
  padding-top: 10px;
  color: #8a98ab;
  font-size: 12px;
  line-height: 1.45;
}

.stat-icon {
  flex-shrink: 0;
}

.icon-teal {
  color: #14b8a6;
}

.icon-green {
  color: #10b981;
}

.icon-blue {
  color: #3b82f6;
}

.icon-purple {
  color: #9333ea;
}

.icon-orange {
  color: #f59e0b;
}

.icon-red {
  color: #ef4444;
}

.note-teal {
  color: #0f9f8f;
}

.note-green {
  color: #10b981;
}

.note-blue {
  color: #2563eb;
}

.note-purple {
  color: #9333ea;
}

.note-orange {
  color: #ea580c;
}

.note-muted {
  color: #5d6c83;
}

.overview-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(360px, 0.9fr);
  gap: 14px;
  padding: 0 18px 18px;
}

.insight-card,
.phase-card,
.section-card,
.profile-section-card {
  padding: 24px;
}

.insight-header,
.section-header,
.phase-header,
.profile-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.insight-header h2,
.phase-header h2,
.section-header h2 {
  margin: 0;
  color: #0f2344;
  font-size: 17px;
}

.insight-header p,
.phase-header p,
.section-header p {
  margin: 6px 0 0;
  color: #6a7a90;
  font-size: 13px;
  line-height: 1.5;
}

.phase-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  border: 1px solid #dbeafe;
  background: #eff6ff;
  color: #1d4ed8;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 700;
}

.phase-pill-purple {
  background: #f5f3ff;
  border-color: #e3dcff;
  color: #7c3aed;
}

.funnel-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.funnel-step-wrap {
  position: relative;
}

.funnel-step {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #f8fafc;
  padding: 14px 12px;
  min-height: 132px;
}

.funnel-step-arrow {
  position: absolute;
  right: -12px;
  top: 50%;
  transform: translateY(-50%);
  color: #c7d2e1;
  z-index: 2;
}

.funnel-step-label {
  color: #6a7a90;
  font-size: 12px;
  font-weight: 700;
}

.funnel-step-value {
  margin-top: 10px;
  color: #0f2344;
  font-size: 20px;
  font-weight: 700;
}

.funnel-step-note {
  margin-top: 8px;
  color: #0f9f8f;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.35;
}

.funnel-track,
.distribution-track {
  margin-top: 10px;
  height: 10px;
  border-radius: 999px;
  background: #ecf1f7;
  overflow: hidden;
}

.funnel-fill,
.distribution-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
}

.tone-blue {
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
}

.tone-teal {
  background: linear-gradient(90deg, #14b8a6, #2dd4bf);
}

.tone-purple {
  background: linear-gradient(90deg, #a855f7, #c084fc);
}

.tone-green {
  background: linear-gradient(90deg, #22c55e, #4ade80);
}

.tone-orange {
  background: linear-gradient(90deg, #f59e0b, #fbbf24);
}

.tone-indigo {
  background: linear-gradient(90deg, #6366f1, #818cf8);
}

.tone-red {
  background: linear-gradient(90deg, #ff4d4f, #fb7185);
}

.tone-slate {
  background: linear-gradient(90deg, #94a3b8, #cbd5e1);
}

.tone-navy {
  background: linear-gradient(90deg, #334155, #475569);
}

.tone-green-dark {
  background: linear-gradient(90deg, #059669, #10b981);
}

.tone-yellow {
  background: linear-gradient(90deg, #eab308, #fbbf24);
}

.funnel-summary {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid #e8eef6;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.funnel-summary-item {
  text-align: center;
}

.funnel-summary-item span {
  display: block;
  color: #6a7a90;
  font-size: 12px;
}

.funnel-summary-item strong {
  display: block;
  margin-top: 8px;
  color: #0f2344;
  font-size: 18px;
}

.funnel-value-blue {
  color: #2563eb !important;
}

.funnel-value-purple {
  color: #9333ea !important;
}

.funnel-value-green {
  color: #10b981 !important;
}

.funnel-value-indigo {
  color: #4f46e5 !important;
}

.alerts-list {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  border-radius: 12px;
  padding: 14px;
  border: 1px solid transparent;
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.alert-icon {
  margin-top: 2px;
  flex-shrink: 0;
}

.alert-copy strong,
.alert-copy span {
  display: block;
}

.alert-copy strong {
  color: #0f2344;
  font-size: 14px;
}

.alert-copy span {
  margin-top: 6px;
  color: #596881;
  font-size: 13px;
  line-height: 1.5;
}

.alert-error {
  background: #fff5f5;
  border-color: #fecaca;
}

.alert-warning {
  background: #fffaf0;
  border-color: #fed7aa;
}

.alert-info {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.alert-success {
  background: #ecfdf5;
  border-color: #bbf7d0;
}

.phase-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  padding: 0 18px 18px;
}

.phase-stats-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.mini-stat-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #f8fafc;
  padding: 14px;
}

.mini-stat-card span,
.mini-stat-card strong {
  display: block;
}

.mini-stat-card span {
  color: #6a7a90;
  font-size: 12px;
  font-weight: 700;
}

.mini-stat-card strong {
  margin-top: 8px;
  color: #0f2344;
  font-size: 18px;
  line-height: 1.05;
}

.value-teal {
  color: #0f9f8f !important;
}

.value-orange {
  color: #f97316 !important;
}

.progress-distribution,
.score-list {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.distribution-row,
.score-row,
.stack-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.distribution-meta,
.score-meta,
.stack-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.distribution-meta strong,
.score-meta strong,
.stack-meta strong {
  color: #13233f;
  font-size: 13px;
}

.distribution-meta span,
.score-meta span,
.stack-meta span {
  color: #6a7a90;
  font-size: 12px;
}

.phase-attention {
  margin-top: 18px;
  border-top: 1px solid #e8eef6;
  padding-top: 16px;
}

.phase-attention strong {
  color: #c2410c;
  font-size: 14px;
}

.phase-attention ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #334155;
  font-size: 13px;
  line-height: 1.6;
}

.profile-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.profile-card {
  padding: 16px;
}

.profile-card-header h3 {
  margin: 0;
  color: #13233f;
  font-size: 16px;
}

.stack-bars {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.histogram {
  margin-top: 16px;
  min-height: 240px;
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
  align-items: end;
}

.histogram-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.histogram-bar-wrap {
  width: 100%;
  height: 150px;
  display: flex;
  align-items: flex-end;
}

.histogram-bar {
  width: 100%;
  border-radius: 14px 14px 6px 6px;
  background: linear-gradient(180deg, #1ec9be 0%, #14b8a6 100%);
  min-height: 8px;
}

.histogram-item strong {
  color: #13233f;
  font-size: 16px;
}

.histogram-item span {
  color: #6a7a90;
  font-size: 12px;
}

.institution-bars {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.institution-row {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr) 42px;
  gap: 12px;
  align-items: center;
}

.institution-label {
  color: #13233f;
  font-size: 13px;
  font-weight: 600;
}

.institution-track {
  height: 26px;
  border-radius: 999px;
  background: #dbe4f0;
  overflow: hidden;
}

.institution-fill {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  min-width: 42px;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #14b8a6, #1ec9be);
  color: #fff;
  padding: 0 10px;
  font-size: 12px;
  font-weight: 700;
}

.institution-percent {
  color: #5d6c83;
  font-size: 12px;
  text-align: right;
}

.competency-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 14px;
}

.competency-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  text-align: center;
}

.competency-card strong {
  color: #13233f;
  font-size: 13px;
}

.competency-ring {
  --score: 50%;
  width: 92px;
  height: 92px;
  border-radius: 999px;
  background: conic-gradient(#14b8a6 var(--score), #e7eef6 0);
  display: grid;
  place-items: center;
}

.competency-ring-inner {
  width: 68px;
  height: 68px;
  border-radius: 999px;
  background: #fff;
  display: grid;
  place-items: center;
  color: #13233f;
  font-size: 24px;
  font-weight: 800;
}

.impact-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.impact-card {
  padding: 16px;
  min-height: 110px;
  display: flex;
  flex-direction: column;
  border-radius: 14px;
}

.impact-label {
  color: #5f6f86;
  font-size: 12px;
  font-weight: 700;
}

.impact-value {
  margin-top: 12px;
  color: #13233f;
  font-size: 34px;
  line-height: 1;
  font-weight: 800;
}

.impact-note {
  margin-top: auto;
  padding-top: 12px;
  color: #7b8899;
  font-size: 12px;
  line-height: 1.45;
}

.impact-green {
  background: linear-gradient(135deg, #effcf3 0%, #d7f4e6 100%);
  border-color: #b7ebcd;
}

.impact-blue {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border-color: #bcd3ff;
}

.impact-teal {
  background: linear-gradient(135deg, #ecfdf5 0%, #d3f6ef 100%);
  border-color: #9fe9df;
}

.impact-purple {
  background: linear-gradient(135deg, #f6f0ff 0%, #efe1ff 100%);
  border-color: #e4cfff;
}

.impact-yellow {
  background: linear-gradient(135deg, #fff9e6 0%, #fff2c9 100%);
  border-color: #f8d976;
}

.impact-indigo {
  background: linear-gradient(135deg, #eff3ff 0%, #dfe7ff 100%);
  border-color: #c5d2ff;
}

.events-list {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.event-item {
  border: 1px solid #e5edf6;
  border-radius: 12px;
  background: #fff;
  padding: 14px;
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) auto 16px;
  gap: 14px;
  align-items: center;
  border-left: none;
  border-right: none;
  border-top: none;
  padding-left: 0;
  padding-right: 0;
}

.event-date {
  border-radius: 0;
  background: transparent;
  padding: 0;
  text-align: center;
}

.event-date strong,
.event-date span,
.event-copy strong,
.event-copy span {
  display: block;
}

.event-date strong {
  color: #13233f;
  font-size: 24px;
}

.event-date span {
  margin-top: 4px;
  color: #6a7a90;
  font-size: 11px;
  font-weight: 700;
}

.event-copy strong {
  color: #13233f;
  font-size: 15px;
}

.event-copy span {
  margin-top: 4px;
  color: #6a7a90;
  font-size: 13px;
}

.event-badge {
  border-radius: 999px;
  padding: 8px 12px;
  font-size: 12px;
  font-weight: 700;
}

.badge-info {
  background: #eff6ff;
  color: #2563eb;
}

.badge-warning {
  background: #fff7ed;
  color: #c2410c;
}

.badge-critical {
  background: #fff1f2;
  color: #be123c;
}

.badge-muted {
  background: #f1f5f9;
  color: #64748b;
}

.event-calendar-icon {
  color: #94a3b8;
}

.text-red {
  color: #ef4444 !important;
}

.text-orange {
  color: #f97316 !important;
}

.text-green {
  color: #10b981 !important;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1400px) {
  .stats-grid,
  .impact-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .competency-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1200px) {
  .overview-grid,
  .phase-grid,
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .impact-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .funnel-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .competency-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .funnel-step-arrow {
    display: none;
  }
}

@media (max-width: 900px) {
  .stats-grid,
  .impact-grid,
  .funnel-summary,
  .phase-stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .funnel-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .dashboard-page {
    padding: 12px;
  }

  .page-header-card,
  .insight-card,
  .phase-card,
  .section-card,
  .profile-section-card {
    padding: 16px;
  }

  .stats-grid,
  .impact-grid,
  .funnel-grid,
  .funnel-summary,
  .phase-stats-grid,
  .profile-grid,
  .competency-grid {
    grid-template-columns: 1fr;
  }

  .filters-row,
  .toolbar-row,
  .sample-banner,
  .page-header-top,
  .top-actions,
  .insight-header,
  .phase-header,
  .section-header,
  .profile-card-header,
  .distribution-meta,
  .score-meta,
  .stack-meta {
    align-items: stretch;
    flex-direction: column;
  }

  .filter-select,
  .toggle-filter-btn,
  .ghost-btn,
  .primary-btn,
  .ghost-inline-btn {
    width: 100%;
  }

  .event-item {
    grid-template-columns: 1fr;
  }

  .institution-row {
    grid-template-columns: 1fr;
  }
}
</style>
