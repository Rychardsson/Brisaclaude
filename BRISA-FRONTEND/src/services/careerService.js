import api from './api';

export const careerService = {
  async getFollowUps(params = {}) {
    try {
      const response = await api.get('/career/follow-ups', { params });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async createFollowUp(payload) {
    try {
      const response = await api.post('/career/follow-ups', payload);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getAutomationSettings() {
    try {
      const response = await api.get('/career/automation');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async updateAutomationSettings(payload) {
    try {
      const response = await api.put('/career/automation', payload);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async sendAutomationTest(payload) {
    try {
      const response = await api.post('/career/automation/test', payload);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};
