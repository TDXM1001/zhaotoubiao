import { describe, expect, it } from 'vitest';

import {
  ATTACHMENT_FILE_CATEGORY_OPTIONS,
  formatBidFileSize,
  getAttachmentCategoryText,
  hasBidAction,
  isAwardCandidateSubmission,
  isAwardReadyEvaluation,
  isEvaluationReadyLot,
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

  it('requires both access code and allowed action for bid actions', () => {
    expect(
      hasBidAction({
        accessCode: 'bid:tender:update',
        accessCodes: ['bid:tender:update'],
        allowedAction: 'edit-tender',
        allowedActions: ['edit-tender'],
      }),
    ).toBe(true);
    expect(
      hasBidAction({
        accessCode: 'bid:tender:update',
        accessCodes: ['bid:tender:update'],
        allowedAction: 'edit-tender',
        allowedActions: [],
      }),
    ).toBe(false);
    expect(
      hasBidAction({
        accessCode: 'bid:tender:update',
        accessCodes: [],
        allowedAction: 'edit-tender',
        allowedActions: ['edit-tender'],
      }),
    ).toBe(false);
  });

  it('only allows bid-closed lots to be selected for opening creation', () => {
    expect(isOpeningReadyLot({ status: 'BID_CLOSED' })).toBe(true);
    expect(isOpeningReadyLot({ status: 'BIDDING' })).toBe(false);
    expect(isOpeningReadyLot({ status: 'OPENED' })).toBe(false);
    expect(isOpeningReadyLot({})).toBe(false);
  });

  it('only allows opened lots to be selected for evaluation creation', () => {
    expect(isEvaluationReadyLot({ status: 'OPENED' })).toBe(true);
    expect(isEvaluationReadyLot({ status: 'BID_CLOSED' })).toBe(false);
    expect(isEvaluationReadyLot({ status: 'EVALUATING' })).toBe(false);
    expect(isEvaluationReadyLot({})).toBe(false);
  });

  it('only allows finalized evaluations to be selected for award creation', () => {
    expect(isAwardReadyEvaluation({ status: 'FINALIZED' })).toBe(true);
    expect(isAwardReadyEvaluation({ status: 'PENDING' })).toBe(false);
    expect(isAwardReadyEvaluation({ status: 'SCORING' })).toBe(false);
    expect(isAwardReadyEvaluation({})).toBe(false);
  });

  it('only allows opened submissions to be selected as award candidates', () => {
    expect(isAwardCandidateSubmission({ status: 'OPENED' })).toBe(true);
    expect(isAwardCandidateSubmission({ status: 'SUBMITTED' })).toBe(false);
    expect(isAwardCandidateSubmission({ status: 'WITHDRAWN' })).toBe(false);
    expect(isAwardCandidateSubmission({})).toBe(false);
  });
});
