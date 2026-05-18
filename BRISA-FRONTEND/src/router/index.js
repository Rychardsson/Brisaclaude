import { createRouter, createWebHistory } from 'vue-router';
import { authService } from '@/services/authService';
import LoginView from '@/views/LoginView.vue';
import ResetPasswordView from '@/views/ResetPasswordView.vue';
import HomeView from '@/views/HomeView.vue';
import DashboardsView from '@/views/DashboardView.vue';
import PeopleView from '@/views/PeopleView.vue';
import PessoaPerfilView from '@/views/PessoaPerfilView.vue';
import ProgramsView from '@/views/ProgramsView.vue';

// 👇 ESTA FOI A ÚNICA LINHA ALTERADA 👇
// O router agora procura o Arquivo Pai dentro da nova pasta que você criou.
import ProgramRegistrationView from '@/views/ProgramRegistration/ProgramRegistrationView.vue';

import ClassDetailsView from '@/views/ClassDetailsView.vue';
import StageDetailsView from '@/views/StageDetailsView.vue';
import InstitutionsView from '@/views/InstitutionsView.vue';
import LogsView from '@/views/LogsView.vue';
import CoursesView from '@/views/CoursesView.vue';
import AdminPanelView from '@/views/AdminPanelView.vue';
import CareerView from '@/views/CareerView.vue';

const routes = [
  {
    path: '/',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: ResetPasswordView
  },
  {
    path: '/dashboard',
    name: 'Dashboards',
    component: DashboardsView,
    meta: { requiresAuth: true }
  },
  {
    path: '/home',
    name: 'Home',
    component: HomeView,
    meta: { requiresAuth: true }
  },
  {
    path: '/people',
    name: 'People',
    component: PeopleView,
    meta: { requiresAuth: true }
  },
  {
    path: '/people/:id',
    name: 'PersonDetails',
    component: PessoaPerfilView,
    meta: { requiresAuth: true }
  },
  {
    path: '/institutions',
    name: 'Institutions',
    component: InstitutionsView,
    meta: { requiresAuth: true }
  },
  {
    path: '/programs',
    name: 'Programs',
    component: ProgramsView,
    meta: { requiresAuth: true }
  },
  // 👇 A ROTA CONTINUA A MESMA, APONTANDO SÓ PARA O PAI 👇
  {
    path: '/programs/register',
    name: 'ProgramRegistration',
    component: ProgramRegistrationView,
    meta: { requiresAuth: true }
  },
  {
    path: '/courses',
    name: 'Courses',
    component: CoursesView,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin-panel',
    name: 'AdminPanel',
    component: AdminPanelView,
    meta: { requiresAuth: true }
  },
  {
    path: '/carreira',
    name: 'Career',
    component: CareerView,
    meta: { requiresAuth: true }
  },
  {
    path: '/programs/:id',
    name: 'ProgramDetails',
    redirect: to => ({
      path: '/programs',
      query: { programId: String(to.params.id) }
    })
  },
  {
    path: '/programs/:programId/classes/:classId',
    name: 'ClassDetails',
    component: ClassDetailsView,
    meta: { requiresAuth: true }
  },
  {
    path: '/programs/:programId/classes/:classId/courses',
    name: 'ClassCourses',
    component: () => import('@/views/ClassCoursesView.vue'),
    meta: { requiresAuth: true }
  },
  // ✅ Tela 4 — Detalhes de um Curso
  {
    path: '/programs/:programId/classes/:classId/courses/:courseId',
    name: 'CourseDetails',
    component: () => import('@/views/CourseDetailsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/programs/:programId/classes/:classId/stages/:stageId',
    name: 'StageDetails',
    component: StageDetailsView,
    meta: { requiresAuth: true }
  },
  {
    path: '/logs',
    name: 'Logs',
    component: LogsView,
    meta: { requiresAuth: true, requiresAdmin: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !authService.isAuthenticated()) {
    next('/');
  } else if (to.path === '/' && authService.isAuthenticated()) {
    next('/home');
  } else {
    next();
  }
});

export default router;
