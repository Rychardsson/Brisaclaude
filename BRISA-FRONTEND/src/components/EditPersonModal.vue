<template>
  <div class="modal-overlay" @click="close">
    <div class="modal-card" @click.stop>
      <div class="modal-header">
        <h2>Editar Perfil</h2>
        <button class="btn-close" @click="close">✕</button>
      </div>
      <div class="modal-body">
        <div v-if="loading">Carregando...</div>
        <div v-else>
          <label>Nome</label>
          <input v-model="form.name" type="text" />

          <label>E-mail</label>
          <input v-model="form.email" type="email" />

          <label>Telefone</label>
          <input v-model="form.phone" type="text" />

          <div class="actions">
            <button class="btn-outline" @click="close">Cancelar</button>
            <button class="btn-primary" :disabled="saving" @click="save">{{ saving ? 'Salvando...' : 'Salvar' }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { peopleService } from '@/services/peopleService';

const props = defineProps({ personId: { type: [String, Number], required: true } });
const emit = defineEmits(['close','saved']);

const form = ref({ name: '', email: '', phone: '' });
const loading = ref(true);
const saving = ref(false);

const load = async () => {
  loading.value = true;
  try {
    const data = await peopleService.getById(props.personId);
    form.value.name = data.name || '';
    form.value.email = data.email || '';
    form.value.phone = data.phone || '';
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const save = async () => {
  saving.value = true;
  try {
    await peopleService.update(props.personId, { name: form.value.name, email: form.value.email, phone: form.value.phone });
    emit('saved');
    close();
  } catch (err) {
    console.error(err);
  } finally {
    saving.value = false;
  }
};

const close = () => emit('close');

onMounted(load);
</script>

<style scoped>
.modal-body label { display:block; margin-top:10px; color:#5f728d }
.modal-body input { width:100%; padding:8px; margin-top:4px; border:1px solid #e2eaf2; border-radius:6px }
.actions { display:flex; gap:8px; justify-content:flex-end; margin-top:16px }
.btn-primary { background:#14b8a6; color:white; border:none; padding:10px 16px; border-radius:8px }
.btn-outline { background:white; border:1px solid #d8e1eb; padding:10px 16px; border-radius:8px }
</style>