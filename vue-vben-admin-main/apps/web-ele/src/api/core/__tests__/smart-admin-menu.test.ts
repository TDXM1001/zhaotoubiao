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

  it('keeps supplier portal permission codes out of the admin app', () => {
    expect(
      extractSmartAdminAccessCodes([
        {
          cacheFlag: false,
          disabledFlag: false,
          frameFlag: false,
          menuId: 10,
          menuName: 'Portal',
          menuType: 2,
          parentId: 0,
          path: '/bid-portal/project/list',
          visibleFlag: true,
          webPerms: 'bid:portal:project',
        },
        {
          cacheFlag: false,
          disabledFlag: false,
          frameFlag: false,
          menuId: 11,
          menuName: 'Admin Bid',
          menuType: 2,
          parentId: 0,
          path: '/system/bid/project/list',
          visibleFlag: true,
          webPerms: 'bid:project:query',
        },
      ]),
    ).toEqual(['bid:project:query']);
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
        '/system/bid/project/project-form.vue',
        '/system/bid/project/project-list.vue',
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

  it('maps p1 bid tender submission paths and filters supplier portal menus', () => {
    const routes = buildSmartAdminRoutes(
      [
        {
          disabledFlag: false,
          frameFlag: false,
          menuId: 731,
          menuName: '招投标管理',
          menuType: 1,
          parentId: 0,
          sort: 1,
          visibleFlag: true,
        },
        {
          component: '/system/bid/tender/tender-list.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 760,
          menuName: '招标文件',
          menuType: 2,
          parentId: 731,
          path: '/system/bid/tender/list',
          sort: 4,
          visibleFlag: true,
        },
        {
          component: '/system/bid/tender/tender-form.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 761,
          menuName: '招标文件新增',
          menuType: 2,
          parentId: 760,
          path: '/system/bid/tender/create',
          sort: 11,
          visibleFlag: false,
        },
        {
          component: '/system/bid/tender/tender-detail.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 762,
          menuName: '招标文件详情',
          menuType: 2,
          parentId: 760,
          path: '/system/bid/tender/detail',
          sort: 12,
          visibleFlag: false,
        },
        {
          component: '/system/bid/submission/submission-list.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 769,
          menuName: '投标管理',
          menuType: 2,
          parentId: 731,
          path: '/system/bid/submission/list',
          sort: 5,
          visibleFlag: true,
        },
        {
          component: '/system/bid/submission/submission-detail.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 770,
          menuName: '投标详情',
          menuType: 2,
          parentId: 769,
          path: '/system/bid/submission/detail',
          sort: 11,
          visibleFlag: false,
        },
        {
          component: '/bid-portal/project/portal-project-list.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 776,
          menuName: '供应商门户',
          menuType: 2,
          parentId: 731,
          path: '/bid-portal/project/list',
          sort: 6,
          visibleFlag: true,
        },
        {
          component: '/bid-portal/project/portal-project-detail.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 777,
          menuName: '门户项目详情',
          menuType: 2,
          parentId: 776,
          path: '/bid-portal/project/detail',
          sort: 11,
          visibleFlag: false,
        },
        {
          component: '/bid-portal/submission/portal-submission-form.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 778,
          menuName: '门户投标表单',
          menuType: 2,
          parentId: 776,
          path: '/bid-portal/submission/form',
          sort: 12,
          visibleFlag: false,
        },
      ],
      new Set([
        '/bid-portal/project/portal-project-detail.vue',
        '/bid-portal/project/portal-project-list.vue',
        '/bid-portal/submission/portal-submission-form.vue',
        '/system/bid/submission/submission-detail.vue',
        '/system/bid/submission/submission-list.vue',
        '/system/bid/tender/tender-detail.vue',
        '/system/bid/tender/tender-form.vue',
        '/system/bid/tender/tender-list.vue',
      ]),
    );

    expect(routes).toHaveLength(1);
    expect(routes[0]?.children).toHaveLength(5);
    const childRoutesByPath = new Map(
      routes[0]?.children?.map((route) => [route.path, route]),
    );

    expect(childRoutesByPath.get('/system/bid/tender/list')).toMatchObject({
      component: '/system/bid/tender/tender-list.vue',
      path: '/system/bid/tender/list',
    });
    expect(childRoutesByPath.get('/system/bid/tender/create')).toMatchObject({
      component: '/system/bid/tender/tender-form.vue',
      meta: {
        activePath: '/system/bid/tender/list',
        hideInMenu: true,
      },
      path: '/system/bid/tender/create',
    });
    expect(childRoutesByPath.get('/system/bid/tender/detail')).toMatchObject({
      component: '/system/bid/tender/tender-detail.vue',
      meta: {
        activePath: '/system/bid/tender/list',
        hideInMenu: true,
      },
      path: '/system/bid/tender/detail',
    });
    expect(childRoutesByPath.get('/system/bid/submission/list')).toMatchObject(
      {
        component: '/system/bid/submission/submission-list.vue',
        path: '/system/bid/submission/list',
      },
    );
    expect(
      childRoutesByPath.get('/system/bid/submission/detail'),
    ).toMatchObject({
      component: '/system/bid/submission/submission-detail.vue',
      meta: {
        activePath: '/system/bid/submission/list',
        hideInMenu: true,
      },
      path: '/system/bid/submission/detail',
    });
    expect(childRoutesByPath.has('/bid-portal/project/list')).toBe(false);
    expect(childRoutesByPath.has('/bid-portal/project/detail')).toBe(false);
    expect(childRoutesByPath.has('/bid-portal/submission/form')).toBe(false);
  });

  it('maps p2 bid opening evaluation and award menu paths to their routes', () => {
    const routes = buildSmartAdminRoutes(
      [
        {
          disabledFlag: false,
          frameFlag: false,
          menuId: 731,
          menuName: '招投标管理',
          menuType: 1,
          parentId: 0,
          sort: 1,
          visibleFlag: true,
        },
        {
          component: '/system/bid/opening/opening-list.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 779,
          menuName: '开标管理',
          menuType: 2,
          parentId: 731,
          path: '/system/bid/opening/list',
          sort: 7,
          visibleFlag: true,
        },
        {
          component: '/system/bid/opening/opening-detail.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 781,
          menuName: '开标详情',
          menuType: 2,
          parentId: 779,
          path: '/system/bid/opening/detail',
          sort: 12,
          visibleFlag: false,
        },
        {
          component: '/system/bid/evaluation/evaluation-list.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 787,
          menuName: '评标管理',
          menuType: 2,
          parentId: 731,
          path: '/system/bid/evaluation/list',
          sort: 8,
          visibleFlag: true,
        },
        {
          component: '/system/bid/evaluation/evaluation-detail.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 789,
          menuName: '评标详情',
          menuType: 2,
          parentId: 787,
          path: '/system/bid/evaluation/detail',
          sort: 12,
          visibleFlag: false,
        },
        {
          component: '/system/bid/award/award-list.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 795,
          menuName: '定标管理',
          menuType: 2,
          parentId: 731,
          path: '/system/bid/award/list',
          sort: 9,
          visibleFlag: true,
        },
        {
          component: '/system/bid/award/award-detail.vue',
          disabledFlag: false,
          frameFlag: false,
          menuId: 797,
          menuName: '定标详情',
          menuType: 2,
          parentId: 795,
          path: '/system/bid/award/detail',
          sort: 12,
          visibleFlag: false,
        },
      ],
      new Set([
        '/system/bid/award/award-detail.vue',
        '/system/bid/award/award-list.vue',
        '/system/bid/evaluation/evaluation-detail.vue',
        '/system/bid/evaluation/evaluation-list.vue',
        '/system/bid/opening/opening-detail.vue',
        '/system/bid/opening/opening-list.vue',
      ]),
    );

    expect(routes).toHaveLength(1);
    expect(routes[0]?.children).toHaveLength(6);
    const childRoutesByPath = new Map(
      routes[0]?.children?.map((route) => [route.path, route]),
    );

    expect(childRoutesByPath.get('/system/bid/opening/list')).toMatchObject({
      component: '/system/bid/opening/opening-list.vue',
      path: '/system/bid/opening/list',
    });
    expect(childRoutesByPath.get('/system/bid/opening/detail')).toMatchObject({
      component: '/system/bid/opening/opening-detail.vue',
      meta: {
        activePath: '/system/bid/opening/list',
        hideInMenu: true,
      },
      path: '/system/bid/opening/detail',
    });
    expect(childRoutesByPath.get('/system/bid/evaluation/list')).toMatchObject({
      component: '/system/bid/evaluation/evaluation-list.vue',
      path: '/system/bid/evaluation/list',
    });
    expect(
      childRoutesByPath.get('/system/bid/evaluation/detail'),
    ).toMatchObject({
      component: '/system/bid/evaluation/evaluation-detail.vue',
      meta: {
        activePath: '/system/bid/evaluation/list',
        hideInMenu: true,
      },
      path: '/system/bid/evaluation/detail',
    });
    expect(childRoutesByPath.get('/system/bid/award/list')).toMatchObject({
      component: '/system/bid/award/award-list.vue',
      path: '/system/bid/award/list',
    });
    expect(childRoutesByPath.get('/system/bid/award/detail')).toMatchObject({
      component: '/system/bid/award/award-detail.vue',
      meta: {
        activePath: '/system/bid/award/list',
        hideInMenu: true,
      },
      path: '/system/bid/award/detail',
    });
  });
});
