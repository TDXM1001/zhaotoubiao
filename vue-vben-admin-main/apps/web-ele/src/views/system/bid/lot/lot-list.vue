<script lang="ts" setup>
import type { SystemBidLotApi, SystemBidProjectApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';
import { Plus } from '@vben/icons';

import {
  ElAlert,
  ElButton,
  ElMessage,
  ElMessageBox,
  ElTag,
} from 'element-plus';

import {
  closeBidLotApi,
  queryBidLotPageApi,
  queryBidProjectListApi,
  voidBidLotApi,
} from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';
import ProTable from '#/components/pro-table/index.vue';

import {
  getLotStatusText,
  getStatusTagType,
  LOT_STATUS_OPTIONS,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidLotList',
});

const route = useRoute();
const router = useRouter();
const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const projectOptions = ref<SystemBidProjectApi.ProjectOption[]>([]);

const { options: evaluationModeOptions } = useDictOptions(
  'BID_EVALUATION_MODE',
);

const currentProjectId = computed(() => parseRouteNumber(route.query.projectId));
const currentProjectName = computed(() => String(route.query.projectName || ''));

const projectSearchOptions = computed(() => {
  return projectOptions.value.map((item) => ({
    label: `${item.projectName}（${item.projectCode}）`,
    value: item.projectId,
  }));
});

const tableColumns = reactive<ColumnProps<SystemBidLotApi.LotItem>[]>([
  {
    label: '标段名称',
    minWidth: 220,
    prop: 'lotName',
    search: {
      el: 'input',
      key: 'keyword',
      label: '关键字',
      props: {
        placeholder: '请输入标段名称、标段编号或项目名称',
      },
    },
  },
  {
    label: '所属项目',
    minWidth: 220,
    prop: 'projectName',
    search: {
      el: 'select',
      label: '所属项目',
    },
    enum: projectSearchOptions,
  },
  {
    label: '标段编号',
    minWidth: 160,
    prop: 'lotCode',
  },
  {
    label: '标段序号',
    minWidth: 100,
    prop: 'lotNo',
  },
  {
    enum: LOT_STATUS_OPTIONS,
    label: '标段状态',
    minWidth: 120,
    prop: 'status',
    search: {
      el: 'select',
      label: '标段状态',
    },
  },
  {
    enum: evaluationModeOptions,
    label: '评标方式',
    minWidth: 140,
    prop: 'evaluationMode',
  },
  {
    label: '投标截止时间',
    minWidth: 180,
    prop: 'bidEndTime',
  },
  {
    label: '开标时间',
    minWidth: 180,
    prop: 'openingTime',
  },
  {
    fixed: 'right',
    label: '操作',
    prop: 'operation',
    width: 280,
  },
]);

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:lot:query');
});

const hasCreatePermission = computed(() => {
  return accessStore.accessCodes.includes('bid:lot:create');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('bid:lot:update');
});

const hasCloseBidPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:lot:close-bid');
});

const hasVoidPermission = computed(() => {
  return accessStore.accessCodes.includes('bid:lot:void');
});

async function loadProjectOptions() {
  projectOptions.value = await queryBidProjectListApi();
}

async function queryLotTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    return {
      list: [],
      total: 0,
    };
  }

  return queryBidLotPageApi({
    keyword: params.keyword?.trim() || undefined,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    projectId: params.projectId || currentProjectId.value,
    searchCount: true,
    status: params.status || undefined,
  });
}

function handleSearch() {
  if (!hasQueryPermission.value) {
    ElMessage.warning('当前账号没有标段查询权限');
  }
}

function openCreatePage() {
  router.push({
    path: '/system/bid/lot/create',
    query: currentProjectId.value
      ? { projectId: String(currentProjectId.value) }
      : undefined,
  });
}

function openEditPage(row: SystemBidLotApi.LotItem) {
  router.push({
    path: '/system/bid/lot/edit',
    query: {
      lotId: String(row.lotId),
    },
  });
}

function openDetailPage(row: SystemBidLotApi.LotItem) {
  router.push({
    path: '/system/bid/lot/detail',
    query: {
      lotId: String(row.lotId),
    },
  });
}

function hasAction(row: SystemBidLotApi.LotItem, action: string) {
  return row.allowedActions?.includes(action) ?? false;
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

async function handleCloseBid(row: SystemBidLotApi.LotItem) {
  await ElMessageBox.confirm(
    `确定要关闭标段“${row.lotName}”的投标吗？`,
    '关闭投标确认',
    {
      type: 'warning',
    },
  );

  await closeBidLotApi({
    lotId: row.lotId,
    version: row.version,
  });
  ElMessage.success('标段投标已关闭');
  await reloadTable();
}

async function handleVoidLot(row: SystemBidLotApi.LotItem) {
  await ElMessageBox.confirm(
    `确定要废止标段“${row.lotName}”吗？`,
    '废止确认',
    {
      type: 'warning',
    },
  );

  await voidBidLotApi({
    lotId: row.lotId,
    version: row.version,
  });
  ElMessage.success('标段废止成功');
  await reloadTable();
}

function clearProjectFilter() {
  router.replace({
    path: '/system/bid/lot/list',
  });
  void reloadTable();
}

onMounted(() => {
  void loadProjectOptions();
});
</script>

<template>
  <Page auto-content-height class="bid-lot-page">
    <div class="bid-lot-page__content">
      <ElAlert
        v-if="currentProjectId"
        :closable="false"
        class="bid-lot-page__alert"
        show-icon
        type="info"
      >
        <template #title>
          正在查看项目
          <strong>{{ currentProjectName || currentProjectId }}</strong>
          下的标段
          <ElButton link type="primary" @click="clearProjectFilter">
            清除筛选
          </ElButton>
        </template>
      </ElAlert>

      <ElAlert
        v-if="!hasQueryPermission"
        :closable="false"
        class="bid-lot-page__alert"
        show-icon
        title="当前账号没有标段查询权限"
        type="warning"
      />

      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryLotTable"
        row-key="lotId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
        @search="handleSearch"
      >
        <template #tableHeader>
          <div class="bid-lot-page__toolbar">
            <ElButton
              v-if="hasCreatePermission"
              :icon="Plus"
              type="primary"
              @click="openCreatePage"
            >
              新增标段
            </ElButton>
          </div>
        </template>

        <template #status="{ row }">
          <ElTag :type="getStatusTagType(row.status)">
            {{ getLotStatusText(row.status) }}
          </ElTag>
        </template>

        <template #operation="{ row }">
          <ElButton link type="primary" @click="openDetailPage(row)">
            详情
          </ElButton>
          <ElButton
            v-if="hasUpdatePermission && hasAction(row, 'edit-lot')"
            link
            type="primary"
            @click="openEditPage(row)"
          >
            编辑
          </ElButton>
          <ElButton
            v-if="hasCloseBidPermission && hasAction(row, 'close-bid')"
            link
            type="warning"
            @click="handleCloseBid(row)"
          >
            关闭投标
          </ElButton>
          <ElButton
            v-if="hasVoidPermission && hasAction(row, 'void-lot')"
            link
            type="danger"
            @click="handleVoidLot(row)"
          >
            废止
          </ElButton>
        </template>
      </ProTable>
    </div>
  </Page>
</template>

<style scoped>
.bid-lot-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.bid-lot-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.bid-lot-page__alert {
  margin-bottom: 4px;
}
</style>
