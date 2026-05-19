<template>
  <div class="enrollment-modal__overlay" @click.self="close">
    <div class="enrollment-modal__card" @click.stop>
      <div class="enrollment-modal__head">
        <div>
          <h2>Registrar vínculo</h2>
          <p>Informe os dados do programa e da turma para vincular esta pessoa.</p>
        </div>
        <button type="button" class="enrollment-modal__close" aria-label="Fechar" @click="close">✕</button>
      </div>

      <form class="enrollment-modal__form" @submit.prevent="save">
        <div class="enrollment-modal__body">
          <div v-if="loadingOptions" class="enrollment-modal__loading">Carregando programas...</div>

          <div class="enrollment-modal__grid">
            <div class="enrollment-modal__field">
              <label for="enrollment-program-id">Programa *</label>
              <select id="enrollment-program-id" v-model="form.programId" :disabled="loadingOptions || saving">
                <option value="">Selecione o programa</option>
                <option v-for="program in programOptions" :key="program.id" :value="String(program.id)">
                  {{ program.label }}
                </option>
              </select>
            </div>

            <div class="enrollment-modal__field">
              <label for="enrollment-class-id">Turma *</label>
              <select
                id="enrollment-class-id"
                v-model="form.classId"
                :disabled="!form.programId || loadingClasses || saving"
              >
                <option value="">
                  {{
                    !form.programId
                      ? 'Selecione um programa primeiro'
                      : loadingClasses
                        ? 'Carregando turmas...'
                        : 'Selecione a turma'
                  }}
                </option>
                <option v-for="classItem in classOptions" :key="classItem.id" :value="String(classItem.id)">
                  {{ classItem.label }}
                </option>
              </select>
              <small v-if="form.programId && !loadingClasses && classOptions.length === 0" class="enrollment-modal__helper">
                Nenhuma turma encontrada para este programa.
              </small>
            </div>
          </div>

          <div v-if="errorMessage" class="enrollment-modal__alert">
            {{ errorMessage }}
          </div>
        </div>

        <div class="enrollment-modal__actions">
          <button type="button" class="enrollment-modal__btn enrollment-modal__btn--secondary" @click="close">Cancelar</button>
          <button type="submit" class="enrollment-modal__btn enrollment-modal__btn--primary" :disabled="saveDisabled">
            {{ saving ? 'Registrando...' : 'Registrar vínculo' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { enrollmentService } from '@/services/enrollmentService';
import { peopleService } from '@/services/peopleService';
import { classService } from '@/services/classService';

const props = defineProps({ personId: { type: [String, Number], required: true } });
const emit = defineEmits(['close','saved']);

const form = ref({ programId: '', classId: '' });
const saving = ref(false);
const errorMessage = ref('');
const loadingOptions = ref(false);
const loadingClasses = ref(false);
const programOptions = ref([]);
const classOptions = ref([]);
const saveDisabled = computed(() => saving.value || loadingOptions.value || loadingClasses.value || !form.value.programId || !form.value.classId);

const normalizeOptions = (items = []) => items
  .map((item) => ({
    id: item?.id,
    label: item?.label || item?.name || item?.code || ''
  }))
  .filter((item) => item.id !== null && item.id !== undefined && item.label);

const loadProgramOptions = async () => {
  loadingOptions.value = true;
  errorMessage.value = '';
  try {
    const referenceData = await peopleService.getReferenceData();
    programOptions.value = normalizeOptions(referenceData?.programas || []);
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || err?.message || 'Erro ao carregar programas.';
  } finally {
    loadingOptions.value = false;
  }
};

const loadClassOptions = async (programId) => {
  classOptions.value = [];
  form.value.classId = '';
  if (!programId) return;

  loadingClasses.value = true;
  errorMessage.value = '';
  try {
    const classes = await classService.getByProgramId(Number(programId));
    classOptions.value = (classes || [])
      .map((classItem) => ({
        id: classItem?.id,
        label: classItem?.code || `Turma ${classItem?.id}`
      }))
      .filter((item) => item.id !== null && item.id !== undefined && item.label);
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || err?.message || 'Erro ao carregar turmas.';
  } finally {
    loadingClasses.value = false;
  }
};

const save = async () => {
  if (saveDisabled.value) {
    errorMessage.value = 'Selecione programa e turma para registrar o vínculo.';
    return;
  }

  errorMessage.value = '';
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
    errorMessage.value = err?.response?.data?.message || err?.message || 'Erro ao registrar vínculo.';
  } finally {
    saving.value = false;
  }
};

const close = () => emit('close');

watch(
  () => form.value.programId,
  (programId) => {
    loadClassOptions(programId);
  }
);

onMounted(() => {
  loadProgramOptions();
});
</script>

<style scoped>
.enrollment-modal__overlay {
  position: fixed;
  inset: 0;
  background: rgba(8, 15, 28, 0.56);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 4000;
  padding: 18px;
}

.enrollment-modal__card {
  width: min(720px, 100%);
  max-height: 92vh;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 22px 60px rgba(8, 15, 28, 0.28);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.enrollment-modal__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 22px 14px;
  border-bottom: 1px solid #e2eaf2;
}

.enrollment-modal__head h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
}

.enrollment-modal__head p {
  margin: 4px 0 0;
  color: #6a7a90;
  font-size: 14px;
  line-height: 1.4;
}

.enrollment-modal__close {
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

.enrollment-modal__close:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.enrollment-modal__form {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.enrollment-modal__body {
  padding: 16px 22px 8px;
  overflow: auto;
}

.enrollment-modal__loading {
  margin-bottom: 12px;
  color: #64748b;
  font-size: 14px;
}

.enrollment-modal__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 18px;
}

.enrollment-modal__field {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.enrollment-modal__field label {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.enrollment-modal__field input,
.enrollment-modal__field select {
  width: 100%;
  box-sizing: border-box;
  height: 42px;
  border-radius: 12px;
  border: 1px solid #d7e1eb;
  padding: 0 12px;
  color: #0f172a;
  background: #fff;
  outline: none;
  font-size: 16px;
}

.enrollment-modal__field input:focus,
.enrollment-modal__field select:focus {
  border-color: #14b8a6;
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.1);
}

.enrollment-modal__field select:disabled {
  background: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
}

.enrollment-modal__helper {
  color: #64748b;
  font-size: 12px;
}

.enrollment-modal__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 22px 18px;
  border-top: 1px solid #e2eaf2;
}

.enrollment-modal__btn {
  height: 40px;
  border-radius: 12px;
  border: 0;
  padding: 0 14px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.enrollment-modal__btn--primary {
  color: #fff;
  background: #14b8a6;
}

.enrollment-modal__btn--secondary {
  background: #fff;
  border: 1px solid #d7e1eb;
  color: #1f2a3d;
}

.enrollment-modal__btn--primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.enrollment-modal__alert {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #fff0f0;
  color: #b42318;
}

@media (max-width: 720px) {
  .enrollment-modal__grid {
    grid-template-columns: 1fr;
  }
}
</style>