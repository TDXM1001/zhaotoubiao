<script lang="ts" setup>
import type DepartmentFormModal from './components/department-form-modal.vue';

import type { SystemDepartmentApi, SystemEmployeeApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, onMounted, reactive, ref } from 'vue';

import { Page, VbenIcon } from '@vben/common-ui';
import { Plus } from '@vben/icons';
import { useAccessStore } from '@vben/stores';

import { Delete } from '@element-plus/icons-vue';
import {
  ElButton,
  ElMessage,
  ElMessageBox,
} from 'element-plus';

import {
  batchDeleteDepartmentApi,
  deleteDepartmentApi,
  listDepartmentOptionsApi,
  queryAllEmployeeApi,
  queryDepartmentTreeApi,
} from '#/api';
import ProTable from '#/components/pro-table/index.vue';

import DepartmentFormModalView from './components/department-form-modal.vue';

defineOptions({
  name: 'SystemDepartmentList',
});

const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const departmentFormModalRef = ref<InstanceType<typeof DepartmentFormModal>>();

const departmentTree = ref<SystemDepartmentApi.DepartmentItem[]>([]);
const employeeOptions = ref<SystemEmployeeApi.EmployeeOption[]>([]);

const tableColumns = reactive<ColumnProps<SystemDepartmentApi.DepartmentItem>[]>([
  {
    type: 'selection',
    width: 56,
  },
  {
    label: '部门名称',
    minWidth: 220,
    prop: 'departmentName',
    search: {
      el: 'input',
      key: 'keywords',
      label: '关键字',
      props: {
        placeholder: '请输入部门名称',
      },
    },
  },
  {
    label: '负责人',
    minWidth: 150,
    prop: 'managerName',
  },
  {
    label: '排序',
    minWidth: 100,
    prop: 'sort',
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
    width: 220,
  },
]);

const hasAddPermission = computed(() => {
  return accessStore.accessCodes.includes('system:department:add');
});

const hasDeletePermission = computed(() => {
  return accessStore.accessCodes.includes('system:department:delete');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('system:department:update');
});

const selectedRows = computed(() => {
  return (proTable.value?.selectedList ?? []) as SystemDepartmentApi.DepartmentItem[];
});

const hasSelected = computed(() => selectedRows.value.length > 0);

async function queryDepartmentTable(params: any = {}) {
  const resp = await queryDepartmentTreeApi({
    keywords: params.keywords?.trim() || undefined,
  });
  const items = Array.isArray(resp) ? resp : (resp as any)?.data ?? [];
  departmentTree.value = items;
  return items;
}

async function loadEmployeeOptions() {
  const employeeList = await queryAllEmployeeApi();
  employeeOptions.value = (employeeList ?? []).filter(
    (item) => !item.disabledFlag,
  );
}

async function loadDepartmentOptions() {
  // 预取部门全量列表，确保与后端契约保持一致，页面当前直接复用树数据渲染选项。
  await listDepartmentOptionsApi();
}

async function loadPageDependencies() {
  await Promise.all([loadEmployeeOptions(), loadDepartmentOptions()]);
}

async function handleRefresh() {
  proTable.value?.clearSelection();
  await proTable.value?.getTableList();
}

function openCreateModal() {
  departmentFormModalRef.value?.openCreate();
}

function openCreateChildModal(row: SystemDepartmentApi.DepartmentItem) {
  departmentFormModalRef.value?.openCreateChild(row);
}

function openEditModal(row: SystemDepartmentApi.DepartmentItem) {
  departmentFormModalRef.value?.openEdit(row);
}

async function handleDelete(rows: SystemDepartmentApi.DepartmentItem[]) {
  const names = rows.map((item) => item.departmentName).join('、');

  await ElMessageBox.confirm(
    `确定要删除部门“${names}”吗？`,
    '删除确认',
    {
      type: 'warning',
    },
  );

  await batchDeleteDepartmentApi(rows.map((item) => item.departmentId));
  ElMessage.success(rows.length > 1 ? '部门批量删除成功' : '部门删除成功');
  await handleRefresh();
}

async function handleBatchDelete() {
  if (selectedRows.value.length === 0) {
    return;
  }
  await handleDelete(selectedRows.value);
}

onMounted(async () => {
  await loadPageDependencies();
});
</script>

<template>
  <Page auto-content-height class="department-page">
    <div class="department-page__content">
      <ProTable
        ref="proTable"
        :columns="tableColumns"
        default-expand-all
        :pagination="false"
        :request-api="queryDepartmentTable"
        row-key="departmentId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
        :tree-props="{ children: 'children' }"
      >
        <template #tableHeader>
          <div class="department-page__toolbar">
            <ElButton
              v-if="hasAddPermission"
              :icon="Plus"
              type="primary"
              @click="openCreateModal"
            >
              新增部门
            </ElButton>
            <ElButton
              v-if="hasDeletePermission"
              :disabled="!hasSelected"
              :icon="Delete"
              @click="handleBatchDelete"
            >
              批量删除
            </ElButton>
          </div>
        </template>
 
        <template #operation="{ row }">
          <ElButton
            v-if="hasAddPermission"
            link
            type="primary"
            @click="openCreateChildModal(row)"
          >
            新增下级
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
            @click="handleDelete([row])"
          >
            删除
          </ElButton>
        </template>
      </ProTable>
    </div>
 
    <DepartmentFormModalView
      ref="departmentFormModalRef"
      :department-tree="departmentTree"
      :employee-options="employeeOptions"
      @success="handleRefresh"
    />
  </Page>
</template>

<style scoped>
.department-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.department-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
</style>
