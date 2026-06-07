<template>
  <component
    :is="searchComponent"
    v-bind="componentProps"
    v-model.trim="searchValue"
  >
    <template v-if="column.search?.el === 'select'">
      <ElOption
        v-for="item in columnEnum"
        :key="String(item[fieldNames.value])"
        :label="item[fieldNames.label]"
        :value="item[fieldNames.value]"
      />
    </template>
  </component>
</template>

<script lang="ts" setup>
import type { Component } from 'vue';

import type { ColumnProps } from '#/components/pro-table/types';

import { computed, inject, ref } from 'vue';

import {
  ElCascader,
  ElDatePicker,
  ElInput,
  ElInputNumber,
  ElOption,
  ElSelect,
  ElSelectV2,
  ElSlider,
  ElSwitch,
  ElTimePicker,
  ElTimeSelect,
  ElTreeSelect,
} from 'element-plus';

import { getColumnPropName } from '#/components/pro-table/utils';

const props = defineProps<{
  column: ColumnProps;
  searchParam: Record<string, any>;
}>();

const enumMap = inject(
  'enumMap',
  ref(new Map<string, Record<string, any>[]>()),
);

const fieldNames = computed(() => ({
  children: props.column.fieldNames?.children ?? 'children',
  label: props.column.fieldNames?.label ?? 'label',
  value: props.column.fieldNames?.value ?? 'value',
}));

const searchKey = computed(() => {
  return props.column.search?.key ?? getColumnPropName(props.column.prop!);
});

const searchValue = computed({
  get: () => props.searchParam[searchKey.value],
  set: (value) => {
    props.searchParam[searchKey.value] = value;
  },
});

const componentMap: Record<string, Component> = {
  cascader: ElCascader,
  'date-picker': ElDatePicker,
  input: ElInput,
  'input-number': ElInputNumber,
  select: ElSelect,
  'select-v2': ElSelectV2,
  slider: ElSlider,
  switch: ElSwitch,
  'time-picker': ElTimePicker,
  'time-select': ElTimeSelect,
  'tree-select': ElTreeSelect,
};

const searchComponent = computed(() => {
  return props.column.search?.render
    ? props.column.search.render
    : componentMap[props.column.search?.el ?? 'input'];
});

const columnEnum = computed(() => {
  let enumData = enumMap.value.get(props.column.prop ?? '');

  if (!enumData) {
    return [];
  }

  if (props.column.search?.el === 'select-v2' && props.column.fieldNames) {
    enumData = enumData.map((item) => ({
      ...item,
      label: item[fieldNames.value.label],
      value: item[fieldNames.value.value],
    }));
  }

  return enumData;
});

const componentProps = computed(() => {
  const search = props.column.search;
  const label = fieldNames.value.label;
  const value = fieldNames.value.value;
  const children = fieldNames.value.children;
  let searchProps = search?.props ?? {};

  if (search?.el === 'tree-select') {
    searchProps = {
      ...searchProps,
      data: columnEnum.value,
      nodeKey: value,
      props: { children, label, ...searchProps.props },
    };
  }

  if (search?.el === 'cascader') {
    searchProps = {
      ...searchProps,
      options: columnEnum.value,
      props: { children, label, value, ...searchProps.props },
    };
  }

  if (search?.el === 'select-v2') {
    searchProps = {
      ...searchProps,
      options: columnEnum.value,
    };
  }

  const isRange =
    ['datetimerange', 'daterange', 'monthrange'].includes(
      search?.props?.type,
    ) || search?.props?.isRange;

  const placeholder = isRange
    ? {
        endPlaceholder: search?.props?.endPlaceholder ?? '结束时间',
        rangeSeparator: search?.props?.rangeSeparator ?? '至',
        startPlaceholder: search?.props?.startPlaceholder ?? '开始时间',
      }
    : {
        placeholder:
          search?.props?.placeholder ??
          (search?.el?.includes('input') ? '请输入' : '请选择'),
      };

  const clearable =
    search?.props?.clearable ??
    (search?.defaultValue === null || search?.defaultValue === undefined);

  return {
    ...searchProps,
    ...placeholder,
    clearable,
    data: search?.el === 'tree-select' ? columnEnum.value : undefined,
    options:
      search?.el === 'cascader' || search?.el === 'select-v2'
        ? columnEnum.value
        : undefined,
    searchParam: props.searchParam,
  };
});
</script>
