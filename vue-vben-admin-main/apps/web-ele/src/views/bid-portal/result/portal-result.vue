<script lang="ts" setup>
import type { SystemBidPortalApi } from '#/api';

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
  ElResult,
  ElTag,
} from 'element-plus';

import { getBidPortalLotResultApi } from '#/api';

import { getAwardStatusText, getStatusTagType, parseRouteNumber } from '#/views/system/bid/shared/bid-helper';

defineOptions({
  name: 'BidPortalResult',
});

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const result = ref<null | SystemBidPortalApi.PortalResultItem>(null);
const lotId = computed(() => parseRouteNumber(route.query.lotId));

async function loadResult() {
  if (!lotId.value) {
    ElMessage.warning('缺少标段ID，无法查看结果');
    return;
  }
  loading.value = true;
  try {
    result.value = await getBidPortalLotResultApi(lotId.value);
  } catch {
    result.value = null;
    ElMessage.warning('结果不存在或当前供应商无权查看');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadResult();
});
</script>

<template>
  <Page auto-content-height class="bid-portal-result-page">
    <div class="bid-portal-result-page__toolbar">
      <div class="bid-portal-result-page__title">标段结果</div>
      <ElButton :icon="ArrowLeft" @click="router.push('/bid-portal/project/list')">
        返回
      </ElButton>
    </div>

    <ElCard v-loading="loading" shadow="never">
      <template v-if="result">
        <ElResult
          :title="result.message || '结果状态'"
          :sub-title="result.lotName || result.projectName || '--'"
          :icon="result.winnerFlag ? 'success' : 'info'"
        />
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="所属项目">
            {{ result.projectName || '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="所属标段">
            {{ result.lotName || '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="定标状态">
            <ElTag :type="getStatusTagType(result.awardStatus)">
              {{ getAwardStatusText(result.awardStatus) }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="是否中标">
            {{ result.winnerFlag ? '是' : '否' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="中标金额">
            {{ result.awardAmount ?? '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="公示时间">
            {{ result.publicNoticeTime || '--' }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </template>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-portal-result-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-portal-result-page__title {
  font-size: 18px;
  font-weight: 600;
}
</style>
