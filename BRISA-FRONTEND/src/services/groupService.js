import api from './api';

export const groupService = {
  createGroup(classId, data) {
    return api.post(`/classes/${classId}/groups`, data);
  },

  getGroupsByClass(classId) {
    return api.get(`/classes/${classId}/groups`);
  },

  getGroupDetail(classId, groupId) {
    return api.get(`/classes/${classId}/groups/${groupId}`);
  },

  updateGroup(classId, groupId, data) {
    return api.put(`/classes/${classId}/groups/${groupId}`, data);
  },

  deleteGroup(classId, groupId) {
    return api.delete(`/classes/${classId}/groups/${groupId}`);
  },

  getImmersionStudents(classId) {
    // Buscar alunos que estão na etapa de imersão
    // Para isso, precisamos buscar todos os stages e filtrar pela classe
    return api.get(`/classes/${classId}/immersion-students`);
  },
};
