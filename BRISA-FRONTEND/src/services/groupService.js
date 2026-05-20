import api from './api';

export const groupService = {
  async createGroup(classId, data) {
    const response = await api.post(`/classes/${classId}/groups`, data);
    return response.data;
  },

  async getGroupsByClass(classId) {
    const response = await api.get(`/classes/${classId}/groups`);
    return response.data;
  },

  async getGroupDetail(classId, groupId) {
    const response = await api.get(`/classes/${classId}/groups/${groupId}`);
    return response.data;
  },

  async updateGroup(classId, groupId, data) {
    const response = await api.put(`/classes/${classId}/groups/${groupId}`, data);
    return response.data;
  },

  async deleteGroup(classId, groupId) {
    const response = await api.delete(`/classes/${classId}/groups/${groupId}`);
    return response.data;
  },

  async getImmersionStudents(classId) {
    const response = await api.get(`/classes/${classId}/immersion-students`);
    return response.data;
  },
};
