<script lang="ts" setup>
import type { SystemRoleApi } from '#/api';
import type RoleEmployeeSelectModal from './role-employee-select-modal.vue';

import { computed, reactive, ref } from 'vue';

import {
  ElButton,
  ElDrawer,
  ElEmpty,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElPagination,
  ElSpace,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {
  batchRemoveRoleEmployeeApi,
  queryRoleEmployeePageApi,
  removeRoleEmployeeApi,
} from '#/api';

import RoleEmployeeSelectModalView from './role-employee-select-modal.vue';

defineOptions({
  name: 'SystemRoleEmployeeDrawer',
});

interface RoleEmployeeItem {
  actualName: string;
  createTime?: string;
  departmentId?: null | number;
  departmentName?: string;
  disabledFlag?: boolean;
  employeeId: number;
  loginName: string;
  phone?: string;
}

const emit = defineEmits<{
  success: [];
}>();

const employeeSelectModalRef = ref<InstanceType<typeof RoleEmployeeSelectModal>>();
const visible = ref(false);
const loading = ref(false);
const currentRoleId = ref<null | number>(null);
const currentRoleName = ref('');
const memberList = ref<RoleEmployeeItem[]>([]);
const total = ref(0);
const selectedEmployeeIds = ref<number[]>([]);

const queryForm = reactive({
  keywords: '',
  pageNum: 1,
  pageSize: 10,
});

const selectedCount = computed(() => selectedEmployeeIds.value.length);

const drawerTitle = computed(() => {
  return currentRoleName.value
    ? `角色成员 - ${currentRoleName.value}`
    : '角色成员';
});

function close() {
  visible.value = false;
}

async function loadMemberList() {
  if (!currentRoleId.value) {
    memberList.value = [];
    total.value = 0;
    return;
  }

  try {
    loading.value = true;
    const result = await queryRoleEmployeePageApi({
      keywords: queryForm.keywords.trim() || undefined,
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      roleId: currentRoleId.value,
      searchCount: true,
    });

    memberList.value = result.list ?? [];
    total.value = result.total ?? 0;
    const pageIds = new Set(memberList.value.map((item) => item.employeeId));
    selectedEmployeeIds.value = selectedEmployeeIds.value.filter((id) =>
      pageIds.has(id),
    );
  } finally {
    loading.value = false;
  }
}

async function open(role: Pick<SystemRoleApi.RoleItem, 'roleId' | 'roleName'>) {
  currentRoleId.value = role.roleId;
  currentRoleName.value = role.roleName;
  queryForm.keywords = '';
  queryForm.pageNum = 1;
  queryForm.pageSize = 10;
  selectedEmployeeIds.value = [];
  visible.value = true;
  await loadMemberList();
}

function handleSelectionChange(rows: RoleEmployeeItem[]) {
  selectedEmployeeIds.value = rows.map((item) => item.employeeId);
}

async function handleSearch() {
  queryForm.pageNum = 1;
  await loadMemberList();
}

async function handlePageChange(pageNum: number) {
  queryForm.pageNum = pageNum;
  await loadMemberList();
}

async function handlePageSizeChange(pageSize: number) {
  queryForm.pageSize = pageSize;
  queryForm.pageNum = 1;
  await loadMemberList();
}

async function handleRemove(row: RoleEmployeeItem) {
  if (!currentRoleId.value) {
    return;
  }

  await ElMessageBox.confirm(
    `确定要移除成员“${row.actualName}”吗？`,
    '移除确认',
    {
      type: 'warning',
    },
  );

  await removeRoleEmployeeApi(row.employeeId, currentRoleId.value);
  ElMessage.success('角色成员移除成功');
  await loadMemberList();
  emit('success');
}

async function handleBatchRemove() {
  if (!currentRoleId.value || selectedEmployeeIds.value.length === 0) {
    ElMessage.warning('请先选择角色成员');
    return;
  }

  await ElMessageBox.confirm(
    `确定要移除选中的 ${selectedEmployeeIds.value.length} 名角色成员吗？`,
    '批量移除确认',
    {
      type: 'warning',
    },
  );

  await batchRemoveRoleEmployeeApi({
    employeeIdList: selectedEmployeeIds.value,
    roleId: currentRoleId.value,
  });
  ElMessage.success('批量移除角色成员成功');
  selectedEmployeeIds.value = [];
  await loadMemberList();
  emit('success');
}

function openEmployeeSelectModal() {
  if (!currentRoleId.value) {
    return;
  }

  employeeSelectModalRef.value?.open({
    existingEmployeeIds: memberList.value.map((item) => item.employeeId),
    roleId: currentRoleId.value,
  });
}

function getStatusText(disabledFlag?: boolean) {
  return disabledFlag ? '禁用' : '启用';
}

defineExpose({
  open,
});
</script>

<template>
  <ElDrawer v-model="visible" :size="1080" :title="drawerTitle">
    <div class="role-employee-drawer">
      <div class="role-employee-drawer__toolbar">
        <ElInput
          v-model="queryForm.keywords"
          clearable
          placeholder="搜索员工姓名或账号"
          style="width: 320px"
          @keyup.enter="handleSearch"
        />

        <ElSpace wrap>
          <ElButton round type="primary" @click="openEmployeeSelectModal">
            添加员工
          </ElButton>
          <ElButton :disabled="selectedCount === 0" type="danger" @click="handleBatchRemove">
            批量移除
          </ElButton>
          <ElButton :loading="loading" @click="handleSearch">刷新</ElButton>
        </ElSpace>
      </div>

      <div v-if="selectedCount > 0" class="role-employee-drawer__selected">
        已选 {{ selectedCount }} 人
      </div>

      <ElTable
        v-loading="loading"
        :data="memberList"
        border
        height="100%"
        @selection-change="handleSelectionChange"
      >
        <template #empty>
          <ElEmpty description="暂无角色成员" />
        </template>

        <ElTableColumn type="selection" width="54" />
        <ElTableColumn label="员工姓名" min-width="160" prop="actualName" />
        <ElTableColumn label="登录账号" min-width="180" prop="loginName" />
        <ElTableColumn label="手机号" min-width="160" prop="phone" />
        <ElTableColumn label="所属部门" min-width="220" prop="departmentName" />
        <ElTableColumn label="状态" min-width="120">
          <template #default="{ row }">
            <ElTag :type="row.disabledFlag ? 'danger' : 'success'">
              {{ getStatusText(row.disabledFlag) }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="创建时间" min-width="180" prop="createTime" />
        <ElTableColumn fixed="right" label="操作" min-width="120">
          <template #default="{ row }">
            <ElButton link type="danger" @click="handleRemove(row)">
              移除
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

      <div class="role-employee-drawer__pagination">
        <ElPagination
          :current-page="queryForm.pageNum"
          :page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          background
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>

    </div>

    <RoleEmployeeSelectModalView
      ref="employeeSelectModalRef"
      @success="
        async () => {
          await loadMemberList();
          emit('success');
        }
      "
    />

    <template #footer>
      <div class="flex justify-end gap-3">
        <ElButton @click="close">关闭</ElButton>
      </div>
    </template>
  </ElDrawer>
</template>

<style scoped>
.role-employee-drawer {
  display: flex;
  height: 100%;
  flex-direction: column;
  gap: 16px;
}

.role-employee-drawer__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.role-employee-drawer__selected {
  color: var(--el-color-primary);
  font-size: 13px;
}

.role-employee-drawer__pagination {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 960px) {
  .role-employee-drawer__toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
