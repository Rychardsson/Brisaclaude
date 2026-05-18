<template>
  <div class="modal-overlay" @click="close">
    <div class="modal-card" @click.stop>
      <div class="modal-header">
        <h2>Registrar Vínculo</h2>
        <button class="btn-close" @click="close">✕</button>
      </div>
      <div class="modal-body">
        <label>Programa (ID)</label>
        <input v-model="form.programId" type="number" />

        <label>Turma (ID)</label>
        <input v-model="form.classId" type="number" />

        <div class="actions">
          <button class="btn-outline" @click="close">Cancelar</button>
          <button class="btn-primary" :disabled="saving" @click="save">{{ saving ? 'Registrando...' : 'Registrar' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { enrollmentService } from '@/services/enrollmentService';

const props = defineProps({ personId: { type: [String, Number], required: true } });
const emit = defineEmits(['close','saved']);

const form = ref({ programId: '', classId: '' });
const saving = ref(false);

const save = async () => {
  saving.value = true;
  try {
    const payload = {
      personId: Number(props.personId),
      programId: form.value.programId ? Number(form.value.programId) : null,
      classId: form.value.classId ? Number(form.value.classId) : null
    };
    await enrollmentService.create(payload);
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
.modal-body input { width:100%; padding:8px; margin-top:4px; border:1px solid #e2eaf2; border-radius:6px }
.actions { display:flex; gap:8px; justify-content:flex-end; margin-top:16px }
.btn-primary { background:#14b8a6; color:white; border:none; padding:10px 16px; border-radius:8px }
.btn-outline { background:white; border:1px solid #d8e1eb; padding:10px 16px; border-radius:8px }
</style>