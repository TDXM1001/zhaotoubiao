import type { H3Event } from 'h3';

import { eventHandler, getQuery } from 'h3';
import { verifyAccessToken } from '~/utils/jwt-utils';
import { MOCK_MENU_LIST } from '~/utils/mock-data';
import { unAuthorizedResponse, useResponseSuccess } from '~/utils/response';

interface MockMenuItem {
  apiPerms?: string;
  cacheFlag: boolean;
  children?: MockMenuItem[];
  component?: string;
  contextMenuId?: null | number;
  createTime?: string;
  disabledFlag: boolean;
  frameFlag: boolean;
  frameUrl?: string;
  icon?: string;
  menuId: number;
  menuName: string;
  menuType: number;
  parentId: number;
  path?: string;
  permsType?: number;
  sort?: number;
  updateTime?: string;
  visibleFlag: boolean;
  webPerms?: string;
}

interface MenuListQuery {
  cacheFlag?: boolean;
  disabledFlag?: boolean;
  frameFlag?: boolean;
  keywords?: string;
  menuType?: number;
  visibleFlag?: boolean;
}

const LEGACY_MENU_TYPE_MAP: Record<string, number> = {
  button: 3,
  catalog: 1,
  embedded: 2,
  link: 2,
  menu: 2,
};

function firstQueryValue(value: unknown) {
  return Array.isArray(value) ? value[0] : value;
}

function toQueryString(value: unknown) {
  const item = firstQueryValue(value);
  return typeof item === 'string' ? item.trim() : undefined;
}

function toQueryBoolean(value: unknown) {
  const item = toQueryString(value);
  if (item === undefined || item === '') {
    return undefined;
  }

  if (['1', 'true'].includes(item.toLowerCase())) {
    return true;
  }

  if (['0', 'false'].includes(item.toLowerCase())) {
    return false;
  }

  return undefined;
}

function toQueryNumber(value: unknown) {
  const item = toQueryString(value);
  if (!item) {
    return undefined;
  }

  const numberValue = Number(item);
  return Number.isFinite(numberValue) ? numberValue : undefined;
}

function getLegacyMenuType(item: Record<string, any>) {
  if (typeof item.menuType === 'number') {
    return item.menuType;
  }

  if (typeof item.type === 'string') {
    return LEGACY_MENU_TYPE_MAP[item.type] ?? 2;
  }

  return 2;
}

function getLegacyMenuId(item: Record<string, any>) {
  return Number(item.menuId ?? item.id);
}

function flattenMenuItems(items: Record<string, any>[], parentId = 0) {
  return items.flatMap<MockMenuItem>((item) => {
    const menuId = getLegacyMenuId(item);
    const meta = item.meta ?? {};
    const menuType = getLegacyMenuType(item);
    const frameUrl = meta.iframeSrc ?? meta.link ?? item.frameUrl ?? '';
    const current: MockMenuItem = {
      apiPerms: item.apiPerms ?? '',
      cacheFlag: Boolean(item.cacheFlag ?? meta.keepAlive),
      component: item.component,
      contextMenuId: item.contextMenuId ?? null,
      createTime: item.createTime,
      disabledFlag: Boolean(item.disabledFlag ?? item.status === 0),
      frameFlag: Boolean(item.frameFlag ?? frameUrl),
      frameUrl,
      icon: item.icon ?? meta.icon,
      menuId,
      menuName: item.menuName ?? item.name ?? meta.title ?? '',
      menuType,
      parentId: Number(item.parentId ?? item.pid ?? parentId),
      path: item.path,
      permsType: item.permsType ?? 1,
      sort: item.sort ?? meta.order,
      updateTime: item.updateTime,
      visibleFlag: Boolean(item.visibleFlag ?? item.status !== 0),
      webPerms: item.webPerms ?? item.authCode ?? '',
    };

    const children = Array.isArray(item.children)
      ? flattenMenuItems(item.children, menuId)
      : [];

    return [current, ...children];
  });
}

function getMenuListQuery(event: H3Event) {
  const query = getQuery(event);
  return {
    cacheFlag: toQueryBoolean(query.cacheFlag),
    disabledFlag: toQueryBoolean(query.disabledFlag),
    frameFlag: toQueryBoolean(query.frameFlag),
    keywords: toQueryString(query.keywords ?? query.keyword),
    menuType: toQueryNumber(query.menuType),
    visibleFlag: toQueryBoolean(query.visibleFlag),
  } satisfies MenuListQuery;
}

function matchesKeyword(item: MockMenuItem, keyword?: string) {
  if (!keyword) {
    return true;
  }

  const normalizedKeyword = keyword.toLowerCase();
  return [
    item.apiPerms,
    item.component,
    item.menuName,
    item.path,
    item.webPerms,
  ].some((value) => value?.toLowerCase().includes(normalizedKeyword));
}

function filterMenuItems(items: MockMenuItem[], query: MenuListQuery) {
  return items.filter((item) => {
    return (
      matchesKeyword(item, query.keywords) &&
      (query.menuType === undefined || item.menuType === query.menuType) &&
      (query.disabledFlag === undefined ||
        item.disabledFlag === query.disabledFlag) &&
      (query.frameFlag === undefined || item.frameFlag === query.frameFlag) &&
      (query.cacheFlag === undefined || item.cacheFlag === query.cacheFlag) &&
      (query.visibleFlag === undefined ||
        item.visibleFlag === query.visibleFlag)
    );
  });
}

export default eventHandler(async (event) => {
  const userinfo = verifyAccessToken(event);
  if (!userinfo) {
    return unAuthorizedResponse(event);
  }

  const query = getMenuListQuery(event);
  const menuItems = flattenMenuItems(MOCK_MENU_LIST);

  return useResponseSuccess(filterMenuItems(menuItems, query));
});
