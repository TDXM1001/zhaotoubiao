<script lang="ts" setup>
import { reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import { ArrowLeft, Check } from '@element-plus/icons-vue';
import {
  ElButton,
  ElCard,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
} from 'element-plus';

import { addBidEvaluationApi } from '#/api';

import { parseRouteNumber } from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidEvaluationForm',
});

const route = useRoute();
const router = useRouter();

const form = reactive({
  evaluationMode: '',
  finalSummary: '',
  lotId: parseRouteNumber(route.query.lotId) as number | undefined,
  openingId: parseRouteNumber(route.query.openingId) as number | undefined,
  projectId: parseRouteNumber(route.query.projectId) as number | undefined,
});

function goBack() {
  router.push('/system/bid/evaluation/list');
}

async function handleSubmit() {
  if (!form.projectId || !form.lotId) {
    ElMessage.warning('项目ID和标段ID不能为空');
    return;
  }
  await addBidEvaluationApi({
    evaluationMode: form.evaluationMode || undefined,
    finalSummary: form.finalSummary || undefined,
    lotId: form.lotId,
    openingId: form.openingId,
    projectId: form.projectId,
  });
  ElMessage.success('评标记录已创建');
  goBack();
}
</script>

<template>
  <Page auto-content-height>
    <div class="bid-evaluation-form__toolbar">
      <div class="bid-evaluation-form__title">新增评标</div>
      <div class="bid-evaluation-form__actions">
        <ElButton :icon="ArrowLeft" @click="goBack">返回</ElButton>
        <ElButton :icon="Check" type="primary" @click="handleSubmit">
          保存
        </ElButton>
      </div>
    </div>
    <ElCard shadow="never">
      <ElForm :model="form" label-width="110px">
        <ElFormItem label="项目ID" required>
          <ElInputNumber v-model="form.projectId" :min="1" />
        </ElFormItem>
        <ElFormItem label="标段ID" required>
          <ElInputNumber v-model="form.lotId" :min="1" />
        </ElFormItem>
        <ElFormItem label="开标ID">
          <ElInputNumber v-model="form.openingId" :min="1" />
        </ElFormItem>
        <ElFormItem label="评标方式">
          <ElInput v-model="form.evaluationMode" maxlength="64" />
        </ElFormItem>
        <ElFormItem label="评标摘要">
          <ElInput v-model="form.finalSummary" maxlength="1000" type="textarea" />
        </ElFormItem>
      </ElForm>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-evaluation-form__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-evaluation-form__title {
  font-size: 18px;
  font-weight: 600;
}

.bid-evaluation-form__actions {
  display: inline-flex;
  gap: 8px;
}
</style>
