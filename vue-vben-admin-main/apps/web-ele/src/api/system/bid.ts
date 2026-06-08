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

export namespace SystemBidTenderApi {
  /** 招投标附件摘要 */
  export interface AttachmentItem {
    attachmentId: number;
    businessId: number;
    businessType: string;
    downloadUrl?: string;
    fileCategory: string;
    fileId: number;
    fileName?: string;
    fileSize?: null | number;
    mainFlag?: boolean;
    previewUrl?: string;
    uploaderName?: string;
    uploadTime?: string;
    versionNo?: null | number;
  }

  /** 招投标附件关联创建参数 */
  export interface AttachmentCreateParams {
    fileCategory: string;
    fileId: number;
    mainFlag?: boolean;
    sortNo?: number;
    versionNo?: null | number;
  }

  /** 招标文件分页查询参数 */
  export interface TenderQueryParams {
    keyword?: string;
    lotId?: number;
    pageNum: number;
    pageSize: number;
    projectId?: number;
    searchCount?: boolean;
    status?: string;
    versionType?: string;
  }

  /** 招标文件动作参数 */
  export interface TenderActionParams {
    remark?: string;
    tenderVersionId: number;
    version: number;
  }

  /** 招标文件列表项 */
  export interface TenderItem {
    allowedActions?: string[];
    attachments?: AttachmentItem[];
    createTime?: string;
    currentFlag?: boolean;
    effectiveTime?: string;
    lotCode?: string;
    lotId: number;
    lotName?: string;
    parentVersionId?: null | number;
    projectId: number;
    projectName?: string;
    publishTime?: string;
    remark?: string;
    status: string;
    summary?: string;
    tenderVersionId: number;
    updateTime?: string;
    version: number;
    versionNo: number;
    versionType: string;
  }

  /** 招标文件新增参数 */
  export interface TenderCreateParams {
    lotId: number;
    projectId: number;
    remark?: string;
    summary?: string;
    versionType: string;
  }

  /** 招标文件编辑参数 */
  export interface TenderUpdateParams extends TenderCreateParams {
    tenderVersionId: number;
    version: number;
  }
}

export namespace SystemBidSubmissionApi {
  export type AttachmentItem = SystemBidTenderApi.AttachmentItem;
  export type AttachmentCreateParams =
    SystemBidTenderApi.AttachmentCreateParams;

  /** 投标分页查询参数 */
  export interface SubmissionQueryParams {
    keyword?: string;
    lotId?: number;
    pageNum: number;
    pageSize: number;
    projectId?: number;
    registrationId?: number;
    searchCount?: boolean;
    status?: string;
    supplierEnterpriseId?: number;
  }

  /** 投标动作参数 */
  export interface SubmissionActionParams {
    remark?: string;
    submissionId: number;
    version: number;
  }

  /** 投标提交参数 */
  export interface SubmissionSubmitParams extends SubmissionActionParams {
    contactName?: string;
    contactPhone?: string;
    fileManifestJson?: string;
    priceAmount?: null | number;
  }

  /** 投标创建参数 */
  export interface SubmissionCreateParams {
    lotId: number;
    projectId: number;
    registrationId: number;
    remark?: string;
  }

  /** 投标列表项 */
  export interface SubmissionItem {
    allowedActions?: string[];
    attachments?: AttachmentItem[];
    contactName?: string;
    contactPhone?: string;
    createTime?: string;
    fileManifestJson?: string;
    latestSubmitTime?: string;
    latestSubmissionVersionId?: null | number;
    latestVersionNo: number;
    lotCode?: string;
    lotId: number;
    lotName?: string;
    priceAmount?: null | number;
    projectId: number;
    projectName?: string;
    receiptNo?: string;
    registrationId: number;
    remark?: string;
    status: string;
    submissionId: number;
    supplierCreditCode: string;
    supplierEnterpriseId?: null | number;
    supplierNameSnapshot: string;
    updateTime?: string;
    version: number;
    withdrawReason?: string;
    withdrawTime?: string;
  }
}

export namespace SystemBidOpeningApi {
  /** 开标分页查询参数 */
  export interface OpeningQueryParams {
    keyword?: string;
    lotId?: number;
    pageNum: number;
    pageSize: number;
    projectId?: number;
    searchCount?: boolean;
    status?: string;
  }

  /** 开标动作参数 */
  export interface OpeningActionParams {
    openingId: number;
    remark?: string;
    version: number;
  }

  /** 开标创建参数 */
  export interface OpeningCreateParams {
    hostEmployeeId?: null | number;
    lotId: number;
    openingPlace?: string;
    openingTime?: string;
    projectId: number;
    recorderEmployeeId?: null | number;
    summary?: string;
  }

  /** 开标明细 */
  export interface OpeningItemDetail {
    createTime?: string;
    documentCheckResult?: string;
    openComment?: string;
    openingId: number;
    openingItemId: number;
    quotedPrice?: null | number;
    sortNo?: number;
    submissionId: number;
    submissionVersionId: number;
    supplierCreditCode: string;
    supplierEnterpriseId?: null | number;
    supplierNameSnapshot: string;
  }

  /** 开标列表项 */
  export interface OpeningItem {
    abnormalFlag?: boolean;
    abnormalReason?: string;
    allowedActions?: string[];
    createTime?: string;
    hostEmployeeId?: null | number;
    itemList?: OpeningItemDetail[];
    lotCode?: string;
    lotId: number;
    lotName?: string;
    openingId: number;
    openingPlace?: string;
    openingTime?: string;
    projectId: number;
    projectName?: string;
    recorderEmployeeId?: null | number;
    status: string;
    summary?: string;
    updateTime?: string;
    version: number;
  }
}

export namespace SystemBidEvaluationApi {
  /** 评标分页查询参数 */
  export interface EvaluationQueryParams {
    keyword?: string;
    lotId?: number;
    openingId?: number;
    pageNum: number;
    pageSize: number;
    projectId?: number;
    searchCount?: boolean;
    status?: string;
  }

  /** 评标动作参数 */
  export interface EvaluationActionParams {
    evaluationId: number;
    remark?: string;
    version: number;
  }

  /** 评标创建参数 */
  export interface EvaluationCreateParams {
    evaluationMode?: string;
    finalSummary?: string;
    lotId: number;
    openingId?: null | number;
    projectId: number;
  }

  /** 评标明细 */
  export interface EvaluationItemDetail {
    createTime?: string;
    evaluationComment?: string;
    evaluationId: number;
    evaluationItemId: number;
    openingItemId: number;
    quotedPrice?: null | number;
    rankingNo?: null | number;
    recommendFlag?: boolean;
    submissionId: number;
    supplierNameSnapshot: string;
    totalScore?: null | number;
  }

  /** 评标列表项 */
  export interface EvaluationItem {
    allowedActions?: string[];
    createTime?: string;
    evaluationId: number;
    evaluationMode?: string;
    finalSummary?: string;
    finalizeTime?: string;
    itemList?: EvaluationItemDetail[];
    lotCode?: string;
    lotId: number;
    lotName?: string;
    openingId: number;
    projectId: number;
    projectName?: string;
    rollbackReason?: string;
    startTime?: string;
    status: string;
    updateTime?: string;
    version: number;
  }
}

export namespace SystemBidAwardApi {
  /** 定标分页查询参数 */
  export interface AwardQueryParams {
    evaluationId?: number;
    keyword?: string;
    lotId?: number;
    pageNum: number;
    pageSize: number;
    projectId?: number;
    searchCount?: boolean;
    status?: string;
  }

  /** 定标动作参数 */
  export interface AwardActionParams {
    awardId: number;
    remark?: string;
    version: number;
  }

  /** 定标创建参数 */
  export interface AwardCreateParams {
    awardAmount?: null | number;
    evaluationId: number;
    lotId: number;
    projectId: number;
    remark?: string;
    winningSubmissionId: number;
  }

  /** 定标列表项 */
  export interface AwardItem {
    allowedActions?: string[];
    awardAmount?: null | number;
    awardId: number;
    confirmTime?: string;
    confirmUserId?: null | number;
    createTime?: string;
    evaluationId: number;
    lotCode?: string;
    lotId: number;
    lotName?: string;
    projectId: number;
    projectName?: string;
    publicNoticeTime?: string;
    remark?: string;
    rollbackReason?: string;
    status: string;
    updateTime?: string;
    version: number;
    winnerCreditCode: string;
    winnerEnterpriseId?: null | number;
    winnerNameSnapshot: string;
    winningSubmissionId: number;
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

/** 查询招标文件分页 */
export async function queryBidTenderPageApi(
  data: SystemBidTenderApi.TenderQueryParams,
) {
  return requestClient.post<
    SystemBidProjectApi.PageResult<SystemBidTenderApi.TenderItem>
  >('/bid/tenders/search', data);
}

/** 查询招标文件详情 */
export async function getBidTenderDetailApi(tenderVersionId: number | string) {
  return requestClient.get<SystemBidTenderApi.TenderItem>(
    `/bid/tenders/${tenderVersionId}`,
  );
}

/** 查询标段当前有效招标文件 */
export async function getActiveBidTenderByLotIdApi(lotId: number | string) {
  return requestClient.get<SystemBidTenderApi.TenderItem>(
    `/bid/lots/${lotId}/tenders/active`,
  );
}

/** 新增招标文件 */
export async function addBidTenderApi(
  data: SystemBidTenderApi.TenderCreateParams,
) {
  return requestClient.post<string>('/bid/tenders', data);
}

/** 编辑招标文件 */
export async function updateBidTenderApi(
  data: SystemBidTenderApi.TenderUpdateParams,
) {
  return requestClient.put<string>(
    `/bid/tenders/${data.tenderVersionId}`,
    data,
  );
}

/** 发布招标文件 */
export async function publishBidTenderApi(
  data: SystemBidTenderApi.TenderActionParams,
) {
  return requestClient.post<string>(
    `/bid/tenders/${data.tenderVersionId}/actions/publish-tender`,
    data,
  );
}

/** 发布招标文件澄清 */
export async function clarifyBidTenderApi(
  data: SystemBidTenderApi.TenderActionParams,
) {
  return requestClient.post<string>(
    `/bid/tenders/${data.tenderVersionId}/actions/clarify-tender`,
    data,
  );
}

/** 撤回招标文件 */
export async function withdrawBidTenderApi(
  data: SystemBidTenderApi.TenderActionParams,
) {
  return requestClient.post<string>(
    `/bid/tenders/${data.tenderVersionId}/actions/withdraw-tender`,
    data,
  );
}

/** 关联招标文件附件 */
export async function createBidTenderAttachmentApi(
  tenderVersionId: number | string,
  data: SystemBidTenderApi.AttachmentCreateParams,
) {
  return requestClient.post<string>(
    `/bid/tenders/${tenderVersionId}/attachments`,
    data,
  );
}

/** 查询投标分页 */
export async function queryBidSubmissionPageApi(
  data: SystemBidSubmissionApi.SubmissionQueryParams,
) {
  return requestClient.post<
    SystemBidProjectApi.PageResult<SystemBidSubmissionApi.SubmissionItem>
  >('/bid/submissions/search', data);
}

/** 查询投标详情 */
export async function getBidSubmissionDetailApi(submissionId: number | string) {
  return requestClient.get<SystemBidSubmissionApi.SubmissionItem>(
    `/bid/submissions/${submissionId}`,
  );
}

/** 按报名查询投标 */
export async function getBidSubmissionByRegistrationIdApi(
  registrationId: number | string,
) {
  return requestClient.get<SystemBidSubmissionApi.SubmissionItem>(
    `/bid/registrations/${registrationId}/submission`,
  );
}

/** 新增投标主记录 */
export async function addBidSubmissionApi(
  data: SystemBidSubmissionApi.SubmissionCreateParams,
) {
  return requestClient.post<string>('/bid/submissions', data);
}

/** 提交投标 */
export async function submitBidSubmissionApi(
  data: SystemBidSubmissionApi.SubmissionSubmitParams,
) {
  return requestClient.post<string>(
    `/bid/submissions/${data.submissionId}/actions/submit-bid`,
    data,
  );
}

/** 撤回投标 */
export async function withdrawBidSubmissionApi(
  data: SystemBidSubmissionApi.SubmissionActionParams,
) {
  return requestClient.post<string>(
    `/bid/submissions/${data.submissionId}/actions/withdraw-bid`,
    data,
  );
}

/** 关联投标附件 */
export async function createBidSubmissionAttachmentApi(
  submissionId: number | string,
  data: SystemBidSubmissionApi.AttachmentCreateParams,
) {
  return requestClient.post<string>(
    `/bid/submissions/${submissionId}/attachments`,
    data,
  );
}

/** 查询开标分页 */
export async function queryBidOpeningPageApi(
  data: SystemBidOpeningApi.OpeningQueryParams,
) {
  return requestClient.post<
    SystemBidProjectApi.PageResult<SystemBidOpeningApi.OpeningItem>
  >('/bid/openings/search', data);
}

/** 查询开标详情 */
export async function getBidOpeningDetailApi(openingId: number | string) {
  return requestClient.get<SystemBidOpeningApi.OpeningItem>(
    `/bid/openings/${openingId}`,
  );
}

/** 查询标段开标记录 */
export async function getBidOpeningByLotIdApi(lotId: number | string) {
  return requestClient.get<SystemBidOpeningApi.OpeningItem>(
    `/bid/lots/${lotId}/opening`,
  );
}

/** 新增开标记录 */
export async function addBidOpeningApi(
  data: SystemBidOpeningApi.OpeningCreateParams,
) {
  return requestClient.post<string>('/bid/openings', data);
}

/** 开始开标 */
export async function startBidOpeningApi(
  data: SystemBidOpeningApi.OpeningActionParams,
) {
  return requestClient.post<string>(
    `/bid/openings/${data.openingId}/actions/start-opening`,
    data,
  );
}

/** 完成开标 */
export async function completeBidOpeningApi(
  data: SystemBidOpeningApi.OpeningActionParams,
) {
  return requestClient.post<string>(
    `/bid/openings/${data.openingId}/actions/complete-opening`,
    data,
  );
}

/** 异常关闭开标 */
export async function abnormalCloseBidOpeningApi(
  data: SystemBidOpeningApi.OpeningActionParams,
) {
  return requestClient.post<string>(
    `/bid/openings/${data.openingId}/actions/abnormal-close-opening`,
    data,
  );
}

/** 查询评标分页 */
export async function queryBidEvaluationPageApi(
  data: SystemBidEvaluationApi.EvaluationQueryParams,
) {
  return requestClient.post<
    SystemBidProjectApi.PageResult<SystemBidEvaluationApi.EvaluationItem>
  >('/bid/evaluations/search', data);
}

/** 查询评标详情 */
export async function getBidEvaluationDetailApi(evaluationId: number | string) {
  return requestClient.get<SystemBidEvaluationApi.EvaluationItem>(
    `/bid/evaluations/${evaluationId}`,
  );
}

/** 查询标段评标记录 */
export async function getBidEvaluationByLotIdApi(lotId: number | string) {
  return requestClient.get<SystemBidEvaluationApi.EvaluationItem>(
    `/bid/lots/${lotId}/evaluation`,
  );
}

/** 新增评标记录 */
export async function addBidEvaluationApi(
  data: SystemBidEvaluationApi.EvaluationCreateParams,
) {
  return requestClient.post<string>('/bid/evaluations', data);
}

/** 开始评标 */
export async function startBidEvaluationApi(
  data: SystemBidEvaluationApi.EvaluationActionParams,
) {
  return requestClient.post<string>(
    `/bid/evaluations/${data.evaluationId}/actions/start-evaluation`,
    data,
  );
}

/** 评标定稿 */
export async function finalizeBidEvaluationApi(
  data: SystemBidEvaluationApi.EvaluationActionParams,
) {
  return requestClient.post<string>(
    `/bid/evaluations/${data.evaluationId}/actions/finalize-evaluation`,
    data,
  );
}

/** 回退评标 */
export async function rollbackBidEvaluationApi(
  data: SystemBidEvaluationApi.EvaluationActionParams,
) {
  return requestClient.post<string>(
    `/bid/evaluations/${data.evaluationId}/actions/rollback-evaluation`,
    data,
  );
}

/** 查询定标分页 */
export async function queryBidAwardPageApi(
  data: SystemBidAwardApi.AwardQueryParams,
) {
  return requestClient.post<
    SystemBidProjectApi.PageResult<SystemBidAwardApi.AwardItem>
  >('/bid/awards/search', data);
}

/** 查询定标详情 */
export async function getBidAwardDetailApi(awardId: number | string) {
  return requestClient.get<SystemBidAwardApi.AwardItem>(
    `/bid/awards/${awardId}`,
  );
}

/** 查询标段定标记录 */
export async function getBidAwardByLotIdApi(lotId: number | string) {
  return requestClient.get<SystemBidAwardApi.AwardItem>(
    `/bid/lots/${lotId}/award`,
  );
}

/** 新增定标记录 */
export async function addBidAwardApi(
  data: SystemBidAwardApi.AwardCreateParams,
) {
  return requestClient.post<string>('/bid/awards', data);
}

/** 确认定标 */
export async function confirmBidAwardApi(
  data: SystemBidAwardApi.AwardActionParams,
) {
  return requestClient.post<string>(
    `/bid/awards/${data.awardId}/actions/confirm-award`,
    data,
  );
}

/** 回退定标 */
export async function rollbackBidAwardApi(
  data: SystemBidAwardApi.AwardActionParams,
) {
  return requestClient.post<string>(
    `/bid/awards/${data.awardId}/actions/rollback-award`,
    data,
  );
}

/** 取消定标 */
export async function cancelBidAwardApi(
  data: SystemBidAwardApi.AwardActionParams,
) {
  return requestClient.post<string>(
    `/bid/awards/${data.awardId}/actions/cancel-award`,
    data,
  );
}
