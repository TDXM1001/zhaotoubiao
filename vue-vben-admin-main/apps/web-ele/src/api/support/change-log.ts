import { requestClient } from '#/api/request';

export namespace SupportChangeLogApi {
  /** 更新日志分页查询参数 */
  export interface QueryParams {
    createTime?: string;
    keyword?: string;
    link?: string;
    pageNum: number;
    pageSize: number;
    publicDateBegin?: string;
    publicDateEnd?: string;
    searchCount?: boolean;
    type?: number;
  }

  /** 更新日志新增参数 */
  export interface CreateParams {
    content: string;
    link?: string;
    publicDate: string;
    publishAuthor: string;
    type: number;
    updateVersion: string;
  }

  /** 更新日志编辑参数 */
  export interface UpdateParams extends CreateParams {
    changeLogId: number;
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

  /** 更新日志列表项 */
  export interface ChangeLogItem {
    changeLogId: number;
    content: string;
    createTime?: string;
    link?: string;
    publicDate?: string;
    publishAuthor?: string;
    type?: number;
    updateTime?: string;
    updateVersion: string;
  }
}

/** 查询更新日志分页 */
export async function queryChangeLogPageApi(
  data: SupportChangeLogApi.QueryParams,
) {
  return requestClient.post<
    SupportChangeLogApi.PageResult<SupportChangeLogApi.ChangeLogItem>
  >('/support/changeLog/queryPage', data);
}

/** 查询更新日志详情 */
export async function getChangeLogDetailApi(changeLogId: number | string) {
  return requestClient.get<SupportChangeLogApi.ChangeLogItem>(
    `/support/changeLog/getDetail/${changeLogId}`,
  );
}

/** 新增更新日志 */
export async function addChangeLogApi(data: SupportChangeLogApi.CreateParams) {
  return requestClient.post<string>('/support/changeLog/add', data);
}

/** 编辑更新日志 */
export async function updateChangeLogApi(
  data: SupportChangeLogApi.UpdateParams,
) {
  return requestClient.post<string>('/support/changeLog/update', data);
}

/** 删除单条更新日志 */
export async function deleteChangeLogApi(changeLogId: number | string) {
  return requestClient.get<string>(`/support/changeLog/delete/${changeLogId}`);
}

/** 批量删除更新日志 */
export async function batchDeleteChangeLogApi(changeLogIdList: number[]) {
  return requestClient.post<string>(
    '/support/changeLog/batchDelete',
    changeLogIdList,
  );
}
