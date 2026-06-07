import type { UserInfo } from '@vben/types';

import { preferences } from '@vben/preferences';

import { requestClient } from '#/api/request';

import type { AuthApi } from './auth';
import { mapLoginResultToUserInfo } from './smart-admin-auth';

export async function getLoginInfoApi() {
  return requestClient.get<AuthApi.LoginResult>('/login/getLoginInfo');
}

export async function getUserInfoApi(): Promise<UserInfo> {
  const loginResult = await getLoginInfoApi();

  return mapLoginResultToUserInfo(loginResult, {
    defaultAvatar: preferences.app.defaultAvatar,
    defaultHomePath: preferences.app.defaultHomePath,
  });
}
