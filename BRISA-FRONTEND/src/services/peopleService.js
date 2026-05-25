import api from './api';

export const peopleService = {
  async getAll() {
    try {
      const response = await api.get('/people');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getById(id) {
    try {
      const response = await api.get(`/people/${id}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async create(peopleData) {
    try {
      const response = await api.post('/people', peopleData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async createLink(payload) {
    try {
      const response = await api.post('/people/link', payload);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async createOnly(payload) {
    try {
      const response = await api.post('/people/create-only', payload);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async linkExisting(payload) {
    try {
      const response = await api.post('/people/link-existing', payload);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getReferenceData() {
    try {
      const response = await api.get('/people/reference-data');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async update(id, peopleData) {
    try {
      const response = await api.put(`/people/${id}`, peopleData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async delete(id) {
    try {
      await api.delete(`/people/${id}`);
    } catch (error) {
      throw error;
    }
  },

  async importExcel(file, destination = {}) {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const params = {};
      ['programaId', 'turmaId', 'etapaId', 'statusInicial'].forEach((key) => {
        if (destination[key] !== undefined && destination[key] !== null && destination[key] !== '') {
          params[key] = destination[key];
        }
      });

      const response = await api.post('/people/import/excel', formData, {
        params,
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      return response.data;
    } catch (error) {
      throw error;
    }
  }
};
