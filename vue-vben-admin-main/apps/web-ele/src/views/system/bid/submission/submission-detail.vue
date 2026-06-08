<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type { SupportFileApi, SystemBidSubmissionApi } from '#/api';

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
  createBidSubmissionAttachmentApi,
  getBidSubmissionDetailApi,
  queryFilePageApi,
  submitBidSubmissionApi,
  withdrawBidSubmissionApi,
} from '#/api';
import { requestClient } from '#/api/request';

import {
  ATTACHMENT_FILE_CATEGORY_OPTIONS,
  formatBidFileSize,
  getAttachmentCategoryText,
  getStatusTagType,
  getSubmissionStatusText,
  hasBidAction,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidSubmissionDetail',
});

const route = useRoute();
const router = useRouter();
const accessStore = useAccessStore();

const loading = ref(false);
const attachmentDialogVisible = ref(false);
const attachmentSaving = ref(false);
const fileLoading = ref(false);
const submitDialogVisible = ref(false);
const submitSaving = ref(false);
const submissionDetail =
  ref<null | SystemBidSubmissionApi.SubmissionItem>(null);
const attachmentFormRef = ref<FormInstance>();
const submitFormRef = ref<FormInstance>();
const fileList = ref<SupportFileApi.FileItem[]>([]);
const selectedFile = ref<null | SupportFileApi.FileItem>(null);
const fileTotal = ref(0);
const submissionId = computed(() => parseRouteNumber(route.query.submissionId));
const attachmentDialogMode = ref<'direct' | 'submit'>('direct');

interface SubmissionAttachmentDraft
  extends SystemBidSubmissionApi.AttachmentCreateParams {
  fileName?: string;
  fileSize?: null | number;
}

const fileQuery = reactive({
  keyword: '',
  pageNum: 1,
  pageSize: 5,
});

const attachmentForm = reactive<SystemBidSubmissionApi.AttachmentCreateParams>({
  fileCategory: 'SUBMISSION_MAIN',
  fileId: undefined as unknown as number,
  mainFlag: true,
  sortNo: undefined,
  versionNo: undefined,
});

const submitForm = reactive<SystemBidSubmissionApi.SubmissionSubmitParams>({
  contactName: '',
  contactPhone: '',
  fileManifestJson: '',
  priceAmount: undefined,
  remark: '',
  submissionId: undefined as unknown as number,
  version: 0,
});

const submitAttachmentDrafts = ref<SubmissionAttachmentDraft[]>([]);

const attachmentRules: FormRules<typeof attachmentForm> = {
  fileCategory: [
    { message: '请选择附件分类', required: true, trigger: 'change' },
  ],
  fileId: [{ message: '请选择已有文件', required: true, trigger: 'change' }],
};

const submitRules: FormRules<typeof submitForm> = {
  contactName: [{ max: 100, message: '联系人不能超过 100 个字符', trigger: 'blur' }],
  contactPhone: [
    { max: 50, message: '联系电话不能超过 50 个字符', trigger: 'blur' },
  ],
  fileManifestJson: [
    { max: 4000, message: '文件清单不能超过 4000 个字符', trigger: 'blur' },
  ],
};

const submissionAttachmentCategoryOptions = computed(() => {
  return ATTACHMENT_FILE_CATEGORY_OPTIONS.filter((item) =>
    ['OTHER', 'SUBMISSION_MAIN', 'SUBMISSION_PRICE'].includes(String(item.value)),
  );
});

const canSubmit = computed(() => {
  return hasBidAction({
    accessCode: 'bid:submission:submit-bid',
    accessCodes: accessStore.accessCodes,
    allowedAction: 'submit-bid',
    allowedActions: submissionDetail.value?.allowedActions,
  });
});

const canWithdraw = computed(() => {
  return hasBidAction({
    accessCode: 'bid:submission:withdraw-bid',
    accessCodes: accessStore.accessCodes,
    allowedAction: 'withdraw-bid',
    allowedActions: submissionDetail.value?.allowedActions,
  });
});

const canLinkAttachment = computed(() => {
  return canSubmit.value;
});

const canOpenAttachment = computed(() => {
  return accessStore.accessCodes.includes('bid:submission:download');
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
    if (route.query.openSubmit === '1') {
      await nextTick();
      if (canSubmit.value) {
        await openSubmitDialog();
      } else {
        ElMessage.warning('当前投标记录不允许提交');
      }
      const query = { ...route.query };
      delete query.openSubmit;
      await router.replace({ path: route.path, query });
    }
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

function resetSubmitForm() {
  if (!submissionDetail.value) {
    return;
  }

  submitForm.submissionId = submissionDetail.value.submissionId;
  submitForm.version = submissionDetail.value.version;
  submitForm.priceAmount = submissionDetail.value.priceAmount ?? undefined;
  submitForm.contactName = submissionDetail.value.contactName ?? '';
  submitForm.contactPhone = submissionDetail.value.contactPhone ?? '';
  submitForm.fileManifestJson = submissionDetail.value.fileManifestJson ?? '';
  submitForm.remark = '';
  submitAttachmentDrafts.value = [];
}

async function openSubmitDialog() {
  if (!submissionDetail.value) {
    return;
  }
  resetSubmitForm();
  submitDialogVisible.value = true;
  await nextTick();
  submitFormRef.value?.clearValidate();
}

async function handleSubmit() {
  await openSubmitDialog();
}

async function submitBid() {
  if (!submissionDetail.value) {
    return;
  }

  const valid = await submitFormRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  submitSaving.value = true;
  try {
    await submitBidSubmissionApi({
      contactName: submitForm.contactName?.trim() || undefined,
      contactPhone: submitForm.contactPhone?.trim() || undefined,
      fileManifestJson: submitForm.fileManifestJson?.trim() || undefined,
      priceAmount: submitForm.priceAmount ?? undefined,
      remark: submitForm.remark?.trim() || undefined,
      submissionId: submissionDetail.value.submissionId,
      version: submissionDetail.value.version,
    });

    let linkedCount = 0;
    let failedCount = 0;
    for (const item of submitAttachmentDrafts.value) {
      try {
        await createBidSubmissionAttachmentApi(submissionDetail.value.submissionId, {
          fileCategory: item.fileCategory,
          fileId: item.fileId,
          mainFlag: item.mainFlag,
          sortNo: item.sortNo,
        });
        linkedCount += 1;
      } catch {
        failedCount += 1;
      }
    }

    if (failedCount > 0) {
      ElMessage.warning(
        `投标已提交，已关联 ${linkedCount} 个附件，${failedCount} 个附件关联失败`,
      );
    } else {
      ElMessage.success(
        linkedCount > 0 ? '投标已提交，附件已关联' : '投标已提交',
      );
    }
    submitDialogVisible.value = false;
    await loadDetail();
  } finally {
    submitSaving.value = false;
  }
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
  if (!canOpenAttachment.value) {
    ElMessage.warning('当前账号没有投标附件下载权限');
    return;
  }
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

async function handlePreviewAttachment(
  attachment: SystemBidSubmissionApi.AttachmentItem,
) {
  if (!canOpenAttachment.value) {
    ElMessage.warning('当前账号没有投标附件预览权限');
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
  attachmentForm.fileCategory = 'SUBMISSION_MAIN';
  attachmentForm.fileId = undefined as unknown as number;
  attachmentForm.mainFlag = true;
  attachmentForm.sortNo = undefined;
  attachmentForm.versionNo = submissionDetail.value?.latestVersionNo;
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

async function openAttachmentDialog(mode: 'direct' | 'submit' = 'direct') {
  if (mode === 'direct' && !submissionDetail.value?.latestSubmissionVersionId) {
    ElMessage.warning('请先提交投标后再关联附件，或在提交弹窗中选择文件');
    return;
  }

  attachmentDialogMode.value = mode;
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

function removeSubmitAttachmentDraft(fileId: number) {
  submitAttachmentDrafts.value = submitAttachmentDrafts.value.filter(
    (item) => item.fileId !== fileId,
  );
}

async function submitAttachment() {
  if (!submissionDetail.value) {
    return;
  }

  const valid = await attachmentFormRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }
  if (!selectedFile.value) {
    ElMessage.warning('请选择已有文件');
    return;
  }

  if (attachmentDialogMode.value === 'submit') {
    const draft: SubmissionAttachmentDraft = {
      fileCategory: attachmentForm.fileCategory,
      fileId: attachmentForm.fileId,
      fileName: selectedFile.value.fileName,
      fileSize: selectedFile.value.fileSize,
      mainFlag: attachmentForm.mainFlag,
      sortNo: attachmentForm.sortNo,
    };
    submitAttachmentDrafts.value = [
      ...submitAttachmentDrafts.value.filter(
        (item) => item.fileId !== draft.fileId,
      ),
      draft,
    ];
    attachmentDialogVisible.value = false;
    return;
  }

  attachmentSaving.value = true;
  try {
    await createBidSubmissionAttachmentApi(submissionDetail.value.submissionId, {
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
        <div class="bid-submission-detail-page__card-header">
          <span>附件清单</span>
          <ElButton
            v-if="canLinkAttachment"
            :icon="Link"
            size="small"
            type="primary"
            @click="openAttachmentDialog('direct')"
          >
            关联已有文件
          </ElButton>
        </div>
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
      v-model="submitDialogVisible"
      title="提交投标"
      width="720px"
      append-to-body
    >
      <ElForm
        ref="submitFormRef"
        :model="submitForm"
        :rules="submitRules"
        label-width="104px"
      >
        <div class="bid-submission-detail-page__submit-grid">
          <ElFormItem label="报价金额">
            <ElInputNumber
              v-model="submitForm.priceAmount"
              :min="0"
              :precision="2"
              controls-position="right"
              placeholder="可留空"
            />
          </ElFormItem>
          <ElFormItem label="联系人" prop="contactName">
            <ElInput
              v-model="submitForm.contactName"
              maxlength="100"
              placeholder="请输入联系人"
            />
          </ElFormItem>
          <ElFormItem label="联系电话" prop="contactPhone">
            <ElInput
              v-model="submitForm.contactPhone"
              maxlength="50"
              placeholder="请输入联系电话"
            />
          </ElFormItem>
        </div>

        <ElFormItem label="文件清单" prop="fileManifestJson">
          <ElInput
            v-model="submitForm.fileManifestJson"
            :rows="4"
            maxlength="4000"
            placeholder="请输入本次投标文件清单，支持 JSON 或文本说明"
            show-word-limit
            type="textarea"
          />
        </ElFormItem>

        <ElFormItem label="本次附件">
          <div class="bid-submission-detail-page__submit-attachments">
            <div class="bid-submission-detail-page__submit-attachment-toolbar">
              <ElButton
                :icon="Link"
                size="small"
                type="primary"
                @click="openAttachmentDialog('submit')"
              >
                关联已有文件
              </ElButton>
            </div>
            <ElTable
              v-if="submitAttachmentDrafts.length"
              :data="submitAttachmentDrafts"
              border
              size="small"
            >
              <ElTableColumn label="文件名称" min-width="220" prop="fileName" />
              <ElTableColumn label="分类" min-width="120">
                <template #default="{ row }">
                  {{ getAttachmentCategoryText(row.fileCategory) }}
                </template>
              </ElTableColumn>
              <ElTableColumn label="大小" min-width="100">
                <template #default="{ row }">
                  {{ formatBidFileSize(row.fileSize) }}
                </template>
              </ElTableColumn>
              <ElTableColumn fixed="right" label="操作" width="80">
                <template #default="{ row }">
                  <ElButton
                    link
                    type="danger"
                    @click="removeSubmitAttachmentDraft(row.fileId)"
                  >
                    移除
                  </ElButton>
                </template>
              </ElTableColumn>
            </ElTable>
            <div v-else class="bid-submission-detail-page__submit-empty">
              暂未选择本次关联附件
            </div>
          </div>
        </ElFormItem>

        <ElFormItem label="提交备注">
          <ElInput
            v-model="submitForm.remark"
            :rows="2"
            maxlength="500"
            placeholder="可选，填写本次提交备注"
            type="textarea"
          />
        </ElFormItem>
      </ElForm>

      <template #footer>
        <ElButton @click="submitDialogVisible = false">取消</ElButton>
        <ElButton :loading="submitSaving" type="primary" @click="submitBid">
          确认提交
        </ElButton>
      </template>
    </ElDialog>

    <ElDialog
      v-model="attachmentDialogVisible"
      :title="attachmentDialogMode === 'submit' ? '选择本次投标附件' : '关联已有文件'"
      width="840px"
      append-to-body
    >
      <ElForm
        ref="attachmentFormRef"
        :model="attachmentForm"
        :rules="attachmentRules"
        label-width="92px"
      >
        <div class="bid-submission-detail-page__file-search">
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

        <div class="bid-submission-detail-page__file-pagination">
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

        <div class="bid-submission-detail-page__attachment-form">
          <ElFormItem label="附件分类" prop="fileCategory">
            <ElSelect
              v-model="attachmentForm.fileCategory"
              placeholder="请选择附件分类"
            >
              <ElOption
                v-for="item in submissionAttachmentCategoryOptions"
                :key="String(item.value)"
                :label="item.label"
                :value="String(item.value)"
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="版本号">
            <ElInputNumber
              v-model="attachmentForm.versionNo"
              :disabled="attachmentDialogMode === 'submit'"
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
          {{ attachmentDialogMode === 'submit' ? '加入本次附件' : '确认关联' }}
        </ElButton>
      </template>
    </ElDialog>
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

.bid-submission-detail-page__card-header,
.bid-submission-detail-page__file-search,
.bid-submission-detail-page__submit-attachment-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.bid-submission-detail-page__file-search {
  margin-bottom: 12px;
}

.bid-submission-detail-page__file-search .el-input {
  max-width: 320px;
}

.bid-submission-detail-page__file-pagination {
  display: flex;
  justify-content: flex-end;
  margin: 8px 0 16px;
}

.bid-submission-detail-page__attachment-form,
.bid-submission-detail-page__submit-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 16px;
}

.bid-submission-detail-page__submit-attachments {
  width: 100%;
}

.bid-submission-detail-page__submit-attachment-toolbar {
  justify-content: flex-start;
  margin-bottom: 8px;
}

.bid-submission-detail-page__submit-empty {
  padding: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-light);
  border: 1px dashed var(--el-border-color);
  border-radius: 4px;
}
</style>
