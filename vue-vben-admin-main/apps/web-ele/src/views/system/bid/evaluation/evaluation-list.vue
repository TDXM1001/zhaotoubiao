<script lang="ts" setup>
import type { SystemBidEvaluationApi, SystemBidProjectApi } from '#/api';
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
  finalizeBidEvaluationApi,
  queryBidEvaluationPageApi,
  queryBidProjectListApi,
  rollbackBidEvaluationApi,
  startBidEvaluationApi,
} from '#/api';
import ProTable from '#/components/pro-table/index.vue';

import {
  EVALUATION_STATUS_OPTIONS,
  getEvaluationStatusText,
  getStatusTagType,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidEvaluationList',
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

const tableColumns = reactive<
  ColumnProps<SystemBidEvaluationApi.EvaluationItem>[]
>([
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
      props: { placeholder: '请输入项目、标段或摘要' },
    },
  },
  { label: '开标ID', minWidth: 100, prop: 'openingId' },
  { label: '评标方式', minWidth: 130, prop: 'evaluationMode' },
  {
    enum: EVALUATION_STATUS_OPTIONS,
    label: '状态',
    minWidth: 120,
    prop: 'status',
    search: { el: 'select', label: '状态' },
  },
  { label: '开始时间', minWidth: 180, prop: 'startTime' },
  { label: '定稿时间', minWidth: 180, prop: 'finalizeTime' },
  { fixed: 'right', label: '操作', prop: 'operation', width: 280 },
]);

const hasQueryPermission = computed(() =>
  accessStore.accessCodes.includes('bid:evaluation:query'),
);
const hasCreatePermission = computed(() =>
  accessStore.accessCodes.includes('bid:evaluation:create'),
);
const hasStartPermission = computed(() =>
  accessStore.accessCodes.includes('bid:evaluation:start-evaluation'),
);
const hasFinalizePermission = computed(() =>
  accessStore.accessCodes.includes('bid:evaluation:finalize-evaluation'),
);
const hasRollbackPermission = computed(() =>
  accessStore.accessCodes.includes('bid:evaluation:rollback-evaluation'),
);

async function loadProjectOptions() {
  projectOptions.value = await queryBidProjectListApi();
}

async function queryEvaluationTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    return { list: [], total: 0 };
  }
  return queryBidEvaluationPageApi({
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
    ElMessage.warning('当前账号没有评标查询权限');
  }
}

function openCreatePage() {
  router.push({
    path: '/system/bid/evaluation/create',
    query: {
      ...(currentProjectId.value
        ? { projectId: String(currentProjectId.value) }
        : {}),
      ...(currentLotId.value ? { lotId: String(currentLotId.value) } : {}),
    },
  });
}

function openDetailPage(row: SystemBidEvaluationApi.EvaluationItem) {
  router.push({
    path: '/system/bid/evaluation/detail',
    query: { evaluationId: String(row.evaluationId) },
  });
}

function hasAction(row: SystemBidEvaluationApi.EvaluationItem, action: string) {
  return row.allowedActions?.includes(action) ?? false;
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

async function handleStart(row: SystemBidEvaluationApi.EvaluationItem) {
  await startBidEvaluationApi({
    evaluationId: row.evaluationId,
    version: row.version,
  });
  ElMessage.success('评标已开始');
  await reloadTable();
}

async function handleFinalize(row: SystemBidEvaluationApi.EvaluationItem) {
  const { value } = await ElMessageBox.prompt(
    `请输入“${row.lotName}”评标定稿摘要，可留空`,
    '评标定稿',
    { confirmButtonText: '确认定稿', inputPlaceholder: '请输入评标摘要' },
  );
  await finalizeBidEvaluationApi({
    evaluationId: row.evaluationId,
    remark: String(value ?? '').trim() || undefined,
    version: row.version,
  });
  ElMessage.success('评标已定稿');
  await reloadTable();
}

async function handleRollback(row: SystemBidEvaluationApi.EvaluationItem) {
  const { value } = await ElMessageBox.prompt(
    `请输入“${row.lotName}”评标回退原因`,
    '回退评标',
    {
      confirmButtonText: '确认回退',
      inputErrorMessage: '回退原因不能为空',
      inputPattern: /\S+/,
      inputPlaceholder: '请输入回退原因',
      type: 'warning',
    },
  );
  await rollbackBidEvaluationApi({
    evaluationId: row.evaluationId,
    remark: String(value).trim(),
    version: row.version,
  });
  ElMessage.success('评标已回退');
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
      :request-api="queryEvaluationTable"
      row-key="evaluationId"
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
          创建评标记录
        </ElButton>
      </template>

      <template #status="{ row }">
        <ElTag :type="getStatusTagType(row.status)">
          {{ getEvaluationStatusText(row.status) }}
        </ElTag>
      </template>

      <template #operation="{ row }">
        <ElButton link type="primary" @click="openDetailPage(row)">
          详情
        </ElButton>
        <ElButton
          v-if="hasStartPermission && hasAction(row, 'start-evaluation')"
          link
          type="success"
          @click="handleStart(row)"
        >
          开始
        </ElButton>
        <ElButton
          v-if="hasFinalizePermission && hasAction(row, 'finalize-evaluation')"
          link
          type="success"
          @click="handleFinalize(row)"
        >
          定稿
        </ElButton>
        <ElButton
          v-if="hasRollbackPermission && hasAction(row, 'rollback-evaluation')"
          link
          type="danger"
          @click="handleRollback(row)"
        >
          回退
        </ElButton>
      </template>
    </ProTable>
  </Page>
</template>
