<script lang="ts" setup>
import type { SystemBidProjectApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

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
  cancelBidProjectApi,
  publishBidProjectApi,
  queryBidProjectPageApi,
  submitBidProjectPlanApi,
} from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';
import ProTable from '#/components/pro-table/index.vue';

import {
  getProjectStatusText,
  getStatusTagType,
  PROJECT_STATUS_OPTIONS,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidProjectList',
});

const router = useRouter();
const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();

const { options: projectTypeOptions } = useDictOptions('BID_PROJECT_TYPE');
const { options: procurementModeOptions } = useDictOptions(
  'BID_PROCUREMENT_MODE',
);

const tableColumns = reactive<ColumnProps<SystemBidProjectApi.ProjectItem>[]>([
  {
    label: '项目名称',
    minWidth: 220,
    prop: 'projectName',
    search: {
      el: 'input',
      key: 'keyword',
      label: '关键字',
      props: {
        placeholder: '请输入项目名称或项目编号',
      },
    },
  },
  {
    label: '项目编号',
    minWidth: 160,
    prop: 'projectCode',
  },
  {
    enum: projectTypeOptions,
    label: '项目类型',
    minWidth: 140,
    prop: 'projectType',
    search: {
      el: 'select',
      label: '项目类型',
    },
  },
  {
    enum: procurementModeOptions,
    label: '采购方式',
    minWidth: 160,
    prop: 'procurementMode',
    search: {
      el: 'select',
      label: '采购方式',
    },
  },
  {
    enum: PROJECT_STATUS_OPTIONS,
    label: '项目状态',
    minWidth: 120,
    prop: 'status',
    search: {
      el: 'select',
      label: '项目状态',
    },
  },
  {
    label: '归属组织',
    minWidth: 160,
    prop: 'ownerOrgName',
  },
  {
    label: '项目负责人',
    minWidth: 140,
    prop: 'managerEmployeeName',
  },
  {
    label: '项目预算',
    minWidth: 140,
    prop: 'budgetAmount',
  },
  {
    label: '标段数量',
    minWidth: 100,
    prop: 'lotCount',
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
    width: 320,
  },
]);

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:project:query');
});

const hasCreatePermission = computed(() => {
  return accessStore.accessCodes.includes('bid:project:create');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('bid:project:update');
});

const hasSubmitPlanPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:project:submit-plan');
});

const hasPublishPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:project:publish');
});

const hasCancelPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:project:cancel');
});

async function queryProjectTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    return {
      list: [],
      total: 0,
    };
  }

  return queryBidProjectPageApi({
    keyword: params.keyword?.trim() || undefined,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    procurementMode: params.procurementMode || undefined,
    projectType: params.projectType || undefined,
    searchCount: true,
    status: params.status || undefined,
  });
}

function handleSearch() {
  if (!hasQueryPermission.value) {
    ElMessage.warning('当前账号没有招标项目查询权限');
  }
}

function openCreatePage() {
  router.push('/system/bid/project/create');
}

function openEditPage(row: SystemBidProjectApi.ProjectItem) {
  router.push({
    path: '/system/bid/project/edit',
    query: {
      projectId: String(row.projectId),
    },
  });
}

function openDetailPage(row: SystemBidProjectApi.ProjectItem) {
  router.push({
    path: '/system/bid/project/detail',
    query: {
      projectId: String(row.projectId),
    },
  });
}

function openLotPage(row: SystemBidProjectApi.ProjectItem) {
  router.push({
    path: '/system/bid/lot/list',
    query: {
      projectId: String(row.projectId),
      projectName: row.projectName,
    },
  });
}

function hasAction(
  row: SystemBidProjectApi.ProjectItem,
  action: string,
) {
  return row.allowedActions?.includes(action) ?? false;
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

async function handleSubmitPlan(row: SystemBidProjectApi.ProjectItem) {
  await ElMessageBox.confirm(
    `确定要提交项目“${row.projectName}”的计划吗？`,
    '提交计划确认',
    {
      type: 'warning',
    },
  );

  await submitBidProjectPlanApi({
    projectId: row.projectId,
    version: row.version,
  });
  ElMessage.success('项目计划提交成功');
  await reloadTable();
}

async function handlePublish(row: SystemBidProjectApi.ProjectItem) {
  await ElMessageBox.confirm(
    `确定要发布项目“${row.projectName}”吗？发布后草稿标段会同步进入投标中。`,
    '发布确认',
    {
      type: 'warning',
    },
  );

  await publishBidProjectApi({
    projectId: row.projectId,
    version: row.version,
  });
  ElMessage.success('项目发布成功');
  await reloadTable();
}

async function handleCancel(row: SystemBidProjectApi.ProjectItem) {
  await ElMessageBox.confirm(
    `确定要作废项目“${row.projectName}”吗？`,
    '作废确认',
    {
      type: 'warning',
    },
  );

  await cancelBidProjectApi({
    projectId: row.projectId,
    version: row.version,
  });
  ElMessage.success('项目作废成功');
  await reloadTable();
}
</script>

<template>
  <Page auto-content-height class="bid-project-page">
    <div class="bid-project-page__content">
      <ElAlert
        v-if="!hasQueryPermission"
        :closable="false"
        class="bid-project-page__alert"
        show-icon
        title="当前账号没有招标项目查询权限"
        type="warning"
      />

      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryProjectTable"
        row-key="projectId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
        @search="handleSearch"
      >
        <template #tableHeader>
          <div class="bid-project-page__toolbar">
            <ElButton
              v-if="hasCreatePermission"
              :icon="Plus"
              type="primary"
              @click="openCreatePage"
            >
              新增项目
            </ElButton>
          </div>
        </template>

        <template #status="{ row }">
          <ElTag :type="getStatusTagType(row.status)">
            {{ getProjectStatusText(row.status) }}
          </ElTag>
        </template>

        <template #operation="{ row }">
          <ElButton link type="primary" @click="openDetailPage(row)">
            详情
          </ElButton>
          <ElButton link type="primary" @click="openLotPage(row)">
            标段
          </ElButton>
          <ElButton
            v-if="hasUpdatePermission && hasAction(row, 'edit-project')"
            link
            type="primary"
            @click="openEditPage(row)"
          >
            编辑
          </ElButton>
          <ElButton
            v-if="hasSubmitPlanPermission && hasAction(row, 'submit-plan')"
            link
            type="warning"
            @click="handleSubmitPlan(row)"
          >
            提交计划
          </ElButton>
          <ElButton
            v-if="hasPublishPermission && hasAction(row, 'publish-project')"
            link
            type="success"
            @click="handlePublish(row)"
          >
            发布
          </ElButton>
          <ElButton
            v-if="hasCancelPermission && hasAction(row, 'cancel-project')"
            link
            type="danger"
            @click="handleCancel(row)"
          >
            作废
          </ElButton>
        </template>
      </ProTable>
    </div>
  </Page>
</template>

<style scoped>
.bid-project-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.bid-project-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.bid-project-page__alert {
  margin-bottom: 4px;
}
</style>
