import type { RouteRecordRaw } from 'vue-router';

import { describe, expect, it } from 'vitest';

import { routes } from '../index';

function findRouteByPath(
  routeList: RouteRecordRaw[],
  targetPath: string,
  parentPath = '',
): RouteRecordRaw | undefined {
  for (const route of routeList) {
    const currentPath = route.path.startsWith('/')
      ? route.path
      : `${parentPath}/${route.path}`.replaceAll('//', '/');

    if (currentPath === targetPath) {
      return route;
    }

    const matchedChild = findRouteByPath(
      route.children ?? [],
      targetPath,
      currentPath,
    );
    if (matchedChild) {
      return matchedChild;
    }
  }
}

describe('public bid portal routes', () => {
  it('exposes supplier portal pages without admin access', () => {
    for (const path of [
      '/bid-portal/project/list',
      '/bid-portal/project/detail',
      '/bid-portal/submission/form',
      '/bid-portal/lots/result',
    ]) {
      expect(findRouteByPath(routes, path)?.meta?.ignoreAccess).toBe(true);
    }
  });
});
