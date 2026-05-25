import api from './api';

export const courseEvaluationService = {
  async listByCourse(courseId) {
    const response = await api.get(`/courses/${courseId}/evaluations`);
    return response.data;
  },

  async create(courseId, payload) {
    const response = await api.post(`/courses/${courseId}/evaluations`, payload);
    return response.data;
  },

  async update(courseId, evaluationId, payload) {
    const response = await api.put(`/courses/${courseId}/evaluations/${evaluationId}`, payload);
    return response.data;
  },

  async delete(courseId, evaluationId) {
    await api.delete(`/courses/${courseId}/evaluations/${evaluationId}`);
  }
};
