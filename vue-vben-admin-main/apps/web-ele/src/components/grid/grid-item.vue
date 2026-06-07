<template>
  <div v-show="isShow" :style="style">
    <slot></slot>
  </div>
</template>

<script lang="ts" setup name="GridItem">
import type { Ref } from 'vue';

import type { BreakPoint, Responsive } from './types';

import { computed, inject, ref, useAttrs, watch } from 'vue';

const props = withDefaults(
  defineProps<{
    lg?: Responsive;
    md?: Responsive;
    offset?: number;
    sm?: Responsive;
    span?: number;
    suffix?: boolean;
    xl?: Responsive;
    xs?: Responsive;
  }>(),
  {
    offset: 0,
    span: 1,
    suffix: false,
  },
);

const attrs = useAttrs() as { index?: string };
const isShow = ref(true);
const breakPoint = inject<Ref<BreakPoint>>('breakPoint', ref('xl'));
const shouldHiddenIndex = inject<Ref<number>>('shouldHiddenIndex', ref(-1));
const gap = inject<number>('gap', 0);
const cols = inject<Ref<number>>('cols', ref(4));

watch(
  () => [shouldHiddenIndex.value, breakPoint.value] as const,
  ([hiddenIndex]) => {
    const currentHiddenIndex = Number(hiddenIndex ?? -1);

    if (attrs.index !== undefined) {
      isShow.value = !(
        currentHiddenIndex !== -1 &&
        Number.parseInt(attrs.index) >= currentHiddenIndex
      );
    }
  },
  { immediate: true },
);

const style = computed(() => {
  const span = props[breakPoint.value]?.span ?? props.span;
  const offset = props[breakPoint.value]?.offset ?? props.offset;
  const width = span + offset > cols.value ? cols.value : span + offset;

  if (props.suffix) {
    return {
      gridColumnEnd: `span ${span + offset}`,
      gridColumnStart: cols.value - span - offset + 1,
      marginLeft:
        offset === 0
          ? 'unset'
          : `calc(((100% + ${gap}px) / ${span + offset}) * ${offset})`,
    };
  }

  return {
    gridColumn: `span ${width}/span ${width}`,
    marginLeft:
      offset === 0
        ? 'unset'
        : `calc(((100% + ${gap}px) / ${span + offset}) * ${offset})`,
  };
});
</script>
