import { requestClient } from '#/api/request';

export namespace SystemDepartmentApi {
  /** 部门树节点 */
  export interface DepartmentItem {
    children?: DepartmentItem[] | null;
    createTime?: string;
    departmentId: number;
    departmentName: string;
    managerId?: null | number;
    managerName?: string;
    nextId?: null | number;
    parentId?: null | number;
    preId?: null | number;
    selfAndAllChildrenIdList?: number[];
    sort: number;
    updateTime?: string;
  }

  /** 部门新增参数 */
  export interface DepartmentAddParams {
    departmentName: string;
    managerId?: null | number;
    parentId?: null | number;
    sort: number;
  }

  /** 部门编辑参数 */
  export interface DepartmentUpdateParams extends DepartmentAddParams {
    departmentId: number;
  }

  /** 上级部门选项 */
  export interface DepartmentOption {
    children?: DepartmentOption[] | null;
    departmentId: number;
    departmentName: string;
    parentId?: null | number;
  }
}

/** 查询部门树 */
export async function queryDepartmentTreeApi(params?: { keywords?: string }) {
  return requestClient.get<SystemDepartmentApi.DepartmentItem[]>(
    '/department/treeList',
    { params },
  );
}

/** 新增部门 */
export async function addDepartmentApi(
  data: SystemDepartmentApi.DepartmentAddParams,
) {
  return requestClient.post<string>('/department/add', data);
}

/** 编辑部门 */
export async function updateDepartmentApi(
  data: SystemDepartmentApi.DepartmentUpdateParams,
) {
  return requestClient.post<string>('/department/update', data);
}

/** 删除部门 */
export async function deleteDepartmentApi(departmentId: number | string) {
  return requestClient.get<string>(`/department/delete/${departmentId}`);
}
 
/** 批量删除部门 */
export async function batchDeleteDepartmentApi(departmentIdList: number[]) {
  return requestClient.get<string>('/department/batchDelete', {
    params: {
      departmentIdList,
    },
  });
}
 
/** 查询全部部门选项 */
export async function listDepartmentOptionsApi() {
  return requestClient.get<SystemDepartmentApi.DepartmentOption[]>(
    '/department/listAll',
  );
}
