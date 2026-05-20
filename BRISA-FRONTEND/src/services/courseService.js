import api from './api';

export const courseService = {
  async getAll() {
    try {
      const response = await api.get('/courses');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getById(id) {
    try {
      const response = await api.get(`/courses/${id}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getProgressionsByClassId(classId) {
    try {
      const response = await api.get(`/courses/progressions/class/${classId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async importProgressionsFromExcel(classId, formData) {
    try {
      const response = await api.post(`/courses/progressions/class/${classId}/import/excel`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // ✅ Novo: envia alerta por email para alunos pendentes de um curso em uma turma
  // Endpoint a ser implementado na Tela 5
  async sendAlert(courseId, classId, payload) {
    try {
      const response = await api.post(`/courses/${courseId}/alert/class/${classId}`, payload);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
  ,

  // GET backfill suggestions for a class
  async getBackfill(classId) {
    try {
      const response = await api.get('/courses/backfill', { params: { classId } });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Assign a course to a class (creates progressions for enrolled students)
  async assignCourseToClass(courseId, classId, required = true) {
    try {
      const response = await api.post(`/courses/${courseId}/assign/class/${classId}`, null, { params: { required } });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Remove a course from a class (deletes progressions for that class/course)
  async removeCourseFromClass(courseId, classId) {
    try {
      const response = await api.delete(`/courses/${courseId}/assign/class/${classId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
  ,

  // Create a single course
  async create(course) {
    try {
      const response = await api.post('/courses', course);
      return response.data;
    } catch (error) {
      throw error;
    }
  },
    // Get assignments for a class (includes required flag)
    async getAssignmentsByClassId(classId) {
      try {
        const response = await api.get(`/courses/assignments/class/${classId}`);
        return response.data;
      } catch (error) {
        throw error;
      }
    },

  async importCoursesToClassFromExcel(classId, formData) {
    try {
      const response = await api.post(`/courses/assignments/class/${classId}/import/excel`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Bulk import courses (frontend parses Excel into DTOs and posts JSON array)
  async importBulk(courseDtos) {
    try {
      const response = await api.post('/courses/bulk', courseDtos);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
,
  // Update an existing course
  async update(courseId, course) {
    try {
      const response = await api.put(`/courses/${courseId}`, course);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Delete a course
  async delete(courseId) {
    try {
      const response = await api.delete(`/courses/${courseId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};