<template>
  <div class="course-details-view">

    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <span>Carregando detalhes do curso...</span>
    </div>

    <template v-else>
      <div v-if="loadError" class="error-state">
        {{ loadError }}
      </div>

      <!-- ===== HEADER ===== -->
      <div class="page-header">
        <button class="btn-back" @click="goBack">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
               fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12"/>
            <polyline points="12 19 5 12 12 5"/>
          </svg>
          Voltar
        </button>

        <div class="header-info">
          <h1 class="page-title">{{ details.courseName || 'Carregando...' }}</h1>
          <span class="badge-required">Obrigatório</span>
          <span v-if="details.knowledgeArea" class="badge-area">{{ details.knowledgeArea }}</span>
        </div>

        <button class="btn-alert" @click="showAlertModal = true">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
               fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
          </svg>
          Enviar Alerta para Pendentes
        </button>
      </div>

      <!-- ===== OVERVIEW ROW ===== -->
      <div class="overview-row">

        <!-- Donut Chart -->
        <div class="donut-card">
          <h3 class="card-title">Distribuição de Status</h3>
          <div class="donut-wrapper">
            <svg viewBox="0 0 120 120" class="donut-svg">
              <!-- Segmento: Não Iniciado -->
              <circle
                class="donut-segment not-started"
                cx="60" cy="60" r="45"
                :stroke-dasharray="`${donut.notStarted} ${donut.rest1}`"
                :stroke-dashoffset="donut.offset0"
              />
              <!-- Segmento: Em Andamento -->
              <circle
                class="donut-segment in-progress"
                cx="60" cy="60" r="45"
                :stroke-dasharray="`${donut.inProgress} ${donut.rest2}`"
                :stroke-dashoffset="donut.offset1"
              />
              <!-- Segmento: Concluído -->
              <circle
                class="donut-segment completed"
                cx="60" cy="60" r="45"
                :stroke-dasharray="`${donut.completed} ${donut.rest3}`"
                :stroke-dashoffset="donut.offset2"
              />
              <!-- Texto central -->
              <text x="60" y="55" class="donut-pct">{{ details.averageCompletion || 0 }}%</text>
              <text x="60" y="70" class="donut-label">conclusão</text>
            </svg>
            <div class="donut-legend">
              <div class="legend-row">
                <span class="dot not-started"></span>
                <span class="legend-text">Não Iniciado</span>
                <span class="legend-count">{{ details.notStarted || 0 }}</span>
              </div>
              <div class="legend-row">
                <span class="dot in-progress"></span>
                <span class="legend-text">Em Andamento</span>
                <span class="legend-count">{{ details.inProgress || 0 }}</span>
              </div>
              <div class="legend-row">
                <span class="dot completed"></span>
                <span class="legend-text">Concluídos</span>
                <span class="legend-count">{{ details.completed || 0 }}</span>
              </div>
            </div>
          </div>

          <!-- Barra de progresso geral -->
          <div class="overall-progress">
            <div class="progress-label-row">
              <span class="progress-label-text">Progresso Geral da Turma</span>
              <span class="progress-label-pct">{{ details.averageCompletion || 0 }}%</span>
            </div>
            <div class="progress-track">
              <div class="progress-fill" :style="{ width: (details.averageCompletion || 0) + '%', background: progressColor(details.averageCompletion) }"></div>
            </div>
          </div>
        </div>

        <!-- Summary Cards -->
        <div class="summary-cards">
          <div class="summary-card not-started-card">
            <div class="summary-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24"
                   fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
            </div>
            <div class="summary-data">
              <span class="summary-number">{{ details.notStarted || 0 }}</span>
              <span class="summary-label">Não Iniciados</span>
            </div>
          </div>

          <div class="summary-card in-progress-card">
            <div class="summary-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24"
                   fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="23 4 23 10 17 10"/>
                <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
              </svg>
            </div>
            <div class="summary-data">
              <span class="summary-number">{{ details.inProgress || 0 }}</span>
              <span class="summary-label">Em Andamento</span>
            </div>
          </div>

          <div class="summary-card completed-card">
            <div class="summary-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24"
                   fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
            <div class="summary-data">
              <span class="summary-number">{{ details.completed || 0 }}</span>
              <span class="summary-label">Concluídos</span>
            </div>
          </div>

          <div class="summary-card total-card">
            <div class="summary-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24"
                   fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </div>
            <div class="summary-data">
              <span class="summary-number">{{ details.totalStudents || 0 }}</span>
              <span class="summary-label">Total de Alunos</span>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== TABELA ===== -->
      <div class="table-card">
        <div class="table-header-row">
          <h3 class="card-title">Progressão por Aluno</h3>
          <div class="table-filters">
            <!-- Busca por nome -->
            <div class="search-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24"
                   fill="none" stroke="#888" stroke-width="2" class="search-icon">
                <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              <input v-model="searchTerm" type="text" placeholder="Buscar aluno..." class="search-input" />
            </div>

            <!-- Filtro de status -->
            <select v-model="filterStatus" class="filter-select">
              <option value="">Todos</option>
              <option value="não iniciado">Não Iniciado</option>
              <option value="em andamento">Em Andamento</option>
              <option value="concluído">Concluído</option>
            </select>
          </div>
        </div>

        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th @click="sortBy('studentName')" class="sortable">
                  Aluno
                  <span class="sort-arrow" :class="{ active: sortKey === 'studentName' }">
                    {{ sortKey === 'studentName' && sortDesc ? '↓' : '↑' }}
                  </span>
                </th>
                <th @click="sortBy('status')" class="sortable">
                  Status
                  <span class="sort-arrow" :class="{ active: sortKey === 'status' }">
                    {{ sortKey === 'status' && sortDesc ? '↓' : '↑' }}
                  </span>
                </th>
                <th @click="sortBy('completionPercentage')" class="sortable">
                  Progresso
                  <span class="sort-arrow" :class="{ active: sortKey === 'completionPercentage' }">
                    {{ sortKey === 'completionPercentage' && sortDesc ? '↓' : '↑' }}
                  </span>
                </th>
                <th @click="sortBy('lastAccess')" class="sortable">
                  Último Acesso
                  <span class="sort-arrow" :class="{ active: sortKey === 'lastAccess' }">
                    {{ sortKey === 'lastAccess' && sortDesc ? '↓' : '↑' }}
                  </span>
                </th>
                <th @click="sortBy('grade')" class="sortable">
                  Nota
                  <span class="sort-arrow" :class="{ active: sortKey === 'grade' }">
                    {{ sortKey === 'grade' && sortDesc ? '↓' : '↑' }}
                  </span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="filteredStudents.length === 0">
                <td colspan="5" class="empty-row">Nenhum aluno encontrado.</td>
              </tr>
              <tr v-for="student in filteredStudents" :key="student.peopleId" class="data-row">
                <!-- Nome -->
                <td class="td-name">
                  <div class="student-avatar">{{ initials(student.studentName) }}</div>
                  <div class="student-info">
                    <span class="student-name">{{ student.studentName }}</span>
                    <span class="student-email">{{ student.studentEmail }}</span>
                  </div>
                </td>

                <!-- Status -->
                <td>
                  <span class="status-badge" :class="statusClass(student.status)">
                    {{ student.status || 'não iniciado' }}
                  </span>
                </td>

                <!-- Progresso (mini barra) -->
                <td class="td-progress">
                  <div class="mini-progress-wrapper">
                    <div class="mini-track">
                      <div
                        class="mini-fill"
                        :style="{ width: student.completionPercentage + '%', background: progressColor(student.completionPercentage) }"
                      ></div>
                    </div>
                    <span class="mini-pct">{{ Math.round(student.completionPercentage) }}%</span>
                  </div>
                </td>

                <!-- Último Acesso com badge de urgência -->
                <td class="td-access">
                  <span v-if="!student.lastAccess" class="access-never">Nunca acessou</span>
                  <template v-else>
                    <span class="access-date">{{ formatDate(student.lastAccess) }}</span>
                    <span v-if="daysSince(student.lastAccess) > 7" class="badge-urgent">
                      {{ daysSince(student.lastAccess) }}d sem acesso
                    </span>
                  </template>
                </td>

                <!-- Nota -->
                <td>
                  <span v-if="student.grade !== null && student.grade !== undefined"
                        class="grade-badge" :class="gradeClass(student.grade)">
                    {{ student.grade.toFixed(1) }}
                  </span>
                  <span v-else class="grade-none">—</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>

    <!-- ===== MODAL DE ALERTA ===== -->
    <div v-if="showAlertModal" class="modal-overlay" @click.self="showAlertModal = false">
      <div class="modal-box">
        <div class="modal-header">
          <h3>Enviar Alerta para Alunos Pendentes</h3>
          <button class="modal-close" @click="showAlertModal = false">×</button>
        </div>
        <div class="modal-body">
          <p class="modal-desc">
            Serão notificados <strong>{{ pendingCount }}</strong> alunos com status
            <em>Não Iniciado</em> ou <em>Em Andamento</em>.
          </p>

          <label class="form-label">Assunto</label>
          <input v-model="alertSubject" type="text" class="form-input" placeholder="Ex: Atenção: curso pendente" />

          <label class="form-label">Mensagem</label>
          <textarea v-model="alertMessage" class="form-textarea" rows="5"
            placeholder="Ex: Você ainda não concluiu o curso. Por favor, acesse a plataforma e continue seus estudos.">
          </textarea>

          <div v-if="alertError" class="alert-error">{{ alertError }}</div>
          <div v-if="alertSuccess" class="alert-success">{{ alertSuccess }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showAlertModal = false">Cancelar</button>
          <button class="btn-send" :disabled="sendingAlert" @click="sendAlert">
            <span v-if="sendingAlert">Enviando...</span>
            <span v-else>Enviar Alerta</span>
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { courseService } from '@/services/courseService';

export default {
  name: 'CourseDetailsView',
  setup() {
    const route  = useRoute();
    const router = useRouter();

    const courseId  = computed(() => route.params.courseId);
    const classId   = computed(() => route.params.classId);
    const programId = computed(() => route.params.programId);

    const loading = ref(true);
    const details = ref({});
    const loadError = ref('');

    // Tabela
    const searchTerm   = ref('');
    const filterStatus = ref('');
    const sortKey      = ref('studentName');
    const sortDesc     = ref(false);

    // Modal de alerta
    const showAlertModal = ref(false);
    const alertSubject   = ref('Lembrete: Curso pendente no BRISA');
    const alertMessage   = ref('');
    const sendingAlert   = ref(false);
    const alertError     = ref(null);
    const alertSuccess   = ref(null);

    // ── Computed: donut chart ──────────────────────────────────────────────
    const donut = computed(() => {
      const circ  = 2 * Math.PI * 45; // ≈ 282.74
      const total = details.value.totalStudents || 1;
      const ns    = details.value.notStarted || 0;
      const ip    = details.value.inProgress || 0;
      const co    = details.value.completed  || 0;

      const arcNS = (ns / total) * circ;
      const arcIP = (ip / total) * circ;
      const arcCO = (co / total) * circ;

      // stroke-dashoffset:
      // - começa no topo: offset = circ/4
      // - cada segmento seguinte subtrai o arco anterior
      // - usamos ((valor % circ) + circ) % circ para garantir sempre positivo
      const wrap = (v) => ((v % circ) + circ) % circ;
      const startOffset = circ / 4;

      return {
        notStarted: arcNS,
        rest1:      circ - arcNS,
        inProgress: arcIP,
        rest2:      circ - arcIP,
        completed:  arcCO,
        rest3:      circ - arcCO,
        offset0: wrap(startOffset),
        offset1: wrap(startOffset - arcNS),
        offset2: wrap(startOffset - arcNS - arcIP),
      };
    });

    // ── Computed: estudantes filtrados / ordenados ─────────────────────────
    const filteredStudents = computed(() => {
      let list = (details.value.students || []);

      if (searchTerm.value) {
        const term = searchTerm.value.toLowerCase();
        list = list.filter(s => s.studentName?.toLowerCase().includes(term));
      }
      if (filterStatus.value) {
        list = list.filter(s => s.status === filterStatus.value);
      }

      list = [...list].sort((a, b) => {
        const va = a[sortKey.value] ?? '';
        const vb = b[sortKey.value] ?? '';
        if (va < vb) return sortDesc.value ? 1 : -1;
        if (va > vb) return sortDesc.value ? -1 : 1;
        return 0;
      });

      return list;
    });

    const pendingCount = computed(() =>
      (details.value.students || []).filter(
        s => s.status === 'não iniciado' || s.status === 'em andamento'
      ).length
    );

    // ── Helpers ───────────────────────────────────────────────────────────
    const progressColor = (pct) => {
      if (pct >= 80) return '#27ae60';
      if (pct >= 40) return '#f39c12';
      return '#e74c3c';
    };

    const statusClass = (status) => {
      if (status === 'concluído')    return 'status-completed';
      if (status === 'em andamento') return 'status-inprogress';
      return 'status-notstarted';
    };

    const gradeClass = (grade) => {
      if (grade >= 7) return 'grade-good';
      if (grade >= 5) return 'grade-mid';
      return 'grade-bad';
    };

    const initials = (name) => {
      if (!name) return '?';
      return name.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
    };

    const formatDate = (dateStr) => {
      if (!dateStr) return '—';
      return new Date(dateStr).toLocaleDateString('pt-BR');
    };

    const daysSince = (dateStr) => {
      if (!dateStr) return 999;
      const diff = Date.now() - new Date(dateStr).getTime();
      return Math.floor(diff / (1000 * 60 * 60 * 24));
    };

    const sortBy = (key) => {
      if (sortKey.value === key) {
        sortDesc.value = !sortDesc.value;
      } else {
        sortKey.value  = key;
        sortDesc.value = false;
      }
    };

    // ── Demo data (fallback) ──────────────────────────────────────────────
    const demoStudents = () => [
      { peopleId: 1, studentName: 'Ana Paula Silva',   studentEmail: 'ana@example.com',   status: 'concluído',    completionPercentage: 100, lastAccess: '2025-06-01', grade: 9.5 },
      { peopleId: 2, studentName: 'Bruno Costa',       studentEmail: 'bruno@example.com', status: 'em andamento', completionPercentage: 62,  lastAccess: '2025-06-10', grade: 7.2 },
      { peopleId: 3, studentName: 'Carla Mendes',      studentEmail: 'carla@example.com', status: 'não iniciado', completionPercentage: 0,   lastAccess: null,         grade: null },
      { peopleId: 4, studentName: 'Diego Ferreira',    studentEmail: 'diego@example.com', status: 'em andamento', completionPercentage: 38,  lastAccess: '2025-05-20', grade: 6.0 },
      { peopleId: 5, studentName: 'Eduarda Lima',      studentEmail: 'edu@example.com',   status: 'concluído',    completionPercentage: 100, lastAccess: '2025-06-12', grade: 8.8 },
      { peopleId: 6, studentName: 'Felipe Santos',     studentEmail: 'felipe@example.com',status: 'não iniciado', completionPercentage: 0,   lastAccess: '2025-05-28', grade: null },
    ];

    // ── Carregar dados ────────────────────────────────────────────────────
    const loadData = async () => {
      loading.value = true;
      loadError.value = '';
      try {
        const data = await courseService.getCourseDetails(courseId.value, classId.value);
        details.value = data || {};
      } catch (err) {
        console.error('Erro ao carregar detalhes do curso:', err);
        loadError.value = err.response?.data?.message || err.message || 'Nao foi possivel carregar os detalhes reais do curso.';
        details.value = {
          courseName: '',
          knowledgeArea: '',
          workload: 0,
          totalStudents: 0,
          notStarted: 0,
          inProgress: 0,
          completed: 0,
          averageCompletion: 0,
          students: []
        };
      } finally {
        loading.value = false;
      }
    };

    // ── Enviar alerta ─────────────────────────────────────────────────────
    const sendAlert = async () => {
      if (!alertSubject.value.trim() || !alertMessage.value.trim()) {
        alertError.value = 'Preencha o assunto e a mensagem.';
        return;
      }
      sendingAlert.value = true;
      alertError.value   = null;
      alertSuccess.value = null;
      try {
        await courseService.sendAlert(courseId.value, classId.value, {
          subject: alertSubject.value,
          message: alertMessage.value,
        });
        alertSuccess.value = `Alerta enviado com sucesso para ${pendingCount.value} aluno(s)!`;
        setTimeout(() => { showAlertModal.value = false; alertSuccess.value = null; }, 2500);
      } catch (err) {
        alertError.value = 'Erro ao enviar alerta: ' + (err.response?.data?.message || err.message);
      } finally {
        sendingAlert.value = false;
      }
    };

    const goBack = () => {
        router.back();
    };

    onMounted(loadData);

    return {
      loading, details, donut, loadError,
      searchTerm, filterStatus, sortKey, sortDesc,
      filteredStudents, pendingCount,
      showAlertModal, alertSubject, alertMessage, sendingAlert, alertError, alertSuccess,
      progressColor, statusClass, gradeClass, initials,
      formatDate, daysSince, sortBy, sendAlert, goBack,
    };
  }
};
</script>

<style scoped>
/* ── Base ──────────────────────────────────────────────────────────────── */
.course-details-view {
  padding: 28px 32px;
  max-width: 1140px;
  margin: 0 auto;
  background: #eaf1fb;
  min-height: 100vh;
}

/* ── Loading ───────────────────────────────────────────────────────────── */
.loading-state {
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  padding: 80px; gap: 16px; color: #666;
}
.error-state {
  margin-bottom: 18px;
  padding: 12px 14px;
  border: 1px solid #f3c2ba;
  border-radius: 12px;
  background: #fff4f2;
  color: #b42318;
  font-size: 14px;
  line-height: 1.5;
}
.spinner {
  width: 40px; height: 40px;
  border: 3px solid #dde6f0;
  border-top-color: #1F285F;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Header ────────────────────────────────────────────────────────────── */
.page-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}
.btn-back {
  display: flex; align-items: center; gap: 6px;
  background: white; border: 1.5px solid #dde6f0;
  border-radius: 10px; padding: 8px 14px;
  font-size: 13px; font-weight: 600; color: #1F285F;
  cursor: pointer; transition: background 0.2s;
  flex-shrink: 0;
}
.btn-back:hover { background: #f0f6ff; }
.header-info {
  display: flex; align-items: center; gap: 10px; flex: 1; flex-wrap: wrap;
}
.page-title {
  font-size: 22px; font-weight: 700; color: #1F285F; margin: 0;
}
.badge-required {
  background: #1F285F; color: white;
  padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
}
.badge-area {
  background: #e8f0fe; color: #1F285F;
  padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
}
.btn-alert {
  display: flex; align-items: center; gap: 8px;
  background: #e74c3c; color: white;
  border: none; border-radius: 10px;
  padding: 9px 18px; font-size: 13px; font-weight: 600;
  cursor: pointer; transition: background 0.2s;
  margin-left: auto;
}
.btn-alert:hover { background: #c0392b; }

/* ── Overview Row ──────────────────────────────────────────────────────── */
.overview-row {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

/* ── Donut Card ────────────────────────────────────────────────────────── */
.donut-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(31,40,95,0.07);
}
.card-title {
  font-size: 15px; font-weight: 700; color: #1F285F;
  margin: 0 0 20px 0;
}
.donut-wrapper {
  display: flex; align-items: center; gap: 20px; margin-bottom: 20px;
}
.donut-svg {
  width: 120px; height: 120px; flex-shrink: 0;
}
.donut-segment {
  fill: none;
  stroke-width: 18;
  transition: stroke-dasharray 0.6s ease;
}
.donut-segment.not-started { stroke: #e74c3c; }
.donut-segment.in-progress { stroke: #f39c12; }
.donut-segment.completed   { stroke: #27ae60; }
.donut-pct {
  font-size: 18px; font-weight: 700; fill: #1F285F;
  text-anchor: middle; dominant-baseline: middle;
}
.donut-label {
  font-size: 9px; fill: #888;
  text-anchor: middle; dominant-baseline: middle;
}
.donut-legend { display: flex; flex-direction: column; gap: 10px; }
.legend-row { display: flex; align-items: center; gap: 8px; }
.dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.dot.not-started { background: #e74c3c; }
.dot.in-progress { background: #f39c12; }
.dot.completed   { background: #27ae60; }
.legend-text { font-size: 12px; color: #555; flex: 1; }
.legend-count { font-size: 13px; font-weight: 700; color: #1F285F; }

/* Progresso geral */
.overall-progress { margin-top: 4px; }
.progress-label-row {
  display: flex; justify-content: space-between;
  font-size: 12px; color: #666; margin-bottom: 6px;
}
.progress-label-pct { font-weight: 700; color: #1F285F; }
.progress-track {
  height: 10px; background: #eaf1fb; border-radius: 6px; overflow: hidden;
}
.progress-fill {
  height: 100%; border-radius: 6px; transition: width 0.6s ease;
}

/* ── Summary Cards ─────────────────────────────────────────────────────── */
.summary-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 16px;
}
.summary-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  display: flex; align-items: center; gap: 16px;
  box-shadow: 0 2px 10px rgba(31,40,95,0.06);
  border-left: 4px solid transparent;
}
.not-started-card { border-left-color: #e74c3c; }
.in-progress-card { border-left-color: #f39c12; }
.completed-card   { border-left-color: #27ae60; }
.total-card       { border-left-color: #1F285F; }
.summary-icon {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  background: #eaf1fb; flex-shrink: 0;
}
.not-started-card .summary-icon { background: #fdf0ef; color: #e74c3c; }
.in-progress-card .summary-icon { background: #fef9ec; color: #f39c12; }
.completed-card   .summary-icon { background: #edfaf3; color: #27ae60; }
.total-card       .summary-icon { background: #eaf1fb; color: #1F285F; }
.summary-data { display: flex; flex-direction: column; gap: 2px; }
.summary-number { font-size: 28px; font-weight: 800; color: #1F285F; line-height: 1; }
.summary-label  { font-size: 12px; color: #888; }

/* ── Table Card ────────────────────────────────────────────────────────── */
.table-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(31,40,95,0.07);
}
.table-header-row {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px; gap: 16px; flex-wrap: wrap;
}
.table-filters { display: flex; gap: 10px; flex-wrap: wrap; }
.search-wrapper {
  position: relative; display: flex; align-items: center;
}
.search-icon { position: absolute; left: 10px; pointer-events: none; }
.search-input {
  padding: 8px 12px 8px 32px;
  border: 1.5px solid #dde6f0; border-radius: 8px;
  font-size: 13px; color: #333; outline: none; width: 200px;
  transition: border-color 0.2s;
}
.search-input:focus { border-color: #1F285F; }
.filter-select {
  padding: 8px 12px; border: 1.5px solid #dde6f0; border-radius: 8px;
  font-size: 13px; color: #444; background: white; outline: none; cursor: pointer;
}

/* Table */
.table-wrapper { overflow-x: auto; }
.data-table {
  width: 100%; border-collapse: collapse; min-width: 680px;
}
.data-table th {
  text-align: left; padding: 10px 14px;
  font-size: 12px; font-weight: 700; color: #888;
  text-transform: uppercase; letter-spacing: 0.5px;
  border-bottom: 2px solid #eaf1fb;
  white-space: nowrap;
}
.sortable { cursor: pointer; user-select: none; }
.sortable:hover { color: #1F285F; }
.sort-arrow { font-size: 11px; color: #ccc; margin-left: 4px; }
.sort-arrow.active { color: #1F285F; }
.data-row td {
  padding: 12px 14px; border-bottom: 1px solid #f4f7fb;
  vertical-align: middle;
}
.data-row:last-child td { border-bottom: none; }
.data-row:hover td { background: #f8faff; }
.empty-row { text-align: center; color: #aaa; padding: 40px; font-size: 14px; }

/* Coluna aluno */
.td-name { display: flex; align-items: center; gap: 12px; }
.student-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: #1F285F; color: white;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700; flex-shrink: 0;
}
.student-info { display: flex; flex-direction: column; }
.student-name  { font-size: 13px; font-weight: 600; color: #1F285F; }
.student-email { font-size: 11px; color: #888; }

/* Status badges */
.status-badge {
  padding: 3px 10px; border-radius: 20px;
  font-size: 11px; font-weight: 600; white-space: nowrap;
}
.status-notstarted { background: #fdf0ef; color: #e74c3c; }
.status-inprogress { background: #fef9ec; color: #d68910; }
.status-completed  { background: #edfaf3; color: #1e8449; }

/* Mini progress */
.td-progress { min-width: 140px; }
.mini-progress-wrapper { display: flex; align-items: center; gap: 8px; }
.mini-track {
  flex: 1; height: 8px; background: #eaf1fb;
  border-radius: 4px; overflow: hidden;
}
.mini-fill { height: 100%; border-radius: 4px; transition: width 0.4s ease; }
.mini-pct { font-size: 12px; font-weight: 600; color: #555; width: 36px; text-align: right; }

/* Último acesso */
.td-access { white-space: nowrap; }
.access-date { font-size: 13px; color: #444; margin-right: 8px; }
.access-never { font-size: 12px; color: #aaa; font-style: italic; }
.badge-urgent {
  background: #fdf0ef; color: #e74c3c;
  border: 1px solid #f5c6c0;
  padding: 2px 8px; border-radius: 20px;
  font-size: 11px; font-weight: 600;
}

/* Nota */
.grade-badge {
  padding: 3px 10px; border-radius: 20px;
  font-size: 12px; font-weight: 700;
}
.grade-good { background: #edfaf3; color: #1e8449; }
.grade-mid  { background: #fef9ec; color: #d68910; }
.grade-bad  { background: #fdf0ef; color: #e74c3c; }
.grade-none { color: #ccc; font-size: 14px; }

/* ── Modal ─────────────────────────────────────────────────────────────── */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.modal-box {
  background: white; border-radius: 18px;
  width: 480px; max-width: 95vw;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  overflow: hidden;
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 20px 24px 16px;
  border-bottom: 1px solid #eaf1fb;
}
.modal-header h3 { font-size: 16px; font-weight: 700; color: #1F285F; margin: 0; }
.modal-close {
  background: none; border: none; font-size: 22px; color: #aaa;
  cursor: pointer; line-height: 1; padding: 0;
}
.modal-close:hover { color: #333; }
.modal-body { padding: 20px 24px; }
.modal-desc { font-size: 14px; color: #555; margin-bottom: 16px; }
.form-label { display: block; font-size: 12px; font-weight: 600; color: #555; margin-bottom: 6px; }
.form-input, .form-textarea {
  width: 100%; padding: 10px 12px; box-sizing: border-box;
  border: 1.5px solid #dde6f0; border-radius: 8px;
  font-size: 13px; color: #333; outline: none;
  margin-bottom: 14px; transition: border-color 0.2s;
  font-family: inherit;
}
.form-input:focus, .form-textarea:focus { border-color: #1F285F; }
.form-textarea { resize: vertical; min-height: 100px; }
.alert-error   { background: #fdf0ef; color: #e74c3c; border-radius: 8px; padding: 10px 14px; font-size: 13px; margin-top: 4px; }
.alert-success { background: #edfaf3; color: #1e8449; border-radius: 8px; padding: 10px 14px; font-size: 13px; margin-top: 4px; }
.modal-footer {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid #eaf1fb;
}
.btn-cancel {
  padding: 9px 18px; background: white;
  border: 1.5px solid #dde6f0; border-radius: 8px;
  font-size: 13px; font-weight: 600; color: #555; cursor: pointer;
}
.btn-cancel:hover { background: #f4f7fb; }
.btn-send {
  padding: 9px 20px; background: #1F285F; color: white;
  border: none; border-radius: 8px;
  font-size: 13px; font-weight: 600; cursor: pointer;
  transition: background 0.2s;
}
.btn-send:hover:not(:disabled) { background: #2e3d8f; }
.btn-send:disabled { opacity: 0.6; cursor: not-allowed; }

/* ── Responsive ────────────────────────────────────────────────────────── */
@media (max-width: 900px) {
  .overview-row { grid-template-columns: 1fr; }
  .summary-cards { grid-template-columns: 1fr 1fr; grid-template-rows: auto; }
}
@media (max-width: 600px) {
  .course-details-view { padding: 16px; }
  .summary-cards { grid-template-columns: 1fr; }
  .page-title { font-size: 18px; }
  .btn-alert { width: 100%; justify-content: center; margin-left: 0; }
}
</style>
