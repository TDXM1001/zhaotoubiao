import { requestClient } from '#/api/request';

export namespace SupportReloadApi {
  /** Reload 更新参数 */
  export interface UpdateParams {
    args?: string;
    identification: string;
    tag: string;
  }

  /** Reload 列表项 */
  export interface ReloadItem {
    args?: string;
    createTime?: string;
    identification?: string;
    tag: string;
    updateTime?: string;
  }

  /** Reload 结果项 */
  export interface ReloadResult {
    args?: string;
    createTime?: string;
    exception?: string;
    result?: boolean;
    tag: string;
  }
}

/** 查询 Reload 列表 */
export async function queryReloadListApi() {
  return requestClient.get<SupportReloadApi.ReloadItem[]>('/support/reload/query');
}

/** 查询指定 Reload 项执行结果 */
export async function queryReloadResultApi(tag: string) {
  return requestClient.get<SupportReloadApi.ReloadResult[]>(
    `/support/reload/result/${encodeURIComponent(tag)}`,
  );
}

/** 更新 Reload 标识 */
export async function updateReloadApi(data: SupportReloadApi.UpdateParams) {
  return requestClient.post<string>('/support/reload/update', data);
}

