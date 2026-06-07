<template>
  <RenderTableColumn v-bind="column" />
</template>

<script lang="tsx" setup>
import type {
  ColumnProps,
  HeaderRenderScope,
  RenderScope,
} from '../types';

import { inject, ref, useSlots } from 'vue';

import { ElTag, ElTableColumn } from 'element-plus';

import {
  filterEnum,
  formatTableValue,
  getColumnPropName,
  getRowValueByProp,
} from '../utils';

defineProps<{
  column: ColumnProps;
}>();

const slots = useSlots();
const enumMap = inject(
  'enumMap',
  ref(new Map<string, Record<string, any>[]>()),
);

function renderCellData(item: ColumnProps, scope: RenderScope<any>) {
  const value = getRowValueByProp(scope.row, item.prop!);

  return enumMap.value.get(item.prop!) && item.isFilterEnum
    ? filterEnum(value, enumMap.value.get(item.prop!)!, item.fieldNames)
    : formatTableValue(value);
}

function getTagType(item: ColumnProps, scope: RenderScope<any>) {
  return (
    filterEnum(
      getRowValueByProp(scope.row, item.prop!),
      enumMap.value.get(item.prop!),
      item.fieldNames,
      'tag',
    ) || 'primary'
  );
}

const RenderTableColumn = (item: ColumnProps) => {
  return (
    <>
      {item.isShow && (
        <ElTableColumn
          {...item}
          align={item.align ?? 'center'}
          showOverflowTooltip={item.showOverflowTooltip ?? item.prop !== 'operation'}
        >
          {{
            default: (scope: RenderScope<any>) => {
              if (item._children) {
                return item._children.map((child) => RenderTableColumn(child));
              }

              if (item.render) {
                return item.render(scope);
              }

              if (item.prop && slots[getColumnPropName(item.prop)]) {
                return slots[getColumnPropName(item.prop)]?.(scope);
              }

              if (item.tag) {
                return (
                  <ElTag type={getTagType(item, scope)}>
                    {renderCellData(item, scope)}
                  </ElTag>
                );
              }

              return renderCellData(item, scope);
            },
            header: (scope: HeaderRenderScope) => {
              if (item.headerRender) {
                return item.headerRender(scope);
              }

              if (item.prop && slots[`${getColumnPropName(item.prop)}Header`]) {
                return slots[`${getColumnPropName(item.prop)}Header`]?.(scope);
              }

              return item.label;
            },
          }}
        </ElTableColumn>
      )}
    </>
  );
};
</script>
