<script lang="ts" setup>
import type JobExecuteModal from './components/job-execute-modal.vue';
import type JobFormModal from './components/job-form-modal.vue';
import type JobLogDrawer from './components/job-log-drawer.vue';

import type { SupportJobApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, reactive, ref } from 'vue';

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

import { queryJobPageApi, updateJobEnabledApi } from '#/api';
import ProTable from '#/components/pro-table/index.vue';

import JobExecuteModalView from './components/job-execute-modal.vue';
import JobFormModalView from './components/job-form-modal.vue';
import JobLogDrawerView from './components/job-log-drawer.vue';

defineOptions({
  name: 'SupportJobList',
});

const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const jobFormModalRef = ref<InstanceType<typeof JobFormModal>>();
const jobExecuteModalRef = ref<InstanceType<typeof JobExecuteModal>>();
const jobLogDrawerRef = ref<InstanceType<typeof JobLogDrawer>>();

const triggerTypeOptions = [
  { label: 'cron 表达式', value: 'cron' },
  { label: '固定间隔', value: 'fixed_delay' },
];

const enabledFlagOptions = [
  { label: '启用', tagType: 'success', value: true },
  { label: '禁用', tagType: 'info', value: false },
];

const tableColumns = reactive<ColumnProps<SupportJobApi.JobItem>[]>([
  {
    label: '任务名称',
    minWidth: 180,
    prop: 'jobName',
    search: {
      el: 'input',
      key: 'searchWord',
      label: '关键字',
      props: {
        placeholder: '请输入任务名称或执行类',
      },
    },
  },
  {
    label: '执行类',
    minWidth: 260,
    prop: 'jobClass',
  },
  {
    enum: triggerTypeOptions,
    label: '触发类型',
    minWidth: 120,
    prop: 'triggerType',
    search: {
      el: 'select',
      label: '触发类型',
    },
  },
  {
    label: '触发配置',
    minWidth: 180,
    prop: 'triggerValue',
  },
  {
    label: '任务参数',
    minWidth: 220,
    prop: 'param',
  },
  {
    enum: enabledFlagOptions,
    label: '启用状态',
    minWidth: 110,
    prop: 'enabledFlag',
    search: {
      el: 'select',
      label: '启用状态',
    },
  },
  {
    label: '最后执行时间',
    minWidth: 180,
    prop: 'lastExecuteTime',
  },
  {
    label: '排序',
    minWidth: 90,
    prop: 'sort',
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
    width: 260,
  },
]);

const hasAddPermission = computed(() => {
  return accessStore.accessCodes.includes('support:job:add');
});

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('support:job:query');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('support:job:update');
});

const hasExecutePermission = computed(() => {
  return accessStore.accessCodes.includes('support:job:execute');
});

const hasLogQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('support:job:log:query');
});

async function queryJobTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    return {
      list: [],
      total: 0,
    };
  }

  return queryJobPageApi({
    enabledFlag: params.enabledFlag,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    searchCount: true,
    searchWord: params.searchWord?.trim() || undefined,
    triggerType: params.triggerType || undefined,
  });
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

function getTriggerTypeText(triggerType?: string) {
  return (
    triggerTypeOptions.find((item) => item.value === triggerType)?.label ??
    triggerType ??
    '--'
  );
}

function getEnabledText(enabledFlag?: boolean) {
  return enabledFlag ? '启用' : '禁用';
}

function getEnabledTagType(enabledFlag?: boolean) {
  return enabledFlag ? 'success' : 'info';
}

function handleSearch() {
  if (!hasQueryPermission.value) {
    ElMessage.warning('当前账号没有定时任务查询权限');
  }
}

function openCreateModal() {
  jobFormModalRef.value?.openCreate();
}

function openEditModal(row: SupportJobApi.JobItem) {
  jobFormModalRef.value?.openEdit(row);
}

function openExecuteModal(row: SupportJobApi.JobItem) {
  jobExecuteModalRef.value?.open(row);
}

function openLogDrawer(row: SupportJobApi.JobItem) {
  jobLogDrawerRef.value?.open(row);
}

async function handleToggleEnabled(row: SupportJobApi.JobItem) {
  const nextEnabledFlag = !row.enabledFlag;
  await ElMessageBox.confirm(
    `确定要${nextEnabledFlag ? '启用' : '禁用'}任务“${row.jobName ?? row.jobId}”吗？`,
    '启停确认',
    {
      type: 'warning',
    },
  );

  await updateJobEnabledApi({
    enabledFlag: nextEnabledFlag,
    jobId: row.jobId,
  });
  ElMessage.success(`任务已${nextEnabledFlag ? '启用' : '禁用'}`);
  await reloadTable();
}
</script>

<template>
  <Page auto-content-height class="job-page">
    <div class="job-page__content">
      <ElAlert
        v-if="!hasQueryPermission"
        :closable="false"
        class="job-page__alert"
        show-icon
        title="当前账号没有定时任务查询权限"
        type="warning"
      />

      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryJobTable"
        row-key="jobId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
        @search="handleSearch"
      >
        <template #tableHeader>
          <div class="job-page__toolbar">
            <ElButton
              v-if="hasAddPermission"
              :icon="Plus"
              type="primary"
              @click="openCreateModal"
            >
              新增任务
            </ElButton>
          </div>
        </template>

          <template #triggerType="{ row }">
            {{ getTriggerTypeText(row.triggerType) }}
          </template>

          <template #enabledFlag="{ row }">
            <ElTag :type="getEnabledTagType(row.enabledFlag)">
              {{ getEnabledText(row.enabledFlag) }}
            </ElTag>
          </template>

          <template #operation="{ row }">
            <ElButton
              v-if="hasUpdatePermission"
              link
              type="primary"
              @click="openEditModal(row)"
            >
              编辑
            </ElButton>
            <ElButton
              v-if="hasUpdatePermission"
              link
              type="primary"
              @click="handleToggleEnabled(row)"
            >
              {{ row.enabledFlag ? '禁用' : '启用' }}
            </ElButton>
            <ElButton
              v-if="hasExecutePermission"
              link
              type="primary"
              @click="openExecuteModal(row)"
            >
              执行
            </ElButton>
            <ElButton
              v-if="hasLogQueryPermission"
              link
              type="primary"
              @click="openLogDrawer(row)"
            >
              日志
            </ElButton>
          </template>
        </ProTable>
    </div>

    <JobFormModalView ref="jobFormModalRef" @success="reloadTable" />
    <JobExecuteModalView ref="jobExecuteModalRef" @success="reloadTable" />
    <JobLogDrawerView ref="jobLogDrawerRef" />
  </Page>
</template>

<style scoped>
.job-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.job-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.job-page__alert {
  margin-bottom: 4px;
}
</style>
