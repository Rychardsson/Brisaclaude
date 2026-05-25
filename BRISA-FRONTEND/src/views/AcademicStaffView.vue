<template>
  <div class="staff-page">
    <div class="staff-shell">
      <section class="page-header-card">
        <div class="page-header-top">
          <div class="header-content">
            <h1>Orientadores e Gestores</h1>
            <p class="subtitle">
              Centralize o cadastro da equipe acadêmica, acompanhe perfis ativos e mantenha gestores e orientadores acessíveis para as operações do programa.
            </p>
          </div>

          <div class="top-actions">
            <button type="button" class="ghost-btn" @click="loadMembers">
              Atualizar lista
            </button>
            <button type="button" class="primary-btn" @click="openCreateModal">
              <Plus :size="18" />
              Novo membro
            </button>
          </div>
        </div>

        <div class="stats-grid">
          <article class="stat-card stat-card-primary">
            <div class="stat-header">
              <span class="stat-label">Equipe cadastrada</span>
              <Users :size="18" />
            </div>
            <div class="stat-value">{{ formatNumber(totalMembers) }}</div>
            <div class="stat-note">Base geral da equipe acadêmica</div>
          </article>

          <article class="stat-card stat-card-success">
            <div class="stat-header">
              <span class="stat-label">Orientadores</span>
              <GraduationCap :size="18" />
            </div>
            <div class="stat-value">{{ formatNumber(advisorsCount) }}</div>
            <div class="stat-note">Orientadores vinculados ou disponíveis</div>
          </article>

          <article class="stat-card stat-card-warning">
            <div class="stat-header">
              <span class="stat-label">Gestores</span>
              <BriefcaseBusiness :size="18" />
            </div>
            <div class="stat-value">{{ formatNumber(managersCount) }}</div>
            <div class="stat-note">Perfis de coordenação e gestão</div>
          </article>

          <article class="stat-card stat-card-teal">
            <div class="stat-header">
              <span class="stat-label">Ativos</span>
              <ShieldCheck :size="18" />
            </div>
            <div class="stat-value">{{ formatNumber(activeMembersCount) }}</div>
            <div class="stat-note">Disponíveis para uso imediato</div>
          </article>
        </div>
      </section>

      <section class="table-card">
        <div class="tabs-section">
          <div class="tabs-bar">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              type="button"
              class="tab-item"
              :class="{ active: activeTab === tab.id }"
              @click="activeTab = tab.id"
            >
              <span class="tab-label">{{ tab.label }}</span>
              <span class="tab-count">{{ tab.count }}</span>
            </button>
          </div>
        </div>

        <div class="search-section">
          <div class="filters-row">
            <div class="search-box">
              <Search class="search-icon" :size="18" />
              <input
                v-model="searchTerm"
                type="text"
                placeholder="Buscar por nome, CPF, email ou formação..."
                class="search-input"
              />
            </div>

            <div class="filters-actions">
              <select v-model="statusFilter" class="filter-select">
                <option value="all">Todos os status</option>
                <option value="active">Ativos</option>
                <option value="inactive">Inativos</option>
              </select>
            </div>
          </div>
        </div>

        <div class="table-section">
          <div v-if="loading" class="state-row">
            <div class="spinner"></div>
            <span>Carregando equipe acadêmica...</span>
          </div>

          <div v-else-if="errorMessage" class="state-row state-row-error">
            <span>{{ errorMessage }}</span>
          </div>

          <div v-else-if="filteredMembers.length === 0" class="state-row state-row-empty">
            <div>
              <strong>Nenhum membro encontrado</strong>
              <p>Ajuste os filtros ou cadastre um novo orientador ou gestor.</p>
            </div>
          </div>

          <div v-else class="table-scroll">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>CPF</th>
                  <th>Perfil</th>
                  <th>Contato</th>
                  <th>Formação</th>
                  <th>Nascimento</th>
                  <th>Status</th>
                  <th class="actions-column">Ações</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="member in filteredMembers" :key="member.id">
                  <td>
                    <div class="person-cell">
                      <div class="person-avatar">{{ initials(member.name) }}</div>
                      <div>
                        <strong>{{ member.name }}</strong>
                        <small>{{ roleDescription(member.roleType) }}</small>
                      </div>
                    </div>
                  </td>
                  <td>{{ formatCpf(member.cpf) }}</td>
                  <td>
                    <span class="role-badge" :class="roleBadgeClass(member.roleType)">
                      {{ roleLabel(member.roleType) }}
                    </span>
                  </td>
                  <td>{{ member.email || '-' }}</td>
                  <td>{{ member.formation || '-' }}</td>
                  <td>{{ formatDate(member.birthDate) }}</td>
                  <td>
                    <span class="status-badge" :class="member.active ? 'status-active' : 'status-inactive'">
                      {{ member.active ? 'Ativo' : 'Inativo' }}
                    </span>
                  </td>
                  <td class="actions-column">
                    <button type="button" class="row-action-btn" @click="openEditModal(member)">
                      <Pencil :size="15" />
                      Editar
                    </button>
                    <button type="button" class="row-action-btn row-action-danger" @click="removeMember(member)">
                      <Trash2 :size="15" />
                      Excluir
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </div>

    <NewAdvisorModal
      v-if="showModal"
      :member="editingMember"
      :show-role-field="true"
      @close="closeModal"
      @created="handleCreated"
      @updated="handleUpdated"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { BriefcaseBusiness, GraduationCap, Pencil, Plus, Search, ShieldCheck, Trash2, Users } from 'lucide-vue-next';
import NewAdvisorModal from '@/components/NewAdvisorModal.vue';
import { advisorService } from '@/services/advisorService';

const members = ref([]);
const loading = ref(false);
const errorMessage = ref('');
const showModal = ref(false);
const editingMember = ref(null);
const searchTerm = ref('');
const activeTab = ref('all');
const statusFilter = ref('all');

const totalMembers = computed(() => members.value.length);
const activeMembersCount = computed(() => members.value.filter((member) => member.active !== false).length);
const managersCount = computed(() => members.value.filter((member) => member.roleType === 'GESTOR').length);
const advisorsCount = computed(() => members.value.filter((member) => member.roleType === 'ORIENTADOR').length);

const tabs = computed(() => [
  { id: 'all', label: 'Todos', count: totalMembers.value },
  { id: 'ORIENTADOR', label: 'Orientadores', count: advisorsCount.value },
  { id: 'GESTOR', label: 'Gestores', count: managersCount.value }
]);

const filteredMembers = computed(() => {
  const term = String(searchTerm.value || '').trim().toLowerCase();

  return members.value.filter((member) => {
    const tabMatches = activeTab.value === 'all' || member.roleType === activeTab.value;
    const statusMatches = statusFilter.value === 'all'
      || (statusFilter.value === 'active' && member.active !== false)
      || (statusFilter.value === 'inactive' && member.active === false);

    const searchMatches = !term || [
      member.name,
      member.cpf,
      member.email,
      member.formation
    ].some((value) => String(value || '').toLowerCase().includes(term));

    return tabMatches && statusMatches && searchMatches;
  });
});

onMounted(() => {
  loadMembers();
});

async function loadMembers() {
  loading.value = true;
  errorMessage.value = '';

  try {
    members.value = await advisorService.getAll();
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Não foi possível carregar a equipe acadêmica.';
  } finally {
    loading.value = false;
  }
}

function openCreateModal() {
  editingMember.value = null;
  showModal.value = true;
}

function openEditModal(member) {
  editingMember.value = { ...member };
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
  editingMember.value = null;
}

async function handleCreated() {
  await loadMembers();
}

async function handleUpdated() {
  await loadMembers();
}

async function removeMember(member) {
  const confirmed = window.confirm(`Deseja excluir ${member.name}?`);
  if (!confirmed) return;

  try {
    await advisorService.delete(member.id);
    await loadMembers();
  } catch (error) {
    window.alert(error.response?.data?.message || 'Não foi possível excluir este membro.');
  }
}

function formatNumber(value) {
  return new Intl.NumberFormat('pt-BR').format(Number(value || 0));
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(`${value}T00:00:00`).toLocaleDateString('pt-BR');
}

function formatCpf(value) {
  const digits = String(value || '').replace(/\D/g, '');
  if (digits.length !== 11) return value || '-';
  return digits.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
}

function initials(name) {
  return String(name || '')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() || '')
    .join('') || 'EA';
}

function roleLabel(roleType) {
  const labels = {
    ORIENTADOR: 'Orientador',
    GESTOR: 'Gestor'
  };
  return labels[roleType] || 'Equipe acadêmica';
}

function roleDescription(roleType) {
  const descriptions = {
    ORIENTADOR: 'Acompanhamento de turma e projetos',
    GESTOR: 'Gestão operacional do programa'
  };
  return descriptions[roleType] || 'Equipe acadêmica';
}

function roleBadgeClass(roleType) {
  // Consolidar classes: orientador
  const isAdvisor = roleType === 'ORIENTADOR';
  return {
    'role-advisor': isAdvisor,
    'role-manager': roleType === 'GESTOR'
  };
}
</script>

<style scoped>
.staff-page {
  min-height: 100%;
  background: #eef3f8;
  padding: 14px 16px 20px;
}

.staff-shell {
  max-width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header-card,
.table-card,
.stat-card {
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(13, 27, 42, 0.05);
}

.page-header-card {
  padding: 18px 18px 16px;
}

.page-header-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.header-content {
  min-width: min(760px, 100%);
}

.header-content h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.1;
  color: #13233f;
  font-weight: 800;
}

.subtitle {
  margin: 6px 0 0;
  color: #6a7a90;
  font-size: 14px;
  line-height: 1.5;
}

.top-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.ghost-btn,
.primary-btn,
.row-action-btn {
  border: none;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 600;
}

.ghost-btn,
.primary-btn {
  height: 40px;
  padding: 0 16px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.ghost-btn {
  background: #fff;
  border: 1px solid #d1dbe8;
  color: #13233f;
}

.ghost-btn:hover {
  background: #f8fafc;
  border-color: #cfd9e6;
}

.primary-btn {
  background: #14b8a6;
  color: #fff;
  border: 1px solid #14b8a6;
  box-shadow: 0 8px 16px rgba(20, 184, 166, 0.20);
}

.primary-btn:hover {
  transform: translateY(-1px);
  background: #0d9488;
  border-color: #0d9488;
  box-shadow: 0 12px 20px rgba(13, 148, 136, 0.24);
}

.stats-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card { padding: 18px; }

.stat-card-primary {
  background: linear-gradient(180deg, #eff6ff 0%, #ffffff 100%);
}

.stat-card-success {
  background: linear-gradient(180deg, #ecfdf5 0%, #ffffff 100%);
}

.stat-card-warning {
  background: linear-gradient(180deg, #fff7ed 0%, #ffffff 100%);
}

.stat-card-teal {
  background: linear-gradient(180deg, #ecfeff 0%, #ffffff 100%);
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #3f5166;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.stat-value {
  margin-top: 16px;
  font-size: 34px;
  font-weight: 700;
  color: #0f172a;
}

.stat-note {
  margin-top: 6px;
  color: #5f7187;
  font-size: 13px;
}

.table-card {
  padding: 0;
  max-height: calc(100vh - 168px);
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #b8c6d8 transparent;
}

.table-card::-webkit-scrollbar {
  width: 8px;
}

.table-card::-webkit-scrollbar-thumb {
  background: #b8c6d8;
  border-radius: 999px;
}

.tabs-section {
  border-bottom: 1px solid #e2eaf2;
}

.search-section {
  background: #f8fafc;
  border-bottom: 1px solid #e2eaf2;
  padding: 16px 18px;
}

.table-section {
  padding: 0;
}

.tabs-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px 0;
  border-bottom: 1px solid #e2eaf2;
  overflow-x: auto;
}

.tab-item {
  border: none;
  background: transparent;
  color: #64748b;
  border-radius: 0;
  min-height: 44px;
  padding: 12px 16px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-weight: 600;
  border-bottom: 2px solid transparent;
  white-space: nowrap;
}

.tab-item.active {
  color: var(--teal-600);
  border-bottom-color: var(--teal-600);
}

.tab-count {
  min-width: 28px;
  height: 28px;
  padding: 0 8px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #eef2f7;
  color: #8a98ab;
  font-size: 11px;
  font-weight: 600;
}

.tab-item.active .tab-count {
  background: #ecfdf5;
  color: #0f766e;
}

.filters-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.search-box {
  flex: 1;
  min-width: 0;
  max-width: 100%;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #7a8aa0;
}

.search-input,
.filter-select {
  height: 48px;
  border-radius: 12px;
  border: 1px solid #d9e4ef;
  background: #fff;
  color: #11243d;
  font-size: 14px;
  outline: none;
}

.search-input {
  width: 100%;
  padding: 0 16px 0 42px;
}

.search-input:focus,
.filter-select:focus {
  border-color: #14b8a6;
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.10);
}

.filter-select {
  min-width: 180px;
  padding: 0 14px;
}

.filters-actions {
  display: flex;
  gap: 12px;
}

.state-row {
  padding: 40px 24px;
  display: flex;
  align-items: center;
  gap: 14px;
  color: #526277;
}

.state-row-empty {
  justify-content: center;
  text-align: center;
}

.state-row-error {
  color: #b42318;
}

.state-row strong {
  display: block;
  color: #0f172a;
  margin-bottom: 6px;
}

.spinner {
  width: 20px;
  height: 20px;
  border-radius: 999px;
  border: 3px solid #d7e3f1;
  border-top-color: #14b8a6;
  animation: spin 0.8s linear infinite;
}

.table-scroll {
  overflow-x: auto;
  padding: 0 18px 18px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1080px;
}

.data-table th,
.data-table td {
  padding: 18px 20px;
  border-bottom: 1px solid #edf2f7;
  text-align: left;
  vertical-align: middle;
}

.data-table th {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #68778c;
  font-weight: 700;
}

.person-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.person-avatar {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: linear-gradient(135deg, #1f285f 0%, #14b8a6 100%);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.person-cell strong {
  display: block;
  color: #0f172a;
}

.person-cell small {
  color: #6c7c91;
}

.role-badge,
.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}


.role-manager {
  background: #fef3c7;
  color: #b45309;
}

.role-advisor {
  background: #dcfce7;
  color: #15803d;
}

.status-active {
  background: #ecfdf5;
  color: #047857;
}

.status-inactive {
  background: #fef2f2;
  color: #b42318;
}

.actions-column {
  white-space: nowrap;
}

.row-action-btn {
  height: 36px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #fff;
  border: 1px solid #d8e2ee;
  color: #1f3552;
  margin-right: 8px;
  border-radius: 10px;
}

.row-action-btn:hover {
  background: #f8fafc;
  border-color: #cfd9e6;
}

.row-action-danger {
  color: #b42318;
  background: #fff5f5;
  border-color: #f3d0d2;
}

.row-action-danger:hover {
  background: #ffe9ea;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 840px) {
  .staff-shell {
    gap: 14px;
  }

  .page-header-top,
  .filters-row {
    flex-direction: column;
  }

  .filters-actions {
    width: 100%;
  }

  .filter-select {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .staff-page {
    padding: 12px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .header-content h1 {
    font-size: 28px;
  }

  .tabs-bar {
    flex-direction: column;
  }

  .tab-item {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
