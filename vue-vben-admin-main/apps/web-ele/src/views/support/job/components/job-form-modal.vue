<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type { SupportJobApi } from '#/api';

import { reactive, ref } from 'vue';

import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElOption,
  ElSelect,
  ElSwitch,
} from 'element-plus';

import { addJobApi, getJobDetailApi, updateJobApi } from '#/api';

defineOptions({
  name: 'SupportJobFormModal',
});

const emit = defineEmits<{
  success: [];
}>();

const visible = ref(false);
const loading = ref(false);
const saving = ref(false);
const formRef = ref<FormInstance>();
const currentJobId = ref<number>();

const formModel = reactive<SupportJobApi.CreateParams>({
  enabledFlag: true,
  jobClass: '',
  jobName: '',
  param: '',
  remark: '',
  sort: 1,
  triggerType: 'cron',
  triggerValue: '',
});

const triggerTypeOptions = [
  { label: 'cron 表达式', value: 'cron' },
  { label: '固定间隔', value: 'fixed_delay' },
];

const rules: FormRules<SupportJobApi.CreateParams> = {
  jobClass: [{ message: '请输入任务执行类', required: true, trigger: 'blur' }],
  jobName: [{ message: '请输入任务名称', required: true, trigger: 'blur' }],
  sort: [{ message: '请输入排序', required: true, trigger: 'change' }],
  triggerType: [{ message: '请选择触发类型', required: true, trigger: 'change' }],
  triggerValue: [{ message: '请输入触发配置', required: true, trigger: 'blur' }],
};

function resetForm() {
  currentJobId.value = undefined;
  Object.assign(formModel, {
    enabledFlag: true,
    jobClass: '',
    jobName: '',
    param: '',
    remark: '',
    sort: 1,
    triggerType: 'cron',
    triggerValue: '',
  });
  formRef.value?.clearValidate();
}

async function openCreate() {
  resetForm();
  visible.value = true;
}

async function openEdit(row: SupportJobApi.JobItem) {
  resetForm();
  visible.value = true;
  loading.value = true;
  try {
    const detail = await getJobDetailApi(row.jobId);
    currentJobId.value = detail.jobId;
    Object.assign(formModel, {
      enabledFlag: Boolean(detail.enabledFlag),
      jobClass: detail.jobClass ?? '',
      jobName: detail.jobName ?? '',
      param: detail.param ?? '',
      remark: detail.remark ?? '',
      sort: detail.sort ?? 1,
      triggerType: detail.triggerType ?? 'cron',
      triggerValue: detail.triggerValue ?? '',
    });
  } finally {
    loading.value = false;
  }
}

async function handleSubmit() {
  await formRef.value?.validate();

  saving.value = true;
  try {
    const payload = {
      enabledFlag: formModel.enabledFlag,
      jobClass: formModel.jobClass.trim(),
      jobName: formModel.jobName.trim(),
      param: formModel.param?.trim() || undefined,
      remark: formModel.remark?.trim() || undefined,
      sort: Number(formModel.sort),
      triggerType: formModel.triggerType,
      triggerValue: formModel.triggerValue.trim(),
    };

    if (currentJobId.value) {
      await updateJobApi({
        ...payload,
        jobId: currentJobId.value,
      });
      ElMessage.success('定时任务编辑成功');
    } else {
      await addJobApi(payload);
      ElMessage.success('定时任务新增成功');
    }

    visible.value = false;
    emit('success');
  } finally {
    saving.value = false;
  }
}

defineExpose({
  openCreate,
  openEdit,
});
</script>

<template>
  <ElDialog
    v-model="visible"
    append-to-body
    :title="currentJobId ? '编辑定时任务' : '新增定时任务'"
    width="720px"
  >
    <ElForm
      ref="formRef"
      v-loading="loading"
      :model="formModel"
      :rules="rules"
      label-width="108px"
    >
      <ElFormItem label="任务名称" prop="jobName">
        <ElInput
          v-model="formModel.jobName"
          maxlength="100"
          placeholder="请输入任务名称"
        />
      </ElFormItem>
      <ElFormItem label="执行类" prop="jobClass">
        <ElInput
          v-model="formModel.jobClass"
          maxlength="200"
          placeholder="请输入任务执行类完整类名"
        />
      </ElFormItem>
      <ElFormItem label="触发类型" prop="triggerType">
        <ElSelect v-model="formModel.triggerType" placeholder="请选择触发类型">
          <ElOption
            v-for="item in triggerTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="触发配置" prop="triggerValue">
        <ElInput
          v-model="formModel.triggerValue"
          maxlength="100"
          placeholder="cron 表达式或固定间隔配置"
        />
      </ElFormItem>
      <ElFormItem label="任务参数">
        <ElInput
          v-model="formModel.param"
          :rows="4"
          maxlength="1000"
          placeholder="请输入任务参数"
          show-word-limit
          type="textarea"
        />
      </ElFormItem>
      <ElFormItem label="启用状态">
        <ElSwitch
          v-model="formModel.enabledFlag"
          active-text="启用"
          inactive-text="禁用"
        />
      </ElFormItem>
      <ElFormItem label="排序" prop="sort">
        <ElInputNumber v-model="formModel.sort" :min="0" :step="1" />
      </ElFormItem>
      <ElFormItem label="备注">
        <ElInput
          v-model="formModel.remark"
          :rows="3"
          maxlength="250"
          placeholder="请输入备注"
          show-word-limit
          type="textarea"
        />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton :loading="saving" type="primary" @click="handleSubmit">
        保存
      </ElButton>
    </template>
  </ElDialog>
</template>
