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
  { label: '已作废', tagType: 'danger', value: 'CANCELLED' },
  { label: '已归档', tagType: 'info', value: 'ARCHIVED' },
];

/** 标段状态选项 */
export const LOT_STATUS_OPTIONS: EnumProps[] = [
  { label: '草稿', tagType: 'info', value: 'DRAFT' },
  { label: '投标中', tagType: 'success', value: 'BIDDING' },
  { label: '已截标', tagType: 'warning', value: 'BID_CLOSED' },
  { label: '已废止', tagType: 'danger', value: 'VOIDED' },
];

/** 供应商报名状态选项 */
export const REGISTRATION_STATUS_OPTIONS: EnumProps[] = [
  { label: '待审核', tagType: 'warning', value: 'SUBMITTED' },
  { label: '已通过', tagType: 'success', value: 'QUALIFIED' },
  { label: '已驳回', tagType: 'danger', value: 'REJECTED' },
  { label: '已取消', tagType: 'info', value: 'CANCELLED' },
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
  PLANNED: 'warning',
  PUBLISHED: 'success',
  QUALIFIED: 'success',
  REJECTED: 'danger',
  SUBMITTED: 'warning',
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
