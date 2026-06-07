<script lang="ts" setup>
import type { SupportJobApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { nextTick, reactive, ref } from 'vue';

import { ElDrawer, ElTag } from 'element-plus';

import { queryJobLogPageApi } from '#/api';
import ProTable from '#/components/pro-table/index.vue';

defineOptions({
  name: 'SupportJobLogDrawer',
});

const visible = ref(false);
const proTable = ref<ProTableInstance>();
const currentJobId = ref<number>();
const currentJobName = ref('');

const successFlagOptions = [
  { label: '成功', tagType: 'success', value: true },
  { label: '失败', tagType: 'danger', value: false },
];

const tableColumns = reactive<ColumnProps<SupportJobApi.JobLogItem>[]>([
  {
    label: '任务名称',
    minWidth: 180,
    prop: 'jobName',
    search: {
      el: 'input',
      key: 'searchWord',
      label: '关键字',
      props: {
        placeholder: '请输入任务名称或执行结果',
      },
    },
  },
  {
    enum: successFlagOptions,
    label: '执行结果',
    minWidth: 110,
    prop: 'successFlag',
    search: {
      el: 'select',
      label: '执行结果',
    },
  },
  {
    label: '开始时间',
    minWidth: 180,
    prop: 'executeStartTime',
    search: {
      el: 'date-picker',
      key: 'dateRange',
      label: '执行日期',
      props: {
        endPlaceholder: '结束日期',
        startPlaceholder: '开始日期',
        type: 'daterange',
        valueFormat: 'YYYY-MM-DD',
      },
    },
  },
  {
    label: '结束时间',
    minWidth: 180,
    prop: 'executeEndTime',
  },
  {
    label: '耗时(ms)',
    minWidth: 110,
    prop: 'executeTimeMillis',
  },
  {
    label: '执行参数',
    minWidth: 220,
    prop: 'param',
  },
  {
    label: '执行结果描述',
    minWidth: 280,
    prop: 'executeResult',
  },
  {
    label: 'IP',
    minWidth: 140,
    prop: 'ip',
  },
  {
    label: '进程 ID',
    minWidth: 140,
    prop: 'processId',
  },
  {
    label: '程序目录',
    minWidth: 220,
    prop: 'programPath',
  },
  {
    label: '创建时间',
    minWidth: 180,
    prop: 'createTime',
  },
]);

async function queryJobLogTable(params: Record<string, any>) {
  return queryJobLogPageApi({
    endTime: params.dateRange?.[1],
    jobId: currentJobId.value,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    searchCount: true,
    searchWord: params.searchWord?.trim() || undefined,
    startTime: params.dateRange?.[0],
    successFlag: params.successFlag,
  });
}

function getSuccessText(successFlag?: boolean) {
  return successFlag ? '成功' : '失败';
}

function getSuccessTagType(successFlag?: boolean) {
  return successFlag ? 'success' : 'danger';
}

async function open(row: SupportJobApi.JobItem) {
  currentJobId.value = row.jobId;
  currentJobName.value = row.jobName ?? `任务 ${row.jobId}`;
  visible.value = true;
  await nextTick();
  await proTable.value?.getTableList();
}

defineExpose({
  open,
});
</script>

<template>
  <ElDrawer
    v-model="visible"
    :title="`执行日志 - ${currentJobName}`"
    size="920px"
  >
    <div class="job-log-drawer">
      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryJobLogTable"
        :request-auto="false"
        :search-col="{ lg: 2, md: 2, sm: 1, xl: 3, xs: 1 }"
        row-key="logId"
      >
        <template #successFlag="{ row }">
          <ElTag :type="getSuccessTagType(row.successFlag)">
            {{ getSuccessText(row.successFlag) }}
          </ElTag>
        </template>
      </ProTable>
    </div>
  </ElDrawer>
</template>

<style scoped>
.job-log-drawer {
  height: 100%;
}
</style>
