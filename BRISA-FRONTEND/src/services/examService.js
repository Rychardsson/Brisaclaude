import api from './api';

export const examService = {
  async importResults(classId, formData) {
    const response = await api.post(`/classes/${classId}/exams/import-results`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
  },

  async getSummary(classId) {
    const response = await api.get(`/classes/${classId}/exams/summary`);
    return response.data;
  },

  async getRespondentProfile(classId) {
    const response = await api.get(`/classes/${classId}/exams/respondent-profile`);
    return response.data;
  },

  async calculateApprovedRanking(classId, payload) {
    const response = await api.post(`/classes/${classId}/exams/approved-ranking`, payload);
    return response.data;
  }
};
