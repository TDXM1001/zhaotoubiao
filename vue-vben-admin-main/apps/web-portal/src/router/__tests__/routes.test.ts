import { describe, expect, it } from 'vitest';

import { portalRoutes } from '../routes';

describe('supplier portal routes', () => {
  it('exposes supplier-only route paths without admin system routes', () => {
    const routesJson = JSON.stringify(portalRoutes);

    expect(routesJson).toContain('/bid-portal/project/list');
    expect(routesJson).toContain('/bid-portal/project/detail');
    expect(routesJson).toContain('/bid-portal/submission/form');
    expect(routesJson).toContain('/bid-portal/lots/result');
    expect(routesJson).not.toContain('/system/');
    expect(routesJson).not.toContain('BasicLayout');
  });
});
