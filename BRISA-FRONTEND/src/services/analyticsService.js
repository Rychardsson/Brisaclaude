import api from './api';

export const analyticsService = {
  async getCourseProgressionFunnel(classId, options = {}) {
    try {
      const params = { classId };
      if (options.stageId) params.stageId = options.stageId;
      if (options.stage) params.stage = options.stage;

      const response = await api.get('/analytics/course-progression-funnel', { params });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getCourseCompletions(options = {}) {
    try {
      const params = {};
      if (options.classId) params.classId = options.classId;
      if (options.stageId) params.stageId = options.stageId;
      if (options.granularity) params.granularity = options.granularity;

      const response = await api.get('/analytics/course-completions', { params });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getClassStatusReport(classId) {
    try {
      const response = await api.get('/analytics/class-status', { params: { classId } });
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};
