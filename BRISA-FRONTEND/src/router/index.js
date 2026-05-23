import { createRouter, createWebHistory } from 'vue-router';
import { authService } from '@/services/authService';
import LoginView from '@/views/LoginView.vue';
import ResetPasswordView from '@/views/ResetPasswordView.vue';
import DashboardView from '@/views/DashboardView.vue';
import PeopleView from '@/views/PeopleView.vue';
import PessoaPerfilView from '@/views/PessoaPerfilView.vue';
import ProgramsView from '@/views/ProgramsView.vue';
import ProgramRegistrationView from '@/views/ProgramRegistration/ProgramRegistrationView.vue';
import ClassDetailsView from '@/views/ClassDetailsView.vue';
import StageDetailsView from '@/views/StageDetailsView.vue';
import InstitutionsView from '@/views/InstitutionsView.vue';
import LogsView from '@/views/LogsView.vue';
import CoursesView from '@/views/CoursesView.vue';
import AdminPanelView from '@/views/AdminPanelView.vue';
import CareerView from '@/views/CareerView.vue';
import ClassesView from '@/views/ClassesView.vue';
import EnrollmentsView from '@/views/EnrollmentsView.vue';
import AcademicStaffView from '@/views/AcademicStaffView.vue';

const APP_TITLE = 'BRISA One';

const routes = [
  {
    path: '/',
    name: 'Login',
    component: LoginView,
    meta: { title: 'Entrar' }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: ResetPasswordView,
    meta: { title: 'Redefinir Senha' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: DashboardView,
    meta: { requiresAuth: true, title: 'Dashboard' }
  },
  {
    path: '/people',
    name: 'People',
    component: PeopleView,
    meta: { requiresAuth: true, title: 'Pessoas' }
  },
  {
    path: '/academic-staff',
    name: 'AcademicStaff',
    component: AcademicStaffView,
    meta: { requiresAuth: true, title: 'Equipe Acadêmica' }
  },
  {
    path: '/people/:id',
    name: 'PersonDetails',
    component: PessoaPerfilView,
    meta: { requiresAuth: true, title: 'Perfil da Pessoa' }
  },
  {
    path: '/institutions',
    name: 'Institutions',
    component: InstitutionsView,
    meta: { requiresAuth: true, title: 'Instituições' }
  },
  {
    path: '/programs',
    name: 'Programs',
    component: ProgramsView,
    meta: { requiresAuth: true, title: 'Programas' }
  },
  {
    path: '/programs/register',
    name: 'ProgramRegistration',
    component: ProgramRegistrationView,
    meta: { requiresAuth: true, title: 'Cadastro de Programa' }
  },
  {
    path: '/courses',
    name: 'Courses',
    component: CoursesView,
    meta: { requiresAuth: true, title: 'Cursos' }
  },
  {
    path: '/classes',
    name: 'Classes',
    component: ClassesView,
    meta: { requiresAuth: true, title: 'Turmas' }
  },
  {
    path: '/enrollments',
    name: 'Enrollments',
    component: EnrollmentsView,
    meta: { requiresAuth: true, title: 'Matrículas' }
  },
  {
    path: '/admin-panel',
    name: 'AdminPanel',
    component: AdminPanelView,
    meta: { requiresAuth: true, title: 'Painel Administrativo' }
  },
  {
    path: '/carreira',
    name: 'Career',
    component: CareerView,
    meta: { requiresAuth: true, title: 'Carreira' }
  },
  {
    path: '/programs/:id',
    name: 'ProgramDetails',
    redirect: (to) => ({
      path: '/programs',
      query: { programId: String(to.params.id) }
    })
  },
  {
    path: '/programs/:programId/classes/:classId',
    name: 'ClassDetails',
    component: ClassDetailsView,
    meta: { requiresAuth: true, title: 'Detalhes da Turma' }
  },
  {
    path: '/programs/:programId/classes/:classId/courses',
    name: 'ClassCourses',
    redirect: (to) => ({
      name: 'ClassDetails',
      params: {
        programId: to.params.programId,
        classId: to.params.classId
      },
      query: {
        tab: 'etapas',
        subTab: 'nivelamento'
      }
    })
  },
  {
    path: '/programs/:programId/classes/:classId/stages/:stageId',
    name: 'StageDetails',
    component: StageDetailsView,
    meta: { requiresAuth: true, title: 'Detalhes da Etapa' }
  },
  {
    path: '/logs',
    name: 'Logs',
    component: LogsView,
    meta: { requiresAuth: true, title: 'Logs' }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const currentUser = authService.getUser();
  const currentRole = String(currentUser?.role || '').toUpperCase();

  if (to.meta.requiresAuth && !authService.isAuthenticated()) {
    next('/');
  } else if (to.meta.requiresAdmin && currentRole !== 'ADMIN') {
    next('/dashboard');
  } else if (to.path === '/' && authService.isAuthenticated()) {
    next('/dashboard');
  } else {
    next();
  }
});

router.afterEach((to) => {
  const routeTitle = typeof to.meta.title === 'string' ? to.meta.title.trim() : '';
  document.title = routeTitle ? `${routeTitle} | ${APP_TITLE}` : APP_TITLE;
});

export default router;
