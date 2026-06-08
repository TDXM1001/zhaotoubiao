const BID_PORTAL_ACCESS_TOKEN_KEY = 'bid_portal_access_token';

function getStorage() {
  return typeof window === 'undefined' ? undefined : window.localStorage;
}

export function getBidPortalAccessToken() {
  return getStorage()?.getItem(BID_PORTAL_ACCESS_TOKEN_KEY) ?? null;
}

export function setBidPortalAccessToken(token: string) {
  getStorage()?.setItem(BID_PORTAL_ACCESS_TOKEN_KEY, token);
}

export function clearBidPortalAccessToken() {
  getStorage()?.removeItem(BID_PORTAL_ACCESS_TOKEN_KEY);
}
