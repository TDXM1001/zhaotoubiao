import { requestClient } from '#/api/request';

import {
  createSmartAdminLoginParams,
  type SmartAdminLoginResult,
} from './smart-admin-auth';

export namespace AuthApi {
  /** 登录请求参数 */
  export interface LoginParams {
    password?: string;
    username?: string;
  }

  /** 登录接口返回结果 */
  export type LoginResult = SmartAdminLoginResult;

  export interface RefreshTokenResult {
    data: string;
    status: number;
  }
}

/** 调用 SmartAdmin 登录接口。 */
export async function loginApi(data: AuthApi.LoginParams) {
  return requestClient.post<AuthApi.LoginResult>(
    '/login',
    createSmartAdminLoginParams(data),
  );
}

/** 保留原有刷新 token 能力，当前步骤不启用。 */
export async function refreshTokenApi() {
  return requestClient.post<AuthApi.RefreshTokenResult>('/auth/refresh', {
    withCredentials: true,
  });
}

/** 调用 SmartAdmin 退出登录接口。 */
export async function logoutApi() {
  return requestClient.get('/login/logout');
}
