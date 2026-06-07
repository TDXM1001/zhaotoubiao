<script lang="ts" setup>
import type { SupportCacheApi } from '#/api';

import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { Copy, Eraser, RotateCw, Search } from '@vben/icons';
import { useAccessStore } from '@vben/stores';

import {
  ElAlert,
  ElButton,
  ElCard,
  ElEmpty,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElScrollbar,
  ElSkeleton,
  ElSpace,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {
  queryCacheKeysApi,
  queryCacheNamesApi,
  removeCacheApi,
} from '#/api';

defineOptions({
  name: 'SupportCacheList',
});

interface CacheKeyRow {
  cacheKey: SupportCacheApi.CacheKey;
  rowKey: string;
}

const accessStore = useAccessStore();

const cacheNameKeyword = ref('');
const keyKeyword = ref('');
const cacheNames = ref<SupportCacheApi.CacheName[]>([]);
const cacheKeys = ref<SupportCacheApi.CacheKey[]>([]);
const selectedCacheName = ref('');
const cacheNameLoading = ref(false);
const keyLoading = ref(false);

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('support:cache:keys');
});

const hasDeletePermission = computed(() => {
  return accessStore.accessCodes.includes('support:cache:delete');
});

const filteredCacheNames = computed(() => {
  const keyword = cacheNameKeyword.value.trim().toLowerCase();
  if (!keyword) {
    return cacheNames.value;
  }

  return cacheNames.value.filter((cacheName) =>
    cacheName.toLowerCase().includes(keyword),
  );
});

const cacheKeyRows = computed<CacheKeyRow[]>(() => {
  return cacheKeys.value.map((cacheKey, index) => ({
    cacheKey,
    rowKey: `${selectedCacheName.value}-${index}-${cacheKey}`,
  }));
});

const filteredCacheKeyRows = computed(() => {
  const keyword = keyKeyword.value.trim().toLowerCase();
  if (!keyword) {
    return cacheKeyRows.value;
  }

  return cacheKeyRows.value.filter((item) =>
    item.cacheKey.toLowerCase().includes(keyword),
  );
});

function sortStringList(list: string[]) {
  return list.toSorted((left, right) => left.localeCompare(right));
}

function warnNoQueryPermission() {
  ElMessage.warning('当前账号没有获取缓存 key 权限');
}

async function loadCacheKeys(cacheName: string) {
  if (!hasQueryPermission.value) {
    warnNoQueryPermission();
    return;
  }

  keyLoading.value = true;
  try {
    cacheKeys.value = sortStringList(await queryCacheKeysApi(cacheName));
  } finally {
    keyLoading.value = false;
  }
}

async function refreshCacheNames() {
  if (!hasQueryPermission.value) {
    cacheNames.value = [];
    cacheKeys.value = [];
    selectedCacheName.value = '';
    return;
  }

  cacheNameLoading.value = true;
  try {
    const names = sortStringList(await queryCacheNamesApi());
    cacheNames.value = names;

    // 后端清理缓存后缓存分组通常仍存在，这里优先保持当前选中项。
    const nextCacheName = names.includes(selectedCacheName.value)
      ? selectedCacheName.value
      : (names[0] ?? '');

    selectedCacheName.value = nextCacheName;
    if (nextCacheName) {
      await loadCacheKeys(nextCacheName);
    } else {
      cacheKeys.value = [];
    }
  } finally {
    cacheNameLoading.value = false;
  }
}

async function handleSelectCache(cacheName: string) {
  if (selectedCacheName.value === cacheName) {
    return;
  }

  selectedCacheName.value = cacheName;
  keyKeyword.value = '';
  await loadCacheKeys(cacheName);
}

async function handleRefreshCacheNames() {
  await refreshCacheNames();
}

async function handleRefreshKeys() {
  if (!selectedCacheName.value) {
    ElMessage.warning('请先选择缓存分组');
    return;
  }

  await loadCacheKeys(selectedCacheName.value);
}

async function handleCopyKey(cacheKey: string) {
  if (!navigator.clipboard?.writeText) {
    ElMessage.warning('当前浏览器不支持复制');
    return;
  }

  await navigator.clipboard.writeText(cacheKey);
  ElMessage.success('缓存 key 已复制');
}

async function handleRemoveCache() {
  if (!hasDeletePermission.value) {
    ElMessage.warning('当前账号没有清除缓存权限');
    return;
  }

  if (!selectedCacheName.value) {
    ElMessage.warning('请先选择缓存分组');
    return;
  }

  await ElMessageBox.confirm(
    `确定要清除缓存「${selectedCacheName.value}」下的所有 key 吗？`,
    '清除缓存确认',
    {
      type: 'warning',
    },
  );

  await removeCacheApi(selectedCacheName.value);
  ElMessage.success('缓存清除成功');
  await refreshCacheNames();
}

onMounted(() => {
  void refreshCacheNames();
});
</script>

<template>
  <Page auto-content-height class="cache-page">
    <div class="cache-page__content">
      <ElAlert
        v-if="!hasQueryPermission"
        :closable="false"
        class="cache-page__alert"
        show-icon
        title="当前账号没有缓存管理查询权限"
        type="warning"
      />

      <div class="cache-page__layout">
        <ElCard class="cache-page__aside" shadow="never">
          <template #header>
            <div class="cache-page__card-header">
              <div class="cache-page__title">
                <span>缓存分组</span>
                <ElTag round size="small">{{ cacheNames.length }}</ElTag>
              </div>
              <ElButton
                :disabled="!hasQueryPermission"
                :icon="RotateCw"
                :loading="cacheNameLoading"
                circle
                @click="handleRefreshCacheNames"
              />
            </div>
          </template>

          <ElInput
            v-model="cacheNameKeyword"
            :disabled="!hasQueryPermission"
            :prefix-icon="Search"
            clearable
            placeholder="搜索缓存分组"
          />

          <ElSkeleton
            v-if="cacheNameLoading"
            :rows="8"
            animated
            class="cache-page__skeleton"
          />
          <ElEmpty
            v-else-if="filteredCacheNames.length === 0"
            description="暂无缓存分组"
          />
          <ElScrollbar v-else class="cache-page__cache-scroll">
            <button
              v-for="cacheName in filteredCacheNames"
              :key="cacheName"
              class="cache-page__cache-item" :class="[
                { 'is-active': selectedCacheName === cacheName },
              ]"
              type="button"
              @click="handleSelectCache(cacheName)"
            >
              <span class="cache-page__cache-name">{{ cacheName }}</span>
            </button>
          </ElScrollbar>
        </ElCard>

        <ElCard class="cache-page__main" shadow="never">
          <template #header>
            <div class="cache-page__card-header">
              <div class="cache-page__title">
                <span>{{ selectedCacheName || '缓存 key' }}</span>
                <ElTag v-if="selectedCacheName" round size="small">
                  {{ cacheKeys.length }} 个 key
                </ElTag>
              </div>

              <ElSpace wrap>
                <ElButton
                  :disabled="!selectedCacheName"
                  :icon="RotateCw"
                  :loading="keyLoading"
                  @click="handleRefreshKeys"
                >
                  刷新 key
                </ElButton>
                <ElButton
                  v-if="hasDeletePermission"
                  :disabled="!selectedCacheName"
                  :icon="Eraser"
                  type="danger"
                  @click="handleRemoveCache"
                >
                  清除缓存
                </ElButton>
              </ElSpace>
            </div>
          </template>

          <div class="cache-page__table-tools">
            <ElInput
              v-model="keyKeyword"
              :disabled="!selectedCacheName"
              :prefix-icon="Search"
              clearable
              placeholder="搜索缓存 key"
            />
          </div>

          <ElTable
            v-loading="keyLoading"
            :data="filteredCacheKeyRows"
            :empty-text="selectedCacheName ? '暂无缓存 key' : '请先选择缓存分组'"
            border
            class="cache-page__table"
            height="100%"
            row-key="rowKey"
          >
            <ElTableColumn
              align="center"
              label="序号"
              type="index"
              width="80"
            />
            <ElTableColumn
              label="缓存 key"
              min-width="320"
              prop="cacheKey"
              show-overflow-tooltip
            />
            <ElTableColumn align="center" fixed="right" label="操作" width="120">
              <template #default="{ row }">
                <ElButton
                  :icon="Copy"
                  link
                  type="primary"
                  @click="handleCopyKey(row.cacheKey)"
                >
                  复制
                </ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElCard>
      </div>
    </div>
  </Page>
</template>

<style scoped>
.cache-page__content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
}

.cache-page__alert {
  flex: none;
}

.cache-page__layout {
  display: grid;
  flex: 1;
  grid-template-columns: minmax(260px, 320px) minmax(0, 1fr);
  gap: 16px;
  min-height: 0;
}

.cache-page__aside,
.cache-page__main {
  display: flex;
  flex-direction: column;
  min-height: 0;
  border: none;
}

.cache-page__aside :deep(.el-card__body),
.cache-page__main :deep(.el-card__body) {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.cache-page__card-header {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

.cache-page__title {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  min-width: 0;
  font-weight: 600;
}

.cache-page__title span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cache-page__skeleton {
  padding: 8px 0;
}

.cache-page__cache-scroll {
  flex: 1;
  min-height: 0;
}

.cache-page__cache-item {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: 40px;
  padding: 8px 12px;
  margin-bottom: 6px;
  color: var(--el-text-color-primary);
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease,
    color 0.2s ease;
}

.cache-page__cache-item:hover,
.cache-page__cache-item.is-active {
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary-light-5);
}

.cache-page__cache-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cache-page__table-tools {
  display: flex;
  justify-content: flex-end;
}

.cache-page__table-tools .el-input {
  width: min(100%, 360px);
}

.cache-page__table {
  flex: 1;
  min-height: 0;
}

@media (max-width: 960px) {
  .cache-page__layout {
    grid-template-columns: 1fr;
  }

  .cache-page__aside {
    min-height: 320px;
  }
}

@media (max-width: 640px) {
  .cache-page__card-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
