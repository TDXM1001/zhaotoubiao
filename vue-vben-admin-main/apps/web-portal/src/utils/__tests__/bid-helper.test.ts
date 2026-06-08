import { describe, expect, it } from 'vitest';

import { parseCreatedRecordId } from '../bid-helper';

describe('bid helper', () => {
  it('parses created record id from backend payload', () => {
    expect(parseCreatedRecordId('12')).toBe(12);
    expect(parseCreatedRecordId(13)).toBe(13);
    expect(parseCreatedRecordId('')).toBeUndefined();
    expect(parseCreatedRecordId('abc')).toBeUndefined();
    expect(parseCreatedRecordId(0)).toBeUndefined();
  });
});
