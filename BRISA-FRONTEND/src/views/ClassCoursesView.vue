<template>
  <div class="courses-view">
    <!-- Header -->
    <div class="page-header">
      <button class="btn-back" @click="router.back()">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/>
        </svg>
        Voltar
      </button>
      <div class="header-info">
        <h1 class="page-title">{{ classData.code || 'Turma' }}</h1>
        <span v-if="classData.location" class="header-badge">
          {{ classData.location.state }} · {{ classData.location.acronym }}
        </span>
      </div>
      <button class="btn-import-progressions" @click="showProgressionImportModal = true">
        Importar Progressões (Excel)
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Carregando cursos...</p>
    </div>

    <!-- Conteúdo -->
    <div v-else>
      <!-- Cards de resumo -->
      <div class="summary-cards">
        <div class="summary-card">
          <span class="summary-label">TOTAL DE CURSOS</span>
          <span class="summary-number">{{ courseStats.total }}</span>
        </div>
        <div class="summary-card not-started">
          <span class="summary-label">NÃO INICIADOS</span>
          <span class="summary-number">{{ courseStats.notStarted }}</span>
        </div>
        <div class="summary-card in-progress">
          <span class="summary-label">EM ANDAMENTO</span>
          <span class="summary-number">{{ courseStats.inProgress }}</span>
        </div>
        <div class="summary-card completed">
          <span class="summary-label">CONCLUÍDOS</span>
          <span class="summary-number">{{ courseStats.completed }}</span>
        </div>
      </div>

      <!-- Lista de cursos -->
      <div class="courses-list">
        <div v-if="courseItems.length === 0" class="no-data">
          Nenhum curso encontrado para esta turma.
        </div>

        <div
          v-for="course in courseItems"
          :key="course.id"
          class="course-card"
        >
          <div class="course-left">
            <!-- Info do curso -->
            <div class="course-info">
              <div class="course-name-row">
                <span class="course-name">{{ course.name }}</span>
                <span v-if="course.required" class="tag-required">Obrigatório</span>
              </div>
              <span v-if="course.knowledgeArea" class="course-area">{{ course.knowledgeArea }}</span>
            </div>
          </div>

          <div class="course-right">
            <div class="course-stats">
              <div class="stat">
                <strong>{{ course.completionPct }}%</strong>
                <span>Média</span>
              </div>
              <div class="stat">
                <strong>{{ course.pctCompleted }}%</strong>
                <span>Concluídos</span>
              </div>
            </div>

            <!-- Badge circular de % -->
            <div class="completion-badge" :style="{ '--pct': course.completionPct, '--color': getCompletionColor(course.completionPct) }">
              <svg viewBox="0 0 36 36" class="circular-chart">
                <path class="circle-bg" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"/>
                <path
                  class="circle"
                  :stroke="getCompletionColor(course.completionPct)"
                  :stroke-dasharray="`${course.completionPct}, 100`"
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                />
              </svg>
              <span class="badge-pct">{{ course.completionPct }}%</span>
            </div>

            <div class="course-actions">
              <button class="btn-secondary-action" @click.stop="openEvaluationModal(course)">Avaliações</button>
              <button v-if="course.assigned" class="btn-remove" @click.stop="removeCourse(course)">Remover</button>
              <button v-else class="btn-add" @click.stop="assignCourse(course)">Adicionar</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Aviso e Prova Final -->
      <div class="alert-banner alert-yellow" style="margin-top:18px;">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3.05h16.94a2 2 0 0 0 1.71-3.05L13.71 3.86a2 2 0 0 0-3.42 0z" />
          <line x1="12" y1="9" x2="12" y2="13" />
          <line x1="12" y1="17" x2="12.01" y2="17" />
        </svg>
        <div class="alert-copy">
          <strong>Prova Final do Nivelamento marcada para 30/04</strong>
          <p>Recomenda-se revisar as notas e progressões antes da prova final.</p>
        </div>
        <button type="button" class="alert-link" @click="() => {}">Ver cronograma</button>
      </div>

      <div class="final-exam-cards" style="margin-top:16px; display:flex; gap:12px;">
        <div class="exam-card">
          <h4>Prova Final - Turma A</h4>
          <p>Data: 30/04 · Local: Online</p>
          <div class="exam-meta"><strong>Participantes:</strong> {{ courseStats.total }}</div>
        </div>
        <div class="exam-card">
          <h4>Resultado Consolidado</h4>
          <p>Média de aprovação: <strong>{{ Math.round((courseStats.completed / Math.max(courseStats.total,1)) * 100) }}%</strong></p>
          <div class="exam-meta"><strong>Cursos:</strong> {{ courseStats.total }}</div>
        </div>
      </div>

      <!-- Alunos do Nivelamento -->
      <div class="students-section" style="margin-top:20px;">
        <h3>Alunos do Nivelamento</h3>
        <div class="students-table">
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>CPF</th>
                <th>Progresso médio</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="student in getNivelamentoStudents" :key="student.id">
                <td>{{ student.name || student.id }}</td>
                <td>{{ student.cpf || '-' }}</td>
                <td>
                  <div class="student-progress">
                    <div class="student-progress-bar">
                      <div class="student-progress-fill" :style="{ width: student.avg + '%' }"></div>
                    </div>
                    <small>{{ student.avg }}%</small>
                  </div>
                </td>
                <td><span class="status-pill" :class="student.avg===100 ? 'status-approved' : 'status-inprogress'">{{ student.avg===100 ? 'Concluído' : 'Em andamento' }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div v-if="showProgressionImportModal" class="modal-overlay" @click="closeProgressionImportModal">
      <div class="modal-content" @click.stop>
        <h2>Importar Progressões por Excel</h2>
        <p class="modal-hint">Formato esperado: Nome do curso | CPF | Nota | Progresso</p>

        <div class="upload-area">
          <input
            type="file"
            accept=".xlsx,.xls"
            @change="handleProgressionFileChange"
          />
          <p v-if="progressionImportFile">{{ progressionImportFile.name }}</p>
        </div>

        <div v-if="progressionImportError" class="alert-error">{{ progressionImportError }}</div>
        <div v-if="progressionImportSuccess" class="alert-success">{{ progressionImportSuccess }}</div>

        <div class="modal-actions">
          <button class="btn-cancel" @click="closeProgressionImportModal">Cancelar</button>
          <button
            class="btn-send"
            :disabled="importingProgressions || !progressionImportFile"
            @click="importProgressionsExcel"
          >
            {{ importingProgressions ? 'Importando...' : 'Importar' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showEvaluationModal && evaluationCourse" class="modal-overlay" @click="closeEvaluationModal">
      <div class="modal-content modal-content-large" @click.stop>
        <div class="evaluation-modal-head">
          <div>
            <h2>Avaliações do curso</h2>
            <p class="modal-hint">
              Cadastre até 4 avaliações para <strong>{{ evaluationCourse.name }}</strong>.
            </p>
          </div>
          <button class="btn-cancel btn-inline" @click="closeEvaluationModal">Fechar</button>
        </div>

        <div class="evaluation-summary">
          <div class="evaluation-summary-card">
            <span>Total cadastrado</span>
            <strong>{{ evaluations.length }}/4</strong>
          </div>
          <div class="evaluation-summary-card">
            <span>Área</span>
            <strong>{{ evaluationCourse.knowledgeArea || 'Não informada' }}</strong>
          </div>
          <div class="evaluation-summary-card">
            <span>Status</span>
            <strong>{{ canCreateMoreEvaluations ? 'Aceitando novos itens' : 'Limite atingido' }}</strong>
          </div>
        </div>

        <div v-if="evaluationsError" class="alert-error">{{ evaluationsError }}</div>

        <div class="evaluation-layout">
          <div class="evaluation-list-panel">
            <div class="evaluation-panel-head">
              <h3>Avaliações cadastradas</h3>
            </div>

            <div v-if="evaluationsLoading" class="no-data-inline">Carregando avaliações...</div>
            <div v-else-if="evaluations.length === 0" class="no-data-inline">
              Nenhuma avaliação cadastrada para este curso.
            </div>

            <div v-else class="evaluation-list">
              <article v-for="evaluation in evaluations" :key="evaluation.id" class="evaluation-card">
                <div class="evaluation-card-top">
                  <div>
                    <strong>{{ evaluation.name || 'Avaliação sem nome' }}</strong>
                    <div class="evaluation-card-subtitle">
                      {{ evaluation.type || 'Tipo não informado' }}
                    </div>
                  </div>
                  <div class="evaluation-card-actions">
                    <button class="btn-icon-pill" @click="startEditingEvaluation(evaluation)">Editar</button>
                    <button
                      class="btn-icon-pill danger"
                      :disabled="deletingEvaluationId === evaluation.id"
                      @click="removeEvaluation(evaluation)"
                    >
                      {{ deletingEvaluationId === evaluation.id ? 'Excluindo...' : 'Excluir' }}
                    </button>
                  </div>
                </div>

                <div class="evaluation-card-meta">
                  <span><strong>Código:</strong> {{ evaluation.code || '-' }}</span>
                  <span><strong>Data:</strong> {{ formatDate(evaluation.eventDate) }}</span>
                </div>
                <div class="evaluation-card-range">
                  <span>Faixa de pontuação</span>
                  <strong>{{ evaluation.minimumScore ?? 0 }} - {{ evaluation.maximumScore ?? 0 }}</strong>
                </div>
              </article>
            </div>
          </div>

          <form class="evaluation-form-panel" @submit.prevent="saveEvaluation">
            <div class="evaluation-panel-head">
              <h3>{{ editingEvaluationId ? 'Editar avaliação' : 'Nova avaliação' }}</h3>
              <button
                v-if="editingEvaluationId"
                type="button"
                class="btn-cancel btn-inline"
                @click="resetEvaluationForm"
              >
                Limpar
              </button>
            </div>

            <div v-if="!canCreateMoreEvaluations && !editingEvaluationId" class="alert-error">
              Este curso já atingiu o limite de 4 avaliações.
            </div>

            <div class="evaluation-form-grid">
              <label class="evaluation-field">
                <span>Código</span>
                <input v-model="evaluationForm.code" type="text" placeholder="Ex: AV1" />
              </label>

              <label class="evaluation-field">
                <span>Tipo</span>
                <input v-model="evaluationForm.type" type="text" placeholder="Ex: Prova final" />
              </label>

              <label class="evaluation-field evaluation-field-full">
                <span>Nome</span>
                <input v-model="evaluationForm.name" type="text" placeholder="Ex: Avaliação final do módulo" required />
              </label>

              <label class="evaluation-field">
                <span>Pontuação mínima</span>
                <input v-model="evaluationForm.minimumScore" type="number" min="0" step="0.01" placeholder="0" />
              </label>

              <label class="evaluation-field">
                <span>Pontuação máxima</span>
                <input v-model="evaluationForm.maximumScore" type="number" min="0" step="0.01" placeholder="10" />
              </label>

              <label class="evaluation-field evaluation-field-full">
                <span>Data de realização</span>
                <input v-model="evaluationForm.eventDate" type="date" />
              </label>
            </div>

            <div class="modal-actions">
              <button type="button" class="btn-cancel" @click="resetEvaluationForm">Cancelar</button>
              <button
                type="submit"
                class="btn-send"
                :disabled="savingEvaluation || (!editingEvaluationId && !canCreateMoreEvaluations)"
              >
                {{ savingEvaluation ? 'Salvando...' : editingEvaluationId ? 'Salvar alterações' : 'Cadastrar avaliação' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { classService } from '@/services/classService';
import { courseService } from '@/services/courseService';
import { courseEvaluationService } from '@/services/courseEvaluationService';

export default {
  name: 'ClassCoursesView',
  setup() {
    const route = useRoute();
    const router = useRouter();

    const classData = ref({});
    const courses = ref([]);
    const progressions = ref([]);
    const assignments = ref([]);
    const assignedCourseIds = ref([]);
    const loading = ref(true);
    const showProgressionImportModal = ref(false);
    const progressionImportFile = ref(null);
    const importingProgressions = ref(false);
    const progressionImportError = ref(null);
    const progressionImportSuccess = ref('');
    const showEvaluationModal = ref(false);
    const evaluationCourse = ref(null);
    const evaluations = ref([]);
    const evaluationsLoading = ref(false);
    const evaluationsError = ref('');
    const savingEvaluation = ref(false);
    const deletingEvaluationId = ref(null);
    const editingEvaluationId = ref(null);
    const evaluationForm = ref({
      code: '',
      type: '',
      name: '',
      minimumScore: '',
      maximumScore: '',
      eventDate: ''
    });

    const classId = computed(() => route.params.classId);

    // Monta os itens de curso com estatísticas calculadas
    const courseItems = computed(() => {
      if (courses.value.length === 0) {
        return [];
      }

      return courses.value.map(course => {
        const courseProgressions = progressions.value.filter(p => p.course?.id === course.id);
        const total = courseProgressions.length || 1;
        const assignment = assignments.value.find(a => a.course?.id === course.id);

        const notStarted  = courseProgressions.filter(p => p.status === 'não iniciado').length;
        const inProgress  = courseProgressions.filter(p => p.status === 'em andamento').length;
        const completed   = courseProgressions.filter(p => p.status === 'concluído').length;

        const avgCompletion = courseProgressions.length
          ? Math.round(courseProgressions.reduce((acc, p) => acc + p.completionPercentage, 0) / total)
          : 0;

        return {
          id: course.id,
          name: course.name,
          knowledgeArea: course.knowledgeArea?.name,
          required: assignment ? assignment.required !== false : true,
          completionPct: avgCompletion,
          pctNotStarted:  Math.round((notStarted / total) * 100),
          pctInProgress:  Math.round((inProgress / total) * 100),
          pctCompleted:   Math.round((completed  / total) * 100),
          assigned: assignedCourseIds.value.includes(course.id),
        };
      });
    });

    const courseStats = computed(() => ({
      total:      courseItems.value.length,
      notStarted: courseItems.value.filter(c => c.completionPct === 0).length,
      inProgress: courseItems.value.filter(c => c.completionPct > 0 && c.completionPct < 100).length,
      completed:  courseItems.value.filter(c => c.completionPct === 100).length,
    }));

    const canCreateMoreEvaluations = computed(() => evaluations.value.length < 4 || Boolean(editingEvaluationId.value));

    const getCompletionColor = (pct) => {
      if (pct >= 80) return '#27ae60';
      if (pct >= 40) return '#f39c12';
      return '#e74c3c';
    };

    const formatDate = (value) => {
      if (!value) return '-';
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) return '-';
      return date.toLocaleDateString('pt-BR');
    };

    const assignCourse = async (course) => {
      try {
        loading.value = true;
        await courseService.assignCourseToClass(course.id, classId.value, true);
        await loadData();
      } catch (err) {
        console.error('Erro ao atribuir curso:', err);
      } finally {
        loading.value = false;
      }
    };

    const removeCourse = async (course) => {
      try {
        loading.value = true;
        await courseService.removeCourseFromClass(course.id, classId.value);
        await loadData();
      } catch (err) {
        console.error('Erro ao remover curso:', err);
      } finally {
        loading.value = false;
      }
    };

    const loadData = async () => {
      loading.value = true;
      try {
        const cls = await classService.getById(classId.value);
        const asgs = await courseService.getAssignmentsByClassId(classId.value).catch(() => []);
        const progs = await courseService.getProgressionsByClassId(classId.value).catch(() => []);

        classData.value = cls || {};
        assignments.value = Array.isArray(asgs) ? asgs : [];
        progressions.value = Array.isArray(progs) ? progs : [];

        // Cursos atribuídos à turma (a partir das assignments persistidas)
        const assignedIds = Array.from(new Set(assignments.value.map(a => a.course?.id).filter(Boolean)));
        assignedCourseIds.value = assignedIds;

        if (assignedIds.length > 0) {
          const allCourses = await courseService.getAll();
          courses.value = Array.isArray(allCourses) ? allCourses.filter(c => assignedIds.includes(c.id)) : [];
        } else {
          courses.value = [];
        }

      } catch (err) {
        console.error('Erro ao carregar cursos:', err);
      } finally {
        loading.value = false;
      }
    };

    const handleProgressionFileChange = (event) => {
      const file = event.target.files[0];
      progressionImportFile.value = file || null;
      progressionImportError.value = null;
      progressionImportSuccess.value = '';
    };

    const importProgressionsExcel = async () => {
      if (!progressionImportFile.value) return;
      importingProgressions.value = true;
      progressionImportError.value = null;
      progressionImportSuccess.value = '';

      try {
        const formData = new FormData();
        formData.append('file', progressionImportFile.value);
        const result = await courseService.importProgressionsFromExcel(classId.value, formData);

        progressionImportSuccess.value =
          `Processadas: ${result.totalProcessed}. ` +
          `Criadas: ${result.createdProgressions}. ` +
          `Atualizadas: ${result.updatedProgressions}. ` +
          `Notas atualizadas: ${result.updatedGrades}. ` +
          `Ignoradas: ${result.skippedRows}.`;

        await loadData();
      } catch (err) {
        progressionImportError.value = 'Erro ao importar progressões: ' + (err.response?.data?.message || err.message);
      } finally {
        importingProgressions.value = false;
      }
    };

    const closeProgressionImportModal = () => {
      showProgressionImportModal.value = false;
      progressionImportFile.value = null;
      progressionImportError.value = null;
      progressionImportSuccess.value = '';
    };

    const resetEvaluationForm = () => {
      editingEvaluationId.value = null;
      evaluationForm.value = {
        code: '',
        type: '',
        name: '',
        minimumScore: '',
        maximumScore: '',
        eventDate: ''
      };
    };

    const loadEvaluations = async (courseId) => {
      evaluationsLoading.value = true;
      evaluationsError.value = '';
      try {
        const data = await courseEvaluationService.listByCourse(courseId);
        evaluations.value = Array.isArray(data) ? data : [];
      } catch (err) {
        evaluations.value = [];
        evaluationsError.value = `Erro ao carregar avaliações: ${err.response?.data?.message || err.message}`;
      } finally {
        evaluationsLoading.value = false;
      }
    };

    const openEvaluationModal = async (course) => {
      evaluationCourse.value = course;
      showEvaluationModal.value = true;
      resetEvaluationForm();
      await loadEvaluations(course.id);
    };

    const closeEvaluationModal = () => {
      showEvaluationModal.value = false;
      evaluationCourse.value = null;
      evaluations.value = [];
      evaluationsError.value = '';
      resetEvaluationForm();
    };

    const startEditingEvaluation = (evaluation) => {
      editingEvaluationId.value = evaluation.id;
      evaluationForm.value = {
        code: evaluation.code || '',
        type: evaluation.type || '',
        name: evaluation.name || '',
        minimumScore: evaluation.minimumScore ?? '',
        maximumScore: evaluation.maximumScore ?? '',
        eventDate: evaluation.eventDate || ''
      };
    };

    const saveEvaluation = async () => {
      if (!evaluationCourse.value) return;

      savingEvaluation.value = true;
      evaluationsError.value = '';

      try {
        const payload = {
          code: evaluationForm.value.code || null,
          type: evaluationForm.value.type || null,
          name: evaluationForm.value.name,
          minimumScore: evaluationForm.value.minimumScore === '' ? null : Number(evaluationForm.value.minimumScore),
          maximumScore: evaluationForm.value.maximumScore === '' ? null : Number(evaluationForm.value.maximumScore),
          eventDate: evaluationForm.value.eventDate || null
        };

        if (editingEvaluationId.value) {
          await courseEvaluationService.update(evaluationCourse.value.id, editingEvaluationId.value, payload);
        } else {
          await courseEvaluationService.create(evaluationCourse.value.id, payload);
        }

        await loadEvaluations(evaluationCourse.value.id);
        resetEvaluationForm();
      } catch (err) {
        evaluationsError.value = `Erro ao salvar avaliação: ${err.response?.data?.message || err.message}`;
      } finally {
        savingEvaluation.value = false;
      }
    };

    const removeEvaluation = async (evaluation) => {
      if (!evaluationCourse.value) return;
      const confirmed = window.confirm(`Deseja excluir a avaliação "${evaluation.name}"?`);
      if (!confirmed) return;

      deletingEvaluationId.value = evaluation.id;
      evaluationsError.value = '';
      try {
        await courseEvaluationService.delete(evaluationCourse.value.id, evaluation.id);
        await loadEvaluations(evaluationCourse.value.id);
        if (editingEvaluationId.value === evaluation.id) {
          resetEvaluationForm();
        }
      } catch (err) {
        evaluationsError.value = `Erro ao excluir avaliação: ${err.response?.data?.message || err.message}`;
      } finally {
        deletingEvaluationId.value = null;
      }
    };

    onMounted(loadData);

    const getNivelamentoStudents = computed(() => {
      const map = {};
      (progressions.value || []).forEach(p => {
        const pid = p.people?.id || p.peopleId || p.personId || p.person?.id || null;
        if (!pid) return;
        if (!map[pid]) {
          map[pid] = {
            id: pid,
            name: p.people?.name || p.person?.name || p.personName || '',
            cpf: p.people?.cpf || p.person?.cpf || '' ,
            total: 0,
            sum: 0
          };
        }
        const pct = Number(p.completionPercentage ?? p.completionPct ?? p.completion ?? 0);
        map[pid].total += 1;
        map[pid].sum += isNaN(pct) ? 0 : pct;
      });
      return Object.values(map).map(s => ({
        id: s.id,
        name: s.name,
        cpf: s.cpf,
        avg: s.total ? Math.round(s.sum / s.total) : 0
      }));
    });

    return {
      router, classData, loading,
      courseItems, courseStats,
      getCompletionColor,
      formatDate,
      assignCourse, removeCourse,
      showProgressionImportModal,
      progressionImportFile,
      importingProgressions,
      progressionImportError,
      progressionImportSuccess,
      handleProgressionFileChange,
      importProgressionsExcel,
      closeProgressionImportModal,
      getNivelamentoStudents,
      showEvaluationModal,
      evaluationCourse,
      evaluations,
      evaluationsLoading,
      evaluationsError,
      savingEvaluation,
      deletingEvaluationId,
      editingEvaluationId,
      evaluationForm,
      canCreateMoreEvaluations,
      openEvaluationModal,
      closeEvaluationModal,
      startEditingEvaluation,
      resetEvaluationForm,
      saveEvaluation,
      removeEvaluation
    };
  }
};
</script>

<style scoped>
.courses-view {
  padding: 28px 32px;
  max-width: 1100px;
  margin: 0 auto;
  background: #eaf1fb;
  min-height: 100vh;
}

/* Header */
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  justify-content: space-between;
  margin-bottom: 28px;
}
.header-info { display: flex; align-items: center; gap: 12px; flex: 1; }
.btn-back {
  display: flex;
  align-items: center;
  gap: 6px;
  background: white;
  border: 1.5px solid #dde6f0;
  border-radius: 10px;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 600;
  color: #1F285F;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-back:hover { background: #f0f6ff; }
.page-title { font-size: 24px; font-weight: 700; color: #1F285F; margin: 0; }
.header-badge {
  background: #1F285F;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}
.btn-import-progressions {
  background: #1F285F;
  color: #fff;
  border: 1px solid #1F285F;
  border-radius: 10px;
  padding: 10px 14px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}
.btn-import-progressions:hover { background: #2f3d8f; }

/* Loading */
.loading-state {
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  padding: 80px; gap: 16px; color: #666;
}
.spinner {
  width: 40px; height: 40px;
  border: 3px solid #dde6f0;
  border-top-color: #1F285F;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Summary cards */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}
.summary-card {
  background: white;
  border-radius: 14px;
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  box-shadow: 0 2px 12px rgba(31,40,95,0.07);
  border-left: 4px solid #1F285F;
}
.summary-card.not-started { border-left-color: #e74c3c; }
.summary-card.in-progress { border-left-color: #f39c12; }
.summary-card.completed   { border-left-color: #27ae60; }
.summary-label {
  font-size: 10px; font-weight: 700;
  color: #888; letter-spacing: 0.5px; text-transform: uppercase;
}
.summary-number { font-size: 32px; font-weight: 700; color: #1F285F; line-height: 1; }

/* Courses list */
.courses-list { display: flex; flex-direction: column; gap: 14px; }
.course-card {
  background: white;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(31,40,95,0.07);
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.15s;
}
.course-card:hover {
  box-shadow: 0 4px 20px rgba(31,40,95,0.13);
  transform: translateY(-1px);
}
.course-left { display: flex; align-items: center; gap: 20px; flex: 1; }
.course-right { display:flex; align-items:center; gap:14px; flex-shrink:0; }
.course-stats { display:flex; flex-direction:column; gap:6px; text-align:right; margin-right:6px; }
.course-stats .stat { font-size:12px; color:#666; }
.course-stats .stat strong { display:block; font-size:16px; color:#1F285F; }

/* Badge circular */
.completion-badge {
  position: relative;
  width: 56px; height: 56px;
  flex-shrink: 0;
}
.circular-chart { width: 56px; height: 56px; }
.circle-bg {
  fill: none;
  stroke: #eaf1fb;
  stroke-width: 3.5;
}
.circle {
  fill: none;
  stroke-width: 3.5;
  stroke-linecap: round;
  transform: rotate(-90deg);
  transform-origin: 50% 50%;
  transition: stroke-dasharray 0.6s ease;
}
.badge-pct {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: #1F285F;
}

/* Course info */
.course-info { flex: 1; }
.course-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}
.course-name { font-size: 15px; font-weight: 700; color: #1F285F; }
.tag-required {
  background: #eaf1fb;
  color: #1F285F;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 20px;
  border: 1px solid #c8d8ee;
}
.course-area { font-size: 12px; color: #888; display: block; margin-bottom: 10px; }

/* Barra segmentada */
.progress-bar-wrapper { display: flex; flex-direction: column; gap: 6px; }
.progress-bar {
  height: 10px;
  border-radius: 8px;
  background: #eaf1fb;
  display: flex;
  overflow: hidden;
  width: 100%;
  max-width: 500px;
}
.progress-segment { height: 100%; transition: width 0.5s ease; }
.progress-segment.not-started { background: #e74c3c; }
.progress-segment.in-progress { background: #f39c12; }
.progress-segment.completed   { background: #27ae60; }
.progress-legend {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
.legend-item { display: flex; align-items: center; gap: 5px; font-size: 11px; color: #666; }
.legend-dot-sm {
  width: 8px; height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.legend-dot-sm.not-started { background: #e74c3c; }
.legend-dot-sm.in-progress { background: #f39c12; }
.legend-dot-sm.completed   { background: #27ae60; }

.arrow-icon { flex-shrink: 0; margin-left: 12px; }
.no-data { text-align: center; color: #aaa; padding: 48px; font-size: 14px; }

.course-actions { display: flex; gap: 8px; align-items: center; margin-right: 8px; }
.btn-secondary-action,
.btn-add, .btn-remove {
  background: #fff; border: 1px solid #c8d8ee; color: #1F285F; padding: 8px 10px; border-radius: 8px; font-weight: 700; cursor: pointer;
}
.btn-secondary-action:hover { background: #f6f8ff; }
.btn-add:hover { background: #f0fbff; }
.btn-remove { border-color: #f5d2d2; color: #b71c1c; }
.btn-remove:hover { background: #fff5f5; }

/* Alert and final exam cards */
.alert-yellow { background:#fff8e6; border-radius:12px; padding:12px 16px; display:flex; gap:12px; align-items:center; border:1px solid #f2e7c9; }
.alert-yellow svg { flex-shrink:0; color:#f39c12; }
.alert-copy strong { display:block; color:#1F285F; }
.alert-link { margin-left:auto; background:none; border:none; color:#1F285F; font-weight:700; cursor:pointer; }

.final-exam-cards { display:flex; gap:12px; }
.exam-card { background:white; padding:12px 14px; border-radius:10px; box-shadow:0 2px 8px rgba(0,0,0,0.04); flex:1; }
.exam-card h4 { margin:0 0 6px 0; color:#1F285F; }
.exam-meta { margin-top:8px; font-size:13px; color:#666; }

/* Students table */
.students-table table { width:100%; border-collapse:collapse; margin-top:8px; }
.students-table th, .students-table td { text-align:left; padding:10px 8px; border-bottom:1px solid #f1f3f6; }
.student-progress { display:flex; align-items:center; gap:8px; }
.student-progress-bar { width:140px; height:8px; background:#eaf1fb; border-radius:8px; overflow:hidden; }
.student-progress-fill { height:100%; background:#27ae60; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  background: #fff;
  border-radius: 14px;
  width: 520px;
  max-width: 94vw;
  padding: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.modal-content-large {
  width: min(980px, 96vw);
  max-width: min(980px, 96vw);
}

.modal-content h2 {
  margin: 0 0 8px 0;
  color: #1F285F;
  font-size: 20px;
}

.modal-hint {
  margin: 0 0 16px 0;
  color: #666;
  font-size: 13px;
}

.upload-area {
  border: 2px dashed #d8e1ee;
  border-radius: 12px;
  padding: 18px;
  background: #f8fbff;
}

.upload-area p {
  margin: 10px 0 0;
  color: #1F285F;
  font-size: 13px;
  font-weight: 600;
}

.alert-error {
  background: #fdf0ef;
  color: #c0392b;
  border-radius: 8px;
  padding: 10px 12px;
  margin-top: 12px;
  font-size: 13px;
}

.alert-success {
  background: #edfaf3;
  color: #1e8449;
  border-radius: 8px;
  padding: 10px 12px;
  margin-top: 12px;
  font-size: 13px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.btn-cancel {
  background: #fff;
  color: #555;
  border: 1px solid #cfd9e8;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
}

.btn-send {
  background: #1F285F;
  color: #fff;
  border: 1px solid #1F285F;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
}

.btn-send:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-inline {
  padding: 7px 10px;
}

.evaluation-modal-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.evaluation-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.evaluation-summary-card {
  background: #f8fbff;
  border: 1px solid #d8e1ee;
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.evaluation-summary-card span {
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.evaluation-summary-card strong {
  color: #1F285F;
  font-size: 16px;
  line-height: 1.2;
}

.evaluation-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.evaluation-list-panel,
.evaluation-form-panel {
  background: #fcfdff;
  border: 1px solid #d8e1ee;
  border-radius: 14px;
  padding: 16px;
}

.evaluation-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.evaluation-panel-head h3 {
  margin: 0;
  color: #1F285F;
  font-size: 16px;
}

.no-data-inline {
  padding: 22px 14px;
  text-align: center;
  color: #64748b;
  background: #fff;
  border: 1px dashed #d8e1ee;
  border-radius: 12px;
}

.evaluation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.evaluation-card {
  border: 1px solid #d8e1ee;
  border-radius: 12px;
  background: #fff;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.evaluation-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.evaluation-card-top strong {
  color: #1F285F;
  font-size: 15px;
}

.evaluation-card-subtitle {
  color: #64748b;
  font-size: 12px;
  margin-top: 4px;
}

.evaluation-card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-icon-pill {
  border: 1px solid #d8e1ee;
  background: #fff;
  color: #1F285F;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.btn-icon-pill:hover {
  background: #f6f8ff;
}

.btn-icon-pill.danger {
  border-color: #f5d2d2;
  color: #b71c1c;
}

.btn-icon-pill.danger:hover {
  background: #fff5f5;
}

.evaluation-card-meta,
.evaluation-card-range {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  color: #475569;
  font-size: 13px;
}

.evaluation-card-range span {
  color: #64748b;
}

.evaluation-card-range strong {
  color: #1F285F;
}

.evaluation-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.evaluation-field {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.evaluation-field span {
  color: #1F285F;
  font-size: 13px;
  font-weight: 700;
}

.evaluation-field input {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #d8e1ee;
  border-radius: 10px;
  padding: 11px 12px;
  background: #fff;
  color: #1F285F;
}

.evaluation-field input:focus {
  outline: none;
  border-color: #5773d1;
  box-shadow: 0 0 0 3px rgba(87, 115, 209, 0.14);
}

.evaluation-field-full {
  grid-column: 1 / -1;
}

@media (max-width: 900px) {
  .summary-cards { grid-template-columns: repeat(2, 1fr); }
  .evaluation-layout { grid-template-columns: 1fr; }
  .evaluation-summary { grid-template-columns: 1fr; }
}
@media (max-width: 600px) {
  .courses-view { padding: 16px; }
  .summary-cards { grid-template-columns: 1fr 1fr; }
  .course-card { flex-direction: column; align-items: stretch; gap: 16px; }
  .course-right { width: 100%; justify-content: space-between; flex-wrap: wrap; }
  .course-actions { width: 100%; justify-content: flex-end; margin-right: 0; }
  .evaluation-form-grid { grid-template-columns: 1fr; }
}
</style>
