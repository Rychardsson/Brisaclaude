<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="closeModal">
    <div class="modal-card modal-card-large create-modal" @click.stop>
      <div class="modal-head">
        <div>
          <h2>Cadastrar Novo Grupo</h2>
          <p class="modal-subtitle">Preencha os dados do grupo de imersão.</p>
        </div>
        <button type="button" class="modal-close" @click="closeModal" aria-label="Fechar">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>

      <form class="create-form" @submit.prevent="submitForm">
        <div class="modal-body">
          <div class="modal-section">
            <h3 class="modal-section-title">Dados do projeto</h3>
            <div class="form-grid">
              <div class="form-group full-width">
                <label for="projectTheme">Nome do Projeto *</label>
                <input id="projectTheme" v-model="form.projectTheme" type="text" placeholder="Digite o nome do projeto" required />
              </div>

              <div class="form-group full-width">
                <label for="description">Resumo do Projeto *</label>
                <textarea id="description" v-model="form.description" placeholder="Descreva o projeto" rows="4" required></textarea>
              </div>

              <div class="form-group full-width">
                <label for="sponsorCompany">Empresa do Grupo</label>
                <input id="sponsorCompany" v-model="form.sponsorCompany" type="text" placeholder="Digite o nome da empresa patrocinadora" required />
              </div>

              <div class="form-group">
                <label for="leaderId">Orientador do Projeto *</label>
                <input id="leaderId-search" v-model="leaderSearch" type="text" placeholder="Buscar orientador" @input="searchLeaders" />
                <select v-model="form.leaderId" required>
                  <option :value="null">Selecione o orientador</option>
                  <option v-for="leader in filteredLeaders" :key="leader.id" :value="leader.id">{{ leader.name }}</option>
                </select>
              </div>

              <div class="form-group">
                <label for="memberIds">Alunos da Etapa de Imersão *</label>
                <div class="multi-select-wrapper">
                  <input id="memberSearch" v-model="memberSearch" type="text" placeholder="Buscar aluno" @input="searchMembers" />
                  <div class="multi-select-list">
                    <label v-for="member in filteredMembers" :key="member.id" class="checkbox-label">
                      <input v-model="form.memberIds" type="checkbox" :value="member.id" />
                      {{ member.name }}
                    </label>
                  </div>
                </div>
                <p class="selected-count">{{ form.memberIds.length }} aluno(s) selecionado(s)</p>
              </div>

              <div class="form-group">
                <label for="firstMeetingDate">Primeira Data de Reunião *</label>
                <input id="firstMeetingDate" v-model="form.firstMeetingDate" type="date" required />
              </div>

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

              <div class="form-group full-width">
                <label for="repositoryLink">Link do Repositório (opcional)</label>
                <input id="repositoryLink" v-model="form.repositoryLink" type="url" placeholder="https://github.com/..." />
              </div>
            </div>
          </div>
        </div>

        <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

        <div class="modal-actions">
          <button type="button" class="secondary-btn modal-secondary" @click="closeModal">Cancelar</button>
          <button type="submit" class="primary-btn" :disabled="submitDisabled" :aria-disabled="submitDisabled">{{ isLoading ? 'Criando...' : 'Criar Grupo' }}</button>
        </div>
      </form>
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
        sponsorCompany: '',
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
  computed: {
    canSubmit() {
      const themeOk = this.form.projectTheme && this.form.projectTheme.trim().length > 0;
      const descriptionOk = this.form.description && this.form.description.trim().length > 0;
      const sponsorOk = this.form.sponsorCompany && this.form.sponsorCompany.trim().length > 0;
      const leaderOk = !!this.form.leaderId;
      const membersOk = Array.isArray(this.form.memberIds) && this.form.memberIds.length > 0;
      const dateOk = !!this.form.firstMeetingDate;
      const dayOk = this.form.weeklyMeetingDay !== null && this.form.weeklyMeetingDay !== '';
      return themeOk && descriptionOk && sponsorOk && leaderOk && membersOk && dateOk && dayOk;
    },
    submitDisabled() {
      return this.isLoading || !this.canSubmit;
    }
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
        this.leaders = Array.isArray(peopleResponse) ? peopleResponse : [];
        this.filteredLeaders = this.leaders;

        // Carregar alunos na etapa de imersão desta classe
        const membersResponse = await groupService.getImmersionStudents(this.classId);
        this.members = Array.isArray(membersResponse) ? membersResponse : [];
        this.filteredMembers = this.members;

        // Carregar empresas/instituições (mantém lista para compatibilidade se necessário)
        const companiesResponse = await institutionService.getAll();
        this.companies = Array.isArray(companiesResponse) ? companiesResponse : [];
      } catch (error) {
        this.errorMessage = 'Erro ao carregar dados: ' + (error.response?.data?.message || error.response?.data?.error || error.message);
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
      if (!this.form.sponsorCompany || !this.form.sponsorCompany.trim()) {
        this.errorMessage = 'Empresa do Grupo é obrigatória';
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
        const createdGroup = await groupService.createGroup(this.classId, {
          projectTheme: this.form.projectTheme,
          description: this.form.description,
          sponsorCompany: this.form.sponsorCompany,
          projectCompanyId: this.form.projectCompanyId,
          leaderId: this.form.leaderId,
          memberIds: this.form.memberIds,
          weeklyMeetingDay: parseInt(this.form.weeklyMeetingDay),
          firstMeetingDate: this.form.firstMeetingDate,
          repositoryLink: this.form.repositoryLink,
        });

        this.$emit('group-created', createdGroup);
        this.closeModal();
      } catch (error) {
        this.errorMessage = error.response?.data?.message || error.response?.data?.error || 'Erro ao criar grupo';
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
/* Reuse project create-modal styles to match People modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(8, 15, 28, 0.56);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 18px;
}

.modal-card {
  width: min(760px, 100%);
  max-height: 92vh;
  overflow: auto;
  background: #fff;
  border-radius: 20px;
  padding: 22px;
  box-shadow: 0 22px 60px rgba(8, 15, 28, 0.28);
}

.modal-card-large { width: min(980px, 100%); }

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.modal-head h2 { font-size: 20px; font-weight: 600; color: #0f172a; margin: 0; }
.modal-subtitle { margin: 4px 0 0; color: #6a7a90; font-size: 14px; }

.modal-close {
  width: 32px; height: 32px; border-radius: 10px; border: 1px solid #e2e8f0;
  background: #fff; color: #64748b; display: inline-grid; place-items: center; cursor: pointer;
}
.modal-close:hover { background: #f8fafc; border-color: #cbd5e1; }

.create-form { display: flex; flex-direction: column; min-height: 0; }
.modal-body { padding: 16px 22px 8px; flex: 1 1 auto; min-height: 0; overflow: auto; }
.modal-section + .modal-section { margin-top: 18px; padding-top: 16px; border-top: 1px solid #e2eaf2; }
.modal-section-title { margin: 0 0 12px; color: #0f172a; font-size: 18px; font-weight: 600; }

.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px 18px; }
.form-group { display: flex; flex-direction: column; gap: 7px; }
.full-width { grid-column: 1 / -1; }

.form-group label { font-size: 14px; font-weight: 500; color: #334155; }
.form-group input, .form-group select, textarea { font-size: 16px; font-weight: 400; color: #0f172a; }

.form-group input, .form-group select, textarea {
  width: 100%; box-sizing: border-box; height: 42px; border-radius: 12px; border: 1px solid #d7e1eb; padding: 0 12px; color: #13233f; background: #fff; outline: none;
}

.form-group textarea { height: auto; min-height: 84px; padding: 10px 12px; }
.form-group input:focus, .form-group select:focus, textarea:focus { border-color: #14b8a6; box-shadow: 0 0 0 4px rgba(20,184,166,0.1); }

/* multi-select specific */
.multi-select-wrapper { border: 1px solid #d7e1eb; border-radius: 12px; padding: 8px; }
.multi-select-list { max-height: 200px; overflow-y: auto; display:flex; flex-direction:column; gap:8px; margin-top:10px; }
.checkbox-label { display:flex; align-items:center; gap:8px; cursor:pointer; font-weight:400; margin:0; }
.checkbox-label input[type="checkbox"] { width:auto; margin:0; }
.selected-count { margin-top:8px; font-size:12px; color:#666; }

/* Actions */
.modal-actions { margin-top: 0; padding: 14px 22px 18px; border-top: 1px solid #e2eaf2; display:flex; justify-content:flex-end; gap:10px; }

.primary-btn, .secondary-btn { height:40px; border-radius:12px; border:0; padding:0 14px; font-weight:700; cursor:pointer; }
.primary-btn { background:#14b8a6; color:#fff; box-shadow:0 8px 16px rgba(20,184,166,0.2); }
.primary-btn:hover { background:#0d9488; }
.secondary-btn { background:#eef2f7; color:#13233f; }

.modal-secondary { background:#fff; border:1px solid #d7e1eb; color:#1f2a3d; }

/* make buttons visually disabled when needed */
.primary-btn:disabled, .primary-btn[disabled] { opacity:0.45; cursor:not-allowed; pointer-events:none; box-shadow:none; transform:none; filter:grayscale(6%); }

/* Alerts */
.alert { margin-top:14px; padding:12px 14px; border-radius:12px; display:flex; gap:10px; align-items:flex-start; }
.alert-error { background:#fff0f0; color:#b42318; }

@media (max-width:720px) { .form-grid { grid-template-columns: 1fr; } }

/* Ensure inputs inside this modal aren't overridden by legacy rules */
.create-modal .form-group input, .create-modal .form-group select, .create-modal textarea { display:block !important; width:100% !important; box-sizing:border-box !important; }

</style>
