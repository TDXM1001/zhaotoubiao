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

import { addBidAwardApi } from '#/api';

import { parseRouteNumber } from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidAwardForm',
});

const route = useRoute();
const router = useRouter();

const form = reactive({
  awardAmount: undefined as number | undefined,
  evaluationId: parseRouteNumber(route.query.evaluationId) as number | undefined,
  lotId: parseRouteNumber(route.query.lotId) as number | undefined,
  projectId: parseRouteNumber(route.query.projectId) as number | undefined,
  remark: '',
  winningSubmissionId: parseRouteNumber(route.query.submissionId) as
    | number
    | undefined,
});

function goBack() {
  router.push('/system/bid/award/list');
}

async function handleSubmit() {
  if (!form.projectId || !form.lotId || !form.evaluationId || !form.winningSubmissionId) {
    ElMessage.warning('项目ID、标段ID、评标ID和中标投标ID不能为空');
    return;
  }
  await addBidAwardApi({
    awardAmount: form.awardAmount,
    evaluationId: form.evaluationId,
    lotId: form.lotId,
    projectId: form.projectId,
    remark: form.remark || undefined,
    winningSubmissionId: form.winningSubmissionId,
  });
  ElMessage.success('定标记录已创建');
  goBack();
}
</script>

<template>
  <Page auto-content-height>
    <div class="bid-award-form__toolbar">
      <div class="bid-award-form__title">新增定标</div>
      <div class="bid-award-form__actions">
        <ElButton :icon="ArrowLeft" @click="goBack">返回</ElButton>
        <ElButton :icon="Check" type="primary" @click="handleSubmit">
          保存
        </ElButton>
      </div>
    </div>
    <ElCard shadow="never">
      <ElForm :model="form" label-width="130px">
        <ElFormItem label="项目ID" required>
          <ElInputNumber v-model="form.projectId" :min="1" />
        </ElFormItem>
        <ElFormItem label="标段ID" required>
          <ElInputNumber v-model="form.lotId" :min="1" />
        </ElFormItem>
        <ElFormItem label="评标ID" required>
          <ElInputNumber v-model="form.evaluationId" :min="1" />
        </ElFormItem>
        <ElFormItem label="中标投标ID" required>
          <ElInputNumber v-model="form.winningSubmissionId" :min="1" />
        </ElFormItem>
        <ElFormItem label="中标金额">
          <ElInputNumber v-model="form.awardAmount" :min="0" :precision="2" />
        </ElFormItem>
        <ElFormItem label="备注">
          <ElInput v-model="form.remark" maxlength="500" type="textarea" />
        </ElFormItem>
      </ElForm>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-award-form__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-award-form__title {
  font-size: 18px;
  font-weight: 600;
}

.bid-award-form__actions {
  display: inline-flex;
  gap: 8px;
}
</style>
