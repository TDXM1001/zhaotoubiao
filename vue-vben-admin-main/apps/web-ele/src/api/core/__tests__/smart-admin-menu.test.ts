import { describe, expect, it } from 'vitest';

import {
  buildSmartAdminRoutes,
  extractSmartAdminAccessCodes,
  findSmartAdminHomePath,
  normalizeSmartAdminComponentPath,
  type SmartAdminMenuItem,
} from '../smart-admin-menu';

const mockMenuList: SmartAdminMenuItem[] = [
  {
    cacheFlag: false,
    disabledFlag: false,
    frameFlag: false,
    icon: 'SettingOutlined',
    menuId: 1,
    menuName: 'System',
    menuType: 1,
    parentId: 0,
    sort: 1,
    visibleFlag: true,
  },
  {
    apiPerms: 'system:user:query',
    cacheFlag: true,
    component: '/system/user/index.vue',
    disabledFlag: false,
    frameFlag: false,
    menuId: 2,
    menuName: 'User List',
    menuType: 2,
    parentId: 1,
    path: '/system/user',
    sort: 1,
    visibleFlag: true,
    webPerms: 'system:user:query',
  },
  {
    apiPerms: 'system:user:detail',
    cacheFlag: false,
    component: '/system/user/detail.vue',
    disabledFlag: false,
    frameFlag: false,
    menuId: 3,
    menuName: 'User Detail',
    menuType: 2,
    parentId: 1,
    path: '/system/user/detail',
    sort: 2,
    visibleFlag: false,
    webPerms: 'system:user:detail',
  },
  {
    apiPerms: 'system:user:delete,system:user:remove',
    cacheFlag: false,
    disabledFlag: false,
    frameFlag: false,
    menuId: 4,
    menuName: 'Delete User',
    menuType: 3,
    parentId: 2,
    visibleFlag: false,
    webPerms: 'system:user:delete,system:user:remove',
  },
  {
    cacheFlag: false,
    disabledFlag: false,
    frameFlag: true,
    frameUrl: 'https://example.com/docs',
    menuId: 5,
    menuName: 'Docs',
    menuType: 2,
    parentId: 0,
    path: '/docs',
    sort: 2,
    visibleFlag: true,
  },
];

describe('normalizeSmartAdminComponentPath', () => {
  it('normalizes local view paths into vben route component paths', () => {
    expect(normalizeSmartAdminComponentPath('../views/demo/index.vue')).toBe(
      '/demo/index.vue',
    );
    expect(normalizeSmartAdminComponentPath('/system/user/index.vue')).toBe(
      '/system/user/index.vue',
    );
  });
});

describe('extractSmartAdminAccessCodes', () => {
  it('deduplicates permission codes from web and api perms', () => {
    expect(extractSmartAdminAccessCodes(mockMenuList)).toEqual([
      'system:user:query',
      'system:user:detail',
      'system:user:delete',
      'system:user:remove',
    ]);
  });
});

describe('findSmartAdminHomePath', () => {
  it('uses the first visible backend menu path as the home path', () => {
    expect(findSmartAdminHomePath(mockMenuList, '/analytics')).toBe(
      '/system/user',
    );
  });
});

describe('buildSmartAdminRoutes', () => {
  it('converts smart-admin menus into backend routes with placeholder fallbacks', () => {
    const routes = buildSmartAdminRoutes(
      mockMenuList,
      new Set(['/system/user/index.vue']),
    );

    expect(routes).toHaveLength(2);

    expect(routes[0]).toMatchObject({
      component: 'BasicLayout',
      meta: {
        order: 1,
        title: 'System',
      },
      name: 'SmartAdminMenu1',
      path: '/smart-admin/catalog/1',
    });

    expect(routes[0]?.children).toHaveLength(2);
    expect(routes[0]?.children?.[0]).toMatchObject({
      component: '/system/user/index.vue',
      meta: {
        keepAlive: true,
        title: 'User List',
      },
      path: '/system/user',
    });
    expect(routes[0]?.children?.[1]).toMatchObject({
      component: '/_core/fallback/coming-soon.vue',
      meta: {
        hideInMenu: true,
        title: 'User Detail',
      },
      path: '/system/user/detail',
    });

    expect(routes[1]).toMatchObject({
      component: 'IFrameView',
      meta: {
        iframeSrc: 'https://example.com/docs',
        title: 'Docs',
      },
      path: '/docs',
    });
  });

  it('flattens hidden form pages so they do not render inside list pages', () => {
    const routes = buildSmartAdminRoutes(
      [
        {
          disabledFlag: false,
          frameFlag: false,
          menuId: 100,
          menuName: '招投标管理',
          menuType: 1,
          parentId: 0,
          sort: 1,
          visibleFlag: true,
        },
        {
          component: '/system/bid/project/project-list.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 101,
          menuName: '招标项目',
          menuType: 2,
          parentId: 100,
          path: '/system/bid/project/list',
          sort: 1,
          visibleFlag: true,
        },
        {
          component: '/system/bid/project/project-form.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 102,
          menuName: '招标项目新增',
          menuType: 2,
          parentId: 101,
          path: '/system/bid/project/create',
          sort: 11,
          visibleFlag: false,
        },
      ],
      new Set([
        '/system/bid/project/project-list.vue',
        '/system/bid/project/project-form.vue',
      ]),
    );

    expect(routes).toHaveLength(1);
    expect(routes[0]?.children).toHaveLength(2);
    expect(routes[0]?.children?.[0]).toMatchObject({
      component: '/system/bid/project/project-list.vue',
      path: '/system/bid/project/list',
    });
    expect(routes[0]?.children?.[0]?.children).toBeUndefined();
    expect(routes[0]?.children?.[1]).toMatchObject({
      component: '/system/bid/project/project-form.vue',
      meta: {
        activePath: '/system/bid/project/list',
        hideInMenu: true,
        title: '招标项目新增',
      },
      path: '/system/bid/project/create',
    });
  });
});
