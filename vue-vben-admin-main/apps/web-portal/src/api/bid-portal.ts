import {
  clearBidPortalAccessToken,
  setBidPortalAccessToken,
} from './bid-portal-token';
import { portalGet, portalPost } from './request';

export namespace BidPortalApi {
  export interface PageResult<T> {
    emptyFlag?: boolean;
    list: T[];
    pageNum: number;
    pageSize: number;
    pages: number;
    total: number;
  }

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

  export interface PortalAuthRegisterParams {
    contactPhone: string;
    loginName: string;
    password: string;
    supplierCreditCode: string;
  }

  export interface PortalAuthLoginParams {
    loginName: string;
    password: string;
  }

  export interface PortalAuthInfo {
    contactName?: string;
    contactPhone?: string;
    loginName: string;
    portalAccountId: number;
    supplierCreditCode: string;
    supplierEnterpriseId?: null | number;
    supplierName: string;
    token?: string;
  }

  export interface PortalProjectQueryParams {
    keyword?: string;
    pageNum: number;
    pageSize: number;
    searchCount?: boolean;
  }

  export interface PortalLotItem {
    bidEndTime?: string;
    bidStartTime?: string;
    lotCode?: string;
    lotId: number;
    lotName?: string;
    lotScope?: string;
    registrationEndTime?: string;
    registrationStartTime?: string;
    tenderAttachments?: AttachmentItem[];
    tenderSummary?: string;
    tenderVersionId?: null | number;
    tenderVersionNo?: null | number;
  }

  export interface PortalProjectItem {
    budgetAmount?: null | number;
    lotCount?: number;
    lots?: PortalLotItem[];
    procurementMode?: string;
    projectCode?: string;
    projectId: number;
    projectName: string;
    projectType?: string;
    publishTime?: string;
  }

  export interface PortalRegistrationCreateParams {
    contactEmail?: string;
    contactName?: string;
    contactPhone?: string;
    lotId: number;
    projectId: number;
    registrationType: string;
    remark?: string;
  }

  export interface PortalSubmissionCreateParams {
    lotId: number;
    projectId: number;
    registrationId: number;
    remark?: string;
  }

  export interface PortalSubmissionActionParams {
    remark?: string;
    submissionId: number;
    version: number;
  }

  export interface PortalSubmissionSubmitParams
    extends PortalSubmissionActionParams {
    contactName?: string;
    contactPhone?: string;
    fileManifestJson?: string;
    priceAmount?: null | number;
  }

  export interface PortalResultItem {
    awardAmount?: null | number;
    awardStatus?: string;
    lotCode?: string;
    lotId: number;
    lotName?: string;
    message?: string;
    projectId: number;
    projectName?: string;
    publicNoticeTime?: string;
    resultVisible?: boolean;
    winnerFlag?: boolean;
  }
}

async function persistAuth(result: BidPortalApi.PortalAuthInfo) {
  if (result.token) {
    setBidPortalAccessToken(result.token);
  }
  return result;
}

/** 门户账号注册 */
export async function registerBidPortalAccountApi(
  data: BidPortalApi.PortalAuthRegisterParams,
) {
  return persistAuth(
    await portalPost<BidPortalApi.PortalAuthInfo>(
      '/bid/portal/auth/register',
      data,
    ),
  );
}

/** 门户账号登录 */
export async function loginBidPortalAccountApi(
  data: BidPortalApi.PortalAuthLoginParams,
) {
  return persistAuth(
    await portalPost<BidPortalApi.PortalAuthInfo>(
      '/bid/portal/auth/login',
      data,
    ),
  );
}

/** 门户当前供应商 */
export function getBidPortalCurrentSupplierApi() {
  return portalGet<BidPortalApi.PortalAuthInfo>('/bid/portal/auth/me');
}

/** 门户账号退出 */
export async function logoutBidPortalAccountApi() {
  try {
    return await portalPost<string>('/bid/portal/auth/logout');
  } finally {
    clearBidPortalAccessToken();
  }
}

/** 门户查询项目分页 */
export function queryBidPortalProjectPageApi(
  data: BidPortalApi.PortalProjectQueryParams,
) {
  return portalPost<BidPortalApi.PageResult<BidPortalApi.PortalProjectItem>>(
    '/bid/portal/projects/search',
    data,
  );
}

/** 门户查询项目详情 */
export function getBidPortalProjectDetailApi(projectId: number | string) {
  return portalGet<BidPortalApi.PortalProjectItem>(
    `/bid/portal/projects/${projectId}`,
  );
}

/** 门户提交报名 */
export function createBidPortalRegistrationApi(
  data: BidPortalApi.PortalRegistrationCreateParams,
) {
  return portalPost<string>('/bid/portal/registrations', data);
}

/** 门户创建投标主记录 */
export function createBidPortalSubmissionApi(
  data: BidPortalApi.PortalSubmissionCreateParams,
) {
  return portalPost<string>('/bid/portal/submissions', data);
}

/** 门户提交投标 */
export function submitBidPortalSubmissionApi(
  data: BidPortalApi.PortalSubmissionSubmitParams,
) {
  return portalPost<string>(
    `/bid/portal/submissions/${data.submissionId}/actions/submit-bid`,
    data,
  );
}

/** 门户撤回投标 */
export function withdrawBidPortalSubmissionApi(
  data: BidPortalApi.PortalSubmissionActionParams,
) {
  return portalPost<string>(
    `/bid/portal/submissions/${data.submissionId}/actions/withdraw-bid`,
    data,
  );
}

/** 门户查询本人标段结果 */
export function getBidPortalLotResultApi(lotId: number | string) {
  return portalGet<BidPortalApi.PortalResultItem>(
    `/bid/portal/lots/${lotId}/result`,
  );
}
