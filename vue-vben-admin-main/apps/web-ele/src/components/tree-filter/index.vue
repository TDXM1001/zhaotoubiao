<template>
  <div class="tree-filter">
    <h4 v-if="title" class="tree-filter__title">
      {{ title }}
    </h4>
    <div class="tree-filter__search">
      <ElInput v-model="filterText" clearable placeholder="输入关键字进行过滤" />
      <ElDropdown trigger="click">
        <ElIcon :size="20"><More /></ElIcon>
        <template #dropdown>
          <ElDropdownMenu>
            <ElDropdownItem @click="toggleTreeNodes(true)">
              展开全部
            </ElDropdownItem>
            <ElDropdownItem @click="toggleTreeNodes(false)">
              折叠全部
            </ElDropdownItem>
          </ElDropdownMenu>
        </template>
      </ElDropdown>
    </div>
    <ElScrollbar :style="{ height: title ? 'calc(100% - 95px)' : 'calc(100% - 56px)' }">
      <ElTree
        ref="treeRef"
        :check-on-click-node="multiple"
        :check-strictly="false"
        :current-node-key="!multiple ? selected : ''"
        :data="multiple ? treeData : treeAllData"
        default-expand-all
        :default-checked-keys="multiple ? selected : []"
        :expand-on-click-node="false"
        :filter-node-method="filterNode"
        :highlight-current="!multiple"
        :node-key="id"
        :props="defaultProps"
        :show-checkbox="multiple"
        @check="handleCheckChange"
        @node-click="handleNodeClick"
      >
        <template #default="scope">
          <span class="tree-filter__node-label">
            <slot :row="scope">
              {{ scope.node.label }}
            </slot>
          </span>
        </template>
      </ElTree>
    </ElScrollbar>
  </div>
</template>

<script lang="ts" setup>
import { More } from '@element-plus/icons-vue';
import {
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
  ElIcon,
  ElInput,
  ElScrollbar,
  ElTree,
} from 'element-plus';
import { nextTick, onBeforeMount, ref, watch } from 'vue';

const props = withDefaults(
  defineProps<{
    data?: Record<string, any>[];
    defaultValue?: any;
    id?: string;
    label?: string;
    multiple?: boolean;
    requestApi?: (data?: any) => Promise<any>;
    title?: string;
  }>(),
  {
    data: () => [],
    id: 'id',
    label: 'label',
    multiple: false,
  },
);

const emit = defineEmits<{
  change: [value: any];
}>();

const defaultProps = {
  children: 'children',
  label: props.label,
};

const treeRef = ref<InstanceType<typeof ElTree>>();
const treeData = ref<Record<string, any>[]>([]);
const treeAllData = ref<Record<string, any>[]>([]);
const selected = ref();
const filterText = ref('');

function buildAllData(data: Record<string, any>[]) {
  treeData.value = data;
  treeAllData.value = [{ [props.id]: '', [props.label]: '全部' }, ...data];
}

function setSelected() {
  if (props.multiple) {
    selected.value = Array.isArray(props.defaultValue)
      ? props.defaultValue
      : [props.defaultValue].filter(
          (item) => item !== undefined && item !== null,
        );
    return;
  }

  selected.value = props.defaultValue ?? '';
}

onBeforeMount(async () => {
  setSelected();

  if (props.requestApi) {
    const response = await props.requestApi();
    const data = response?.data ?? response;
    buildAllData(Array.isArray(data) ? data : []);
  }
});

watch(
  () => props.defaultValue,
  () => nextTick(() => setSelected()),
  { deep: true, immediate: true },
);

watch(
  () => props.data,
  () => {
    buildAllData(props.data ?? []);
  },
  { deep: true, immediate: true },
);

watch(filterText, (value) => {
  treeRef.value?.filter(value);
});

function filterNode(value: string, _data: Record<string, any>, node: any) {
  if (!value) {
    return true;
  }

  let parentNode = node.parent;
  let level = 1;
  let labels = [node.label];

  while (level < node.level) {
    labels = [...labels, parentNode.label];
    parentNode = parentNode.parent;
    level++;
  }

  return labels.some((label) => String(label).includes(value));
}

function toggleTreeNodes(isExpand: boolean) {
  const nodes = treeRef.value?.store.nodesMap;

  if (!nodes) {
    return;
  }

  for (const node in nodes) {
    if (Object.prototype.hasOwnProperty.call(nodes, node)) {
      const treeNode = nodes[node];

      if (treeNode) {
        treeNode.expanded = isExpand;
      }
    }
  }
}

function handleNodeClick(data: Record<string, any>) {
  if (props.multiple) {
    return;
  }

  emit('change', data[props.id]);
}

function handleCheckChange() {
  emit('change', treeRef.value?.getCheckedKeys());
}

defineExpose({ treeAllData, treeData, treeRef });
</script>

<style scoped>
.tree-filter {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 16px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--el-border-radius-base);
}

.tree-filter__title {
  margin: 0 0 14px;
  overflow: hidden;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-filter__search {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}

.tree-filter__node-label {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
