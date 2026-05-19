<template>
  <div class="follow-up-modal__overlay" @click.self="close">
    <div class="follow-up-modal__card" @click.stop>
      <div class="follow-up-modal__head">
        <div>
          <h2>Registrar acompanhamento</h2>
          <p>Atualize o status profissional e registre observações do acompanhamento.</p>
        </div>
        <button type="button" class="follow-up-modal__close" aria-label="Fechar" @click="close">✕</button>
      </div>

      <form class="follow-up-modal__form" @submit.prevent="save">
        <div class="follow-up-modal__body">
          <div class="follow-up-modal__grid">
            <div class="follow-up-modal__field">
              <label for="followup-survey-date">Data</label>
              <input id="followup-survey-date" v-model="form.surveyDate" type="date" />
            </div>

            <div class="follow-up-modal__field">
              <label for="followup-status">Status</label>
              <select id="followup-status" v-model="form.status">
                <option value="SEM_ACOMPANHAMENTO">Sem acompanhamento</option>
                <option value="EMPREGADO">Empregado</option>
                <option value="EM_FORMACAO">Em formação</option>
                <option value="OUTRO">Outro</option>
              </select>
            </div>

            <div class="follow-up-modal__field">
              <label for="followup-company">Empresa</label>
              <input id="followup-company" v-model="form.company" type="text" />
            </div>

            <div class="follow-up-modal__field">
              <label for="followup-position">Cargo</label>
              <input id="followup-position" v-model="form.position" type="text" />
            </div>

            <div class="follow-up-modal__field follow-up-modal__field--full">
              <label for="followup-notes">Notas</label>
              <textarea id="followup-notes" v-model="form.notes" rows="4"></textarea>
            </div>
          </div>

          <div v-if="errorMessage" class="follow-up-modal__alert">
            {{ errorMessage }}
          </div>
        </div>

        <div class="follow-up-modal__actions">
          <button type="button" class="follow-up-modal__btn follow-up-modal__btn--secondary" @click="close">Cancelar</button>
          <button type="submit" class="follow-up-modal__btn follow-up-modal__btn--primary" :disabled="saving">
            {{ saving ? 'Salvando...' : 'Salvar acompanhamento' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { careerService } from '@/services/careerService';

const props = defineProps({ personId: { type: [String, Number], required: true } });
const emit = defineEmits(['close','saved']);

const form = ref({ surveyDate: '', status: '', company: '', position: '', notes: '' });
const saving = ref(false);
const errorMessage = ref('');

const save = async () => {
  errorMessage.value = '';
  saving.value = true;
  try {
    const payload = {
      peopleId: Number(props.personId),
      surveyDate: form.value.surveyDate,
      status: form.value.status,
      company: form.value.company.trim(),
      position: form.value.position.trim(),
      notes: form.value.notes.trim()
    };
    await careerService.createFollowUp(payload);
    emit('saved');
    close();
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || err?.message || 'Erro ao registrar acompanhamento.';
  } finally {
    saving.value = false;
  }
};

const close = () => emit('close');
</script>

<style scoped>
.follow-up-modal__overlay {
  position: fixed;
  inset: 0;
  background: rgba(8, 15, 28, 0.56);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 4000;
  padding: 18px;
}

.follow-up-modal__card {
  width: min(760px, 100%);
  max-height: 92vh;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 22px 60px rgba(8, 15, 28, 0.28);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.follow-up-modal__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 22px 14px;
  border-bottom: 1px solid #e2eaf2;
}

.follow-up-modal__head h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
}

.follow-up-modal__head p {
  margin: 4px 0 0;
  color: #6a7a90;
  font-size: 14px;
  line-height: 1.4;
}

.follow-up-modal__close {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #64748b;
  display: inline-grid;
  place-items: center;
  cursor: pointer;
}

.follow-up-modal__close:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.follow-up-modal__form {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.follow-up-modal__body {
  padding: 16px 22px 8px;
  overflow: auto;
}

.follow-up-modal__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 18px;
}

.follow-up-modal__field {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.follow-up-modal__field--full {
  grid-column: 1 / -1;
}

.follow-up-modal__field label {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.follow-up-modal__field input,
.follow-up-modal__field select,
.follow-up-modal__field textarea {
  width: 100%;
  box-sizing: border-box;
  border-radius: 12px;
  border: 1px solid #d7e1eb;
  color: #0f172a;
  background: #fff;
  outline: none;
  font-size: 16px;
}

.follow-up-modal__field input,
.follow-up-modal__field select {
  height: 42px;
  padding: 0 12px;
}

.follow-up-modal__field textarea {
  padding: 10px 12px;
  min-height: 120px;
  resize: vertical;
}

.follow-up-modal__field input:focus,
.follow-up-modal__field select:focus,
.follow-up-modal__field textarea:focus {
  border-color: #14b8a6;
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.1);
}

.follow-up-modal__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 22px 18px;
  border-top: 1px solid #e2eaf2;
}

.follow-up-modal__btn {
  height: 40px;
  border-radius: 12px;
  border: 0;
  padding: 0 14px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.follow-up-modal__btn--primary {
  color: #fff;
  background: #14b8a6;
}

.follow-up-modal__btn--secondary {
  background: #fff;
  border: 1px solid #d7e1eb;
  color: #1f2a3d;
}

.follow-up-modal__btn--primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.follow-up-modal__alert {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #fff0f0;
  color: #b42318;
}

@media (max-width: 720px) {
  .follow-up-modal__grid {
    grid-template-columns: 1fr;
  }

  .follow-up-modal__field--full {
    grid-column: auto;
  }
}
</style>