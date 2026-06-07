import { requestClient } from '#/api/request';

export namespace SystemRoleApi {
  /** 角色列表项 */
  export interface RoleItem {
    remark?: string;
    roleCode: string;
    roleId: number;
    roleName: string;
  }

  /** 角色表单模型 */
  export interface RoleFormModel {
    remark?: string;
    roleCode: string;
    roleName: string;
  }

  /** 角色筛选参数 */
  export interface RoleFilterParams {
    roleCode?: string;
    roleName?: string;
  }

  /** 菜单树节点 */
  export interface RoleMenuTreeItem {
    children?: RoleMenuTreeItem[];
    contextMenuId?: null | number;
    menuId: number;
    menuName: string;
    menuType?: number;
    parentId?: null | number;
  }

  /** 角色菜单树 */
  export interface RoleMenuTree {
    menuTreeList: RoleMenuTreeItem[];
    roleId: number;
    selectedMenuId: number[];
  }

  /** 角色菜单更新参数 */
  export interface RoleMenuUpdateParams {
    menuIdList: number[];
    roleId: number;
  }

  /** 数据范围可选项 */
  export interface DataScopeViewTypeItem {
    viewType: number;
    viewTypeLevel: number;
    viewTypeName: string;
  }

  /** 数据范围项 */
  export interface DataScopeItem {
    dataScopeType: number;
    dataScopeTypeDesc: string;
    dataScopeTypeName: string;
    dataScopeTypeSort: number;
    viewTypeList: DataScopeViewTypeItem[];
  }

  /** 角色已有数据范围项 */
  export interface RoleDataScopeItem {
    dataScopeType: number;
    viewType: number;
  }

  /** 角色数据范围更新项 */
  export interface RoleDataScopeUpdateItem {
    dataScopeType: number;
    viewType: number;
  }

  /** 角色数据范围更新参数 */
  export interface RoleDataScopeUpdateParams {
    dataScopeItemList: RoleDataScopeUpdateItem[];
    roleId: number;
  }

  /** 角色成员分页参数 */
  export interface RoleEmployeeQueryParams {
    keywords?: string;
    pageNum: number;
    pageSize: number;
    roleId: number | string;
    searchCount?: boolean;
  }

  /** 角色成员批量更新参数 */
  export interface RoleEmployeeBatchUpdateParams {
    employeeIdList: number[];
    roleId: number;
  }

  /** 通用分页结构 */
  export interface PageResult<T> {
    emptyFlag?: boolean;
    list: T[];
    pageNum: number;
    pageSize: number;
    pages: number;
    total: number;
  }
}

/** 查询全部角色 */
export async function getAllRoleApi() {
  return requestClient.get<SystemRoleApi.RoleItem[]>('/role/getAll');
}

/** 查询角色详情 */
export async function getRoleDetailApi(roleId: number | string) {
  return requestClient.get<SystemRoleApi.RoleItem>(`/role/get/${roleId}`);
}

/** 新增角色 */
export async function addRoleApi(data: SystemRoleApi.RoleFormModel) {
  return requestClient.post<string>('/role/add', data);
}

/** 编辑角色 */
export async function updateRoleApi(
  data: SystemRoleApi.RoleFormModel & {
    roleId: number;
  },
) {
  return requestClient.post<string>('/role/update', data);
}

/** 删除角色 */
export async function deleteRoleApi(roleId: number | string) {
  return requestClient.get<string>(`/role/delete/${roleId}`);
}

/** 查询角色菜单树与选中项 */
export async function getRoleSelectedMenuApi(roleId: number | string) {
  return requestClient.get<SystemRoleApi.RoleMenuTree>(
    `/role/menu/getRoleSelectedMenu/${roleId}`,
  );
}

/** 更新角色菜单权限 */
export async function updateRoleMenuApi(
  data: SystemRoleApi.RoleMenuUpdateParams,
) {
  return requestClient.post<string>('/role/menu/updateRoleMenu', data);
}

/** 查询系统数据范围定义 */
export async function getDataScopeListApi() {
  return requestClient.get<SystemRoleApi.DataScopeItem[]>('/dataScope/list');
}

/** 查询角色数据范围回填 */
export async function getRoleDataScopeListApi(roleId: number | string) {
  return requestClient.get<SystemRoleApi.RoleDataScopeItem[]>(
    `/role/dataScope/getRoleDataScopeList/${roleId}`,
  );
}

/** 更新角色数据范围 */
export async function updateRoleDataScopeListApi(
  data: SystemRoleApi.RoleDataScopeUpdateParams,
) {
  return requestClient.post<string>(
    '/role/dataScope/updateRoleDataScopeList',
    data,
  );
}

/** 查询角色成员分页 */
export async function queryRoleEmployeePageApi(
  data: SystemRoleApi.RoleEmployeeQueryParams,
) {
  return requestClient.post<
    SystemRoleApi.PageResult<{
      actualName: string;
      createTime?: string;
      departmentId?: null | number;
      departmentName?: string;
      disabledFlag?: boolean;
      employeeId: number;
      loginName: string;
      phone?: string;
    }>
  >('/role/employee/queryEmployee', data);
}

/** 移除单个角色成员 */
export async function removeRoleEmployeeApi(
  employeeId: number | string,
  roleId: number | string,
) {
  return requestClient.get<string>('/role/employee/removeEmployee', {
    params: {
      employeeId,
      roleId,
    },
  });
}

/** 批量移除角色成员 */
export async function batchRemoveRoleEmployeeApi(
  data: SystemRoleApi.RoleEmployeeBatchUpdateParams,
) {
  return requestClient.post<string>(
    '/role/employee/batchRemoveRoleEmployee',
    data,
  );
}

/** 批量添加角色成员 */
export async function batchAddRoleEmployeeApi(
  data: SystemRoleApi.RoleEmployeeBatchUpdateParams,
) {
  return requestClient.post<string>('/role/employee/batchAddRoleEmployee', data);
}
