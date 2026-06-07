<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type {
  SystemBidLotApi,
  SystemBidProjectApi,
  SystemBidRegistrationApi,
} from '#/api';

import { computed, onMounted, reactive, ref, watch } from 'vue';
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
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElOption,
  ElRow,
  ElSelect,
} from 'element-plus';

import {
  addBidRegistrationApi,
  queryBidLotByProjectIdApi,
  queryBidProjectListApi,
} from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';

import { parseRouteNumber } from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidRegistrationForm',
});

const route = useRoute();
const router = useRouter();

const formRef = ref<FormInstance>();
const loading = ref(false);
const saving = ref(false);
const projectOptions = ref<SystemBidProjectApi.ProjectOption[]>([]);
const lotOptions = ref<SystemBidLotApi.LotItem[]>([]);

const { options: registrationTypeOptions } = useDictOptions(
  'BID_REGISTRATION_TYPE',
);

const defaultProjectId = computed(() => parseRouteNumber(route.query.projectId));
const defaultLotId = computed(() => parseRouteNumber(route.query.lotId));

const formModel = reactive<SystemBidRegistrationApi.RegistrationAddParams>({
  contactEmail: '',
  contactName: '',
  contactPhone: '',
  lotId: defaultLotId.value ?? (undefined as unknown as number),
  projectId: defaultProjectId.value ?? (undefined as unknown as number),
  registrationType: 'MANUAL_ENTRY',
  remark: '',
  supplierCreditCode: '',
  supplierEnterpriseId: undefined,
  supplierNameSnapshot: '',
});

const rules: FormRules<typeof formModel> = {
  contactName: [
    { message: '请输入联系人', required: true, trigger: 'blur' },
  ],
  contactPhone: [
    { message: '请输入联系电话', required: true, trigger: 'blur' },
  ],
  lotId: [
    { message: '请选择所属标段', required: true, trigger: 'change' },
  ],
  projectId: [
    { message: '请选择所属项目', required: true, trigger: 'change' },
  ],
  registrationType: [
    { message: '请选择报名方式', required: true, trigger: 'change' },
  ],
  supplierCreditCode: [
    { message: '请输入统一社会信用代码', required: true, trigger: 'blur' },
  ],
  supplierNameSnapshot: [
    { message: '请输入供应商名称', required: true, trigger: 'blur' },
  ],
};

async function loadProjectOptions() {
  projectOptions.value = await queryBidProjectListApi();
}

async function loadLotOptions(projectId?: number) {
  if (!projectId) {
    lotOptions.value = [];
    return;
  }

  const lots = await queryBidLotByProjectIdApi(projectId);
  // 报名只允许选择投标中的标段，避免前端选择后再由后端拦截。
  lotOptions.value = lots.filter((item) => item.status === 'BIDDING');
}

function handleProjectChange() {
  formModel.lotId = undefined as unknown as number;
  void loadLotOptions(formModel.projectId);
}

function getListQuery() {
  return formModel.projectId
    ? {
        projectId: String(formModel.projectId),
      }
    : undefined;
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  const payload: SystemBidRegistrationApi.RegistrationAddParams = {
    contactEmail: formModel.contactEmail?.trim() || undefined,
    contactName: formModel.contactName?.trim() || undefined,
    contactPhone: formModel.contactPhone?.trim() || undefined,
    lotId: formModel.lotId,
    projectId: formModel.projectId,
    registrationType: formModel.registrationType,
    remark: formModel.remark?.trim() || undefined,
    supplierCreditCode: formModel.supplierCreditCode.trim(),
    supplierEnterpriseId: formModel.supplierEnterpriseId ?? undefined,
    supplierNameSnapshot: formModel.supplierNameSnapshot.trim(),
  };

  saving.value = true;
  try {
    await addBidRegistrationApi(payload);
    ElMessage.success('供应商报名新增成功');
    router.push({
      path: '/system/bid/registration/list',
      query: getListQuery(),
    });
  } finally {
    saving.value = false;
  }
}

function goBack() {
  router.push({
    path: '/system/bid/registration/list',
    query: getListQuery(),
  });
}

watch(
  () => formModel.projectId,
  (projectId, oldProjectId) => {
    if (projectId && projectId !== oldProjectId) {
      void loadLotOptions(projectId);
    }
  },
);

onMounted(async () => {
  loading.value = true;
  try {
    await loadProjectOptions();
    await loadLotOptions(formModel.projectId);
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <Page auto-content-height class="bid-registration-form-page">
    <div class="bid-registration-form-page__toolbar">
      <div class="bid-registration-form-page__title">新增供应商报名</div>
      <div class="bid-registration-form-page__actions">
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
        label-width="126px"
      >
        <ElRow :gutter="24">
          <ElCol :span="12">
            <ElFormItem label="所属项目" prop="projectId">
              <ElSelect
                v-model="formModel.projectId"
                clearable
                filterable
                placeholder="请选择所属项目"
                @change="handleProjectChange"
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
            <ElFormItem label="所属标段" prop="lotId">
              <ElSelect
                v-model="formModel.lotId"
                clearable
                filterable
                placeholder="请选择所属标段"
              >
                <ElOption
                  v-for="item in lotOptions"
                  :key="item.lotId"
                  :label="`${item.lotName}（${item.lotCode}）`"
                  :value="item.lotId"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="24">
          <ElCol :span="12">
            <ElFormItem label="供应商名称" prop="supplierNameSnapshot">
              <ElInput
                v-model="formModel.supplierNameSnapshot"
                maxlength="200"
                placeholder="请输入供应商名称"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="统一社会信用代码" prop="supplierCreditCode">
              <ElInput
                v-model="formModel.supplierCreditCode"
                maxlength="64"
                placeholder="请输入统一社会信用代码"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="24">
          <ElCol :span="12">
            <ElFormItem label="报名方式" prop="registrationType">
              <ElSelect
                v-model="formModel.registrationType"
                clearable
                placeholder="请选择报名方式"
              >
                <ElOption
                  v-for="item in registrationTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="联系人" prop="contactName">
              <ElInput
                v-model="formModel.contactName"
                maxlength="64"
                placeholder="请输入联系人"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="24">
          <ElCol :span="12">
            <ElFormItem label="联系电话" prop="contactPhone">
              <ElInput
                v-model="formModel.contactPhone"
                maxlength="32"
                placeholder="请输入联系电话"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="联系邮箱">
              <ElInput
                v-model="formModel.contactEmail"
                maxlength="100"
                placeholder="请输入联系邮箱"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElFormItem label="备注">
          <ElInput
            v-model="formModel.remark"
            maxlength="500"
            placeholder="请输入报名材料摘要、资质说明或其他备注"
            :rows="3"
            type="textarea"
          />
        </ElFormItem>
      </ElForm>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-registration-form-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-registration-form-page__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.bid-registration-form-page__actions {
  display: inline-flex;
  gap: 8px;
}
</style>
