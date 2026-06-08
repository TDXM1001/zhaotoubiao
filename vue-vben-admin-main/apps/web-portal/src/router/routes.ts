import type { RouteRecordRaw } from 'vue-router';

const ProjectList = () => import('#/views/project/portal-project-list.vue');
const ProjectDetail = () =>
  import('#/views/project/portal-project-detail.vue');
const SubmissionForm = () =>
  import('#/views/submission/portal-submission-form.vue');
const Result = () => import('#/views/result/portal-result.vue');

export const portalRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/bid-portal/project/list',
  },
  {
    component: ProjectList,
    meta: { title: '项目大厅' },
    name: 'PortalProjectList',
    path: '/bid-portal/project/list',
  },
  {
    component: ProjectDetail,
    meta: { title: '项目详情' },
    name: 'PortalProjectDetail',
    path: '/bid-portal/project/detail',
  },
  {
    component: SubmissionForm,
    meta: { title: '投标办理' },
    name: 'PortalSubmissionForm',
    path: '/bid-portal/submission/form',
  },
  {
    component: Result,
    meta: { title: '标段结果' },
    name: 'PortalResult',
    path: '/bid-portal/lots/result',
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/bid-portal/project/list',
  },
];
