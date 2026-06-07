<script lang="ts" setup>
import type OperateLogDetailDrawer from './components/operate-log-detail-drawer.vue';

import type { SupportOperateLogApi } from '#/api';
import type { ColumnProps } from '#/components/pro-table/types';

import { computed, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import { ElAlert, ElButton, ElMessage, ElTag } from 'element-plus';

import { queryOperateLogPageApi } from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';
import ProTable from '#/components/pro-table/index.vue';

import OperateLogDetailDrawerView from './components/operate-log-detail-drawer.vue';

defineOptions({
  name: 'SupportOperateLogList',
});

type OperateLogItem = SupportOperateLogApi.OperateLogItem;

const accessStore = useAccessStore();
const operateLogDetailDrawerRef =
  ref<InstanceType<typeof OperateLogDetailDrawer>>();
const { optionMap: userTypeOptionMap, options: userTypeOptions } =
  useDictOptions('USER_TYPE');

const successFlagOptions = [
  { label: '成功', tagType: 'success', value: true },
  { label: '失败', tagType: 'danger', value: false },
];

const tableColumns = reactive<ColumnProps<OperateLogItem>[]>([
  {
    label: '操作人',
    minWidth: 140,
    prop: 'operateUserName',
    search: {
      el: 'input',
      key: 'userName',
      label: '操作人',
      props: {
        placeholder: '请输入操作人',
      },
    },
  },
  {
    enum: userTypeOptions,
    label: '用户类型',
    minWidth: 110,
    prop: 'operateUserType',
    search: {
      el: 'select',
      label: '用户类型',
    },
  },
  {
    label: '模块',
    minWidth: 180,
    prop: 'module',
    search: {
      el: 'input',
      key: 'keywords',
      label: '业务关键字',
      props: {
        placeholder: '请输入模块或操作内容',
      },
    },
  },
  {
    label: '操作内容',
    minWidth: 220,
    prop: 'content',
  },
  {
    label: '请求地址',
    minWidth: 240,
    prop: 'url',
    search: {
      el: 'input',
      key: 'requestKeywords',
      label: '请求关键字',
      props: {
        placeholder: '请输入请求地址、方法或参数',
      },
    },
  },
  {
    label: '请求方法',
    minWidth: 120,
    prop: 'method',
  },
  {
    label: 'IP',
    minWidth: 140,
    prop: 'ip',
  },
  {
    label: 'IP 地区',
    minWidth: 180,
    prop: 'ipRegion',
  },
  {
    enum: successFlagOptions,
    label: '结果',
    minWidth: 100,
    prop: 'successFlag',
    search: {
      el: 'select',
      label: '请求结果',
    },
  },
  {
    label: '操作时间',
    minWidth: 180,
    prop: 'createTime',
    search: {
      el: 'date-picker',
      key: 'dateRange',
      label: '操作时间',
      props: {
        endPlaceholder: '结束日期',
        startPlaceholder: '开始日期',
        type: 'daterange',
        valueFormat: 'YYYY-MM-DD',
      },
    },
  },
  {
    fixed: 'right',
    label: '操作',
    prop: 'operation',
    width: 120,
  },
]);

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('support:operateLog:query');
});

const hasDetailPermission = computed(() => {
  return accessStore.accessCodes.includes('support:operateLog:detail');
});

async function queryOperateLogTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    return {
      list: [],
      total: 0,
    };
  }

  return queryOperateLogPageApi({
    endDate: params.dateRange?.[1],
    keywords: params.keywords?.trim() || undefined,
    // 保留旧页面的用户类型筛选，类型定义已补齐以匹配后端字段。
    operateUserType:
      params.operateUserType === '' || params.operateUserType === undefined
        ? undefined
        : Number(params.operateUserType),
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    requestKeywords: params.requestKeywords?.trim() || undefined,
    startDate: params.dateRange?.[0],
    successFlag: params.successFlag,
    userName: params.userName?.trim() || undefined,
  });
}

function getSuccessText(successFlag?: boolean) {
  return successFlag ? '成功' : '失败';
}

function getSuccessTagType(successFlag?: boolean) {
  return successFlag ? 'success' : 'danger';
}

function getUserTypeText(operateUserType?: number) {
  if (operateUserType === undefined || operateUserType === null) {
    return '--';
  }

  return (
    userTypeOptionMap.value.get(String(operateUserType))?.label ??
    `未知(${operateUserType})`
  );
}

function handleSearch() {
  if (!hasQueryPermission.value) {
    ElMessage.warning('当前账号没有操作日志查询权限');
  }
}

function handleOpenDetail(row: OperateLogItem) {
  operateLogDetailDrawerRef.value?.open(row.operateLogId);
}
</script>

<template>
  <Page auto-content-height class="operate-log-page">
    <div class="operate-log-page__content">
      <ElAlert
        v-if="!hasQueryPermission"
        :closable="false"
        class="operate-log-page__alert"
        show-icon
        title="当前账号没有操作日志查询权限"
        type="warning"
      />

      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryOperateLogTable"
        row-key="operateLogId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
        @search="handleSearch"
      >
          <template #operateUserType="{ row }">
            {{ getUserTypeText(row.operateUserType) }}
          </template>

          <template #successFlag="{ row }">
            <ElTag :type="getSuccessTagType(row.successFlag)">
              {{ getSuccessText(row.successFlag) }}
            </ElTag>
          </template>

          <template #operation="{ row }">
            <ElButton
              v-if="hasDetailPermission"
              link
              type="primary"
              @click="handleOpenDetail(row)"
            >
              详情
            </ElButton>
          </template>
        </ProTable>
    </div>

    <OperateLogDetailDrawerView ref="operateLogDetailDrawerRef" />
  </Page>
</template>

<style scoped>
.operate-log-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.operate-log-page__alert {
  margin-bottom: 4px;
}
</style>
