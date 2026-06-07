import { requestClient } from '#/api/request';

export namespace SupportJobApi {
  export type TriggerType = 'cron' | 'fixed_delay';

  /** 定时任务分页查询参数 */
  export interface QueryParams {
    deletedFlag?: boolean;
    enabledFlag?: boolean;
    pageNum: number;
    pageSize: number;
    searchCount?: boolean;
    searchWord?: string;
    triggerType?: TriggerType;
  }

  /** 定时任务新增参数 */
  export interface CreateParams {
    enabledFlag: boolean;
    jobClass: string;
    jobName: string;
    param?: string;
    remark?: string;
    sort: number;
    triggerType: TriggerType;
    triggerValue: string;
  }

  /** 定时任务编辑参数 */
  export interface UpdateParams extends CreateParams {
    jobId: number;
  }

  /** 定时任务启停参数 */
  export interface EnabledUpdateParams {
    enabledFlag: boolean;
    jobId: number;
  }

  /** 定时任务立即执行参数 */
  export interface ExecuteParams {
    jobId: number;
    param?: string;
  }

  /** 定时任务执行日志查询参数 */
  export interface LogQueryParams {
    endTime?: string;
    jobId?: number;
    pageNum: number;
    pageSize: number;
    searchCount?: boolean;
    searchWord?: string;
    startTime?: string;
    successFlag?: boolean;
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

  /** 定时任务列表项 */
  export interface JobItem {
    createTime?: string;
    enabledFlag?: boolean;
    jobClass?: string;
    jobId: number;
    jobName?: string;
    lastExecuteLogId?: number;
    lastExecuteTime?: string;
    lastJobLog?: JobLogItem;
    nextJobExecuteTimeList?: string[];
    param?: string;
    remark?: string;
    sort?: number;
    triggerType?: TriggerType;
    triggerValue?: string;
    updateName?: string;
    updateTime?: string;
  }

  /** 定时任务执行日志 */
  export interface JobLogItem {
    createName?: string;
    createTime?: string;
    executeEndTime?: string;
    executeResult?: string;
    executeStartTime?: string;
    executeTimeMillis?: number;
    ip?: string;
    jobId?: number;
    jobName?: string;
    logId: number;
    param?: string;
    processId?: string;
    programPath?: string;
    successFlag?: boolean;
  }
}

/** 查询定时任务分页 */
export async function queryJobPageApi(data: SupportJobApi.QueryParams) {
  return requestClient.post<
    SupportJobApi.PageResult<SupportJobApi.JobItem>
  >('/support/job/query', data);
}

/** 查询定时任务详情 */
export async function getJobDetailApi(jobId: number) {
  return requestClient.get<SupportJobApi.JobItem>(`/support/job/${jobId}`);
}

/** 新增定时任务 */
export async function addJobApi(data: SupportJobApi.CreateParams) {
  return requestClient.post<string>('/support/job/add', data);
}

/** 编辑定时任务 */
export async function updateJobApi(data: SupportJobApi.UpdateParams) {
  return requestClient.post<string>('/support/job/update', data);
}

/** 更新定时任务启用状态 */
export async function updateJobEnabledApi(
  data: SupportJobApi.EnabledUpdateParams,
) {
  return requestClient.post<string>('/support/job/update/enabled', data);
}

/** 立即执行定时任务 */
export async function executeJobApi(data: SupportJobApi.ExecuteParams) {
  return requestClient.post<string>('/support/job/execute', data);
}

/** 删除定时任务 */
export async function deleteJobApi(jobId: number) {
  return requestClient.get<string>('/support/job/delete', {
    params: {
      jobId,
    },
  });
}

/** 查询定时任务执行日志分页 */
export async function queryJobLogPageApi(data: SupportJobApi.LogQueryParams) {
  return requestClient.post<
    SupportJobApi.PageResult<SupportJobApi.JobLogItem>
  >('/support/job/log/query', data);
}
