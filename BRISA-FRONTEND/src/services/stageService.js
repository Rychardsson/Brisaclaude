import api from './api';

export const stageService = {
  // Stage methods
  async getAll() {
    try {
      const response = await api.get('/stages');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getById(id) {
    try {
      const response = await api.get(`/stages/${id}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getByClassId(classId) {
    try {
      const response = await api.get(`/stages/class/${classId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getCandidatesCountByClassId(classId) {
    try {
      const response = await api.get(`/stages/class/${classId}/candidates/count`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async create(stageData) {
    try {
      const response = await api.post('/stages', stageData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async update(id, stageData) {
    try {
      const response = await api.put(`/stages/${id}`, stageData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async delete(id) {
    try {
      await api.delete(`/stages/${id}`);
    } catch (error) {
      throw error;
    }
  },

  // Stage Candidate methods
  async getAllCandidates() {
    try {
      const response = await api.get('/stages/candidates');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getCandidateById(id) {
    try {
      const response = await api.get(`/stages/candidates/${id}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getCandidatesByStageId(stageId) {
    try {
      const response = await api.get(`/stages/${stageId}/candidates`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getCandidatesByPeopleId(peopleId) {
    try {
      const response = await api.get(`/stages/people/${peopleId}/candidates`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async addCandidate(candidateData) {
    try {
      const response = await api.post('/stages/candidates', candidateData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async updateCandidate(id, candidateData) {
    try {
      const response = await api.put(`/stages/candidates/${id}`, candidateData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async deleteCandidate(id) {
    try {
      await api.delete(`/stages/candidates/${id}`);
    } catch (error) {
      throw error;
    }
  },

  // Import candidates from Excel
  async importCandidates(stageId, formData) {
    try {
      const response = await api.post(`/stages/${stageId}/candidates/import/excel`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async importApprovedCandidates(stageId, formData) {
    try {
      const response = await api.post(`/stages/${stageId}/candidates/import/approved`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async convokeWaitlist(stageId, payload) {
    try {
      const response = await api.post(`/stages/${stageId}/waitlist/convoke`, payload);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};
