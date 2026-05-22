<template>
  <div class="programs-page">
    <div class="programs-shell">
      <!-- Etapa 1: cabeçalho principal -->
      <section class="page-header-card">
        <div class="page-header-top">
          <div class="header-content">
            <h1>Programas</h1>
            <p class="subtitle">
              Gerencie os programas cadastrados, acompanhe suas turmas, etapas e andamento geral.
            </p>

            <div class="program-selector-row">
              <div class="program-selector">
                <button
                  type="button"
                  class="program-selector-btn"
                  :class="{ active: !!selectedProgramId || showAllProgramsClasses }"
                  @click="toggleProgramSelector"
                >
                  <Target :size="16" />
                  <span v-if="selectedProgram">
                    {{ selectedProgram.nome }} ({{ selectedProgram.totalTurmas }} turma<span v-if="selectedProgram.totalTurmas !== 1">s</span>)
                  </span>
                  <span v-else>Todos os programas</span>
                </button>

                <div v-if="showProgramSelector" class="program-selector-menu">
                  <button
                    type="button"
                    class="program-option-btn"
                    :class="{ active: showAllProgramsClasses }"
                    @click="selectAllPrograms"
                  >
                    <strong>Todos os programas</strong>
                    <small>Exibe todas as turmas de todos os programas</small>
                  </button>

                  <button
                    v-for="option in filteredProgramCatalog"
                    :key="option.programId"
                    type="button"
                    class="program-option-btn"
                    :class="{ active: selectedProgramId === option.programId }"
                    @click="selectProgram(option.programId)"
                  >
                    <strong>{{ option.nome }}</strong>
                    <small>{{ option.parceiro }} • {{ option.totalTurmas }} turma(s)</small>
                  </button>

                  <div v-if="filteredProgramCatalog.length === 0" class="program-option-empty">
                    Nenhum programa encontrado.
                  </div>
                </div>
              </div>

              <div class="program-selector-search">
                <Search class="search-icon" :size="18" />
                <input
                  v-model="programSelectorSearch"
                  type="text"
                  placeholder="Pesquisar programa..."
                  class="search-input"
                  @focus="showProgramSelector = true"
                />
              </div>
            </div>
          </div>

          <div class="top-actions">
            <button v-if="selectedProgramId" type="button" class="ghost-btn" @click="editSelectedProgram">
              <Edit2 :size="18" />
              Editar programa
            </button>

            <button type="button" class="ghost-btn" @click="openNewClassModal">
              <Plus :size="18" />
              Nova turma
            </button>

            <button type="button" class="primary-btn" @click="goToProgramRegistration">
              <Plus :size="18" />
              Novo programa
            </button>
          </div>
        </div>

        <div class="stats-grid" v-if="summaryCards.length">
          <article v-for="card in summaryCards" :key="card.label" class="stat-card">
            <div class="stat-header">
              <span class="stat-label">{{ card.label }}</span>
              <component :is="card.icon" :size="18" :class="`icon-${card.iconColor}`" />
            </div>
            <div class="stat-value">{{ card.value }}</div>
          </article>
        </div>
      </section>

      <!-- Etapa 2: blocos unidos da listagem -->
      <section class="table-card">
        <div class="tabs-bar">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            type="button"
            class="tab-item"
            :class="{ active: activeTab === tab.id }"
            @click="activeTab = tab.id"
          >
            <span class="tab-label">{{ tab.label }}</span>
            <span class="tab-count">{{ tab.count }}</span>
          </button>
        </div>

        <div class="filters-row">
          <div class="search-box">
            <Search class="search-icon" :size="18" />
            <input
              v-model="searchInput"
              type="text"
              placeholder="Buscar por turma, parceiro, localidade ou período..."
              class="search-input"
              @keyup.enter="applySearch"
            />
          </div>

          <div class="filters-actions">
            <button type="button" class="filters-button" @click="showFilters = !showFilters">
              Filtros avançados
              <span class="filters-badge">{{ activeFiltersCount }}</span>
            </button>

            <button type="button" class="search-button" @click="applySearch">
              Pesquisar
            </button>
          </div>
        </div>

        <div v-if="showFilters" class="advanced-filters">
          <select v-model="selectedStatus" class="filter-select">
            <option value="">Status</option>
            <option value="andamento">Em andamento</option>
            <option value="inscricao">Inscrição aberta</option>
            <option value="encerrado">Encerrado</option>
          </select>

          <select v-model="selectedStage" class="filter-select">
            <option value="">Etapa atual</option>
            <option v-for="stage in stageOptions" :key="stage" :value="stage">
              {{ stage }}
            </option>
          </select>

          <select v-model="selectedYear" class="filter-select">
            <option value="">Ano</option>
            <option v-for="year in yearOptions" :key="year" :value="year">
              {{ year }}
            </option>
          </select>

          <select v-model="selectedLocality" class="filter-select">
            <option value="">Localidade</option>
            <option v-for="locality in localityOptions" :key="locality" :value="locality">
              {{ locality }}
            </option>
          </select>
        </div>

        <!-- Etapa 3: estados da tela -->
        <div v-if="loading" class="state-row">
          <div class="spinner"></div>
          <span>Carregando programas...</span>
        </div>

        <div v-else-if="errorMessage" class="state-row state-row-error">
          <AlertTriangle :size="20" />
          <span>{{ errorMessage }}</span>
        </div>

        <div v-else-if="!selectedProgramId && !showAllProgramsClasses" class="state-row state-row-empty">
          <Target :size="22" />
          <div>
            <strong>Selecione uma visualização</strong>
            <p>Use "Programas" para zerar seleção ou "Todos os programas" para listar todas as turmas.</p>
          </div>
        </div>

        <div v-else-if="filteredPrograms.length === 0" class="state-row state-row-empty">
          <Target :size="22" />
          <div>
            <strong>Nenhuma turma encontrada</strong>
            <p>Use a busca ou ajuste os filtros para localizar turmas.</p>
          </div>
        </div>

        <!-- Etapa 4: listagem principal -->
        <div v-else class="table-scroll">
          <article
            v-for="program in paginatedPrograms"
            :key="`${program.programId}-${program.classId}`"
            class="program-row"
            :class="{ 'program-row-active-tab': activeTab === 'ativos' }"
            @click="viewProgram(program)"
          >
            <div class="program-main">
              <div class="program-title-row">
                <div>
                  <div class="program-title-line">
                    <h3>{{ `Turma ${program.turma || 'Sem nome'}` }}</h3>
                    <span class="class-chip">{{ program.nome || 'Programa sem nome' }}</span>
                  </div>

                  <div class="program-meta">
                    <span><Building2 :size="14" />{{ program.parceiro }}</span>
                    <span><MapPin :size="14" />{{ program.localidade }}</span>
                    <span><Calendar :size="14" />{{ program.periodo }}</span>
                  </div>
                </div>

                <span class="status-chip" :class="statusClass(program.status)">
                  {{ statusLabel(program.status) }}
                </span>
              </div>

              <div class="stage-track">
                <div v-for="(stage, index) in stageTrack" :key="stage" class="stage-step">
                  <div class="stage-pill" :class="stageClass(program, stage)">
                    {{ stage }}
                  </div>
                  <div
                    v-if="index < stageTrack.length - 1"
                    class="stage-connector"
                    :class="{ active: stageConnectorActive(program, index) }"
                  />
                </div>
              </div>

              <div class="progress-wrap">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: `${program.progresso}%` }" />
                </div>
              </div>

              <div class="metrics-grid">
                <div class="metric-box">
                  <span>Inscritos</span>
                  <strong>{{ program.inscritos }}</strong>
                </div>
                <div class="metric-box">
                  <span>Ativos</span>
                  <strong class="accent">{{ program.ativos }}</strong>
                </div>
                <div class="metric-box">
                  <span>Nivelamento</span>
                  <strong>{{ program.nivelamento }}</strong>
                </div>
                <div class="metric-box">
                  <span>Imersão</span>
                  <strong>{{ program.imersao }}</strong>
                </div>
                <div class="metric-box">
                  <span>Projetos</span>
                  <strong>{{ program.projetos }}</strong>
                </div>
                <div class="metric-box">
                  <span>Orientadores</span>
                  <strong>{{ program.orientadores }}</strong>
                </div>
              </div>

              <div v-if="program.proximoMarco && program.proximoMarco !== '-'" class="milestone">
                <Clock :size="16" />
                <span>Próximo marco:</span>
                <strong>{{ program.proximoMarco }}</strong>
              </div>
            </div>

            <div class="program-actions" @click.stop>
              <button type="button" class="action-btn" @click="viewProgram(program)">
                <Eye :size="16" />
                Visualizar
              </button>
              <button type="button" class="action-btn" @click="openDashboard(program)">
                <BarChart3 :size="16" />
                Dashboard
              </button>
              <button type="button" class="action-btn" @click="openPeople(program)">
                <Users :size="16" />
                Pessoas
              </button>
              <button type="button" class="action-btn" @click="openClassCourses(program)">
                <BookOpen :size="16" />
                Cursos
              </button>

              <div class="options-menu-container">
                <button type="button" class="action-btn" @click="toggleOptionsMenu(actionMenuId(program))">
                  <MoreVertical :size="16" />
                  Opções
                </button>

                <div v-if="showOptionsMenu === actionMenuId(program)" class="options-menu">
                  <button type="button" class="options-action" @click="editClass(program)">
                    <Edit2 :size="16" />
                    Editar turma
                  </button>
                  <button type="button" class="options-danger" @click="askDelete(program)">
                    <Trash2 :size="16" />
                    Excluir programa
                  </button>
                </div>
              </div>
            </div>
          </article>
        </div>

        <!-- Etapa 5: paginação -->
        <footer class="pagination-footer">
          <div class="pagination-text">
            Mostrando <strong>{{ rangeStart }}-{{ rangeEnd }}</strong> de
            <strong>{{ filteredPrograms.length }}</strong> turmas
          </div>

          <div class="pagination-actions">
            <button type="button" class="page-btn" :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">
              Anterior
            </button>

            <button
              v-for="page in pageNumbers"
              :key="page"
              type="button"
              class="page-btn number"
              :class="{ active: page === currentPage }"
              @click="goToPage(page)"
            >
              {{ page }}
            </button>

            <button
              type="button"
              class="page-btn"
              :disabled="currentPage === totalPages"
              @click="goToPage(currentPage + 1)"
            >
              Próximo
            </button>
          </div>
        </footer>
      </section>
    </div>

    <!-- Etapa 7: modal Nova Turma -->
    <div v-if="showNewClassModal" class="modal-overlay" @click.self="closeNewClassModal">
      <div class="modal-content modal-large">
        <div class="modal-header">
          <div>
            <h3>Criar nova turma</h3>
            <p class="modal-subtitle">
              Selecione um programa existente e preencha os dados da nova turma.
            </p>
          </div>
          <button type="button" class="btn-close-modal" @click="closeNewClassModal">
            <X :size="20" />
          </button>
        </div>

        <div class="modal-body">
          <div class="modal-grid">
            <div class="modal-sidebar">
              <h4>Programas disponíveis</h4>
              <button
                v-for="template in classTemplates"
                :key="template.id"
                type="button"
                class="template-card"
                :class="{ active: selectedTemplateId === template.id }"
                @click="selectedTemplateId = template.id"
              >
                <strong>{{ template.nome }}</strong>
                <span>{{ template.parceiro }}</span>
                <small>{{ template.turmas }} turma(s)</small>
              </button>
            </div>

            <div class="modal-details">
              <template v-if="selectedTemplate">
                <h4>Regras herdadas do programa</h4>
                <div class="details-panel">
                  <div class="details-grid">
                    <div>
                      <span>Nome do programa</span>
                      <strong>{{ selectedTemplate.nome }}</strong>
                    </div>
                    <div>
                      <span>Parceiro</span>
                      <strong>{{ selectedTemplate.parceiro }}</strong>
                    </div>
                    <div>
                      <span>Modalidade do nivelamento</span>
                      <strong>{{ selectedTemplate.modalidadeNivelamento }}</strong>
                    </div>
                    <div>
                      <span>Duração do nivelamento</span>
                      <strong>{{ selectedTemplate.duracaoNivelamento }}</strong>
                    </div>
                    <div>
                      <span>Duração da imersão</span>
                      <strong>{{ selectedTemplate.duracaoImersao }}</strong>
                    </div>
                    <div>
                      <span>Carga horária da imersão</span>
                      <strong>{{ selectedTemplate.cargaHorariaImersao }} horas</strong>
                    </div>
                    <div>
                      <span>Vagas de nivelamento</span>
                      <strong>{{ selectedTemplate.vagasNivelamento }}</strong>
                    </div>
                    <div>
                      <span>Vagas de imersão</span>
                      <strong>{{ selectedTemplate.vagasImersao }}</strong>
                    </div>
                  </div>

                  <div class="details-section">
                    <span>Critérios de cotas</span>
                    <div class="tag-list">
                      <span v-for="item in selectedTemplate.criteriosCotas" :key="item" class="tag-pill">
                        {{ item }}
                      </span>
                    </div>
                  </div>

                  <div class="details-section">
                    <span>Cursos obrigatórios</span>
                    <p>{{ selectedTemplate.cursosObrigatorios.join(', ') }}</p>
                  </div>
                </div>
              </template>

              <template v-else>
                <div class="empty-detail">
                  <Target :size="36" />
                  <p>Selecione um programa para ver as regras herdadas.</p>
                </div>
              </template>

              <h4>Dados da nova turma</h4>
              <div class="form-stack">
                <div class="form-section">
                  <h5>Identificação da turma</h5>
                  <div class="form-grid two-cols">
                    <label>
                      Nome da turma *
                      <input v-model="newClassForm.nomeTurma" type="text" placeholder="Ex: Turma UFAL 2026.1" />
                    </label>
                    <label>
                      Localidade *
                      <input v-model="newClassForm.localidade" type="text" placeholder="Ex: Maceió - AL" />
                    </label>
                    <label class="span-2">
                      Instituição parceira/local
                      <input v-model="newClassForm.instituicaoNome" type="text" placeholder="Ex: UFAL" />
                    </label>
                    <label>
                      Vagas
                      <input v-model="newClassForm.qtdVagas" type="number" min="1" placeholder="Ex: 20" />
                    </label>
                  </div>
                </div>

                <div class="form-section">
                  <h5>Inscrição e seleção</h5>
                  <div class="form-grid two-cols">
                    <label>
                      Publicação do edital
                      <input v-model="newClassForm.publicationDate" type="date" />
                    </label>
                    <label>
                      Inscrição: data inicial
                      <input v-model="newClassForm.applicationStartDate" type="date" />
                    </label>
                    <label>
                      Inscrição: data final
                      <input v-model="newClassForm.applicationEndDate" type="date" />
                    </label>
                    <label>
                      Divulgação selecionados nivelamento
                      <input v-model="newClassForm.levelingSelectionAnnouncementDate" type="date" />
                    </label>
                  </div>
                </div>

                <div class="form-section">
                  <h5>Nivelamento</h5>
                  <div class="form-grid two-cols">
                    <label>
                      Confirmação: data inicial
                      <input v-model="newClassForm.levelingConfirmationStartDate" type="date" />
                    </label>
                    <label>
                      Confirmação: data final
                      <input v-model="newClassForm.levelingConfirmationEndDate" type="date" />
                    </label>
                    <label>
                      Início do nivelamento
                      <input v-model="newClassForm.levelingStartDate" type="date" />
                    </label>
                    <label>
                      Fim do nivelamento
                      <input v-model="newClassForm.levelingEndDate" type="date" />
                    </label>
                    <label>
                      Prova final
                      <input v-model="newClassForm.levelingFinalExamDate" type="date" />
                    </label>
                    <label>
                      Divulgação para imersão
                      <input v-model="newClassForm.immersionSelectionAnnouncementDate" type="date" />
                    </label>
                  </div>
                </div>

                <div class="form-section">
                  <h5>Imersão</h5>
                  <div class="form-grid two-cols">
                    <label>
                      Confirmação: data inicial
                      <input v-model="newClassForm.immersionConfirmationStartDate" type="date" />
                    </label>
                    <label>
                      Confirmação: data final
                      <input v-model="newClassForm.immersionConfirmationEndDate" type="date" />
                    </label>
                    <label>
                      Início da imersão
                      <input v-model="newClassForm.immersionStartDate" type="date" />
                    </label>
                    <label>
                      Fim da imersão
                      <input v-model="newClassForm.immersionEndDate" type="date" />
                    </label>
                  </div>
                </div>

                <div class="form-section">
                  <h5>Avaliações e encerramento</h5>
                  <div class="form-grid two-cols">
                    <label>
                      Avaliação parcial
                      <input v-model="newClassForm.partialEvaluationDate" type="date" />
                    </label>
                    <label>
                      Avaliação final
                      <input v-model="newClassForm.finalEvaluationDate" type="date" />
                    </label>
                    <label>
                      Emissão dos certificados
                      <input v-model="newClassForm.certificateIssueDate" type="date" />
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="footer-btn secondary" @click="closeNewClassModal">Cancelar</button>
          <button type="button" class="footer-btn primary" :disabled="creatingClass" @click="createClass">
            {{ creatingClass ? 'Criando...' : 'Criar turma' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Etapa 8: modal de exclusão -->
    <div v-if="programToDelete" class="modal-overlay" @click.self="closeDeleteModal">
      <div class="modal-content modal-sm">
        <div class="modal-header">
          <div>
            <h3>Excluir programa</h3>
            <p class="modal-subtitle">Essa ação não poderá ser desfeita.</p>
          </div>
          <button type="button" class="btn-close-modal" @click="closeDeleteModal">
            <X :size="20" />
          </button>
        </div>

        <div class="modal-body">
          <p class="delete-text">
            Deseja excluir <strong>{{ programToDelete.nome }} - Turma {{ programToDelete.turma }}</strong>?
          </p>
        </div>

        <div class="modal-footer">
          <button type="button" class="footer-btn secondary" @click="closeDeleteModal">Cancelar</button>
          <button type="button" class="footer-btn danger" :disabled="deletingProgram" @click="deleteProgram">
            {{ deletingProgram ? 'Excluindo...' : 'Excluir programa' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  AlertTriangle,
  BarChart3,
  Building2,
  Calendar,
  Award,
  BookOpen,
  CheckCircle,
  Clock,
  Edit2,
  Eye,
  MapPin,
  MoreVertical,
  Plus,
  Search,
  Target,
  Trash2,
  Users,
  X,
} from 'lucide-vue-next';
import { programService } from '@/services/programService';
import { classService } from '@/services/classService';

const router = useRouter();
const route = useRoute();

const loading = ref(true);
const errorMessage = ref('');
const overview = ref(null);
const classTemplates = ref([]);
const activeTab = ref('todos');
const showFilters = ref(false);
const searchInput = ref('');
const appliedSearch = ref('');
const selectedStatus = ref('');
const selectedStage = ref('');
const selectedYear = ref('');
const selectedLocality = ref('');
const currentPage = ref(1);
const pageSize = 4;
const showOptionsMenu = ref(null);
const showProgramSelector = ref(false);
const programSelectorSearch = ref('');
const selectedProgramId = ref('');
const showAllProgramsClasses = ref(true);
const allClasses = ref([]);
const selectedProgramClasses = ref([]);
const classCountsByProgram = ref({});
const programToDelete = ref(null);
const deletingProgram = ref(false);
const showNewClassModal = ref(false);
const creatingClass = ref(false);
const selectedTemplateId = ref(null);
const newClassForm = ref(createEmptyClassForm());

const stageTrack = ['Inscrição', 'Seleção', 'Nivelamento', 'Imersão', 'Encerrado'];

const statusLabels = {
  andamento: 'Em andamento',
  inscricao: 'Inscrição aberta',
  encerrado: 'Encerrado',
};

const statusClasses = {
  andamento: 'status-teal',
  inscricao: 'status-blue',
  encerrado: 'status-slate',
};

function createEmptyClassForm() {
  return {
    nomeTurma: '',
    localidade: '',
    instituicaoId: null,
    instituicaoNome: '',
    qtdVagas: '',
    publicationDate: '',
    applicationStartDate: '',
    applicationEndDate: '',
    levelingSelectionAnnouncementDate: '',
    levelingConfirmationStartDate: '',
    levelingConfirmationEndDate: '',
    levelingStartDate: '',
    levelingEndDate: '',
    levelingFinalExamDate: '',
    immersionSelectionAnnouncementDate: '',
    immersionConfirmationStartDate: '',
    immersionConfirmationEndDate: '',
    immersionStartDate: '',
    immersionEndDate: '',
    partialEvaluationDate: '',
    finalEvaluationDate: '',
    certificateIssueDate: '',
  };
}

function normalizeText(value) {
  return String(value ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase();
}

function parseYears(period) {
  const matches = String(period ?? '').match(/\d{4}/g) ?? [];
  return matches;
}

function normalizedStatus(status) {
  const value = normalizeText(status);
  if (value.includes('encerr')) return 'encerrado';
  if (value.includes('inscri') || value.includes('espera')) return 'inscricao';
  if (value.includes('andamento') || value.includes('ativo') || value.includes('active') || value.includes('progress')) return 'andamento';
  return value;
}

function hasClassLikeData(item) {
  const className = normalizeText(item?.turma);
  return className && className !== 'sem turma' && className !== '-';
}

function resolveClassId(item) {
  return item?.classId
    ?? item?.classid
    ?? item?.classID
    ?? item?.turmaId
    ?? item?.idTurma
    ?? null;
}

function resolveProgramId(item) {
  const programId = item?.programId
    ?? item?.program?.id
    ?? item?.idPrograma
    ?? item?.programid
    ?? null;

  return programId == null ? null : String(programId);
}

function mapClassToProgramListItem(classItem, fallbackProgram) {
  const programId = resolveProgramId(classItem) ?? String(fallbackProgram?.programId ?? '');
  const catalogProgram = programCatalog.value.find((item) => item.programId === programId) || null;
  const programName = classItem?.program?.name ?? fallbackProgram?.nome ?? catalogProgram?.nome ?? 'Programa';
  const executorName = fallbackProgram?.executor ?? catalogProgram?.executor ?? '-';
  const localityName = classItem?.locality ?? classItem?.location?.name ?? fallbackProgram?.location ?? '-';
  
  // Format period/dates: "DD/MM/YYYY - DD/MM/YYYY"
  const formatDate = (date) => {
    if (!date) return null;
    if (typeof date === 'string') {
      const d = new Date(date);
      if (isNaN(d.getTime())) return null;
      return d.toLocaleDateString('pt-BR');
    }
    if (date instanceof Date) {
      return date.toLocaleDateString('pt-BR');
    }
    return null;
  };
  
  const startDate = formatDate(classItem?.publishDate ?? classItem?.startDate ?? fallbackProgram?.startDate);
  const endDate = formatDate(classItem?.endDate ?? fallbackProgram?.endDate);
  const periodoText = startDate && endDate ? `${startDate} - ${endDate}` : (startDate || '-');

  return {
    programId: Number(programId || 0),
    classId: classItem?.id ?? classItem?.classId ?? null,
    nome: programName,
    turma: classItem?.code ?? classItem?.name ?? `Turma ${classItem?.id ?? '-'}`,
    parceiro: executorName,
    localidade: localityName,
    periodo: periodoText,
    status: classItem?.status || 'andamento',
    etapaAtual: classItem?.currentStage || 'Inscricao',
    inscricao: 0,
    selecao: 0,
    inscritos: 0,
    ativos: 0,
    nivelamento: 0,
    imersao: 0,
    projetos: 0,
    orientadores: 0,
    proximoMarco: '-',
    progresso: 0,
  };
}

function formatError(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback;
}

const allPrograms = computed(() => overview.value?.items ?? []);
const classesById = computed(() =>
  new Map((allClasses.value || []).map((item) => [Number(item.id), item]))
);

const programCatalog = computed(() => {
  const map = new Map();
  const overviewCounts = new Map();

  allPrograms.value.forEach((item) => {
    const key = String(item.programId ?? '');
    if (!key) return;

    if (!map.has(key)) {
      map.set(key, {
        programId: key,
        nome: item.nome ?? 'Programa sem nome',
        parceiro: item.parceiro ?? '-',
        totalTurmas: 0,
      });
    }

    if (hasClassLikeData(item) || resolveClassId(item) != null) {
      overviewCounts.set(key, (overviewCounts.get(key) ?? 0) + 1);
    }
  });

  map.forEach((item, key) => {
    const countFromService = Number(classCountsByProgram.value[key]);
    const countFromOverview = Number(overviewCounts.get(key) ?? 0);
    item.totalTurmas = Number.isFinite(countFromService) && countFromService >= 0
      ? countFromService
      : countFromOverview;
  });

  return [...map.values()].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
});

const filteredProgramCatalog = computed(() => {
  const term = programSelectorSearch.value.trim();
  if (!term) return programCatalog.value;

  const normalizedTerm = normalizeText(term);
  return programCatalog.value.filter((item) =>
    [item.nome, item.parceiro].some((field) => normalizeText(field).includes(normalizedTerm))
  );
});

const selectedProgram = computed(() => {
  return programCatalog.value.find((item) => item.programId === selectedProgramId.value) || null;
});

const classesBySelectedProgram = computed(() => {
  if (!selectedProgramId.value) {
    if (!showAllProgramsClasses.value) return [];

    if (allClasses.value.length > 0) {
      return allClasses.value.map((item) => mapClassToProgramListItem(item, null));
    }

    return allPrograms.value.filter((item) => hasClassLikeData(item) || resolveClassId(item) != null);
  }

  const fromOverview = allPrograms.value.filter((item) => {
    return String(item.programId) === selectedProgramId.value && (hasClassLikeData(item) || resolveClassId(item) != null);
  });

  if (fromOverview.length > 0) {
    return fromOverview;
  }

  if (selectedProgramClasses.value.length === 0) {
    return [];
  }

  return selectedProgramClasses.value.map((classItem) => mapClassToProgramListItem(classItem, selectedProgram.value));
});

function numberValue(value) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function parseDateValue(value) {
  if (!value) return null;
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

function classByProgram(program) {
  const classId = Number(program?.classId);
  if (!classId) return null;
  return classesById.value.get(classId) || null;
}

function hasStartedInscricao(program) {
  const classModel = classByProgram(program);
  const startDate = parseDateValue(classModel?.applicationStartDate);
  if (!startDate) return false;
  return startDate.getTime() <= Date.now();
}

function isStage(item, stageName) {
  return normalizeText(item?.etapaAtual) === normalizeText(stageName);
}

const summaryCards = computed(() => {
  const resumo = overview.value?.resumo ?? {};

  if (selectedProgramId.value) {
    const items = classesBySelectedProgram.value;
    return [
      { label: 'Total de turmas', value: items.length, icon: Award, iconColor: 'primary' },
      { label: 'Turmas ativas', value: items.filter((item) => normalizedStatus(item.status) === 'andamento').length, icon: CheckCircle, iconColor: 'success' },
      { label: 'Em espera', value: items.filter((item) => normalizedStatus(item.status) === 'inscricao').length, icon: Clock, iconColor: 'info' },
      { label: 'Em imersão', value: items.filter((item) => isStage(item, 'Imersão')).length, icon: Users, iconColor: 'purple' },
      { label: 'Em nivelamento', value: items.filter((item) => isStage(item, 'Nivelamento')).length, icon: Building2, iconColor: 'warning' },
      { label: 'Encerradas', value: items.filter((item) => normalizedStatus(item.status) === 'encerrado').length, icon: CheckCircle, iconColor: 'muted' },
    ];
  }

  const inscritosTotal = allPrograms.value.reduce((sum, item) => sum + numberValue(item.inscritos), 0);
  const ativosTotal = allPrograms.value.reduce((sum, item) => sum + numberValue(item.ativos), 0);

  return [
    { label: 'Total de programas', value: numberValue(resumo.totalProgramas), icon: Award, iconColor: 'primary' },
    { label: 'Programas ativos', value: numberValue(resumo.programasAtivos), icon: CheckCircle, iconColor: 'success' },
    { label: 'Total de turmas', value: allPrograms.value.length, icon: Users, iconColor: 'info' },
    { label: 'Inscritos nas turmas', value: inscritosTotal, icon: Clock, iconColor: 'purple' },
    { label: 'Ativos nas turmas', value: ativosTotal, icon: Building2, iconColor: 'warning' },
    { label: 'Encerrados', value: numberValue(resumo.encerrados), icon: CheckCircle, iconColor: 'muted' },
  ];
});

const tabs = computed(() => {
  const items = classesBySelectedProgram.value;
  const ativos = items.filter((item) => normalizedStatus(item.status) === 'andamento').length;
  const espera = items.filter((item) => normalizedStatus(item.status) === 'inscricao').length;
  const encerrados = items.filter((item) => normalizedStatus(item.status) === 'encerrado').length;

  return [
    { id: 'ativos', label: 'Turmas ativas', count: ativos },
    { id: 'espera', label: 'Em espera', count: espera },
    { id: 'todos', label: 'Todas as turmas', count: items.length },
    { id: 'encerrados', label: 'Encerradas', count: encerrados },
  ];
});

const stageOptions = computed(() => {
  const values = classesBySelectedProgram.value.map((item) => item.etapaAtual).filter(Boolean);
  return [...new Set(values)].sort((a, b) => a.localeCompare(b, 'pt-BR'));
});

const localityOptions = computed(() => {
  const values = classesBySelectedProgram.value.map((item) => item.localidade).filter(Boolean);
  return [...new Set(values)].sort((a, b) => a.localeCompare(b, 'pt-BR'));
});

const yearOptions = computed(() => {
  const years = new Set();
  classesBySelectedProgram.value.forEach((item) => {
    parseYears(item.periodo).forEach((year) => years.add(year));
  });
  return [...years].sort((a, b) => Number(b) - Number(a));
});

const selectedTemplate = computed(() => classTemplates.value.find((item) => item.id === selectedTemplateId.value) || null);

const activeFiltersCount = computed(() => {
  return [selectedStatus.value, selectedStage.value, selectedYear.value, selectedLocality.value].filter(Boolean).length;
});

const filteredPrograms = computed(() => {
  let items = [...classesBySelectedProgram.value];

  if (activeTab.value === 'ativos') {
    items = items.filter((item) => normalizedStatus(item.status) === 'andamento');
  } else if (activeTab.value === 'espera') {
    items = items.filter((item) => normalizedStatus(item.status) === 'inscricao');
  } else if (activeTab.value === 'encerrados') {
    items = items.filter((item) => normalizedStatus(item.status) === 'encerrado');
  }

  if (appliedSearch.value) {
    const term = normalizeText(appliedSearch.value);
    items = items.filter((item) =>
      [item.nome, item.turma, item.parceiro, item.localidade, item.periodo]
        .some((field) => normalizeText(field).includes(term))
    );
  }

  if (selectedStatus.value) {
    items = items.filter((item) => normalizedStatus(item.status) === selectedStatus.value);
  }

  if (selectedStage.value) {
    items = items.filter((item) => item.etapaAtual === selectedStage.value);
  }

  if (selectedYear.value) {
    items = items.filter((item) => parseYears(item.periodo).includes(String(selectedYear.value)));
  }

  if (selectedLocality.value) {
    const target = normalizeText(selectedLocality.value);
    items = items.filter((item) => normalizeText(item.localidade).includes(target));
  }

  return items;
});

const totalPages = computed(() => Math.max(1, Math.ceil(filteredPrograms.value.length / pageSize)));

const paginatedPrograms = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return filteredPrograms.value.slice(start, start + pageSize);
});

const rangeStart = computed(() => {
  if (filteredPrograms.value.length === 0) return 0;
  return (currentPage.value - 1) * pageSize + 1;
});

const rangeEnd = computed(() => {
  return Math.min(currentPage.value * pageSize, filteredPrograms.value.length);
});

const pageNumbers = computed(() => {
  const total = totalPages.value;
  if (total <= 3) {
    return Array.from({ length: total }, (_, index) => index + 1);
  }

  const start = Math.max(1, currentPage.value - 1);
  const end = Math.min(total, currentPage.value + 1);
  const adjustedStart = end === total ? Math.max(1, total - 2) : start;
  const adjustedEnd = start === 1 ? Math.min(total, 3) : end;

  return Array.from({ length: adjustedEnd - adjustedStart + 1 }, (_, index) => adjustedStart + index);
});

function stageCountFor(program, stageLabel) {
  const label = normalizeText(stageLabel);
  if (label.includes('inscri')) {
    const count = numberValue(program?.inscricao ?? program?.inscritos);
    return hasStartedInscricao(program) ? Math.max(count, 1) : count;
  }
  if (label.includes('sele')) return numberValue(program?.selecao);
  if (label.includes('nivel')) return numberValue(program?.nivelamento);
  if (label.includes('imers')) return numberValue(program?.imersao);
  if (label.includes('encer')) return normalizedStatus(program?.status) === 'encerrado' ? 1 : 0;
  return 0;
}

function stageClass(program, stageLabel) {
  return stageCountFor(program, stageLabel) > 0 ? 'done' : 'pending';
}

function stageConnectorActive(program, index) {
  const lastActiveIndex = Math.max(
    ...stageTrack.map((stage, idx) => (stageCountFor(program, stage) > 0 ? idx : -1))
  );
  return lastActiveIndex >= 0 && index < lastActiveIndex;
}

function statusLabel(status) {
  return statusLabels[status] || status;
}

function statusClass(status) {
  return statusClasses[status] || 'status-slate';
}

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
}

function applySearch() {
  appliedSearch.value = searchInput.value.trim();
  currentPage.value = 1;
}

function resetClassListingFilters() {
  activeTab.value = 'todos';
  showFilters.value = false;
  searchInput.value = '';
  appliedSearch.value = '';
  selectedStatus.value = '';
  selectedStage.value = '';
  selectedYear.value = '';
  selectedLocality.value = '';
}

function toggleProgramSelector() {
  showProgramSelector.value = !showProgramSelector.value;
}

async function selectProgram(programId) {
  selectedProgramId.value = String(programId);
  showAllProgramsClasses.value = false;
  resetClassListingFilters();
  await loadSelectedProgramClasses();
  showProgramSelector.value = false;
  currentPage.value = 1;
}

function selectAllPrograms() {
  selectedProgramId.value = '';
  showAllProgramsClasses.value = true;
  selectedProgramClasses.value = [];
  activeTab.value = 'todos';
  showFilters.value = false;
  searchInput.value = '';
  appliedSearch.value = '';
  selectedStatus.value = '';
  selectedStage.value = '';
  selectedYear.value = '';
  selectedLocality.value = '';
  showProgramSelector.value = false;
  currentPage.value = 1;
}

function viewProgram(program) {
  const classId = resolveClassId(program) || resolveClassIdByCode(program);
  if (classId != null) {
    router.push({
      name: 'ClassDetails',
      params: {
        programId: program.programId,
        classId,
      },
    });
    return;
  }

  router.push({ name: 'ProgramDetails', params: { id: program.programId } });
}

function resolveClassIdByCode(program) {
  const turma = normalizeText(program?.turma);
  if (!turma || selectedProgramClasses.value.length === 0) return null;
  const found = selectedProgramClasses.value.find((item) => normalizeText(item.code) === turma);
  return found?.id ?? null;
}

async function loadSelectedProgramClasses() {
  if (!selectedProgramId.value) {
    selectedProgramClasses.value = [];
    return;
  }

  try {
    const classes = await classService.getByProgramId(selectedProgramId.value);
    selectedProgramClasses.value = Array.isArray(classes) ? classes : [];
    classCountsByProgram.value = {
      ...classCountsByProgram.value,
      [selectedProgramId.value]: selectedProgramClasses.value.length,
    };
  } catch {
    selectedProgramClasses.value = [];
  }
}

function openDashboard(program) {
  const classId = resolveClassId(program) || resolveClassIdByCode(program);
  const resolvedProgramId = program?.programId ?? program?.idPrograma ?? null;
  const query = { programa: `${program.nome} - Turma ${program.turma}` };

  if (resolvedProgramId != null) {
    query.programaId = String(resolvedProgramId);
  }
  if (classId != null) {
    query.turmaId = String(classId);
  }

  router.push({ path: '/dashboard', query });
}

function openPeople(program) {
  const classId = resolveClassId(program) || resolveClassIdByCode(program);
  const resolvedProgramId = program?.programId ?? program?.idPrograma ?? null;
  const query = { programa: `${program.nome} - Turma ${program.turma}` };

  if (resolvedProgramId != null) {
    query.programaId = String(resolvedProgramId);
  }
  if (classId != null) {
    query.turmaId = String(classId);
  }

  router.push({ path: '/people', query });
}

function openClassCourses(program) {
  const classId = resolveClassId(program) || resolveClassIdByCode(program);
  if (classId == null) {
    errorMessage.value = 'Não foi possível identificar a turma para abrir os detalhes.';
    return;
  }

  router.push({
    name: 'ClassDetails',
    params: {
      programId: program.programId,
      classId,
    },
    query: {
      tab: 'etapas',
      subTab: 'nivelamento',
    },
  });
}

function editSelectedProgram() {
  if (!selectedProgramId.value) return;
  router.push({ path: '/programs/register', query: { edit: selectedProgramId.value } });
}

function editClass(program) {
  const classId = resolveClassId(program) || resolveClassIdByCode(program);
  if (classId == null) {
    errorMessage.value = 'Não foi possível identificar a turma para edição.';
    return;
  }

  showOptionsMenu.value = null;
  router.push({
    name: 'ClassDetails',
    params: {
      programId: program.programId,
      classId,
    },
  });
}

function goToProgramRegistration() {
  router.push({ path: '/programs/register' });
}

function actionMenuId(program) {
  return `${program.programId}-${resolveClassId(program) ?? program.turma ?? 'sem-turma'}`;
}

function toggleOptionsMenu(menuId) {
  showOptionsMenu.value = showOptionsMenu.value === menuId ? null : menuId;
}

function askDelete(program) {
  programToDelete.value = program;
  showOptionsMenu.value = null;
}

function closeDeleteModal() {
  programToDelete.value = null;
}

async function deleteProgram() {
  if (!programToDelete.value) return;

  deletingProgram.value = true;
  try {
    await programService.delete(programToDelete.value.programId);
    programToDelete.value = null;
    await loadData();
  } catch (error) {
    errorMessage.value = formatError(error, 'Erro ao excluir programa.');
  } finally {
    deletingProgram.value = false;
  }
}

function openNewClassModal() {
  if (!selectedTemplateId.value && classTemplates.value.length > 0) {
    selectedTemplateId.value = classTemplates.value[0].id;
  }

  newClassForm.value = createEmptyClassForm();
  showNewClassModal.value = true;
}

function closeNewClassModal() {
  showNewClassModal.value = false;
  creatingClass.value = false;
  newClassForm.value = createEmptyClassForm();
}

async function createClass() {
  if (!selectedTemplate.value) return;

  creatingClass.value = true;
  try {
    await programService.createClassFromProgram(selectedTemplate.value.id, {
      ...newClassForm.value,
      qtdVagas: newClassForm.value.qtdVagas === '' ? null : Number(newClassForm.value.qtdVagas),
    });
    closeNewClassModal();
    await loadData();
  } catch (error) {
    errorMessage.value = formatError(error, 'Erro ao criar turma.');
  } finally {
    creatingClass.value = false;
  }
}

async function loadData() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const [overviewData, templates, countByProgram, classesData] = await Promise.all([
      programService.getOverview(),
      programService.getClassTemplates().catch(() => []),
      classService.getCountByProgram().catch(() => ({})),
      classService.getAll().catch(() => []),
    ]);

    overview.value = overviewData;
    classTemplates.value = templates;
    classCountsByProgram.value = countByProgram && typeof countByProgram === 'object' ? countByProgram : {};
    allClasses.value = Array.isArray(classesData) ? classesData : [];
    if (selectedProgramId.value && !programCatalog.value.some((item) => item.programId === selectedProgramId.value)) {
      selectedProgramId.value = '';
      showAllProgramsClasses.value = true;
      selectedProgramClasses.value = [];
    }
    if (selectedProgramId.value) {
      await loadSelectedProgramClasses();
    }
    if (templates.length > 0 && !templates.some((template) => template.id === selectedTemplateId.value)) {
      selectedTemplateId.value = templates[0].id;
    }
    currentPage.value = 1;
  } catch (error) {
    errorMessage.value = formatError(error, 'Erro ao carregar programas.');
  } finally {
    loading.value = false;
  }
}

watch(
  () => [selectedProgramId.value, showAllProgramsClasses.value, activeTab.value, appliedSearch.value, selectedStatus.value, selectedStage.value, selectedYear.value, selectedLocality.value],
  () => {
    currentPage.value = 1;
  }
);

watch(showNewClassModal, (opened) => {
  if (opened && !selectedTemplateId.value && classTemplates.value.length > 0) {
    selectedTemplateId.value = classTemplates.value[0].id;
  }
});

function handleOutsideMenuClick(event) {
  const target = event.target;
  if (!(target instanceof HTMLElement)) return;

  if (showOptionsMenu.value !== null && !target.closest('.options-menu-container')) {
    showOptionsMenu.value = null;
  }

  if (showProgramSelector.value && !target.closest('.program-selector-row')) {
    showProgramSelector.value = false;
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleOutsideMenuClick);
  loadData().then(async () => {
    const queryProgramId = String(route.query.programId ?? '').trim();
    if (queryProgramId && programCatalog.value.some((item) => item.programId === queryProgramId)) {
      await selectProgram(queryProgramId);
    }
  });
});

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleOutsideMenuClick);
});
</script>

<style scoped>
.programs-page {
  min-height: 100%;
  background: #eef3f8;
  padding: 14px 16px 20px;
}

.programs-shell {
  max-width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header-card,
.table-card,
.stat-card {
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(13, 27, 42, 0.05);
}

.page-header-card {
  padding: 18px 18px 16px;
}

.page-header-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.1;
  color: #13233f;
  font-weight: 800;
}

.subtitle {
  margin: 6px 0 0;
  color: #6a7a90;
  font-size: 14px;
}

.header-content {
  min-width: min(760px, 100%);
}

.program-selector-row {
  margin-top: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.program-selector {
  position: relative;
}

.program-selector-btn {
  height: 40px;
  border-radius: 12px;
  border: 1px solid #d8e1eb;
  background: #fff;
  color: #13233f;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
}

.program-selector-btn:hover {
  background: #f8fafc;
  border-color: #cfd9e6;
}

.program-selector-btn.active {
  background: #ecfdf5;
  border-color: #b7ebcd;
  color: #0f766e;
}

.program-selector-menu {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  width: min(420px, 90vw);
  max-height: 320px;
  overflow: auto;
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 14px;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
  padding: 8px;
  z-index: 20;
}

.program-option-btn {
  width: 100%;
  text-align: left;
  border: 1px solid transparent;
  background: #fff;
  border-radius: 10px;
  padding: 10px 12px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.program-option-btn strong {
  color: #13233f;
}

.program-option-btn small {
  color: #6a7a90;
}

.program-option-btn:hover,
.program-option-btn.active {
  background: #ecfdf5;
  border-color: #b7ebcd;
}

.program-option-empty {
  color: #6a7a90;
  font-size: 13px;
  padding: 10px 12px;
}

.program-selector-search {
  position: relative;
  flex: 1;
  min-width: 240px;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.ghost-btn,
.primary-btn,
.primary-search-btn,
.filters-btn,
.action-btn,
.page-btn,
.footer-btn {
  height: 40px;
  border-radius: 12px;
  border: 1px solid #d1dbe8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 14px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
  font: inherit;
  font-weight: 700;
}

.ghost-btn {
  background: #fff;
  color: #13233f;
}

.primary-btn,
.primary-search-btn,
.footer-btn.primary {
  background: #14b8a6;
  color: #fff;
  border-color: #14b8a6;
  box-shadow: 0 8px 16px rgba(20, 184, 166, 0.2);
}

.ghost-btn:hover,
.primary-btn:hover,
.action-btn:hover,
.page-btn:hover,
.filters-btn:hover {
  transform: translateY(-1px);
}

.ghost-btn:hover {
  background: #f8fafc;
  border-color: #cfd9e6;
}

.primary-btn:hover {
  background: #0d9488;
  border-color: #0d9488;
  box-shadow: 0 12px 20px rgba(13, 148, 136, 0.24);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
  margin-top: 14px;
}

.stat-card {
  padding: 16px;
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.stat-label {
  font-size: 12px;
  font-weight: 500;
  color: #50619e;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #171f4a;
  line-height: 1.1;
}

.icon-primary {
  color: #6377ba;
}

.icon-success {
  color: #4caf50;
}

.icon-info {
  color: #2196f3;
}

.icon-warning {
  color: #ff9800;
}

.icon-purple {
  color: #9c27b0;
}

.icon-muted {
  color: #9aa4c0;
}

.table-card {
  padding: 0;
  overflow: hidden;
}

.tabs-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px 0;
  border-bottom: 1px solid #e2eaf2;
  overflow-x: auto;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 2px 14px;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  color: #5f728d;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.tab-item:hover {
  color: #2a3566;
}

.tab-item.active {
  color: #0f766e;
  border-bottom-color: #14b8a6;
}

.tab-count {
  background: #eef2f7;
  color: #8a98ab;
  border-radius: 999px;
  padding: 2px 7px;
  font-size: 11px;
  font-weight: 700;
}

.tab-item.active .tab-count {
  color: #14b8a6;
}

.filters-row {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 12px 14px 14px;
}

.filters-row .search-box {
  position: relative;
  flex: 1;
  min-width: 0;
  max-width: none !important;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #97a4b6;
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 40px;
  border: 1px solid #d9e2ec;
  border-radius: 12px;
  background: #fff;
  padding: 0 14px 0 44px;
  color: #13233f;
  outline: none;
}

.search-input:focus {
  border-color: #14b8a6;
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.10);
}

.filters-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.filters-button {
  height: 40px;
  border-radius: 12px;
  padding: 0 14px;
  border: 1px solid #d8e1eb;
  background: #fff;
  color: #13233f;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}

.filters-button:hover {
  background: #f8fafc;
  border-color: #cfd9e6;
}

.search-button {
  height: 40px;
  border-radius: 12px;
  padding: 0 18px;
  border: 1px solid #14b8a6;
  background: #14b8a6;
  color: #fff;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.search-button:hover {
  background: #0d9488;
  border-color: #0d9488;
  box-shadow: 0 10px 18px rgba(13, 148, 136, 0.2);
}

.filters-badge {
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 999px;
  background: #d6f3ec;
  color: #14a6a6;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  padding: 0 14px 14px;
}

.advanced-filters {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 14px 14px;
}

.advanced-filters .filter-select {
  flex: 1 1 0;
  min-width: 0;
}

.filter-select {
  height: 40px;
  border: 1px solid #e0e8f1;
  border-radius: 12px;
  padding: 0 12px;
  background: #fff;
  color: #13233f;
}

.state-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 24px 18px;
  color: #41546e;
}

.state-row-error {
  color: #b42318;
}

.state-row-empty {
  color: #41546e;
}

.state-row-empty strong {
  display: block;
  color: #13233f;
  margin-bottom: 3px;
}

.spinner {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  border: 3px solid rgba(20, 184, 166, 0.2);
  border-top-color: #14b8a6;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.program-row {
  width: 100%;
  display: flex;
  gap: 18px;
  padding: 20px;
  border-bottom: 1px solid #e6edf5;
  cursor: pointer;
}

.program-row:hover {
  background: #f9fbfd;
}

.program-row-active-tab .program-title-line h3 {
  font-size: 24px;
  font-weight: 800;
}

.program-row-active-tab .class-chip {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 8px;
}

.program-main {
  flex: 1;
  min-width: 0;
}

.program-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.program-title-line {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.program-title-line h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #13233f;
}

.class-chip,
.status-chip {
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  padding: 6px 10px;
  border: 1px solid transparent;
}

.class-chip {
  background: #f1f8ff;
  color: #1d6fb8;
  border-color: #d2e7f8;
}

.status-chip.status-teal {
  background: #ecfdf5;
  color: #047857;
  border-color: #a7f3d0;
}

.status-chip.status-blue {
  background: #eff6ff;
  color: #1d4ed8;
  border-color: #bfdbfe;
}

.status-chip.status-slate {
  background: #f1f5f9;
  color: #475569;
  border-color: #cbd5e1;
}

.program-meta {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  margin-top: 8px;
  color: #50619e;
  font-size: 13px;
}

.program-meta span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.stage-track {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 16px 0 12px;
  flex-wrap: wrap;
}

.stage-step {
  display: flex;
  align-items: center;
}

.stage-pill {
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid #d8e1ec;
  font-size: 12px;
  font-weight: 600;
}

.stage-pill.current {
  background: #dff7f4;
  border-color: #9be1d8;
  color: #0f766e;
}

.stage-pill.done {
  background: #ecfdf5;
  border-color: #b7ebcd;
  color: #059669;
}

.stage-pill.pending {
  background: #f8fafc;
  color: #64748b;
}

.stage-connector {
  width: 24px;
  height: 2px;
  margin: 0 4px;
  background: #d8e1ec;
}

.stage-connector.active {
  background: #10b981;
}

.progress-wrap {
  margin-bottom: 14px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e2e8f0;
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #14b8a6, #2dd4bf);
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.metric-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px;
}

.metric-box span {
  display: block;
  color: #6b7280;
  font-size: 12px;
  margin-bottom: 4px;
}

.metric-box strong {
  color: #13233f;
  font-size: 14px;
}

.metric-box strong.accent {
  color: #0f766e;
}

.milestone {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #475569;
  font-size: 13px;
}

.milestone strong {
  color: #b45309;
}

.program-actions {
  width: 172px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  position: relative;
}

.action-btn {
  width: 100%;
  background: #fff;
  color: #13233f;
  justify-content: flex-start;
}

.options-menu-container {
  position: relative;
}

.options-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 6px);
  width: 200px;
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 14px;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
  padding: 8px;
  z-index: 10;
}

.options-action,
.options-danger {
  width: 100%;
  border: none;
  background: #fff;
  color: #13233f;
  padding: 10px 12px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.options-action:hover {
  background: #f8fafc;
}

.options-danger {
  color: #b42318;
}

.options-danger:hover {
  background: #fef2f2;
}

.pagination-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #e6edf5;
}

.pagination-text {
  color: #50619e;
  font-size: 14px;
}

.pagination-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.page-btn {
  background: #fff;
  color: #13233f;
}

.page-btn.number.active {
  background: #ecfdf5;
  color: #0f766e;
  border-color: #9be1d8;
}

.page-btn:disabled,
.footer-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.54);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
  padding: 20px;
}

.modal-content {
  width: min(1180px, 100%);
  max-height: 90vh;
  overflow: hidden;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.22);
  display: flex;
  flex-direction: column;
}

.modal-large {
  max-width: 1180px;
}

.modal-sm {
  max-width: 540px;
}

.modal-header,
.modal-footer {
  padding: 18px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.modal-header {
  border-bottom: 1px solid #e6edf5;
}

.modal-footer {
  border-top: 1px solid #e6edf5;
  justify-content: flex-end;
}

.modal-header h3 {
  margin: 0;
  color: #13233f;
  font-size: 20px;
}

.modal-subtitle {
  margin: 6px 0 0;
  color: #6a7a90;
  font-size: 14px;
}

.btn-close-modal {
  border: none;
  background: #f1f5f9;
  color: #334155;
  border-radius: 10px;
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
  overflow: auto;
}

.modal-grid {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 18px;
}

.modal-sidebar {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.modal-sidebar h4,
.modal-details h4 {
  margin: 0 0 10px;
  color: #13233f;
}

.template-card {
  text-align: left;
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 14px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  cursor: pointer;
}

.template-card.active {
  border-color: #14b8a6;
  background: #ecfdf5;
}

.template-card strong {
  color: #13233f;
}

.template-card span,
.template-card small {
  color: #6a7a90;
}

.modal-details {
  min-width: 0;
}

.details-panel {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 18px;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.details-grid span,
.details-section span {
  display: block;
  color: #6b7280;
  font-size: 12px;
  margin-bottom: 4px;
}

.details-grid strong,
.details-section p {
  color: #13233f;
  font-size: 14px;
}

.details-section {
  margin-top: 14px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-pill {
  background: #f3e8ff;
  color: #7e22ce;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 600;
}

.empty-detail {
  min-height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #6a7a90;
  border: 1px dashed #d7dfea;
  border-radius: 16px;
  margin-bottom: 18px;
}

.form-stack {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-section {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px;
}

.form-section h5 {
  margin: 0 0 12px;
  color: #334155;
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 0.04em;
}

.form-grid {
  display: grid;
  gap: 12px;
}

.form-grid.two-cols {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.form-grid label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #334155;
  font-size: 13px;
  font-weight: 600;
}

.form-grid input {
  height: 40px;
  border: 1px solid #cfd7e6;
  border-radius: 12px;
  padding: 0 12px;
}

.form-grid .span-2 {
  grid-column: span 2;
}

.delete-text {
  margin: 0;
  color: #334155;
}

.footer-btn.secondary {
  background: #fff;
  color: #13233f;
}

.footer-btn.danger {
  background: #dc2626;
  color: #fff;
  border-color: #dc2626;
}

@media (max-width: 1200px) {
  .stats-grid,
  .metrics-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .header-content {
    min-width: 100%;
  }

  .modal-grid {
    grid-template-columns: 1fr;
  }

  .program-row {
    flex-direction: column;
  }

  .program-actions {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .programs-page {
    padding: 12px;
  }

  h1 {
    font-size: 28px;
  }

  .stats-grid,
  .metrics-grid,
  .filters-grid,
  .form-grid.two-cols,
  .details-grid {
    grid-template-columns: 1fr;
  }

  .filters-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filters-row .search-box,
  .filters-actions {
    width: 100%;
  }

  .advanced-filters {
    flex-direction: column;
    align-items: stretch;
  }

  .filters-actions {
    width: 100%;
  }

  .program-selector,
  .program-selector-btn,
  .program-selector-search {
    width: 100%;
  }

  .filters-button,
  .search-button {
    flex: 1;
  }

  .form-grid .span-2 {
    grid-column: auto;
  }

  .program-actions {
    grid-template-columns: 1fr;
  }

  .pagination-footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
