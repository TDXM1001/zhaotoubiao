<script lang="ts" setup>
import type { SystemBidAwardApi, SystemBidProjectApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { Plus } from '@vben/icons';
import { useAccessStore } from '@vben/stores';

import { ElButton, ElMessage, ElMessageBox, ElTag } from 'element-plus';

import {
  cancelBidAwardApi,
  confirmBidAwardApi,
  queryBidAwardPageApi,
  queryBidProjectListApi,
  rollbackBidAwardApi,
} from '#/api';
import ProTable from '#/components/pro-table/index.vue';

import {
  AWARD_STATUS_OPTIONS,
  getAwardStatusText,
  getStatusTagType,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidAwardList',
});

const route = useRoute();
const router = useRouter();
const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const projectOptions = ref<SystemBidProjectApi.ProjectOption[]>([]);

const currentProjectId = computed(() => parseRouteNumber(route.query.projectId));
const currentLotId = computed(() => parseRouteNumber(route.query.lotId));

const projectSearchOptions = computed(() =>
  projectOptions.value.map((item) => ({
    label: `${item.projectName}（${item.projectCode}）`,
    value: item.projectId,
  })),
);

const tableColumns = reactive<ColumnProps<SystemBidAwardApi.AwardItem>[]>([
  {
    enum: projectSearchOptions,
    label: '所属项目',
    minWidth: 220,
    prop: 'projectName',
    search: { el: 'select', label: '所属项目' },
  },
  {
    label: '所属标段',
    minWidth: 180,
    prop: 'lotName',
    search: {
      el: 'input',
      key: 'keyword',
      label: '关键字',
      props: { placeholder: '请输入项目、标段或供应商' },
    },
  },
  { label: '中标供应商', minWidth: 220, prop: 'winnerNameSnapshot' },
  { label: '中标金额', minWidth: 120, prop: 'awardAmount' },
  {
    enum: AWARD_STATUS_OPTIONS,
    label: '状态',
    minWidth: 120,
    prop: 'status',
    search: { el: 'select', label: '状态' },
  },
  { label: '确认时间', minWidth: 180, prop: 'confirmTime' },
  { fixed: 'right', label: '操作', prop: 'operation', width: 280 },
]);

const hasQueryPermission = computed(() =>
  accessStore.accessCodes.includes('bid:award:query'),
);
const hasCreatePermission = computed(() =>
  accessStore.accessCodes.includes('bid:award:create'),
);
const hasConfirmPermission = computed(() =>
  accessStore.accessCodes.includes('bid:award:confirm-award'),
);
const hasRollbackPermission = computed(() =>
  accessStore.accessCodes.includes('bid:award:rollback-award'),
);
const hasCancelPermission = computed(() =>
  accessStore.accessCodes.includes('bid:award:cancel-award'),
);

async function loadProjectOptions() {
  projectOptions.value = await queryBidProjectListApi();
}

async function queryAwardTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    return { list: [], total: 0 };
  }
  return queryBidAwardPageApi({
    keyword: params.keyword?.trim() || undefined,
    lotId: params.lotId || currentLotId.value,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    projectId: params.projectId || currentProjectId.value,
    searchCount: true,
    status: params.status || undefined,
  });
}

function handleSearch() {
  if (!hasQueryPermission.value) {
    ElMessage.warning('当前账号没有定标查询权限');
  }
}

function openCreatePage() {
  router.push({
    path: '/system/bid/award/create',
    query: {
      ...(currentProjectId.value
        ? { projectId: String(currentProjectId.value) }
        : {}),
      ...(currentLotId.value ? { lotId: String(currentLotId.value) } : {}),
    },
  });
}

function openDetailPage(row: SystemBidAwardApi.AwardItem) {
  router.push({
    path: '/system/bid/award/detail',
    query: { awardId: String(row.awardId) },
  });
}

function hasAction(row: SystemBidAwardApi.AwardItem, action: string) {
  return row.allowedActions?.includes(action) ?? false;
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

async function handleConfirm(row: SystemBidAwardApi.AwardItem) {
  await ElMessageBox.confirm(
    `确定确认“${row.winnerNameSnapshot}”为中标供应商吗？`,
    '确认定标',
    { type: 'warning' },
  );
  await confirmBidAwardApi({ awardId: row.awardId, version: row.version });
  ElMessage.success('定标已确认');
  await reloadTable();
}

async function handleRollback(row: SystemBidAwardApi.AwardItem) {
  const { value } = await ElMessageBox.prompt(
    `请输入“${row.lotName}”定标回退原因`,
    '回退定标',
    {
      confirmButtonText: '确认回退',
      inputErrorMessage: '回退原因不能为空',
      inputPattern: /\S+/,
      inputPlaceholder: '请输入回退原因',
      type: 'warning',
    },
  );
  await rollbackBidAwardApi({
    awardId: row.awardId,
    remark: String(value).trim(),
    version: row.version,
  });
  ElMessage.success('定标已回退');
  await reloadTable();
}

async function handleCancel(row: SystemBidAwardApi.AwardItem) {
  await ElMessageBox.confirm(`确定取消“${row.lotName}”定标记录吗？`, '取消定标', {
    type: 'warning',
  });
  await cancelBidAwardApi({ awardId: row.awardId, version: row.version });
  ElMessage.success('定标已取消');
  await reloadTable();
}

onMounted(() => {
  void loadProjectOptions();
});
</script>

<template>
  <Page auto-content-height>
    <ProTable
      ref="proTable"
      :columns="tableColumns"
      :request-api="queryAwardTable"
      row-key="awardId"
      :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
      :search-default-collapsed="false"
      :tool-button="['refresh', 'setting']"
      @search="handleSearch"
    >
      <template #tableHeader>
        <ElButton
          v-if="hasCreatePermission"
          :icon="Plus"
          type="primary"
          @click="openCreatePage"
        >
          新增定标
        </ElButton>
      </template>

      <template #status="{ row }">
        <ElTag :type="getStatusTagType(row.status)">
          {{ getAwardStatusText(row.status) }}
        </ElTag>
      </template>

      <template #operation="{ row }">
        <ElButton link type="primary" @click="openDetailPage(row)">
          详情
        </ElButton>
        <ElButton
          v-if="hasConfirmPermission && hasAction(row, 'confirm-award')"
          link
          type="success"
          @click="handleConfirm(row)"
        >
          确认
        </ElButton>
        <ElButton
          v-if="hasRollbackPermission && hasAction(row, 'rollback-award')"
          link
          type="danger"
          @click="handleRollback(row)"
        >
          回退
        </ElButton>
        <ElButton
          v-if="hasCancelPermission && hasAction(row, 'cancel-award')"
          link
          type="warning"
          @click="handleCancel(row)"
        >
          取消
        </ElButton>
      </template>
    </ProTable>
  </Page>
</template>
