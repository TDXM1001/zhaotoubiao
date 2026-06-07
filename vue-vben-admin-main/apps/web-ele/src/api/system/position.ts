import { requestClient } from '#/api/request';

export namespace SystemPositionApi {
  /** 职务分页查询参数 */
  export interface PositionQueryParams {
    keywords?: string;
    pageNum: number;
    pageSize: number;
    searchCount?: boolean;
  }

  /** 职务分页结构 */
  export interface PageResult<T> {
    emptyFlag?: boolean;
    list: T[];
    pageNum: number;
    pageSize: number;
    pages: number;
    total: number;
  }

  /** 职务列表项 */
  export interface PositionItem {
    createTime?: string;
    positionId: number;
    positionLevel?: string;
    positionName: string;
    remark?: string;
    sort: number;
    updateTime?: string;
  }

  /** 职务新增参数 */
  export interface PositionAddParams {
    positionLevel?: string;
    positionName: string;
    remark?: string;
    sort: number;
  }

  /** 职务编辑参数 */
  export interface PositionUpdateParams extends PositionAddParams {
    positionId: number;
  }
}

/** 查询职务分页 */
export async function queryPositionPageApi(
  data: SystemPositionApi.PositionQueryParams,
) {
  return requestClient.post<
    SystemPositionApi.PageResult<SystemPositionApi.PositionItem>
  >('/position/queryPage', data);
}

/** 新增职务 */
export async function addPositionApi(data: SystemPositionApi.PositionAddParams) {
  return requestClient.post<string>('/position/add', data);
}

/** 编辑职务 */
export async function updatePositionApi(
  data: SystemPositionApi.PositionUpdateParams,
) {
  return requestClient.post<string>('/position/update', data);
}

/** 删除单个职务 */
export async function deletePositionApi(positionId: number | string) {
  return requestClient.get<string>(`/position/delete/${positionId}`);
}

/** 批量删除职务 */
export async function batchDeletePositionApi(positionIdList: number[]) {
  return requestClient.post<string>('/position/batchDelete', positionIdList);
}

/** 查询全部职务选项 */
export async function queryPositionListApi() {
  return requestClient.get<SystemPositionApi.PositionItem[]>('/position/queryList');
}
