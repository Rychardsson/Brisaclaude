import api from './api';

export const academicRoleService = {
  async getAll() {
    const response = await api.get('/academic-roles');
    return response.data;
  }
};
