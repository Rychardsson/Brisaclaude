<template>
  <div class="contracts-page">
    <div class="contracts-shell">
      <section class="page-header-card">
        <div class="page-header-top">
          <div>
            <h1>Contratos e Aditivos</h1>
            <p class="subtitle">
              Acompanhe a vigencia contratual dos programas, revise dados de fomento e mantenha os aditivos organizados em um fluxo proprio de backoffice.
            </p>
          </div>

          <div class="top-actions">
            <button type="button" class="ghost-btn" @click="loadContracts">Atualizar dados</button>
          </div>
        </div>

        <div class="stats-grid">
          <article class="stat-card stat-card-primary">
            <div class="stat-label">Contratos monitorados</div>
            <div class="stat-value">{{ formatNumber(totalContracts) }}</div>
            <div class="stat-note">Programas com dados contratuais</div>
          </article>

          <article class="stat-card stat-card-success">
            <div class="stat-label">Ativos</div>
            <div class="stat-value">{{ formatNumber(activeContractsCount) }}</div>
            <div class="stat-note">Dentro da vigencia atual</div>
          </article>

          <article class="stat-card stat-card-warning">
            <div class="stat-label">Aditivos</div>
            <div class="stat-value">{{ formatNumber(totalAddendums) }}</div>
            <div class="stat-note">Prorrogacoes e complementos registrados</div>
          </article>

          <article class="stat-card stat-card-teal">
            <div class="stat-label">Valor consolidado</div>
            <div class="stat-value stat-value-money">{{ formatCurrency(totalCommittedValue) }}</div>
            <div class="stat-note">Base do programa + aditivos</div>
          </article>
        </div>
      </section>

      <section class="content-card">
        <div class="filters-row">
          <div class="search-box">
            <Search class="search-icon" :size="18" />
            <input
              v-model="searchTerm"
              type="text"
              placeholder="Buscar por programa, contrato, fomento ou coordenacao..."
              class="search-input"
            />
          </div>

          <select v-model="statusFilter" class="filter-select">
            <option value="all">Todos os status</option>
            <option value="active">Ativos</option>
            <option value="upcoming">Planejados</option>
            <option value="closed">Encerrados</option>
          </select>
        </div>

        <div v-if="loading" class="state-row">
          <div class="spinner"></div>
          <span>Carregando contratos e aditivos...</span>
        </div>

        <div v-else-if="errorMessage" class="state-row state-row-error">
          <span>{{ errorMessage }}</span>
        </div>

        <div v-else-if="filteredContracts.length === 0" class="state-row state-row-empty">
          <div>
            <strong>Nenhum contrato encontrado</strong>
            <p>Ajuste os filtros ou conclua o cadastro de programas para visualizar os contratos aqui.</p>
          </div>
        </div>

        <div v-else class="contracts-grid">
          <article v-for="contract in filteredContracts" :key="contract.id" class="contract-card">
            <div class="contract-top">
              <div>
                <div class="contract-eyebrow">{{ contract.code || 'Programa sem codigo' }}</div>
                <h2>{{ contract.name || 'Programa sem nome' }}</h2>
              </div>
              <span class="status-badge" :class="contract.statusClass">
                {{ contract.statusLabel }}
              </span>
            </div>

            <div class="contract-meta-grid">
              <div class="meta-item">
                <span>Numero do contrato</span>
                <strong>{{ contract.contractNumber || '-' }}</strong>
              </div>
              <div class="meta-item">
                <span>Entidade de fomento</span>
                <strong>{{ contract.fundingEntity || '-' }}</strong>
              </div>
              <div class="meta-item">
                <span>Executora</span>
                <strong>{{ contract.executorName || '-' }}</strong>
              </div>
              <div class="meta-item">
                <span>Coordenacao geral</span>
                <strong>{{ contract.generalCoordinator || '-' }}</strong>
              </div>
              <div class="meta-item">
                <span>Vigencia</span>
                <strong>{{ formatDate(contract.startDate) }} - {{ formatDate(contract.endDate) }}</strong>
              </div>
              <div class="meta-item">
                <span>Valor base</span>
                <strong>{{ formatCurrency(contract.programValue) }}</strong>
              </div>
              <div class="meta-item">
                <span>Aditivos cadastrados</span>
                <strong>{{ formatNumber(contract.addendumCount) }}</strong>
              </div>
              <div class="meta-item">
                <span>Valor consolidado</span>
                <strong>{{ formatCurrency(contract.totalValue) }}</strong>
              </div>
            </div>

            <div class="addendum-preview">
              <div class="preview-head">
                <strong>Aditivos recentes</strong>
                <small>{{ contract.addendumCount }} registro(s)</small>
              </div>
              <div v-if="contract.addendums.length === 0" class="preview-empty">
                Nenhum aditivo cadastrado para este contrato.
              </div>
              <div v-else class="preview-list">
                <div v-for="addendum in contract.addendums.slice(0, 2)" :key="addendum.id" class="preview-item">
                  <span>Aditivo {{ addendum.addendumNumber || '-' }}</span>
                  <strong>{{ formatDate(addendum.startDate) }} - {{ formatDate(addendum.endDate) }}</strong>
                </div>
              </div>
            </div>

            <div class="contract-actions">
              <button type="button" class="ghost-btn action-btn" @click="openContractModal(contract)">Editar contrato</button>
              <button type="button" class="ghost-btn action-btn" @click="openAddendumManager(contract)">Gerenciar aditivos</button>
              <button type="button" class="primary-btn action-btn" @click="openProgram(contract)">Abrir programa</button>
            </div>
          </article>
        </div>
      </section>
    </div>

    <div v-if="showContractModal" class="modal-overlay" @click.self="closeContractModal">
      <div class="modal-card" @click.stop>
        <div class="modal-head">
          <div>
            <h3>Editar contrato</h3>
            <p>{{ editingProgram?.name }}</p>
          </div>
          <button type="button" class="icon-close" @click="closeContractModal">×</button>
        </div>

        <form class="modal-form" @submit.prevent="saveContract">
          <div class="form-grid">
            <label class="form-group">
              <span>Numero do contrato *</span>
              <input v-model="contractForm.contractNumber" type="text" required />
            </label>

            <label class="form-group">
              <span>Entidade de fomento</span>
              <input v-model="contractForm.fundingEntity" type="text" />
            </label>

            <label class="form-group">
              <span>Executora</span>
              <input v-model="contractForm.executorName" type="text" />
            </label>

            <label class="form-group">
              <span>Coordenacao geral</span>
              <input v-model="contractForm.generalCoordinator" type="text" />
            </label>

            <label class="form-group">
              <span>Data inicial *</span>
              <input v-model="contractForm.startDate" type="date" required />
            </label>

            <label class="form-group">
              <span>Data final *</span>
              <input v-model="contractForm.endDate" type="date" required />
            </label>

            <label class="form-group full-width">
              <span>Valor do programa</span>
              <input v-model="contractForm.programValue" type="number" min="0" step="0.01" />
            </label>
          </div>

          <div v-if="contractError" class="inline-error">{{ contractError }}</div>

          <div class="modal-actions">
            <button type="button" class="ghost-btn" @click="closeContractModal">Cancelar</button>
            <button type="submit" class="primary-btn" :disabled="savingContract">
              {{ savingContract ? 'Salvando...' : 'Salvar contrato' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showAddendumModal" class="modal-overlay" @click.self="closeAddendumManager">
      <div class="modal-card modal-card-wide" @click.stop>
        <div class="modal-head">
          <div>
            <h3>Aditivos do contrato</h3>
            <p>{{ selectedProgram?.name }}</p>
          </div>
          <button type="button" class="icon-close" @click="closeAddendumManager">×</button>
        </div>

        <div class="addendum-manager">
          <div class="addendum-list">
            <div class="list-head">
              <strong>Registros existentes</strong>
              <button type="button" class="ghost-btn" @click="startCreateAddendum">Novo aditivo</button>
            </div>

            <div v-if="selectedAddendums.length === 0" class="preview-empty">
              Nenhum aditivo cadastrado para este programa.
            </div>

            <div v-else class="manager-items">
              <article v-for="addendum in selectedAddendums" :key="addendum.id" class="manager-item">
                <div>
                  <strong>Aditivo {{ addendum.addendumNumber || '-' }}</strong>
                  <p>{{ formatDate(addendum.startDate) }} - {{ formatDate(addendum.endDate) }}</p>
                  <small>{{ formatCurrency(addendum.value) }}</small>
                </div>
                <div class="manager-actions">
                  <button type="button" class="row-action-btn" @click="startEditAddendum(addendum)">Editar</button>
                  <button type="button" class="row-action-btn row-action-danger" @click="removeAddendum(addendum)">Excluir</button>
                </div>
              </article>
            </div>
          </div>

          <form class="addendum-form" @submit.prevent="saveAddendum">
            <h4>{{ editingAddendum ? 'Editar aditivo' : 'Novo aditivo' }}</h4>
            <div class="form-grid">
              <label class="form-group">
                <span>Numero do aditivo</span>
                <input v-model="addendumForm.addendumNumber" type="number" min="1" />
              </label>

              <label class="form-group">
                <span>Valor</span>
                <input v-model="addendumForm.value" type="number" min="0" step="0.01" />
              </label>

              <label class="form-group">
                <span>Data inicial *</span>
                <input v-model="addendumForm.startDate" type="date" required />
              </label>

              <label class="form-group">
                <span>Data final *</span>
                <input v-model="addendumForm.endDate" type="date" required />
              </label>
            </div>

            <div v-if="addendumError" class="inline-error">{{ addendumError }}</div>

            <div class="modal-actions">
              <button type="button" class="ghost-btn" @click="resetAddendumForm">Limpar</button>
              <button type="submit" class="primary-btn" :disabled="savingAddendum">
                {{ savingAddendum ? 'Salvando...' : 'Salvar aditivo' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Search } from 'lucide-vue-next';
import { programService } from '@/services/programService';
import { programAddendumService } from '@/services/programAddendumService';

const router = useRouter();

const programs = ref([]);
const addendumsByProgram = ref({});
const loading = ref(false);
const errorMessage = ref('');
const searchTerm = ref('');
const statusFilter = ref('all');

const showContractModal = ref(false);
const editingProgram = ref(null);
const contractForm = ref(createContractForm());
const contractError = ref('');
const savingContract = ref(false);

const showAddendumModal = ref(false);
const selectedProgram = ref(null);
const editingAddendum = ref(null);
const addendumForm = ref(createAddendumForm());
const addendumError = ref('');
const savingAddendum = ref(false);

const contractRows = computed(() => {
  return programs.value.map((program) => {
    const addendums = Array.isArray(addendumsByProgram.value[program.id]) ? addendumsByProgram.value[program.id] : [];
    const additionalValue = addendums.reduce((sum, addendum) => sum + numberValue(addendum.value), 0);
    const totalValue = numberValue(program.programValue) + additionalValue;
    const status = resolveStatus(program.startDate, program.endDate);

    return {
      ...program,
      addendums,
      addendumCount: addendums.length,
      additionalValue,
      totalValue,
      statusLabel: status.label,
      statusClass: status.className
    };
  });
});

const filteredContracts = computed(() => {
  const term = String(searchTerm.value || '').trim().toLowerCase();

  return contractRows.value.filter((contract) => {
    const searchMatches = !term || [
      contract.name,
      contract.code,
      contract.contractNumber,
      contract.fundingEntity,
      contract.executorName,
      contract.generalCoordinator
    ].some((value) => String(value || '').toLowerCase().includes(term));

    const statusKey = contract.statusClass.replace('status-', '');
    const statusMatches = statusFilter.value === 'all' || statusKey === statusFilter.value;

    return searchMatches && statusMatches;
  });
});

const totalContracts = computed(() => contractRows.value.length);
const activeContractsCount = computed(() => contractRows.value.filter((row) => row.statusClass === 'status-active').length);
const totalAddendums = computed(() => contractRows.value.reduce((sum, row) => sum + row.addendumCount, 0));
const totalCommittedValue = computed(() => contractRows.value.reduce((sum, row) => sum + row.totalValue, 0));
const selectedAddendums = computed(() => {
  if (!selectedProgram.value) return [];
  return Array.isArray(addendumsByProgram.value[selectedProgram.value.id])
    ? addendumsByProgram.value[selectedProgram.value.id]
    : [];
});

onMounted(() => {
  loadContracts();
});

async function loadContracts() {
  loading.value = true;
  errorMessage.value = '';

  try {
    const programData = await programService.getAll();
    const addendumEntries = await Promise.all(
      programData.map(async (program) => [
        program.id,
        await programAddendumService.listByProgram(program.id).catch(() => [])
      ])
    );

    programs.value = [...programData].sort((a, b) => String(a.name || '').localeCompare(String(b.name || '')));
    addendumsByProgram.value = Object.fromEntries(addendumEntries);
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Nao foi possivel carregar os contratos.';
  } finally {
    loading.value = false;
  }
}

function openProgram(program) {
  router.push({ path: '/programs', query: { programId: String(program.id) } });
}

function openContractModal(program) {
  editingProgram.value = { ...program };
  contractForm.value = createContractForm(program);
  contractError.value = '';
  showContractModal.value = true;
}

function closeContractModal() {
  showContractModal.value = false;
  editingProgram.value = null;
  contractForm.value = createContractForm();
  contractError.value = '';
  savingContract.value = false;
}

async function saveContract() {
  if (!editingProgram.value) return;

  if (contractForm.value.endDate && contractForm.value.startDate && contractForm.value.endDate < contractForm.value.startDate) {
    contractError.value = 'A data final nao pode ser anterior a data inicial.';
    return;
  }

  savingContract.value = true;
  contractError.value = '';

  try {
    const payload = {
      ...editingProgram.value,
      contractNumber: contractForm.value.contractNumber,
      fundingEntity: contractForm.value.fundingEntity,
      executorName: contractForm.value.executorName,
      generalCoordinator: contractForm.value.generalCoordinator,
      startDate: contractForm.value.startDate,
      endDate: contractForm.value.endDate,
      programValue: contractForm.value.programValue === '' ? null : Number(contractForm.value.programValue)
    };

    await programService.update(editingProgram.value.id, payload);
    await loadContracts();
    closeContractModal();
  } catch (error) {
    contractError.value = error.response?.data?.details?.join(' ')
      || error.response?.data?.message
      || 'Nao foi possivel salvar o contrato.';
  } finally {
    savingContract.value = false;
  }
}

function openAddendumManager(program) {
  selectedProgram.value = { ...program };
  editingAddendum.value = null;
  addendumForm.value = createAddendumForm();
  addendumError.value = '';
  showAddendumModal.value = true;
}

function closeAddendumManager() {
  showAddendumModal.value = false;
  selectedProgram.value = null;
  editingAddendum.value = null;
  addendumForm.value = createAddendumForm();
  addendumError.value = '';
  savingAddendum.value = false;
}

function startCreateAddendum() {
  editingAddendum.value = null;
  addendumForm.value = createAddendumForm();
  addendumError.value = '';
}

function startEditAddendum(addendum) {
  editingAddendum.value = addendum;
  addendumForm.value = createAddendumForm(addendum);
  addendumError.value = '';
}

function resetAddendumForm() {
  if (editingAddendum.value) {
    addendumForm.value = createAddendumForm(editingAddendum.value);
  } else {
    addendumForm.value = createAddendumForm();
  }
  addendumError.value = '';
}

async function saveAddendum() {
  if (!selectedProgram.value) return;

  savingAddendum.value = true;
  addendumError.value = '';

  try {
    const payload = {
      addendumNumber: addendumForm.value.addendumNumber === '' ? null : Number(addendumForm.value.addendumNumber),
      startDate: addendumForm.value.startDate,
      endDate: addendumForm.value.endDate,
      value: addendumForm.value.value === '' ? null : Number(addendumForm.value.value)
    };

    if (editingAddendum.value) {
      await programAddendumService.update(selectedProgram.value.id, editingAddendum.value.id, payload);
    } else {
      await programAddendumService.create(selectedProgram.value.id, payload);
    }

    addendumsByProgram.value = {
      ...addendumsByProgram.value,
      [selectedProgram.value.id]: await programAddendumService.listByProgram(selectedProgram.value.id)
    };

    startCreateAddendum();
  } catch (error) {
    addendumError.value = error.response?.data?.details?.join(' ')
      || error.response?.data?.message
      || 'Nao foi possivel salvar o aditivo.';
  } finally {
    savingAddendum.value = false;
  }
}

async function removeAddendum(addendum) {
  if (!selectedProgram.value) return;
  const confirmed = window.confirm(`Deseja excluir o aditivo ${addendum.addendumNumber || ''}?`);
  if (!confirmed) return;

  try {
    await programAddendumService.delete(selectedProgram.value.id, addendum.id);
    addendumsByProgram.value = {
      ...addendumsByProgram.value,
      [selectedProgram.value.id]: await programAddendumService.listByProgram(selectedProgram.value.id)
    };
    if (editingAddendum.value?.id === addendum.id) {
      startCreateAddendum();
    }
  } catch (error) {
    window.alert(error.response?.data?.message || 'Nao foi possivel excluir este aditivo.');
  }
}

function createContractForm(program = null) {
  return {
    contractNumber: program?.contractNumber || '',
    fundingEntity: program?.fundingEntity || '',
    executorName: program?.executorName || '',
    generalCoordinator: program?.generalCoordinator || '',
    startDate: program?.startDate || '',
    endDate: program?.endDate || '',
    programValue: program?.programValue ?? ''
  };
}

function createAddendumForm(addendum = null) {
  return {
    addendumNumber: addendum?.addendumNumber ?? '',
    startDate: addendum?.startDate || '',
    endDate: addendum?.endDate || '',
    value: addendum?.value ?? ''
  };
}

function resolveStatus(startDate, endDate) {
  const today = new Date();
  const start = startDate ? new Date(`${startDate}T00:00:00`) : null;
  const end = endDate ? new Date(`${endDate}T00:00:00`) : null;

  if (start && start > today) {
    return { label: 'Planejado', className: 'status-upcoming' };
  }
  if (end && end < today) {
    return { label: 'Encerrado', className: 'status-closed' };
  }
  return { label: 'Ativo', className: 'status-active' };
}

function numberValue(value) {
  const parsed = Number(value || 0);
  return Number.isFinite(parsed) ? parsed : 0;
}

function formatNumber(value) {
  return new Intl.NumberFormat('pt-BR').format(numberValue(value));
}

function formatCurrency(value) {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  }).format(numberValue(value));
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(`${value}T00:00:00`).toLocaleDateString('pt-BR');
}
</script>

<style scoped>
.contracts-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(15, 118, 110, 0.08), transparent 28%),
    linear-gradient(180deg, #f7fafc 0%, #eef4fb 100%);
}

.contracts-shell {
  max-width: 1520px;
  margin: 0 auto;
  padding: 32px 24px 48px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header-card,
.content-card,
.contract-card,
.modal-card {
  background: rgba(255, 255, 255, 0.97);
  border: 1px solid #dbe5f0;
  border-radius: 24px;
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.06);
}

.page-header-card {
  padding: 28px;
}

.page-header-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

h1,
h2,
h3,
h4,
p {
  margin: 0;
}

h1 {
  color: #0f172a;
  font-size: 32px;
}

.subtitle {
  margin-top: 10px;
  max-width: 860px;
  color: #526277;
  line-height: 1.6;
}

.top-actions,
.contract-actions,
.manager-actions,
.modal-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.ghost-btn,
.primary-btn,
.row-action-btn,
.icon-close {
  border: none;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ghost-btn,
.primary-btn,
.row-action-btn {
  height: 42px;
  padding: 0 14px;
  font-weight: 600;
}

.ghost-btn,
.row-action-btn {
  background: #f8fbff;
  border: 1px solid #d7e3f1;
  color: #1f3a5f;
}

.ghost-btn:hover,
.row-action-btn:hover {
  background: #eef6ff;
}

.primary-btn {
  background: linear-gradient(135deg, #14b8a6 0%, #0f9fb5 100%);
  color: #fff;
  box-shadow: 0 12px 24px rgba(20, 184, 166, 0.22);
}

.primary-btn:hover {
  transform: translateY(-1px);
}

.stats-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  border-radius: 20px;
  padding: 18px;
  border: 1px solid #dbe7f3;
}

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

.stat-label {
  color: #48617d;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  font-weight: 700;
}

.stat-value {
  margin-top: 14px;
  font-size: 30px;
  font-weight: 700;
  color: #0f172a;
}

.stat-value-money {
  font-size: 24px;
}

.stat-note {
  margin-top: 6px;
  color: #607287;
  font-size: 13px;
}

.content-card {
  padding: 20px;
}

.filters-row {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 18px;
}

.search-box {
  flex: 1;
  min-width: 260px;
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
.filter-select,
.form-group input {
  height: 46px;
  border-radius: 14px;
  border: 1px solid #d9e4ef;
  background: #f8fbff;
  color: #11243d;
  font-size: 14px;
}

.search-input {
  width: 100%;
  padding: 0 16px 0 42px;
}

.filter-select,
.form-group input {
  padding: 0 14px;
}

.filter-select {
  min-width: 190px;
}

.state-row {
  padding: 44px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  color: #526277;
}

.state-row-error {
  color: #b42318;
}

.state-row-empty {
  justify-content: center;
  text-align: center;
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

.contracts-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.contract-card {
  padding: 20px;
}

.contract-top {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.contract-eyebrow {
  color: #0f9fb5;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.contract-top h2 {
  margin-top: 8px;
  color: #0f172a;
  font-size: 24px;
}

.status-badge {
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.status-active {
  background: #ecfdf5;
  color: #047857;
}

.status-upcoming {
  background: #e0f2fe;
  color: #0369a1;
}

.status-closed {
  background: #fef2f2;
  color: #b42318;
}

.contract-meta-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.meta-item {
  padding: 14px;
  border-radius: 16px;
  background: #f8fbff;
  border: 1px solid #e3edf7;
}

.meta-item span {
  display: block;
  color: #607287;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-bottom: 8px;
}

.meta-item strong {
  color: #0f172a;
  line-height: 1.4;
}

.addendum-preview {
  margin-top: 18px;
  padding: 16px;
  border: 1px solid #e3edf7;
  border-radius: 18px;
  background: linear-gradient(180deg, #fcfdff 0%, #f8fbff 100%);
}

.preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.preview-head small,
.preview-empty {
  color: #607287;
}

.preview-list,
.manager-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 14px;
}

.preview-item,
.manager-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #e1eaf5;
}

.preview-item span,
.manager-item p,
.manager-item small {
  color: #607287;
}

.contract-actions {
  margin-top: 18px;
}

.action-btn {
  flex: 1 1 180px;
  justify-content: center;
}

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
  padding: 22px;
}

.modal-card-wide {
  width: min(1080px, 100%);
}

.modal-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 18px;
}

.modal-head p {
  color: #607287;
  margin-top: 6px;
}

.icon-close {
  width: 34px;
  height: 34px;
  background: #f8fbff;
  border: 1px solid #d7e3f1;
  color: #1f3a5f;
  font-size: 20px;
}

.modal-form,
.addendum-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group span {
  font-size: 13px;
  color: #43566f;
  font-weight: 600;
}

.full-width {
  grid-column: 1 / -1;
}

.inline-error {
  padding: 12px 14px;
  border-radius: 14px;
  background: #fff1f2;
  color: #b42318;
  border: 1px solid #fecdd3;
}

.addendum-manager {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 18px;
}

.addendum-list,
.addendum-form {
  padding: 18px;
  border-radius: 20px;
  background: #f8fbff;
  border: 1px solid #e1eaf5;
}

.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 12px;
}

.row-action-danger {
  color: #b42318;
  background: #fff5f5;
  border-color: #f3d0d2;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1100px) {
  .stats-grid,
  .contracts-grid,
  .addendum-manager {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 840px) {
  .contracts-shell {
    padding: 24px 16px 40px;
  }

  .page-header-top,
  .filters-row,
  .contract-top {
    flex-direction: column;
  }

  .contract-meta-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .filter-select {
    width: 100%;
  }
}
</style>
