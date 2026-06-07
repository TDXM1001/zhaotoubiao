<script lang="ts" setup>
import type { SystemBidLotApi, SystemBidProjectApi } from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import {
  ArrowLeft,
  Edit,
  Plus,
} from '@element-plus/icons-vue';
import {
  ElButton,
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElEmpty,
  ElMessage,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {
  getBidProjectDetailApi,
  queryBidLotByProjectIdApi,
} from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';

import {
  getLotStatusText,
  getProjectStatusText,
  getStatusTagType,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidProjectDetail',
});

const route = useRoute();
const router = useRouter();
const accessStore = useAccessStore();

const loading = ref(false);
const projectDetail = ref<null | SystemBidProjectApi.ProjectItem>(null);
const lotList = ref<SystemBidLotApi.LotItem[]>([]);

const { optionMap: projectTypeMap } = useDictOptions('BID_PROJECT_TYPE');
const { optionMap: procurementModeMap } = useDictOptions('BID_PROCUREMENT_MODE');

const projectId = computed(() => parseRouteNumber(route.query.projectId));

const canEdit = computed(() => {
  return accessStore.accessCodes.includes('bid:project:update')
    && (projectDetail.value?.allowedActions?.includes('edit-project') ?? false);
});

const canCreateLot = computed(() => {
  return accessStore.accessCodes.includes('bid:lot:create');
});

function getDictLabel(map: Map<string, { label: string }>, value?: string) {
  if (!value) {
    return '--';
  }
  return map.get(value)?.label ?? value;
}

async function loadDetail() {
  if (!projectId.value) {
    ElMessage.warning('缺少项目ID，无法查看详情');
    return;
  }

  loading.value = true;
  try {
    const [detail, lots] = await Promise.all([
      getBidProjectDetailApi(projectId.value),
      queryBidLotByProjectIdApi(projectId.value),
    ]);
    projectDetail.value = detail;
    lotList.value = lots;
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push('/system/bid/project/list');
}

function openEdit() {
  if (!projectId.value) {
    return;
  }
  router.push({
    path: '/system/bid/project/edit',
    query: {
      projectId: String(projectId.value),
    },
  });
}

function openCreateLot() {
  if (!projectId.value) {
    return;
  }
  router.push({
    path: '/system/bid/lot/create',
    query: {
      projectId: String(projectId.value),
    },
  });
}

function openLotDetail(row: SystemBidLotApi.LotItem) {
  router.push({
    path: '/system/bid/lot/detail',
    query: {
      lotId: String(row.lotId),
    },
  });
}

onMounted(() => {
  void loadDetail();
});
</script>

<template>
  <Page auto-content-height class="bid-project-detail-page">
    <div class="bid-project-detail-page__toolbar">
      <div class="bid-project-detail-page__title">招标项目详情</div>
      <div class="bid-project-detail-page__actions">
        <ElButton :icon="ArrowLeft" @click="goBack">返回</ElButton>
        <ElButton
          v-if="canEdit"
          :icon="Edit"
          type="primary"
          @click="openEdit"
        >
          编辑项目
        </ElButton>
        <ElButton
          v-if="canCreateLot"
          :icon="Plus"
          @click="openCreateLot"
        >
          新增标段
        </ElButton>
      </div>
    </div>

    <ElCard v-loading="loading" shadow="never">
      <template v-if="projectDetail">
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="项目名称">
            {{ projectDetail.projectName }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="项目编号">
            {{ projectDetail.projectCode }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="项目类型">
            {{ getDictLabel(projectTypeMap, projectDetail.projectType) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="采购方式">
            {{ getDictLabel(procurementModeMap, projectDetail.procurementMode) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="项目状态">
            <ElTag :type="getStatusTagType(projectDetail.status)">
              {{ getProjectStatusText(projectDetail.status) }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="归属组织">
            {{ projectDetail.ownerOrgName || '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="代理组织">
            {{ projectDetail.agentOrgName || '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="项目负责人">
            {{ projectDetail.managerEmployeeName || '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="项目预算">
            {{ projectDetail.budgetAmount ?? '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="标段数量">
            {{ projectDetail.lotCount ?? 0 }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="备注" :span="2">
            {{ projectDetail.remark || '--' }}
          </ElDescriptionsItem>
        </ElDescriptions>

        <div class="bid-project-detail-page__sub-title">项目标段</div>
        <ElTable v-if="lotList.length > 0" :data="lotList" border>
          <ElTableColumn label="标段序号" prop="lotNo" width="100" />
          <ElTableColumn label="标段名称" min-width="180" prop="lotName" />
          <ElTableColumn label="标段编号" min-width="160" prop="lotCode" />
          <ElTableColumn label="状态" min-width="120">
            <template #default="{ row }">
              <ElTag :type="getStatusTagType(row.status)">
                {{ getLotStatusText(row.status) }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="预算金额" min-width="140" prop="budgetAmount" />
          <ElTableColumn label="操作" width="120">
            <template #default="{ row }">
              <ElButton link type="primary" @click="openLotDetail(row)">
                查看
              </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
        <ElEmpty v-else description="当前项目还没有维护标段" />
      </template>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-project-detail-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-project-detail-page__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.bid-project-detail-page__actions {
  display: inline-flex;
  gap: 8px;
}

.bid-project-detail-page__sub-title {
  margin: 20px 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
</style>
