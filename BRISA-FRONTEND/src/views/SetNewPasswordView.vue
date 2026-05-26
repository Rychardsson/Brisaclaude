<template>
  <div class="password-container">
    <div class="password-card">

      <!-- Logo + nome -->
      <div class="logo-section">
        <div class="logo-circle">
          <img src="https://inscricoesrestic.brisabr.com.br/_next/image?url=%2Flogo_no-txt.png&w=256&q=75" alt="logo" class="logo">
        </div>
        <div class="brand-name">
          <h1>Brisa</h1>
          <h1 class="brand-highlight">One</h1>
        </div>
      </div>

      <!-- Mensagem de Sucesso -->
      <div v-if="successMessage" class="success-banner">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
          <path d="M20 6L9 17L4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>{{ successMessage }}</span>
      </div>

      <!-- Mensagem de Erro -->
      <div v-if="errorMessage" class="error-banner">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <path d="M12 8V12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <circle cx="12" cy="16" r="1" fill="currentColor"/>
        </svg>
        <span>{{ errorMessage }}</span>
      </div>

      <!-- Corpo: título + campos de senha + botão -->
      <div class="password-body">
        <h2 class="password-title">Definir nova senha</h2>
        <p class="password-description">
          Digite sua nova senha. Certifique-se de que é segura e diferente da anterior.
        </p>

        <!-- Campo de Nova Senha -->
        <div class="form-group">
          <label for="newPassword">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="11" width="18" height="10" rx="2" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Nova Senha
          </label>
          <div class="password-wrapper">
            <input
              id="newPassword"
              v-model="newPassword"
              :type="showNewPassword ? 'text' : 'password'"
              placeholder="Digite sua nova senha"
              autocomplete="new-password"
              @keydown.enter="handleSubmit"
              :disabled="loading"
            />
            <button
              type="button"
              class="eye-button"
              :aria-label="showNewPassword ? 'Ocultar senha' : 'Mostrar senha'"
              @click="toggleNewPassword"
            >
              <svg v-if="showNewPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M17.94 17.94C16.18 19.3 14.13 20 12 20C7 20 2.73 16.89 1 12C1.56 10.42 2.42 8.96 3.53 7.71M9.9 4.24C10.58 4.08 11.29 4 12 4C17 4 21.27 7.11 23 12C22.48 13.46 21.72 14.82 20.77 16.04M14.12 14.12C13.84 14.42 13.5 14.66 13.12 14.82C12.74 14.97 12.33 15.03 11.92 14.98C11.51 14.93 11.13 14.77 10.8 14.5C10.47 14.22 10.2 13.84 10.05 13.43C9.9 13.03 9.86 12.59 9.92 12.16C9.98 11.73 10.15 11.32 10.44 10.97" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M1 1L23 23" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M1 12C2.73 7.11 7 4 12 4C17 4 21.27 7.11 23 12C21.27 16.89 17 20 12 20C7 20 2.73 16.89 1 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- Campo de Confirmação de Senha -->
        <div class="form-group">
          <label for="confirmPassword">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="11" width="18" height="10" rx="2" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Confirmar Senha
          </label>
          <div class="password-wrapper">
            <input
              id="confirmPassword"
              v-model="confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="Confirme sua nova senha"
              autocomplete="new-password"
              @keydown.enter="handleSubmit"
              :disabled="loading"
            />
            <button
              type="button"
              class="eye-button"
              :aria-label="showConfirmPassword ? 'Ocultar senha' : 'Mostrar senha'"
              @click="toggleConfirmPassword"
            >
              <svg v-if="showConfirmPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M17.94 17.94C16.18 19.3 14.13 20 12 20C7 20 2.73 16.89 1 12C1.56 10.42 2.42 8.96 3.53 7.71M9.9 4.24C10.58 4.08 11.29 4 12 4C17 4 21.27 7.11 23 12C22.48 13.46 21.72 14.82 20.77 16.04M14.12 14.12C13.84 14.42 13.5 14.66 13.12 14.82C12.74 14.97 12.33 15.03 11.92 14.98C11.51 14.93 11.13 14.77 10.8 14.5C10.47 14.22 10.2 13.84 10.05 13.43C9.9 13.03 9.86 12.59 9.92 12.16C9.98 11.73 10.15 11.32 10.44 10.97" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M1 1L23 23" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M1 12C2.73 7.11 7 4 12 4C17 4 21.27 7.11 23 12C21.27 16.89 17 20 12 20C7 20 2.73 16.89 1 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
              </svg>
            </button>
          </div>
        </div>

        <button class="btn-submit" :disabled="loading || !isFormValid" @click="handleSubmit">
          <span v-if="!loading">Redefinir senha</span>
          <span v-else class="loading-text">
            <svg class="spinner" width="18" height="18" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" stroke-opacity="0.25"/>
              <path d="M12 2C6.47715 2 2 6.47715 2 12" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            </svg>
            Redefinindo...
          </span>
        </button>
      </div>

      <!-- Link de voltar -->
      <div class="footer-text">
        <router-link to="/" class="back-link">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          Voltar para o login
        </router-link>
      </div>

    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { authService } from '@/services/authService';

export default {
  name: 'SetNewPasswordView',
  setup() {
    const route = useRoute();
    const router = useRouter();
    
    const newPassword = ref('');
    const confirmPassword = ref('');
    const loading = ref(false);
    const errorMessage = ref('');
    const successMessage = ref('');
    const showNewPassword = ref(false);
    const showConfirmPassword = ref(false);
    const token = ref('');

    const isFormValid = computed(() => {
      if (!newPassword.value || !confirmPassword.value) return false;
      if (newPassword.value.length < 6) return false;
      return newPassword.value === confirmPassword.value;
    });

    const toggleNewPassword = () => {
      showNewPassword.value = !showNewPassword.value;
    };

    const toggleConfirmPassword = () => {
      showConfirmPassword.value = !showConfirmPassword.value;
    };

    const handleSubmit = async () => {
      if (!isFormValid.value) return;

      loading.value = true;
      errorMessage.value = '';
      successMessage.value = '';

      try {
        await authService.resetPassword(token.value, newPassword.value);
        successMessage.value = 'Senha redefinida com sucesso! Você será redirecionado para o login.';
        setTimeout(() => {
          router.push('/');
        }, 2000);
      } catch (error) {
        errorMessage.value = error.response?.data || 'Erro ao redefinir senha. Tente novamente.';
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      token.value = route.query.token;
      if (!token.value) {
        errorMessage.value = 'Token inválido ou ausente. Solicite um novo link de recuperação.';
      }
    });

    return {
      newPassword,
      confirmPassword,
      loading,
      errorMessage,
      successMessage,
      showNewPassword,
      showConfirmPassword,
      isFormValid,
      toggleNewPassword,
      toggleConfirmPassword,
      handleSubmit
    };
  }
};
</script>

<style scoped>
.password-container {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1F285F 0%, #0288d1 100%);
  padding: 20px;
  z-index: 9999;
}

.password-card {
  background: white;
  padding: 48px 40px;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(31, 40, 95, 0.25);
  width: 100%;
  max-width: 420px;
  text-align: center;
}

/* Logo */
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32px;
}

.logo-circle {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #1F285F 0%, #0288d1 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  box-shadow: 0 4px 12px rgba(31, 40, 95, 0.2);
}

.logo {
  height: 80px;
}

.brand-name {
  display: flex;
  justify-content: center;
  gap: 6px;
}

.brand-name h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1F285F;
}

.brand-highlight {
  color: #0288d1 !important;
}

/* Corpo */
.password-body {
  padding: 24px 0 28px;
  border-top: 1px solid #e0e0e0;
  border-bottom: 1px solid #e0e0e0;
  margin-bottom: 28px;
}

.password-title {
  margin: 0 0 10px;
  color: #1F285F;
  font-size: 20px;
  font-weight: 700;
}

.password-description {
  margin: 0 0 24px;
  color: #777;
  font-size: 15.5px;
  line-height: 1.7;
}

/* Campo de Senha */
.form-group {
  display: flex;
  flex-direction: column;
  text-align: left;
  margin-bottom: 15px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #1F285F;
  font-weight: 600;
  font-size: 16px;
}

.form-group label svg {
  color: #0288d1;
}

.password-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-wrapper input {
  width: 100%;
  padding: 14px 40px 14px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.2s ease;
  background: white;
  color: #333;
  outline: none;
}

.password-wrapper input:focus {
  border-color: #0288d1;
  box-shadow: 0 0 0 4px rgba(2, 136, 209, 0.08);
}

.password-wrapper input::placeholder {
  color: #aaa;
}

.eye-button {
  position: absolute;
  right: 12px;
  background: none;
  border: none;
  cursor: pointer;
  color: #666;
  transition: color 0.2s ease;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.eye-button:hover {
  color: #0288d1;
}

/* Botão */
.btn-submit {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #1F285F 0%, #0288d1 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(31, 40, 95, 0.2);
  margin-top: 10px;
}

.btn-submit:hover:not(:disabled) {
  box-shadow: 0 6px 20px rgba(31, 40, 95, 0.3);
  transform: translateY(-1px);
}

.btn-submit:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
}

.loading-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.spinner {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

/* Link de voltar */
.footer-text {
  display: flex;
  justify-content: center;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #0288d1;
  font-size: 17px;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s ease;
}

.back-link:hover {
  color: #1F285F;
}

/* Banners de Sucesso e Erro */
.success-banner,
.error-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 20px;
  animation: slideDown 0.3s ease;
}

.success-banner {
  background-color: #ecfdf5;
  border: 1px solid #a7f3d0;
  color: #065f46;
}

.success-banner svg {
  color: #10b981;
  flex-shrink: 0;
}

.error-banner {
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  color: #991b1b;
}

.error-banner svg {
  color: #ef4444;
  flex-shrink: 0;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
