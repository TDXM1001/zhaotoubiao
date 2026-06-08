import type { SystemDepartmentApi } from '#/api';
import type { EnumProps } from '#/components/pro-table/types';

/** 部门树选项 */
export interface DepartmentTreeOption {
  children?: DepartmentTreeOption[];
  label: string;
  value: number;
}

/** 招标项目状态选项 */
export const PROJECT_STATUS_OPTIONS: EnumProps[] = [
  { label: '草稿', tagType: 'info', value: 'DRAFT' },
  { label: '已提交计划', tagType: 'warning', value: 'PLANNED' },
  { label: '已发布', tagType: 'success', value: 'PUBLISHED' },
  { label: '开标中', tagType: 'warning', value: 'OPENING_IN_PROGRESS' },
  { label: '评标中', tagType: 'warning', value: 'EVALUATING' },
  { label: '已定标', tagType: 'success', value: 'AWARDED' },
  { label: '已作废', tagType: 'danger', value: 'CANCELLED' },
  { label: '已归档', tagType: 'info', value: 'ARCHIVED' },
];

/** 标段状态选项 */
export const LOT_STATUS_OPTIONS: EnumProps[] = [
  { label: '草稿', tagType: 'info', value: 'DRAFT' },
  { label: '投标中', tagType: 'success', value: 'BIDDING' },
  { label: '已截标', tagType: 'warning', value: 'BID_CLOSED' },
  { label: '已开标', tagType: 'primary', value: 'OPENED' },
  { label: '评标中', tagType: 'warning', value: 'EVALUATING' },
  { label: '已定标', tagType: 'success', value: 'AWARDED' },
  { label: '已废止', tagType: 'danger', value: 'VOIDED' },
];

/** 供应商报名状态选项 */
export const REGISTRATION_STATUS_OPTIONS: EnumProps[] = [
  { label: '待审核', tagType: 'warning', value: 'SUBMITTED' },
  { label: '已通过', tagType: 'success', value: 'QUALIFIED' },
  { label: '已驳回', tagType: 'danger', value: 'REJECTED' },
  { label: '已取消', tagType: 'info', value: 'CANCELLED' },
];

/** 招标文件状态选项 */
export const TENDER_STATUS_OPTIONS: EnumProps[] = [
  { label: '草稿', tagType: 'info', value: 'DRAFT' },
  { label: '当前有效', tagType: 'success', value: 'ACTIVE' },
  { label: '已替代', tagType: 'warning', value: 'SUPERSEDED' },
  { label: '已撤回', tagType: 'danger', value: 'WITHDRAWN' },
];

/** 投标状态选项 */
export const SUBMISSION_STATUS_OPTIONS: EnumProps[] = [
  { label: '资格通过', tagType: 'success', value: 'QUALIFIED' },
  { label: '已投标', tagType: 'warning', value: 'SUBMITTED' },
  { label: '已撤回', tagType: 'info', value: 'WITHDRAWN' },
  { label: '已开标', tagType: 'primary', value: 'OPENED' },
];

/** 开标状态选项 */
export const OPENING_STATUS_OPTIONS: EnumProps[] = [
  { label: '待开标', tagType: 'info', value: 'PENDING' },
  { label: '开标中', tagType: 'warning', value: 'IN_PROGRESS' },
  { label: '已完成', tagType: 'success', value: 'COMPLETED' },
  { label: '异常关闭', tagType: 'danger', value: 'ABNORMAL_CLOSED' },
];

/** 评标状态选项 */
export const EVALUATION_STATUS_OPTIONS: EnumProps[] = [
  { label: '待评标', tagType: 'info', value: 'PENDING' },
  { label: '评分中', tagType: 'warning', value: 'SCORING' },
  { label: '汇总中', tagType: 'warning', value: 'SUMMARIZING' },
  { label: '已定稿', tagType: 'success', value: 'FINALIZED' },
  { label: '已回退', tagType: 'danger', value: 'ROLLED_BACK' },
];

/** 定标状态选项 */
export const AWARD_STATUS_OPTIONS: EnumProps[] = [
  { label: '待定标', tagType: 'info', value: 'PENDING' },
  { label: '确认中', tagType: 'warning', value: 'REVIEWING' },
  { label: '已确认', tagType: 'success', value: 'CONFIRMED' },
  { label: '已回退', tagType: 'danger', value: 'ROLLED_BACK' },
  { label: '已取消', tagType: 'info', value: 'CANCELLED' },
];

/** 招标文件版本类型选项 */
export const TENDER_VERSION_TYPE_OPTIONS: EnumProps[] = [
  { label: '招标文件', value: 'TENDER_MAIN' },
  { label: '公告', value: 'ANNOUNCEMENT' },
  { label: '澄清文件', value: 'CLARIFICATION' },
  { label: '更正文件', value: 'CORRECTION' },
];

/** 招投标附件分类选项 */
export const ATTACHMENT_FILE_CATEGORY_OPTIONS: EnumProps[] = [
  { label: '招标文件正文', value: 'TENDER_MAIN' },
  { label: '招标公告', value: 'TENDER_ANNOUNCEMENT' },
  { label: '澄清文件', value: 'TENDER_CLARIFICATION' },
  { label: '投标文件正文', value: 'SUBMISSION_MAIN' },
  { label: '报价文件', value: 'SUBMISSION_PRICE' },
  { label: '其他附件', value: 'OTHER' },
];

const STATUS_TYPE_MAP: Record<
  string,
  'danger' | 'info' | 'primary' | 'success' | 'warning'
> = {
  ARCHIVED: 'info',
  BID_CLOSED: 'warning',
  BIDDING: 'success',
  CANCELLED: 'danger',
  DRAFT: 'info',
  ACTIVE: 'success',
  ABNORMAL_CLOSED: 'danger',
  AWARDED: 'success',
  PLANNED: 'warning',
  PUBLISHED: 'success',
  COMPLETED: 'success',
  CONFIRMED: 'success',
  EVALUATING: 'warning',
  FINALIZED: 'success',
  IN_PROGRESS: 'warning',
  OPENING_IN_PROGRESS: 'warning',
  PENDING: 'info',
  QUALIFIED: 'success',
  REJECTED: 'danger',
  REVIEWING: 'warning',
  ROLLED_BACK: 'danger',
  SCORING: 'warning',
  SUBMITTED: 'warning',
  SUMMARIZING: 'warning',
  SUPERSEDED: 'warning',
  WITHDRAWN: 'info',
  OPENED: 'primary',
  VOIDED: 'danger',
};

const REGISTRATION_STATUS_TYPE_MAP: Record<
  string,
  'danger' | 'info' | 'primary' | 'success' | 'warning'
> = {
  CANCELLED: 'info',
  QUALIFIED: 'success',
  REJECTED: 'danger',
  SUBMITTED: 'warning',
};

function getLabelFromOptions(options: EnumProps[], value?: string) {
  if (!value) {
    return '--';
  }
  return options.find((item) => item.value === value)?.label ?? value;
}

/** 判断招投标动作是否同时具备前端权限码和后端允许动作 */
export function hasBidAction(options: {
  accessCode: string;
  accessCodes?: string[];
  allowedAction: string;
  allowedActions?: string[];
}) {
  return (
    (options.accessCodes?.includes(options.accessCode) ?? false)
    && (options.allowedActions?.includes(options.allowedAction) ?? false)
  );
}

/** 获取项目状态文本 */
export function getProjectStatusText(status?: string) {
  return getLabelFromOptions(PROJECT_STATUS_OPTIONS, status);
}

/** 获取标段状态文本 */
export function getLotStatusText(status?: string) {
  return getLabelFromOptions(LOT_STATUS_OPTIONS, status);
}

/** 获取供应商报名状态文本 */
export function getRegistrationStatusText(status?: string) {
  return getLabelFromOptions(REGISTRATION_STATUS_OPTIONS, status);
}

/** 获取招标文件状态文本 */
export function getTenderStatusText(status?: string) {
  return getLabelFromOptions(TENDER_STATUS_OPTIONS, status);
}

/** 获取投标状态文本 */
export function getSubmissionStatusText(status?: string) {
  return getLabelFromOptions(SUBMISSION_STATUS_OPTIONS, status);
}

/** 获取开标状态文本 */
export function getOpeningStatusText(status?: string) {
  return getLabelFromOptions(OPENING_STATUS_OPTIONS, status);
}

/** 获取评标状态文本 */
export function getEvaluationStatusText(status?: string) {
  return getLabelFromOptions(EVALUATION_STATUS_OPTIONS, status);
}

/** 获取定标状态文本 */
export function getAwardStatusText(status?: string) {
  return getLabelFromOptions(AWARD_STATUS_OPTIONS, status);
}

/** 获取招标文件版本类型文本 */
export function getTenderVersionTypeText(versionType?: string) {
  return getLabelFromOptions(TENDER_VERSION_TYPE_OPTIONS, versionType);
}

/** 获取附件分类文本 */
export function getAttachmentCategoryText(fileCategory?: string) {
  return getLabelFromOptions(ATTACHMENT_FILE_CATEGORY_OPTIONS, fileCategory);
}

/** 获取供应商报名状态标签样式 */
export function getRegistrationStatusTagType(status?: string) {
  return REGISTRATION_STATUS_TYPE_MAP[status ?? 'SUBMITTED'] ?? 'info';
}

/** 获取状态标签样式 */
export function getStatusTagType(status?: string) {
  return STATUS_TYPE_MAP[status ?? 'DRAFT'] ?? 'info';
}

/** 转换部门树为 TreeSelect 结构 */
export function transformDepartmentTree(
  items: SystemDepartmentApi.DepartmentOption[] = [],
): DepartmentTreeOption[] {
  return items.map((item) => ({
    children: transformDepartmentTree(item.children ?? []),
    label: item.departmentName,
    value: item.departmentId,
  }));
}

/** 提取路由里的数字参数 */
export function parseRouteNumber(value: unknown) {
  if (value === null || value === undefined || value === '') {
    return undefined;
  }
  const parsed = Number(value);
  return Number.isNaN(parsed) ? undefined : parsed;
}

/** 判断标段是否已进入可创建开标安排的状态 */
export function isOpeningReadyLot(lot?: { status?: string }) {
  return lot?.status === 'BID_CLOSED';
}

/** 判断标段是否已进入可创建评标记录的状态 */
export function isEvaluationReadyLot(lot?: { status?: string }) {
  return lot?.status === 'OPENED';
}

/** 判断评标记录是否已进入可创建定标记录的状态 */
export function isAwardReadyEvaluation(evaluation?: { status?: string }) {
  return evaluation?.status === 'FINALIZED';
}

/** 判断投标记录是否可作为定标候选 */
export function isAwardCandidateSubmission(submission?: { status?: string }) {
  return submission?.status === 'OPENED';
}

/** 格式化招投标附件大小 */
export function formatBidFileSize(fileSize?: null | number) {
  if (fileSize === null || fileSize === undefined) {
    return '--';
  }
  if (fileSize < 1024) {
    return `${fileSize} B`;
  }
  if (fileSize < 1024 * 1024) {
    return `${Number.parseFloat((fileSize / 1024).toFixed(1))} KB`;
  }
  return `${Number.parseFloat((fileSize / 1024 / 1024).toFixed(1))} MB`;
}
