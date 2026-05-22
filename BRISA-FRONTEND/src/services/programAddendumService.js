import api from './api';

export const programAddendumService = {
  async listByProgram(programId) {
    const response = await api.get(`/programs/${programId}/addendums`);
    return response.data;
  },

  async create(programId, payload) {
    const response = await api.post(`/programs/${programId}/addendums`, payload);
    return response.data;
  },

  async update(programId, addendumId, payload) {
    const response = await api.put(`/programs/${programId}/addendums/${addendumId}`, payload);
    return response.data;
  },

  async delete(programId, addendumId) {
    await api.delete(`/programs/${programId}/addendums/${addendumId}`);
  }
};
