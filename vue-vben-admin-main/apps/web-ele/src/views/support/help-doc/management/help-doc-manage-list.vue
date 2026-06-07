<script lang="ts" setup>
import type HelpDocCatalogDrawer from './components/help-doc-catalog-drawer.vue';
import type HelpDocDetailDrawer from './components/help-doc-detail-drawer.vue';
import type HelpDocFormModal from './components/help-doc-form-modal.vue';

import type { SupportHelpDocApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import {
  ElAlert,
  ElButton,
  ElCard,
  ElMessage,
  ElMessageBox,
  ElSpace,
  ElSwitch,
} from 'element-plus';

import {
  deleteHelpDocApi,
  queryHelpDocCatalogListApi,
  queryHelpDocPageApi,
} from '#/api';
import ProTable from '#/components/pro-table/index.vue';
import TreeFilter from '#/components/tree-filter/index.vue';

import HelpDocCatalogDrawerView from './components/help-doc-catalog-drawer.vue';
import HelpDocDetailDrawerView from './components/help-doc-detail-drawer.vue';
import HelpDocFormModalView from './components/help-doc-form-modal.vue';

defineOptions({
  name: 'SupportHelpDocManageList',
});

interface CatalogTreeNode extends SupportHelpDocApi.CatalogItem {
  children?: CatalogTreeNode[];
}

const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const helpDocCatalogDrawerRef =
  ref<InstanceType<typeof HelpDocCatalogDrawer>>();
const helpDocDetailDrawerRef = ref<InstanceType<typeof HelpDocDetailDrawer>>();
const helpDocFormModalRef = ref<InstanceType<typeof HelpDocFormModal>>();

const initParam = reactive<{
  helpDocCatalogId?: number;
}>({});

const catalogList = ref<SupportHelpDocApi.CatalogItem[]>([]);
const treeSortAsc = ref(true);

const tableColumns = reactive<ColumnProps<SupportHelpDocApi.HelpDocItem>[]>([
  {
    label: '标题',
    minWidth: 220,
    prop: 'title',
    search: {
      el: 'input',
      key: 'keywords',
      label: '关键字',
      props: {
        placeholder: '请输入标题或作者',
      },
    },
  },
  {
    label: '目录',
    minWidth: 160,
    prop: 'helpDocCatalogName',
  },
  {
    label: '作者',
    minWidth: 120,
    prop: 'author',
  },
  {
    label: '排序',
    minWidth: 100,
    prop: 'sort',
  },
  {
    label: '页面浏览量',
    minWidth: 120,
    prop: 'pageViewCount',
  },
  {
    label: '用户浏览量',
    minWidth: 120,
    prop: 'userViewCount',
  },
  {
    isSetting: false,
    isShow: false,
    label: '创建时间',
    prop: 'createTime',
    search: {
      el: 'date-picker',
      key: 'createDateRange',
      label: '创建时间',
      props: {
        endPlaceholder: '结束日期',
        startPlaceholder: '开始日期',
        type: 'daterange',
        valueFormat: 'YYYY-MM-DD',
      },
    },
  },
  {
    label: '更新时间',
    minWidth: 180,
    prop: 'updateTime',
  },
  {
    fixed: 'right',
    label: '操作',
    prop: 'operation',
    width: 260,
  },
]);

const hasAddPermission = computed(() => {
  return accessStore.accessCodes.includes('support:helpDoc:add');
});

const hasDeletePermission = computed(() => {
  return accessStore.accessCodes.includes('support:helpDoc:delete');
});

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('support:helpDoc:query');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('support:helpDoc:update');
});

const hasDetailPermission = computed(() => {
  // 后端详情权限当前复用新增/更新权限，保持旧页面的兜底展示逻辑。
  return hasAddPermission.value || hasUpdatePermission.value;
});

const hasManageCatalogPermission = computed(() => {
  return (
    accessStore.accessCodes.includes('support:helpDocCatalog:addCategory') ||
    accessStore.accessCodes.includes('support:helpDocCatalog:update') ||
    accessStore.accessCodes.includes('support:helpDocCatalog:delete')
  );
});

const currentCatalogName = computed(() => {
  const currentCatalog = catalogList.value.find((item) => {
    return item.helpDocCatalogId === initParam.helpDocCatalogId;
  });

  return currentCatalog?.name ?? '全部目录';
});

const catalogTree = computed<CatalogTreeNode[]>(() => {
  const itemMap = new Map<number, CatalogTreeNode>();

  for (const item of catalogList.value) {
    itemMap.set(item.helpDocCatalogId, {
      ...item,
      children: [],
    });
  }

  const rootList: CatalogTreeNode[] = [];

  for (const item of itemMap.values()) {
    const parentId = Number(item.parentId ?? 0);
    const parent = itemMap.get(parentId);

    if (parent) {
      parent.children = parent.children ?? [];
      parent.children.push(item);
    } else {
      rootList.push(item);
    }
  }

  function sortTree(list: CatalogTreeNode[]) {
    list.sort((left, right) => {
      const sortValue = treeSortAsc.value ? 1 : -1;
      const sortDiff = (left.sort ?? 0) - (right.sort ?? 0);

      if (sortDiff !== 0) {
        return sortDiff * sortValue;
      }

      return left.name.localeCompare(right.name) * sortValue;
    });

    for (const item of list) {
      if (item.children?.length) {
        sortTree(item.children);
      }
    }
  }

  sortTree(rootList);
  return rootList;
});

async function queryHelpDocTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    return {
      list: [],
      total: 0,
    };
  }

  return queryHelpDocPageApi({
    createTimeBegin: params.createDateRange?.[0],
    createTimeEnd: params.createDateRange?.[1],
    helpDocCatalogId: params.helpDocCatalogId,
    keywords: params.keywords?.trim() || undefined,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    searchCount: true,
  });
}

async function loadCatalogList() {
  const result = await queryHelpDocCatalogListApi();
  catalogList.value = Array.isArray(result) ? result : [];
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

function handleSearch() {
  if (!hasQueryPermission.value) {
    ElMessage.warning('当前账号没有帮助文档查询权限');
  }
}

async function handleCatalogChange(value: number | string) {
  initParam.helpDocCatalogId = value === '' ? undefined : Number(value);
  await proTable.value?.search();
}

function openCatalogDrawer() {
  helpDocCatalogDrawerRef.value?.open();
}

function openCreateCatalog() {
  helpDocCatalogDrawerRef.value?.openCreate();
}

function openCreateModal() {
  if (catalogList.value.length === 0) {
    ElMessage.warning('请先创建帮助文档目录');
    return;
  }

  helpDocFormModalRef.value?.openCreate(initParam.helpDocCatalogId);
}

function openEditModal(row: SupportHelpDocApi.HelpDocItem) {
  helpDocFormModalRef.value?.openEdit(row);
}

function openDetailDrawer(row: SupportHelpDocApi.HelpDocItem) {
  if (!hasDetailPermission.value) {
    ElMessage.warning('当前账号没有帮助文档详情权限');
    return;
  }

  helpDocDetailDrawerRef.value?.open(row.helpDocId);
}

async function handleDelete(row: SupportHelpDocApi.HelpDocItem) {
  await ElMessageBox.confirm(
    `确定要删除帮助文档“${row.title}”吗？`,
    '删除确认',
    {
      type: 'warning',
    },
  );

  await deleteHelpDocApi(row.helpDocId);
  ElMessage.success('帮助文档删除成功');
  await reloadTable();
}

async function handleCatalogSuccess() {
  await loadCatalogList();

  const currentCatalogExists = catalogList.value.some((item) => {
    return item.helpDocCatalogId === initParam.helpDocCatalogId;
  });

  if (initParam.helpDocCatalogId && !currentCatalogExists) {
    initParam.helpDocCatalogId = undefined;
  }

  await reloadTable();
}

async function handleDocSuccess() {
  await reloadTable();
}

onMounted(async () => {
  await loadCatalogList();
});
</script>

<template>
  <Page auto-content-height class="help-doc-page">
    <div class="help-doc-page__content">
      <div class="help-doc-page__sidebar">
        <div class="help-doc-page__catalog-actions">
          <ElSpace wrap>
            <ElButton
              v-if="hasManageCatalogPermission"
              link
              type="primary"
              @click="openCreateCatalog"
            >
              新增目录
            </ElButton>
            <ElButton
              v-if="hasManageCatalogPermission"
              link
              type="primary"
              @click="openCatalogDrawer"
            >
              管理目录
            </ElButton>
          </ElSpace>

          <div class="help-doc-page__catalog-sort">
            <span>排序</span>
            <ElSwitch
              v-model="treeSortAsc"
              active-text="正序"
              inactive-text="倒序"
              inline-prompt
            />
          </div>
        </div>

        <TreeFilter
          :data="catalogTree"
          :default-value="initParam.helpDocCatalogId"
          id="helpDocCatalogId"
          label="name"
          title="目录筛选"
          @change="handleCatalogChange"
        />
      </div>

      <ElCard class="help-doc-page__table-card" shadow="never">
        <ElAlert
          v-if="!hasQueryPermission"
          :closable="false"
          class="help-doc-page__alert"
          show-icon
          title="当前账号没有帮助文档查询权限"
          type="warning"
        />

        <ProTable
          ref="proTable"
          :columns="tableColumns"
          :init-param="initParam"
          :request-api="queryHelpDocTable"
          :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
          row-key="helpDocId"
          @search="handleSearch"
        >
          <template #tableHeader>
            <div class="help-doc-page__toolbar">
              <ElButton
                v-if="hasAddPermission"
                round
                type="primary"
                @click="openCreateModal"
              >
                新建帮助文档
              </ElButton>

              <span class="help-doc-page__current-catalog">
                当前目录：{{ currentCatalogName }}
              </span>
            </div>
          </template>

          <template #operation="{ row }">
            <ElButton
              v-if="hasDetailPermission"
              link
              type="primary"
              @click="openDetailDrawer(row)"
            >
              详情
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
              @click="handleDelete(row)"
            >
              删除
            </ElButton>
          </template>
        </ProTable>
      </ElCard>
    </div>

    <HelpDocFormModalView
      ref="helpDocFormModalRef"
      :catalog-list="catalogList"
      @success="handleDocSuccess"
    />
    <HelpDocDetailDrawerView ref="helpDocDetailDrawerRef" />
    <HelpDocCatalogDrawerView
      ref="helpDocCatalogDrawerRef"
      :catalog-list="catalogList"
      @success="handleCatalogSuccess"
    />
  </Page>
</template>

<style scoped>
.help-doc-page__content {
  display: grid;
  gap: 16px;
  grid-template-columns: 300px minmax(0, 1fr);
  height: 100%;
}

.help-doc-page__sidebar {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.help-doc-page__catalog-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px 16px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--el-border-radius-base);
}

.help-doc-page__catalog-sort {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.help-doc-page__table-card {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  border: none;
}

.help-doc-page__table-card :deep(.el-card__body) {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.help-doc-page__toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.help-doc-page__current-catalog {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.help-doc-page__alert {
  margin-bottom: 4px;
}

@media (max-width: 1200px) {
  .help-doc-page__content {
    grid-template-columns: 260px minmax(0, 1fr);
  }
}

@media (max-width: 960px) {
  .help-doc-page__content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .help-doc-page__toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
