import { describe, expect, it } from 'vitest';

import {
  ATTACHMENT_FILE_CATEGORY_OPTIONS,
  formatBidFileSize,
  getAttachmentCategoryText,
  isOpeningReadyLot,
  TENDER_VERSION_TYPE_OPTIONS,
} from '../bid-helper';

describe('bid helper options', () => {
  it('keeps tender version types aligned with BID_TENDER_VERSION_TYPE dictionary values', () => {
    expect(TENDER_VERSION_TYPE_OPTIONS.map((item) => item.value)).toEqual([
      'TENDER_MAIN',
      'ANNOUNCEMENT',
      'CLARIFICATION',
      'CORRECTION',
    ]);
  });

  it('keeps attachment categories aligned with BID_ATTACHMENT_FILE_CATEGORY dictionary values', () => {
    expect(ATTACHMENT_FILE_CATEGORY_OPTIONS.map((item) => item.value)).toEqual([
      'TENDER_MAIN',
      'TENDER_ANNOUNCEMENT',
      'TENDER_CLARIFICATION',
      'SUBMISSION_MAIN',
      'SUBMISSION_PRICE',
      'OTHER',
    ]);
    expect(getAttachmentCategoryText('SUBMISSION_PRICE')).toBe('报价文件');
  });

  it('formats bid attachment file sizes for detail tables', () => {
    expect(formatBidFileSize(undefined)).toBe('--');
    expect(formatBidFileSize(512)).toBe('512 B');
    expect(formatBidFileSize(1536)).toBe('1.5 KB');
    expect(formatBidFileSize(2 * 1024 * 1024)).toBe('2 MB');
  });

  it('only allows bid-closed lots to be selected for opening creation', () => {
    expect(isOpeningReadyLot({ status: 'BID_CLOSED' })).toBe(true);
    expect(isOpeningReadyLot({ status: 'BIDDING' })).toBe(false);
    expect(isOpeningReadyLot({ status: 'OPENED' })).toBe(false);
    expect(isOpeningReadyLot({})).toBe(false);
  });
});
