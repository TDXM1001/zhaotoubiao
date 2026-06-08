<script lang="ts" setup>
import type { SystemBidOpeningApi, SystemBidProjectApi } from '#/api';
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
  abnormalCloseBidOpeningApi,
  completeBidOpeningApi,
  queryBidOpeningPageApi,
  queryBidProjectListApi,
  startBidOpeningApi,
} from '#/api';
import ProTable from '#/components/pro-table/index.vue';

import {
  getOpeningStatusText,
  getStatusTagType,
  OPENING_STATUS_OPTIONS,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidOpeningList',
});

const route = useRoute();
const router = useRouter();
const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const projectOptions = ref<SystemBidProjectApi.ProjectOption[]>([]);

const currentProjectId = computed(() => parseRouteNumber(route.query.projectId));
const currentLotId = computed(() => parseRouteNumber(route.query.lotId));

const projectSearchOptions = computed(() => {
  return projectOptions.value.map((item) => ({
    label: `${item.projectName}（${item.projectCode}）`,
    value: item.projectId,
  }));
});

const tableColumns = reactive<ColumnProps<SystemBidOpeningApi.OpeningItem>[]>([
  {
    label: '所属项目',
    minWidth: 220,
    prop: 'projectName',
    search: {
      el: 'select',
      label: '所属项目',
    },
    enum: projectSearchOptions,
  },
  {
    label: '所属标段',
    minWidth: 180,
    prop: 'lotName',
    search: {
      el: 'input',
      key: 'keyword',
      label: '关键字',
      props: {
        placeholder: '请输入项目、标段或地点',
      },
    },
  },
  {
    label: '开标地点',
    minWidth: 180,
    prop: 'openingPlace',
  },
  {
    label: '开标时间',
    minWidth: 180,
    prop: 'openingTime',
  },
  {
    enum: OPENING_STATUS_OPTIONS,
    label: '状态',
    minWidth: 120,
    prop: 'status',
    search: {
      el: 'select',
      label: '状态',
    },
  },
  {
    label: '更新时间',
    minWidth: 180,
    prop: 'updateTime',
  },
  {
    fixed: 'right',
    label: '操作',
    prop: 'operation',
    width: 280,
  },
]);

const hasQueryPermission = computed(() =>
  accessStore.accessCodes.includes('bid:opening:query'),
);
const hasCreatePermission = computed(() =>
  accessStore.accessCodes.includes('bid:opening:create'),
);
const hasStartPermission = computed(() =>
  accessStore.accessCodes.includes('bid:opening:start-opening'),
);
const hasCompletePermission = computed(() =>
  accessStore.accessCodes.includes('bid:opening:complete-opening'),
);
const hasAbnormalPermission = computed(() =>
  accessStore.accessCodes.includes('bid:opening:abnormal-close-opening'),
);

async function loadProjectOptions() {
  projectOptions.value = await queryBidProjectListApi();
}

async function queryOpeningTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    return { list: [], total: 0 };
  }

  return queryBidOpeningPageApi({
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
    ElMessage.warning('当前账号没有开标查询权限');
  }
}

function openCreatePage() {
  router.push({
    path: '/system/bid/opening/create',
    query: {
      ...(currentProjectId.value
        ? { projectId: String(currentProjectId.value) }
        : {}),
      ...(currentLotId.value ? { lotId: String(currentLotId.value) } : {}),
    },
  });
}

function openDetailPage(row: SystemBidOpeningApi.OpeningItem) {
  router.push({
    path: '/system/bid/opening/detail',
    query: { openingId: String(row.openingId) },
  });
}

function hasAction(row: SystemBidOpeningApi.OpeningItem, action: string) {
  return row.allowedActions?.includes(action) ?? false;
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

async function handleStart(row: SystemBidOpeningApi.OpeningItem) {
  await ElMessageBox.confirm(`确定开始“${row.lotName}”开标吗？`, '开始开标', {
    type: 'warning',
  });
  await startBidOpeningApi({ openingId: row.openingId, version: row.version });
  ElMessage.success('开标已开始');
  await reloadTable();
}

async function handleComplete(row: SystemBidOpeningApi.OpeningItem) {
  await ElMessageBox.confirm(`确定完成“${row.lotName}”开标吗？`, '完成开标', {
    type: 'warning',
  });
  await completeBidOpeningApi({
    openingId: row.openingId,
    version: row.version,
  });
  ElMessage.success('开标已完成');
  await reloadTable();
}

async function handleAbnormal(row: SystemBidOpeningApi.OpeningItem) {
  const { value } = await ElMessageBox.prompt(
    `请输入“${row.lotName}”异常关闭原因`,
    '异常关闭开标',
    {
      confirmButtonText: '确认关闭',
      inputErrorMessage: '异常关闭原因不能为空',
      inputPattern: /\S+/,
      inputPlaceholder: '请输入异常关闭原因',
      type: 'warning',
    },
  );
  await abnormalCloseBidOpeningApi({
    openingId: row.openingId,
    remark: String(value).trim(),
    version: row.version,
  });
  ElMessage.success('开标已异常关闭');
  await reloadTable();
}

onMounted(() => {
  void loadProjectOptions();
});
</script>

<template>
  <Page auto-content-height class="bid-opening-page">
    <ProTable
      ref="proTable"
      :columns="tableColumns"
      :request-api="queryOpeningTable"
      row-key="openingId"
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
          创建开标安排
        </ElButton>
      </template>

      <template #status="{ row }">
        <ElTag :type="getStatusTagType(row.status)">
          {{ getOpeningStatusText(row.status) }}
        </ElTag>
      </template>

      <template #operation="{ row }">
        <ElButton link type="primary" @click="openDetailPage(row)">
          详情
        </ElButton>
        <ElButton
          v-if="hasStartPermission && hasAction(row, 'start-opening')"
          link
          type="success"
          @click="handleStart(row)"
        >
          开始
        </ElButton>
        <ElButton
          v-if="hasCompletePermission && hasAction(row, 'complete-opening')"
          link
          type="success"
          @click="handleComplete(row)"
        >
          完成
        </ElButton>
        <ElButton
          v-if="hasAbnormalPermission && hasAction(row, 'abnormal-close-opening')"
          link
          type="danger"
          @click="handleAbnormal(row)"
        >
          异常关闭
        </ElButton>
      </template>
    </ProTable>
  </Page>
</template>
