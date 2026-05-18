<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content modal-large">
      <div class="modal-header">
        <h2>Cadastrar Novo Grupo</h2>
        <button class="close-btn" @click="closeModal">✕</button>
      </div>

      <div class="modal-body">
        <form @submit.prevent="submitForm">
          <!-- Nome do Projeto -->
          <div class="form-group">
            <label for="projectTheme">Nome do Projeto *</label>
            <input
              id="projectTheme"
              v-model="form.projectTheme"
              type="text"
              placeholder="Digite o nome do projeto"
              required
            />
          </div>

          <!-- Resumo do Projeto -->
          <div class="form-group">
            <label for="description">Resumo do Projeto *</label>
            <textarea
              id="description"
              v-model="form.description"
              placeholder="Descreva o projeto"
              rows="4"
              required
            ></textarea>
          </div>

          <!-- Empresa/Instituição -->
          <div class="form-group">
            <label for="projectCompanyId">Empresa/Instituição</label>
            <select v-model="form.projectCompanyId">
              <option :value="null">Selecione uma empresa (opcional)</option>
              <option v-for="company in companies" :key="company.id" :value="company.id">
                {{ company.name }}
              </option>
            </select>
          </div>

          <!-- Orientador -->
          <div class="form-group">
            <label for="leaderId">Orientador do Projeto *</label>
            <input
              id="leaderId-search"
              v-model="leaderSearch"
              type="text"
              placeholder="Buscar orientador"
              @input="searchLeaders"
            />
            <select v-model="form.leaderId" required>
              <option :value="null">Selecione o orientador</option>
              <option v-for="leader in filteredLeaders" :key="leader.id" :value="leader.id">
                {{ leader.name }}
              </option>
            </select>
          </div>

          <!-- Seleção de Alunos -->
          <div class="form-group">
            <label for="memberIds">Alunos da Etapa de Imersão *</label>
            <div class="multi-select-wrapper">
              <input
                id="memberSearch"
                v-model="memberSearch"
                type="text"
                placeholder="Buscar aluno"
                @input="searchMembers"
              />
              <div class="multi-select-list">
                <label v-for="member in filteredMembers" :key="member.id" class="checkbox-label">
                  <input
                    v-model="form.memberIds"
                    type="checkbox"
                    :value="member.id"
                  />
                  {{ member.name }}
                </label>
              </div>
            </div>
            <p class="selected-count">{{ form.memberIds.length }} aluno(s) selecionado(s)</p>
          </div>

          <!-- Primeira Data de Reunião -->
          <div class="form-row">
            <div class="form-group">
              <label for="firstMeetingDate">Primeira Data de Reunião *</label>
              <input
                id="firstMeetingDate"
                v-model="form.firstMeetingDate"
                type="date"
                required
              />
            </div>

            <!-- Dia da Semana -->
            <div class="form-group">
              <label for="weeklyMeetingDay">Dia da Semana para Reuniões *</label>
              <select v-model="form.weeklyMeetingDay" required>
                <option :value="null">Selecione o dia</option>
                <option value="1">Segunda-feira</option>
                <option value="2">Terça-feira</option>
                <option value="3">Quarta-feira</option>
                <option value="4">Quinta-feira</option>
                <option value="5">Sexta-feira</option>
                <option value="6">Sábado</option>
                <option value="0">Domingo</option>
              </select>
            </div>
          </div>

          <!-- Repository Link (opcional) -->
          <div class="form-group">
            <label for="repositoryLink">Link do Repositório (opcional)</label>
            <input
              id="repositoryLink"
              v-model="form.repositoryLink"
              type="url"
              placeholder="https://github.com/..."
            />
          </div>

          <!-- Botões -->
          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="closeModal">Cancelar</button>
            <button type="submit" class="btn-primary" :disabled="isLoading">
              {{ isLoading ? 'Criando...' : 'Criar Grupo' }}
            </button>
          </div>
        </form>
      </div>

      <!-- Mensagem de Erro -->
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
    </div>
  </div>
</template>

<script>
import { groupService } from '@/services/groupService';
import { peopleService } from '@/services/peopleService';
import { institutionService } from '@/services/institutionService';

export default {
  name: 'GroupCreateModal',
  props: {
    isOpen: Boolean,
    classId: { type: [Number, String], default: null },
  },
  emits: ['close', 'group-created'],
  data() {
    return {
      form: {
        projectTheme: '',
        description: '',
        projectCompanyId: null,
        leaderId: null,
        memberIds: [],
        weeklyMeetingDay: null,
        firstMeetingDate: '',
        repositoryLink: '',
      },
      leaders: [],
      filteredLeaders: [],
      leaderSearch: '',
      members: [],
      filteredMembers: [],
      memberSearch: '',
      companies: [],
      isLoading: false,
      errorMessage: '',
    };
  },
  watch: {
    isOpen(newVal) {
      if (newVal) {
        this.loadData();
      } else {
        this.resetForm();
      }
    },
  },
  methods: {
    async loadData() {
      try {
        if (!this.classId) {
          this.errorMessage = 'Classe não selecionada';
          return;
        }
        // Carregar orientadores (qualquer pessoa)
        const peopleResponse = await peopleService.getAll();
        this.leaders = peopleResponse.data || [];
        this.filteredLeaders = this.leaders;

        // Carregar alunos na etapa de imersão desta classe
        const membersResponse = await groupService.getImmersionStudents(this.classId);
        this.members = membersResponse.data || [];
        this.filteredMembers = this.members;

        // Carregar empresas/instituições
        const companiesResponse = await institutionService.getAll();
        this.companies = companiesResponse.data || [];
      } catch (error) {
        this.errorMessage = 'Erro ao carregar dados: ' + (error.response?.data?.error || error.message);
      }
    },

    searchLeaders() {
      if (this.leaderSearch.trim() === '') {
        this.filteredLeaders = this.leaders;
      } else {
        const query = this.leaderSearch.toLowerCase();
        this.filteredLeaders = this.leaders.filter(l => l.name.toLowerCase().includes(query));
      }
    },
    searchMembers() {
      if (this.memberSearch.trim() === '') {
        this.filteredMembers = this.members;
      } else {
        const query = this.memberSearch.toLowerCase();
        this.filteredMembers = this.members.filter(m => m.name.toLowerCase().includes(query));
      }
    },
    async submitForm() {
      // Validações
      if (!this.form.projectTheme.trim()) {
        this.errorMessage = 'Nome do projeto é obrigatório';
        return;
      }
      if (!this.form.description.trim()) {
        this.errorMessage = 'Resumo do projeto é obrigatório';
        return;
      }
      if (!this.form.leaderId) {
        this.errorMessage = 'Selecione um orientador';
        return;
      }
      if (this.form.memberIds.length === 0) {
        this.errorMessage = 'Selecione pelo menos um aluno';
        return;
      }
      if (!this.form.firstMeetingDate) {
        this.errorMessage = 'Selecione a primeira data de reunião';
        return;
      }
      if (!this.form.weeklyMeetingDay) {
        this.errorMessage = 'Selecione o dia da semana';
        return;
      }

      this.isLoading = true;
      this.errorMessage = '';

      try {
        const response = await groupService.createGroup(this.classId, {
          projectTheme: this.form.projectTheme,
          description: this.form.description,
          projectCompanyId: this.form.projectCompanyId,
          leaderId: this.form.leaderId,
          memberIds: this.form.memberIds,
          weeklyMeetingDay: parseInt(this.form.weeklyMeetingDay),
          firstMeetingDate: this.form.firstMeetingDate,
          repositoryLink: this.form.repositoryLink,
        });

        this.$emit('group-created', response.data);
        this.closeModal();
      } catch (error) {
        this.errorMessage = error.response?.data?.error || 'Erro ao criar grupo';
      } finally {
        this.isLoading = false;
      }
    },
    closeModal() {
      this.resetForm();
      this.$emit('close');
    },
    resetForm() {
      this.form = {
        projectTheme: '',
        description: '',
        projectCompanyId: null,
        leaderId: null,
        memberIds: [],
        weeklyMeetingDay: null,
        firstMeetingDate: '',
        repositoryLink: '',
      };
      this.errorMessage = '';
      this.leaderSearch = '';
      this.memberSearch = '';
    },
  },
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-large {
  max-width: 800px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

input[type="text"],
input[type="url"],
input[type="date"],
select,
textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-family: inherit;
  font-size: 14px;
}

input[type="text"]:focus,
input[type="url"]:focus,
input[type="date"]:focus,
select:focus,
textarea:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

textarea {
  resize: vertical;
}

.multi-select-wrapper {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
}

.multi-select-list {
  max-height: 200px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: normal;
  margin: 0;
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  margin: 0;
}

.selected-count {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e0e0e0;
}

.btn-cancel,
.btn-primary {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.btn-cancel {
  background-color: #e0e0e0;
  color: #333;
}

.btn-cancel:hover {
  background-color: #d0d0d0;
}

.btn-primary {
  background-color: #2563eb;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #1d4ed8;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  color: #dc2626;
  background-color: #fee2e2;
  border: 1px solid #fca5a5;
  padding: 12px;
  border-radius: 4px;
  margin: 12px 20px 20px;
  font-size: 14px;
}
</style>
