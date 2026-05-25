import api from './api';

export const advisorService = {
  async create(payload) {
    const response = await api.post('/advisors', payload);
    return response.data;
  },

  async getAll(roleType = null) {
    const response = await api.get('/advisors', {
      params: roleType ? { roleType } : {}
    });
    return response.data;
  },

  async getById(id) {
    const response = await api.get(`/advisors/${id}`);
    return response.data;
  },

  async update(id, payload) {
    const response = await api.put(`/advisors/${id}`, payload);
    return response.data;
  },

  async delete(id) {
    await api.delete(`/advisors/${id}`);
  }
};
