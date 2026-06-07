import type { RouteRecordStringComponent } from '@vben/types';

type SmartAdminFlag = boolean | number | null | undefined;

interface SmartAdminMenuItem {
  apiPerms?: null | string;
  cacheFlag?: SmartAdminFlag;
  children?: SmartAdminMenuItem[];
  component?: null | string;
  disabledFlag?: SmartAdminFlag;
  frameFlag?: SmartAdminFlag;
  frameUrl?: null | string;
  icon?: null | string;
  menuId: number | string;
  menuName: string;
  menuType: number;
  parentId: number | string;
  path?: null | string;
  sort?: null | number;
  visibleFlag?: SmartAdminFlag;
  webPerms?: null | string;
}

const SMART_ADMIN_CATALOG_PATH_PREFIX = '/smart-admin/catalog';
const SMART_ADMIN_PLACEHOLDER_COMPONENT = '/_core/fallback/coming-soon.vue';

function isEnabledFlag(flag: SmartAdminFlag, defaultValue: boolean = false) {
  if (flag === null || flag === undefined) {
    return defaultValue;
  }
  return flag === true || flag === 1;
}

function isHttpUrl(value?: null | string) {
  return /^https?:\/\//.test(value ?? '');
}

function normalizeSmartAdminComponentPath(component?: null | string) {
  if (!component) {
    return '';
  }

  if (isHttpUrl(component)) {
    return component;
  }

  const normalizedComponent = component
    .replaceAll('\\', '/')
    .replace(/^(\.\/|\.\.\/)+/, '')
    .replace(/^views\//, '')
    .replace(/^\/views\//, '/');

  return normalizedComponent.startsWith('/')
    ? normalizedComponent
    : `/${normalizedComponent}`;
}

function sortSmartAdminMenus(menuList: SmartAdminMenuItem[]) {
  return [...menuList].sort((left, right) => {
    const leftSort = left.sort ?? Number.MAX_SAFE_INTEGER;
    const rightSort = right.sort ?? Number.MAX_SAFE_INTEGER;

    if (leftSort !== rightSort) {
      return leftSort - rightSort;
    }

    return Number(left.menuId) - Number(right.menuId);
  });
}

function splitPerms(perms?: null | string) {
  if (!perms) {
    return [];
  }

  return perms
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
}

function createCatalogPath(menuId: number | string) {
  return `${SMART_ADMIN_CATALOG_PATH_PREFIX}/${menuId}`;
}

function getMenuRoutePath(menu: SmartAdminMenuItem) {
  return menu.path?.trim() || createCatalogPath(menu.menuId);
}

function buildMenuTree(menuList: SmartAdminMenuItem[]) {
  const groupedChildren = new Map<string, SmartAdminMenuItem[]>();
  const normalizedMenuList = sortSmartAdminMenus(
    menuList.filter((menu) => !isEnabledFlag(menu.disabledFlag)),
  ).map((menu) => ({ ...menu, children: [] as SmartAdminMenuItem[] }));
  const menuIdSet = new Set(
    normalizedMenuList.map((menu) => String(menu.menuId)),
  );
  const roots: SmartAdminMenuItem[] = [];

  for (const menu of normalizedMenuList) {
    const siblingList = groupedChildren.get(String(menu.parentId)) ?? [];
    siblingList.push(menu);
    groupedChildren.set(String(menu.parentId), siblingList);
  }

  for (const menu of normalizedMenuList) {
    menu.children = sortSmartAdminMenus(
      groupedChildren.get(String(menu.menuId)) ?? [],
    );

    const hasParent =
      Number(menu.parentId) !== 0 && menuIdSet.has(String(menu.parentId));
    if (!hasParent) {
      roots.push(menu);
    }
  }

  return roots;
}

function findFirstVisiblePath(menuList: SmartAdminMenuItem[]): null | string {
  for (const menu of sortSmartAdminMenus(menuList)) {
    if (!isEnabledFlag(menu.visibleFlag, true)) {
      continue;
    }

    if (menu.menuType === 3) {
      continue;
    }

    if (menu.menuType === 2) {
      return getMenuRoutePath(menu);
    }

    const childPath = findFirstVisiblePath(menu.children ?? []);
    if (childPath) {
      return childPath;
    }

    return getMenuRoutePath(menu);
  }

  return null;
}

function normalizeRouteIcon(icon?: null | string) {
  if (!icon) {
    return undefined;
  }

  return icon.includes(':') || isHttpUrl(icon) ? icon : undefined;
}

function resolveRouteComponent(
  menu: SmartAdminMenuItem,
  availableViewPaths: ReadonlySet<string>,
) {
  const frameUrl = menu.frameUrl?.trim();
  const componentPath = normalizeSmartAdminComponentPath(menu.component);

  if (isEnabledFlag(menu.frameFlag) && (frameUrl || isHttpUrl(componentPath))) {
    return {
      component: 'IFrameView',
      iframeSrc: frameUrl || componentPath,
    };
  }

  if (menu.menuType === 1) {
    return {
      component: 'BasicLayout',
      iframeSrc: undefined,
    };
  }

  const resolvedComponent = availableViewPaths.has(componentPath)
    ? componentPath
    : SMART_ADMIN_PLACEHOLDER_COMPONENT;

  return {
    component: resolvedComponent,
    iframeSrc: undefined,
  };
}

function convertMenuToRoute(
  menu: SmartAdminMenuItem,
  availableViewPaths: ReadonlySet<string>,
  parentMenu?: SmartAdminMenuItem,
): null | RouteRecordStringComponent {
  if (menu.menuType === 3) {
    return null;
  }

  const childRoutes = sortSmartAdminMenus(menu.children ?? [])
    .map((child) => convertMenuToRoute(child, availableViewPaths, menu))
    .filter((child): child is RouteRecordStringComponent => !!child);
  const { component, iframeSrc } = resolveRouteComponent(menu, availableViewPaths);
  const meta: NonNullable<RouteRecordStringComponent['meta']> = {
    hideInMenu: !isEnabledFlag(menu.visibleFlag, true),
    icon: normalizeRouteIcon(menu.icon),
    iframeSrc,
    keepAlive: isEnabledFlag(menu.cacheFlag),
    order: menu.sort ?? undefined,
    title: menu.menuName,
  };
  const route: RouteRecordStringComponent = {
    component,
    meta,
    name: `SmartAdminMenu${menu.menuId}`,
    path: getMenuRoutePath(menu),
  };

  if (!isEnabledFlag(menu.visibleFlag, true) && parentMenu?.path) {
    meta.activePath = getMenuRoutePath(parentMenu);
  }

  if (childRoutes.length > 0) {
    route.children = childRoutes;
  }

  return route;
}

function extractSmartAdminAccessCodes(menuList: SmartAdminMenuItem[]) {
  const codes = new Set<string>();

  for (const menu of menuList) {
    for (const code of [...splitPerms(menu.webPerms), ...splitPerms(menu.apiPerms)]) {
      codes.add(code);
    }
  }

  return [...codes];
}

function findSmartAdminHomePath(
  menuList: SmartAdminMenuItem[],
  fallbackHomePath: string,
) {
  const homePath = findFirstVisiblePath(buildMenuTree(menuList));
  return homePath || fallbackHomePath;
}

function buildSmartAdminRoutes(
  menuList: SmartAdminMenuItem[],
  availableViewPaths: ReadonlySet<string>,
) {
  return buildMenuTree(menuList)
    .map((menu) => convertMenuToRoute(menu, availableViewPaths))
    .filter((route): route is RouteRecordStringComponent => !!route);
}

export type { SmartAdminMenuItem };
export {
  buildSmartAdminRoutes,
  extractSmartAdminAccessCodes,
  findSmartAdminHomePath,
  normalizeSmartAdminComponentPath,
};
