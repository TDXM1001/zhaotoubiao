<script lang="ts" setup>
import type { SystemBidTenderApi } from '#/api';

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
  Edit,
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
  clarifyBidTenderApi,
  getBidTenderDetailApi,
  publishBidTenderApi,
  withdrawBidTenderApi,
} from '#/api';
import { requestClient } from '#/api/request';

import {
  formatBidFileSize,
  getAttachmentCategoryText,
  getStatusTagType,
  getTenderStatusText,
  getTenderVersionTypeText,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidTenderDetail',
});

const route = useRoute();
const router = useRouter();
const accessStore = useAccessStore();

const loading = ref(false);
const tenderDetail = ref<null | SystemBidTenderApi.TenderItem>(null);
const tenderVersionId = computed(() =>
  parseRouteNumber(route.query.tenderVersionId),
);

const canEdit = computed(() => {
  return accessStore.accessCodes.includes('bid:tender:update')
    && (tenderDetail.value?.allowedActions?.includes('edit-tender') ?? false);
});

const canPublish = computed(() => {
  return accessStore.accessCodes.includes('bid:tender:publish-tender')
    && (tenderDetail.value?.allowedActions?.includes('publish-tender') ?? false);
});

const canClarify = computed(() => {
  return accessStore.accessCodes.includes('bid:tender:clarify-tender')
    && (tenderDetail.value?.allowedActions?.includes('clarify-tender') ?? false);
});

const canWithdraw = computed(() => {
  return accessStore.accessCodes.includes('bid:tender:withdraw-tender')
    && (tenderDetail.value?.allowedActions?.includes('withdraw-tender') ?? false);
});

function getListQuery() {
  if (!tenderDetail.value) {
    return undefined;
  }

  return {
    lotId: String(tenderDetail.value.lotId),
    lotName: tenderDetail.value.lotName,
    projectId: String(tenderDetail.value.projectId),
    projectName: tenderDetail.value.projectName,
  };
}

async function loadDetail() {
  if (!tenderVersionId.value) {
    ElMessage.warning('缺少招标文件版本ID，无法查看详情');
    return;
  }

  loading.value = true;
  try {
    tenderDetail.value = await getBidTenderDetailApi(tenderVersionId.value);
  } catch {
    tenderDetail.value = null;
    ElMessage.warning('招标文件不存在或已被删除');
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push({
    path: '/system/bid/tender/list',
    query: getListQuery(),
  });
}

function openEditPage() {
  if (!tenderDetail.value) {
    return;
  }

  router.push({
    path: '/system/bid/tender/create',
    query: {
      tenderVersionId: String(tenderDetail.value.tenderVersionId),
    },
  });
}

async function handlePublish() {
  if (!tenderDetail.value) {
    return;
  }

  await ElMessageBox.confirm(
    `确定发布标段“${tenderDetail.value.lotName}”的第 ${tenderDetail.value.versionNo} 版招标文件吗？`,
    '发布招标文件',
    { type: 'warning' },
  );

  await publishBidTenderApi({
    tenderVersionId: tenderDetail.value.tenderVersionId,
    version: tenderDetail.value.version,
  });
  ElMessage.success('招标文件已发布');
  await loadDetail();
}

async function handleClarify() {
  if (!tenderDetail.value) {
    return;
  }

  const { value } = await ElMessageBox.prompt(
    `请输入标段“${tenderDetail.value.lotName}”本次澄清说明`,
    '发布澄清文件',
    {
      confirmButtonText: '确认发布',
      inputErrorMessage: '澄清说明不能为空',
      inputPattern: /\S+/,
      inputPlaceholder: '请输入澄清说明',
      type: 'warning',
    },
  );

  await clarifyBidTenderApi({
    remark: String(value).trim(),
    tenderVersionId: tenderDetail.value.tenderVersionId,
    version: tenderDetail.value.version,
  });
  ElMessage.success('澄清文件已发布');
  await loadDetail();
}

async function handleWithdraw() {
  if (!tenderDetail.value) {
    return;
  }

  const { value } = await ElMessageBox.prompt(
    `请输入撤回标段“${tenderDetail.value.lotName}”招标文件的原因`,
    '撤回招标文件',
    {
      confirmButtonText: '确认撤回',
      inputErrorMessage: '撤回原因不能为空',
      inputPattern: /\S+/,
      inputPlaceholder: '请输入撤回原因',
      type: 'warning',
    },
  );

  await withdrawBidTenderApi({
    remark: String(value).trim(),
    tenderVersionId: tenderDetail.value.tenderVersionId,
    version: tenderDetail.value.version,
  });
  ElMessage.success('招标文件已撤回');
  await loadDetail();
}

async function handleDownloadAttachment(
  attachment: SystemBidTenderApi.AttachmentItem,
) {
  if (!attachment.downloadUrl) {
    ElMessage.warning('附件下载地址不存在');
    return;
  }

  const blob = await requestClient.download<Blob>(attachment.downloadUrl);
  downloadFileFromBlob({
    fileName: attachment.fileName || '招标附件',
    source: blob,
  });
}

onMounted(() => {
  void loadDetail();
});
</script>

<template>
  <Page auto-content-height class="bid-tender-detail-page">
    <div class="bid-tender-detail-page__toolbar">
      <div class="bid-tender-detail-page__title">招标文件详情</div>
      <div class="bid-tender-detail-page__actions">
        <ElButton :icon="ArrowLeft" @click="goBack">返回</ElButton>
        <ElButton
          v-if="canEdit"
          :icon="Edit"
          type="primary"
          @click="openEditPage"
        >
          编辑
        </ElButton>
        <ElButton
          v-if="canPublish"
          :icon="Check"
          type="success"
          @click="handlePublish"
        >
          发布
        </ElButton>
        <ElButton
          v-if="canClarify"
          :icon="Check"
          type="warning"
          @click="handleClarify"
        >
          澄清
        </ElButton>
        <ElButton
          v-if="canWithdraw"
          :icon="Close"
          type="danger"
          @click="handleWithdraw"
        >
          撤回
        </ElButton>
      </div>
    </div>

    <ElCard v-loading="loading" shadow="never">
      <ElDescriptions v-if="tenderDetail" :column="2" border>
        <ElDescriptionsItem label="所属项目">
          {{ tenderDetail.projectName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="所属标段">
          {{ tenderDetail.lotName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="版本类型">
          {{ getTenderVersionTypeText(tenderDetail.versionType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="版本号">
          第 {{ tenderDetail.versionNo }} 版
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          <ElTag :type="getStatusTagType(tenderDetail.status)">
            {{ getTenderStatusText(tenderDetail.status) }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="当前有效">
          {{ tenderDetail.currentFlag ? '是' : '否' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="发布时间">
          {{ tenderDetail.publishTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="生效时间">
          {{ tenderDetail.effectiveTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="上级版本ID">
          {{ tenderDetail.parentVersionId || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="更新时间">
          {{ tenderDetail.updateTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="版本摘要" :span="2">
          {{ tenderDetail.summary || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注" :span="2">
          {{ tenderDetail.remark || '--' }}
        </ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>

    <ElCard
      v-if="tenderDetail"
      class="bid-tender-detail-page__attachments"
      shadow="never"
    >
      <template #header>
        <span>附件清单</span>
      </template>
      <ElTable :data="tenderDetail.attachments ?? []" border>
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
.bid-tender-detail-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-tender-detail-page__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.bid-tender-detail-page__actions {
  display: inline-flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.bid-tender-detail-page__attachments {
  margin-top: 12px;
}
</style>
