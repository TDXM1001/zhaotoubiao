<script lang="ts" setup>
import type { SupportProtectApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Delete } from '@element-plus/icons-vue';
import {
  ElButton,
  ElMessage,
  ElMessageBox,
  ElTag,
} from 'element-plus';

import { batchDeleteLoginFailApi, queryLoginFailPageApi } from '#/api';
import ProTable from '#/components/pro-table/index.vue';

defineOptions({
  name: 'SupportLoginFailList',
});

const proTable = ref<ProTableInstance>();

const lockFlagOptions = [
  { label: '已锁定', tagType: 'danger', value: true },
  { label: '未锁定', tagType: 'success', value: false },
];

const tableColumns = reactive<ColumnProps<SupportProtectApi.LoginFailItem>[]>([
  {
    type: 'selection',
    width: 56,
  },
  {
    label: '登录名',
    minWidth: 160,
    prop: 'loginName',
    search: {
      el: 'input',
      label: '登录名',
      props: {
        placeholder: '请输入登录名',
      },
    },
  },
  {
    label: '用户 ID',
    minWidth: 120,
    prop: 'userId',
  },
  {
    label: '用户类型',
    minWidth: 110,
    prop: 'userType',
  },
  {
    label: '失败次数',
    minWidth: 110,
    prop: 'loginFailCount',
  },
  {
    enum: lockFlagOptions,
    label: '锁定状态',
    minWidth: 120,
    prop: 'lockFlag',
    search: {
      el: 'select',
      label: '锁定状态',
    },
  },
  {
    label: '锁定开始时间',
    minWidth: 180,
    prop: 'loginLockBeginTime',
    search: {
      el: 'date-picker',
      key: 'lockDateRange',
      label: '锁定日期',
      props: {
        endPlaceholder: '结束日期',
        startPlaceholder: '开始日期',
        type: 'daterange',
        valueFormat: 'YYYY-MM-DD',
      },
    },
  },
  {
    label: '创建时间',
    minWidth: 180,
    prop: 'createTime',
  },
  {
    label: '更新时间',
    minWidth: 180,
    prop: 'updateTime',
  },
]);

const selectedLoginFailIds = computed<number[]>(() => {
  return ((proTable.value?.selectedListIds ?? []) as Array<number | string>).map(
    Number,
  );
});

const selectedCount = computed(() => selectedLoginFailIds.value.length);

async function queryLoginFailTable(params: Record<string, any>) {
  return queryLoginFailPageApi({
    lockFlag: params.lockFlag,
    loginLockBeginTimeBegin: params.lockDateRange?.[0],
    loginLockBeginTimeEnd: params.lockDateRange?.[1],
    loginName: params.loginName?.trim() || undefined,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    searchCount: true,
  });
}

function clearSelection() {
  proTable.value?.clearSelection();
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

function getLockText(lockFlag?: number) {
  return Number(lockFlag) === 1 ? '已锁定' : '未锁定';
}

function getLockTagType(lockFlag?: number) {
  return Number(lockFlag) === 1 ? 'danger' : 'success';
}

function getUserTypeText(userType?: number) {
  return Number(userType) === 1 ? '员工' : `未知(${userType ?? '--'})`;
}

function handleSearch() {
  clearSelection();
}

function handleReset() {
  clearSelection();
}

async function handleBatchDelete() {
  if (selectedLoginFailIds.value.length === 0) {
    ElMessage.warning('请先选择登录失败记录');
    return;
  }

  await ElMessageBox.confirm(
    `确定要删除选中的 ${selectedLoginFailIds.value.length} 条登录失败记录吗？`,
    '批量删除确认',
    {
      type: 'warning',
    },
  );

  await batchDeleteLoginFailApi(selectedLoginFailIds.value);
  ElMessage.success('登录失败记录删除成功');
  clearSelection();
  await reloadTable();
}
</script>

<template>
  <Page auto-content-height class="login-fail-page">
    <div class="login-fail-page__content">
      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryLoginFailTable"
        row-key="loginFailId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
        @reset="handleReset"
        @search="handleSearch"
      >
          <template #tableHeader>
            <div class="login-fail-page__toolbar">
              <ElButton
                :disabled="selectedCount === 0"
                :icon="Delete"
                type="danger"
                @click="handleBatchDelete"
              >
                批量删除
              </ElButton>
            </div>
          </template>

          <template #userType="{ row }">
            {{ getUserTypeText(row.userType) }}
          </template>

          <template #lockFlag="{ row }">
            <ElTag :type="getLockTagType(row.lockFlag)">
              {{ getLockText(row.lockFlag) }}
            </ElTag>
          </template>
        </ProTable>
    </div>
  </Page>
</template>

<style scoped>
.login-fail-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.login-fail-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
</style>
