<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type { SupportFileApi, SystemBidTenderApi } from '#/api';

import { computed, nextTick, onMounted, reactive, ref } from 'vue';
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
  Link,
  Search,
  View,
} from '@element-plus/icons-vue';
import {
  ElButton,
  ElCard,
  ElCheckbox,
  ElDescriptions,
  ElDescriptionsItem,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {
  clarifyBidTenderApi,
  createBidTenderAttachmentApi,
  getBidTenderDetailApi,
  publishBidTenderApi,
  queryFilePageApi,
  withdrawBidTenderApi,
} from '#/api';
import { requestClient } from '#/api/request';

import {
  ATTACHMENT_FILE_CATEGORY_OPTIONS,
  formatBidFileSize,
  getAttachmentCategoryText,
  getStatusTagType,
  getTenderStatusText,
  getTenderVersionTypeText,
  hasBidAction,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidTenderDetail',
});

const route = useRoute();
const router = useRouter();
const accessStore = useAccessStore();

const loading = ref(false);
const attachmentSaving = ref(false);
const fileLoading = ref(false);
const attachmentDialogVisible = ref(false);
const tenderDetail = ref<null | SystemBidTenderApi.TenderItem>(null);
const attachmentFormRef = ref<FormInstance>();
const fileList = ref<SupportFileApi.FileItem[]>([]);
const selectedFile = ref<null | SupportFileApi.FileItem>(null);
const fileTotal = ref(0);
const tenderVersionId = computed(() =>
  parseRouteNumber(route.query.tenderVersionId),
);

const fileQuery = reactive({
  keyword: '',
  pageNum: 1,
  pageSize: 5,
});

const attachmentForm = reactive<SystemBidTenderApi.AttachmentCreateParams>({
  fileCategory: 'TENDER_MAIN',
  fileId: undefined as unknown as number,
  mainFlag: true,
  sortNo: undefined,
  versionNo: undefined,
});

const attachmentRules: FormRules<typeof attachmentForm> = {
  fileCategory: [
    { message: '请选择附件分类', required: true, trigger: 'change' },
  ],
  fileId: [{ message: '请选择已有文件', required: true, trigger: 'change' }],
};

const tenderAttachmentCategoryOptions = computed(() => {
  return ATTACHMENT_FILE_CATEGORY_OPTIONS.filter((item) =>
    ['OTHER', 'TENDER_ANNOUNCEMENT', 'TENDER_CLARIFICATION', 'TENDER_MAIN'].includes(
      String(item.value),
    ),
  );
});

const canEdit = computed(() => {
  return hasBidAction({
    accessCode: 'bid:tender:update',
    accessCodes: accessStore.accessCodes,
    allowedAction: 'edit-tender',
    allowedActions: tenderDetail.value?.allowedActions,
  });
});

const canPublish = computed(() => {
  return hasBidAction({
    accessCode: 'bid:tender:publish-tender',
    accessCodes: accessStore.accessCodes,
    allowedAction: 'publish-tender',
    allowedActions: tenderDetail.value?.allowedActions,
  });
});

const canClarify = computed(() => {
  return hasBidAction({
    accessCode: 'bid:tender:clarify-tender',
    accessCodes: accessStore.accessCodes,
    allowedAction: 'clarify-tender',
    allowedActions: tenderDetail.value?.allowedActions,
  });
});

const canWithdraw = computed(() => {
  return hasBidAction({
    accessCode: 'bid:tender:withdraw-tender',
    accessCodes: accessStore.accessCodes,
    allowedAction: 'withdraw-tender',
    allowedActions: tenderDetail.value?.allowedActions,
  });
});

const canLinkAttachment = computed(() => {
  return canEdit.value;
});

const canOpenAttachment = computed(() => {
  return accessStore.accessCodes.includes('bid:tender:query');
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
  if (!canOpenAttachment.value) {
    ElMessage.warning('当前账号没有招标文件附件下载权限');
    return;
  }
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

async function handlePreviewAttachment(
  attachment: SystemBidTenderApi.AttachmentItem,
) {
  if (!canOpenAttachment.value) {
    ElMessage.warning('当前账号没有招标文件附件预览权限');
    return;
  }
  if (!attachment.previewUrl) {
    ElMessage.warning('附件预览地址不存在');
    return;
  }

  const previewUrl = await requestClient.get<string>(attachment.previewUrl);
  if (!previewUrl) {
    ElMessage.warning('附件预览地址不存在');
    return;
  }
  window.open(previewUrl, '_blank', 'noopener,noreferrer');
}

function resetAttachmentForm() {
  selectedFile.value = null;
  fileQuery.keyword = '';
  fileQuery.pageNum = 1;
  attachmentForm.fileCategory = 'TENDER_MAIN';
  attachmentForm.fileId = undefined as unknown as number;
  attachmentForm.mainFlag = true;
  attachmentForm.sortNo = undefined;
  attachmentForm.versionNo = tenderDetail.value?.versionNo;
}

async function loadFileList() {
  fileLoading.value = true;
  try {
    const keyword = fileQuery.keyword.trim();
    const result = await queryFilePageApi({
      fileName: keyword || undefined,
      pageNum: fileQuery.pageNum,
      pageSize: fileQuery.pageSize,
      searchCount: true,
    });
    fileList.value = result.list ?? [];
    fileTotal.value = result.total ?? 0;
  } finally {
    fileLoading.value = false;
  }
}

async function openAttachmentDialog() {
  resetAttachmentForm();
  attachmentDialogVisible.value = true;
  await nextTick();
  attachmentFormRef.value?.clearValidate();
  await loadFileList();
}

function handleFileSearch() {
  fileQuery.pageNum = 1;
  void loadFileList();
}

function handleFilePageChange(pageNum: number) {
  fileQuery.pageNum = pageNum;
  void loadFileList();
}

function handleFilePageSizeChange(pageSize: number) {
  fileQuery.pageNum = 1;
  fileQuery.pageSize = pageSize;
  void loadFileList();
}

function selectFile(row: SupportFileApi.FileItem) {
  selectedFile.value = row;
  attachmentForm.fileId = row.fileId;
  attachmentFormRef.value?.validateField('fileId');
}

async function submitAttachment() {
  if (!tenderDetail.value) {
    return;
  }

  const valid = await attachmentFormRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  attachmentSaving.value = true;
  try {
    await createBidTenderAttachmentApi(tenderDetail.value.tenderVersionId, {
      fileCategory: attachmentForm.fileCategory,
      fileId: attachmentForm.fileId,
      mainFlag: attachmentForm.mainFlag,
      sortNo: attachmentForm.sortNo,
      versionNo: attachmentForm.versionNo,
    });
    ElMessage.success('附件已关联');
    attachmentDialogVisible.value = false;
    await loadDetail();
  } finally {
    attachmentSaving.value = false;
  }
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
        <div class="bid-tender-detail-page__card-header">
          <span>附件清单</span>
          <ElButton
            v-if="canLinkAttachment"
            :icon="Link"
            size="small"
            type="primary"
            @click="openAttachmentDialog"
          >
            关联已有文件
          </ElButton>
        </div>
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
        <ElTableColumn fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <ElButton
              v-if="canOpenAttachment"
              :icon="View"
              link
              type="primary"
              @click="handlePreviewAttachment(row)"
            >
              预览
            </ElButton>
            <ElButton
              v-if="canOpenAttachment"
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

    <ElDialog
      v-model="attachmentDialogVisible"
      title="关联已有文件"
      width="840px"
      append-to-body
    >
      <ElForm
        ref="attachmentFormRef"
        :model="attachmentForm"
        :rules="attachmentRules"
        label-width="92px"
      >
        <div class="bid-tender-detail-page__file-search">
          <ElInput
            v-model="fileQuery.keyword"
            clearable
            placeholder="按文件名称搜索"
            @keyup.enter="handleFileSearch"
          />
          <ElButton :icon="Search" :loading="fileLoading" @click="handleFileSearch">
            搜索
          </ElButton>
        </div>

        <ElFormItem label="选择文件" prop="fileId">
          <ElTable
            v-loading="fileLoading"
            :data="fileList"
            border
            highlight-current-row
            max-height="260"
            row-key="fileId"
            @row-click="selectFile"
          >
            <ElTableColumn width="54">
              <template #default="{ row }">
                <ElCheckbox
                  :model-value="selectedFile?.fileId === row.fileId"
                  @change="selectFile(row)"
                />
              </template>
            </ElTableColumn>
            <ElTableColumn label="文件名称" min-width="220" prop="fileName" />
            <ElTableColumn label="类型" min-width="90" prop="fileType" />
            <ElTableColumn label="大小" min-width="110">
              <template #default="{ row }">
                {{ formatBidFileSize(row.fileSize) }}
              </template>
            </ElTableColumn>
            <ElTableColumn label="上传人" min-width="120" prop="creatorName" />
            <ElTableColumn label="创建时间" min-width="170" prop="createTime" />
          </ElTable>
        </ElFormItem>

        <div class="bid-tender-detail-page__file-pagination">
          <ElPagination
            :current-page="fileQuery.pageNum"
            :page-size="fileQuery.pageSize"
            :page-sizes="[5, 10, 20]"
            :total="fileTotal"
            background
            layout="total, sizes, prev, pager, next"
            small
            @current-change="handleFilePageChange"
            @size-change="handleFilePageSizeChange"
          />
        </div>

        <div class="bid-tender-detail-page__attachment-form">
          <ElFormItem label="附件分类" prop="fileCategory">
            <ElSelect v-model="attachmentForm.fileCategory" placeholder="请选择附件分类">
              <ElOption
                v-for="item in tenderAttachmentCategoryOptions"
                :key="String(item.value)"
                :label="item.label"
                :value="String(item.value)"
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="版本号">
            <ElInputNumber
              v-model="attachmentForm.versionNo"
              :min="1"
              controls-position="right"
            />
          </ElFormItem>
          <ElFormItem label="排序号">
            <ElInputNumber
              v-model="attachmentForm.sortNo"
              :min="0"
              controls-position="right"
            />
          </ElFormItem>
          <ElFormItem label="主文件">
            <ElCheckbox v-model="attachmentForm.mainFlag">设为主文件</ElCheckbox>
          </ElFormItem>
        </div>
      </ElForm>

      <template #footer>
        <ElButton @click="attachmentDialogVisible = false">取消</ElButton>
        <ElButton
          :loading="attachmentSaving"
          type="primary"
          @click="submitAttachment"
        >
          确认关联
        </ElButton>
      </template>
    </ElDialog>
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

.bid-tender-detail-page__card-header,
.bid-tender-detail-page__file-search {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.bid-tender-detail-page__file-search {
  margin-bottom: 12px;
}

.bid-tender-detail-page__file-search .el-input {
  max-width: 320px;
}

.bid-tender-detail-page__file-pagination {
  display: flex;
  justify-content: flex-end;
  margin: 8px 0 16px;
}

.bid-tender-detail-page__attachment-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 16px;
}
</style>
