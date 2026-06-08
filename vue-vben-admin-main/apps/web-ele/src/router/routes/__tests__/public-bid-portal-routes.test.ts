import { describe, expect, it } from 'vitest';

import { routes } from '../index';

describe('public bid portal routes', () => {
  it('keeps supplier portal routes out of the admin app', () => {
    const adminRoutesJson = JSON.stringify(routes);

    expect(adminRoutesJson).not.toContain('/bid-portal');
    expect(adminRoutesJson).not.toContain('BidPortal');
  });
});
