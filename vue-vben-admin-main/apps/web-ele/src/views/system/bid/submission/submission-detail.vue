<script lang="ts" setup>
import type { SystemBidSubmissionApi } from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';
import { downloadFileFromBlob } from '@vben/utils';

import {
  ArrowLeft,
  Check,
  Close,
  Download,
} from '@element-plus/icons-vue';
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
  getBidSubmissionDetailApi,
  submitBidSubmissionApi,
  withdrawBidSubmissionApi,
} from '#/api';
import { requestClient } from '#/api/request';

import {
  formatBidFileSize,
  getAttachmentCategoryText,
  getStatusTagType,
  getSubmissionStatusText,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidSubmissionDetail',
});

const route = useRoute();
const router = useRouter();
const accessStore = useAccessStore();

const loading = ref(false);
const submissionDetail =
  ref<null | SystemBidSubmissionApi.SubmissionItem>(null);
const submissionId = computed(() => parseRouteNumber(route.query.submissionId));

const canSubmit = computed(() => {
  return accessStore.accessCodes.includes('bid:submission:submit-bid')
    && (submissionDetail.value?.allowedActions?.includes('submit-bid') ?? false);
});

const canWithdraw = computed(() => {
  return accessStore.accessCodes.includes('bid:submission:withdraw-bid')
    && (submissionDetail.value?.allowedActions?.includes('withdraw-bid') ?? false);
});

function getListQuery() {
  if (!submissionDetail.value) {
    return undefined;
  }

  return {
    lotId: String(submissionDetail.value.lotId),
    lotName: submissionDetail.value.lotName,
    projectId: String(submissionDetail.value.projectId),
    projectName: submissionDetail.value.projectName,
  };
}

async function loadDetail() {
  if (!submissionId.value) {
    ElMessage.warning('缺少投标ID，无法查看详情');
    return;
  }

  loading.value = true;
  try {
    submissionDetail.value = await getBidSubmissionDetailApi(submissionId.value);
  } catch {
    submissionDetail.value = null;
    ElMessage.warning('投标记录不存在或已被删除');
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push({
    path: '/system/bid/submission/list',
    query: getListQuery(),
  });
}

async function handleSubmit() {
  if (!submissionDetail.value) {
    return;
  }

  const { value } = await ElMessageBox.prompt(
    `请输入供应商“${submissionDetail.value.supplierNameSnapshot}”本次投标报价，可留空`,
    '提交投标',
    {
      confirmButtonText: '确认提交',
      inputPlaceholder: '请输入报价金额',
      type: 'warning',
    },
  );
  const amountText = String(value ?? '').trim();
  const priceAmount = amountText === '' ? undefined : Number(amountText);
  if (priceAmount !== undefined && !Number.isFinite(priceAmount)) {
    ElMessage.warning('报价金额格式不正确');
    return;
  }

  await submitBidSubmissionApi({
    priceAmount,
    submissionId: submissionDetail.value.submissionId,
    version: submissionDetail.value.version,
  });
  ElMessage.success('投标已提交');
  await loadDetail();
}

async function handleWithdraw() {
  if (!submissionDetail.value) {
    return;
  }

  const { value } = await ElMessageBox.prompt(
    `请输入撤回供应商“${submissionDetail.value.supplierNameSnapshot}”投标的原因`,
    '撤回投标',
    {
      confirmButtonText: '确认撤回',
      inputErrorMessage: '撤回原因不能为空',
      inputPattern: /\S+/,
      inputPlaceholder: '请输入撤回原因',
      type: 'warning',
    },
  );

  await withdrawBidSubmissionApi({
    remark: String(value).trim(),
    submissionId: submissionDetail.value.submissionId,
    version: submissionDetail.value.version,
  });
  ElMessage.success('投标已撤回');
  await loadDetail();
}

async function handleDownloadAttachment(
  attachment: SystemBidSubmissionApi.AttachmentItem,
) {
  if (!attachment.downloadUrl) {
    ElMessage.warning('附件下载地址不存在');
    return;
  }

  const blob = await requestClient.download<Blob>(attachment.downloadUrl);
  downloadFileFromBlob({
    fileName: attachment.fileName || '投标附件',
    source: blob,
  });
}

onMounted(() => {
  void loadDetail();
});
</script>

<template>
  <Page auto-content-height class="bid-submission-detail-page">
    <div class="bid-submission-detail-page__toolbar">
      <div class="bid-submission-detail-page__title">投标详情</div>
      <div class="bid-submission-detail-page__actions">
        <ElButton :icon="ArrowLeft" @click="goBack">返回</ElButton>
        <ElButton
          v-if="canSubmit"
          :icon="Check"
          type="success"
          @click="handleSubmit"
        >
          提交
        </ElButton>
        <ElButton
          v-if="canWithdraw"
          :icon="Close"
          type="warning"
          @click="handleWithdraw"
        >
          撤回
        </ElButton>
      </div>
    </div>

    <ElCard v-loading="loading" shadow="never">
      <ElDescriptions v-if="submissionDetail" :column="2" border>
        <ElDescriptionsItem label="供应商名称">
          {{ submissionDetail.supplierNameSnapshot }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="统一社会信用代码">
          {{ submissionDetail.supplierCreditCode }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="所属项目">
          {{ submissionDetail.projectName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="所属标段">
          {{ submissionDetail.lotName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="报名ID">
          {{ submissionDetail.registrationId }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="投标状态">
          <ElTag :type="getStatusTagType(submissionDetail.status)">
            {{ getSubmissionStatusText(submissionDetail.status) }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="最新版本">
          第 {{ submissionDetail.latestVersionNo }} 版
        </ElDescriptionsItem>
        <ElDescriptionsItem label="回执号">
          {{ submissionDetail.receiptNo || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="报价金额">
          {{ submissionDetail.priceAmount ?? '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="最近提交时间">
          {{ submissionDetail.latestSubmitTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="联系人">
          {{ submissionDetail.contactName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="联系电话">
          {{ submissionDetail.contactPhone || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="撤回时间">
          {{ submissionDetail.withdrawTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="更新时间">
          {{ submissionDetail.updateTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="撤回原因" :span="2">
          {{ submissionDetail.withdrawReason || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="投标文件清单" :span="2">
          <pre class="bid-submission-detail-page__manifest">{{
            submissionDetail.fileManifestJson || '--'
          }}</pre>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注" :span="2">
          {{ submissionDetail.remark || '--' }}
        </ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>

    <ElCard
      v-if="submissionDetail"
      class="bid-submission-detail-page__attachments"
      shadow="never"
    >
      <template #header>
        <span>附件清单</span>
      </template>
      <ElTable :data="submissionDetail.attachments ?? []" border>
        <ElTableColumn label="文件名称" min-width="220" prop="fileName" />
        <ElTableColumn label="分类" min-width="140">
          <template #default="{ row }">
            {{ getAttachmentCategoryText(row.fileCategory) }}
          </template>
        </ElTableColumn>
        <ElTableColumn label="版本" min-width="100">
          <template #default="{ row }">
            {{ row.versionNo ? `第 ${row.versionNo} 版` : '--' }}
          </template>
        </ElTableColumn>
        <ElTableColumn label="大小" min-width="110">
          <template #default="{ row }">
            {{ formatBidFileSize(row.fileSize) }}
          </template>
        </ElTableColumn>
        <ElTableColumn label="上传人" min-width="140" prop="uploaderName" />
        <ElTableColumn label="上传时间" min-width="180" prop="uploadTime" />
        <ElTableColumn fixed="right" label="操作" width="110">
          <template #default="{ row }">
            <ElButton
              :icon="Download"
              link
              type="primary"
              @click="handleDownloadAttachment(row)"
            >
              下载
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-submission-detail-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-submission-detail-page__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.bid-submission-detail-page__actions {
  display: inline-flex;
  gap: 8px;
}

.bid-submission-detail-page__manifest {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
}

.bid-submission-detail-page__attachments {
  margin-top: 12px;
}
</style>
