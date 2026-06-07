import type { UserInfo } from '@vben/types';

import { encryptSmartAdminPassword } from './smart-admin-encrypt';
import {
  findSmartAdminHomePath,
  type SmartAdminMenuItem,
} from './smart-admin-menu';

/** SmartAdmin 登录请求参数。 */
interface SmartAdminLoginParams {
  loginDevice: number;
  loginName: string;
  password?: string;
}

/** SmartAdmin 登录返回结果。 */
interface SmartAdminLoginResult {
  actualName?: string;
  avatar?: string;
  departmentName?: string;
  employeeId: number | string;
  loginName?: string;
  menuList?: SmartAdminMenuItem[];
  token: string;
}

// 将 Vben 登录表单转换为 SmartAdmin 登录请求参数。
function createSmartAdminLoginParams(params: {
  password?: string;
  username?: string;
}): SmartAdminLoginParams {
  return {
    loginDevice: 1,
    loginName: params.username ?? '',
    // SmartAdmin 登录接口要求密码字段先做 SM4 加密，再按 Base64 传输。
    password: params.password
      ? encryptSmartAdminPassword(params.password)
      : undefined,
  };
}

// 将 SmartAdmin 登录结果映射为 Vben 用户信息。
function mapLoginResultToUserInfo(
  data: SmartAdminLoginResult,
  options: {
    defaultAvatar: string;
    defaultHomePath: string;
  },
): UserInfo {
  return {
    avatar: data.avatar || options.defaultAvatar,
    desc: data.departmentName ?? '',
    homePath: findSmartAdminHomePath(
      data.menuList ?? [],
      options.defaultHomePath,
    ),
    realName: data.actualName ?? data.loginName ?? '',
    roles: [],
    token: data.token,
    userId: String(data.employeeId),
    username: data.loginName ?? '',
  };
}

export type { SmartAdminLoginParams, SmartAdminLoginResult };
export { createSmartAdminLoginParams, mapLoginResultToUserInfo };
