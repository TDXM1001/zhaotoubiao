import { requestClient } from '#/api/request';

export namespace SystemMenuApi {
  /** 菜单类型 */
  export type MenuTypeValue = 1 | 2 | 3;

  /** 权限类型 */
  export type PermsTypeValue = 1;

  /** 菜单列表查询参数 */
  export interface MenuQueryParams {
    cacheFlag?: boolean;
    disabledFlag?: boolean;
    frameFlag?: boolean;
    keywords?: string;
    menuType?: MenuTypeValue;
    visibleFlag?: boolean;
  }

  /** 菜单列表项 */
  export interface MenuItem {
    apiPerms?: string;
    cacheFlag: boolean;
    children?: MenuItem[];
    component?: string;
    contextMenuId?: null | number;
    createTime?: string;
    disabledFlag: boolean;
    frameFlag: boolean;
    frameUrl?: string;
    icon?: string;
    menuId: number;
    menuName: string;
    menuType: MenuTypeValue;
    parentId: number;
    path?: string;
    permsType?: PermsTypeValue;
    sort?: number;
    updateTime?: string;
    visibleFlag: boolean;
    webPerms?: string;
  }

  /** 菜单树节点 */
  export interface MenuTreeItem {
    children?: MenuTreeItem[];
    disabledFlag: boolean;
    menuId: number;
    menuName: string;
    menuType: MenuTypeValue;
    parentId: number;
  }

  /** 菜单表单模型 */
  export interface MenuFormModel {
    apiPerms: string;
    apiPermsList: string[];
    cacheFlag: boolean;
    component: string;
    contextMenuId?: number;
    disabledFlag: boolean;
    frameFlag: boolean;
    frameUrl: string;
    icon: string;
    menuName: string;
    menuType: MenuTypeValue;
    parentId: number;
    path: string;
    permsType: PermsTypeValue;
    sort: number;
    visibleFlag: boolean;
    webPerms: string;
  }

  /** 新增菜单参数 */
  export interface MenuAddParams {
    apiPerms?: string;
    cacheFlag: boolean;
    component?: string;
    contextMenuId?: null | number;
    disabledFlag: boolean;
    frameFlag: boolean;
    frameUrl?: string;
    icon?: string;
    menuName: string;
    menuType: MenuTypeValue;
    parentId: number;
    path?: string;
    permsType?: PermsTypeValue;
    sort?: number;
    visibleFlag: boolean;
    webPerms?: string;
  }

  /** 编辑菜单参数 */
  export interface MenuUpdateParams extends MenuAddParams {
    menuId: number;
  }

  /** 请求路径候选 */
  export interface AuthUrlItem {
    comment?: string;
    name?: string;
    url: string;
  }
}

/** 查询菜单列表 */
export async function queryMenuListApi(params?: SystemMenuApi.MenuQueryParams) {
  return requestClient.get<SystemMenuApi.MenuItem[]>('/menu/query', {
    params,
  });
}

/** 查询菜单详情 */
export async function getMenuDetailApi(menuId: number | string) {
  return requestClient.get<SystemMenuApi.MenuItem>(`/menu/detail/${menuId}`);
}

/** 查询菜单树 */
export async function queryMenuTreeApi(onlyMenu = true) {
  return requestClient.get<SystemMenuApi.MenuTreeItem[]>('/menu/tree', {
    params: {
      onlyMenu,
    },
  });
}

/** 查询系统所有请求路径 */
export async function getMenuAuthUrlApi() {
  return requestClient.get<SystemMenuApi.AuthUrlItem[]>('/menu/auth/url');
}

/** 新增菜单 */
export async function addMenuApi(data: SystemMenuApi.MenuAddParams) {
  return requestClient.post<string>('/menu/add', data);
}

/** 编辑菜单 */
export async function updateMenuApi(data: SystemMenuApi.MenuUpdateParams) {
  return requestClient.post<string>('/menu/update', data);
}

/** 批量删除菜单 */
export async function batchDeleteMenuApi(menuIdList: number[]) {
  return requestClient.get<string>('/menu/batchDelete', {
    params: {
      menuIdList,
    },
  });
}
