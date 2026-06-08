<script lang="ts" setup>
import type { SystemBidOpeningApi } from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import { ArrowLeft, Check, Close } from '@element-plus/icons-vue';
import {
  ElButton,
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElMessage,
  ElMessageBox,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {
  abnormalCloseBidOpeningApi,
  completeBidOpeningApi,
  getBidOpeningDetailApi,
  startBidOpeningApi,
} from '#/api';

import {
  getOpeningStatusText,
  getStatusTagType,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidOpeningDetail',
});

const route = useRoute();
const router = useRouter();
const accessStore = useAccessStore();

const loading = ref(false);
const openingDetail = ref<null | SystemBidOpeningApi.OpeningItem>(null);
const openingId = computed(() => parseRouteNumber(route.query.openingId));

const canStart = computed(
  () =>
    accessStore.accessCodes.includes('bid:opening:start-opening')
    && (openingDetail.value?.allowedActions?.includes('start-opening') ?? false),
);
const canComplete = computed(
  () =>
    accessStore.accessCodes.includes('bid:opening:complete-opening')
    && (openingDetail.value?.allowedActions?.includes('complete-opening') ??
      false),
);
const canAbnormal = computed(
  () =>
    accessStore.accessCodes.includes('bid:opening:abnormal-close-opening')
    && (openingDetail.value?.allowedActions?.includes(
      'abnormal-close-opening',
    ) ??
      false),
);

async function loadDetail() {
  if (!openingId.value) {
    ElMessage.warning('缺少开标ID，无法查看详情');
    return;
  }
  loading.value = true;
  try {
    openingDetail.value = await getBidOpeningDetailApi(openingId.value);
  } catch {
    openingDetail.value = null;
    ElMessage.warning('开标记录不存在或已被删除');
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push('/system/bid/opening/list');
}

async function handleStart() {
  if (!openingDetail.value) return;
  await startBidOpeningApi({
    openingId: openingDetail.value.openingId,
    version: openingDetail.value.version,
  });
  ElMessage.success('开标已开始');
  await loadDetail();
}

async function handleComplete() {
  if (!openingDetail.value) return;
  await completeBidOpeningApi({
    openingId: openingDetail.value.openingId,
    version: openingDetail.value.version,
  });
  ElMessage.success('开标已完成');
  await loadDetail();
}

async function handleAbnormal() {
  if (!openingDetail.value) return;
  const { value } = await ElMessageBox.prompt('请输入异常关闭原因', '异常关闭开标', {
    confirmButtonText: '确认关闭',
    inputErrorMessage: '异常关闭原因不能为空',
    inputPattern: /\S+/,
    inputPlaceholder: '请输入异常关闭原因',
    type: 'warning',
  });
  await abnormalCloseBidOpeningApi({
    openingId: openingDetail.value.openingId,
    remark: String(value).trim(),
    version: openingDetail.value.version,
  });
  ElMessage.success('开标已异常关闭');
  await loadDetail();
}

onMounted(() => {
  void loadDetail();
});
</script>

<template>
  <Page auto-content-height class="bid-opening-detail-page">
    <div class="bid-opening-detail-page__toolbar">
      <div class="bid-opening-detail-page__title">开标详情</div>
      <div class="bid-opening-detail-page__actions">
        <ElButton :icon="ArrowLeft" @click="goBack">返回</ElButton>
        <ElButton v-if="canStart" :icon="Check" type="success" @click="handleStart">
          开始
        </ElButton>
        <ElButton
          v-if="canComplete"
          :icon="Check"
          type="success"
          @click="handleComplete"
        >
          完成
        </ElButton>
        <ElButton
          v-if="canAbnormal"
          :icon="Close"
          type="danger"
          @click="handleAbnormal"
        >
          异常关闭
        </ElButton>
      </div>
    </div>

    <ElCard v-loading="loading" shadow="never">
      <ElDescriptions v-if="openingDetail" :column="2" border>
        <ElDescriptionsItem label="所属项目">
          {{ openingDetail.projectName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="所属标段">
          {{ openingDetail.lotName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          <ElTag :type="getStatusTagType(openingDetail.status)">
            {{ getOpeningStatusText(openingDetail.status) }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="开标时间">
          {{ openingDetail.openingTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="开标地点">
          {{ openingDetail.openingPlace || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="更新时间">
          {{ openingDetail.updateTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="开标摘要" :span="2">
          {{ openingDetail.summary || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="异常原因" :span="2">
          {{ openingDetail.abnormalReason || '--' }}
        </ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>

    <ElCard
      v-if="openingDetail"
      class="bid-opening-detail-page__items"
      shadow="never"
    >
      <template #header>
        <span>开标明细</span>
      </template>
      <ElTable :data="openingDetail.itemList ?? []" border>
        <ElTableColumn label="供应商" min-width="220" prop="supplierNameSnapshot" />
        <ElTableColumn label="信用代码" min-width="180" prop="supplierCreditCode" />
        <ElTableColumn label="报价" min-width="120" prop="quotedPrice" />
        <ElTableColumn label="文件检查" min-width="120" prop="documentCheckResult" />
        <ElTableColumn label="备注" min-width="180" prop="openComment" />
      </ElTable>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-opening-detail-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-opening-detail-page__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.bid-opening-detail-page__actions {
  display: inline-flex;
  gap: 8px;
}

.bid-opening-detail-page__items {
  margin-top: 12px;
}
</style>
