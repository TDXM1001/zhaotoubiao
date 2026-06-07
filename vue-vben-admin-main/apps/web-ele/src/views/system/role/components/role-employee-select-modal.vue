<script lang="ts" setup>
import type { SystemEmployeeApi } from '#/api';

import { computed, ref } from 'vue';

import {
  ElButton,
  ElDialog,
  ElEmpty,
  ElInput,
  ElMessage,
  ElSpace,
  ElTable,
  ElTableColumn,
} from 'element-plus';

import { batchAddRoleEmployeeApi, queryAllEmployeeApi } from '#/api';

defineOptions({
  name: 'SystemRoleEmployeeSelectModal',
});

const emit = defineEmits<{
  success: [];
}>();

const visible = ref(false);
const loading = ref(false);
const submitting = ref(false);
const keyword = ref('');
const roleId = ref<null | number>(null);
const existingEmployeeIds = ref<number[]>([]);
const employeeOptions = ref<SystemEmployeeApi.EmployeeOption[]>([]);
const selectedEmployeeIds = ref<number[]>([]);

const filteredEmployeeOptions = computed(() => {
  const text = keyword.value.trim().toLowerCase();
  const existingSet = new Set(existingEmployeeIds.value);

  return employeeOptions.value.filter((item) => {
    if (existingSet.has(item.employeeId)) {
      return false;
    }

    if (!text) {
      return true;
    }

    return (
      item.actualName.toLowerCase().includes(text) ||
      String(item.employeeId).includes(text) ||
      item.departmentName?.toLowerCase().includes(text)
    );
  });
});

async function loadEmployeeOptions() {
  try {
    loading.value = true;
    employeeOptions.value = await queryAllEmployeeApi(false);
  } finally {
    loading.value = false;
  }
}

async function open(payload: {
  existingEmployeeIds: number[];
  roleId: number;
}) {
  roleId.value = payload.roleId;
  existingEmployeeIds.value = [...payload.existingEmployeeIds];
  keyword.value = '';
  selectedEmployeeIds.value = [];
  visible.value = true;
  await loadEmployeeOptions();
}

function close() {
  visible.value = false;
}

function handleSelectionChange(rows: SystemEmployeeApi.EmployeeOption[]) {
  selectedEmployeeIds.value = rows.map((item) => item.employeeId);
}

async function submit() {
  if (!roleId.value || selectedEmployeeIds.value.length === 0) {
    ElMessage.warning('请先选择要添加的员工');
    return;
  }

  try {
    submitting.value = true;
    await batchAddRoleEmployeeApi({
      employeeIdList: selectedEmployeeIds.value,
      roleId: roleId.value,
    });
    ElMessage.success('角色成员添加成功');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
}

defineExpose({
  open,
});
</script>

<template>
  <ElDialog
    v-model="visible"
    :close-on-click-modal="false"
    title="添加角色成员"
    width="880px"
  >
    <div class="role-employee-select-modal">
      <div class="role-employee-select-modal__toolbar">
        <ElInput
          v-model="keyword"
          clearable
          placeholder="搜索员工姓名、ID 或部门"
          style="width: 320px"
        />
        <ElSpace>
          <span class="role-employee-select-modal__selected">
            已选 {{ selectedEmployeeIds.length }} 人
          </span>
          <ElButton :loading="loading" @click="loadEmployeeOptions">刷新</ElButton>
        </ElSpace>
      </div>

      <ElTable
        v-loading="loading"
        :data="filteredEmployeeOptions"
        border
        height="420"
        @selection-change="handleSelectionChange"
      >
        <template #empty>
          <ElEmpty description="暂无可添加员工" />
        </template>

        <ElTableColumn type="selection" width="54" />
        <ElTableColumn label="员工姓名" min-width="160" prop="actualName" />
        <ElTableColumn label="员工 ID" min-width="120" prop="employeeId" />
        <ElTableColumn label="所属部门" min-width="220" prop="departmentName" />
      </ElTable>
    </div>

    <template #footer>
      <div class="flex justify-end gap-3">
        <ElButton @click="close">取消</ElButton>
        <ElButton :loading="submitting" type="primary" @click="submit">
          确定添加
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<style scoped>
.role-employee-select-modal {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.role-employee-select-modal__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.role-employee-select-modal__selected {
  color: var(--el-color-primary);
  font-size: 13px;
}

@media (max-width: 900px) {
  .role-employee-select-modal__toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
