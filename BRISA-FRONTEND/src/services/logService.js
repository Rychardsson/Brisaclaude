import api from './api';

export const logService = {
  // Buscar logs com filtros
  async getLogs(filters = {}) {
    try {
      const params = new URLSearchParams();
      
      if (filters.action) params.append('action', filters.action);
      if (filters.userId) params.append('userId', filters.userId);
      if (filters.entityType) params.append('entityType', filters.entityType);
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
      if (filters.page !== undefined) params.append('page', filters.page);
      if (filters.size !== undefined) params.append('size', filters.size);
      if (filters.sortBy) params.append('sortBy', filters.sortBy);
      if (filters.sortDirection) params.append('sortDirection', filters.sortDirection);
      
      const response = await api.get(`/logs?${params.toString()}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Buscar log por ID
  async getById(id) {
    try {
      const response = await api.get(`/logs/${id}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Buscar logs recentes
  async getRecentLogs(limit = 20) {
    try {
      const response = await api.get(`/logs/recent?limit=${limit}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Buscar estatísticas de logs
  async getStats() {
    try {
      const response = await api.get('/logs/stats');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Limpar logs antigos
  async cleanupOldLogs(daysToKeep = 90) {
    try {
      const response = await api.delete(`/logs/cleanup?daysToKeep=${daysToKeep}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};
