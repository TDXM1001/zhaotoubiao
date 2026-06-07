<script lang="ts" setup>
import type DictDataDrawer from './components/dict-data-drawer.vue';
import type DictFormModal from './components/dict-form-modal.vue';

import type { SupportDictApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import { Plus } from '@vben/icons';

import {
  ElButton,
  ElMessage,
  ElMessageBox,
  ElTag,
} from 'element-plus';

import {
  deleteDictApi,
  queryDictPageApi,
  updateDictDisabledApi,
} from '#/api';
import ProTable from '#/components/pro-table/index.vue';

import DictDataDrawerView from './components/dict-data-drawer.vue';
import DictFormModalView from './components/dict-form-modal.vue';

defineOptions({
  name: 'SupportDictIndex',
});

const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const dictFormModalRef = ref<InstanceType<typeof DictFormModal>>();
const dictDataDrawerRef = ref<InstanceType<typeof DictDataDrawer>>();

const statusOptions = [
  { label: '已启用', tagType: 'success', value: false },
  { label: '已禁用', tagType: 'info', value: true },
];

const tableColumns = reactive<ColumnProps<SupportDictApi.DictItem>[]>([
  {
    label: '字典名称',
    minWidth: 150,
    prop: 'dictName',
    search: {
      el: 'input',
      key: 'keywords',
      label: '关键字',
      props: {
        placeholder: '请输入字典名称或编码',
      },
    },
  },
  {
    label: '字典编码',
    minWidth: 170,
    prop: 'dictCode',
  },
  {
    enum: statusOptions,
    fieldNames: {
      label: 'label',
      value: 'value',
    },
    label: '状态',
    minWidth: 100,
    prop: 'disabledFlag',
    search: {
      el: 'select',
      label: '状态',
    },
  },
  {
    label: '备注',
    minWidth: 180,
    prop: 'remark',
  },
  {
    label: '创建时间',
    minWidth: 160,
    prop: 'createTime',
  },
  {
    label: '更新时间',
    minWidth: 160,
    prop: 'updateTime',
  },
  {
    fixed: 'right',
    label: '操作',
    prop: 'operation',
    width: 240,
  },
]);

const hasAddPermission = computed(() => {
  return accessStore.accessCodes.includes('support:dict:add');
});

const hasDeletePermission = computed(() => {
  return accessStore.accessCodes.includes('support:dict:delete');
});

const hasDictDataPermission = computed(() => {
  return accessStore.accessCodes.includes('support:dictData:query');
});

const hasUpdateDisabledPermission = computed(() => {
  return accessStore.accessCodes.includes('support:dict:updateDisabled');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('support:dict:update');
});

async function queryDictTable(params: Record<string, any>) {
  return queryDictPageApi({
    disabledFlag: params.disabledFlag,
    keywords: params.keywords?.trim() || undefined,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    searchCount: true,
  });
}

function getStatusText(disabledFlag?: boolean) {
  return disabledFlag ? '已禁用' : '已启用';
}

async function refreshCurrentPage() {
  await proTable.value?.getTableList();
}

function openCreateModal() {
  dictFormModalRef.value?.openCreate();
}

function openEditModal(row: SupportDictApi.DictItem) {
  dictFormModalRef.value?.openEdit(row);
}

function openDictDataDrawer(row: SupportDictApi.DictItem) {
  dictDataDrawerRef.value?.open({
    dictCode: row.dictCode,
    dictId: row.dictId,
    dictName: row.dictName,
  });
}

async function handleToggle(row: SupportDictApi.DictItem) {
  const actionText = row.disabledFlag ? '启用' : '禁用';

  await ElMessageBox.confirm(
    `确定要${actionText}数据字典“${row.dictName}”吗？`,
    `${actionText}确认`,
    {
      type: 'warning',
    },
  );

  await updateDictDisabledApi(row.dictId);
  ElMessage.success(`数据字典${actionText}成功`);
  await refreshCurrentPage();
}

async function handleDelete(row: SupportDictApi.DictItem) {
  await ElMessageBox.confirm(
    `确定要删除数据字典“${row.dictName}”吗？`,
    '删除确认',
    {
      type: 'warning',
    },
  );

  await deleteDictApi(row.dictId);
  ElMessage.success('数据字典删除成功');
  await refreshCurrentPage();
}
</script>

<template>
  <Page auto-content-height class="dict-page">
    <div class="dict-page__content">
      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryDictTable"
        row-key="dictId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
      >
        <template #tableHeader>
          <div class="dict-page__toolbar">
            <ElButton
              v-if="hasAddPermission"
              :icon="Plus"
              type="primary"
              @click="openCreateModal"
            >
              新增字典
            </ElButton>
          </div>
        </template>

          <template #disabledFlag="{ row }">
            <ElTag :type="row.disabledFlag ? 'info' : 'success'">
              {{ getStatusText(row.disabledFlag) }}
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
              v-if="hasUpdateDisabledPermission"
              link
              type="primary"
              @click="handleToggle(row)"
            >
              {{ row.disabledFlag ? '启用' : '禁用' }}
            </ElButton>
            <ElButton
              v-if="hasDeletePermission"
              link
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </ElButton>
            <ElButton
              v-if="hasDictDataPermission"
              link
              type="primary"
              @click="openDictDataDrawer(row)"
            >
              字典项管理
            </ElButton>
          </template>
        </ProTable>
    </div>

    <DictFormModalView ref="dictFormModalRef" @success="refreshCurrentPage" />
    <DictDataDrawerView ref="dictDataDrawerRef" />
  </Page>
</template>

<style scoped>
.dict-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.dict-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
</style>
