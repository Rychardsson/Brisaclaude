<template>
  <main class="career-public-page">
    <section class="career-public-shell">
      <header class="career-public-hero">
        <div>
          <p class="career-public-kicker">BRISA One</p>
          <h1>Formulario de acompanhamento da carreira</h1>
          <p>
            Este espaco foi preparado para que o egresso confirme sua identidade e compartilhe seu
            momento profissional atual com a equipe BRISA.
          </p>
        </div>
        <div class="career-public-badge">
          <span>Fluxo seguro</span>
          <strong>Email + token</strong>
        </div>
      </header>

      <section class="career-public-card" v-if="!validatedAccess">
        <div class="career-public-card-head">
          <div>
            <h2>Validar acesso</h2>
            <p>Confirme o e-mail que recebeu a mensagem e o token informado no disparo.</p>
          </div>
          <span class="career-public-step">Etapa 1</span>
        </div>

        <form class="career-public-form" @submit.prevent="validateAccess">
          <label class="career-public-field">
            <span>E-mail</span>
            <input
              v-model="validationForm.email"
              type="email"
              autocomplete="email"
              placeholder="nome@exemplo.com"
            />
          </label>

          <label class="career-public-field">
            <span>Token</span>
            <input
              v-model="validationForm.token"
              type="text"
              autocomplete="off"
              placeholder="Cole o token recebido no e-mail"
            />
          </label>

          <p v-if="validationError" class="career-public-alert career-public-alert-error">
            {{ validationError }}
          </p>

          <div class="career-public-actions">
            <router-link to="/" class="career-public-link">Voltar ao sistema</router-link>
            <button type="submit" class="career-public-primary" :disabled="validating">
              {{ validating ? 'Validando...' : 'Validar e continuar' }}
            </button>
          </div>
        </form>
      </section>

      <section v-else-if="!isSubmitted" class="career-public-card">
        <div class="career-public-card-head">
          <div>
            <h2>Responder acompanhamento</h2>
            <p>Seu retorno sera registrado diretamente no historico de carreira do programa.</p>
          </div>
          <span class="career-public-step">Etapa 2</span>
        </div>

        <div class="career-public-context">
          <article class="career-public-context-card">
            <small>Egresso</small>
            <strong>{{ validatedAccess.personName || validationForm.email }}</strong>
            <span>{{ validationForm.email }}</span>
          </article>
          <article class="career-public-context-card">
            <small>Programa</small>
            <strong>{{ validatedAccess.programName || 'BRISA' }}</strong>
            <span>{{ validatedAccess.classCode || 'Turma em acompanhamento' }}</span>
          </article>
          <article class="career-public-context-card">
            <small>Checkpoint</small>
            <strong>{{ validatedAccess.checkpointMonths }} meses</strong>
            <span>Previsto para {{ formatDate(validatedAccess.dueDate) }}</span>
          </article>
        </div>

        <div v-if="validatedAccess.latestFollowUp" class="career-public-history">
          <div class="career-public-history-head">
            <strong>Ultimo acompanhamento registrado</strong>
            <span>{{ formatDate(validatedAccess.latestFollowUp.surveyDate) }}</span>
          </div>
          <p>
            {{ statusLabel(validatedAccess.latestFollowUp.status) }}
            <template v-if="validatedAccess.latestFollowUp.company">
              | {{ validatedAccess.latestFollowUp.company }}
            </template>
            <template v-if="validatedAccess.latestFollowUp.position">
              | {{ validatedAccess.latestFollowUp.position }}
            </template>
          </p>
        </div>

        <form class="career-public-form" @submit.prevent="submitFollowUp">
          <div class="career-public-grid">
            <label class="career-public-field">
              <span>Data da resposta</span>
              <input v-model="followUpForm.surveyDate" type="date" />
            </label>

            <label class="career-public-field">
              <span>Situacao profissional</span>
              <select v-model="followUpForm.status">
                <option value="">Selecione</option>
                <option value="EMPREGADO">Empregado</option>
                <option value="DESEMPREGADO">Desempregado</option>
                <option value="SEM_RESPOSTA">Sem resposta</option>
              </select>
            </label>

            <label class="career-public-field">
              <span>Empresa</span>
              <input
                v-model="followUpForm.company"
                type="text"
                placeholder="Informe a empresa, se houver"
              />
            </label>

            <label class="career-public-field">
              <span>Cargo</span>
              <input
                v-model="followUpForm.position"
                type="text"
                placeholder="Informe o cargo atual"
              />
            </label>
          </div>

          <label class="career-public-field">
            <span>Conte um pouco sobre seu momento profissional</span>
            <textarea
              v-model="followUpForm.notes"
              rows="5"
              placeholder="Ex: area em que esta atuando, busca por recolocacao, projetos recentes ou observacoes que queira compartilhar."
            ></textarea>
          </label>

          <p v-if="submitError" class="career-public-alert career-public-alert-error">
            {{ submitError }}
          </p>

          <p v-if="followUpForm.status === 'EMPREGADO' && !followUpForm.position.trim()" class="career-public-alert">
            Para marcar como empregado, informe tambem o cargo atual.
          </p>

          <div class="career-public-actions">
            <button type="button" class="career-public-secondary" @click="resetAccess" :disabled="submitting">
              Alterar validacao
            </button>
            <button type="submit" class="career-public-primary" :disabled="submitting || !canSubmit">
              {{ submitting ? 'Enviando...' : 'Enviar resposta' }}
            </button>
          </div>
        </form>
      </section>

      <section v-else class="career-public-card career-public-card-success">
        <div class="career-public-card-head">
          <div>
            <h2>Resposta registrada</h2>
            <p>Obrigado por atualizar suas informacoes. O acompanhamento da carreira foi salvo.</p>
          </div>
          <span class="career-public-step">Concluido</span>
        </div>

        <div class="career-public-context">
          <article class="career-public-context-card">
            <small>Egresso</small>
            <strong>{{ validatedAccess?.personName || validationForm.email }}</strong>
            <span>{{ validationForm.email }}</span>
          </article>
          <article class="career-public-context-card">
            <small>Situacao</small>
            <strong>{{ statusLabel(submittedSnapshot?.status) }}</strong>
            <span>{{ formatDate(submittedSnapshot?.surveyDate) }}</span>
          </article>
          <article class="career-public-context-card">
            <small>Checkpoint</small>
            <strong>{{ validatedAccess?.checkpointMonths }} meses</strong>
            <span>{{ validatedAccess?.classCode || 'Turma em acompanhamento' }}</span>
          </article>
        </div>

        <div v-if="submittedSnapshot" class="career-public-summary">
          <p v-if="submittedSnapshot.company"><strong>Empresa:</strong> {{ submittedSnapshot.company }}</p>
          <p v-if="submittedSnapshot.position"><strong>Cargo:</strong> {{ submittedSnapshot.position }}</p>
          <p v-if="submittedSnapshot.notes"><strong>Observacoes:</strong> {{ submittedSnapshot.notes }}</p>
        </div>

        <div class="career-public-actions">
          <router-link to="/" class="career-public-link">Fechar</router-link>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { careerService } from '@/services/careerService';

const route = useRoute();

const validating = ref(false);
const submitting = ref(false);
const validationError = ref('');
const submitError = ref('');
const validatedAccess = ref(null);
const submittedFollowUp = ref(null);

const validationForm = reactive({
  email: '',
  token: ''
});

const followUpForm = reactive(defaultFollowUpForm());

watch(
  () => route.query.token,
  (value) => {
    validationForm.token = typeof value === 'string' ? value : '';
  },
  { immediate: true }
);

const submittedSnapshot = computed(() => submittedFollowUp.value || validatedAccess.value?.latestFollowUp || null);

const isSubmitted = computed(() => {
  if (submittedFollowUp.value) return true;
  return Boolean(validatedAccess.value?.alreadySubmitted);
});

const canSubmit = computed(() => {
  if (!validatedAccess.value) return false;
  if (!followUpForm.surveyDate || !followUpForm.status) return false;
  if (followUpForm.status === 'EMPREGADO' && !followUpForm.position.trim()) return false;
  return true;
});

async function validateAccess() {
  validationError.value = '';
  submitError.value = '';
  validating.value = true;

  try {
    const access = await careerService.validatePublicAccess({
      email: validationForm.email,
      token: validationForm.token
    });

    validatedAccess.value = access;
    hydrateFollowUpForm(access?.latestFollowUp);

    if (access?.alreadySubmitted && access?.latestFollowUp) {
      submittedFollowUp.value = access.latestFollowUp;
    } else {
      submittedFollowUp.value = null;
    }
  } catch (error) {
    validatedAccess.value = null;
    submittedFollowUp.value = null;
    validationError.value = extractApiMessage(error, 'Nao foi possivel validar o acesso.');
  } finally {
    validating.value = false;
  }
}

async function submitFollowUp() {
  submitError.value = '';
  submitting.value = true;

  try {
    const response = await careerService.submitPublicFollowUp({
      email: validationForm.email,
      token: validationForm.token,
      surveyDate: followUpForm.surveyDate,
      status: followUpForm.status,
      company: followUpForm.company,
      position: followUpForm.position,
      notes: followUpForm.notes
    });

    submittedFollowUp.value = response;
    validatedAccess.value = {
      ...(validatedAccess.value || {}),
      alreadySubmitted: true,
      latestFollowUp: response
    };
  } catch (error) {
    submitError.value = extractApiMessage(error, 'Nao foi possivel enviar sua resposta agora.');
  } finally {
    submitting.value = false;
  }
}

function resetAccess() {
  validatedAccess.value = null;
  submittedFollowUp.value = null;
  validationError.value = '';
  submitError.value = '';
  hydrateFollowUpForm(null);
}

function hydrateFollowUpForm(latestFollowUp) {
  followUpForm.surveyDate = latestFollowUp?.surveyDate || todayIso();
  followUpForm.status = latestFollowUp?.status || '';
  followUpForm.company = latestFollowUp?.company || '';
  followUpForm.position = latestFollowUp?.position || '';
  followUpForm.notes = latestFollowUp?.notes || '';
}

function defaultFollowUpForm() {
  return {
    surveyDate: todayIso(),
    status: '',
    company: '',
    position: '',
    notes: ''
  };
}

function todayIso() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function formatDate(value) {
  if (!value) return 'Sem data';

  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) return value;

  return new Intl.DateTimeFormat('pt-BR').format(date);
}

function statusLabel(status) {
  if (status === 'EMPREGADO') return 'Empregado';
  if (status === 'DESEMPREGADO') return 'Desempregado';
  if (status === 'SEM_RESPOSTA') return 'Sem resposta';
  return 'Nao informado';
}

function extractApiMessage(error, fallback) {
  const details = error?.response?.data?.details;
  if (Array.isArray(details) && details.length) {
    return details.join(' ');
  }

  const message = error?.response?.data?.message || error?.message;
  return message || fallback;
}
</script>

<style scoped>
.career-public-page {
  min-height: 100vh;
  padding: 48px 20px;
  background:
    radial-gradient(circle at top left, rgba(20, 184, 166, 0.24), transparent 28%),
    linear-gradient(160deg, #f6fbfa 0%, #eef4fb 52%, #f8fbff 100%);
}

.career-public-shell {
  width: min(920px, 100%);
  margin: 0 auto;
  display: grid;
  gap: 24px;
}

.career-public-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 32px;
  border-radius: 24px;
  background: linear-gradient(135deg, #0f3f53 0%, #0f766e 54%, #16a394 100%);
  color: #f8fffd;
  box-shadow: 0 20px 42px rgba(15, 63, 83, 0.18);
}

.career-public-kicker {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: rgba(240, 255, 250, 0.8);
}

.career-public-hero h1 {
  margin: 0 0 12px;
  font-size: clamp(28px, 4vw, 38px);
  line-height: 1.1;
}

.career-public-hero p {
  margin: 0;
  max-width: 620px;
  font-size: 15px;
  line-height: 1.75;
  color: rgba(240, 255, 250, 0.88);
}

.career-public-badge {
  min-width: 170px;
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.18);
  text-align: right;
}

.career-public-badge span,
.career-public-badge strong {
  display: block;
}

.career-public-badge span {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: rgba(240, 255, 250, 0.74);
}

.career-public-badge strong {
  margin-top: 8px;
  font-size: 20px;
}

.career-public-card {
  padding: 28px 32px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(159, 188, 214, 0.34);
  box-shadow: 0 18px 36px rgba(15, 63, 83, 0.08);
}

.career-public-card-success {
  border-color: rgba(15, 118, 110, 0.24);
}

.career-public-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.career-public-card-head h2 {
  margin: 0 0 6px;
  font-size: 24px;
  color: #123046;
}

.career-public-card-head p {
  margin: 0;
  color: #5e7284;
  line-height: 1.65;
}

.career-public-step {
  padding: 10px 14px;
  border-radius: 999px;
  background: #edf8f6;
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.career-public-context {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 22px;
}

.career-public-context-card {
  padding: 16px 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #f8fcff 0%, #f3f8fb 100%);
  border: 1px solid #d8e7f0;
}

.career-public-context-card small,
.career-public-context-card strong,
.career-public-context-card span {
  display: block;
}

.career-public-context-card small {
  margin-bottom: 8px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: #6e7f8e;
}

.career-public-context-card strong {
  font-size: 16px;
  color: #123046;
}

.career-public-context-card span {
  margin-top: 6px;
  color: #5e7284;
  line-height: 1.55;
}

.career-public-history,
.career-public-summary {
  margin-bottom: 22px;
  padding: 16px 18px;
  border-radius: 18px;
  background: #f9fbfe;
  border: 1px solid #dce7f2;
}

.career-public-history-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.career-public-history strong,
.career-public-summary strong {
  color: #123046;
}

.career-public-history p,
.career-public-summary p {
  margin: 0;
  color: #4f6374;
  line-height: 1.65;
}

.career-public-form {
  display: grid;
  gap: 18px;
}

.career-public-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.career-public-field {
  display: grid;
  gap: 8px;
}

.career-public-field span {
  font-size: 13px;
  font-weight: 700;
  color: #385166;
}

.career-public-field input,
.career-public-field select,
.career-public-field textarea {
  width: 100%;
  padding: 14px 15px;
  border-radius: 14px;
  border: 1px solid #cfe0eb;
  background: #ffffff;
  color: #183447;
  font-size: 15px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.career-public-field textarea {
  min-height: 138px;
  resize: vertical;
}

.career-public-field input:focus,
.career-public-field select:focus,
.career-public-field textarea:focus {
  outline: none;
  border-color: #14b8a6;
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.14);
}

.career-public-alert {
  margin: 0;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f3fbfa;
  color: #27535a;
  line-height: 1.6;
}

.career-public-alert-error {
  background: #fff3f2;
  color: #9b3c36;
  border: 1px solid #f2d0cb;
}

.career-public-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}

.career-public-primary,
.career-public-secondary,
.career-public-link {
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;
}

.career-public-primary,
.career-public-secondary {
  border: 0;
  cursor: pointer;
  padding: 14px 20px;
}

.career-public-primary {
  background: linear-gradient(135deg, #0f766e 0%, #14b8a6 100%);
  color: #ffffff;
  box-shadow: 0 10px 20px rgba(15, 118, 110, 0.18);
}

.career-public-primary:disabled,
.career-public-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.career-public-secondary {
  background: #ecf2f7;
  color: #294155;
}

.career-public-link {
  color: #0f766e;
}

@media (max-width: 760px) {
  .career-public-page {
    padding: 24px 14px;
  }

  .career-public-hero,
  .career-public-card-head,
  .career-public-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .career-public-card,
  .career-public-hero {
    padding: 22px 20px;
    border-radius: 20px;
  }

  .career-public-badge {
    min-width: 0;
    text-align: left;
  }

  .career-public-context,
  .career-public-grid {
    grid-template-columns: 1fr;
  }
}
</style>
