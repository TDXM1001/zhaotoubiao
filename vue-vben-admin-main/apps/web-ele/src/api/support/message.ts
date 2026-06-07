import { requestClient } from '#/api/request';

export namespace SupportMessageApi {
  export type MessageType = 1 | 2;
  export type ReceiverUserType = 1;

  /** 消息分页查询参数 */
  export interface QueryParams {
    endDate?: string;
    messageType?: MessageType;
    pageNum: number;
    pageSize: number;
    readFlag?: boolean;
    receiverUserId?: number;
    receiverUserType?: ReceiverUserType;
    searchCount?: boolean;
    searchWord?: string;
    startDate?: string;
  }

  /** 消息发送参数 */
  export interface SendParams {
    content: string;
    dataId?: unknown;
    messageType: MessageType;
    receiverUserId: number;
    receiverUserType: ReceiverUserType;
    title: string;
  }

  /** 消息列表项 */
  export interface MessageItem {
    content?: string;
    createTime?: string;
    dataId?: string;
    messageId: number;
    messageType?: MessageType;
    readFlag?: boolean;
    readTime?: string;
    receiverUserId?: number;
    receiverUserType?: ReceiverUserType;
    title?: string;
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

/** 查询消息分页 */
export async function queryMessagePageApi(data: SupportMessageApi.QueryParams) {
  return requestClient.post<
    SupportMessageApi.PageResult<SupportMessageApi.MessageItem>
  >('/support/message/query', data);
}

/** 发送消息，后端要求数组 */
export async function sendMessagesApi(data: SupportMessageApi.SendParams[]) {
  return requestClient.post<string>('/support/message/sendMessages', data);
}

/** 删除消息 */
export async function deleteMessageApi(messageId: number) {
  return requestClient.get<string>(`/support/message/delete/${messageId}`);
}
