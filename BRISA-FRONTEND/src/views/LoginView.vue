<template>
  <div class="login-page">
    <section class="brand-side" aria-hidden="true">
      <img :src="brandReferenceUrl" alt="" class="brand-reference" />
    </section>

    <section class="form-side">
      <div class="login-card">
        <div class="card-logo-shell">
          <img :src="brandSymbolUrl" alt="Brisa One" class="card-logo" />
        </div>

        <h2>Bem-vindo de volta!</h2>
        <p class="subtitle">Acesse sua conta para continuar.</p>

        <form class="login-form" @submit.prevent="handleLogin">
          <div v-if="error" class="error-banner" role="alert">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" />
              <path d="M12 8V12" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
              <circle cx="12" cy="16" r="1" fill="currentColor" />
            </svg>
            <span>{{ error }}</span>
          </div>

          <div class="form-group">
            <label for="usuario">
              <span class="icon" aria-hidden="true">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                  <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </span>
              Usu&aacute;rio
            </label>

            <input
              id="usuario"
              v-model="credentials.login"
              type="text"
              placeholder="Digite seu usu&aacute;rio"
              autocomplete="username"
              required
            />
          </div>

          <div class="form-group">
            <label for="senha">
              <span class="icon" aria-hidden="true">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <rect x="3" y="11" width="18" height="10" rx="2" stroke="currentColor" stroke-width="2" stroke-linejoin="round" />
                  <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </span>
              Senha
            </label>

            <div class="password-wrapper">
              <input
                id="senha"
                v-model="credentials.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="Digite sua senha"
                autocomplete="current-password"
                required
              />

              <button
                type="button"
                class="eye-button"
                :aria-label="showPassword ? 'Ocultar senha' : 'Mostrar senha'"
                @click="togglePassword"
              >
                <svg v-if="showPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M17.94 17.94C16.18 19.3 14.13 20 12 20C7 20 2.73 16.89 1 12C1.56 10.42 2.42 8.96 3.53 7.71M9.9 4.24C10.58 4.08 11.29 4 12 4C17 4 21.27 7.11 23 12C22.48 13.46 21.72 14.82 20.77 16.04M14.12 14.12C13.84 14.42 13.5 14.66 13.12 14.82C12.74 14.97 12.33 15.03 11.92 14.98C11.51 14.93 11.13 14.77 10.8 14.5C10.47 14.22 10.2 13.84 10.05 13.43C9.9 13.03 9.86 12.59 9.92 12.16C9.98 11.73 10.15 11.32 10.44 10.97" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                  <path d="M1 1L23 23" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                </svg>

                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M1 12C2.73 7.11 7 4 12 4C17 4 21.27 7.11 23 12C21.27 16.89 17 20 12 20C7 20 2.73 16.89 1 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                  <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2" />
                </svg>
              </button>
            </div>
          </div>

          <button type="submit" class="login-button" :disabled="loading">
            <span v-if="!loading">Entrar</span>

            <span v-else class="loading-state">
              <svg class="spinner" width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" stroke-opacity="0.28" />
                <path d="M12 2C6.47715 2 2 6.47715 2 12" stroke="currentColor" stroke-width="3" stroke-linecap="round" />
              </svg>
              Entrando...
            </span>
          </button>

          <div class="separator">
            <span></span>
            <small>ou</small>
            <span></span>
          </div>

          <p class="forgot-password">
            Esqueceu sua senha?
            <router-link to="/reset-password">Clique aqui.</router-link>
          </p>
        </form>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { authService } from '@/services/authService';
import brandReferenceUrl from '@/assets/login-brand-reference.jpg';

const router = useRouter();
const brandSymbolUrl = 'https://inscricoesrestic.brisabr.com.br/_next/image?url=%2Flogo_no-txt.png&w=256&q=75';

const credentials = ref({
  login: '',
  password: ''
});

const loading = ref(false);
const error = ref('');
const showPassword = ref(false);

async function handleLogin() {
  loading.value = true;
  error.value = '';

  try {
    await authService.login(credentials.value.login.trim(), credentials.value.password);
    router.push('/dashboard');
  } catch (err) {
    error.value = err.response?.data?.message || 'Erro ao fazer login. Verifique suas credenciais.';
  } finally {
    loading.value = false;
  }
}

function togglePassword() {
  showPassword.value = !showPassword.value;
}
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.login-page {
  width: 100%;
  min-height: 100vh;
  min-height: 100dvh;
  height: 100vh;
  height: 100dvh;
  display: flex;
  overflow: hidden;
  font-family: "Inter", "Segoe UI", Arial, sans-serif;
  background: #f5f9ff;
}

.brand-side {
  width: 50%;
  min-height: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  background: #072a6d;
}

.brand-reference {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  user-select: none;
  pointer-events: none;
}

.form-side {
  width: 50%;
  min-height: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28px 40px;
  background:
    radial-gradient(circle at 50% 8%, rgba(0, 136, 255, 0.06), transparent 0 26%),
    radial-gradient(circle at 84% 14%, rgba(0, 184, 255, 0.05), transparent 0 22%),
    linear-gradient(180deg, #fbfdff 0%, #f4f8fe 100%);
}

.login-card {
  width: 100%;
  max-width: 580px;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(222, 234, 246, 0.95);
  border-radius: 28px;
  padding: 34px 52px 36px;
  box-shadow:
    0 30px 90px rgba(17, 48, 96, 0.11),
    0 10px 32px rgba(17, 48, 96, 0.05);
  text-align: center;
  backdrop-filter: blur(12px);
}

.card-logo-shell {
  width: 98px;
  height: 98px;
  margin: 0 auto 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #173d93 0%, #118de3 100%);
  box-shadow:
    0 18px 34px rgba(17, 103, 188, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.card-logo {
  width: 62px;
  height: 62px;
  object-fit: contain;
}

.login-card h2 {
  margin: 0;
  color: #142567;
  font-size: 25px;
  font-weight: 800;
}

.subtitle {
  margin: 8px 0 30px;
  color: #586684;
  font-size: 16px;
}

.login-form {
  text-align: left;
}

.error-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 24px;
  border: 1px solid #f3c1c7;
  border-radius: 16px;
  background: #fff5f6;
  color: #b42318;
  font-size: 14px;
  font-weight: 600;
}

.error-banner svg {
  flex-shrink: 0;
}

.form-group {
  text-align: left;
  margin-bottom: 20px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  color: #142567;
  font-weight: 700;
  font-size: 15px;
}

.icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
}

.form-group input,
.password-wrapper input {
  width: 100%;
  height: 56px;
  border: 1px solid #d7e0ec;
  border-radius: 14px;
  padding: 0 18px;
  font-size: 15px;
  color: #202940;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  background: #ffffff;
}

.form-group input::placeholder,
.password-wrapper input::placeholder {
  color: #93a0b5;
}

.form-group input:focus,
.password-wrapper input:focus {
  border-color: #008fe3;
  box-shadow: 0 0 0 4px rgba(0, 143, 227, 0.1);
}

.password-wrapper {
  position: relative;
}

.password-wrapper input {
  padding-right: 52px;
}

.eye-button {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  padding: 0;
  background: transparent;
  color: #6f7c90;
  cursor: pointer;
  opacity: 0.7;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.2s ease, color 0.2s ease;
}

.eye-button:hover {
  opacity: 1;
  color: #142567;
}

.login-button {
  width: 100%;
  height: 58px;
  border: none;
  border-radius: 13px;
  cursor: pointer;
  color: #ffffff;
  font-size: 17px;
  font-weight: 700;
  margin-top: 8px;
  background: linear-gradient(90deg, #142567, #0098e5);
  box-shadow: 0 12px 28px rgba(0, 107, 190, 0.3);
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 16px 34px rgba(0, 107, 190, 0.38);
}

.login-button:disabled {
  opacity: 0.8;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 12px 28px rgba(0, 107, 190, 0.22);
}

.loading-state {
  display: inline-flex;
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

.separator {
  display: flex;
  align-items: center;
  gap: 18px;
  margin: 26px 0 22px;
  color: #7b8498;
}

.separator span {
  flex: 1;
  height: 1px;
  background: #e1e5ec;
}

.separator small {
  font-size: 15px;
  font-weight: 600;
}

.forgot-password {
  margin: 0;
  text-align: center;
  color: #7b8498;
  font-size: 15px;
}

.forgot-password a {
  color: #008fe3;
  font-weight: 700;
  text-decoration: none;
}

.forgot-password a:hover {
  text-decoration: underline;
}

@media (max-width: 900px) {
  .login-page {
    flex-direction: column;
    height: auto;
    min-height: 100vh;
    min-height: 100dvh;
    overflow: visible;
  }

  .brand-side,
  .form-side {
    width: 100%;
  }

  .brand-side {
    min-height: auto;
    aspect-ratio: 836 / 941;
  }

  .brand-reference {
    object-fit: cover;
  }

  .form-side {
    min-height: auto;
    padding: 32px 20px 40px;
  }

  .login-card {
    max-width: 620px;
    padding: 36px 32px 40px;
  }

  .card-logo-shell {
    width: 96px;
    height: 96px;
  }

  .card-logo {
    width: 62px;
    height: 62px;
  }
}

@media (max-width: 480px) {
  .brand-side {
    display: none;
  }

  .form-side {
    width: 100%;
    min-height: 100vh;
    padding: 20px;
  }

  .login-card {
    box-shadow: none;
    border-radius: 18px;
    padding: 30px 22px 32px;
  }

  .card-logo-shell {
    width: 88px;
    height: 88px;
    margin-bottom: 22px;
  }

  .card-logo {
    width: 56px;
    height: 56px;
  }

  .form-group input,
  .password-wrapper input,
  .login-button {
    height: 56px;
  }

  .login-card h2 {
    font-size: 24px;
  }

  .subtitle,
  .forgot-password {
    font-size: 15px;
  }
}
</style>
