<template>
  <div :style="style">
    <slot></slot>
  </div>
</template>

<script lang="ts" setup name="Grid">
import type { VNode, VNodeArrayChildren } from 'vue';

import type { BreakPoint } from './types';

import {
  computed,
  onActivated,
  onBeforeMount,
  onDeactivated,
  onMounted,
  onUnmounted,
  provide,
  ref,
  useSlots,
  watch,
} from 'vue';

const props = withDefaults(
  defineProps<{
    collapsed?: boolean;
    collapsedRows?: number;
    cols?: number | Record<BreakPoint, number>;
    gap?: [number, number] | number;
  }>(),
  {
    collapsed: false,
    collapsedRows: 1,
    cols: () => ({ lg: 3, md: 2, sm: 2, xl: 4, xs: 1 }),
    gap: 0,
  },
);

const slots = useSlots().default?.() ?? [];
const breakPoint = ref<BreakPoint>('xl');
const hiddenIndex = ref(-1);

const gridCols = computed(() => {
  if (typeof props.cols === 'object') {
    return props.cols[breakPoint.value] ?? 4;
  }

  return props.cols;
});

const gridGap = computed(() => {
  if (typeof props.gap === 'number') {
    return `${props.gap}px`;
  }

  if (Array.isArray(props.gap)) {
    return `${props.gap[1]}px ${props.gap[0]}px`;
  }

  return 'unset';
});

const style = computed(() => ({
  display: 'grid',
  gap: gridGap.value,
  gridTemplateColumns: `repeat(${gridCols.value}, minmax(0, 1fr))`,
}));

provide('gap', Array.isArray(props.gap) ? props.gap[0] : props.gap);
provide('breakPoint', breakPoint);
provide('shouldHiddenIndex', hiddenIndex);
provide('cols', gridCols);

function resize(e: UIEvent) {
  const width = (e.target as Window).innerWidth;

  if (width < 768) {
    breakPoint.value = 'xs';
  } else if (width < 992) {
    breakPoint.value = 'sm';
  } else if (width < 1200) {
    breakPoint.value = 'md';
  } else if (width < 1920) {
    breakPoint.value = 'lg';
  } else {
    breakPoint.value = 'xl';
  }
}

function findIndex() {
  const fields: VNodeArrayChildren = [];
  let suffix: null | VNode = null;

  for (const slot of slots as any[]) {
    if (
      typeof slot.type === 'object' &&
      slot.type.name === 'GridItem' &&
      slot.props?.suffix !== undefined
    ) {
      suffix = slot;
    }

    if (typeof slot.type === 'symbol' && Array.isArray(slot.children)) {
      fields.push(...slot.children);
    }
  }

  let suffixCols = 0;

  if (suffix) {
    suffixCols =
      ((suffix as VNode).props?.[breakPoint.value]?.span ??
        (suffix as VNode).props?.span ??
        1) +
      ((suffix as VNode).props?.[breakPoint.value]?.offset ??
        (suffix as VNode).props?.offset ??
        0);
  }

  let nextHiddenIndex = -1;

  fields.reduce((previous: number, current, index) => {
    if (nextHiddenIndex !== -1) {
      return previous;
    }

    const currentProps = (current as VNode).props ?? {};
    const currentSpan =
      (currentProps[breakPoint.value]?.span ?? currentProps.span ?? 1) +
      (currentProps[breakPoint.value]?.offset ?? currentProps.offset ?? 0);
    const next = previous + currentSpan;

    if (next > props.collapsedRows * gridCols.value - suffixCols) {
      nextHiddenIndex = index;
    }

    return next;
  }, 0);

  hiddenIndex.value = nextHiddenIndex;
}

onBeforeMount(() => {
  if (props.collapsed) {
    findIndex();
  }
});

onMounted(() => {
  resize({ target: { innerWidth: window.innerWidth } } as unknown as UIEvent);
  window.addEventListener('resize', resize);
});

onActivated(() => {
  resize({ target: { innerWidth: window.innerWidth } } as unknown as UIEvent);
  window.addEventListener('resize', resize);
});

onUnmounted(() => {
  window.removeEventListener('resize', resize);
});

onDeactivated(() => {
  window.removeEventListener('resize', resize);
});

watch(
  () => breakPoint.value,
  () => {
    if (props.collapsed) {
      findIndex();
    }
  },
);

watch(
  () => props.collapsed,
  (value) => {
    if (value) {
      findIndex();
      return;
    }

    hiddenIndex.value = -1;
  },
);

defineExpose({ breakPoint });
</script>
