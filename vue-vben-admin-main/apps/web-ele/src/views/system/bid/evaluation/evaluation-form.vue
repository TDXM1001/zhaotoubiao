<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type {
  SystemBidEvaluationApi,
  SystemBidLotApi,
  SystemBidOpeningApi,
  SystemBidProjectApi,
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
  ElMessage,
  ElOption,
  ElRow,
  ElSelect,
  ElTag,
} from 'element-plus';

import {
  addBidEvaluationApi,
  getBidLotDetailApi,
  getBidOpeningByLotIdApi,
  queryBidLotByProjectIdApi,
  queryBidProjectListApi,
} from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';

import {
  getLotStatusText,
  getOpeningStatusText,
  getProjectStatusText,
  getStatusTagType,
  isEvaluationReadyLot,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidEvaluationForm',
});

const route = useRoute();
const router = useRouter();

const formRef = ref<FormInstance>();
const loading = ref(false);
const openingLoading = ref(false);
const saving = ref(false);
const projectOptions = ref<SystemBidProjectApi.ProjectOption[]>([]);
const allLotOptions = ref<SystemBidLotApi.LotItem[]>([]);
const openingRecord = ref<SystemBidOpeningApi.OpeningItem>();

const { optionMap: evaluationModeMap, options: evaluationModeOptions } =
  useDictOptions('BID_EVALUATION_MODE');

const defaultProjectId = computed(() => parseRouteNumber(route.query.projectId));
const defaultLotId = computed(() => parseRouteNumber(route.query.lotId));
const defaultOpeningId = computed(() => parseRouteNumber(route.query.openingId));

const formModel = reactive<SystemBidEvaluationApi.EvaluationCreateParams>({
  evaluationMode: '',
  finalSummary: '',
  lotId: defaultLotId.value ?? (undefined as unknown as number),
  openingId: defaultOpeningId.value,
  projectId: defaultProjectId.value ?? (undefined as unknown as number),
});

const rules: FormRules<typeof formModel> = {
  lotId: [
    { message: '请选择已开标标段', required: true, trigger: 'change' },
  ],
  projectId: [
    { message: '请选择所属项目', required: true, trigger: 'change' },
  ],
};

const selectedProject = computed(() => {
  return projectOptions.value.find(
    (item) => item.projectId === formModel.projectId,
  );
});

const readyLotOptions = computed(() => {
  return allLotOptions.value.filter(isEvaluationReadyLot);
});

const selectedLot = computed(() => {
  return allLotOptions.value.find((item) => item.lotId === formModel.lotId);
});

const lotSelectPlaceholder = computed(() => {
  return formModel.projectId ? '请选择已开标标段' : '请先选择项目';
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

function getEvaluationModeLabel(value?: string) {
  if (!value) {
    return '--';
  }
  return evaluationModeMap.value.get(value)?.label ?? value;
}

function applyLotDefaults(lot?: SystemBidLotApi.LotItem, force = false) {
  if (!lot) {
    return;
  }

  // 评标方式默认继承标段配置，用户仍可按实际评标规则调整。
  if (lot.evaluationMode && (force || !formModel.evaluationMode)) {
    formModel.evaluationMode = lot.evaluationMode;
  }
}

async function loadBaseOptions() {
  projectOptions.value = await queryBidProjectListApi();
}

async function initProjectFromRouteLot() {
  if (!formModel.lotId || formModel.projectId) {
    return;
  }

  const detail = await getBidLotDetailApi(formModel.lotId);
  formModel.projectId = detail.projectId;
}

async function loadOpeningRecord(lotId?: number) {
  openingRecord.value = undefined;
  formModel.openingId = undefined;
  if (!lotId) {
    return;
  }

  openingLoading.value = true;
  try {
    const opening = await getBidOpeningByLotIdApi(lotId);
    if (opening.status === 'COMPLETED') {
      openingRecord.value = opening;
      formModel.openingId = opening.openingId;
    }
  } catch {
    ElMessage.warning('未找到已完成开标记录，请先完成开标');
  } finally {
    openingLoading.value = false;
  }
}

async function loadLotOptions(projectId?: number) {
  if (!projectId) {
    allLotOptions.value = [];
    openingRecord.value = undefined;
    return;
  }

  const lots = await queryBidLotByProjectIdApi(projectId);
  allLotOptions.value = lots;

  const currentLot = lots.find((item) => item.lotId === formModel.lotId);
  if (currentLot && isEvaluationReadyLot(currentLot)) {
    applyLotDefaults(currentLot);
    await loadOpeningRecord(currentLot.lotId);
    return;
  }

  if (formModel.lotId) {
    formModel.lotId = undefined as unknown as number;
    formModel.openingId = undefined;
    openingRecord.value = undefined;
  }
}

function handleProjectChange() {
  formModel.lotId = undefined as unknown as number;
  formModel.openingId = undefined;
  formModel.evaluationMode = '';
  openingRecord.value = undefined;
  allLotOptions.value = [];
  void loadLotOptions(formModel.projectId);
}

function handleLotChange(lotId?: number) {
  const lot = allLotOptions.value.find((item) => item.lotId === lotId);
  applyLotDefaults(lot, true);
  void loadOpeningRecord(lotId);
}

function getListQuery() {
  return {
    ...(formModel.projectId ? { projectId: String(formModel.projectId) } : {}),
    ...(formModel.lotId ? { lotId: String(formModel.lotId) } : {}),
  };
}

function goBack() {
  router.push({
    path: '/system/bid/evaluation/list',
    query: getListQuery(),
  });
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  if (!selectedLot.value || !isEvaluationReadyLot(selectedLot.value)) {
    ElMessage.warning('请选择已开标标段后再创建评标记录');
    return;
  }

  if (!openingRecord.value || openingRecord.value.status !== 'COMPLETED') {
    ElMessage.warning('当前标段没有已完成开标记录，暂不能创建评标记录');
    return;
  }

  // 接口契约保持为 ID 提交，页面负责把业务选择结果转换为后端参数。
  const payload: SystemBidEvaluationApi.EvaluationCreateParams = {
    evaluationMode: formModel.evaluationMode?.trim() || undefined,
    finalSummary: formModel.finalSummary?.trim() || undefined,
    lotId: formModel.lotId,
    openingId: openingRecord.value.openingId,
    projectId: formModel.projectId,
  };

  saving.value = true;
  try {
    await addBidEvaluationApi(payload);
    ElMessage.success('评标记录已创建');
    goBack();
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  loading.value = true;
  try {
    await loadBaseOptions();
    await initProjectFromRouteLot();
    await loadLotOptions(formModel.projectId);
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <Page auto-content-height class="bid-evaluation-form-page">
    <div class="bid-evaluation-form-page__toolbar">
      <div class="bid-evaluation-form-page__title">创建评标记录</div>
      <div class="bid-evaluation-form-page__actions">
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
          class="bid-evaluation-form-page__rule"
          :closable="false"
          show-icon
          title="仅已开标标段且已完成开标后可创建评标记录"
          type="info"
        />

        <ElRow :gutter="24">
          <ElCol :md="12" :xs="24">
            <ElFormItem label="所属项目" prop="projectId">
              <ElSelect
                v-model="formModel.projectId"
                class="bid-evaluation-form-page__field"
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
                class="bid-evaluation-form-page__field"
                clearable
                :disabled="!formModel.projectId"
                filterable
                :placeholder="lotSelectPlaceholder"
                @change="handleLotChange"
              >
                <ElOption
                  v-for="item in readyLotOptions"
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
          v-loading="openingLoading"
          border
          class="bid-evaluation-form-page__context"
          :column="2"
          title="评标前置上下文"
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
          <ElDescriptionsItem label="开标状态">
            <ElTag
              v-if="openingRecord"
              effect="plain"
              :type="getStatusTagType(openingRecord.status)"
            >
              {{ getOpeningStatusText(openingRecord.status) }}
            </ElTag>
            <span v-else>未完成</span>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="开标时间">
            {{ openingRecord?.openingTime || '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="开标地点">
            {{ openingRecord?.openingPlace || '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="预算金额">
            {{ formatAmount(selectedLot.budgetAmount) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="评标方式">
            {{ getEvaluationModeLabel(selectedLot.evaluationMode) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="标段编号">
            {{ selectedLot.lotCode }}
          </ElDescriptionsItem>
          <ElDescriptionsItem :span="2" label="采购范围">
            {{ selectedLot.lotScope || '--' }}
          </ElDescriptionsItem>
        </ElDescriptions>

        <ElRow :gutter="24">
          <ElCol :md="12" :xs="24">
            <ElFormItem label="评标方式">
              <ElSelect
                v-model="formModel.evaluationMode"
                class="bid-evaluation-form-page__field"
                clearable
                filterable
                placeholder="请选择评标方式"
              >
                <ElOption
                  v-for="item in evaluationModeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElFormItem label="评标摘要">
          <ElInput
            v-model="formModel.finalSummary"
            maxlength="1000"
            placeholder="请输入评标摘要或说明"
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
.bid-evaluation-form-page__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.bid-evaluation-form-page__title {
  color: var(--el-text-color-primary);
  font-size: 18px;
  font-weight: 600;
}

.bid-evaluation-form-page__actions {
  display: inline-flex;
  gap: 8px;
}

.bid-evaluation-form-page__rule {
  margin-bottom: 18px;
}

.bid-evaluation-form-page__context {
  margin-bottom: 18px;
}

.bid-evaluation-form-page__field {
  width: 100%;
}

@media (max-width: 768px) {
  .bid-evaluation-form-page__toolbar {
    align-items: stretch;
    flex-direction: column;
    gap: 12px;
  }

  .bid-evaluation-form-page__actions {
    justify-content: flex-end;
  }
}
</style>
