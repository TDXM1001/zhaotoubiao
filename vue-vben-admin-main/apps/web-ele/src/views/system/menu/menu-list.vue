<script lang="ts" setup>
import type MenuFormModal from './components/menu-form-modal.vue';

import type { SystemMenuApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, onMounted, reactive, ref } from 'vue';

import { Page, VbenIcon } from '@vben/common-ui';
import { Plus } from '@vben/icons';
import { useAccessStore } from '@vben/stores';

import { Delete } from '@element-plus/icons-vue';
import {
  ElButton,
  ElMessage,
  ElMessageBox,
  ElTag,
} from 'element-plus';

import {
  batchDeleteMenuApi,
  getMenuAuthUrlApi,
  queryMenuListApi,
  queryMenuTreeApi,
} from '#/api';
import ProTable from '#/components/pro-table/index.vue';
import { useDictOptions } from '#/composables/use-dict-data';

import MenuFormModalView from './components/menu-form-modal.vue';

defineOptions({
  name: 'SystemMenuList',
});

const ROOT_PARENT_ID = 0;

const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const menuFormModalRef = ref<InstanceType<typeof MenuFormModal>>();
const menuTree = ref<SystemMenuApi.MenuTreeItem[]>([]);
const allMenuItems = ref<SystemMenuApi.MenuItem[]>([]);
const authUrlOptions = ref<SystemMenuApi.AuthUrlItem[]>([]);
const { optionMap: menuTypeOptionMap } = useDictOptions('MENU_TYPE');

const menuTypeSearchOptions = [
  { label: '目录', value: 1 },
  { label: '菜单', value: 2 },
  { label: '功能点', value: 3 },
];

const menuTypeSearchOptionMap = new Map(
  menuTypeSearchOptions.map((item) => [String(item.value), item.label]),
);

const yesNoOptions = [
  { label: '是', value: true },
  { label: '否', value: false },
];

const tableColumns = reactive<ColumnProps<SystemMenuApi.MenuItem>[]>([
  {
    type: 'selection',
    width: 56,
  },
  {
    align: 'left',
    label: '名称',
    minWidth: 260,
    prop: 'menuName',
    showOverflowTooltip: false,
    search: {
      el: 'input',
      key: 'keywords',
      label: '关键字',
      lg: { span: 2 },
      order: 1,
      props: {
        placeholder: '菜单名称/路由地址/组件路径/权限字符串',
      },
      xl: { span: 2 },
    },
  },
  {
    enum: menuTypeSearchOptions,
    label: '类型',
    minWidth: 100,
    prop: 'menuType',
    search: {
      el: 'select',
      label: '类型',
      order: 2,
      props: {
        placeholder: '请选择类型',
      },
    },
  },
  {
    label: '图标',
    minWidth: 70,
    prop: 'icon',
  },
  {
    align: 'left',
    label: '路径',
    minWidth: 180,
    prop: 'path',
  },
  {
    align: 'left',
    label: '组件',
    minWidth: 220,
    prop: 'component',
  },
  {
    align: 'left',
    label: '后端权限',
    minWidth: 180,
    prop: 'apiPerms',
    isShow: false,
    isSetting: false,
  },
  {
    align: 'left',
    label: '前端权限',
    minWidth: 180,
    prop: 'webPerms',
    isShow: false,
    isSetting: false,
  },
  {
    enum: yesNoOptions,
    label: '禁用',
    prop: 'disabledFlag',
    search: {
      el: 'select',
      label: '禁用',
      order: 3,
      props: {
        placeholder: '请选择',
      },
    },
    isShow: false,
    isSetting: false,
  },
  {
    enum: yesNoOptions,
    label: '外链',
    prop: 'frameFlag',
    search: {
      el: 'select',
      label: '外链',
      order: 4,
      props: {
        placeholder: '请选择',
      },
    },
    isShow: false,
    isSetting: false,
  },
  {
    enum: yesNoOptions,
    label: '缓存',
    prop: 'cacheFlag',
    search: {
      el: 'select',
      label: '缓存',
      order: 5,
      props: {
        placeholder: '请选择',
      },
    },
    isShow: false,
    isSetting: false,
  },
  {
    enum: yesNoOptions,
    label: '显示',
    prop: 'visibleFlag',
    search: {
      el: 'select',
      label: '显示',
      order: 6,
      props: {
        placeholder: '请选择',
      },
    },
    isShow: false,
    isSetting: false,
  },
  {
    label: '顺序',
    minWidth: 90,
    prop: 'sort',
    isShow: false,
    isSetting: false,
  },
  {
    label: '操作',
    fixed: 'right',
    prop: 'operation',
    width: 220,
  },
]);

const searchOnlyColumnProps = new Set([
  'cacheFlag',
  'disabledFlag',
  'frameFlag',
  'visibleFlag',
]);

for (const column of tableColumns) {
  if (column.prop && searchOnlyColumnProps.has(column.prop)) {
    column.isShow = false;
    column.isSetting = false;
  }
}

const hasAddPermission = computed(() => {
  return accessStore.accessCodes.includes('system:menu:add');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('system:menu:update');
});

const hasDeletePermission = computed(() => {
  return accessStore.accessCodes.includes('system:menu:batchDelete');
});

const selectedMenuRows = computed(() => {
  return (proTable.value?.selectedList ?? []) as SystemMenuApi.MenuItem[];
});

const hasSelectedMenus = computed(() => selectedMenuRows.value.length > 0);

function buildMenuTree(items: SystemMenuApi.MenuItem[]) {
  const nodeMap = new Map<number, SystemMenuApi.MenuItem>();
  const tree: SystemMenuApi.MenuItem[] = [];

  // 后端主列表返回平铺结构，这里按 parentId 组装成树表数据。
  for (const item of items) {
    nodeMap.set(item.menuId, {
      ...item,
      children: [],
    });
  }

  for (const item of items) {
    const current = nodeMap.get(item.menuId);

    if (!current) {
      continue;
    }

    const parentId = item.parentId ?? ROOT_PARENT_ID;
    const parent = nodeMap.get(parentId);

    if (!parent || parentId === ROOT_PARENT_ID) {
      tree.push(current);
      continue;
    }

    parent.children ||= [];
    parent.children.push(current);
  }

  return tree;
}

function getMenuTypeLabel(menuType: SystemMenuApi.MenuTypeValue) {
  return (
    menuTypeOptionMap.value.get(String(menuType))?.label ??
    menuTypeSearchOptionMap.get(String(menuType)) ??
    '--'
  );
}

function getMenuTypeTagType(menuType: SystemMenuApi.MenuTypeValue) {
  if (menuType === 1) {
    return 'primary';
  }

  if (menuType === 2) {
    return 'warning';
  }

  return 'info';
}

function normalizeMenuTypeParam(value: unknown) {
  if (value === undefined || value === null || value === '') {
    return undefined;
  }

  const menuType = Number(value);
  return [1, 2, 3].includes(menuType)
    ? (menuType as SystemMenuApi.MenuTypeValue)
    : undefined;
}

async function queryMenuTable(params: Record<string, any> = {}) {
  // 如果缓存为空，则从后端获取全量数据
  if (allMenuItems.value.length === 0) {
    const result = await queryMenuListApi();
    allMenuItems.value = Array.isArray(result) ? result : [];
  }

  // 在前端进行搜索条件过滤
  const keywords = params.keywords?.trim().toLowerCase();
  const menuType = normalizeMenuTypeParam(params.menuType);

  // 这里的过滤逻辑采用了保留命中节点及其所有祖先节点的策略，以维持树形结构的完整性
  const matchedIds = new Set<number>();
  allMenuItems.value.forEach((item) => {
    let match = true;
    if (keywords) {
      match = Boolean(
        item.menuName?.toLowerCase().includes(keywords) ||
          item.path?.toLowerCase().includes(keywords) ||
          item.component?.toLowerCase().includes(keywords) ||
          item.apiPerms?.toLowerCase().includes(keywords) ||
          item.webPerms?.toLowerCase().includes(keywords),
      );
    }
    if (match && menuType !== undefined) {
      match = item.menuType === menuType;
    }
    if (
      match &&
      params.disabledFlag !== undefined &&
      params.disabledFlag !== null &&
      params.disabledFlag !== ''
    ) {
      match = item.disabledFlag === params.disabledFlag;
    }
    if (
      match &&
      params.frameFlag !== undefined &&
      params.frameFlag !== null &&
      params.frameFlag !== ''
    ) {
      match = item.frameFlag === params.frameFlag;
    }
    if (
      match &&
      params.cacheFlag !== undefined &&
      params.cacheFlag !== null &&
      params.cacheFlag !== ''
    ) {
      match = item.cacheFlag === params.cacheFlag;
    }
    if (
      match &&
      params.visibleFlag !== undefined &&
      params.visibleFlag !== null &&
      params.visibleFlag !== ''
    ) {
      match = item.visibleFlag === params.visibleFlag;
    }

    if (match) {
      matchedIds.add(item.menuId);
    }
  });

  // 如果没有过滤条件，直接构建全量树
  const hasSearch = !!(
    keywords ||
    menuType !== undefined ||
    params.disabledFlag !== undefined ||
    params.frameFlag !== undefined ||
    params.cacheFlag !== undefined ||
    params.visibleFlag !== undefined
  );
  if (!hasSearch) {
    return buildMenuTree(allMenuItems.value);
  }

  // 包含匹配节点的所有父节点
  const resultIds = new Set<number>();
  const itemMap = new Map(allMenuItems.value.map((i) => [i.menuId, i]));

  matchedIds.forEach((id) => {
    let currId: number | undefined = id;
    while (currId !== undefined && currId !== ROOT_PARENT_ID) {
      if (resultIds.has(currId)) break;
      resultIds.add(currId);
      const item = itemMap.get(currId);
      currId = item?.parentId;
    }
  });

  const filteredItems = allMenuItems.value.filter((item) =>
    resultIds.has(item.menuId),
  );
  return buildMenuTree(filteredItems);
}

async function loadPageDependencies() {
  const [treeResult, authResult] = await Promise.all([
    queryMenuTreeApi(true),
    getMenuAuthUrlApi(),
  ]);

  menuTree.value = Array.isArray(treeResult) ? treeResult : [];
  authUrlOptions.value = Array.isArray(authResult) ? authResult : [];
}

async function handleRefresh() {
  proTable.value?.clearSelection();
  await loadPageDependencies();
  allMenuItems.value = [];
  await proTable.value?.getTableList();
}

function openCreateModal() {
  menuFormModalRef.value?.openCreate();
}

function openCreateChildModal(row: SystemMenuApi.MenuItem) {
  menuFormModalRef.value?.openCreateChild(row);
}

function openEditModal(row: SystemMenuApi.MenuItem) {
  menuFormModalRef.value?.openEdit(row);
}

async function handleDelete(rows: SystemMenuApi.MenuItem[]) {
  const menuNames = rows.map((item) => item.menuName).join('、');

  await ElMessageBox.confirm(
    `确定要删除菜单“${menuNames}”吗？删除目录会由后端一并处理其下级节点。`,
    '删除确认',
    {
      type: 'warning',
    },
  );

  await batchDeleteMenuApi(rows.map((item) => item.menuId));
  ElMessage.success(rows.length > 1 ? '菜单批量删除成功' : '菜单删除成功');
  await handleRefresh();
}

async function handleBatchDelete() {
  if (selectedMenuRows.value.length === 0) {
    return;
  }

  await handleDelete(selectedMenuRows.value);
}

onMounted(async () => {
  await loadPageDependencies();
});
</script>

<template>
  <Page auto-content-height class="menu-page">
    <div class="menu-page__content">
        <ProTable
          ref="proTable"
          :columns="tableColumns"
          default-expand-all
          :pagination="false"
          :request-api="queryMenuTable"
          row-key="menuId"
          :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
          :search-default-collapsed="false"
          :tool-button="['refresh', 'setting']"
          :tree-props="{ children: 'children' }"
        >
          <template #tableHeader>
            <div class="menu-page__toolbar">
              <ElButton
                v-if="hasAddPermission"
                :icon="Plus"
                type="primary"
                @click="openCreateModal"
              >
                添加菜单
              </ElButton>
              <ElButton
                v-if="hasDeletePermission"
                :icon="Delete"
                :disabled="!hasSelectedMenus"
                @click="handleBatchDelete"
              >
                批量删除
              </ElButton>
            </div>
          </template>
 
          <template #menuName="{ row }">
            {{ row.menuName || row.title || '--' }}
          </template>


          <template #menuType="{ row }">
            <ElTag :type="getMenuTypeTagType(row.menuType)">
              {{ getMenuTypeLabel(row.menuType) }}
            </ElTag>
          </template>

          <template #icon="{ row }">
            <div class="menu-page__icon-cell">
              <VbenIcon v-if="row.icon" :icon="row.icon" class="size-4" />
            </div>
          </template>

          <template #operation="{ row }">
            <ElButton
              v-if="hasAddPermission && row.menuType !== 3"
              link
              type="primary"
              @click="openCreateChildModal(row)"
            >
              添加下级
            </ElButton>
            <ElButton
              v-if="hasUpdatePermission"
              link
              type="primary"
              @click="openEditModal(row)"
            >
              编辑
            </ElButton>
            <ElButton
              v-if="hasDeletePermission"
              link
              type="danger"
              @click="handleDelete([row])"
            >
              删除
            </ElButton>
          </template>
        </ProTable>
    </div>

    <MenuFormModalView
      ref="menuFormModalRef"
      :auth-url-options="authUrlOptions"
      :menu-tree="menuTree"
      @success="handleRefresh"
    />
  </Page>
</template>

<style scoped>
.menu-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.menu-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.menu-page__icon-cell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  color: var(--el-text-color-primary);
}
</style>
