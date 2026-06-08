<script lang="ts" setup>
import type { SystemBidEvaluationApi } from '#/api';

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
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import { getBidEvaluationDetailApi } from '#/api';

import {
  getEvaluationStatusText,
  getStatusTagType,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidEvaluationDetail',
});

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const evaluationDetail =
  ref<null | SystemBidEvaluationApi.EvaluationItem>(null);
const evaluationId = computed(() => parseRouteNumber(route.query.evaluationId));

async function loadDetail() {
  if (!evaluationId.value) {
    ElMessage.warning('缺少评标ID，无法查看详情');
    return;
  }
  loading.value = true;
  try {
    evaluationDetail.value = await getBidEvaluationDetailApi(evaluationId.value);
  } catch {
    evaluationDetail.value = null;
    ElMessage.warning('评标记录不存在或已被删除');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadDetail();
});
</script>

<template>
  <Page auto-content-height class="bid-evaluation-detail-page">
    <div class="bid-evaluation-detail-page__toolbar">
      <div class="bid-evaluation-detail-page__title">评标详情</div>
      <ElButton :icon="ArrowLeft" @click="router.push('/system/bid/evaluation/list')">
        返回
      </ElButton>
    </div>

    <ElCard v-loading="loading" shadow="never">
      <ElDescriptions v-if="evaluationDetail" :column="2" border>
        <ElDescriptionsItem label="所属项目">
          {{ evaluationDetail.projectName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="所属标段">
          {{ evaluationDetail.lotName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          <ElTag :type="getStatusTagType(evaluationDetail.status)">
            {{ getEvaluationStatusText(evaluationDetail.status) }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="开标ID">
          {{ evaluationDetail.openingId }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="评标方式">
          {{ evaluationDetail.evaluationMode || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="开始时间">
          {{ evaluationDetail.startTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="定稿时间">
          {{ evaluationDetail.finalizeTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="更新时间">
          {{ evaluationDetail.updateTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="评标摘要" :span="2">
          {{ evaluationDetail.finalSummary || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="回退原因" :span="2">
          {{ evaluationDetail.rollbackReason || '--' }}
        </ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>

    <ElCard
      v-if="evaluationDetail"
      class="bid-evaluation-detail-page__items"
      shadow="never"
    >
      <template #header>
        <span>评标明细</span>
      </template>
      <ElTable :data="evaluationDetail.itemList ?? []" border>
        <ElTableColumn label="供应商" min-width="220" prop="supplierNameSnapshot" />
        <ElTableColumn label="报价" min-width="120" prop="quotedPrice" />
        <ElTableColumn label="总分" min-width="120" prop="totalScore" />
        <ElTableColumn label="排名" min-width="100" prop="rankingNo" />
        <ElTableColumn label="推荐" min-width="100">
          <template #default="{ row }">
            {{ row.recommendFlag ? '是' : '否' }}
          </template>
        </ElTableColumn>
        <ElTableColumn label="评标意见" min-width="200" prop="evaluationComment" />
      </ElTable>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-evaluation-detail-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-evaluation-detail-page__title {
  font-size: 18px;
  font-weight: 600;
}

.bid-evaluation-detail-page__items {
  margin-top: 12px;
}
</style>
