import { createRouter, createWebHistory } from 'vue-router';

import { portalRoutes } from './routes';

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: portalRoutes,
  scrollBehavior: () => ({ left: 0, top: 0 }),
});

router.afterEach((to) => {
  // 门户独立维护标题，不再依赖后台动态菜单标题。
  document.title = `${String(to.meta.title ?? '供应商门户')} - 供应商门户`;
});
