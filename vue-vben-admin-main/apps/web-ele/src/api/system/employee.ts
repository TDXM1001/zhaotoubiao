import { requestClient } from '#/api/request';

export namespace SystemEmployeeApi {
  /** 员工分页查询参数 */
  export interface EmployeeQueryParams {
    departmentId?: number;
    disabledFlag?: boolean;
    employeeIdList?: number[];
    keyword?: string;
    pageNum: number;
    pageSize: number;
    searchCount?: boolean;
  }

  /** 员工分页结果 */
  export interface PageResult<T> {
    emptyFlag?: boolean;
    list: T[];
    pageNum: number;
    pageSize: number;
    pages: number;
    total: number;
  }

  /** 员工列表项 */
  export interface EmployeeItem {
    actualName: string;
    administratorFlag?: boolean;
    createTime?: string;
    departmentId: number;
    departmentName?: string;
    disabledFlag: boolean;
    email: string;
    employeeId: number;
    gender?: number;
    loginName: string;
    phone: string;
    positionId?: null | number;
    positionName?: string;
    remark?: string;
    roleIdList?: number[];
    roleNameList?: string[];
  }

  /** 员工新增参数 */
  export interface EmployeeAddParams {
    actualName: string;
    departmentId: number;
    disabledFlag: boolean;
    email: string;
    gender?: number;
    loginName: string;
    phone: string;
    positionId?: null | number;
    remark?: string;
    roleIdList?: number[];
  }

  /** 员工编辑参数 */
  export interface EmployeeUpdateParams extends EmployeeAddParams {
    employeeId: number;
  }

  /** 批量调部门参数 */
  export interface BatchUpdateDepartmentParams {
    departmentId: number;
    employeeIdList: number[];
  }

  /** 角色选项 */
  export interface RoleOption {
    remark?: string;
    roleCode?: string;
    roleId: number;
    roleName: string;
  }

  /** 全量员工选项 */
  export interface EmployeeOption {
    actualName: string;
    departmentId?: null | number;
    departmentName?: string;
    disabledFlag?: boolean;
    employeeId: number;
  }
}

/** 查询员工分页 */
export async function queryEmployeePageApi(
  data: SystemEmployeeApi.EmployeeQueryParams,
) {
  return requestClient.post<
    SystemEmployeeApi.PageResult<SystemEmployeeApi.EmployeeItem>
  >('/employee/query', data);
}

/** 新增员工 */
export async function addEmployeeApi(
  data: SystemEmployeeApi.EmployeeAddParams,
) {
  return requestClient.post<string>('/employee/add', data);
}

/** 编辑员工 */
export async function updateEmployeeApi(
  data: SystemEmployeeApi.EmployeeUpdateParams,
) {
  return requestClient.post<string>('/employee/update', data);
}

/** 切换员工启停状态 */
export async function updateEmployeeDisabledApi(employeeId: number | string) {
  return requestClient.get<string>(`/employee/update/disabled/${employeeId}`);
}

/** 批量删除员工 */
export async function batchDeleteEmployeeApi(employeeIdList: number[]) {
  return requestClient.post<string>(
    '/employee/update/batch/delete',
    employeeIdList,
  );
}

/** 批量调整员工部门 */
export async function batchUpdateEmployeeDepartmentApi(
  data: SystemEmployeeApi.BatchUpdateDepartmentParams,
) {
  return requestClient.post<string>('/employee/update/batch/department', data);
}

/** 重置员工密码 */
export async function resetEmployeePasswordApi(employeeId: number | string) {
  return requestClient.get<string>(
    `/employee/update/password/reset/${employeeId}`,
  );
}

/** 查询全部员工选项 */
export async function queryAllEmployeeApi(disabledFlag?: boolean) {
  return requestClient.get<SystemEmployeeApi.EmployeeOption[]>(
    '/employee/queryAll',
    {
      params:
        disabledFlag === undefined
          ? undefined
          : {
              disabledFlag,
            },
    },
  );
}
