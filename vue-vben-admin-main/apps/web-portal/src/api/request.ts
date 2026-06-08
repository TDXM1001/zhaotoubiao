import { ElMessage } from 'element-plus';

import {
  clearBidPortalAccessToken,
  getBidPortalAccessToken,
} from './bid-portal-token';

interface SmartAdminResponse<T> {
  code?: number;
  data?: T;
  msg?: string;
  message?: string;
}

const apiBaseUrl = import.meta.env.VITE_PORTAL_API_BASE_URL || '/api';

export function isBidPortalApiPath(path: string) {
  return path.startsWith('/bid/portal/');
}

function joinApiUrl(path: string) {
  return `${apiBaseUrl.replace(/\/$/, '')}${path}`;
}

function buildHeaders(headers?: HeadersInit) {
  const mergedHeaders = new Headers(headers);
  const token = getBidPortalAccessToken();

  if (!mergedHeaders.has('Content-Type')) {
    mergedHeaders.set('Content-Type', 'application/json;charset=UTF-8');
  }
  if (token) {
    mergedHeaders.set('Authorization', `Bearer ${token}`);
  }
  mergedHeaders.set('Accept-Language', 'zh-CN');
  return mergedHeaders;
}

export async function portalRequest<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  // 独立门户前端只能调用供应商门户接口，避免误接入后台管理接口。
  if (!isBidPortalApiPath(path)) {
    throw new Error(`供应商门户禁止访问非门户接口: ${path}`);
  }

  const response = await fetch(joinApiUrl(path), {
    ...init,
    headers: buildHeaders(init.headers),
  });
  const payload = (await response
    .json()
    .catch(() => ({}))) as SmartAdminResponse<T>;

  if (!response.ok || payload.code !== 0) {
    if (payload.code === 30_007) {
      clearBidPortalAccessToken();
    }
    const message = payload.msg || payload.message || '请求处理失败';
    ElMessage.error(message);
    throw new Error(message);
  }

  return payload.data as T;
}

export function portalGet<T>(path: string) {
  return portalRequest<T>(path, { method: 'GET' });
}

export function portalPost<T>(path: string, data?: unknown) {
  return portalRequest<T>(path, {
    body: data === undefined ? undefined : JSON.stringify(data),
    method: 'POST',
  });
}
