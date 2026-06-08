<script lang="ts" setup>
import type { SystemBidAwardApi } from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import { ArrowLeft } from '@element-plus/icons-vue';
import {
  ElButton,
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElMessage,
  ElTag,
} from 'element-plus';

import { getBidAwardDetailApi } from '#/api';

import {
  getAwardStatusText,
  getStatusTagType,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidAwardDetail',
});

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const awardDetail = ref<null | SystemBidAwardApi.AwardItem>(null);
const awardId = computed(() => parseRouteNumber(route.query.awardId));

async function loadDetail() {
  if (!awardId.value) {
    ElMessage.warning('缺少定标ID，无法查看详情');
    return;
  }
  loading.value = true;
  try {
    awardDetail.value = await getBidAwardDetailApi(awardId.value);
  } catch {
    awardDetail.value = null;
    ElMessage.warning('定标记录不存在或已被删除');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadDetail();
});
</script>

<template>
  <Page auto-content-height class="bid-award-detail-page">
    <div class="bid-award-detail-page__toolbar">
      <div class="bid-award-detail-page__title">定标详情</div>
      <ElButton :icon="ArrowLeft" @click="router.push('/system/bid/award/list')">
        返回
      </ElButton>
    </div>

    <ElCard v-loading="loading" shadow="never">
      <ElDescriptions v-if="awardDetail" :column="2" border>
        <ElDescriptionsItem label="所属项目">
          {{ awardDetail.projectName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="所属标段">
          {{ awardDetail.lotName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          <ElTag :type="getStatusTagType(awardDetail.status)">
            {{ getAwardStatusText(awardDetail.status) }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="评标ID">
          {{ awardDetail.evaluationId }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="中标供应商">
          {{ awardDetail.winnerNameSnapshot }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="信用代码">
          {{ awardDetail.winnerCreditCode }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="中标金额">
          {{ awardDetail.awardAmount ?? '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="确认时间">
          {{ awardDetail.confirmTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="公示时间">
          {{ awardDetail.publicNoticeTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="更新时间">
          {{ awardDetail.updateTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="回退原因" :span="2">
          {{ awardDetail.rollbackReason || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注" :span="2">
          {{ awardDetail.remark || '--' }}
        </ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-award-detail-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-award-detail-page__title {
  font-size: 18px;
  font-weight: 600;
}
</style>
