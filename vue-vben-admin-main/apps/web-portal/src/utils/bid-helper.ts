const ATTACHMENT_FILE_CATEGORY_MAP: Record<string, string> = {
  OTHER: '其他附件',
  SUBMISSION_MAIN: '投标文件正文',
  SUBMISSION_PRICE: '报价文件',
  TENDER_ANNOUNCEMENT: '招标公告',
  TENDER_CLARIFICATION: '澄清文件',
  TENDER_MAIN: '招标文件正文',
};

const AWARD_STATUS_MAP: Record<string, string> = {
  CANCELLED: '已取消',
  CONFIRMED: '已确认',
  PENDING: '待定标',
  REVIEWING: '确认中',
  ROLLED_BACK: '已回退',
};

const STATUS_TYPE_MAP: Record<
  string,
  'danger' | 'info' | 'primary' | 'success' | 'warning'
> = {
  CANCELLED: 'info',
  CONFIRMED: 'success',
  PENDING: 'info',
  REVIEWING: 'warning',
  ROLLED_BACK: 'danger',
};

/** 提取路由里的数字参数，避免把非法 query 传给后端。 */
export function parseRouteNumber(value: unknown) {
  if (value === null || value === undefined || value === '') {
    return undefined;
  }
  const parsed = Number(value);
  return Number.isNaN(parsed) ? undefined : parsed;
}

/** 解析后端创建接口返回的新记录 ID，非法值不继续传递到下一步。 */
export function parseCreatedRecordId(value: unknown) {
  if (value === null || value === undefined || value === '') {
    return undefined;
  }
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
}

/** 格式化招投标附件大小。 */
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

export function getAttachmentCategoryText(fileCategory?: string) {
  return fileCategory
    ? (ATTACHMENT_FILE_CATEGORY_MAP[fileCategory] ?? fileCategory)
    : '--';
}

export function getAwardStatusText(status?: string) {
  return status ? (AWARD_STATUS_MAP[status] ?? status) : '--';
}

export function getStatusTagType(status?: string) {
  return STATUS_TYPE_MAP[status ?? 'PENDING'] ?? 'info';
}
