import { requestClient } from '#/api/request';

export namespace SupportHelpDocApi {
  /** 帮助文档分页查询参数 */
  export interface QueryParams {
    createTimeBegin?: string;
    createTimeEnd?: string;
    helpDocCatalogId?: number;
    keywords?: string;
    pageNum: number;
    pageSize: number;
    searchCount?: boolean;
  }

  /** 目录项 */
  export interface CatalogItem {
    helpDocCatalogId: number;
    name: string;
    parentId?: number;
    sort?: number;
  }

  /** 目录新增参数 */
  export interface CatalogCreateParams {
    name: string;
    parentId?: number;
    sort?: number;
  }

  /** 目录编辑参数 */
  export interface CatalogUpdateParams extends CatalogCreateParams {
    helpDocCatalogId: number;
  }

  /** 文档关联项 */
  export interface RelationItem {
    relationId: number;
    relationName: string;
  }

  /** 文档新增参数 */
  export interface CreateParams {
    attachment?: string;
    author: string;
    contentHtml: string;
    contentText: string;
    helpDocCatalogId: number;
    relationList?: RelationItem[];
    sort: number;
    title: string;
  }

  /** 文档编辑参数 */
  export interface UpdateParams extends CreateParams {
    helpDocId: number;
  }

  /** 帮助文档列表项 */
  export interface HelpDocItem {
    author?: string;
    createTime?: string;
    helpDocCatalogId?: number;
    helpDocCatalogName?: string;
    helpDocId: number;
    pageViewCount?: number;
    sort?: number;
    title: string;
    updateTime?: string;
    userViewCount?: number;
  }

  /** 帮助文档详情 */
  export interface HelpDocDetailItem {
    attachment?: string;
    author?: string;
    contentHtml?: string;
    contentText?: string;
    createTime?: string;
    helpDocCatalogId?: number;
    helpDocCatalogName?: number | string;
    helpDocId: number;
    pageViewCount?: number;
    relationList?: RelationItem[];
    title: string;
    updateTime?: string;
    userViewCount?: number;
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

/** 查询帮助文档分页 */
export async function queryHelpDocPageApi(data: SupportHelpDocApi.QueryParams) {
  return requestClient.post<
    SupportHelpDocApi.PageResult<SupportHelpDocApi.HelpDocItem>
  >('/support/helpDoc/query', data);
}

/** 查询帮助文档详情 */
export async function getHelpDocDetailApi(helpDocId: number | string) {
  return requestClient.get<SupportHelpDocApi.HelpDocDetailItem>(
    `/support/helpDoc/getDetail/${helpDocId}`,
  );
}

/** 新增帮助文档 */
export async function addHelpDocApi(data: SupportHelpDocApi.CreateParams) {
  return requestClient.post<string>('/support/helpDoc/add', data);
}

/** 编辑帮助文档 */
export async function updateHelpDocApi(data: SupportHelpDocApi.UpdateParams) {
  return requestClient.post<string>('/support/helpDoc/update', data);
}

/** 删除帮助文档 */
export async function deleteHelpDocApi(helpDocId: number | string) {
  return requestClient.get<string>(`/support/helpDoc/delete/${helpDocId}`);
}

/** 查询全部帮助文档目录 */
export async function queryHelpDocCatalogListApi() {
  return requestClient.get<SupportHelpDocApi.CatalogItem[]>(
    '/support/helpDoc/helpDocCatalog/getAll',
  );
}

/** 新增帮助文档目录 */
export async function addHelpDocCatalogApi(
  data: SupportHelpDocApi.CatalogCreateParams,
) {
  return requestClient.post<string>('/support/helpDoc/helpDocCatalog/add', data);
}

/** 编辑帮助文档目录 */
export async function updateHelpDocCatalogApi(
  data: SupportHelpDocApi.CatalogUpdateParams,
) {
  return requestClient.post<string>(
    '/support/helpDoc/helpDocCatalog/update',
    data,
  );
}

/** 删除帮助文档目录 */
export async function deleteHelpDocCatalogApi(
  helpDocCatalogId: number | string,
) {
  return requestClient.get<string>(
    `/support/helpDoc/helpDocCatalog/delete/${helpDocCatalogId}`,
  );
}
