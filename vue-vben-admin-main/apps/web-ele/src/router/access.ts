import type {
  ComponentRecordType,
  GenerateMenuAndRoutesOptions,
} from '@vben/types';

import { generateAccessible } from '@vben/access';

import { ElMessage } from 'element-plus';

import { getAllMenusApi } from '#/api';
import { normalizeSmartAdminComponentPath } from '#/api/core/smart-admin-menu';
import { BasicLayout, IFrameView } from '#/layouts';
import { $t } from '#/locales';

const forbiddenComponent = () => import('#/views/_core/fallback/forbidden.vue');

async function generateAccess(options: GenerateMenuAndRoutesOptions) {
  const pageMap: ComponentRecordType = import.meta.glob('../views/**/*.vue');
  const availableViewPaths = new Set(
    Object.keys(pageMap).map(normalizeSmartAdminComponentPath),
  );

  const layoutMap: ComponentRecordType = {
    BasicLayout,
    IFrameView,
  };

  return await generateAccessible('backend', {
    ...options,
    fetchMenuListAsync: async () => {
      ElMessage({
        duration: 1500,
        message: `${$t('common.loadingMenu')}...`,
      });
      return await getAllMenusApi(availableViewPaths);
    },
    forbiddenComponent,
    layoutMap,
    pageMap,
  });
}

export { generateAccess };
