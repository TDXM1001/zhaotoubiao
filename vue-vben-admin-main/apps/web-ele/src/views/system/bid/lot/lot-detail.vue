<script lang="ts" setup>
import type { SystemBidLotApi } from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import {
  ArrowLeft,
  Edit,
} from '@element-plus/icons-vue';
import {
  ElButton,
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElMessage,
  ElTag,
} from 'element-plus';

import { getBidLotDetailApi } from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';

import {
  getLotStatusText,
  getStatusTagType,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidLotDetail',
});

const route = useRoute();
const router = useRouter();
const accessStore = useAccessStore();

const loading = ref(false);
const lotDetail = ref<null | SystemBidLotApi.LotItem>(null);
const lotId = computed(() => parseRouteNumber(route.query.lotId));

const { optionMap: evaluationModeMap } = useDictOptions('BID_EVALUATION_MODE');
const { optionMap: awardModeMap } = useDictOptions('BID_AWARD_MODE');

const canEdit = computed(() => {
  return accessStore.accessCodes.includes('bid:lot:update')
    && (lotDetail.value?.allowedActions?.includes('edit-lot') ?? false);
});

const lotListQuery = computed(() => {
  if (!lotDetail.value?.projectId) {
    return undefined;
  }

  return {
    projectId: String(lotDetail.value.projectId),
    projectName: lotDetail.value.projectName,
  };
});

function getDictLabel(map: Map<string, { label: string }>, value?: string) {
  if (!value) {
    return '--';
  }
  return map.get(value)?.label ?? value;
}

async function loadDetail() {
  if (!lotId.value) {
    ElMessage.warning('缺少标段ID，无法查看详情');
    return;
  }

  loading.value = true;
  try {
    lotDetail.value = await getBidLotDetailApi(lotId.value);
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push({
    path: '/system/bid/lot/list',
    query: lotListQuery.value,
  });
}

function openEdit() {
  if (!lotId.value) {
    return;
  }
  router.push({
    path: '/system/bid/lot/edit',
    query: {
      lotId: String(lotId.value),
    },
  });
}

onMounted(() => {
  void loadDetail();
});
</script>

<template>
  <Page auto-content-height class="bid-lot-detail-page">
    <div class="bid-lot-detail-page__toolbar">
      <div class="bid-lot-detail-page__title">标段详情</div>
      <div class="bid-lot-detail-page__actions">
        <ElButton :icon="ArrowLeft" @click="goBack">返回</ElButton>
        <ElButton
          v-if="canEdit"
          :icon="Edit"
          type="primary"
          @click="openEdit"
        >
          编辑标段
        </ElButton>
      </div>
    </div>

    <ElCard v-loading="loading" shadow="never">
      <ElDescriptions v-if="lotDetail" :column="2" border>
        <ElDescriptionsItem label="所属项目">
          {{ lotDetail.projectName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="标段名称">
          {{ lotDetail.lotName }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="标段编号">
          {{ lotDetail.lotCode }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="标段序号">
          {{ lotDetail.lotNo }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="标段状态">
          <ElTag :type="getStatusTagType(lotDetail.status)">
            {{ getLotStatusText(lotDetail.status) }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="预算金额">
          {{ lotDetail.budgetAmount ?? '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="评标方式">
          {{ getDictLabel(evaluationModeMap, lotDetail.evaluationMode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="定标方式">
          {{ getDictLabel(awardModeMap, lotDetail.awardMode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="报名开始时间">
          {{ lotDetail.registrationStartTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="报名截止时间">
          {{ lotDetail.registrationEndTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="投标开始时间">
          {{ lotDetail.bidStartTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="投标截止时间">
          {{ lotDetail.bidEndTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="开标时间">
          {{ lotDetail.openingTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="更新时间">
          {{ lotDetail.updateTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="标段范围" :span="2">
          {{ lotDetail.lotScope || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注" :span="2">
          {{ lotDetail.remark || '--' }}
        </ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-lot-detail-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-lot-detail-page__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.bid-lot-detail-page__actions {
  display: inline-flex;
  gap: 8px;
}
</style>
