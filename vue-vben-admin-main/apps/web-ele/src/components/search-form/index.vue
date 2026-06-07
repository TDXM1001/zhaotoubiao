<template>
  <div v-if="columns.length" class="table-search">
    <ElForm ref="formRef" :model="searchParam">
      <Grid ref="gridRef" :collapsed="collapsed" :cols="searchCol" :gap="[20, 0]">
        <GridItem
          v-for="(item, index) in columns"
          :key="item.prop"
          v-bind="getResponsive(item)"
          :index="String(index)"
        >
          <ElFormItem>
            <template #label>
              <ElSpace :size="4">
                <span>{{ item.search?.label ?? item.label }}</span>
                <ElTooltip
                  v-if="item.search?.tooltip"
                  :content="item.search.tooltip"
                  effect="dark"
                  placement="top"
                >
                  <span class="search-form__help">?</span>
                </ElTooltip>
              </ElSpace>
              <span>&nbsp;:</span>
            </template>
            <SearchFormItem :column="item" :search-param="searchParam" />
          </ElFormItem>
        </GridItem>

        <GridItem suffix>
          <div class="search-form__operation">
            <ElButton :icon="Search" type="primary" @click="search">
              搜索
            </ElButton>
            <ElButton :icon="Delete" @click="reset">重置</ElButton>
            <ElButton
              v-if="showCollapse"
              class="search-form__toggle"
              link
              type="primary"
              @click="collapsed = !collapsed"
            >
              {{ collapsed ? '展开' : '收起' }}
              <ElIcon class="el-icon--right">
                <component :is="collapsed ? ArrowDown : ArrowUp" />
              </ElIcon>
            </ElButton>
          </div>
        </GridItem>
      </Grid>
    </ElForm>
  </div>
</template>

<script lang="ts" setup>
import type { BreakPoint } from '#/components/grid/types';
import type { ColumnProps } from '#/components/pro-table/types';

import { computed, ref } from 'vue';

import {
  ArrowDown,
  ArrowUp,
  Delete,
  Search,
} from '@element-plus/icons-vue';
import {
  ElButton,
  ElForm,
  ElFormItem,
  ElIcon,
  ElSpace,
  ElTooltip,
} from 'element-plus';

import Grid from '#/components/grid/index.vue';
import GridItem from '#/components/grid/grid-item.vue';

import SearchFormItem from './search-form-item.vue';

const props = withDefaults(
  defineProps<{
    columns?: ColumnProps[];
    defaultCollapsed?: boolean;
    reset: () => void;
    search: () => void;
    searchCol: number | Record<BreakPoint, number>;
    searchParam?: Record<string, any>;
  }>(),
  {
    columns: () => [],
    defaultCollapsed: true,
    searchParam: () => ({}),
  },
);

const collapsed = ref(props.defaultCollapsed);
const gridRef = ref();

const breakPoint = computed<BreakPoint>(() => {
  return gridRef.value?.breakPoint ?? 'xl';
});

function getResponsive(item: ColumnProps) {
  return {
    lg: item.search?.lg,
    md: item.search?.md,
    offset: item.search?.offset ?? 0,
    sm: item.search?.sm,
    span: item.search?.span,
    xl: item.search?.xl,
    xs: item.search?.xs,
  };
}

const showCollapse = computed(() => {
  let show = false;

  props.columns.reduce((previous, current) => {
    previous +=
      (current.search?.[breakPoint.value]?.span ?? current.search?.span ?? 1) +
      (current.search?.[breakPoint.value]?.offset ??
        current.search?.offset ??
        0);

    if (typeof props.searchCol === 'number') {
      if (previous >= props.searchCol) {
        show = true;
      }
    } else if (previous >= props.searchCol[breakPoint.value]) {
      show = true;
    }

    return previous;
  }, 0);

  return show;
});
</script>

<style scoped>
.table-search {
  padding: 18px 18px;
  margin-bottom: 12px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--el-border-radius-base);
}

/* .table-search :deep(.el-form-item) {
  margin-bottom: 18px;
} */

.search-form__operation {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  width: 100%;
}

.search-form__help {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  border: 1px solid var(--el-border-color);
  border-radius: 50%;
}

.search-form__toggle {
  padding-right: 0;
  padding-left: 0;
}
</style>
