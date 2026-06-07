<script lang="ts" setup>
import type BatchDepartmentModal from './components/batch-department-modal.vue';
import type EmployeeFormModal from './components/employee-form-modal.vue';
import type ResetPasswordDialog from './components/reset-password-dialog.vue';

import type {
  SystemDepartmentApi,
  SystemEmployeeApi,
  SystemPositionApi,
} from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import {
  ElButton,
  ElCard,
  ElMessage,
  ElMessageBox,
  ElSpace,
  ElTag,
} from 'element-plus';

import {
  batchDeleteEmployeeApi,
  getAllRoleApi,
  listDepartmentOptionsApi,
  queryDepartmentTreeApi,
  queryEmployeePageApi,
  queryPositionListApi,
  updateEmployeeDisabledApi,
} from '#/api';
import ProTable from '#/components/pro-table/index.vue';
import TreeFilter from '#/components/tree-filter/index.vue';

import BatchDepartmentModalView from './components/batch-department-modal.vue';
import EmployeeFormModalView from './components/employee-form-modal.vue';
import ResetPasswordDialogView from './components/reset-password-dialog.vue';

defineOptions({
  name: 'SystemEmployeeIndex',
});

interface DepartmentTreeSelectOption {
  children?: DepartmentTreeSelectOption[];
  label: string;
  value: number;
}

interface DepartmentTreeNode extends SystemDepartmentApi.DepartmentItem {
  children?: DepartmentTreeNode[];
}

const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const employeeFormModalRef = ref<InstanceType<typeof EmployeeFormModal>>();
const batchDepartmentModalRef = ref<InstanceType<typeof BatchDepartmentModal>>();
const resetPasswordDialogRef = ref<InstanceType<typeof ResetPasswordDialog>>();

const initParam = reactive<{
  departmentId?: number;
}>({});

const departmentTree = ref<DepartmentTreeNode[]>([]);
const departmentOptions = ref<DepartmentTreeSelectOption[]>([]);
const roleOptions = ref<SystemEmployeeApi.RoleOption[]>([]);
const positionOptions = ref<SystemPositionApi.PositionItem[]>([]);

const statusOptions = [
  { label: '启用', tagType: 'success', value: false },
  { label: '禁用', tagType: 'danger', value: true },
];

const hasAddPermission = computed(() => {
  return accessStore.accessCodes.includes('system:employee:add');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('system:employee:update');
});

const hasDisabledPermission = computed(() => {
  return accessStore.accessCodes.includes('system:employee:disabled');
});

const hasDepartmentUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('system:employee:department:update');
});

const hasResetPasswordPermission = computed(() => {
  return accessStore.accessCodes.includes('system:employee:password:reset');
});

const hasDeletePermission = computed(() => {
  return accessStore.accessCodes.includes('system:employee:delete');
});

const tableColumns = reactive<ColumnProps<SystemEmployeeApi.EmployeeItem>[]>([
  {
    type: 'selection',
    width: 56,
  },
  {
    label: '员工姓名',
    minWidth: 140,
    prop: 'actualName',
    search: {
      el: 'input',
      key: 'keyword',
      label: '关键字',
      props: {
        placeholder: '请输入姓名/账号/手机',
      },
    },
  },
  {
    label: '登录账号',
    minWidth: 160,
    prop: 'loginName',
  },
  {
    label: '手机号',
    minWidth: 140,
    prop: 'phone',
  },
  {
    label: '邮箱',
    minWidth: 200,
    prop: 'email',
  },
  {
    label: '部门',
    minWidth: 220,
    prop: 'departmentName',
  },
  {
    label: '职务',
    minWidth: 140,
    prop: 'positionName',
  },
  {
    label: '角色',
    minWidth: 220,
    prop: 'roleNameList',
  },
  {
    enum: statusOptions,
    fieldNames: {
      label: 'label',
      value: 'value',
    },
    label: '状态',
    minWidth: 110,
    prop: 'disabledFlag',
    search: {
      el: 'select',
      label: '状态',
    },
  },
  {
    label: '创建时间',
    minWidth: 180,
    prop: 'createTime',
  },
  {
    fixed: 'right',
    label: '操作',
    prop: 'operation',
    width: 280,
  },
]);

const selectedEmployeeIds = computed(() => {
  return (proTable.value?.selectedListIds ?? []) as number[];
});

const selectedCount = computed(() => selectedEmployeeIds.value.length);

function mapDepartmentOptions(
  items: DepartmentTreeNode[],
): DepartmentTreeSelectOption[] {
  return items.map((item) => ({
    children: item.children?.length
      ? mapDepartmentOptions(item.children)
      : undefined,
    label: item.departmentName,
    value: item.departmentId,
  }));
}

function normalizeDepartmentTree(
  items: SystemDepartmentApi.DepartmentItem[],
): DepartmentTreeNode[] {
  return items.map((item) => ({
    ...item,
    children: item.children?.length
      ? normalizeDepartmentTree(item.children)
      : undefined,
  }));
}

async function queryEmployeeTable(params: Record<string, any>) {
  return queryEmployeePageApi({
    departmentId: params.departmentId,
    disabledFlag: params.disabledFlag,
    keyword: params.keyword?.trim() || undefined,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    searchCount: true,
  });
}

function clearSelection() {
  proTable.value?.clearSelection();
}

async function loadDepartmentData() {
  const [treeResp] = await Promise.all([
    queryDepartmentTreeApi(),
    listDepartmentOptionsApi(),
  ]);

  const treeItems = normalizeDepartmentTree(
    Array.isArray(treeResp) ? treeResp : [],
  );
  departmentTree.value = treeItems;
  departmentOptions.value = mapDepartmentOptions(treeItems);
}

async function loadRoleOptions() {
  roleOptions.value = await getAllRoleApi();
}

async function loadPositionOptions() {
  positionOptions.value = await queryPositionListApi();
}

async function loadPageDependencies() {
  await Promise.all([
    loadDepartmentData(),
    loadRoleOptions(),
    loadPositionOptions(),
  ]);
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
  initParam.departmentId = undefined;
  clearSelection();
}

async function handleDepartmentChange(value: number | string) {
  initParam.departmentId = value === '' ? undefined : Number(value);
  clearSelection();
  await proTable.value?.search();
}

function openCreateModal() {
  employeeFormModalRef.value?.openCreate();
}

function openEditModal(row: SystemEmployeeApi.EmployeeItem) {
  employeeFormModalRef.value?.openEdit(row);
}

async function handleToggleDisabled(row: SystemEmployeeApi.EmployeeItem) {
  const actionText = row.disabledFlag ? '启用' : '禁用';

  await ElMessageBox.confirm(
    `确定要${actionText}员工“${row.actualName}”吗？`,
    `${actionText}确认`,
    {
      type: 'warning',
    },
  );

  await updateEmployeeDisabledApi(row.employeeId);
  ElMessage.success(`员工${actionText}成功`);
  await reloadTable();
}

function openResetPasswordDialog(row: SystemEmployeeApi.EmployeeItem) {
  resetPasswordDialogRef.value?.open(row.employeeId, row.actualName);
}

async function handleDeleteEmployees(
  employeeIds: number[],
  successMessage: string,
) {
  await batchDeleteEmployeeApi(employeeIds);
  ElMessage.success(successMessage);
  clearSelection();
  await reloadTable();
}

async function handleDeleteRow(row: SystemEmployeeApi.EmployeeItem) {
  await ElMessageBox.confirm(
    `确定要删除员工“${row.actualName}”吗？`,
    '删除确认',
    {
      type: 'warning',
    },
  );

  await handleDeleteEmployees([row.employeeId], '员工删除成功');
}

function ensureSelectedEmployeeIds() {
  if (selectedEmployeeIds.value.length > 0) {
    return true;
  }

  ElMessage.warning('请先选择员工');
  return false;
}

function openBatchDepartmentModal() {
  if (!ensureSelectedEmployeeIds()) {
    return;
  }

  batchDepartmentModalRef.value?.open(selectedEmployeeIds.value);
}

async function handleBatchDelete() {
  if (!ensureSelectedEmployeeIds()) {
    return;
  }

  await ElMessageBox.confirm(
    `确定要删除选中的 ${selectedEmployeeIds.value.length} 名员工吗？`,
    '批量删除确认',
    {
      type: 'warning',
    },
  );

  await handleDeleteEmployees(selectedEmployeeIds.value, '批量删除员工成功');
}

function getRoleNames(row: SystemEmployeeApi.EmployeeItem) {
  return row.roleNameList?.join('、') || '--';
}

onMounted(async () => {
  await loadPageDependencies();
});
</script>

<template>
  <Page auto-content-height class="employee-page">
    <div class="employee-page__content">
      <TreeFilter
        :data="departmentTree"
        :default-value="initParam.departmentId"
        id="departmentId"
        label="departmentName"
        title="部门筛选"
        @change="handleDepartmentChange"
      />

      <div class="employee-page__main">
        <ElCard class="employee-page__table-card" shadow="never">
          <ProTable
            ref="proTable"
            :columns="tableColumns"
            :init-param="initParam"
            :request-api="queryEmployeeTable"
            :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
            row-key="employeeId"
            @reset="handleReset"
            @search="handleSearch"
          >
            <template #tableHeader>
              <div class="employee-page__toolbar">
                <ElSpace wrap>
                  <ElButton
                    v-if="hasAddPermission"
                    round
                    type="primary"
                    @click="openCreateModal"
                  >
                    新增员工
                  </ElButton>
                  <ElButton
                    v-if="hasDepartmentUpdatePermission"
                    :disabled="selectedCount === 0"
                    @click="openBatchDepartmentModal"
                  >
                    批量调部门
                  </ElButton>
                  <ElButton
                    v-if="hasDeletePermission"
                    :disabled="selectedCount === 0"
                    type="danger"
                    @click="handleBatchDelete"
                  >
                    批量删除
                  </ElButton>
                </ElSpace>

                <span v-if="selectedCount > 0" class="employee-page__selected">
                  已选 {{ selectedCount }} 人
                </span>
              </div>
            </template>

            <template #roleNameList="{ row }">
              <span>{{ getRoleNames(row) }}</span>
            </template>

            <template #disabledFlag="{ row }">
              <ElTag :type="row.disabledFlag ? 'danger' : 'success'">
                {{ row.disabledFlag ? '禁用' : '启用' }}
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
                v-if="hasDisabledPermission"
                link
                type="warning"
                @click="handleToggleDisabled(row)"
              >
                {{ row.disabledFlag ? '启用' : '禁用' }}
              </ElButton>
              <ElButton
                v-if="hasResetPasswordPermission"
                link
                type="primary"
                @click="openResetPasswordDialog(row)"
              >
                重置密码
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
        </ElCard>
      </div>
    </div>

    <EmployeeFormModalView
      ref="employeeFormModalRef"
      :department-options="departmentOptions"
      :position-options="positionOptions"
      :role-options="roleOptions"
      @success="handleRefresh"
    />
    <BatchDepartmentModalView
      ref="batchDepartmentModalRef"
      :department-options="departmentOptions"
      @success="handleRefresh"
    />
    <ResetPasswordDialogView
      ref="resetPasswordDialogRef"
      @success="handleRefresh"
    />
  </Page>
</template>

<style scoped>
.employee-page__content {
  display: grid;
  gap: 16px;
  grid-template-columns: 280px minmax(0, 1fr);
  height: 100%;
  /* padding: 16px; */
}

.employee-page__main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
}

.employee-page__table-card {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  border: none;
}

.employee-page__table-card :deep(.el-card__body) {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.employee-page__toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.employee-page__selected {
  font-size: 13px;
  color: var(--el-color-primary);
  white-space: nowrap;
}

@media (max-width: 1200px) {
  .employee-page__content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {
  .employee-page__toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
