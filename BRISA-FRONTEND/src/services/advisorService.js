import api from './api';

export const advisorService = {
  async create(payload) {
    try {
      const response = await api.post('/advisors', payload);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getAll() {
    try {
      const response = await api.get('/advisors');
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};
