<template>
  <div class="logs-view">
    <div class="logs-header">
      <h1>Logs do Sistema</h1>
      <p class="subtitle">Histórico de ações e eventos do sistema</p>
    </div>

    <!-- Estatísticas -->
    <div v-if="stats" class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon total">📊</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalLogs }}</div>
          <div class="stat-label">Total de Logs</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon today">📅</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.logsToday }}</div>
          <div class="stat-label">Logs Hoje</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon week">📈</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.logsThisWeek }}</div>
          <div class="stat-label">Logs Esta Semana</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon month">📆</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.logsThisMonth }}</div>
          <div class="stat-label">Logs Este Mês</div>
        </div>
      </div>
    </div>

    <!-- Filtros -->
    <div class="filters-section">
      <div class="filters-header">
        <h2>Filtros</h2>
        <button @click="toggleFilters" class="btn-toggle-filters">
          {{ showFilters ? 'Ocultar' : 'Mostrar' }} Filtros
        </button>
      </div>
      
      <div v-if="showFilters" class="filters-grid">
        <div class="filter-group">
          <label>Ação</label>
          <select v-model="filters.action" class="filter-input">
            <option value="">Todas as ações</option>
            <option value="USER_LOGIN">Login de Usuário</option>
            <option value="USER_LOGOUT">Logout de Usuário</option>
            <option value="USER_REGISTER">Registro de Usuário</option>
            <option value="PEOPLE_CREATE">Pessoa Criada</option>
            <option value="PEOPLE_UPDATE">Pessoa Atualizada</option>
            <option value="PEOPLE_DELETE">Pessoa Excluída</option>
            <option value="PEOPLE_IMPORT">Importação de Pessoas</option>
            <option value="ADVISOR_CREATE">Equipe acadêmica criada</option>
            <option value="ADVISOR_UPDATE">Equipe acadêmica atualizada</option>
            <option value="ADVISOR_DELETE">Equipe acadêmica removida</option>
            <option value="PROGRAM_CREATE">Programa Criado</option>
            <option value="PROGRAM_UPDATE">Programa Atualizado</option>
            <option value="PROGRAM_DELETE">Programa Excluído</option>
            <option value="PROGRAM_IMPORT">Importação de Programas</option>
            <option value="CLASS_CREATE">Turma Criada</option>
            <option value="CLASS_UPDATE">Turma Atualizada</option>
            <option value="CLASS_DELETE">Turma Excluída</option>
            <option value="CLASS_IMPORT">Importação de Turmas</option>
            <option value="STAGE_CREATE">Etapa Criada</option>
            <option value="STAGE_UPDATE">Etapa Atualizada</option>
            <option value="STAGE_DELETE">Etapa Excluída</option>
            <option value="STAGE_CANDIDATES_IMPORT">Importação de Candidatos</option>
            <option value="ENROLLMENT_IMPORT">Importação de Matrículas</option>
            <option value="INSTITUTION_IMPORT">Importação de Instituições</option>
            <option value="SYSTEM_ERROR">Erro do Sistema</option>
          </select>
        </div>

        <div class="filter-group">
          <label>Tipo de Entidade</label>
          <select v-model="filters.entityType" class="filter-input">
            <option value="">Todos os tipos</option>
            <option value="User">Usuário</option>
            <option value="People">Pessoa</option>
            <option value="Advisor">Equipe acadêmica</option>
            <option value="Program">Programa</option>
            <option value="Class">Turma</option>
            <option value="Stage">Etapa</option>
            <option value="Enrollment">Matrícula</option>
            <option value="Institution">Instituição</option>
          </select>
        </div>

        <div class="filter-group">
          <label>Data Inicial</label>
          <input type="datetime-local" v-model="filters.startDate" class="filter-input" />
        </div>

        <div class="filter-group">
          <label>Data Final</label>
          <input type="datetime-local" v-model="filters.endDate" class="filter-input" />
        </div>

        <div class="filter-actions">
          <button @click="applyFilters" class="btn-apply">Aplicar Filtros</button>
          <button @click="clearFilters" class="btn-clear">Limpar</button>
        </div>
      </div>
    </div>

    <!-- Lista de Logs -->
    <div class="logs-section">
      <div class="section-header">
        <h2>Registros</h2>
        <div class="header-actions">
          <button @click="refreshLogs" class="btn-refresh" :disabled="loading">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"/>
            </svg>
            Atualizar
          </button>
        </div>
      </div>

      <div v-if="loading" class="loading">Carregando logs...</div>

      <div v-else-if="error" class="error">{{ error }}</div>

      <div v-else-if="logs.length === 0" class="no-data">
        Nenhum log encontrado com os filtros selecionados
      </div>

      <div v-else class="logs-table-container">
        <table class="logs-table">
          <thead>
            <tr>
              <th>Data/Hora</th>
              <th>Ação</th>
              <th>Descrição</th>
              <th>Usuário</th>
              <th>Entidade</th>
              <th>IP</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in logs" :key="log.id" :class="getLogClass(log.action)">
              <td class="date-cell">{{ formatDateTime(log.createdAt) }}</td>
              <td>
                <span class="action-badge" :class="getActionClass(log.action)">
                  {{ formatAction(log.action) }}
                </span>
              </td>
              <td class="description-cell">{{ log.description }}</td>
              <td>
                <div v-if="log.userName" class="user-info">
                  <div class="user-name">{{ log.userName }}</div>
                  <div class="user-email">{{ log.userEmail }}</div>
                </div>
                <span v-else class="no-user">Sistema</span>
              </td>
              <td>
                <div v-if="log.entityType" class="entity-info">
                  <span class="entity-type">{{ log.entityType }}</span>
                  <span v-if="log.entityId" class="entity-id">#{{ log.entityId }}</span>
                </div>
                <span v-else>-</span>
              </td>
              <td class="ip-cell">{{ log.ipAddress || '-' }}</td>
              <td>
                <button @click="viewDetails(log)" class="btn-details">Detalhes</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Paginação -->
      <div v-if="pagination.totalPages > 1" class="pagination">
        <button 
          @click="goToPage(pagination.currentPage - 1)" 
          :disabled="pagination.currentPage === 0"
          class="btn-page"
        >
          Anterior
        </button>
        <span class="page-info">
          Página {{ pagination.currentPage + 1 }} de {{ pagination.totalPages }}
        </span>
        <button 
          @click="goToPage(pagination.currentPage + 1)" 
          :disabled="pagination.currentPage >= pagination.totalPages - 1"
          class="btn-page"
        >
          Próxima
        </button>
      </div>
    </div>

    <!-- Modal de Detalhes -->
    <div v-if="selectedLog" class="modal-overlay" @click="closeDetails">
      <div class="modal-content log-details-modal" @click.stop>
        <div class="modal-header">
          <h2>Detalhes do Log</h2>
          <button @click="closeDetails" class="btn-close">&times;</button>
        </div>
        <div class="modal-body">
          <div class="detail-row">
            <span class="detail-label">ID:</span>
            <span class="detail-value">{{ selectedLog.id }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Data/Hora:</span>
            <span class="detail-value">{{ formatDateTime(selectedLog.createdAt) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Ação:</span>
            <span class="action-badge" :class="getActionClass(selectedLog.action)">
              {{ formatAction(selectedLog.action) }}
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Descrição:</span>
            <span class="detail-value">{{ selectedLog.description }}</span>
          </div>
          <div v-if="selectedLog.userName" class="detail-row">
            <span class="detail-label">Usuário:</span>
            <span class="detail-value">{{ selectedLog.userName }} ({{ selectedLog.userEmail }})</span>
          </div>
          <div v-if="selectedLog.entityType" class="detail-row">
            <span class="detail-label">Tipo de Entidade:</span>
            <span class="detail-value">{{ selectedLog.entityType }}</span>
          </div>
          <div v-if="selectedLog.entityId" class="detail-row">
            <span class="detail-label">ID da Entidade:</span>
            <span class="detail-value">{{ selectedLog.entityId }}</span>
          </div>
          <div v-if="selectedLog.ipAddress" class="detail-row">
            <span class="detail-label">Endereço IP:</span>
            <span class="detail-value">{{ selectedLog.ipAddress }}</span>
          </div>
          <div v-if="selectedLog.userAgent" class="detail-row">
            <span class="detail-label">User Agent:</span>
            <span class="detail-value small-text">{{ selectedLog.userAgent }}</span>
          </div>
          <div v-if="selectedLog.details" class="detail-row details-json">
            <span class="detail-label">Detalhes Adicionais:</span>
            <pre class="json-details">{{ formatJSON(selectedLog.details) }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { logService } from '@/services/logService';

export default {
  name: 'LogsView',
  setup() {
    const logs = ref([]);
    const stats = ref(null);
    const loading = ref(false);
    const error = ref(null);
    const showFilters = ref(false);
    const selectedLog = ref(null);

    const filters = ref({
      action: '',
      entityType: '',
      startDate: '',
      endDate: '',
      page: 0,
      size: 50
    });

    const pagination = ref({
      currentPage: 0,
      totalPages: 0,
      totalElements: 0
    });

    const loadLogs = async () => {
      loading.value = true;
      error.value = null;
      try {
        const response = await logService.getLogs(filters.value);
        logs.value = response.content || response;
        
        if (response.pageable) {
          pagination.value = {
            currentPage: response.pageable.pageNumber,
            totalPages: response.totalPages,
            totalElements: response.totalElements
          };
        }
      } catch (err) {
        error.value = 'Erro ao carregar logs: ' + (err.response?.data?.message || err.message);
      } finally {
        loading.value = false;
      }
    };

    const loadStats = async () => {
      try {
        stats.value = await logService.getStats();
      } catch (err) {
        console.error('Erro ao carregar estatísticas:', err);
      }
    };

    const applyFilters = () => {
      filters.value.page = 0;
      loadLogs();
    };

    const clearFilters = () => {
      filters.value = {
        action: '',
        entityType: '',
        startDate: '',
        endDate: '',
        page: 0,
        size: 50
      };
      loadLogs();
    };

    const refreshLogs = () => {
      loadLogs();
      loadStats();
    };

    const goToPage = (page) => {
      filters.value.page = page;
      loadLogs();
    };

    const toggleFilters = () => {
      showFilters.value = !showFilters.value;
    };

    const viewDetails = (log) => {
      selectedLog.value = log;
    };

    const closeDetails = () => {
      selectedLog.value = null;
    };

    const formatDateTime = (dateTime) => {
      if (!dateTime) return '-';
      const date = new Date(dateTime);
      return date.toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    };

    const formatAction = (action) => {
      const labels = {
        USER_LOGIN: 'Login de usuário',
        USER_LOGOUT: 'Logout de usuário',
        USER_REGISTER: 'Registro de usuário',
        USER_UPDATE: 'Atualização de usuário',
        USER_DELETE: 'Remoção de usuário',
        PEOPLE_CREATE: 'Pessoa criada',
        PEOPLE_UPDATE: 'Pessoa atualizada',
        PEOPLE_DELETE: 'Pessoa removida',
        PEOPLE_IMPORT: 'Importação de pessoas',
        ADVISOR_CREATE: 'Equipe acadêmica criada',
        ADVISOR_UPDATE: 'Equipe acadêmica atualizada',
        ADVISOR_DELETE: 'Equipe acadêmica removida',
        PROGRAM_CREATE: 'Programa criado',
        PROGRAM_UPDATE: 'Programa atualizado',
        PROGRAM_DELETE: 'Programa removido',
        PROGRAM_IMPORT: 'Importação de programas',
        CLASS_CREATE: 'Turma criada',
        CLASS_UPDATE: 'Turma atualizada',
        CLASS_DELETE: 'Turma removida',
        CLASS_IMPORT: 'Importação de turmas',
        STAGE_CREATE: 'Etapa criada',
        STAGE_UPDATE: 'Etapa atualizada',
        STAGE_DELETE: 'Etapa removida',
        STAGE_CANDIDATES_IMPORT: 'Importação de candidatos',
        ENROLLMENT_IMPORT: 'Importação de matrículas',
        INSTITUTION_IMPORT: 'Importação de instituições',
        SYSTEM_ERROR: 'Erro do sistema',
        SYSTEM_WARNING: 'Alerta do sistema',
        SYSTEM_INFO: 'Evento do sistema'
      };

      return labels[action] || action.replace(/_/g, ' ');
    };

    const getActionClass = (action) => {
      if (action.includes('CREATE') || action.includes('REGISTER')) return 'action-create';
      if (action.includes('UPDATE')) return 'action-update';
      if (action.includes('DELETE')) return 'action-delete';
      if (action.includes('LOGIN')) return 'action-login';
      if (action.includes('LOGOUT')) return 'action-logout';
      if (action.includes('IMPORT')) return 'action-import';
      if (action.includes('ERROR')) return 'action-error';
      return 'action-default';
    };

    const getLogClass = (action) => {
      if (action.includes('ERROR')) return 'log-error';
      if (action.includes('DELETE')) return 'log-warning';
      return '';
    };

    const formatJSON = (jsonString) => {
      try {
        return JSON.stringify(JSON.parse(jsonString), null, 2);
      } catch {
        return jsonString;
      }
    };

    onMounted(() => {
      loadLogs();
      loadStats();
    });

    return {
      logs,
      stats,
      loading,
      error,
      showFilters,
      selectedLog,
      filters,
      pagination,
      applyFilters,
      clearFilters,
      refreshLogs,
      goToPage,
      toggleFilters,
      viewDetails,
      closeDetails,
      formatDateTime,
      formatAction,
      getActionClass,
      getLogClass,
      formatJSON
    };
  }
};
</script>

<style scoped>
.logs-view {
  padding: 20px;
  max-width: 1800px;
  margin: 0 auto;
}

.logs-header {
  margin-bottom: 30px;
}

.logs-header h1 {
  margin: 0 0 8px 0;
  color: #1F285F;
  font-size: 32px;
  font-weight: 700;
}

.subtitle {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.stat-icon {
  font-size: 48px;
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.stat-icon.total { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.today { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.week { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon.month { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1F285F;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.filters-section,
.logs-section {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.filters-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filters-header h2,
.section-header h2 {
  margin: 0;
  color: #1F285F;
  font-size: 20px;
  font-weight: 600;
}

.btn-toggle-filters {
  padding: 8px 16px;
  background: #f0f0f0;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-toggle-filters:hover {
  background: #e0e0e0;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
}

.filter-group label {
  margin-bottom: 6px;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.filter-input {
  padding: 10px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.filter-input:focus {
  outline: none;
  border-color: #0288d1;
}

.filter-actions {
  display: flex;
  gap: 8px;
  grid-column: span 2;
}

.btn-apply,
.btn-clear,
.btn-refresh {
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-apply {
  background: #0288d1;
  color: white;
  border: 2px solid #0288d1;
}

.btn-apply:hover {
  background: #0277bd;
}

.btn-clear,
.btn-refresh {
  background: white;
  color: #666;
  border: 2px solid #e0e0e0;
}

.btn-clear:hover,
.btn-refresh:hover:not(:disabled) {
  background: #f8f8f8;
}

.btn-refresh:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.logs-table-container {
  overflow-x: auto;
}

.logs-table {
  width: 100%;
  border-collapse: collapse;
}

.logs-table thead {
  background: #f8f9fa;
}

.logs-table th {
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #555;
  border-bottom: 2px solid #dee2e6;
  white-space: nowrap;
}

.logs-table td {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.logs-table tbody tr {
  transition: background 0.2s;
}

.logs-table tbody tr:hover {
  background: #f8f9fa;
}

.log-error {
  background: #ffebee !important;
}

.log-warning {
  background: #fff3e0 !important;
}

.date-cell {
  white-space: nowrap;
  font-family: monospace;
  font-size: 13px;
}

.description-cell {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  text-transform: uppercase;
}

.action-create { background: #d4edda; color: #155724; }
.action-update { background: #d1ecf1; color: #0c5460; }
.action-delete { background: #f8d7da; color: #721c24; }
.action-login { background: #cce5ff; color: #004085; }
.action-logout { background: #e2e3e5; color: #383d41; }
.action-import { background: #fff3cd; color: #856404; }
.action-error { background: #f8d7da; color: #721c24; }
.action-default { background: #e9ecef; color: #495057; }

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-weight: 600;
  color: #333;
}

.user-email {
  font-size: 12px;
  color: #666;
}

.no-user {
  color: #999;
  font-style: italic;
}

.entity-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.entity-type {
  font-weight: 600;
  color: #333;
}

.entity-id {
  font-size: 12px;
  color: #666;
  font-family: monospace;
}

.ip-cell {
  font-family: monospace;
  font-size: 13px;
}

.btn-details {
  padding: 6px 12px;
  background: white;
  color: #0288d1;
  border: 2px solid #0288d1;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-details:hover {
  background: #0288d1;
  color: white;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 2px solid #f0f0f0;
}

.btn-page {
  padding: 8px 16px;
  background: white;
  color: #0288d1;
  border: 2px solid #0288d1;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-page:hover:not(:disabled) {
  background: #0288d1;
  color: white;
}

.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-weight: 600;
  color: #666;
}

.loading,
.error,
.no-data {
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

.modal-content {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(31, 40, 95, 0.3);
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  max-height: 80vh;
  overflow-y: auto;
}

.log-details-modal {
  max-width: 800px;
  width: 90%;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 2px solid #f0f0f0;
}

.modal-header h2 {
  margin: 0;
  color: #1F285F;
  font-size: 24px;
  font-weight: 700;
}

.btn-close {
  background: none;
  border: none;
  font-size: 32px;
  cursor: pointer;
  color: #666;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.btn-close:hover {
  background: #f0f0f0;
  color: #333;
}

.modal-body {
  padding: 24px;
}

.detail-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.detail-label {
  font-weight: 700;
  color: #1F285F;
  min-width: 140px;
}

.detail-value {
  flex: 1;
  color: #333;
}

.small-text {
  font-size: 13px;
  word-break: break-all;
}

.details-json {
  flex-direction: column;
  gap: 8px;
}

.json-details {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.6;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
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
</style>
