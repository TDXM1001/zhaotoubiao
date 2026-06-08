<script lang="ts" setup>
import type { SystemBidProjectApi, SystemBidTenderApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { Plus } from '@vben/icons';
import { useAccessStore } from '@vben/stores';

import {
  ElAlert,
  ElButton,
  ElMessage,
  ElMessageBox,
  ElTag,
} from 'element-plus';

import {
  clarifyBidTenderApi,
  publishBidTenderApi,
  queryBidProjectListApi,
  queryBidTenderPageApi,
  withdrawBidTenderApi,
} from '#/api';
import ProTable from '#/components/pro-table/index.vue';

import {
  getStatusTagType,
  getTenderStatusText,
  getTenderVersionTypeText,
  parseRouteNumber,
  TENDER_STATUS_OPTIONS,
  TENDER_VERSION_TYPE_OPTIONS,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidTenderList',
});

const route = useRoute();
const router = useRouter();
const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const projectOptions = ref<SystemBidProjectApi.ProjectOption[]>([]);

const currentProjectId = computed(() => parseRouteNumber(route.query.projectId));
const currentProjectName = computed(() => String(route.query.projectName || ''));
const currentLotId = computed(() => parseRouteNumber(route.query.lotId));
const currentLotName = computed(() => String(route.query.lotName || ''));

const projectSearchOptions = computed(() => {
  return projectOptions.value.map((item) => ({
    label: `${item.projectName}（${item.projectCode}）`,
    value: item.projectId,
  }));
});

const tableColumns = reactive<ColumnProps<SystemBidTenderApi.TenderItem>[]>([
  {
    label: '版本摘要',
    minWidth: 220,
    prop: 'summary',
    search: {
      el: 'input',
      key: 'keyword',
      label: '关键字',
      props: {
        placeholder: '请输入项目、标段或版本摘要',
      },
    },
  },
  {
    enum: projectSearchOptions,
    label: '所属项目',
    minWidth: 220,
    prop: 'projectName',
    search: {
      el: 'select',
      label: '所属项目',
    },
  },
  {
    label: '所属标段',
    minWidth: 200,
    prop: 'lotName',
  },
  {
    enum: TENDER_VERSION_TYPE_OPTIONS,
    label: '版本类型',
    minWidth: 130,
    prop: 'versionType',
    search: {
      el: 'select',
      label: '版本类型',
    },
  },
  {
    label: '版本号',
    minWidth: 100,
    prop: 'versionNo',
  },
  {
    enum: TENDER_STATUS_OPTIONS,
    label: '状态',
    minWidth: 120,
    prop: 'status',
    search: {
      el: 'select',
      label: '状态',
    },
  },
  {
    label: '发布时间',
    minWidth: 180,
    prop: 'publishTime',
  },
  {
    label: '生效时间',
    minWidth: 180,
    prop: 'effectiveTime',
  },
  {
    fixed: 'right',
    label: '操作',
    prop: 'operation',
    width: 320,
  },
]);

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:tender:query');
});

const hasCreatePermission = computed(() => {
  return accessStore.accessCodes.includes('bid:tender:create');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('bid:tender:update');
});

const hasPublishPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:tender:publish-tender');
});

const hasClarifyPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:tender:clarify-tender');
});

const hasWithdrawPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:tender:withdraw-tender');
});

async function loadProjectOptions() {
  projectOptions.value = await queryBidProjectListApi();
}

async function queryTenderTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    return {
      list: [],
      total: 0,
    };
  }

  return queryBidTenderPageApi({
    keyword: params.keyword?.trim() || undefined,
    lotId: params.lotId || currentLotId.value,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    projectId: params.projectId || currentProjectId.value,
    searchCount: true,
    status: params.status || undefined,
    versionType: params.versionType || undefined,
  });
}

function handleSearch() {
  if (!hasQueryPermission.value) {
    ElMessage.warning('当前账号没有招标文件查询权限');
  }
}

function openCreatePage() {
  router.push({
    path: '/system/bid/tender/create',
    query: {
      ...(currentProjectId.value
        ? { projectId: String(currentProjectId.value) }
        : {}),
      ...(currentLotId.value ? { lotId: String(currentLotId.value) } : {}),
    },
  });
}

function openEditPage(row: SystemBidTenderApi.TenderItem) {
  router.push({
    path: '/system/bid/tender/create',
    query: {
      tenderVersionId: String(row.tenderVersionId),
    },
  });
}

function openDetailPage(row: SystemBidTenderApi.TenderItem) {
  router.push({
    path: '/system/bid/tender/detail',
    query: {
      tenderVersionId: String(row.tenderVersionId),
    },
  });
}

function hasAction(row: SystemBidTenderApi.TenderItem, action: string) {
  return row.allowedActions?.includes(action) ?? false;
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

async function handlePublish(row: SystemBidTenderApi.TenderItem) {
  await ElMessageBox.confirm(
    `确定发布标段“${row.lotName}”的第 ${row.versionNo} 版招标文件吗？`,
    '发布招标文件',
    { type: 'warning' },
  );

  await publishBidTenderApi({
    tenderVersionId: row.tenderVersionId,
    version: row.version,
  });
  ElMessage.success('招标文件已发布');
  await reloadTable();
}

async function handleClarify(row: SystemBidTenderApi.TenderItem) {
  const { value } = await ElMessageBox.prompt(
    `请输入标段“${row.lotName}”本次澄清说明`,
    '发布澄清文件',
    {
      confirmButtonText: '确认发布',
      inputErrorMessage: '澄清说明不能为空',
      inputPattern: /\S+/,
      inputPlaceholder: '请输入澄清说明',
      type: 'warning',
    },
  );

  await clarifyBidTenderApi({
    remark: String(value).trim(),
    tenderVersionId: row.tenderVersionId,
    version: row.version,
  });
  ElMessage.success('澄清文件已发布');
  await reloadTable();
}

async function handleWithdraw(row: SystemBidTenderApi.TenderItem) {
  const { value } = await ElMessageBox.prompt(
    `请输入撤回标段“${row.lotName}”招标文件的原因`,
    '撤回招标文件',
    {
      confirmButtonText: '确认撤回',
      inputErrorMessage: '撤回原因不能为空',
      inputPattern: /\S+/,
      inputPlaceholder: '请输入撤回原因',
      type: 'warning',
    },
  );

  await withdrawBidTenderApi({
    remark: String(value).trim(),
    tenderVersionId: row.tenderVersionId,
    version: row.version,
  });
  ElMessage.success('招标文件已撤回');
  await reloadTable();
}

function clearRouteFilter() {
  router.replace('/system/bid/tender/list');
  void reloadTable();
}

onMounted(() => {
  void loadProjectOptions();
});
</script>

<template>
  <Page auto-content-height class="bid-tender-page">
    <div class="bid-tender-page__content">
      <ElAlert
        v-if="currentProjectId || currentLotId"
        :closable="false"
        class="bid-tender-page__alert"
        show-icon
        type="info"
      >
        <template #title>
          正在查看
          <strong>{{ currentLotName || currentProjectName || currentLotId || currentProjectId }}</strong>
          相关的招标文件
          <ElButton link type="primary" @click="clearRouteFilter">
            清除筛选
          </ElButton>
        </template>
      </ElAlert>

      <ElAlert
        v-if="!hasQueryPermission"
        :closable="false"
        class="bid-tender-page__alert"
        show-icon
        title="当前账号没有招标文件查询权限"
        type="warning"
      />

      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryTenderTable"
        row-key="tenderVersionId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
        @search="handleSearch"
      >
        <template #tableHeader>
          <div class="bid-tender-page__toolbar">
            <ElButton
              v-if="hasCreatePermission"
              :icon="Plus"
              type="primary"
              @click="openCreatePage"
            >
              新增招标文件
            </ElButton>
          </div>
        </template>

        <template #versionType="{ row }">
          {{ getTenderVersionTypeText(row.versionType) }}
        </template>

        <template #status="{ row }">
          <ElTag :type="getStatusTagType(row.status)">
            {{ getTenderStatusText(row.status) }}
          </ElTag>
        </template>

        <template #operation="{ row }">
          <ElButton link type="primary" @click="openDetailPage(row)">
            详情
          </ElButton>
          <ElButton
            v-if="hasUpdatePermission && hasAction(row, 'edit-tender')"
            link
            type="primary"
            @click="openEditPage(row)"
          >
            编辑
          </ElButton>
          <ElButton
            v-if="hasPublishPermission && hasAction(row, 'publish-tender')"
            link
            type="success"
            @click="handlePublish(row)"
          >
            发布
          </ElButton>
          <ElButton
            v-if="hasClarifyPermission && hasAction(row, 'clarify-tender')"
            link
            type="warning"
            @click="handleClarify(row)"
          >
            澄清
          </ElButton>
          <ElButton
            v-if="hasWithdrawPermission && hasAction(row, 'withdraw-tender')"
            link
            type="danger"
            @click="handleWithdraw(row)"
          >
            撤回
          </ElButton>
        </template>
      </ProTable>
    </div>
  </Page>
</template>

<style scoped>
.bid-tender-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.bid-tender-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.bid-tender-page__alert {
  margin-bottom: 4px;
}
</style>
