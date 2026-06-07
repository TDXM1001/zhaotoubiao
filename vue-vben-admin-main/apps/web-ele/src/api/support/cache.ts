import { requestClient } from '#/api/request';

export namespace SupportCacheApi {
  /** 缓存名称 */
  export type CacheName = string;

  /** 缓存 key */
  export type CacheKey = string;
}

/** 获取后端当前注册的全部缓存名称 */
export async function queryCacheNamesApi() {
  return requestClient.get<SupportCacheApi.CacheName[]>('/support/cache/names');
}

/** 获取指定缓存分组下的全部 key */
export async function queryCacheKeysApi(cacheName: string) {
  return requestClient.get<SupportCacheApi.CacheKey[]>(
    `/support/cache/keys/${encodeURIComponent(cacheName)}`,
  );
}

/** 清空指定缓存分组 */
export async function removeCacheApi(cacheName: string) {
  return requestClient.get<string>(
    `/support/cache/remove/${encodeURIComponent(cacheName)}`,
  );
}
