import { requestClient } from '#/api/request';

export namespace SystemBidProjectApi {
  /** 分页结果 */
  export interface PageResult<T> {
    emptyFlag?: boolean;
    list: T[];
    pageNum: number;
    pageSize: number;
    pages: number;
    total: number;
  }

  /** 项目分页查询参数 */
  export interface ProjectQueryParams {
    keyword?: string;
    ownerOrgId?: number;
    pageNum: number;
    pageSize: number;
    procurementMode?: string;
    projectType?: string;
    searchCount?: boolean;
    status?: string;
  }

  /** 项目动作参数 */
  export interface ProjectActionParams {
    projectId: number;
    remark?: string;
    version: number;
  }

  /** 项目列表项 */
  export interface ProjectItem {
    agentOrgId?: null | number;
    agentOrgName?: string;
    allowedActions?: string[];
    archiveTime?: string;
    budgetAmount?: null | number;
    createTime?: string;
    lotCount?: number;
    managerEmployeeId?: null | number;
    managerEmployeeName?: string;
    ownerOrgId: number;
    ownerOrgName?: string;
    projectCode: string;
    projectId: number;
    projectName: string;
    projectType: string;
    procurementMode: string;
    publishTime?: string;
    remark?: string;
    status: string;
    updateTime?: string;
    version: number;
  }

  /** 项目选项 */
  export interface ProjectOption {
    projectCode: string;
    projectId: number;
    projectName: string;
    status: string;
  }

  /** 项目新增参数 */
  export interface ProjectAddParams {
    agentOrgId?: null | number;
    budgetAmount?: null | number;
    managerEmployeeId?: null | number;
    ownerOrgId: number;
    projectCode: string;
    projectName: string;
    projectType: string;
    procurementMode: string;
    remark?: string;
  }

  /** 项目编辑参数 */
  export interface ProjectUpdateParams extends ProjectAddParams {
    projectId: number;
    version: number;
  }
}

export namespace SystemBidLotApi {
  /** 标段分页查询参数 */
  export interface LotQueryParams {
    keyword?: string;
    pageNum: number;
    pageSize: number;
    projectId?: number;
    searchCount?: boolean;
    status?: string;
  }

  /** 标段动作参数 */
  export interface LotActionParams {
    lotId: number;
    remark?: string;
    version: number;
  }

  /** 标段列表项 */
  export interface LotItem {
    allowedActions?: string[];
    awardMode?: string;
    bidEndTime?: string;
    bidStartTime?: string;
    budgetAmount?: null | number;
    createTime?: string;
    evaluationMode?: string;
    lotCode: string;
    lotId: number;
    lotName: string;
    lotNo: number;
    lotScope?: string;
    openingTime?: string;
    projectId: number;
    projectName?: string;
    registrationEndTime?: string;
    registrationStartTime?: string;
    remark?: string;
    status: string;
    updateTime?: string;
    version: number;
  }

  /** 标段新增参数 */
  export interface LotAddParams {
    awardMode?: string;
    bidEndTime?: string;
    bidStartTime?: string;
    budgetAmount?: null | number;
    evaluationMode?: string;
    lotCode: string;
    lotName: string;
    lotNo: number;
    lotScope?: string;
    openingTime?: string;
    projectId: number;
    registrationEndTime?: string;
    registrationStartTime?: string;
    remark?: string;
  }

  /** 标段编辑参数 */
  export interface LotUpdateParams extends LotAddParams {
    lotId: number;
    version: number;
  }
}

export namespace SystemBidRegistrationApi {
  /** 报名分页查询参数 */
  export interface RegistrationQueryParams {
    keyword?: string;
    lotId?: number;
    pageNum: number;
    pageSize: number;
    projectId?: number;
    registrationType?: string;
    searchCount?: boolean;
    status?: string;
  }

  /** 报名审核动作参数 */
  export interface RegistrationActionParams {
    registrationId: number;
    remark?: string;
    version: number;
  }

  /** 报名列表项 */
  export interface RegistrationItem {
    allowedActions?: string[];
    cancelReason?: string;
    cancelTime?: string;
    contactEmail?: string;
    contactName: string;
    contactPhone: string;
    createTime?: string;
    lotCode?: string;
    lotId: number;
    lotName?: string;
    projectCode?: string;
    projectId: number;
    projectName?: string;
    qualifiedBy?: null | number;
    qualifiedResult?: string;
    qualifiedTime?: string;
    registrationId: number;
    registrationType: string;
    rejectReason?: string;
    remark?: string;
    status: string;
    submitTime?: string;
    supplierCreditCode: string;
    supplierEnterpriseId?: null | number;
    supplierNameSnapshot: string;
    updateTime?: string;
    version: number;
  }

  /** 报名新增参数 */
  export interface RegistrationAddParams {
    contactEmail?: string;
    contactName?: string;
    contactPhone?: string;
    lotId: number;
    projectId: number;
    registrationType: string;
    remark?: string;
    supplierCreditCode: string;
    supplierEnterpriseId?: null | number;
    supplierNameSnapshot: string;
  }
}

/** 查询项目分页 */
export async function queryBidProjectPageApi(
  data: SystemBidProjectApi.ProjectQueryParams,
) {
  return requestClient.post<
    SystemBidProjectApi.PageResult<SystemBidProjectApi.ProjectItem>
  >('/bid/project/queryPage', data);
}

/** 查询项目详情 */
export async function getBidProjectDetailApi(projectId: number | string) {
  return requestClient.get<SystemBidProjectApi.ProjectItem>(
    `/bid/project/get/${projectId}`,
  );
}

/** 查询项目下拉 */
export async function queryBidProjectListApi(status?: string) {
  return requestClient.get<SystemBidProjectApi.ProjectOption[]>(
    '/bid/project/queryList',
    {
      params: status ? { status } : undefined,
    },
  );
}

/** 新增项目 */
export async function addBidProjectApi(
  data: SystemBidProjectApi.ProjectAddParams,
) {
  return requestClient.post<string>('/bid/project/add', data);
}

/** 编辑项目 */
export async function updateBidProjectApi(
  data: SystemBidProjectApi.ProjectUpdateParams,
) {
  return requestClient.post<string>('/bid/project/update', data);
}

/** 提交项目计划 */
export async function submitBidProjectPlanApi(
  data: SystemBidProjectApi.ProjectActionParams,
) {
  return requestClient.post<string>('/bid/project/submitPlan', data);
}

/** 发布项目 */
export async function publishBidProjectApi(
  data: SystemBidProjectApi.ProjectActionParams,
) {
  return requestClient.post<string>('/bid/project/publish', data);
}

/** 作废项目 */
export async function cancelBidProjectApi(
  data: SystemBidProjectApi.ProjectActionParams,
) {
  return requestClient.post<string>('/bid/project/cancel', data);
}

/** 查询标段分页 */
export async function queryBidLotPageApi(data: SystemBidLotApi.LotQueryParams) {
  return requestClient.post<
    SystemBidProjectApi.PageResult<SystemBidLotApi.LotItem>
  >('/bid/lot/queryPage', data);
}

/** 查询标段详情 */
export async function getBidLotDetailApi(lotId: number | string) {
  return requestClient.get<SystemBidLotApi.LotItem>(`/bid/lot/get/${lotId}`);
}

/** 查询项目下全部标段 */
export async function queryBidLotByProjectIdApi(projectId: number | string) {
  return requestClient.get<SystemBidLotApi.LotItem[]>(
    `/bid/lot/queryByProjectId/${projectId}`,
  );
}

/** 新增标段 */
export async function addBidLotApi(data: SystemBidLotApi.LotAddParams) {
  return requestClient.post<string>('/bid/lot/add', data);
}

/** 编辑标段 */
export async function updateBidLotApi(data: SystemBidLotApi.LotUpdateParams) {
  return requestClient.post<string>('/bid/lot/update', data);
}

/** 关闭投标 */
export async function closeBidLotApi(data: SystemBidLotApi.LotActionParams) {
  return requestClient.post<string>('/bid/lot/closeBid', data);
}

/** 废止标段 */
export async function voidBidLotApi(data: SystemBidLotApi.LotActionParams) {
  return requestClient.post<string>('/bid/lot/void', data);
}

/** 查询供应商报名分页 */
export async function queryBidRegistrationPageApi(
  data: SystemBidRegistrationApi.RegistrationQueryParams,
) {
  return requestClient.post<
    SystemBidProjectApi.PageResult<SystemBidRegistrationApi.RegistrationItem>
  >('/bid/registration/queryPage', data);
}

/** 查询供应商报名详情 */
export async function getBidRegistrationDetailApi(
  registrationId: number | string,
) {
  return requestClient.get<SystemBidRegistrationApi.RegistrationItem>(
    `/bid/registration/get/${registrationId}`,
  );
}

/** 新增供应商报名 */
export async function addBidRegistrationApi(
  data: SystemBidRegistrationApi.RegistrationAddParams,
) {
  return requestClient.post<string>('/bid/registration/add', data);
}

/** 审核通过供应商报名 */
export async function approveBidRegistrationApi(
  data: SystemBidRegistrationApi.RegistrationActionParams,
) {
  return requestClient.post<string>('/bid/registration/approve', data);
}

/** 驳回供应商报名 */
export async function rejectBidRegistrationApi(
  data: SystemBidRegistrationApi.RegistrationActionParams,
) {
  return requestClient.post<string>('/bid/registration/reject', data);
}

/** 取消供应商报名 */
export async function cancelBidRegistrationApi(
  data: SystemBidRegistrationApi.RegistrationActionParams,
) {
  return requestClient.post<string>('/bid/registration/cancel', data);
}
