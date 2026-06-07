<template>
  <ElDrawer v-model="drawerVisible" title="列设置" size="450px">
    <div class="col-setting__body">
      <ElTable
        :border="true"
        :data="colSetting"
        default-expand-all
        row-key="prop"
        :tree-props="{ children: '_children' }"
      >
        <ElTableColumn align="center" label="列名" prop="label" />
        <ElTableColumn align="center" label="显示" prop="isShow">
          <template #default="{ row }">
            <ElSwitch v-model="row.isShow" />
          </template>
        </ElTableColumn>
        <ElTableColumn align="center" label="排序" prop="sortable">
          <template #default="{ row }">
            <ElSwitch v-model="row.sortable" />
          </template>
        </ElTableColumn>
        <template #empty>
          <div class="col-setting__empty">暂无可配置列</div>
        </template>
      </ElTable>
    </div>
  </ElDrawer>
</template>

<script lang="ts" setup>
import type { ColumnProps } from '../types';

import {
  ElDrawer,
  ElSwitch,
  ElTable,
  ElTableColumn,
} from 'element-plus';
import { ref } from 'vue';

defineProps<{
  colSetting: ColumnProps[];
}>();

const drawerVisible = ref(false);

function openColSetting() {
  drawerVisible.value = true;
}

defineExpose({
  openColSetting,
});
</script>

<style scoped>
.col-setting__body {
  min-height: 0;
}

.col-setting__empty {
  padding: 24px 0;
  color: var(--el-text-color-secondary);
  text-align: center;
}
</style>
