<script lang="ts" setup>
import type { SystemBidProjectApi, SystemBidSubmissionApi } from '#/api';
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
  addBidSubmissionApi,
  getBidRegistrationDetailApi,
  queryBidProjectListApi,
  queryBidSubmissionPageApi,
  withdrawBidSubmissionApi,
} from '#/api';
import ProTable from '#/components/pro-table/index.vue';

import {
  getStatusTagType,
  getSubmissionStatusText,
  parseRouteNumber,
  SUBMISSION_STATUS_OPTIONS,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidSubmissionList',
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
const currentRegistrationId = computed(() =>
  parseRouteNumber(route.query.registrationId),
);

const projectSearchOptions = computed(() => {
  return projectOptions.value.map((item) => ({
    label: `${item.projectName}（${item.projectCode}）`,
    value: item.projectId,
  }));
});

const tableColumns = reactive<
  ColumnProps<SystemBidSubmissionApi.SubmissionItem>[]
>([
  {
    label: '供应商名称',
    minWidth: 220,
    prop: 'supplierNameSnapshot',
    search: {
      el: 'input',
      key: 'keyword',
      label: '关键字',
      props: {
        placeholder: '请输入供应商、信用代码、项目、标段或回执号',
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
    label: '统一社会信用代码',
    minWidth: 180,
    prop: 'supplierCreditCode',
  },
  {
    enum: SUBMISSION_STATUS_OPTIONS,
    label: '投标状态',
    minWidth: 120,
    prop: 'status',
    search: {
      el: 'select',
      label: '投标状态',
    },
  },
  {
    label: '回执号',
    minWidth: 220,
    prop: 'receiptNo',
  },
  {
    label: '最新版本',
    minWidth: 100,
    prop: 'latestVersionNo',
  },
  {
    label: '报价金额',
    minWidth: 140,
    prop: 'priceAmount',
  },
  {
    label: '最近提交时间',
    minWidth: 180,
    prop: 'latestSubmitTime',
  },
  {
    fixed: 'right',
    label: '操作',
    prop: 'operation',
    width: 260,
  },
]);

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:submission:query');
});

const hasCreatePermission = computed(() => {
  return accessStore.accessCodes.includes('bid:submission:create');
});

const hasSubmitPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:submission:submit-bid');
});

const hasWithdrawPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:submission:withdraw-bid');
});

async function loadProjectOptions() {
  projectOptions.value = await queryBidProjectListApi();
}

async function querySubmissionTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    return {
      list: [],
      total: 0,
    };
  }

  return queryBidSubmissionPageApi({
    keyword: params.keyword?.trim() || undefined,
    lotId: params.lotId || currentLotId.value,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    projectId: params.projectId || currentProjectId.value,
    registrationId: currentRegistrationId.value,
    searchCount: true,
    status: params.status || undefined,
  });
}

function handleSearch() {
  if (!hasQueryPermission.value) {
    ElMessage.warning('当前账号没有投标查询权限');
  }
}

function hasAction(row: SystemBidSubmissionApi.SubmissionItem, action: string) {
  return row.allowedActions?.includes(action) ?? false;
}

function openDetailPage(row: SystemBidSubmissionApi.SubmissionItem) {
  router.push({
    path: '/system/bid/submission/detail',
    query: {
      submissionId: String(row.submissionId),
    },
  });
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

async function handleCreateFromRegistration() {
  const { value } = await ElMessageBox.prompt(
    '请输入已通过资格审核的报名ID',
    '创建投标记录',
    {
      confirmButtonText: '确认创建',
      inputErrorMessage: '报名ID必须为数字',
      inputPattern: /^\d+$/,
      inputPlaceholder: '请输入报名ID',
      type: 'info',
    },
  );
  const registrationId = Number(value);
  const registration = await getBidRegistrationDetailApi(registrationId);

  await addBidSubmissionApi({
    lotId: registration.lotId,
    projectId: registration.projectId,
    registrationId: registration.registrationId,
  });
  ElMessage.success('投标记录已创建');
  await reloadTable();
}

function handleSubmit(row: SystemBidSubmissionApi.SubmissionItem) {
  router.push({
    path: '/system/bid/submission/detail',
    query: {
      openSubmit: '1',
      submissionId: String(row.submissionId),
    },
  });
}

async function handleWithdraw(row: SystemBidSubmissionApi.SubmissionItem) {
  const { value } = await ElMessageBox.prompt(
    `请输入撤回供应商“${row.supplierNameSnapshot}”投标的原因`,
    '撤回投标',
    {
      confirmButtonText: '确认撤回',
      inputErrorMessage: '撤回原因不能为空',
      inputPattern: /\S+/,
      inputPlaceholder: '请输入撤回原因',
      type: 'warning',
    },
  );

  await withdrawBidSubmissionApi({
    remark: String(value).trim(),
    submissionId: row.submissionId,
    version: row.version,
  });
  ElMessage.success('投标已撤回');
  await reloadTable();
}

function clearRouteFilter() {
  router.replace('/system/bid/submission/list');
  void reloadTable();
}

onMounted(() => {
  void loadProjectOptions();
});
</script>

<template>
  <Page auto-content-height class="bid-submission-page">
    <div class="bid-submission-page__content">
      <ElAlert
        v-if="currentProjectId || currentLotId || currentRegistrationId"
        :closable="false"
        class="bid-submission-page__alert"
        show-icon
        type="info"
      >
        <template #title>
          正在查看
          <strong>
            {{
              currentLotName
              || currentProjectName
              || currentRegistrationId
              || currentLotId
              || currentProjectId
            }}
          </strong>
          相关的投标记录
          <ElButton link type="primary" @click="clearRouteFilter">
            清除筛选
          </ElButton>
        </template>
      </ElAlert>

      <ElAlert
        v-if="!hasQueryPermission"
        :closable="false"
        class="bid-submission-page__alert"
        show-icon
        title="当前账号没有投标查询权限"
        type="warning"
      />

      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="querySubmissionTable"
        row-key="submissionId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
        @search="handleSearch"
      >
        <template #tableHeader>
          <div class="bid-submission-page__toolbar">
            <ElButton
              v-if="hasCreatePermission"
              :icon="Plus"
              type="primary"
              @click="handleCreateFromRegistration"
            >
              从报名创建
            </ElButton>
          </div>
        </template>

        <template #status="{ row }">
          <ElTag :type="getStatusTagType(row.status)">
            {{ getSubmissionStatusText(row.status) }}
          </ElTag>
        </template>

        <template #operation="{ row }">
          <ElButton link type="primary" @click="openDetailPage(row)">
            详情
          </ElButton>
          <ElButton
            v-if="hasSubmitPermission && hasAction(row, 'submit-bid')"
            link
            type="success"
            @click="handleSubmit(row)"
          >
            提交
          </ElButton>
          <ElButton
            v-if="hasWithdrawPermission && hasAction(row, 'withdraw-bid')"
            link
            type="warning"
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
.bid-submission-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.bid-submission-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.bid-submission-page__alert {
  margin-bottom: 4px;
}
</style>
