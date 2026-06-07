import type { Recordable, UserInfo } from '@vben/types';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';
import { resetAllStores, useAccessStore, useUserStore } from '@vben/stores';

import { ElNotification } from 'element-plus';
import { defineStore } from 'pinia';

import { getLoginInfoApi, loginApi, logoutApi } from '#/api';
import { mapLoginResultToUserInfo } from '#/api/core/smart-admin-auth';
import { extractSmartAdminAccessCodes } from '#/api/core/smart-admin-menu';
import { $t } from '#/locales';

export const useAuthStore = defineStore('auth', () => {
  const accessStore = useAccessStore();
  const userStore = useUserStore();
  const router = useRouter();

  const loginLoading = ref(false);

  async function authLogin(
    params: Recordable<any>,
    onSuccess?: () => Promise<void> | void,
  ) {
    let userInfo: null | UserInfo = null;

    try {
      loginLoading.value = true;
      const loginResult = await loginApi(params);

      if (loginResult.token) {
        accessStore.setAccessToken(loginResult.token);
        accessStore.setAccessCodes(
          extractSmartAdminAccessCodes(loginResult.menuList ?? []),
        );

        userInfo = mapLoginResultToUserInfo(loginResult, {
          defaultAvatar: preferences.app.defaultAvatar,
          defaultHomePath: preferences.app.defaultHomePath,
        });

        userStore.setUserInfo(userInfo);

        if (accessStore.loginExpired) {
          accessStore.setLoginExpired(false);
        } else if (onSuccess) {
          await onSuccess();
        } else {
          await router.push(
            userInfo.homePath || preferences.app.defaultHomePath,
          );
        }

        if (userInfo.realName) {
          ElNotification({
            message: `${$t('authentication.loginSuccessDesc')}:${userInfo.realName}`,
            title: $t('authentication.loginSuccess'),
            type: 'success',
          });
        }
      }
    } finally {
      loginLoading.value = false;
    }

    return {
      userInfo,
    };
  }

  async function logout(redirect: boolean = true) {
    try {
      await logoutApi();
    } catch {
      // Clear local auth state even if the backend logout call fails.
    }

    resetAllStores();
    accessStore.setLoginExpired(false);

    await router.replace({
      path: LOGIN_PATH,
      query: redirect
        ? {
            redirect: encodeURIComponent(router.currentRoute.value.fullPath),
          }
        : {},
    });
  }

  async function fetchUserInfo() {
    const loginResult = await getLoginInfoApi();
    const userInfo = mapLoginResultToUserInfo(loginResult, {
      defaultAvatar: preferences.app.defaultAvatar,
      defaultHomePath: preferences.app.defaultHomePath,
    });

    userStore.setUserInfo(userInfo);
    accessStore.setAccessCodes(
      extractSmartAdminAccessCodes(loginResult.menuList ?? []),
    );
    return userInfo;
  }

  function $reset() {
    loginLoading.value = false;
  }

  return {
    $reset,
    authLogin,
    fetchUserInfo,
    loginLoading,
    logout,
  };
});
