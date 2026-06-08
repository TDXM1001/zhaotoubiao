<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type {
  SystemBidLotApi,
  SystemBidProjectApi,
  SystemBidTenderApi,
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
  ElMessage,
  ElOption,
  ElRow,
  ElSelect,
} from 'element-plus';

import {
  addBidTenderApi,
  getBidTenderDetailApi,
  queryBidLotByProjectIdApi,
  queryBidProjectListApi,
  updateBidTenderApi,
} from '#/api';

import {
  parseRouteNumber,
  TENDER_VERSION_TYPE_OPTIONS,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidTenderForm',
});

const route = useRoute();
const router = useRouter();

const formRef = ref<FormInstance>();
const saving = ref(false);
const loading = ref(false);
const version = ref(0);
const projectOptions = ref<SystemBidProjectApi.ProjectOption[]>([]);
const lotOptions = ref<SystemBidLotApi.LotItem[]>([]);

const tenderVersionId = computed(() =>
  parseRouteNumber(route.query.tenderVersionId),
);
const routeProjectId = computed(() => parseRouteNumber(route.query.projectId));
const routeLotId = computed(() => parseRouteNumber(route.query.lotId));
const isEditMode = computed(() => Boolean(tenderVersionId.value));
const pageTitle = computed(() =>
  isEditMode.value ? '编辑招标文件' : '新增招标文件',
);

const formModel = reactive<SystemBidTenderApi.TenderCreateParams>({
  lotId: undefined as unknown as number,
  projectId: undefined as unknown as number,
  remark: '',
  summary: '',
  versionType: 'TENDER_MAIN',
});

const rules: FormRules<typeof formModel> = {
  lotId: [{ message: '请选择所属标段', required: true, trigger: 'change' }],
  projectId: [
    { message: '请选择所属项目', required: true, trigger: 'change' },
  ],
  versionType: [
    { message: '请选择版本类型', required: true, trigger: 'change' },
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
  lotOptions.value = await queryBidLotByProjectIdApi(projectId);
}

async function handleProjectChange(projectId?: number) {
  formModel.lotId = undefined as unknown as number;
  await loadLotOptions(projectId);
}

async function loadDetail() {
  if (!tenderVersionId.value) {
    return;
  }

  loading.value = true;
  try {
    const detail = await getBidTenderDetailApi(tenderVersionId.value);
    version.value = detail.version;
    formModel.lotId = detail.lotId;
    formModel.projectId = detail.projectId;
    formModel.remark = detail.remark ?? '';
    formModel.summary = detail.summary ?? '';
    formModel.versionType = detail.versionType;
    await loadLotOptions(detail.projectId);
  } catch {
    ElMessage.warning('招标文件不存在或已被删除');
    router.push('/system/bid/tender/list');
  } finally {
    loading.value = false;
  }
}

async function initRouteDefaults() {
  if (isEditMode.value) {
    return;
  }
  if (routeProjectId.value) {
    formModel.projectId = routeProjectId.value;
    await loadLotOptions(routeProjectId.value);
  }
  if (routeLotId.value) {
    formModel.lotId = routeLotId.value;
  }
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  const payload: SystemBidTenderApi.TenderCreateParams = {
    lotId: formModel.lotId,
    projectId: formModel.projectId,
    remark: formModel.remark?.trim() || undefined,
    summary: formModel.summary?.trim() || undefined,
    versionType: formModel.versionType,
  };

  saving.value = true;
  try {
    if (isEditMode.value && tenderVersionId.value) {
      await updateBidTenderApi({
        ...payload,
        tenderVersionId: tenderVersionId.value,
        version: version.value,
      });
      ElMessage.success('招标文件编辑成功');
    } else {
      await addBidTenderApi(payload);
      ElMessage.success('招标文件新增成功');
    }

    router.push({
      path: '/system/bid/tender/list',
      query: {
        lotId: String(formModel.lotId),
        projectId: String(formModel.projectId),
      },
    });
  } finally {
    saving.value = false;
  }
}

function goBack() {
  router.push('/system/bid/tender/list');
}

onMounted(async () => {
  await loadProjectOptions();
  await initRouteDefaults();
  await loadDetail();
});
</script>

<template>
  <Page auto-content-height class="bid-tender-form-page">
    <div class="bid-tender-form-page__toolbar">
      <div class="bid-tender-form-page__title">{{ pageTitle }}</div>
      <div class="bid-tender-form-page__actions">
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
                :disabled="isEditMode"
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
                :disabled="isEditMode || !formModel.projectId"
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
            <ElFormItem label="版本类型" prop="versionType">
              <ElSelect
                v-model="formModel.versionType"
                placeholder="请选择版本类型"
              >
                <ElOption
                  v-for="item in TENDER_VERSION_TYPE_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="版本摘要">
              <ElInput
                v-model="formModel.summary"
                maxlength="500"
                placeholder="请输入版本摘要"
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
.bid-tender-form-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-tender-form-page__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.bid-tender-form-page__actions {
  display: inline-flex;
  gap: 8px;
}
</style>
