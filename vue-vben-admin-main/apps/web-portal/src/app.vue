<script lang="ts" setup>
import { computed } from 'vue';
import { RouterLink, RouterView, useRoute } from 'vue-router';

import {
  ArrowDown,
  Bell,
  OfficeBuilding,
} from '@element-plus/icons-vue';

import supplierPortalLogoUrl from './assets/supplier-portal-logo.png';

const route = useRoute();

const navItems = [
  {
    key: 'project',
    label: '项目大厅',
    match: ['/bid-portal/project/list', '/bid-portal/project/detail'],
    to: '/bid-portal/project/list',
  },
  {
    key: 'submission',
    label: '我的投标',
    match: ['/bid-portal/submission/form'],
    to: '/bid-portal/submission/form',
  },
  {
    key: 'result',
    label: '标段结果',
    match: ['/bid-portal/lots/result'],
    to: '/bid-portal/lots/result',
  },
] as const;

const activeTopNav = computed(
  () =>
    navItems.find((item) =>
      item.match.some((path) => route.path.startsWith(path)),
    )?.key ?? 'project',
);
</script>

<template>
  <div class="portal-shell">
    <header class="portal-shell__header">
      <RouterLink class="portal-shell__brand" to="/bid-portal/project/list">
        <span class="portal-shell__brand-mark" aria-hidden="true">
          <img :src="supplierPortalLogoUrl" alt="" />
        </span>
        <span class="portal-shell__brand-title">供应商门户</span>
      </RouterLink>

      <nav class="portal-shell__nav" aria-label="供应商门户导航">
        <RouterLink
          v-for="item in navItems"
          :key="item.key"
          :aria-current="activeTopNav === item.key ? 'page' : undefined"
          :class="[
            'portal-shell__nav-link',
            { 'is-active': activeTopNav === item.key },
          ]"
          :to="item.to"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="portal-shell__account">
        <div class="portal-shell__user">
          <OfficeBuilding class="portal-shell__company-icon" />
          <span class="portal-shell__user-copy">
            <strong>北京XX建设工程有限公司</strong>
            <ArrowDown class="portal-shell__company-arrow" />
            <small>已认证</small>
          </span>
        </div>
        <button class="portal-shell__icon-button" type="button" aria-label="通知">
          <Bell />
          <span class="portal-shell__badge">5</span>
        </button>
        <div class="portal-shell__user">
          <span class="portal-shell__avatar">张</span>
          <ArrowDown class="portal-shell__arrow" />
        </div>
      </div>
    </header>

    <main class="portal-shell__main">
      <RouterView />
    </main>
  </div>
</template>
