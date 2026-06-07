import { requestClient } from '#/api/request';

export namespace SupportDictApi {
  /** 数据字典分页查询参数 */
  export interface DictQueryParams {
    disabledFlag?: boolean;
    keywords?: string;
    pageNum: number;
    pageSize: number;
    searchCount?: boolean;
    sortItemList?: Array<{
      column: string;
      isAsc: boolean;
    }>;
  }

  /** 数据字典新增参数 */
  export interface DictAddParams {
    dictCode: string;
    dictName: string;
    remark?: string;
  }

  /** 数据字典编辑参数 */
  export interface DictUpdateParams extends DictAddParams {
    dictId: number | string;
  }

  /** 数据字典列表项 */
  export interface DictItem {
    createTime?: string;
    dictCode: string;
    dictId: number | string;
    dictName: string;
    disabledFlag?: boolean;
    remark?: string;
    updateTime?: string;
  }

  /** 字典项样式值 */
  export type DictDataStyleValue =
    | 'danger'
    | 'default'
    | 'info'
    | 'primary'
    | 'success'
    | 'warning';

  /** 字典项新增参数 */
  export interface DictDataAddParams {
    dataLabel: string;
    dataStyle?: DictDataStyleValue;
    dataValue: string;
    dictId: number | string;
    remark?: string;
    sortOrder: number;
  }

  /** 字典项编辑参数 */
  export interface DictDataUpdateParams extends DictDataAddParams {
    dictCode: string;
    dictDataId: number | string;
  }

  /** 字典项列表项 */
  export interface DictDataItem {
    createTime?: string;
    dataLabel: string;
    dataStyle?: DictDataStyleValue;
    dataValue: string;
    dictCode: string;
    dictDataId: number | string;
    dictDisabledFlag?: boolean;
    dictId: number | string;
    dictName?: string;
    disabledFlag?: boolean;
    remark?: string;
    sortOrder: number;
    updateTime?: string;
  }

  /** 后端分页返回 */
  export interface PageResult<T> {
    emptyFlag?: boolean;
    list: T[];
    pageNum: number;
    pageSize: number;
    pages: number;
    total: number;
  }
}

export const dictDataStyleOptions: Array<{
  label: string;
  value: SupportDictApi.DictDataStyleValue;
}> = [
  { label: '默认', value: 'default' },
  { label: '主要', value: 'primary' },
  { label: '成功', value: 'success' },
  { label: '信息', value: 'info' },
  { label: '警告', value: 'warning' },
  { label: '危险', value: 'danger' },
];

/** 获取所有字典定义 */
export async function getAllDictApi() {
  return requestClient.get<SupportDictApi.DictItem[]>('/support/dict/getAllDict');
}

/** 获取所有字典项，供前端按字典编码缓存使用 */
export async function getAllDictDataApi() {
  return requestClient.get<SupportDictApi.DictDataItem[]>(
    '/support/dict/getAllDictData',
  );
}

/** 查询数据字典分页数据 */
export async function queryDictPageApi(data: SupportDictApi.DictQueryParams) {
  return requestClient.post<SupportDictApi.PageResult<SupportDictApi.DictItem>>(
    '/support/dict/queryPage',
    data,
  );
}

/** 新增数据字典 */
export async function addDictApi(data: SupportDictApi.DictAddParams) {
  return requestClient.post<string>('/support/dict/add', data);
}

/** 编辑数据字典 */
export async function updateDictApi(data: SupportDictApi.DictUpdateParams) {
  return requestClient.post<string>('/support/dict/update', data);
}

/** 切换数据字典启用状态 */
export async function updateDictDisabledApi(dictId: number | string) {
  return requestClient.get<string>(`/support/dict/updateDisabled/${dictId}`);
}

/** 删除数据字典 */
export async function deleteDictApi(dictId: number | string) {
  return requestClient.get<string>(`/support/dict/delete/${dictId}`);
}

/** 查询字典项列表 */
export async function queryDictDataListApi(dictId: number | string) {
  return requestClient.get<SupportDictApi.DictDataItem[]>(
    `/support/dict/dictData/queryDictData/${dictId}`,
  );
}

/** 新增字典项 */
export async function addDictDataApi(data: SupportDictApi.DictDataAddParams) {
  return requestClient.post<string>('/support/dict/dictData/add', data);
}

/** 编辑字典项 */
export async function updateDictDataApi(
  data: SupportDictApi.DictDataUpdateParams,
) {
  return requestClient.post<string>('/support/dict/dictData/update', data);
}

/** 切换字典项启用状态 */
export async function updateDictDataDisabledApi(
  dictDataId: number | string,
) {
  return requestClient.get<string>(
    `/support/dict/dictData/updateDisabled/${dictDataId}`,
  );
}

/** 删除字典项 */
export async function deleteDictDataApi(dictDataId: number | string) {
  return requestClient.get<string>(
    `/support/dict/dictData/delete/${dictDataId}`,
  );
}
