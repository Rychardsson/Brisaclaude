import api from './api';

export const institutionService = {
  async getAll() {
    try {
      const response = await api.get('/institutions');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async getById(id) {
    try {
      const response = await api.get(`/institutions/${id}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async create(institutionData) {
    try {
      const response = await api.post('/institutions', institutionData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async update(id, institutionData) {
    try {
      const response = await api.put(`/institutions/${id}`, institutionData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  async delete(id) {
    try {
      await api.delete(`/institutions/${id}`);
    } catch (error) {
      throw error;
    }
  },

  async importExcel(file) {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await api.post('/institutions/import/excel', formData, {
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
