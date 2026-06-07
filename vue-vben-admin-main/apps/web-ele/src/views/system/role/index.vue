<script lang="ts" setup>
import type { SystemRoleApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';
import type RoleDataScopeDrawer from './components/role-data-scope-drawer.vue';
import type RoleEmployeeDrawer from './components/role-employee-drawer.vue';
import type RoleFormModal from './components/role-form-modal.vue';
import type RoleMenuDrawer from './components/role-menu-drawer.vue';

import { computed, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import { Plus } from '@vben/icons';

import {
  ElButton,
  ElMessage,
  ElMessageBox,
} from 'element-plus';

import { deleteRoleApi, getAllRoleApi } from '#/api';
import ProTable from '#/components/pro-table/index.vue';

import RoleDataScopeDrawerView from './components/role-data-scope-drawer.vue';
import RoleEmployeeDrawerView from './components/role-employee-drawer.vue';
import RoleFormModalView from './components/role-form-modal.vue';
import RoleMenuDrawerView from './components/role-menu-drawer.vue';

defineOptions({
  name: 'SystemRoleIndex',
});

const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const roleFormModalRef = ref<InstanceType<typeof RoleFormModal>>();
const roleMenuDrawerRef = ref<InstanceType<typeof RoleMenuDrawer>>();
const roleDataScopeDrawerRef = ref<InstanceType<typeof RoleDataScopeDrawer>>();
const roleEmployeeDrawerRef = ref<InstanceType<typeof RoleEmployeeDrawer>>();
const roleList = ref<SystemRoleApi.RoleItem[]>([]);

const tableColumns = reactive<ColumnProps<SystemRoleApi.RoleItem>[]>([
  {
    label: '角色名称',
    minWidth: 180,
    prop: 'roleName',
    search: {
      el: 'input',
      label: '角色名称',
      props: {
        placeholder: '请输入角色名称',
      },
    },
  },
  {
    label: '角色编码',
    minWidth: 180,
    prop: 'roleCode',
    search: {
      el: 'input',
      label: '角色编码',
      props: {
        placeholder: '请输入角色编码',
      },
    },
  },
  {
    label: '备注',
    minWidth: 220,
    prop: 'remark',
  },
  {
    fixed: 'right',
    label: '操作',
    prop: 'operation',
    width: 360,
  },
]);

const hasAddPermission = computed(() => {
  return accessStore.accessCodes.includes('system:role:add');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('system:role:update');
});

const hasDeletePermission = computed(() => {
  return accessStore.accessCodes.includes('system:role:delete');
});

const hasDataScopePermission = computed(() => {
  return accessStore.accessCodes.includes('system:role:dataScope:update');
});

const hasEmployeePermission = computed(() => {
  return (
    accessStore.accessCodes.includes('system:role:employee:add') ||
    accessStore.accessCodes.includes('system:role:employee:delete') ||
    accessStore.accessCodes.includes('system:role:employee:batch:delete')
  );
});

const hasMenuPermission = computed(() => {
  return accessStore.accessCodes.includes('system:role:menu:update');
});

function filterRoleList(
  items: SystemRoleApi.RoleItem[],
  filters: SystemRoleApi.RoleFilterParams,
) {
  const roleName = filters.roleName?.trim().toLowerCase();
  const roleCode = filters.roleCode?.trim().toLowerCase();

  return items.filter((item) => {
    const matchRoleName = roleName
      ? item.roleName?.toLowerCase().includes(roleName)
      : true;
    const matchRoleCode = roleCode
      ? item.roleCode?.toLowerCase().includes(roleCode)
      : true;

    return matchRoleName && matchRoleCode;
  });
}

async function queryRoleTable(params: Record<string, any>) {
  const result = await getAllRoleApi();
  roleList.value = Array.isArray(result) ? result : [];

  // 角色接口是全量列表，保持原页面的前端过滤逻辑。
  return filterRoleList(roleList.value, {
    roleCode: params.roleCode,
    roleName: params.roleName,
  });
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

async function handleRefresh() {
  await reloadTable();
}

function openCreateModal() {
  roleFormModalRef.value?.openCreate();
}

function openEditModal(row: SystemRoleApi.RoleItem) {
  roleFormModalRef.value?.openEdit(row);
}

function openMenuDrawer(row: SystemRoleApi.RoleItem) {
  roleMenuDrawerRef.value?.open(row);
}

function openDataScopeDrawer(row: SystemRoleApi.RoleItem) {
  roleDataScopeDrawerRef.value?.open(row);
}

function openEmployeeDrawer(row: SystemRoleApi.RoleItem) {
  roleEmployeeDrawerRef.value?.open(row);
}

async function handleDelete(row: SystemRoleApi.RoleItem) {
  await ElMessageBox.confirm(
    `确定要删除角色“${row.roleName}”吗？`,
    '删除确认',
    {
      type: 'warning',
    },
  );

  await deleteRoleApi(row.roleId);
  ElMessage.success('角色删除成功');
  await handleRefresh();
}
</script>

<template>
  <Page auto-content-height class="role-page">
    <div class="role-page__content">
      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :pagination="false"
        :request-api="queryRoleTable"
        row-key="roleId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
      >
        <template #tableHeader>
          <div class="role-page__toolbar">
            <ElButton
              v-if="hasAddPermission"
              :icon="Plus"
              type="primary"
              @click="openCreateModal"
            >
              新增角色
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
              v-if="hasMenuPermission"
              link
              type="primary"
              @click="openMenuDrawer(row)"
            >
              菜单权限
            </ElButton>
            <ElButton
              v-if="hasDataScopePermission"
              link
              type="primary"
              @click="openDataScopeDrawer(row)"
            >
              数据范围
            </ElButton>
            <ElButton
              v-if="hasEmployeePermission"
              link
              type="primary"
              @click="openEmployeeDrawer(row)"
            >
              角色成员
            </ElButton>
            <ElButton
              v-if="hasDeletePermission"
              link
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </ElButton>
          </template>
        </ProTable>
      </div>

    <RoleFormModalView ref="roleFormModalRef" @success="handleRefresh" />
    <RoleMenuDrawerView ref="roleMenuDrawerRef" @success="handleRefresh" />
    <RoleDataScopeDrawerView
      ref="roleDataScopeDrawerRef"
      @success="handleRefresh"
    />
    <RoleEmployeeDrawerView ref="roleEmployeeDrawerRef" @success="handleRefresh" />
  </Page>
</template>

<style scoped>
.role-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.role-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
</style>
