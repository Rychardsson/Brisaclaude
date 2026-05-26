<template>
  <nav class="navbar">
    <div class="nav-container">
      <div class="nav-brand">
        <router-link to="/dashboard" class="brand-link">
          <img src="https://inscricoesrestic.brisabr.com.br/_next/image?url=%2Flogo_no-txt.png&w=256&q=75" alt="logo" class="logo">
          <span class="brand-name">BRISA ONE</span>
        </router-link>
      </div>
      
      <!-- Menu Desktop -->
      <ul class="nav-menu">
        <li>
          <router-link :to="dashboardLink" :class="{ active: $route.path === '/dashboard' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 21v-4"></path>
              <path d="M9 21v-10"></path>
              <path d="M15 21v-14"></path>
              <path d="M21 21v-6"></path>
              <path d="M3 21h18"></path>
            </svg>
            Dashboard
          </router-link>
        </li>
        <li>
          <router-link to="/programs/register" :class="{ active: $route.path === '/programs/register' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
            </svg>
            Cadastro
          </router-link>
        </li>
        <li>
          <router-link to="/programs" :class="{ active: $route.path === '/programs' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path>
              <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"></path>
            </svg>
            Programas
          </router-link>
        </li>
        <li>
          <router-link to="/people" :class="{ active: $route.path === '/people' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
            Pessoas
          </router-link>
        </li>
        <li v-if="isAdmin">
          <router-link to="/academic-staff" :class="{ active: $route.path === '/academic-staff' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 3v18"></path>
              <path d="M7 8h10"></path>
              <path d="M7 16h10"></path>
              <path d="M4 12h16"></path>
            </svg>
            Equipe
          </router-link>
        </li>
        <li>
          <router-link to="/admin-panel" :class="{ active: $route.path === '/admin-panel' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
              <path d="M22 6.5V17.5A2.5 2.5 0 0 1 19.5 20H6.5A2.5 2.5 0 0 1 4 17.5V6.5A2.5 2.5 0 0 1 6.5 4H19.5A2.5 2.5 0 0 1 22 6.5Z"></path>
            </svg>
            Painel
          </router-link>
        </li>
        <li>
          <router-link to="/carreira" :class="{ active: $route.path === '/carreira' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 3v18h18"></path>
              <path d="m19 9-5 5-4-4-3 3"></path>
            </svg>
            Carreira
          </router-link>
        </li>
      </ul>

      <!-- Hamburger Button Mobile -->
      <button class="hamburger-btn" @click="mobileMenuOpen = !mobileMenuOpen">
        <svg v-if="!mobileMenuOpen" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="4" y1="6" x2="20" y2="6"></line>
          <line x1="4" y1="12" x2="20" y2="12"></line>
          <line x1="4" y1="18" x2="20" y2="18"></line>
        </svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </button>
      
      <div class="nav-actions">
        <button @click="logout" class="btn-logout">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
            <polyline points="16 17 21 12 16 7"></polyline>
            <line x1="21" y1="12" x2="9" y2="12"></line>
          </svg>
          Sair
        </button>
      </div>
    </div>

    <!-- Mobile Menu Dropdown -->
    <div v-if="mobileMenuOpen" class="mobile-menu">
      <router-link 
        :to="dashboardLink" 
        :class="{ active: $route.path === '/dashboard' }"
        @click="mobileMenuOpen = false"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 21v-4"></path>
          <path d="M9 21v-10"></path>
          <path d="M15 21v-14"></path>
          <path d="M21 21v-6"></path>
          <path d="M3 21h18"></path>
        </svg>
        Dashboard
      </router-link>
      <router-link 
        to="/programs/register" 
        :class="{ active: $route.path === '/programs/register' }"
        @click="mobileMenuOpen = false"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
        </svg>
        Cadastro
      </router-link>
      <router-link 
        to="/programs" 
        :class="{ active: $route.path === '/programs' }"
        @click="mobileMenuOpen = false"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path>
          <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"></path>
        </svg>
        Programas
      </router-link>
      <router-link 
        to="/people" 
        :class="{ active: $route.path === '/people' }"
        @click="mobileMenuOpen = false"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
          <circle cx="9" cy="7" r="4"></circle>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
        </svg>
        Pessoas
      </router-link>
      <router-link 
        v-if="isAdmin"
        to="/academic-staff" 
        :class="{ active: $route.path === '/academic-staff' }"
        @click="mobileMenuOpen = false"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 3v18"></path>
          <path d="M7 8h10"></path>
          <path d="M7 16h10"></path>
          <path d="M4 12h16"></path>
        </svg>
        Equipe
      </router-link>
      <router-link 
        to="/admin-panel" 
        :class="{ active: $route.path === '/admin-panel' }"
        @click="mobileMenuOpen = false"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
          <path d="M22 6.5V17.5A2.5 2.5 0 0 1 19.5 20H6.5A2.5 2.5 0 0 1 4 17.5V6.5A2.5 2.5 0 0 1 6.5 4H19.5A2.5 2.5 0 0 1 22 6.5Z"></path>
        </svg>
        Painel
      </router-link>
      <router-link 
        to="/carreira" 
        :class="{ active: $route.path === '/carreira' }"
        @click="mobileMenuOpen = false"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 3v18h18"></path>
          <path d="m19 9-5 5-4-4-3 3"></path>
        </svg>
        Carreira
      </router-link>
    </div>
  </nav>
</template>

<script>
import { useRouter, useRoute } from 'vue-router';
import { computed, ref } from 'vue';
import { authService } from '@/services/authService';

export default {
  name: 'NavBar',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const mobileMenuOpen = ref(false);

    const logout = () => {
      authService.logout();
      router.push('/');
    };

    const dashboardLink = computed(() => {
      const q = {};
      if (route?.query?.programaId) q.programaId = String(route.query.programaId);
      if (route?.query?.turmaId) q.turmaId = String(route.query.turmaId);
      if (route?.params?.programId) q.programaId = String(route.params.programId);
      if (route?.params?.classId) q.turmaId = String(route.params.classId);
      return Object.keys(q).length ? { path: '/dashboard', query: q } : '/dashboard';
    });

    const isAdmin = computed(() => {
      const currentUser = authService.getUser();
      return String(currentUser?.role || '').toUpperCase() === 'ADMIN';
    });

    return {
      logout,
      dashboardLink,
      isAdmin,
      mobileMenuOpen,
    };
  }
};
</script>

<style scoped>
.navbar {
  background: #1F285F;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  gap: 20px;
}

.nav-brand {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.brand-link {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: white;
  transition: opacity 0.2s;
}

.brand-link:hover {
  opacity: 0.9;
}

.logo {
  color: white;
  height: 40px;
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.nav-menu {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  justify-content: center;
}

.nav-menu li {
  margin: 0;
}

.nav-menu a {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: all 0.2s;
  font-weight: 500;
  font-size: 14px;
  border-bottom: 3px solid transparent;
}

.nav-menu a:hover {
  color: white;
  background: rgba(255, 255, 255, 0.1);
}

.nav-menu a.active {
  color: white;
  border-bottom: 3px solid #0288d1;
  background: rgba(2, 136, 209, 0.1);
}

.nav-menu a svg {
  flex-shrink: 0;
}

.nav-actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.btn-logout {
  background: #0288d1;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
  font-size: 14px;
}

.btn-logout:hover {
  background: #0277bd;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(2, 136, 209, 0.3);
}

/* Hamburger Button */
.hamburger-btn {
  display: none;
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 8px;
  transition: transform 0.2s;
  align-items: center;
  justify-content: center;
}

.hamburger-btn:hover {
  transform: scale(1.1);
}

.hamburger-btn svg {
  width: 24px;
  height: 24px;
}

/* Mobile Menu */
.mobile-menu {
  position: absolute;
  top: 64px;
  left: 0;
  right: 0;
  background: #1a1f52;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  animation: slideDown 0.3s ease-out;
  z-index: 999;
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

.mobile-menu a {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  border-left: 4px solid transparent;
  transition: all 0.2s;
  font-weight: 500;
  font-size: 15px;
}

.mobile-menu a:hover {
  color: white;
  background: rgba(2, 136, 209, 0.15);
}

.mobile-menu a.active {
  color: white;
  background: rgba(2, 136, 209, 0.2);
  border-left-color: #0288d1;
}

.mobile-menu svg {
  flex-shrink: 0;
}

/* Responsive */
@media (max-width: 1024px) {
  .nav-menu {
    display: none;
  }

  .hamburger-btn {
    display: flex;
  }

  .nav-container {
    padding: 0 12px;
  }

  .brand-name {
    font-size: 18px;
  }

  .btn-logout {
    padding: 8px 12px;
    font-size: 13px;
    gap: 6px;
  }

  .btn-logout svg {
    width: 16px;
    height: 16px;
  }
}

@media (max-width: 600px) {
  .nav-container {
    height: 56px;
    padding: 0 8px;
    gap: 8px;
  }

  .logo {
    height: 32px;
  }

  .brand-name {
    display: none;
  }

  .hamburger-btn {
    padding: 4px;
  }

  .btn-logout {
    padding: 6px 10px;
  }

  .btn-logout svg {
    display: none;
  }

  .mobile-menu {
    top: 56px;
  }

  .mobile-menu a {
    padding: 14px 12px;
    font-size: 14px;
    gap: 10px;
  }

  .mobile-menu svg {
    width: 18px;
    height: 18px;
  }
}
</style>
