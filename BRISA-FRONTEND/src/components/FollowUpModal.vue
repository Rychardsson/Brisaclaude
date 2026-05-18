<template>
  <div class="modal-overlay" @click="close">
    <div class="modal-card" @click.stop>
      <div class="modal-header">
        <h2>Registrar Acompanhamento</h2>
        <button class="btn-close" @click="close">✕</button>
      </div>
      <div class="modal-body">
        <label>Data</label>
        <input v-model="form.surveyDate" type="date" />

        <label>Status</label>
        <select v-model="form.status">
          <option value="SEM_ACOMPANHAMENTO">Sem acompanhamento</option>
          <option value="EMPREGADO">Empregado</option>
          <option value="EM_FORMACAO">Em formação</option>
          <option value="OUTRO">Outro</option>
        </select>

        <label>Empresa</label>
        <input v-model="form.company" type="text" />

        <label>Cargo</label>
        <input v-model="form.position" type="text" />

        <label>Notas</label>
        <textarea v-model="form.notes" rows="4"></textarea>

        <div class="actions">
          <button class="btn-outline" @click="close">Cancelar</button>
          <button class="btn-primary" :disabled="saving" @click="save">{{ saving ? 'Salvando...' : 'Salvar' }}</button>
        </div>
      </div>
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

const save = async () => {
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
    console.error(err);
  } finally {
    saving.value = false;
  }
};

const close = () => emit('close');
</script>

<style scoped>
.modal-body label { display:block; margin-top:10px; color:#5f728d }
.modal-body input, .modal-body select, .modal-body textarea { width:100%; padding:8px; margin-top:4px; border:1px solid #e2eaf2; border-radius:6px }
.actions { display:flex; gap:8px; justify-content:flex-end; margin-top:16px }
.btn-primary { background:#14b8a6; color:white; border:none; padding:10px 16px; border-radius:8px }
.btn-outline { background:white; border:1px solid #d8e1eb; padding:10px 16px; border-radius:8px }
</style>