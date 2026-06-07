<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type { SupportHelpDocApi } from '#/api';

import { computed, nextTick, reactive, ref } from 'vue';

import { useAccessStore } from '@vben/stores';

import {
  ElButton,
  ElDrawer,
  ElEmpty,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElTable,
  ElTableColumn,
  ElTreeSelect,
} from 'element-plus';

import {
  addHelpDocCatalogApi,
  deleteHelpDocCatalogApi,
  updateHelpDocCatalogApi,
} from '#/api';

defineOptions({
  name: 'SupportHelpDocCatalogDrawer',
});

interface TreeOption {
  children?: TreeOption[];
  label: string;
  value: number;
}

type CatalogTreeItem = SupportHelpDocApi.CatalogItem & {
  children: CatalogTreeItem[];
};

const props = defineProps<{
  catalogList: SupportHelpDocApi.CatalogItem[];
}>();

const emit = defineEmits<{
  success: [];
}>();

const accessStore = useAccessStore();
const formRef = ref<FormInstance>();
const drawerVisible = ref(false);
const formDrawerVisible = ref(false);
const loading = ref(false);
const currentCatalogId = ref<null | number>(null);

const formModel = reactive({
  name: '',
  parentId: 0,
  sort: 1,
});

const rules: FormRules<typeof formModel> = {
  name: [
    { message: '请输入目录名称', required: true, trigger: 'blur' },
    { max: 200, message: '目录名称长度不能超过 200 个字符', trigger: 'blur' },
  ],
};

const hasAddPermission = computed(() => {
  return accessStore.accessCodes.includes('support:helpDocCatalog:addCategory');
});

const hasDeletePermission = computed(() => {
  return accessStore.accessCodes.includes('support:helpDocCatalog:delete');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('support:helpDocCatalog:update');
});

const formDrawerTitle = computed(() => {
  return currentCatalogId.value ? '编辑目录' : '新增目录';
});

const catalogTree = computed(() => {
  return buildCatalogTree(props.catalogList);
});

const catalogParentOptions = computed<TreeOption[]>(() => {
  return [
    {
      label: '作为根目录',
      value: 0,
    },
    ...buildTreeOptions(
      filterCatalogList(props.catalogList, currentCatalogId.value),
    ),
  ];
});

const flatCatalogList = computed(() => {
  return flattenCatalogTree(catalogTree.value);
});

function buildCatalogTree(catalogList: SupportHelpDocApi.CatalogItem[]) {
  const itemMap = new Map<number, CatalogTreeItem>();

  for (const item of catalogList) {
    itemMap.set(item.helpDocCatalogId, {
      ...item,
      children: [],
    });
  }

  const rootList: CatalogTreeItem[] = [];

  for (const item of itemMap.values()) {
    const parentId = Number(item.parentId ?? 0);
    const parent = itemMap.get(parentId);
    if (parent) {
      parent.children.push(item);
    } else {
      rootList.push(item);
    }
  }

  function sortTree(list: CatalogTreeItem[]) {
    list.sort((left, right) => {
      return (left.sort ?? 0) - (right.sort ?? 0);
    });
    for (const item of list) {
      sortTree(item.children);
    }
  }

  sortTree(rootList);
  return rootList;
}

function flattenCatalogTree(tree: CatalogTreeItem[]) {
  const result: Array<
    SupportHelpDocApi.CatalogItem & {
      depth: number;
      parentName: string;
    }
  > = [];

  const nameMap = new Map<number, string>();
  for (const item of props.catalogList) {
    nameMap.set(item.helpDocCatalogId, item.name);
  }

  function append(list: CatalogTreeItem[], depth: number) {
    for (const item of list) {
      result.push({
        ...item,
        depth,
        parentName:
          item.parentId && nameMap.get(item.parentId)
            ? nameMap.get(item.parentId)!
            : '-',
      });
      append(item.children, depth + 1);
    }
  }

  append(tree, 0);
  return result;
}

function filterCatalogList(
  catalogList: SupportHelpDocApi.CatalogItem[],
  excludedCatalogId?: null | number,
) {
  if (!excludedCatalogId) {
    return catalogList;
  }

  const excludeIdSet = new Set<number>([excludedCatalogId]);
  let changed = true;

  while (changed) {
    changed = false;
    for (const item of catalogList) {
      if (item.parentId && excludeIdSet.has(item.parentId)) {
        if (!excludeIdSet.has(item.helpDocCatalogId)) {
          excludeIdSet.add(item.helpDocCatalogId);
          changed = true;
        }
      }
    }
  }

  return catalogList.filter((item) => !excludeIdSet.has(item.helpDocCatalogId));
}

function buildTreeOptions(catalogList: SupportHelpDocApi.CatalogItem[]): TreeOption[] {
  const tree = buildCatalogTree(catalogList);

  function mapOptions(list: CatalogTreeItem[]): TreeOption[] {
    return list.map((item) => ({
      children: item.children.length ? mapOptions(item.children) : undefined,
      label: item.name,
      value: item.helpDocCatalogId,
    }));
  }

  return mapOptions(tree);
}

function resetFormModel() {
  currentCatalogId.value = null;
  formModel.name = '';
  formModel.parentId = 0;
  formModel.sort = 1;
}

async function resetFormState() {
  resetFormModel();
  await nextTick();
  formRef.value?.clearValidate();
}

function open() {
  drawerVisible.value = true;
}

async function openCreate() {
  formDrawerVisible.value = true;
  await resetFormState();
}

async function openEdit(row: SupportHelpDocApi.CatalogItem) {
  currentCatalogId.value = row.helpDocCatalogId;
  formDrawerVisible.value = true;
  await nextTick();
  formRef.value?.clearValidate();
  formModel.name = row.name;
  formModel.parentId = Number(row.parentId ?? 0);
  formModel.sort = Number(row.sort ?? 1);
}

function closeFormDrawer() {
  formDrawerVisible.value = false;
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  try {
    loading.value = true;
    const payload: SupportHelpDocApi.CatalogCreateParams = {
      name: formModel.name.trim(),
      parentId: formModel.parentId,
      sort: Number(formModel.sort),
    };

    if (currentCatalogId.value) {
      await updateHelpDocCatalogApi({
        ...payload,
        helpDocCatalogId: currentCatalogId.value,
      });
      ElMessage.success('目录编辑成功');
    } else {
      await addHelpDocCatalogApi(payload);
      ElMessage.success('目录新增成功');
    }

    formDrawerVisible.value = false;
    emit('success');
  } finally {
    loading.value = false;
  }
}

async function handleDelete(row: SupportHelpDocApi.CatalogItem) {
  await ElMessageBox.confirm(
    `确定要删除目录“${row.name}”吗？如果目录下仍有文档，后端可能拒绝删除。`,
    '删除确认',
    {
      type: 'warning',
    },
  );

  await deleteHelpDocCatalogApi(row.helpDocCatalogId);
  ElMessage.success('目录删除成功');
  emit('success');
}

function formatCatalogName(row: { depth: number; name: string }) {
  return `${'　'.repeat(row.depth)}${row.name}`;
}

defineExpose({
  open,
  openCreate,
});
</script>

<template>
  <ElDrawer v-model="drawerVisible" :size="960" title="帮助文档目录管理">
    <div class="help-doc-catalog-drawer">
      <div class="help-doc-catalog-drawer__toolbar">
        <ElButton
          v-if="hasAddPermission"
          round
          type="primary"
          @click="openCreate"
        >
          新增目录
        </ElButton>
      </div>

      <ElTable :data="flatCatalogList" border height="100%">
        <template #empty>
          <ElEmpty description="暂无帮助文档目录" />
        </template>

        <ElTableColumn label="目录名称" min-width="240">
          <template #default="{ row }">
            {{ formatCatalogName(row) }}
          </template>
        </ElTableColumn>
        <ElTableColumn label="上级目录" min-width="180" prop="parentName" />
        <ElTableColumn label="排序" min-width="100" prop="sort" />
        <ElTableColumn fixed="right" label="操作" min-width="200">
          <template #default="{ row }">
            <ElButton
              v-if="hasUpdatePermission"
              link
              type="primary"
              @click="openEdit(row)"
            >
              编辑
            </ElButton>
            <ElButton
              v-if="hasDeletePermission"
              link
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </div>

    <ElDrawer
      v-model="formDrawerVisible"
      append-to-body
      :close-on-click-modal="false"
      :size="560"
      :title="formDrawerTitle"
    >
      <ElForm
        ref="formRef"
        :model="formModel"
        :rules="rules"
        class="help-doc-catalog-drawer__form"
        label-width="96px"
      >
        <ElFormItem label="目录名称" prop="name">
          <ElInput
            v-model="formModel.name"
            maxlength="200"
            placeholder="请输入目录名称"
          />
        </ElFormItem>

        <ElFormItem label="上级目录">
          <ElTreeSelect
            v-model="formModel.parentId"
            :data="catalogParentOptions"
            check-strictly
            clearable
            default-expand-all
            node-key="value"
            placeholder="请选择上级目录"
            style="width: 100%"
          />
        </ElFormItem>

        <ElFormItem label="排序">
          <ElInputNumber
            v-model="formModel.sort"
            :min="0"
            controls-position="right"
            style="width: 100%"
          />
        </ElFormItem>
      </ElForm>

      <div class="help-doc-catalog-drawer__footer">
        <div class="flex justify-end gap-3">
          <ElButton @click="closeFormDrawer">取消</ElButton>
          <ElButton :loading="loading" type="primary" @click="submit">
            确定
          </ElButton>
        </div>
      </div>
    </ElDrawer>
  </ElDrawer>
</template>

<style scoped>
.help-doc-catalog-drawer {
  display: flex;
  height: 100%;
  flex-direction: column;
  gap: 16px;
}

.help-doc-catalog-drawer__toolbar {
  display: flex;
  justify-content: flex-end;
}

.help-doc-catalog-drawer__form {
  padding-right: 12px;
}

.help-doc-catalog-drawer__footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}
</style>
