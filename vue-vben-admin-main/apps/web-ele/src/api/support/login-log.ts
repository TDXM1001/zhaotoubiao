import { requestClient } from '#/api/request';

export namespace SupportLoginLogApi {
  /** 登录日志分页查询参数 */
  export interface QueryParams {
    endDate?: string;
    ip?: string;
    pageNum: number;
    pageSize: number;
    searchCount?: boolean;
    startDate?: string;
    userName?: string;
  }

  /** 登录日志列表项 */
  export interface LoginLogItem {
    createTime?: string;
    loginDevice?: string;
    loginIp?: string;
    loginIpRegion?: string;
    loginLogId: number | string;
    loginResult?: number;
    remark?: string;
    userAgent?: string;
    userName?: string;
  }

  /** 后端分页返回结构 */
  export interface PageResult<T> {
    emptyFlag?: boolean;
    list: T[];
    pageNum: number;
    pageSize: number;
    pages: number;
    total: number;
  }
}

/** 查询登录日志分页 */
export async function queryLoginLogPageApi(
  data: SupportLoginLogApi.QueryParams,
) {
  return requestClient.post<
    SupportLoginLogApi.PageResult<SupportLoginLogApi.LoginLogItem>
  >('/support/loginLog/page/query', data);
}
