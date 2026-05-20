<template>
  <div class="login-container">
    <div class="login-card">
      <div class="logo-section">
        <div class="logo-circle">
          <img src="https://inscricoesrestic.brisabr.com.br/_next/image?url=%2Flogo_no-txt.png&w=256&q=75" alt="logo" class="logo">
        </div>
        <div id="brand-name">
          <h1>Brisa</h1>
          <h1 id="brand-highlight">One</h1>
        </div>
        <h2 class="brand-subtitle">Tudo em um só lugar</h2>
      </div>

      <form @submit.prevent="handleLogin">
        <div v-if="error" class="error-message">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="#d32f2f" stroke-width="2"/>
            <path d="M12 8V12" stroke="#d32f2f" stroke-width="2" stroke-linecap="round"/>
            <circle cx="12" cy="16" r="1" fill="#d32f2f"/>
          </svg>
          <span>{{ error }}</span>
        </div>

        <div class="form-group">
          <label for="login">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Usuário
          </label>
          <input
            id="login"
            v-model="credentials.login"
            type="text"
            placeholder="Digite seu usuário"
            required
            autocomplete="username"
            @keyup.enter="handleLogin"
          />
        </div>

        <div class="form-group">
          <label for="password">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Senha
          </label>
          <input
            id="password"
            v-model="credentials.password"
            type="password"
            placeholder="Digite sua senha"
            required
            autocomplete="current-password"
            @keyup.enter="handleLogin"
          />
        </div>

        <button type="submit" :disabled="loading" class="btn-login">
          <span v-if="!loading">Entrar</span>
          <span v-else class="loading-text">
            <svg class="spinner" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" stroke-opacity="0.25"/>
              <path d="M12 2C6.47715 2 2 6.47715 2 12" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            </svg>
            Entrando...
          </span>
        </button>
      </form>

      <div class="footer-text">
        <p>
          Esqueceu sua senha?
          <router-link to="/reset-password" class="reset-link">Clique aqui.</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { authService } from '@/services/authService';

export default {
  name: 'LoginView',
  setup() {
    const router = useRouter();
    const credentials = ref({
      login: '',
      password: ''
    });
    const loading = ref(false);
    const error = ref('');

    const handleLogin = async () => {
      loading.value = true;
      error.value = '';

      try {
        await authService.login(credentials.value.login, credentials.value.password);
        router.push('/dashboard');
      } catch (err) {
        error.value = err.response?.data?.message || 'Erro ao fazer login. Verifique suas credenciais.';
      } finally {
        loading.value = false;
      }
    };

    const handleKeyEnter = (e) => {
      if (e.key === 'Enter' && !loading.value) handleLogin();
    };

    onMounted(() => {
      document.addEventListener('keydown', handleKeyEnter);
    });

    onUnmounted(() => {
      document.removeEventListener('keydown', handleKeyEnter);
    });

    return {
      credentials,
      loading,
      error,
      handleLogin
    };
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1F285F 0%, #0288d1 100%);
  padding: 20px;
}

.login-card {
  background: white;
  padding: 48px 40px;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(31, 40, 95, 0.25);
  width: 100%;
  max-width: 420px;
}
#brand-name {
  justify-content: center;
  display: flex;
  align-items: center;
  gap: 5px;
}
#brand-highlight {
  color: #6ebae3;
  font-family: montserrat, sans-serif;
  padding-top: 10px;
  font-weight: 400;
  font-size: 30px;
}

.logo-section {
  text-align: center;
  margin-bottom: 32px;
}
.logo{
  height: 80px;
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

h1 {
  margin: 0;
  color: #1F285F;
  font-size: 36px;
  font-weight: 700;
  letter-spacing: 2px;
}

.brand-subtitle {
  margin: 8px 0 0 0;
  color: #666;
  font-size: 16px;
  font-weight: 500;
  text-align: center;
}

.subtitle {
  color: #666;
  margin: 0;
  font-size: 14px;
  font-weight: 500;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background-color: #ffebee;
  border: 1px solid #ef5350;
  border-radius: 10px;
  color: #c62828;
  font-size: 14px;
  margin-bottom: 24px;
}

.error-message svg {
  flex-shrink: 0;
}

.error-message span {
  flex: 1;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #1F285F;
  font-weight: 600;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 15px;
  box-sizing: border-box;
  transition: all 0.2s ease;
  background: #fafafa;
}

.form-group input:focus {
  outline: none;
  border-color: #0288d1;
  background: white;
  box-shadow: 0 0 0 4px rgba(2, 136, 209, 0.08);
}

.form-group input::placeholder {
  color: #999;
}

.btn-login {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #1F285F 0%, #0288d1 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 8px;
  box-shadow: 0 4px 12px rgba(31, 40, 95, 0.2);
}

.btn-login:hover:not(:disabled) {
  box-shadow: 0 6px 20px rgba(31, 40, 95, 0.3);
  transform: translateY(-1px);
}

.btn-login:active:not(:disabled) {
  transform: translateY(0);
}

.btn-login:disabled {
  opacity: 0.7;
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
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.footer-text {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e0e0e0;
  text-align: center;
}

.footer-text p {
  margin: 0;
  color: #999;
  font-size: 16.5px;
}

.reset-link {
  color: #0288d1;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.2s ease;
}

.reset-link:hover {
  color: #1F285F;
  text-decoration: underline;
}
</style>