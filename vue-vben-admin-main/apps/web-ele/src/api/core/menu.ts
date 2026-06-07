import type { RouteRecordStringComponent } from '@vben/types';

import {
  buildSmartAdminRoutes,
  normalizeSmartAdminComponentPath,
} from './smart-admin-menu';
import { getLoginInfoApi } from './user';

export async function getAllMenusApi(
  availableViewPaths: Iterable<string>,
): Promise<RouteRecordStringComponent[]> {
  const loginResult = await getLoginInfoApi();
  const normalizedViewPaths = new Set(
    [...availableViewPaths].map(normalizeSmartAdminComponentPath),
  );

  return buildSmartAdminRoutes(loginResult.menuList ?? [], normalizedViewPaths);
}
