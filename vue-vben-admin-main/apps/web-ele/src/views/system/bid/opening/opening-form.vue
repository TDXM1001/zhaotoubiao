<script lang="ts" setup>
import { reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import {
  ArrowLeft,
  Check,
} from '@element-plus/icons-vue';
import {
  ElButton,
  ElCard,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
} from 'element-plus';

import { addBidOpeningApi } from '#/api';

import { parseRouteNumber } from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidOpeningForm',
});

const route = useRoute();
const router = useRouter();

const form = reactive({
  hostEmployeeId: undefined as number | undefined,
  lotId: parseRouteNumber(route.query.lotId) as number | undefined,
  openingPlace: '',
  openingTime: '',
  projectId: parseRouteNumber(route.query.projectId) as number | undefined,
  recorderEmployeeId: undefined as number | undefined,
  summary: '',
});

function goBack() {
  router.push('/system/bid/opening/list');
}

async function handleSubmit() {
  if (!form.projectId || !form.lotId) {
    ElMessage.warning('项目ID和标段ID不能为空');
    return;
  }
  await addBidOpeningApi({
    hostEmployeeId: form.hostEmployeeId,
    lotId: form.lotId,
    openingPlace: form.openingPlace || undefined,
    openingTime: form.openingTime || undefined,
    projectId: form.projectId,
    recorderEmployeeId: form.recorderEmployeeId,
    summary: form.summary || undefined,
  });
  ElMessage.success('开标记录已创建');
  goBack();
}
</script>

<template>
  <Page auto-content-height>
    <div class="bid-opening-form__toolbar">
      <div class="bid-opening-form__title">新增开标</div>
      <div class="bid-opening-form__actions">
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
        <ElFormItem label="开标时间">
          <ElInput v-model="form.openingTime" placeholder="YYYY-MM-DD HH:mm:ss" />
        </ElFormItem>
        <ElFormItem label="开标地点">
          <ElInput v-model="form.openingPlace" maxlength="200" />
        </ElFormItem>
        <ElFormItem label="主持人员工ID">
          <ElInputNumber v-model="form.hostEmployeeId" :min="1" />
        </ElFormItem>
        <ElFormItem label="记录人员工ID">
          <ElInputNumber v-model="form.recorderEmployeeId" :min="1" />
        </ElFormItem>
        <ElFormItem label="摘要">
          <ElInput v-model="form.summary" maxlength="1000" type="textarea" />
        </ElFormItem>
      </ElForm>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-opening-form__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-opening-form__title {
  font-size: 18px;
  font-weight: 600;
}

.bid-opening-form__actions {
  display: inline-flex;
  gap: 8px;
}
</style>
