<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal-card modal-card-small create-modal" @click.stop>
      <div class="modal-head">
        <div>
          <h2>Novo Orientador</h2>
          <p class="modal-subtitle">Preencha os dados do orientador.</p>
        </div>
        <button type="button" class="modal-close" @click="close" aria-label="Fechar">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>

      <form class="create-form" @submit.prevent="submit">
        <div class="modal-body">
          <div class="modal-section">
            <h3 class="modal-section-title">Dados pessoais</h3>
            <div class="form-grid">
              <div class="form-group full-width">
                <label>Nome *</label>
                <input v-model="form.name" type="text" placeholder="Nome completo" required />
              </div>

              <div class="form-group">
                <label>CPF *</label>
                <input v-model="form.cpf" type="text" placeholder="000.000.000-00" required />
              </div>

              <div class="form-group">
                <label>Formação</label>
                <input v-model="form.formation" type="text" placeholder="Formação" />
              </div>

              <div class="form-group">
                <label>Data de nascimento</label>
                <input v-model="form.birthDate" type="date" />
              </div>
            </div>
          </div>
        </div>

        <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

        <div class="modal-actions">
          <button type="button" class="secondary-btn modal-secondary" @click="close">Cancelar</button>
          <button type="submit" class="primary-btn" :disabled="submitDisabled" :aria-disabled="submitDisabled">{{ isLoading ? 'Criando...' : 'Criar Orientador' }}</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { advisorService } from '@/services/advisorService';

const emit = defineEmits(['close', 'created']);
const form = ref({ cpf: '', name: '', formation: '', birthDate: '' });
const isLoading = ref(false);
const errorMessage = ref('');

const canSubmit = computed(() => {
  const nameOk = !!String(form.value.name || '').trim().length;
  const cpfDigits = String(form.value.cpf || '').replace(/\D/g, '');
  const cpfOk = cpfDigits.length === 11; // require full CPF (11 digits)
  return nameOk && cpfOk;
});

const submitDisabled = computed(() => isLoading.value || !canSubmit.value);

function close() {
  emit('close');
}

async function submit() {
  if (!form.value.cpf || !form.value.name) {
    errorMessage.value = 'CPF e Nome são obrigatórios';
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';
  try {
    const payload = {
      cpf: String(form.value.cpf).replace(/\D/g, ''),
      name: form.value.name,
      formation: form.value.formation,
      birthDate: form.value.birthDate || null
    };
    const created = await advisorService.create(payload);
    emit('created', created);
    close();
  } catch (err) {
    errorMessage.value = err.response?.data?.details?.join(' ') || err.response?.data?.message || err.message || 'Erro ao criar orientador';
  } finally {
    isLoading.value = false;
  }
}
</script>

<style scoped>
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

.modal-card-small {
  width: min(540px, 100%);
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.modal-close {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #64748b;
  display: inline-grid;
  place-items: center;
  cursor: pointer;
  transition: all 0.2s ease;
}
.modal-close:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

/* Reuse create-modal structure/styles from PeopleListView */
.create-modal {
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.create-modal .modal-head {
  padding: 18px 22px 14px;
  border-bottom: 1px solid #e2eaf2;
  align-items: flex-start;
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.create-modal .modal-head h2 {
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
  margin: 0;
}

.modal-subtitle {
  margin: 4px 0 0;
  color: #6a7a90;
  font-size: 14px;
  line-height: 1.4;
}

.create-form {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.modal-body {
  padding: 16px 22px 8px;
  flex: 1 1 auto;
  min-height: 0;
  overflow: auto;
}

.modal-section + .modal-section {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #e2eaf2;
}

.modal-section-title {
  margin: 0 0 12px;
  color: #0f172a;
  font-size: 18px;
  font-weight: 600;
}

.create-modal .modal-actions {
  margin-top: 0;
  padding: 14px 22px 18px;
  border-top: 1px solid #e2eaf2;
}

.modal-secondary {
  background: #fff;
  border: 1px solid #d7e1eb;
  color: #1f2a3d;
}

.create-modal .form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 18px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.full-width { grid-column: 1 / -1; }

.create-modal .form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.create-modal .form-group input,
.create-modal .form-group select {
  font-size: 16px;
  font-weight: 400;
  color: #0f172a;
}

.form-group input,
.form-group select {
  width: 100%;
  box-sizing: border-box;
  height: 42px;
  border-radius: 12px;
  border: 1px solid #d7e1eb;
  padding: 0 12px;
  color: #13233f;
  background: #fff;
  outline: none;
}

.form-group input:focus,
.form-group select:focus {
  border-color: #14b8a6;
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.1);
}

.primary-btn,
.secondary-btn {
  height: 40px;
  border-radius: 12px;
  border: 0;
  padding: 0 14px;
  font-weight: 700;
  cursor: pointer;
}

.primary-btn {
  background: #14b8a6;
  color: #fff;
  border-color: #14b8a6;
  box-shadow: 0 8px 16px rgba(20, 184, 166, 0.20);
}

.primary-btn:hover {
  background: #0d9488;
}

.create-modal .primary-btn:disabled,
.create-modal .primary-btn[disabled] {
  opacity: 0.45;
  cursor: not-allowed;
  pointer-events: none;
  box-shadow: none;
  transform: none;
  filter: grayscale(6%);
}

.alert {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 12px;
  display: flex;
  gap: 10px;
  align-items: flex-start;
}
.alert-error { background: #fff0f0; color: #b42318; }

/* Responsive */
@media (max-width: 720px) {
  .form-grid { grid-template-columns: 1fr; }
}

/* Force inputs styling in case global CSS overrides */
.create-modal .form-group input,
.create-modal .form-group select,
.create-modal .form-group textarea {
  display: block !important;
  width: 100% !important;
  box-sizing: border-box !important;
}

</style>
