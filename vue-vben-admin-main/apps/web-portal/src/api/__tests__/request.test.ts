import { describe, expect, it } from 'vitest';

import { isBidPortalApiPath } from '../request';

describe('supplier portal request boundary', () => {
  it('only treats bid portal endpoints as portal API paths', () => {
    expect(isBidPortalApiPath('/bid/portal/projects/search')).toBe(true);
    expect(isBidPortalApiPath('/bid/portal/auth/login')).toBe(true);
    expect(isBidPortalApiPath('/login')).toBe(false);
    expect(isBidPortalApiPath('/bid/project/queryPage')).toBe(false);
  });
});
