<script lang="ts" setup>
import type { TreeInstance } from 'element-plus';
import type { SystemRoleApi } from '#/api';

import { computed, nextTick, ref } from 'vue';

import {
  ElButton,
  ElDrawer,
  ElEmpty,
  ElMessage,
  ElTree,
} from 'element-plus';

import { getRoleSelectedMenuApi, updateRoleMenuApi } from '#/api';

defineOptions({
  name: 'SystemRoleMenuDrawer',
});

const emit = defineEmits<{
  success: [];
}>();

const treeRef = ref<TreeInstance>();
const visible = ref(false);
const loading = ref(false);
const submitting = ref(false);
const currentRoleId = ref<null | number>(null);
const currentRoleName = ref('');
const menuTreeList = ref<SystemRoleApi.RoleMenuTreeItem[]>([]);

const drawerTitle = computed(() => {
  return currentRoleName.value
    ? `菜单权限 - ${currentRoleName.value}`
    : '菜单权限';
});

function close() {
  visible.value = false;
}

async function loadMenuTree(roleId: number) {
  try {
    loading.value = true;
    const result = await getRoleSelectedMenuApi(roleId);
    menuTreeList.value = result.menuTreeList ?? [];
    await nextTick();
    treeRef.value?.setCheckedKeys(result.selectedMenuId ?? []);
  } finally {
    loading.value = false;
  }
}

async function open(role: Pick<SystemRoleApi.RoleItem, 'roleId' | 'roleName'>) {
  currentRoleId.value = role.roleId;
  currentRoleName.value = role.roleName;
  visible.value = true;
  await loadMenuTree(role.roleId);
}

async function submit() {
  if (!currentRoleId.value) {
    return;
  }

  try {
    submitting.value = true;
    await updateRoleMenuApi({
      menuIdList: (treeRef.value?.getCheckedKeys(false) as number[]) ?? [],
      roleId: currentRoleId.value,
    });
    ElMessage.success('角色菜单权限更新成功');
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
  <ElDrawer v-model="visible" :size="720" :title="drawerTitle">
    <div class="role-menu-drawer">
      <div class="role-menu-drawer__toolbar">
        <span class="role-menu-drawer__hint">
          勾选当前角色可访问的菜单和按钮权限
        </span>
        <ElButton :loading="loading" @click="currentRoleId && loadMenuTree(currentRoleId)">
          刷新
        </ElButton>
      </div>

      <div v-loading="loading" class="role-menu-drawer__body">
        <ElEmpty
          v-if="!menuTreeList.length && !loading"
          description="暂无菜单权限数据"
        />
        <ElTree
          v-else
          ref="treeRef"
          :data="menuTreeList"
          default-expand-all
          node-key="menuId"
          :props="{ children: 'children', label: 'menuName' }"
          show-checkbox
        />
      </div>

    </div>

    <template #footer>
      <div class="flex justify-end gap-3">
        <ElButton @click="close">取消</ElButton>
        <ElButton :loading="submitting" type="primary" @click="submit">
          保存
        </ElButton>
      </div>
    </template>
  </ElDrawer>
</template>

<style scoped>
.role-menu-drawer {
  display: flex;
  height: 100%;
  flex-direction: column;
  gap: 16px;
}

.role-menu-drawer__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.role-menu-drawer__hint {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.role-menu-drawer__body {
  flex: 1;
  overflow: auto;
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  padding: 16px;
}
</style>
