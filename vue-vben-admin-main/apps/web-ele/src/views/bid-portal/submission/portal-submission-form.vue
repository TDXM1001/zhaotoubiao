<script lang="ts" setup>
import { reactive } from 'vue';
import { useRoute } from 'vue-router';

import { Page } from '@vben/common-ui';

import { Check, Plus, RefreshLeft } from '@element-plus/icons-vue';
import {
  ElButton,
  ElCard,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
} from 'element-plus';

import {
  createBidPortalRegistrationApi,
  createBidPortalSubmissionApi,
  submitBidPortalSubmissionApi,
  withdrawBidPortalSubmissionApi,
} from '#/api';

defineOptions({
  name: 'BidPortalSubmissionForm',
});

const route = useRoute();
const baseForm = reactive({
  contactName: '',
  contactPhone: '',
  lotId: Number(route.query.lotId || 0),
  projectId: Number(route.query.projectId || 0),
  registrationId: undefined as number | undefined,
  registrationType: 'PUBLIC',
  remark: '',
  submissionId: undefined as number | undefined,
  supplierCreditCode: '',
  supplierNameSnapshot: '',
  version: undefined as number | undefined,
});

const submitForm = reactive({
  fileManifestJson: '',
  priceAmount: undefined as number | undefined,
});

async function handleCreateRegistration() {
  await createBidPortalRegistrationApi({
    contactName: baseForm.contactName,
    contactPhone: baseForm.contactPhone,
    lotId: baseForm.lotId,
    projectId: baseForm.projectId,
    registrationType: baseForm.registrationType,
    remark: baseForm.remark,
    supplierCreditCode: baseForm.supplierCreditCode,
    supplierNameSnapshot: baseForm.supplierNameSnapshot,
  });
  ElMessage.success('报名已提交');
}

async function handleCreateSubmission() {
  if (!baseForm.registrationId) {
    ElMessage.warning('请先填写报名ID');
    return;
  }
  await createBidPortalSubmissionApi({
    lotId: baseForm.lotId,
    projectId: baseForm.projectId,
    registrationId: baseForm.registrationId,
    remark: baseForm.remark,
    supplierCreditCode: baseForm.supplierCreditCode,
  });
  ElMessage.success('投标记录已创建');
}

async function handleSubmitBid() {
  if (!baseForm.submissionId || baseForm.version === undefined) {
    ElMessage.warning('请填写投标ID和版本号');
    return;
  }
  await submitBidPortalSubmissionApi({
    contactName: baseForm.contactName,
    contactPhone: baseForm.contactPhone,
    fileManifestJson: submitForm.fileManifestJson,
    priceAmount: submitForm.priceAmount,
    remark: baseForm.remark,
    submissionId: baseForm.submissionId,
    supplierCreditCode: baseForm.supplierCreditCode,
    version: baseForm.version,
  });
  ElMessage.success('投标已提交');
}

async function handleWithdrawBid() {
  if (!baseForm.submissionId || baseForm.version === undefined) {
    ElMessage.warning('请填写投标ID和版本号');
    return;
  }
  await withdrawBidPortalSubmissionApi({
    remark: baseForm.remark,
    submissionId: baseForm.submissionId,
    supplierCreditCode: baseForm.supplierCreditCode,
    version: baseForm.version,
  });
  ElMessage.success('投标已撤回');
}
</script>

<template>
  <Page auto-content-height class="bid-portal-submission-form-page">
    <ElCard shadow="never">
      <template #header>
        <span>供应商参与信息</span>
      </template>
      <ElForm label-width="120px">
        <ElFormItem label="项目ID">
          <ElInputNumber v-model="baseForm.projectId" :min="1" />
        </ElFormItem>
        <ElFormItem label="标段ID">
          <ElInputNumber v-model="baseForm.lotId" :min="1" />
        </ElFormItem>
        <ElFormItem label="供应商名称">
          <ElInput v-model="baseForm.supplierNameSnapshot" />
        </ElFormItem>
        <ElFormItem label="信用代码">
          <ElInput v-model="baseForm.supplierCreditCode" />
        </ElFormItem>
        <ElFormItem label="联系人">
          <ElInput v-model="baseForm.contactName" />
        </ElFormItem>
        <ElFormItem label="联系电话">
          <ElInput v-model="baseForm.contactPhone" />
        </ElFormItem>
        <ElFormItem label="备注">
          <ElInput v-model="baseForm.remark" type="textarea" />
        </ElFormItem>
        <ElFormItem>
          <ElButton :icon="Plus" type="primary" @click="handleCreateRegistration">
            提交报名
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="bid-portal-submission-form-page__section" shadow="never">
      <template #header>
        <span>投标操作</span>
      </template>
      <ElForm label-width="120px">
        <ElFormItem label="报名ID">
          <ElInputNumber v-model="baseForm.registrationId" :min="1" />
        </ElFormItem>
        <ElFormItem label="投标ID">
          <ElInputNumber v-model="baseForm.submissionId" :min="1" />
        </ElFormItem>
        <ElFormItem label="版本号">
          <ElInputNumber v-model="baseForm.version" :min="0" />
        </ElFormItem>
        <ElFormItem label="报价金额">
          <ElInputNumber v-model="submitForm.priceAmount" :min="0" />
        </ElFormItem>
        <ElFormItem label="文件清单JSON">
          <ElInput v-model="submitForm.fileManifestJson" type="textarea" />
        </ElFormItem>
        <ElFormItem>
          <ElButton :icon="Plus" @click="handleCreateSubmission">
            创建投标记录
          </ElButton>
          <ElButton :icon="Check" type="success" @click="handleSubmitBid">
            提交投标
          </ElButton>
          <ElButton :icon="RefreshLeft" type="warning" @click="handleWithdrawBid">
            撤回投标
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-portal-submission-form-page__section {
  margin-top: 12px;
}
</style>
