import { requestClient } from '#/api/request';

export namespace SupportHeartBeatApi {
  /** 心跳记录分页查询参数 */
  export interface QueryParams {
    endDate?: string;
    keywords?: string;
    pageNum: number;
    pageSize: number;
    searchCount?: boolean;
    startDate?: string;
  }

  /** 心跳记录列表项 */
  export interface HeartBeatRecord {
    heartBeatRecordId: number;
    heartBeatTime?: string;
    processNo?: number;
    processStartTime?: string;
    projectPath?: string;
    serverIp?: string;
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

/** 查询心跳记录分页 */
export async function queryHeartBeatPageApi(
  data: SupportHeartBeatApi.QueryParams,
) {
  return requestClient.post<
    SupportHeartBeatApi.PageResult<SupportHeartBeatApi.HeartBeatRecord>
  >('/support/heartBeat/query', data);
}
