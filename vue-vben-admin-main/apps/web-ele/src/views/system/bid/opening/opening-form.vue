<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type {
  SystemBidLotApi,
  SystemBidOpeningApi,
  SystemBidProjectApi,
  SystemEmployeeApi,
} from '#/api';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import {
  ArrowLeft,
  Check,
} from '@element-plus/icons-vue';
import {
  ElAlert,
  ElButton,
  ElCard,
  ElCol,
  ElDatePicker,
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
  addBidOpeningApi,
  getBidLotDetailApi,
  queryAllEmployeeApi,
  queryBidLotByProjectIdApi,
  queryBidProjectListApi,
} from '#/api';

import {
  getLotStatusText,
  getProjectStatusText,
  getStatusTagType,
  isOpeningReadyLot,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidOpeningForm',
});

const route = useRoute();
const router = useRouter();

const formRef = ref<FormInstance>();
const loading = ref(false);
const saving = ref(false);
const projectOptions = ref<SystemBidProjectApi.ProjectOption[]>([]);
const allLotOptions = ref<SystemBidLotApi.LotItem[]>([]);
const employeeOptions = ref<SystemEmployeeApi.EmployeeOption[]>([]);

const defaultProjectId = computed(() => parseRouteNumber(route.query.projectId));
const defaultLotId = computed(() => parseRouteNumber(route.query.lotId));

const formModel = reactive<SystemBidOpeningApi.OpeningCreateParams>({
  hostEmployeeId: undefined,
  lotId: defaultLotId.value ?? (undefined as unknown as number),
  openingPlace: '',
  openingTime: '',
  projectId: defaultProjectId.value ?? (undefined as unknown as number),
  recorderEmployeeId: undefined,
  summary: '',
});

const rules: FormRules<typeof formModel> = {
  lotId: [
    { message: '请选择所属标段', required: true, trigger: 'change' },
  ],
  openingPlace: [
    { message: '请输入开标地点', required: true, trigger: 'blur' },
  ],
  openingTime: [
    { message: '请选择开标时间', required: true, trigger: 'change' },
  ],
  projectId: [
    { message: '请选择所属项目', required: true, trigger: 'change' },
  ],
};

const selectedProject = computed(() => {
  return projectOptions.value.find((item) => item.projectId === formModel.projectId);
});

const readyLotOptions = computed(() => {
  return allLotOptions.value.filter(isOpeningReadyLot);
});

const selectedLot = computed(() => {
  return allLotOptions.value.find((item) => item.lotId === formModel.lotId);
});

const lotSelectPlaceholder = computed(() => {
  return formModel.projectId ? '请选择已截标标段' : '请先选择项目';
});

function formatProjectLabel(item: SystemBidProjectApi.ProjectOption) {
  return `${item.projectName}（${item.projectCode}）`;
}

function formatLotLabel(item: SystemBidLotApi.LotItem) {
  return `${item.lotName}（${item.lotCode}）`;
}

function formatEmployeeLabel(item: SystemEmployeeApi.EmployeeOption) {
  return item.departmentName
    ? `${item.actualName} / ${item.departmentName}`
    : item.actualName;
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

function applyLotDefaults(lot?: SystemBidLotApi.LotItem, force = false) {
  if (!lot) {
    return;
  }

  // 开标安排默认继承标段计划开标时间，用户仍可按现场安排调整。
  if (lot.openingTime && (force || !formModel.openingTime)) {
    formModel.openingTime = lot.openingTime;
  }
}

async function loadBaseOptions() {
  const [projects, employees] = await Promise.all([
    queryBidProjectListApi(),
    queryAllEmployeeApi(false),
  ]);
  projectOptions.value = projects;
  employeeOptions.value = employees;
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

  const currentLot = lots.find((item) => item.lotId === formModel.lotId);
  if (currentLot && isOpeningReadyLot(currentLot)) {
    applyLotDefaults(currentLot);
    return;
  }

  if (formModel.lotId) {
    formModel.lotId = undefined as unknown as number;
    formModel.openingTime = '';
  }
}

function handleProjectChange() {
  formModel.lotId = undefined as unknown as number;
  formModel.openingTime = '';
  allLotOptions.value = [];
  void loadLotOptions(formModel.projectId);
}

function handleLotChange(lotId?: number) {
  const lot = allLotOptions.value.find((item) => item.lotId === lotId);
  applyLotDefaults(lot, true);
}

function getListQuery() {
  return {
    ...(formModel.projectId ? { projectId: String(formModel.projectId) } : {}),
    ...(formModel.lotId ? { lotId: String(formModel.lotId) } : {}),
  };
}

function goBack() {
  router.push({
    path: '/system/bid/opening/list',
    query: getListQuery(),
  });
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  if (!selectedLot.value || !isOpeningReadyLot(selectedLot.value)) {
    ElMessage.warning('请选择已截标标段后再创建开标安排');
    return;
  }

  // 接口契约保持为 ID 提交，页面负责把业务对象选择结果转换为后端参数。
  const payload: SystemBidOpeningApi.OpeningCreateParams = {
    hostEmployeeId: formModel.hostEmployeeId ?? undefined,
    lotId: formModel.lotId,
    openingPlace: formModel.openingPlace?.trim() || undefined,
    openingTime: formModel.openingTime || undefined,
    projectId: formModel.projectId,
    recorderEmployeeId: formModel.recorderEmployeeId ?? undefined,
    summary: formModel.summary?.trim() || undefined,
  };

  saving.value = true;
  try {
    await addBidOpeningApi(payload);
    ElMessage.success('开标安排已创建');
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
  <Page auto-content-height class="bid-opening-form-page">
    <div class="bid-opening-form-page__toolbar">
      <div class="bid-opening-form-page__title">创建开标安排</div>
      <div class="bid-opening-form-page__actions">
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
          class="bid-opening-form-page__rule"
          :closable="false"
          show-icon
          title="仅已截标标段可创建开标安排"
          type="info"
        />

        <ElRow :gutter="24">
          <ElCol :md="12" :xs="24">
            <ElFormItem label="所属项目" prop="projectId">
              <ElSelect
                v-model="formModel.projectId"
                class="bid-opening-form-page__field"
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
                class="bid-opening-form-page__field"
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
          border
          class="bid-opening-form-page__context"
          :column="2"
          title="标段上下文"
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
          <ElDescriptionsItem label="投标截止">
            {{ selectedLot.bidEndTime || '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="计划开标">
            {{ selectedLot.openingTime || '--' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="预算金额">
            {{ formatAmount(selectedLot.budgetAmount) }}
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
            <ElFormItem label="开标时间" prop="openingTime">
              <ElDatePicker
                v-model="formModel.openingTime"
                class="bid-opening-form-page__field"
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择开标时间"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :md="12" :xs="24">
            <ElFormItem label="开标地点" prop="openingPlace">
              <ElInput
                v-model="formModel.openingPlace"
                maxlength="200"
                placeholder="请输入开标地点"
                show-word-limit
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="24">
          <ElCol :md="12" :xs="24">
            <ElFormItem label="主持人">
              <ElSelect
                v-model="formModel.hostEmployeeId"
                class="bid-opening-form-page__field"
                clearable
                filterable
                placeholder="请选择主持人"
              >
                <ElOption
                  v-for="item in employeeOptions"
                  :key="item.employeeId"
                  :label="formatEmployeeLabel(item)"
                  :value="item.employeeId"
                >
                  <div class="bid-opening-form-page__employee-option">
                    <span>{{ item.actualName }}</span>
                    <span>{{ item.departmentName || '未分配部门' }}</span>
                  </div>
                </ElOption>
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :md="12" :xs="24">
            <ElFormItem label="记录人">
              <ElSelect
                v-model="formModel.recorderEmployeeId"
                class="bid-opening-form-page__field"
                clearable
                filterable
                placeholder="请选择记录人"
              >
                <ElOption
                  v-for="item in employeeOptions"
                  :key="item.employeeId"
                  :label="formatEmployeeLabel(item)"
                  :value="item.employeeId"
                >
                  <div class="bid-opening-form-page__employee-option">
                    <span>{{ item.actualName }}</span>
                    <span>{{ item.departmentName || '未分配部门' }}</span>
                  </div>
                </ElOption>
              </ElSelect>
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElFormItem label="开标说明">
          <ElInput
            v-model="formModel.summary"
            maxlength="1000"
            placeholder="请输入开标说明或备注"
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
.bid-opening-form-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-opening-form-page__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.bid-opening-form-page__actions {
  display: inline-flex;
  gap: 8px;
}

.bid-opening-form-page__rule {
  margin-bottom: 18px;
}

.bid-opening-form-page__context {
  margin-bottom: 18px;
}

.bid-opening-form-page__field {
  width: 100%;
}

.bid-opening-form-page__employee-option {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

.bid-opening-form-page__employee-option span:last-child {
  overflow: hidden;
  color: var(--el-text-color-secondary);
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .bid-opening-form-page__toolbar {
    align-items: stretch;
    flex-direction: column;
    gap: 12px;
  }

  .bid-opening-form-page__actions {
    justify-content: flex-end;
  }
}
</style>
