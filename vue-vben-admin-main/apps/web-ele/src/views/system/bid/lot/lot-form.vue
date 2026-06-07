<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type { SystemBidLotApi, SystemBidProjectApi } from '#/api';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import {
  ArrowLeft,
  Check,
} from '@element-plus/icons-vue';
import {
  ElButton,
  ElCard,
  ElCol,
  ElDatePicker,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElOption,
  ElRow,
  ElSelect,
} from 'element-plus';

import {
  addBidLotApi,
  getBidLotDetailApi,
  queryBidProjectListApi,
  updateBidLotApi,
} from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';

import { parseRouteNumber } from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidLotForm',
});

const route = useRoute();
const router = useRouter();

const formRef = ref<FormInstance>();
const loading = ref(false);
const saving = ref(false);
const version = ref(0);
const projectOptions = ref<SystemBidProjectApi.ProjectOption[]>([]);

const { options: evaluationModeOptions } = useDictOptions(
  'BID_EVALUATION_MODE',
);
const { options: awardModeOptions } = useDictOptions('BID_AWARD_MODE');

const lotId = computed(() => parseRouteNumber(route.query.lotId));
const defaultProjectId = computed(() => parseRouteNumber(route.query.projectId));
const isEditMode = computed(() => route.path.includes('/edit'));
const pageTitle = computed(() => (isEditMode.value ? '编辑标段' : '新增标段'));

const lotListQuery = computed(() => {
  const currentProjectId = formModel.projectId || defaultProjectId.value;
  if (!currentProjectId) {
    return undefined;
  }

  const currentProject = projectOptions.value.find((item) => {
    return item.projectId === currentProjectId;
  });

  return {
    projectId: String(currentProjectId),
    projectName: currentProject?.projectName,
  };
});

const formModel = reactive<SystemBidLotApi.LotAddParams>({
  awardMode: '',
  bidEndTime: '',
  bidStartTime: '',
  budgetAmount: undefined,
  evaluationMode: '',
  lotCode: '',
  lotName: '',
  lotNo: undefined as unknown as number,
  lotScope: '',
  openingTime: '',
  projectId: defaultProjectId.value ?? (undefined as unknown as number),
  registrationEndTime: '',
  registrationStartTime: '',
  remark: '',
});

const rules: FormRules<typeof formModel> = {
  lotCode: [
    { message: '请输入标段编号', required: true, trigger: 'blur' },
  ],
  lotName: [
    { message: '请输入标段名称', required: true, trigger: 'blur' },
  ],
  lotNo: [
    { message: '请输入标段序号', required: true, trigger: 'change' },
  ],
  projectId: [
    { message: '请选择所属项目', required: true, trigger: 'change' },
  ],
};

async function loadProjectOptions() {
  projectOptions.value = await queryBidProjectListApi();
}

async function loadDetail() {
  if (!lotId.value || !isEditMode.value) {
    return;
  }

  loading.value = true;
  try {
    const detail = await getBidLotDetailApi(lotId.value);
    version.value = detail.version;
    formModel.awardMode = detail.awardMode || '';
    formModel.bidEndTime = detail.bidEndTime || '';
    formModel.bidStartTime = detail.bidStartTime || '';
    formModel.budgetAmount = detail.budgetAmount ?? undefined;
    formModel.evaluationMode = detail.evaluationMode || '';
    formModel.lotCode = detail.lotCode;
    formModel.lotName = detail.lotName;
    formModel.lotNo = detail.lotNo;
    formModel.lotScope = detail.lotScope || '';
    formModel.openingTime = detail.openingTime || '';
    formModel.projectId = detail.projectId;
    formModel.registrationEndTime = detail.registrationEndTime || '';
    formModel.registrationStartTime = detail.registrationStartTime || '';
    formModel.remark = detail.remark || '';
  } finally {
    loading.value = false;
  }
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  const payload: SystemBidLotApi.LotAddParams = {
    awardMode: formModel.awardMode || undefined,
    bidEndTime: formModel.bidEndTime || undefined,
    bidStartTime: formModel.bidStartTime || undefined,
    budgetAmount: formModel.budgetAmount ?? undefined,
    evaluationMode: formModel.evaluationMode || undefined,
    lotCode: formModel.lotCode.trim(),
    lotName: formModel.lotName.trim(),
    lotNo: formModel.lotNo,
    lotScope: formModel.lotScope?.trim() || undefined,
    openingTime: formModel.openingTime || undefined,
    projectId: formModel.projectId,
    registrationEndTime: formModel.registrationEndTime || undefined,
    registrationStartTime: formModel.registrationStartTime || undefined,
    remark: formModel.remark?.trim() || undefined,
  };

  saving.value = true;
  try {
    if (isEditMode.value && lotId.value) {
      await updateBidLotApi({
        ...payload,
        lotId: lotId.value,
        version: version.value,
      });
      ElMessage.success('标段编辑成功');
    } else {
      await addBidLotApi(payload);
      ElMessage.success('标段新增成功');
    }

    router.push({
      path: '/system/bid/lot/list',
      query: payload.projectId
        ? { projectId: String(payload.projectId) }
        : undefined,
    });
  } finally {
    saving.value = false;
  }
}

function goBack() {
  router.push({
    path: '/system/bid/lot/list',
    query: lotListQuery.value,
  });
}

onMounted(async () => {
  await loadProjectOptions();
  await loadDetail();
});
</script>

<template>
  <Page auto-content-height class="bid-lot-form-page">
    <div class="bid-lot-form-page__toolbar">
      <div class="bid-lot-form-page__title">{{ pageTitle }}</div>
      <div class="bid-lot-form-page__actions">
        <ElButton :icon="ArrowLeft" @click="goBack">返回</ElButton>
        <ElButton
          :icon="Check"
          :loading="saving"
          type="primary"
          @click="submit"
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
        label-width="108px"
      >
        <ElRow :gutter="24">
          <ElCol :span="12">
            <ElFormItem label="所属项目" prop="projectId">
              <ElSelect
                v-model="formModel.projectId"
                clearable
                filterable
                placeholder="请选择所属项目"
              >
                <ElOption
                  v-for="item in projectOptions"
                  :key="item.projectId"
                  :label="`${item.projectName}（${item.projectCode}）`"
                  :value="item.projectId"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="标段序号" prop="lotNo">
              <ElInputNumber
                v-model="formModel.lotNo"
                :min="1"
                style="width: 100%"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="24">
          <ElCol :span="12">
            <ElFormItem label="标段编号" prop="lotCode">
              <ElInput
                v-model="formModel.lotCode"
                maxlength="64"
                placeholder="请输入标段编号"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="标段名称" prop="lotName">
              <ElInput
                v-model="formModel.lotName"
                maxlength="200"
                placeholder="请输入标段名称"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="24">
          <ElCol :span="12">
            <ElFormItem label="评标方式">
              <ElSelect
                v-model="formModel.evaluationMode"
                clearable
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
          <ElCol :span="12">
            <ElFormItem label="定标方式">
              <ElSelect
                v-model="formModel.awardMode"
                clearable
                placeholder="请选择定标方式"
              >
                <ElOption
                  v-for="item in awardModeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="24">
          <ElCol :span="12">
            <ElFormItem label="预算金额">
              <ElInputNumber
                v-model="formModel.budgetAmount"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="报名开始时间">
              <ElDatePicker
                v-model="formModel.registrationStartTime"
                placeholder="请选择报名开始时间"
                style="width: 100%"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="24">
          <ElCol :span="12">
            <ElFormItem label="报名截止时间">
              <ElDatePicker
                v-model="formModel.registrationEndTime"
                placeholder="请选择报名截止时间"
                style="width: 100%"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="投标开始时间">
              <ElDatePicker
                v-model="formModel.bidStartTime"
                placeholder="请选择投标开始时间"
                style="width: 100%"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="24">
          <ElCol :span="12">
            <ElFormItem label="投标截止时间">
              <ElDatePicker
                v-model="formModel.bidEndTime"
                placeholder="请选择投标截止时间"
                style="width: 100%"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="开标时间">
              <ElDatePicker
                v-model="formModel.openingTime"
                placeholder="请选择开标时间"
                style="width: 100%"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElFormItem label="标段范围">
          <ElInput
            v-model="formModel.lotScope"
            maxlength="1000"
            placeholder="请输入标段范围说明"
            :rows="4"
            type="textarea"
          />
        </ElFormItem>

        <ElFormItem label="备注">
          <ElInput
            v-model="formModel.remark"
            maxlength="500"
            placeholder="请输入备注"
            :rows="3"
            type="textarea"
          />
        </ElFormItem>
      </ElForm>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-lot-form-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-lot-form-page__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.bid-lot-form-page__actions {
  display: inline-flex;
  gap: 8px;
}
</style>
