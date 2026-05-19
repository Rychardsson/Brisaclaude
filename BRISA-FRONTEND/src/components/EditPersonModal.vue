<template>
  <div class="edit-person-modal__overlay" @click.self="close">
    <div class="edit-person-modal__card" @click.stop>
      <div class="edit-person-modal__head">
        <div>
          <h2>Editar perfil</h2>
          <p>Atualize os dados pessoais e acad&ecirc;micos com os mesmos crit&eacute;rios do cadastro.</p>
        </div>
        <button type="button" class="edit-person-modal__close" aria-label="Fechar" @click="close">✕</button>
      </div>

      <div v-if="loading" class="edit-person-modal__loading">Carregando dados da pessoa...</div>

      <form v-else class="edit-person-modal__form" @submit.prevent="save">
        <div class="edit-person-modal__body">
          <section class="edit-person-modal__section">
            <h3>Dados pessoais</h3>
            <div class="edit-person-modal__grid">
              <div class="edit-person-modal__field edit-person-modal__field--full">
                <label for="edit-name">Nome do aluno *</label>
                <input id="edit-name" v-model="form.name" type="text" placeholder="Digite o nome completo" required />
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-birthDate">Data de nascimento *</label>
                <input id="edit-birthDate" v-model="form.birthDate" type="date" required />
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-gender">G&ecirc;nero</label>
                <select id="edit-gender" v-model="form.gender">
                  <option value="">Selecione</option>
                  <option v-for="item in genderOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-quotaCategory">Cota</label>
                <select id="edit-quotaCategory" v-model="form.quotaCategory">
                  <option value="">Selecione</option>
                  <option v-for="item in quotaOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-cpf">CPF *</label>
                <input id="edit-cpf" v-model="form.cpf" type="text" placeholder="000.000.000-00" required />
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-email">E-mail *</label>
                <input id="edit-email" v-model="form.email" type="email" placeholder="email@exemplo.com" required />
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-phone">Telefone</label>
                <input id="edit-phone" v-model="form.phone" type="text" placeholder="(00) 00000-0000" />
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-state">Estado de resid&ecirc;ncia</label>
                <select id="edit-state" v-model="form.state">
                  <option value="">Selecione</option>
                  <option v-for="item in stateOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-city">Cidade de resid&ecirc;ncia</label>
                <input id="edit-city" v-model="form.city" type="text" placeholder="Cidade" />
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-linkedin">LinkedIn</label>
                <input id="edit-linkedin" v-model="form.linkedin" type="text" placeholder="https://linkedin.com/in/..." />
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-zipCode">CEP</label>
                <input id="edit-zipCode" v-model="form.zipCode" type="text" placeholder="00000-000" />
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-address">Endere&ccedil;o</label>
                <input id="edit-address" v-model="form.address" type="text" placeholder="Rua, n&uacute;mero, bairro" />
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-addressExtra">Complemento</label>
                <input id="edit-addressExtra" v-model="form.addressExtra" type="text" placeholder="Apto, bloco, ponto de refer&ecirc;ncia" />
              </div>
            </div>
          </section>

          <section class="edit-person-modal__section">
            <h3>Dados acad&ecirc;micos</h3>
            <div class="edit-person-modal__grid">
              <div class="edit-person-modal__field edit-person-modal__field--full">
                <label for="edit-educationLevel">Tipo de forma&ccedil;&atilde;o</label>
                <select id="edit-educationLevel" v-model="form.educationLevel">
                  <option value="">Selecione</option>
                  <option v-for="item in educationTypeOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-institutionName">Institui&ccedil;&atilde;o</label>
                <input id="edit-institutionName" v-model="form.institutionName" type="text" placeholder="Institui&ccedil;&atilde;o" />
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-courseName">Curso</label>
                <input id="edit-courseName" v-model="form.courseName" type="text" placeholder="Curso" />
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-educationStatus">Status da forma&ccedil;&atilde;o</label>
                <select id="edit-educationStatus" v-model="form.educationStatus">
                  <option value="">Selecione</option>
                  <option v-for="item in educationStatusOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </div>

              <div class="edit-person-modal__field">
                <label for="edit-educationCompletionDate">Data de conclus&atilde;o</label>
                <input id="edit-educationCompletionDate" v-model="form.educationCompletionDate" type="date" />
              </div>
            </div>
          </section>

          <div v-if="errorMessage" class="edit-person-modal__alert">
            {{ errorMessage }}
          </div>
        </div>

        <div class="edit-person-modal__actions">
          <button type="button" class="edit-person-modal__btn edit-person-modal__btn--secondary" @click="close">Cancelar</button>
          <button type="submit" class="edit-person-modal__btn edit-person-modal__btn--primary" :disabled="saveDisabled">
            {{ saving ? 'Salvando...' : 'Salvar altera&ccedil;&otilde;es' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { peopleService } from '@/services/peopleService';

const props = defineProps({
  personId: { type: [String, Number], required: true }
});
const emit = defineEmits(['close', 'saved']);

const fallbackQuotaOptions = ['Ampla Concorrência', 'PCD/Neurodivergente', 'Negro/Pardo', 'Mulher', '45+'];
const fallbackGenderOptions = ['Feminino', 'Masculino', 'Outro', 'Não informado'];
const fixedEducationTypeOptions = [
  'Engenharia de Computação, Ciência da Computação ou outros cursos relacionados a TIC',
  'Outros cursos de ciências exatas ou tecnológicos',
  'Técnico na área de exatas concluído',
  'Engenharia (exceto de Software/Computação)'
];
const fallbackEducationStatusOptions = ['Cursando', 'Concluído', 'Trancado', 'Não informado'];
const fallbackStateOptions = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA',
  'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN',
  'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
];

const loading = ref(true);
const saving = ref(false);
const errorMessage = ref('');
const referenceData = ref(null);
const form = ref({
  name: '',
  email: '',
  cpf: '',
  birthDate: '',
  gender: '',
  quotaCategory: '',
  phone: '',
  state: '',
  city: '',
  linkedin: '',
  zipCode: '',
  address: '',
  addressExtra: '',
  educationLevel: '',
  institutionName: '',
  courseName: '',
  educationStatus: '',
  educationCompletionDate: ''
});

const asOptionList = (value, fallback) => {
  if (!Array.isArray(value) || !value.length) return fallback;
  const mapped = value
    .map((item) => {
      if (typeof item === 'string') return item;
      if (item && typeof item === 'object') {
        return item.label || item.value || item.nome || item.name || '';
      }
      return '';
    })
    .map((item) => item.toString().trim())
    .filter(Boolean);
  return mapped.length ? mapped : fallback;
};

const genderOptions = computed(() => asOptionList(referenceData.value?.generoOptions, fallbackGenderOptions));
const quotaOptions = computed(() => asOptionList(referenceData.value?.cotaOptions, fallbackQuotaOptions));
const stateOptions = computed(() => asOptionList(referenceData.value?.estadoOptions, fallbackStateOptions));
const educationTypeOptions = computed(() => fixedEducationTypeOptions);
const educationStatusOptions = computed(() => asOptionList(referenceData.value?.statusFormacaoOptions, fallbackEducationStatusOptions));
const cpfDigits = computed(() => (form.value.cpf || '').replace(/\D/g, ''));
const saveDisabled = computed(() => saving.value || !form.value.name.trim() || !form.value.email.trim() || !form.value.birthDate || cpfDigits.value.length !== 11);

const formatInputDate = (value) => {
  if (!value) return '';
  if (typeof value === 'string' && value.length >= 10) return value.slice(0, 10);
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return '';
  return parsed.toISOString().slice(0, 10);
};

const toNullableString = (value) => {
  const normalized = (value ?? '').toString().trim();
  return normalized ? normalized : null;
};

const load = async () => {
  loading.value = true;
  errorMessage.value = '';

  try {
    const [personResult, referenceResult] = await Promise.allSettled([
      peopleService.getById(props.personId),
      peopleService.getReferenceData()
    ]);

    if (personResult.status !== 'fulfilled') {
      throw personResult.reason;
    }

    if (referenceResult.status === 'fulfilled') {
      referenceData.value = referenceResult.value;
    }

    const person = personResult.value || {};
    form.value = {
      name: person.name || '',
      email: person.email || '',
      cpf: person.cpf || '',
      birthDate: formatInputDate(person.birthDate),
      gender: person.gender || '',
      quotaCategory: person.quotaCategory || '',
      phone: person.phone || '',
      state: person.state || '',
      city: person.city || '',
      linkedin: person.linkedin || '',
      zipCode: person.zipCode || '',
      address: person.address || '',
      addressExtra: person.addressExtra || '',
      educationLevel: person.educationLevel || '',
      institutionName: person.institutionName || '',
      courseName: person.courseName || '',
      educationStatus: person.educationStatus || '',
      educationCompletionDate: formatInputDate(person.educationCompletionDate)
    };
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || err?.message || 'Erro ao carregar dados da pessoa.';
  } finally {
    loading.value = false;
  }
};

const save = async () => {
  if (saveDisabled.value) {
    errorMessage.value = 'Preencha os campos obrigatórios: nome, e-mail, CPF válido e data de nascimento.';
    return;
  }

  saving.value = true;
  errorMessage.value = '';

  try {
    const payload = {
      name: form.value.name.trim(),
      email: form.value.email.trim(),
      cpf: cpfDigits.value,
      birthDate: form.value.birthDate,
      gender: toNullableString(form.value.gender),
      quotaCategory: toNullableString(form.value.quotaCategory),
      phone: toNullableString(form.value.phone),
      state: toNullableString(form.value.state),
      city: toNullableString(form.value.city),
      linkedin: toNullableString(form.value.linkedin),
      zipCode: toNullableString(form.value.zipCode),
      address: toNullableString(form.value.address),
      addressExtra: toNullableString(form.value.addressExtra),
      educationLevel: toNullableString(form.value.educationLevel),
      institutionName: toNullableString(form.value.institutionName),
      courseName: toNullableString(form.value.courseName),
      educationStatus: toNullableString(form.value.educationStatus),
      educationCompletionDate: form.value.educationCompletionDate || null
    };

    await peopleService.update(props.personId, payload);
    emit('saved');
    close();
  } catch (err) {
    const details = err?.response?.data?.details;
    if (Array.isArray(details) && details.length) {
      errorMessage.value = details.join(' ');
      return;
    }
    errorMessage.value = err?.response?.data?.message || err?.message || 'Erro ao salvar alterações.';
  } finally {
    saving.value = false;
  }
};

const close = () => emit('close');

onMounted(load);
</script>

<style scoped>
.edit-person-modal__overlay {
  position: fixed;
  inset: 0;
  background: rgba(8, 15, 28, 0.56);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 4000;
  padding: 18px;
}

.edit-person-modal__card {
  width: min(980px, 100%);
  max-height: 92vh;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 22px 60px rgba(8, 15, 28, 0.28);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.edit-person-modal__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 22px 14px;
  border-bottom: 1px solid #e2eaf2;
}

.edit-person-modal__head h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
}

.edit-person-modal__head p {
  margin: 4px 0 0;
  color: #6a7a90;
  font-size: 14px;
  line-height: 1.4;
}

.edit-person-modal__close {
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

.edit-person-modal__close:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.edit-person-modal__loading {
  padding: 20px 22px;
  color: #475569;
}

.edit-person-modal__form {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.edit-person-modal__body {
  padding: 16px 22px 8px;
  overflow: auto;
}

.edit-person-modal__section + .edit-person-modal__section {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #e2eaf2;
}

.edit-person-modal__section h3 {
  margin: 0 0 12px;
  color: #0f172a;
  font-size: 18px;
  font-weight: 600;
}

.edit-person-modal__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 18px;
}

.edit-person-modal__field {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.edit-person-modal__field--full {
  grid-column: 1 / -1;
}

.edit-person-modal__field label {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.edit-person-modal__field input,
.edit-person-modal__field select {
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

.edit-person-modal__field input:focus,
.edit-person-modal__field select:focus {
  border-color: #14b8a6;
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.1);
}

.edit-person-modal__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 22px 18px;
  border-top: 1px solid #e2eaf2;
}

.edit-person-modal__btn {
  height: 40px;
  border-radius: 12px;
  border: 0;
  padding: 0 14px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.edit-person-modal__btn--primary {
  color: #fff;
  background: #14b8a6;
}

.edit-person-modal__btn--secondary {
  background: #fff;
  border: 1px solid #d7e1eb;
  color: #1f2a3d;
}

.edit-person-modal__btn--primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.edit-person-modal__alert {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #fff0f0;
  color: #b42318;
}

@media (max-width: 900px) {
  .edit-person-modal__grid {
    grid-template-columns: 1fr;
  }

  .edit-person-modal__field--full {
    grid-column: auto;
  }
}
</style>