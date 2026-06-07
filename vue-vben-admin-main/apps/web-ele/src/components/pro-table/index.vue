<script lang="ts" setup>
import type { ColumnProps, ProTableProps, TypeProps } from './types';

import { computed, onMounted, provide, ref, shallowRef, unref } from 'vue';

import { Operation, Refresh, Search } from '@element-plus/icons-vue';
import {
  ElButton,
  ElRadio,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import SearchForm from '#/components/search-form/index.vue';

import ColSetting from './components/col-setting.vue';
import Pagination from './components/pagination.vue';
import TableColumn from './components/table-column.vue';
import { useSelection } from './use-selection';
import { useTable } from './use-table';
import { getColumnPropName } from './utils';

const props = withDefaults(defineProps<ProTableProps>(), {
  border: true,
  columns: () => [],
  initParam: () => ({}),
  pagination: true,
  requestAuto: true,
  rowKey: 'id',
  searchCol: () => ({ lg: 3, md: 2, sm: 2, xl: 4, xs: 1 }),
  searchDefaultCollapsed: true,
  toolButton: true,
});

const emit = defineEmits<{
  reset: [];
  search: [];
}>();

const tableRef = ref<InstanceType<typeof ElTable>>();
const uuid = ref(`id-${crypto.randomUUID()}`);
const columnTypes: TypeProps[] = ['selection', 'radio', 'index', 'expand', 'sort'];
const isShowSearch = ref(true);
const radio = ref('');
const colRef = ref();

const {
  clearSelectedList,
  isSelected,
  selectedList,
  selectedListIds,
  selectionChange,
} = useSelection(props.rowKey);

const {
  getTableList,
  handleCurrentChange,
  handleSizeChange,
  pageable,
  reset,
  search,
  searchInitParam,
  searchParam,
  tableData,
} = useTable(
  props.requestApi,
  props.initParam,
  props.pagination,
  props.dataCallback,
  props.requestError,
);

const tableColumns = shallowRef<ColumnProps[]>(props.columns);
const enumMap = ref(new Map<string, Record<string, any>[]>());

provide('enumMap', enumMap);

function showToolButton(key: 'refresh' | 'search' | 'setting') {
  return Array.isArray(props.toolButton)
    ? props.toolButton.includes(key)
    : props.toolButton;
}

async function setEnumMap({ enum: enumValue, prop }: ColumnProps) {
  if (!prop || !enumValue) {
    return;
  }

  if (
    enumMap.value.has(prop) &&
    (typeof enumValue === 'function' || enumMap.value.get(prop) === enumValue)
  ) {
    return;
  }

  if (typeof enumValue !== 'function') {
    enumMap.value.set(prop, unref(enumValue));
    return;
  }

  enumMap.value.set(prop, []);
  const response = await enumValue();
  const data = response?.data ?? response;
  enumMap.value.set(prop, Array.isArray(data) ? data : []);
}

function flatColumnsFunc(columns: ColumnProps[], flatArr: ColumnProps[] = []) {
  columns.forEach((column) => {
    if (column._children?.length) {
      flatArr.push(...flatColumnsFunc(column._children));
    }

    flatArr.push(column);
    column.isShow = column.isShow ?? true;
    column.isSetting = column.isSetting ?? true;
    column.isFilterEnum = column.isFilterEnum ?? true;
    setEnumMap(column);
  });

  return flatArr.filter((item) => !item._children?.length);
}

const flatColumns = ref<ColumnProps[]>(flatColumnsFunc(tableColumns.value));

const searchColumns = ref<ColumnProps[]>(
  flatColumns.value
    .filter((item) => item.search?.el || item.search?.render)
    .sort((a, b) => (a.search?.order ?? 0) - (b.search?.order ?? 0)),
);

searchColumns.value?.forEach((column, index) => {
  column.search!.order = column.search?.order ?? index + 2;
  const key = column.search?.key ?? getColumnPropName(column.prop!);
  const defaultValue = column.search?.defaultValue;

  if (defaultValue !== undefined && defaultValue !== null) {
    searchParam.value[key] = defaultValue;
    searchInitParam.value[key] = defaultValue;
  }
});

const colSetting = ref<ColumnProps[]>(
  tableColumns.value.filter((item) => {
    const { isSetting, prop, type } = item;
    return (
      !columnTypes.includes(type!) && prop !== 'operation' && isSetting !== false
    );
  }),
);

const processTableData = computed(() => {
  if (!props.data) {
    return tableData.value;
  }

  if (!props.pagination) {
    return props.data;
  }

  return props.data.slice(
    (pageable.value.pageNum - 1) * pageable.value.pageSize,
    pageable.value.pageSize * pageable.value.pageNum,
  );
});

function openColSetting() {
  colRef.value?.openColSetting();
}

function clearSelection() {
  tableRef.value?.clearSelection();
  clearSelectedList();
}

function _search() {
  emit('search');
  search();
}

function _reset() {
  emit('reset');
  reset();
}

onMounted(() => {
  if (props.requestAuto) {
    getTableList();
  }

  if (props.data) {
    pageable.value.total = props.data.length;
  }
});

defineExpose({
  clearSelection,
  element: tableRef,
  enumMap,
  getTableList,
  handleCurrentChange,
  handleSizeChange,
  isSelected,
  pageable,
  radio,
  reset,
  search,
  searchInitParam,
  searchParam,
  selectedList,
  selectedListIds,
  tableData: processTableData,
});
</script>

<template>
  <div class="pro-table">
    <SearchForm
      v-show="isShowSearch"
      :columns="searchColumns"
      :default-collapsed="searchDefaultCollapsed"
      :reset="_reset"
      :search="_search"
      :search-col="searchCol"
      :search-param="searchParam"
    />

    <div class="pro-table__main">
      <div class="pro-table__header">
        <div class="pro-table__header-left">
          <slot
            :is-selected="isSelected"
            name="tableHeader"
            :selected-list="selectedList"
            :selected-list-ids="selectedListIds"
          ></slot>
        </div>
        <div v-if="toolButton" class="pro-table__header-right">
          <slot name="toolButton">
            <ElButton
              v-if="showToolButton('refresh')"
              :icon="Refresh"
              circle
              @click="getTableList"
            />
            <ElButton
              v-if="showToolButton('setting') && columns.length > 0"
              :icon="Operation"
              circle
              @click="openColSetting"
            />
            <ElButton
              v-if="showToolButton('search') && searchColumns?.length"
              :icon="Search"
              circle
              @click="isShowSearch = !isShowSearch"
            />
          </slot>
        </div>
      </div>

      <ElTable
        :id="uuid"
        ref="tableRef"
        v-bind="$attrs"
        :border="border"
        :data="processTableData"
        :row-key="rowKey"
        @selection-change="selectionChange"
      >
        <slot></slot>
        <template v-for="item in tableColumns" :key="item.prop ?? item.type">
          <ElTableColumn
            v-if="item.type && columnTypes.includes(item.type)"
            v-bind="item"
            :align="item.align ?? 'center'"
            :reserve-selection="item.type === 'selection'"
          >
            <template
              v-if="['expand', 'radio', 'sort'].includes(item.type)"
              #default="scope"
            >
              <template v-if="item.type === 'expand'">
                <component
                  :is="item.render"
                  v-if="item.render"
                  v-bind="scope"
                />
                <slot v-else :name="item.type" v-bind="scope"></slot>
              </template>
              <ElRadio
                v-if="item.type === 'radio'"
                v-model="radio"
                :label="scope.row[rowKey]"
              >
                <span></span>
              </ElRadio>
              <ElTag v-if="item.type === 'sort'" class="pro-table__sort">
                排序
              </ElTag>
            </template>
          </ElTableColumn>

          <TableColumn v-else :column="item">
            <template v-for="slot in Object.keys($slots)" #[slot]="scope">
              <slot :name="slot" v-bind="scope"></slot>
            </template>
          </TableColumn>
        </template>

        <template #append>
          <slot name="append"></slot>
        </template>

        <template #empty>
          <div class="pro-table__empty">
            <slot name="empty">暂无数据</slot>
          </div>
        </template>
      </ElTable>

      <slot name="pagination">
        <Pagination
          v-if="pagination"
          :handle-current-change="handleCurrentChange"
          :handle-size-change="handleSizeChange"
          :pageable="pageable"
        />
      </slot>
    </div>

    <ColSetting v-if="toolButton" ref="colRef" :col-setting="colSetting" />
  </div>
</template>

<style scoped>
.pro-table {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.pro-table__main {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  padding: 16px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--el-border-radius-base);
}

.pro-table__header {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.pro-table__header-left {
  display: flex;
  flex: 1;
  min-width: 0;
}

.pro-table__header-right {
  display: flex;
  flex-shrink: 0;
  gap: 8px;
}

.pro-table__main :deep(.el-table) {
  flex: 1;
}

.pro-table__empty {
  padding: 28px 0;
  color: var(--el-text-color-secondary);
}

.pro-table__sort {
  cursor: move;
}
</style>
