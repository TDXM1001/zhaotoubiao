<script lang="ts" setup>
import type PositionFormModal from './components/position-form-modal.vue';

import type { SystemPositionApi } from '#/api';
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
  ElButton,
  ElMessage,
  ElMessageBox,
} from 'element-plus';

import {
  batchDeletePositionApi,
  deletePositionApi,
  queryPositionPageApi,
} from '#/api';
import ProTable from '#/components/pro-table/index.vue';

import PositionFormModalView from './components/position-form-modal.vue';

defineOptions({
  name: 'SystemPositionList',
});

const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const positionFormModalRef = ref<InstanceType<typeof PositionFormModal>>();

const tableColumns = reactive<ColumnProps<SystemPositionApi.PositionItem>[]>([
  {
    type: 'selection',
    width: 56,
  },
  {
    label: '职务名称',
    minWidth: 180,
    prop: 'positionName',
    search: {
      el: 'input',
      key: 'keywords',
      label: '关键字',
      props: {
        placeholder: '请输入职务名称/职级',
      },
    },
  },
  {
    label: '职级',
    minWidth: 140,
    prop: 'positionLevel',
  },
  {
    label: '排序',
    minWidth: 100,
    prop: 'sort',
  },
  {
    label: '备注',
    minWidth: 220,
    prop: 'remark',
  },
  {
    label: '创建时间',
    minWidth: 180,
    prop: 'createTime',
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
    width: 180,
  },
]);

const hasAddPermission = computed(() => {
  return accessStore.accessCodes.includes('system:position:add');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('system:position:update');
});

const hasDeletePermission = computed(() => {
  return accessStore.accessCodes.includes('system:position:delete');
});

const selectedPositionIds = computed(() => {
  return (proTable.value?.selectedListIds ?? []) as number[];
});

const selectedCount = computed(() => selectedPositionIds.value.length);

async function queryPositionTable(params: Record<string, any>) {
  const resp = await queryPositionPageApi({
    keywords: params.keywords?.trim() || undefined,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    searchCount: true,
  });
  return resp;
}

function clearSelection() {
  proTable.value?.clearSelection();
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

async function handleRefresh() {
  await reloadTable();
}

function handleSearch() {
  clearSelection();
}

function handleReset() {
  clearSelection();
}

function openCreateModal() {
  positionFormModalRef.value?.openCreate();
}

function openEditModal(row: SystemPositionApi.PositionItem) {
  positionFormModalRef.value?.openEdit(row);
}

async function handleDeleteRow(row: SystemPositionApi.PositionItem) {
  await ElMessageBox.confirm(
    `确定要删除职务“${row.positionName}”吗？`,
    '删除确认',
    {
      type: 'warning',
    },
  );

  await deletePositionApi(row.positionId);
  ElMessage.success('职务删除成功');
  clearSelection();
  await reloadTable();
}

async function handleBatchDelete() {
  if (selectedPositionIds.value.length === 0) {
    ElMessage.warning('请先选择职务');
    return;
  }

  await ElMessageBox.confirm(
    `确定要批量删除选中的 ${selectedPositionIds.value.length} 个职务吗？`,
    '批量删除确认',
    {
      type: 'warning',
    },
  );

  await batchDeletePositionApi(selectedPositionIds.value);
  ElMessage.success('职务批量删除成功');
  clearSelection();
  await reloadTable();
}
</script>

<template>
  <Page auto-content-height class="position-page">
    <div class="position-page__content">
      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryPositionTable"
        row-key="positionId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
        @reset="handleReset"
        @search="handleSearch"
      >
        <template #tableHeader>
          <div class="position-page__toolbar">
            <ElButton
              v-if="hasAddPermission"
              :icon="Plus"
              type="primary"
              @click="openCreateModal"
            >
              新增职务
            </ElButton>
            <ElButton
              v-if="hasDeletePermission"
              :disabled="selectedCount === 0"
              :icon="Delete"
              @click="handleBatchDelete"
            >
              批量删除
            </ElButton>
          </div>
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

    <PositionFormModalView
      ref="positionFormModalRef"
      @success="handleRefresh"
    />
  </Page>
</template>

<style scoped>
.position-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.position-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
</style>
