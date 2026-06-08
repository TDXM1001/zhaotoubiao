<script lang="ts" setup>
import type { BidPortalApi } from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { ArrowLeft } from '@element-plus/icons-vue';
import {
  ElButton,
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElResult,
  ElTag,
} from 'element-plus';

import { getBidPortalLotResultApi } from '#/api';
import {
  getAwardStatusText,
  getStatusTagType,
  parseRouteNumber,
} from '#/utils/bid-helper';

defineOptions({ name: 'PortalResult' });

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const result = ref<null | BidPortalApi.PortalResultItem>(null);
const lotId = computed(() => parseRouteNumber(route.query.lotId));

async function loadResult() {
  if (!lotId.value) {
    return;
  }
  loading.value = true;
  try {
    result.value = await getBidPortalLotResultApi(lotId.value);
  } catch {
    result.value = null;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadResult();
});
</script>

<template>
  <section class="portal-page">
    <div class="portal-page__toolbar">
      <div>
        <h1 class="portal-page__title">标段结果</h1>
        <p class="portal-page__subtitle">查看本人参与标段的定标公示结果。</p>
      </div>
      <ElButton :icon="ArrowLeft" @click="router.push('/bid-portal/project/list')">
        返回
      </ElButton>
    </div>

    <ElCard v-loading="loading" class="portal-card" shadow="never">
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
      <div v-else-if="!loading" class="portal-empty">
        {{
          lotId
            ? '结果不存在或当前供应商无权查看'
            : '请从项目详情进入标段结果页'
        }}
      </div>
    </ElCard>
  </section>
</template>
