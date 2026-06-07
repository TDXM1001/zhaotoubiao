<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type {
  SystemBidProjectApi,
  SystemDepartmentApi,
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
  ElButton,
  ElCard,
  ElCol,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElOption,
  ElRow,
  ElSelect,
  ElTreeSelect,
} from 'element-plus';

import {
  addBidProjectApi,
  getBidProjectDetailApi,
  listDepartmentOptionsApi,
  queryAllEmployeeApi,
  updateBidProjectApi,
} from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';

import {
  parseRouteNumber,
  transformDepartmentTree,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidProjectForm',
});

const route = useRoute();
const router = useRouter();

const formRef = ref<FormInstance>();
const saving = ref(false);
const loading = ref(false);
const version = ref(0);
const departmentOptions = ref<SystemDepartmentApi.DepartmentOption[]>([]);
const employeeOptions = ref<SystemEmployeeApi.EmployeeOption[]>([]);

const { options: projectTypeOptions } = useDictOptions('BID_PROJECT_TYPE');
const { options: procurementModeOptions } = useDictOptions(
  'BID_PROCUREMENT_MODE',
);

const projectId = computed(() => parseRouteNumber(route.query.projectId));
const isEditMode = computed(() => route.path.includes('/edit'));
const pageTitle = computed(() => (isEditMode.value ? '编辑招标项目' : '新增招标项目'));

const departmentTreeOptions = computed(() => {
  return transformDepartmentTree(departmentOptions.value);
});

const formModel = reactive<SystemBidProjectApi.ProjectAddParams>({
  agentOrgId: undefined,
  budgetAmount: undefined,
  managerEmployeeId: undefined,
  ownerOrgId: undefined as unknown as number,
  projectCode: '',
  projectName: '',
  projectType: '',
  procurementMode: '',
  remark: '',
});

const rules: FormRules<typeof formModel> = {
  ownerOrgId: [
    { message: '请选择归属组织', required: true, trigger: 'change' },
  ],
  procurementMode: [
    { message: '请选择采购方式', required: true, trigger: 'change' },
  ],
  projectCode: [
    { message: '请输入项目编号', required: true, trigger: 'blur' },
  ],
  projectName: [
    { message: '请输入项目名称', required: true, trigger: 'blur' },
  ],
  projectType: [
    { message: '请选择项目类型', required: true, trigger: 'change' },
  ],
};

async function loadBaseOptions() {
  const [departmentList, employeeList] = await Promise.all([
    listDepartmentOptionsApi(),
    queryAllEmployeeApi(false),
  ]);
  departmentOptions.value = departmentList;
  employeeOptions.value = employeeList;
}

async function loadDetail() {
  if (!projectId.value || !isEditMode.value) {
    return;
  }

  loading.value = true;
  try {
    const detail = await getBidProjectDetailApi(projectId.value);
    version.value = detail.version;
    formModel.agentOrgId = detail.agentOrgId ?? undefined;
    formModel.budgetAmount = detail.budgetAmount ?? undefined;
    formModel.managerEmployeeId = detail.managerEmployeeId ?? undefined;
    formModel.ownerOrgId = detail.ownerOrgId;
    formModel.procurementMode = detail.procurementMode;
    formModel.projectCode = detail.projectCode;
    formModel.projectName = detail.projectName;
    formModel.projectType = detail.projectType;
    formModel.remark = detail.remark ?? '';
  } finally {
    loading.value = false;
  }
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  const payload: SystemBidProjectApi.ProjectAddParams = {
    agentOrgId: formModel.agentOrgId || undefined,
    budgetAmount: formModel.budgetAmount ?? undefined,
    managerEmployeeId: formModel.managerEmployeeId || undefined,
    ownerOrgId: formModel.ownerOrgId,
    procurementMode: formModel.procurementMode,
    projectCode: formModel.projectCode.trim(),
    projectName: formModel.projectName.trim(),
    projectType: formModel.projectType,
    remark: formModel.remark?.trim() || undefined,
  };

  saving.value = true;
  try {
    if (isEditMode.value && projectId.value) {
      await updateBidProjectApi({
        ...payload,
        projectId: projectId.value,
        version: version.value,
      });
      ElMessage.success('招标项目编辑成功');
    } else {
      await addBidProjectApi(payload);
      ElMessage.success('招标项目新增成功');
    }

    router.push('/system/bid/project/list');
  } finally {
    saving.value = false;
  }
}

function goBack() {
  router.push('/system/bid/project/list');
}

onMounted(async () => {
  await loadBaseOptions();
  await loadDetail();
});
</script>

<template>
  <Page auto-content-height class="bid-project-form-page">
    <div class="bid-project-form-page__toolbar">
      <div class="bid-project-form-page__title">{{ pageTitle }}</div>
      <div class="bid-project-form-page__actions">
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
            <ElFormItem label="项目编号" prop="projectCode">
              <ElInput
                v-model="formModel.projectCode"
                maxlength="64"
                placeholder="请输入项目编号"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="项目名称" prop="projectName">
              <ElInput
                v-model="formModel.projectName"
                maxlength="200"
                placeholder="请输入项目名称"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="24">
          <ElCol :span="12">
            <ElFormItem label="项目类型" prop="projectType">
              <ElSelect
                v-model="formModel.projectType"
                clearable
                placeholder="请选择项目类型"
              >
                <ElOption
                  v-for="item in projectTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="采购方式" prop="procurementMode">
              <ElSelect
                v-model="formModel.procurementMode"
                clearable
                placeholder="请选择采购方式"
              >
                <ElOption
                  v-for="item in procurementModeOptions"
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
            <ElFormItem label="归属组织" prop="ownerOrgId">
              <ElTreeSelect
                v-model="formModel.ownerOrgId"
                check-strictly
                clearable
                default-expand-all
                node-key="value"
                :data="departmentTreeOptions"
                placeholder="请选择归属组织"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="代理组织">
              <ElTreeSelect
                v-model="formModel.agentOrgId"
                check-strictly
                clearable
                default-expand-all
                node-key="value"
                :data="departmentTreeOptions"
                placeholder="请选择代理组织"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="24">
          <ElCol :span="12">
            <ElFormItem label="项目负责人">
              <ElSelect
                v-model="formModel.managerEmployeeId"
                clearable
                filterable
                placeholder="请选择项目负责人"
              >
                <ElOption
                  v-for="item in employeeOptions"
                  :key="item.employeeId"
                  :label="item.actualName"
                  :value="item.employeeId"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="项目预算">
              <ElInputNumber
                v-model="formModel.budgetAmount"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElFormItem label="备注">
          <ElInput
            v-model="formModel.remark"
            maxlength="500"
            placeholder="请输入备注"
            :rows="4"
            type="textarea"
          />
        </ElFormItem>
      </ElForm>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-project-form-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-project-form-page__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.bid-project-form-page__actions {
  display: inline-flex;
  gap: 8px;
}
</style>
