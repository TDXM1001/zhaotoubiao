import { requestClient } from '#/api/request';

export namespace SupportConfigApi {
  /** 参数配置分页查询参数 */
  export interface QueryParams {
    configKey?: string;
    pageNum: number;
    pageSize: number;
    searchCount?: boolean;
    sortItemList?: Array<{
      column: string;
      isAsc: boolean;
    }>;
  }

  /** 参数配置新增参数 */
  export interface CreateParams {
    configKey: string;
    configName: string;
    configValue: string;
    remark?: string;
  }

  /** 参数配置编辑参数 */
  export interface UpdateParams extends CreateParams {
    configId: number | string;
  }

  /** 参数配置列表项 */
  export interface ConfigItem {
    configId: number | string;
    configKey: string;
    configName: string;
    configValue: string;
    createTime?: string;
    remark?: string;
    updateTime?: string;
  }

  /** 后端分页返回 */
  export interface PageResult<T> {
    list: T[];
    pageNum: number;
    pageSize: number;
    pages: number;
    total: number;
    emptyFlag?: boolean;
  }
}

/** 查询参数配置分页数据 */
export async function queryConfigPageApi(data: SupportConfigApi.QueryParams) {
  return requestClient.post<SupportConfigApi.PageResult<SupportConfigApi.ConfigItem>>(
    '/support/config/query',
    data,
  );
}

/** 新增参数配置 */
export async function addConfigApi(data: SupportConfigApi.CreateParams) {
  return requestClient.post<string>('/support/config/add', data);
}

/** 编辑参数配置 */
export async function updateConfigApi(data: SupportConfigApi.UpdateParams) {
  return requestClient.post<string>('/support/config/update', data);
}
