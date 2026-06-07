import { requestClient } from '#/api/request';

export namespace SupportProtectApi {
  /** 登录失败记录分页查询参数 */
  export interface LoginFailQueryParams {
    lockFlag?: boolean;
    loginLockBeginTimeBegin?: string;
    loginLockBeginTimeEnd?: string;
    loginName?: string;
    pageNum: number;
    pageSize: number;
    searchCount?: boolean;
  }

  /** 登录失败记录 */
  export interface LoginFailItem {
    createTime?: string;
    lockFlag?: number;
    loginFailCount?: number;
    loginFailId: number;
    loginLockBeginTime?: string;
    loginName?: string;
    updateTime?: string;
    userId?: number;
    userType?: number;
  }

  /** 三级等保配置 */
  export interface Level3ProtectConfig {
    fileDetectFlag: boolean;
    loginActiveTimeoutMinutes: number;
    loginFailLockMinutes: number;
    loginFailMaxTimes: number;
    maxUploadFileSizeMb: number;
    passwordComplexityEnabled: boolean;
    regularChangePasswordMonths: number;
    regularChangePasswordNotAllowRepeatTimes: number;
    twoFactorLoginEnabled: boolean;
  }

  /** 脱敏演示数据 */
  export interface DataMaskingItem {
    address?: string;
    bankCard?: string;
    carLicense?: string;
    email?: string;
    idCard?: string;
    other?: string;
    password?: string;
    phone?: string;
    userId?: number;
  }

  /** 后端分页结构 */
  export interface PageResult<T> {
    emptyFlag?: boolean;
    list: T[];
    pageNum: number;
    pageSize: number;
    pages: number;
    total: number;
  }
}

/** 查询登录失败锁定记录分页 */
export async function queryLoginFailPageApi(
  data: SupportProtectApi.LoginFailQueryParams,
) {
  return requestClient.post<
    SupportProtectApi.PageResult<SupportProtectApi.LoginFailItem>
  >('/support/protect/loginFail/queryPage', data);
}

/** 批量删除登录失败锁定记录 */
export async function batchDeleteLoginFailApi(loginFailIdList: number[]) {
  return requestClient.post<string>(
    '/support/protect/loginFail/batchDelete',
    loginFailIdList,
  );
}

/** 查询三级等保配置，后端返回 JSON 字符串 */
export async function getLevel3ProtectConfigApi() {
  return requestClient.get<string>('/support/protect/level3protect/getConfig');
}

/** 更新三级等保配置 */
export async function updateLevel3ProtectConfigApi(
  data: SupportProtectApi.Level3ProtectConfig,
) {
  return requestClient.post<string>(
    '/support/protect/level3protect/updateConfig',
    data,
  );
}

/** 查询脱敏演示数据 */
export async function queryDataMaskingDemoApi() {
  return requestClient.get<SupportProtectApi.DataMaskingItem[]>(
    '/support/dataMasking/demo/query',
  );
}
