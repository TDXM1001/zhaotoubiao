import type { RouteRecordRaw } from 'vue-router';

const BlankLayout = () => import('#/layouts/blank.vue');

const routes: RouteRecordRaw[] = [
  {
    component: BlankLayout,
    meta: {
      hideInBreadcrumb: true,
      hideInMenu: true,
      ignoreAccess: true,
      title: '供应商门户',
    },
    name: 'BidPortalPublic',
    path: '/bid-portal',
    redirect: '/bid-portal/project/list',
    children: [
      {
        component: () =>
          import('#/views/bid-portal/project/portal-project-list.vue'),
        meta: {
          ignoreAccess: true,
          title: '供应商门户',
        },
        name: 'BidPortalProjectList',
        path: 'project/list',
      },
      {
        component: () =>
          import('#/views/bid-portal/project/portal-project-detail.vue'),
        meta: {
          activePath: '/bid-portal/project/list',
          hideInMenu: true,
          ignoreAccess: true,
          title: '项目详情',
        },
        name: 'BidPortalProjectDetail',
        path: 'project/detail',
      },
      {
        component: () =>
          import('#/views/bid-portal/submission/portal-submission-form.vue'),
        meta: {
          activePath: '/bid-portal/project/list',
          hideInMenu: true,
          ignoreAccess: true,
          title: '投标表单',
        },
        name: 'BidPortalSubmissionForm',
        path: 'submission/form',
      },
      {
        component: () => import('#/views/bid-portal/result/portal-result.vue'),
        meta: {
          activePath: '/bid-portal/project/list',
          hideInMenu: true,
          ignoreAccess: true,
          title: '标段结果',
        },
        name: 'BidPortalResult',
        path: 'lots/result',
      },
    ],
  },
];

export default routes;
