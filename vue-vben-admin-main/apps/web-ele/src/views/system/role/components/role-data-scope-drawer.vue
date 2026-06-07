<script lang="ts" setup>
import type { SystemRoleApi } from '#/api';

import { computed, ref } from 'vue';

import {
  ElButton,
  ElDrawer,
  ElEmpty,
  ElMessage,
  ElOption,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {
  getDataScopeListApi,
  getRoleDataScopeListApi,
  updateRoleDataScopeListApi,
} from '#/api';

defineOptions({
  name: 'SystemRoleDataScopeDrawer',
});

interface DataScopeEditableItem extends SystemRoleApi.DataScopeItem {
  selectedViewType?: number;
}

const emit = defineEmits<{
  success: [];
}>();

const visible = ref(false);
const loading = ref(false);
const submitting = ref(false);
const currentRoleId = ref<null | number>(null);
const currentRoleName = ref('');
const dataScopeList = ref<DataScopeEditableItem[]>([]);

const drawerTitle = computed(() => {
  return currentRoleName.value
    ? `数据范围 - ${currentRoleName.value}`
    : '数据范围';
});

function close() {
  visible.value = false;
}

async function loadData(roleId: number) {
  try {
    loading.value = true;
    const [dataScopeOptions, selectedList] = await Promise.all([
      getDataScopeListApi(),
      getRoleDataScopeListApi(roleId),
    ]);

    const selectedMap = new Map(
      (selectedList ?? []).map((item) => [item.dataScopeType, item.viewType]),
    );

    // 把“系统定义”和“角色当前值”合成可直接编辑的数据源，避免提交时再次做复杂转换。
    dataScopeList.value = (dataScopeOptions ?? [])
      .slice()
      .sort((left, right) => left.dataScopeTypeSort - right.dataScopeTypeSort)
      .map((item) => ({
        ...item,
        selectedViewType:
          selectedMap.get(item.dataScopeType) ?? item.viewTypeList?.[0]?.viewType,
      }));
  } finally {
    loading.value = false;
  }
}

async function open(role: Pick<SystemRoleApi.RoleItem, 'roleId' | 'roleName'>) {
  currentRoleId.value = role.roleId;
  currentRoleName.value = role.roleName;
  visible.value = true;
  await loadData(role.roleId);
}

async function submit() {
  if (!currentRoleId.value || dataScopeList.value.length === 0) {
    return;
  }

  try {
    submitting.value = true;
    await updateRoleDataScopeListApi({
      dataScopeItemList: dataScopeList.value.map((item) => ({
        dataScopeType: item.dataScopeType,
        viewType: item.selectedViewType!,
      })),
      roleId: currentRoleId.value,
    });
    ElMessage.success('角色数据范围更新成功');
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
  <ElDrawer v-model="visible" :size="820" :title="drawerTitle">
    <div class="role-data-scope-drawer">
      <div class="role-data-scope-drawer__toolbar">
        <span class="role-data-scope-drawer__hint">
          为不同数据范围类型配置当前角色可见范围
        </span>
        <ElButton :loading="loading" @click="currentRoleId && loadData(currentRoleId)">
          刷新
        </ElButton>
      </div>

      <div v-loading="loading" class="role-data-scope-drawer__body">
        <ElEmpty
          v-if="!dataScopeList.length && !loading"
          description="暂无数据范围配置"
        />
        <ElTable v-else :data="dataScopeList" border height="100%">
          <ElTableColumn label="数据范围类型" min-width="220">
            <template #default="{ row }">
              <div class="role-data-scope-drawer__type">
                <div>{{ row.dataScopeTypeName }}</div>
                <div class="role-data-scope-drawer__desc">
                  {{ row.dataScopeTypeDesc }}
                </div>
              </div>
            </template>
          </ElTableColumn>
          <ElTableColumn label="可见范围" min-width="260">
            <template #default="{ row }">
              <ElSelect
                v-model="row.selectedViewType"
                placeholder="请选择可见范围"
                style="width: 100%"
              >
                <ElOption
                  v-for="option in row.viewTypeList"
                  :key="`${row.dataScopeType}-${option.viewType}`"
                  :label="option.viewTypeName"
                  :value="option.viewType"
                />
              </ElSelect>
            </template>
          </ElTableColumn>
          <ElTableColumn label="范围等级" min-width="120">
            <template #default="{ row }">
              <ElTag type="info">
                {{
                  row.viewTypeList.find(
                    (option: SystemRoleApi.DataScopeViewTypeItem) =>
                      option.viewType === row.selectedViewType,
                  )?.viewTypeLevel ?? '--'
                }}
              </ElTag>
            </template>
          </ElTableColumn>
        </ElTable>
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
.role-data-scope-drawer {
  display: flex;
  height: 100%;
  flex-direction: column;
  gap: 16px;
}

.role-data-scope-drawer__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.role-data-scope-drawer__hint,
.role-data-scope-drawer__desc {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.role-data-scope-drawer__body {
  flex: 1;
  min-height: 0;
}

.role-data-scope-drawer__type {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
</style>
