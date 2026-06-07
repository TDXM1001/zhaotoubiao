import { requestClient } from '#/api/request';

export namespace SupportFileApi {
  /** 文件分页查询参数 */
  export interface QueryParams {
    createTimeBegin?: string;
    createTimeEnd?: string;
    creatorName?: string;
    fileKey?: string;
    fileName?: string;
    fileType?: string;
    folderType?: number;
    pageNum: number;
    pageSize: number;
    searchCount?: boolean;
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

  /** 文件列表项 */
  export interface FileItem {
    createTime?: string;
    creatorId?: number;
    creatorName?: string;
    creatorUserType?: number;
    fileId: number;
    fileKey?: string;
    fileName: string;
    fileSize?: number;
    fileType?: string;
    fileUrl?: string;
    folderType?: number;
  }
}

/** 查询文件分页 */
export async function queryFilePageApi(data: SupportFileApi.QueryParams) {
  return requestClient.post<SupportFileApi.PageResult<SupportFileApi.FileItem>>(
    '/support/file/queryPage',
    data,
  );
}
 
/** 上传文件 */
export async function uploadFileApi(file: File | Blob) {
  return requestClient.upload<SupportFileApi.FileItem>('/support/file/upload', {
    file,
    folder: 3,
  });
}
