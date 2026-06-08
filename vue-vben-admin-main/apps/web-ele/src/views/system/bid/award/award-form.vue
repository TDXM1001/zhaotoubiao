<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type {
  SystemBidAwardApi,
  SystemBidEvaluationApi,
  SystemBidLotApi,
  SystemBidProjectApi,
  SystemBidSubmissionApi,
} from '#/api';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import { ArrowLeft, Check } from '@element-plus/icons-vue';
import {
  ElAlert,
  ElButton,
  ElCard,
  ElCol,
  ElDescriptions,
  ElDescriptionsItem,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElOption,
  ElRow,
  ElSelect,
  ElTag,
} from 'element-plus';

import {
  addBidAwardApi,
  getBidEvaluationDetailApi,
  getBidLotDetailApi,
  getBidSubmissionDetailApi,
  queryBidEvaluationPageApi,
  queryBidLotByProjectIdApi,
  queryBidProjectListApi,
  queryBidSubmissionPageApi,
} from '#/api';

import {
  getEvaluationStatusText,
  getLotStatusText,
  getProjectStatusText,
  getStatusTagType,
  getSubmissionStatusText,
  isAwardCandidateSubmission,
  isAwardReadyEvaluation,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidAwardForm',
});

const route = useRoute();
const router = useRouter();

const formRef = ref<FormInstance>();
const loading = ref(false);
const optionLoading = ref(false);
const saving = ref(false);
const projectOptions = ref<SystemBidProjectApi.ProjectOption[]>([]);
const allLotOptions = ref<SystemBidLotApi.LotItem[]>([]);
const evaluationOptions = ref<SystemBidEvaluationApi.EvaluationItem[]>([]);
const submissionOptions = ref<SystemBidSubmissionApi.SubmissionItem[]>([]);

const defaultProjectId = computed(() => parseRouteNumber(route.query.projectId));
const defaultLotId = computed(() => parseRouteNumber(route.query.lotId));
const defaultEvaluationId = computed(() =>
  parseRouteNumber(route.query.evaluationId),
);
const defaultSubmissionId = computed(() =>
  parseRouteNumber(route.query.submissionId),
);

const formModel = reactive<SystemBidAwardApi.AwardCreateParams>({
  awardAmount: undefined,
  evaluationId: defaultEvaluationId.value ?? (undefined as unknown as number),
  lotId: defaultLotId.value ?? (undefined as unknown as number),
  projectId: defaultProjectId.value ?? (undefined as unknown as number),
  remark: '',
  winningSubmissionId:
    defaultSubmissionId.value ?? (undefined as unknown as number),
});

const rules: FormRules<typeof formModel> = {
  evaluationId: [
    { message: '请选择已定稿评标记录', required: true, trigger: 'change' },
  ],
  lotId: [
    { message: '请选择所属标段', required: true, trigger: 'change' },
  ],
  projectId: [
    { message: '请选择所属项目', required: true, trigger: 'change' },
  ],
  winningSubmissionId: [
    { message: '请选择中标候选投标', required: true, trigger: 'change' },
  ],
};

const selectedProject = computed(() => {
  return projectOptions.value.find(
    (item) => item.projectId === formModel.projectId,
  );
});

const selectedLot = computed(() => {
  return allLotOptions.value.find((item) => item.lotId === formModel.lotId);
});

const finalizedEvaluationOptions = computed(() => {
  return evaluationOptions.value.filter(isAwardReadyEvaluation);
});

const candidateSubmissionOptions = computed(() => {
  return submissionOptions.value.filter(isAwardCandidateSubmission);
});

const selectedEvaluation = computed(() => {
  return evaluationOptions.value.find(
    (item) => item.evaluationId === formModel.evaluationId,
  );
});

const selectedSubmission = computed(() => {
  return submissionOptions.value.find(
    (item) => item.submissionId === formModel.winningSubmissionId,
  );
});

const lotSelectPlaceholder = computed(() => {
  return formModel.projectId ? '请选择所属标段' : '请先选择项目';
});

const evaluationSelectPlaceholder = computed(() => {
  return formModel.lotId ? '请选择已定稿评标记录' : '请先选择标段';
});

const submissionSelectPlaceholder = computed(() => {
  return formModel.lotId ? '请选择已开标投标' : '请先选择标段';
});

function formatProjectLabel(item: SystemBidProjectApi.ProjectOption) {
  return `${item.projectName}（${item.projectCode}）`;
}

function formatLotLabel(item: SystemBidLotApi.LotItem) {
  return `${item.lotName}（${item.lotCode}）`;
}

function formatAmount(value?: null | number) {
  if (value === null || value === undefined) {
    return '--';
  }
  return `￥${new Intl.NumberFormat('zh-CN', {
    maximumFractionDigits: 2,
    minimumFractionDigits: 2,
  }).format(value)}`;
}

function formatEvaluationLabel(item: SystemBidEvaluationApi.EvaluationItem) {
  const finalizeTime = item.finalizeTime ? ` / ${item.finalizeTime}` : '';
  return `${getEvaluationStatusText(item.status)}${finalizeTime}`;
}

function formatSubmissionLabel(item: SystemBidSubmissionApi.SubmissionItem) {
  const price =
    item.priceAmount === undefined ? '' : ` / ${formatAmount(item.priceAmount)}`;
  return `${item.supplierNameSnapshot}${price}`;
}

function applySubmissionDefaults(
  submission?: SystemBidSubmissionApi.SubmissionItem,
  force = false,
) {
  if (!submission) {
    return;
  }

  // 中标金额默认带入投标报价，允许用户按最终定标结果修正。
  if (
    submission.priceAmount !== null &&
    submission.priceAmount !== undefined &&
    (force || formModel.awardAmount === undefined)
  ) {
    formModel.awardAmount = submission.priceAmount;
  }
}

async function loadBaseOptions() {
  projectOptions.value = await queryBidProjectListApi();
}

async function initContextFromRouteEvaluation() {
  if (!formModel.evaluationId || (formModel.projectId && formModel.lotId)) {
    return;
  }

  const detail = await getBidEvaluationDetailApi(formModel.evaluationId);
  formModel.projectId = detail.projectId;
  formModel.lotId = detail.lotId;
}

async function initContextFromRouteSubmission() {
  if (
    !formModel.winningSubmissionId ||
    (formModel.projectId && formModel.lotId)
  ) {
    return;
  }

  const detail = await getBidSubmissionDetailApi(formModel.winningSubmissionId);
  formModel.projectId = detail.projectId;
  formModel.lotId = detail.lotId;
}

async function initProjectFromRouteLot() {
  if (!formModel.lotId || formModel.projectId) {
    return;
  }

  const detail = await getBidLotDetailApi(formModel.lotId);
  formModel.projectId = detail.projectId;
}

async function loadLotOptions(projectId?: number) {
  if (!projectId) {
    allLotOptions.value = [];
    return;
  }

  const lots = await queryBidLotByProjectIdApi(projectId);
  allLotOptions.value = lots;

  if (formModel.lotId && !lots.some((item) => item.lotId === formModel.lotId)) {
    formModel.lotId = undefined as unknown as number;
    formModel.evaluationId = undefined as unknown as number;
    formModel.winningSubmissionId = undefined as unknown as number;
    formModel.awardAmount = undefined;
  }
}

async function loadEvaluationOptions(projectId?: number, lotId?: number) {
  evaluationOptions.value = [];
  if (!projectId || !lotId) {
    return;
  }

  const result = await queryBidEvaluationPageApi({
    lotId,
    pageNum: 1,
    pageSize: 100,
    projectId,
    searchCount: true,
    status: 'FINALIZED',
  });
  evaluationOptions.value = result.list ?? [];

  if (
    formModel.evaluationId &&
    !finalizedEvaluationOptions.value.some(
      (item) => item.evaluationId === formModel.evaluationId,
    )
  ) {
    formModel.evaluationId = undefined as unknown as number;
  }
}

async function loadSubmissionOptions(projectId?: number, lotId?: number) {
  submissionOptions.value = [];
  if (!projectId || !lotId) {
    return;
  }

  const result = await queryBidSubmissionPageApi({
    lotId,
    pageNum: 1,
    pageSize: 500,
    projectId,
    searchCount: true,
    status: 'OPENED',
  });
  submissionOptions.value = result.list ?? [];

  const currentSubmission = candidateSubmissionOptions.value.find(
    (item) => item.submissionId === formModel.winningSubmissionId,
  );
  if (currentSubmission) {
    applySubmissionDefaults(currentSubmission);
    return;
  }

  if (formModel.winningSubmissionId) {
    formModel.winningSubmissionId = undefined as unknown as number;
    formModel.awardAmount = undefined;
  }
}

async function loadBusinessOptions() {
  optionLoading.value = true;
  try {
    await Promise.all([
      loadEvaluationOptions(formModel.projectId, formModel.lotId),
      loadSubmissionOptions(formModel.projectId, formModel.lotId),
    ]);
  } finally {
    optionLoading.value = false;
  }
}

function resetAwardSelection() {
  formModel.evaluationId = undefined as unknown as number;
  formModel.winningSubmissionId = undefined as unknown as number;
  formModel.awardAmount = undefined;
  evaluationOptions.value = [];
  submissionOptions.value = [];
}

function handleProjectChange() {
  formModel.lotId = undefined as unknown as number;
  resetAwardSelection();
  allLotOptions.value = [];
  void loadLotOptions(formModel.projectId);
}

function handleLotChange() {
  resetAwardSelection();
  void loadBusinessOptions();
}

function handleSubmissionChange(submissionId?: number) {
  const submission = submissionOptions.value.find(
    (item) => item.submissionId === submissionId,
  );
  applySubmissionDefaults(submission, true);
}

function getListQuery() {
  return {
    ...(formModel.projectId ? { projectId: String(formModel.projectId) } : {}),
    ...(formModel.lotId ? { lotId: String(formModel.lotId) } : {}),
  };
}

function goBack() {
  router.push({
    path: '/system/bid/award/list',
    query: getListQuery(),
  });
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  if (
    !selectedEvaluation.value ||
    !isAwardReadyEvaluation(selectedEvaluation.value)
  ) {
    ElMessage.warning('请选择已定稿评标记录后再创建定标记录');
    return;
  }

  if (
    !selectedSubmission.value ||
    !isAwardCandidateSubmission(selectedSubmission.value)
  ) {
    ElMessage.warning('请选择已开标投标后再创建定标记录');
    return;
  }

  // 接口契约保持为 ID 提交，页面负责把业务对象选择结果转换为后端参数。
  const payload: SystemBidAwardApi.AwardCreateParams = {
    awardAmount: formModel.awardAmount ?? undefined,
    evaluationId: formModel.evaluationId,
    lotId: formModel.lotId,
    projectId: formModel.projectId,
    remark: formModel.remark?.trim() || undefined,
    winningSubmissionId: formModel.winningSubmissionId,
  };

  saving.value = true;
  try {
    await addBidAwardApi(payload);
    ElMessage.success('定标记录已创建');
    goBack();
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  loading.value = true;
  try {
    await loadBaseOptions();
    await initContextFromRouteEvaluation();
    await initContextFromRouteSubmission();
    await initProjectFromRouteLot();
    await loadLotOptions(formModel.projectId);
    await loadBusinessOptions();
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <Page auto-content-height class="bid-award-form-page">
    <div class="bid-award-form-page__toolbar">
      <div class="bid-award-form-page__title">创建定标记录</div>
      <div class="bid-award-form-page__actions">
        <ElButton :icon="ArrowLeft" @click="goBack">返回</ElButton>
        <ElButton
          :icon="Check"
          :loading="saving"
          type="primary"
          @click="handleSubmit"
        >
          保存
        </ElButton>
      </div>
    </div>

    <ElCard shadow="never">
      <ElForm
        ref="formRef"
        v-loading="loading"
        :model="formModel"
        :rules="rules"
        label-width="112px"
      >
        <ElAlert
          class="bid-award-form-page__rule"
          :closable="false"
          show-icon
          title="仅已定稿评标且已开标投标可创建定标记录"
          type="info"
        />

        <ElRow :gutter="24">
          <ElCol :md="12" :xs="24">
            <ElFormItem label="所属项目" prop="projectId">
              <ElSelect
                v-model="formModel.projectId"
                class="bid-award-form-page__field"
                clearable
                filterable
                placeholder="请选择所属项目"
                @change="handleProjectChange"
              >
                <ElOption
                  v-for="item in projectOptions"
                  :key="item.projectId"
                  :label="formatProjectLabel(item)"
                  :value="item.projectId"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :md="12" :xs="24">
            <ElFormItem label="所属标段" prop="lotId">
              <ElSelect
                v-model="formModel.lotId"
                class="bid-award-form-page__field"
                clearable
                :disabled="!formModel.projectId"
                filterable
                :placeholder="lotSelectPlaceholder"
                @change="handleLotChange"
              >
                <ElOption
                  v-for="item in allLotOptions"
                  :key="item.lotId"
                  :label="formatLotLabel(item)"
                  :value="item.lotId"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElDescriptions
          v-if="selectedLot"
          v-loading="optionLoading"
          border
          class="bid-award-form-page__context"
          :column="2"
          title="定标前置上下文"
        >
          <ElDescriptionsItem label="项目状态">
            <ElTag
              effect="plain"
              :type="getStatusTagType(selectedProject?.status)"
            >
              {{ getProjectStatusText(selectedProject?.status) }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="标段状态">
            <ElTag effect="plain" :type="getStatusTagType(selectedLot.status)">
              {{ getLotStatusText(selectedLot.status) }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="评标状态">
            <ElTag
              v-if="selectedEvaluation"
              effect="plain"
              :type="getStatusTagType(selectedEvaluation.status)"
            >
              {{ getEvaluationStatusText(selectedEvaluation.status) }}
            </ElTag>
            <span v-else>待选择</span>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="定稿时间">
            {{ selectedEvaluation?.finalizeTime || '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="候选投标">
            {{ candidateSubmissionOptions.length }} 条
          </ElDescriptionsItem>
          <ElDescriptionsItem label="预算金额">
            {{ formatAmount(selectedLot.budgetAmount) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="中标候选">
            {{ selectedSubmission?.supplierNameSnapshot || '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="候选状态">
            <ElTag
              v-if="selectedSubmission"
              effect="plain"
              :type="getStatusTagType(selectedSubmission.status)"
            >
              {{ getSubmissionStatusText(selectedSubmission.status) }}
            </ElTag>
            <span v-else>待选择</span>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="投标报价">
            {{ formatAmount(selectedSubmission?.priceAmount) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="统一信用代码">
            {{ selectedSubmission?.supplierCreditCode || '--' }}
          </ElDescriptionsItem>
        </ElDescriptions>

        <ElRow :gutter="24">
          <ElCol :md="12" :xs="24">
            <ElFormItem label="评标记录" prop="evaluationId">
              <ElSelect
                v-model="formModel.evaluationId"
                class="bid-award-form-page__field"
                clearable
                :disabled="!formModel.lotId"
                filterable
                :loading="optionLoading"
                :placeholder="evaluationSelectPlaceholder"
              >
                <ElOption
                  v-for="item in finalizedEvaluationOptions"
                  :key="item.evaluationId"
                  :label="formatEvaluationLabel(item)"
                  :value="item.evaluationId"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :md="12" :xs="24">
            <ElFormItem label="中标候选" prop="winningSubmissionId">
              <ElSelect
                v-model="formModel.winningSubmissionId"
                class="bid-award-form-page__field"
                clearable
                :disabled="!formModel.lotId"
                filterable
                :loading="optionLoading"
                :placeholder="submissionSelectPlaceholder"
                @change="handleSubmissionChange"
              >
                <ElOption
                  v-for="item in candidateSubmissionOptions"
                  :key="item.submissionId"
                  :label="formatSubmissionLabel(item)"
                  :value="item.submissionId"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="24">
          <ElCol :md="12" :xs="24">
            <ElFormItem label="中标金额">
              <ElInputNumber
                v-model="formModel.awardAmount"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElFormItem label="定标备注">
          <ElInput
            v-model="formModel.remark"
            maxlength="500"
            placeholder="请输入定标备注"
            :rows="4"
            show-word-limit
            type="textarea"
          />
        </ElFormItem>
      </ElForm>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-award-form-page__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.bid-award-form-page__title {
  color: var(--el-text-color-primary);
  font-size: 18px;
  font-weight: 600;
}

.bid-award-form-page__actions {
  display: inline-flex;
  gap: 8px;
}

.bid-award-form-page__rule {
  margin-bottom: 18px;
}

.bid-award-form-page__context {
  margin-bottom: 18px;
}

.bid-award-form-page__field {
  width: 100%;
}

@media (max-width: 768px) {
  .bid-award-form-page__toolbar {
    align-items: stretch;
    flex-direction: column;
    gap: 12px;
  }

  .bid-award-form-page__actions {
    justify-content: flex-end;
  }
}
</style>
