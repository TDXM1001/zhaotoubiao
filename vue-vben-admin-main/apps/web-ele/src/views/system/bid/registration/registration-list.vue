<script lang="ts" setup>
import type { SystemBidRegistrationApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import { Plus } from '@vben/icons';

import {
  ElAlert,
  ElButton,
  ElMessage,
  ElMessageBox,
  ElTag,
} from 'element-plus';

import {
  approveBidRegistrationApi,
  cancelBidRegistrationApi,
  queryBidProjectListApi,
  queryBidRegistrationPageApi,
  rejectBidRegistrationApi,
} from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';
import ProTable from '#/components/pro-table/index.vue';

import {
  getRegistrationStatusText,
  getRegistrationStatusTagType,
  parseRouteNumber,
  REGISTRATION_STATUS_OPTIONS,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidRegistrationList',
});

const route = useRoute();
const router = useRouter();
const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const projectOptions = ref<{ label: string; value: number }[]>([]);

const { options: registrationTypeOptions } = useDictOptions(
  'BID_REGISTRATION_TYPE',
);

const currentProjectId = computed(() => parseRouteNumber(route.query.projectId));
const currentProjectName = computed(() => String(route.query.projectName || ''));
const currentLotId = computed(() => parseRouteNumber(route.query.lotId));
const currentLotName = computed(() => String(route.query.lotName || ''));

const tableColumns = reactive<
  ColumnProps<SystemBidRegistrationApi.RegistrationItem>[]
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
        placeholder: '请输入供应商名称、统一社会信用代码或联系人',
      },
    },
  },
  {
    enum: projectOptions,
    label: '所属项目',
    minWidth: 220,
    prop: 'projectName',
    search: {
      el: 'select',
      key: 'projectId',
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
    enum: registrationTypeOptions,
    label: '报名方式',
    minWidth: 120,
    prop: 'registrationType',
    search: {
      el: 'select',
      label: '报名方式',
    },
  },
  {
    enum: REGISTRATION_STATUS_OPTIONS,
    label: '报名状态',
    minWidth: 120,
    prop: 'status',
    search: {
      el: 'select',
      label: '报名状态',
    },
  },
  {
    label: '联系人',
    minWidth: 120,
    prop: 'contactName',
  },
  {
    label: '联系电话',
    minWidth: 140,
    prop: 'contactPhone',
  },
  {
    label: '提交时间',
    minWidth: 180,
    prop: 'submitTime',
  },
  {
    fixed: 'right',
    label: '操作',
    prop: 'operation',
    width: 300,
  },
]);

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:registration:query');
});

const hasCreatePermission = computed(() => {
  return accessStore.accessCodes.includes('bid:registration:create');
});

const hasApprovePermission = computed(() => {
  return accessStore.accessCodes.includes('bid:registration:approve');
});

const hasRejectPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:registration:reject');
});

const hasCancelPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:registration:cancel');
});

async function loadProjectOptions() {
  const projects = await queryBidProjectListApi();
  projectOptions.value = projects.map((item) => ({
    label: `${item.projectName}（${item.projectCode}）`,
    value: item.projectId,
  }));
}

async function queryRegistrationTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    return {
      list: [],
      total: 0,
    };
  }

  return queryBidRegistrationPageApi({
    keyword: params.keyword?.trim() || undefined,
    lotId: params.lotId || currentLotId.value,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    projectId: params.projectId || currentProjectId.value,
    registrationType: params.registrationType || undefined,
    searchCount: true,
    status: params.status || undefined,
  });
}

function handleSearch() {
  if (!hasQueryPermission.value) {
    ElMessage.warning('当前账号没有供应商报名查询权限');
  }
}

function openCreatePage() {
  router.push({
    path: '/system/bid/registration/create',
    query: {
      ...(currentProjectId.value
        ? { projectId: String(currentProjectId.value) }
        : {}),
      ...(currentLotId.value ? { lotId: String(currentLotId.value) } : {}),
    },
  });
}

function openDetailPage(row: SystemBidRegistrationApi.RegistrationItem) {
  router.push({
    path: '/system/bid/registration/detail',
    query: {
      registrationId: String(row.registrationId),
    },
  });
}

function canAudit(row: SystemBidRegistrationApi.RegistrationItem) {
  return row.status === 'SUBMITTED';
}

function hasAction(
  row: SystemBidRegistrationApi.RegistrationItem,
  action: string,
) {
  return row.allowedActions?.includes(action) ?? false;
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

async function handleApprove(row: SystemBidRegistrationApi.RegistrationItem) {
  await ElMessageBox.confirm(
    `确定通过供应商“${row.supplierNameSnapshot}”的报名申请吗？`,
    '报名审核确认',
    {
      type: 'warning',
    },
  );

  await approveBidRegistrationApi({
    registrationId: row.registrationId,
    version: row.version,
  });
  ElMessage.success('供应商报名已审核通过');
  await reloadTable();
}

async function handleReject(row: SystemBidRegistrationApi.RegistrationItem) {
  const { value } = await ElMessageBox.prompt(
    `请输入驳回供应商“${row.supplierNameSnapshot}”报名申请的原因`,
    '驳回报名',
    {
      confirmButtonText: '确认驳回',
      inputErrorMessage: '驳回原因不能为空',
      inputPattern: /\S+/,
      inputPlaceholder: '请输入驳回原因',
      type: 'warning',
    },
  );

  await rejectBidRegistrationApi({
    registrationId: row.registrationId,
    remark: String(value).trim(),
    version: row.version,
  });
  ElMessage.success('供应商报名已驳回');
  await reloadTable();
}

async function handleCancel(row: SystemBidRegistrationApi.RegistrationItem) {
  const { value } = await ElMessageBox.prompt(
    `请输入取消供应商“${row.supplierNameSnapshot}”报名申请的原因`,
    '取消报名',
    {
      confirmButtonText: '确认取消',
      inputErrorMessage: '取消原因不能为空',
      inputPattern: /\S+/,
      inputPlaceholder: '请输入取消原因',
      type: 'warning',
    },
  );

  await cancelBidRegistrationApi({
    registrationId: row.registrationId,
    remark: String(value).trim(),
    version: row.version,
  });
  ElMessage.success('供应商报名已取消');
  await reloadTable();
}

function clearRouteFilter() {
  router.replace('/system/bid/registration/list');
  void reloadTable();
}

onMounted(() => {
  void loadProjectOptions();
});
</script>

<template>
  <Page auto-content-height class="bid-registration-page">
    <div class="bid-registration-page__content">
      <ElAlert
        v-if="currentProjectId || currentLotId"
        :closable="false"
        class="bid-registration-page__alert"
        show-icon
        type="info"
      >
        <template #title>
          正在查看
          <strong>{{ currentLotName || currentProjectName || currentLotId || currentProjectId }}</strong>
          相关的供应商报名
          <ElButton link type="primary" @click="clearRouteFilter">
            清除筛选
          </ElButton>
        </template>
      </ElAlert>

      <ElAlert
        v-if="!hasQueryPermission"
        :closable="false"
        class="bid-registration-page__alert"
        show-icon
        title="当前账号没有供应商报名查询权限"
        type="warning"
      />

      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryRegistrationTable"
        row-key="registrationId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
        @search="handleSearch"
      >
        <template #tableHeader>
          <div class="bid-registration-page__toolbar">
            <ElButton
              v-if="hasCreatePermission"
              :icon="Plus"
              type="primary"
              @click="openCreatePage"
            >
              新增报名
            </ElButton>
          </div>
        </template>

        <template #status="{ row }">
          <ElTag :type="getRegistrationStatusTagType(row.status)">
            {{ getRegistrationStatusText(row.status) }}
          </ElTag>
        </template>

        <template #operation="{ row }">
          <ElButton link type="primary" @click="openDetailPage(row)">
            详情
          </ElButton>
          <ElButton
            v-if="hasApprovePermission && canAudit(row)"
            link
            type="success"
            @click="handleApprove(row)"
          >
            通过
          </ElButton>
          <ElButton
            v-if="hasRejectPermission && canAudit(row)"
            link
            type="danger"
            @click="handleReject(row)"
          >
            驳回
          </ElButton>
          <ElButton
            v-if="hasCancelPermission && hasAction(row, 'cancel-registration')"
            link
            type="warning"
            @click="handleCancel(row)"
          >
            取消
          </ElButton>
        </template>
      </ProTable>
    </div>
  </Page>
</template>

<style scoped>
.bid-registration-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.bid-registration-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.bid-registration-page__alert {
  margin-bottom: 4px;
}
</style>
