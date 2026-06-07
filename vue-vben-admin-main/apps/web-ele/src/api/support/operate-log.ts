import { requestClient } from '#/api/request';

export namespace SupportOperateLogApi {
  /** 操作日志分页查询参数 */
  export interface QueryParams {
    endDate?: string;
    keywords?: string;
    operateUserType?: number;
    pageNum: number;
    pageSize: number;
    requestKeywords?: string;
    startDate?: string;
    successFlag?: boolean;
    userName?: string;
  }

  /** 操作日志列表项 */
  export interface OperateLogItem {
    content?: string;
    createTime?: string;
    failReason?: string;
    ip?: string;
    ipRegion?: string;
    method?: string;
    module?: string;
    operateLogId: number | string;
    operateUserId?: number | string;
    operateUserName?: string;
    operateUserType?: number;
    param?: string;
    response?: string;
    successFlag?: boolean;
    updateTime?: string;
    url?: string;
    userAgent?: string;
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

/** 查询操作日志分页 */
export async function queryOperateLogPageApi(
  data: SupportOperateLogApi.QueryParams,
) {
  return requestClient.post<
    SupportOperateLogApi.PageResult<SupportOperateLogApi.OperateLogItem>
  >('/support/operateLog/page/query', data);
}

/** 查询操作日志详情 */
export async function getOperateLogDetailApi(
  operateLogId: number | string,
) {
  return requestClient.get<SupportOperateLogApi.OperateLogItem>(
    `/support/operateLog/detail/${operateLogId}`,
  );
}
