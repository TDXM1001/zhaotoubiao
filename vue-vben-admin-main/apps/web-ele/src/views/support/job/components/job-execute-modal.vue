<script lang="ts" setup>
import type { SupportJobApi } from '#/api';

import { reactive, ref } from 'vue';

import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
} from 'element-plus';

import { executeJobApi } from '#/api';

defineOptions({
  name: 'SupportJobExecuteModal',
});

const emit = defineEmits<{
  success: [];
}>();

const visible = ref(false);
const saving = ref(false);
const jobName = ref('');

const formModel = reactive<SupportJobApi.ExecuteParams>({
  jobId: 0,
  param: '',
});

function open(row: SupportJobApi.JobItem) {
  jobName.value = row.jobName ?? `任务 ${row.jobId}`;
  formModel.jobId = row.jobId;
  formModel.param = row.param ?? '';
  visible.value = true;
}

async function handleSubmit() {
  saving.value = true;
  try {
    await executeJobApi({
      jobId: formModel.jobId,
      param: formModel.param?.trim() || undefined,
    });
    ElMessage.success('任务执行已提交');
    visible.value = false;
    emit('success');
  } finally {
    saving.value = false;
  }
}

defineExpose({
  open,
});
</script>

<template>
  <ElDialog
    v-model="visible"
    append-to-body
    :title="`立即执行 - ${jobName}`"
    width="560px"
  >
    <ElForm :model="formModel" label-width="88px">
      <ElFormItem label="任务参数">
        <ElInput
          v-model="formModel.param"
          :rows="6"
          maxlength="2000"
          placeholder="请输入本次执行参数，可留空"
          show-word-limit
          type="textarea"
        />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton :loading="saving" type="primary" @click="handleSubmit">
        执行
      </ElButton>
    </template>
  </ElDialog>
</template>
