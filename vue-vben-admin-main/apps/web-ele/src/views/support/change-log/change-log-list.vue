<script lang="ts" setup>
import type ChangeLogFormModal from './components/change-log-form-modal.vue';

import type { SupportChangeLogApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import { Plus } from '@vben/icons';

import { Delete } from '@element-plus/icons-vue';
import {
  ElAlert,
  ElButton,
  ElMessage,
  ElMessageBox,
  ElTag,
} from 'element-plus';

import {
  batchDeleteChangeLogApi,
  deleteChangeLogApi,
  queryChangeLogPageApi,
} from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';
import ProTable from '#/components/pro-table/index.vue';

import ChangeLogFormModalView from './components/change-log-form-modal.vue';

defineOptions({
  name: 'SupportChangeLogList',
});

const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const changeLogFormModalRef = ref<InstanceType<typeof ChangeLogFormModal>>();

const { options: typeOptions, optionMap: typeOptionMap } =
  useDictOptions('CHANGE_LOG_TYPE');

const tableColumns = reactive<ColumnProps<SupportChangeLogApi.ChangeLogItem>[]>([
  {
    type: 'selection',
    width: 56,
  },
  {
    label: '版本号',
    minWidth: 140,
    prop: 'updateVersion',
    search: {
      el: 'input',
      key: 'keyword',
      label: '关键字',
      props: {
        placeholder: '请输入版本号、发布人或内容关键字',
      },
    },
  },
  {
    enum: typeOptions,
    label: '更新类型',
    minWidth: 120,
    prop: 'type',
    search: {
      el: 'select',
      label: '更新类型',
    },
  },
  {
    label: '发布人',
    minWidth: 140,
    prop: 'publishAuthor',
  },
  {
    label: '发布日期',
    minWidth: 140,
    prop: 'publicDate',
    search: {
      el: 'date-picker',
      key: 'publicDateRange',
      label: '发布日期',
      props: {
        endPlaceholder: '结束日期',
        startPlaceholder: '开始日期',
        type: 'daterange',
        valueFormat: 'YYYY-MM-DD',
      },
    },
  },
  {
    label: '更新内容',
    minWidth: 320,
    prop: 'content',
  },
  {
    label: '跳转链接',
    minWidth: 220,
    prop: 'link',
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
    width: 220,
  },
]);

const hasAddPermission = computed(() => {
  return accessStore.accessCodes.includes('support:changeLog:add');
});

const hasBatchDeletePermission = computed(() => {
  return accessStore.accessCodes.includes('support:changeLog:batchDelete');
});

const hasDeletePermission = computed(() => {
  return accessStore.accessCodes.includes('support:changeLog:delete');
});

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('support:changeLog:query');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('support:changeLog:update');
});

const selectedChangeLogIds = computed<number[]>(() => {
  return ((proTable.value?.selectedListIds ?? []) as Array<number | string>).map(
    Number,
  );
});

const selectedCount = computed(() => selectedChangeLogIds.value.length);

async function queryChangeLogTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    clearSelection();
    return {
      list: [],
      total: 0,
    };
  }

  return queryChangeLogPageApi({
    keyword: params.keyword?.trim() || undefined,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    publicDateBegin: params.publicDateRange?.[0],
    publicDateEnd: params.publicDateRange?.[1],
    searchCount: true,
    // 字典值可能是字符串，也可能是数字，统一转成后端需要的 number。
    type:
      params.type === '' || params.type === undefined
        ? undefined
        : Number(params.type),
  });
}

function clearSelection() {
  proTable.value?.clearSelection();
}

function getTypeText(type?: number) {
  if (type === undefined || type === null) {
    return '--';
  }

  return typeOptionMap.value.get(String(type))?.label ?? `未知(${type})`;
}

function getTypeTagType(type?: number) {
  const style = typeOptionMap.value.get(String(type))?.style;
  const typeMap: Record<
    string,
    'danger' | 'info' | 'primary' | 'success' | 'warning' | undefined
  > = {
    danger: 'danger',
    default: undefined,
    info: 'info',
    primary: 'primary',
    success: 'success',
    warning: 'warning',
  };

  return typeMap[style ?? 'default'];
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

function handleSearch() {
  if (!hasQueryPermission.value) {
    ElMessage.warning('当前账号没有更新日志查询权限');
  }

  clearSelection();
}

function handleReset() {
  clearSelection();
}

async function handleRefresh() {
  await reloadTable();
}

function openCreateModal() {
  changeLogFormModalRef.value?.openCreate();
}

function openEditModal(row: SupportChangeLogApi.ChangeLogItem) {
  changeLogFormModalRef.value?.openEdit(row);
}

function handleOpenLink(row: SupportChangeLogApi.ChangeLogItem) {
  if (!row.link) {
    ElMessage.warning('当前记录没有配置跳转链接');
    return;
  }

  window.open(row.link, '_blank', 'noopener,noreferrer');
}

async function handleDeleteRow(row: SupportChangeLogApi.ChangeLogItem) {
  await ElMessageBox.confirm(
    `确定要删除更新日志“${row.updateVersion}”吗？`,
    '删除确认',
    {
      type: 'warning',
    },
  );

  await deleteChangeLogApi(row.changeLogId);
  ElMessage.success('更新日志删除成功');
  clearSelection();
  await reloadTable();
}

async function handleBatchDelete() {
  if (selectedChangeLogIds.value.length === 0) {
    ElMessage.warning('请先选择更新日志');
    return;
  }

  await ElMessageBox.confirm(
    `确定要批量删除选中的 ${selectedChangeLogIds.value.length} 条更新日志吗？`,
    '批量删除确认',
    {
      type: 'warning',
    },
  );

  await batchDeleteChangeLogApi(selectedChangeLogIds.value);
  ElMessage.success('更新日志批量删除成功');
  clearSelection();
  await reloadTable();
}
</script>

<template>
  <Page auto-content-height class="change-log-page">
    <div class="change-log-page__content">
      <ElAlert
        v-if="!hasQueryPermission"
        :closable="false"
        class="change-log-page__alert"
        show-icon
        title="当前账号没有更新日志查询权限"
        type="warning"
      />

      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryChangeLogTable"
        row-key="changeLogId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
        @reset="handleReset"
        @search="handleSearch"
      >
          <template #tableHeader>
            <div class="change-log-page__toolbar">
              <ElButton
                v-if="hasAddPermission"
                :icon="Plus"
                type="primary"
                @click="openCreateModal"
              >
                新增更新日志
              </ElButton>
              <ElButton
                v-if="hasBatchDeletePermission"
                :disabled="selectedCount === 0"
                :icon="Delete"
                @click="handleBatchDelete"
              >
                批量删除
              </ElButton>
            </div>
          </template>

          <template #type="{ row }">
            <ElTag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </ElTag>
          </template>

          <template #operation="{ row }">
            <ElButton
              v-if="row.link"
              link
              type="primary"
              @click="handleOpenLink(row)"
            >
              打开链接
            </ElButton>
            <ElButton
              v-if="hasUpdatePermission"
              link
              type="primary"
              @click="openEditModal(row)"
            >
              编辑
            </ElButton>
            <ElButton
              v-if="hasDeletePermission"
              link
              type="danger"
              @click="handleDeleteRow(row)"
            >
              删除
            </ElButton>
          </template>
        </ProTable>
    </div>

    <ChangeLogFormModalView
      ref="changeLogFormModalRef"
      @success="handleRefresh"
    />
  </Page>
</template>

<style scoped>
.change-log-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.change-log-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.change-log-page__alert {
  margin-bottom: 4px;
}
</style>
