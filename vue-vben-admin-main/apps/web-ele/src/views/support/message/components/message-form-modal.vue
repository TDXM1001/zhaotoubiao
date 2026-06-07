<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type { SupportMessageApi, SystemEmployeeApi } from '#/api';

import { computed, reactive, ref } from 'vue';

import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElOption,
  ElSelect,
} from 'element-plus';

import { queryAllEmployeeApi, sendMessagesApi } from '#/api';

defineOptions({
  name: 'SupportMessageFormModal',
});

interface MessageFormModel {
  content: string;
  dataId?: string;
  messageType: SupportMessageApi.MessageType;
  receiverUserIds: number[];
  title: string;
}

const emit = defineEmits<{
  success: [];
}>();

const visible = ref(false);
const loading = ref(false);
const saving = ref(false);
const formRef = ref<FormInstance>();
const employeeOptions = ref<SystemEmployeeApi.EmployeeOption[]>([]);

const formModel = reactive<MessageFormModel>({
  content: '',
  dataId: '',
  messageType: 1,
  receiverUserIds: [],
  title: '',
});

const messageTypeOptions = [
  { label: '站内信', value: 1 },
  { label: '订单', value: 2 },
] as const;

const rules: FormRules<MessageFormModel> = {
  content: [{ message: '请输入消息内容', required: true, trigger: 'blur' }],
  messageType: [{ message: '请选择消息类型', required: true, trigger: 'change' }],
  receiverUserIds: [
    { message: '请选择接收员工', required: true, trigger: 'change' },
  ],
  title: [{ message: '请输入消息标题', required: true, trigger: 'blur' }],
};

const employeeSelectOptions = computed(() => {
  return employeeOptions.value.map((item) => ({
    label: `${item.actualName}${item.departmentName ? ` / ${item.departmentName}` : ''}`,
    value: item.employeeId,
  }));
});

function resetForm() {
  Object.assign(formModel, {
    content: '',
    dataId: '',
    messageType: 1,
    receiverUserIds: [],
    title: '',
  });
  formRef.value?.clearValidate();
}

async function loadEmployeeOptions() {
  loading.value = true;
  try {
    employeeOptions.value = await queryAllEmployeeApi(false);
  } finally {
    loading.value = false;
  }
}

async function open() {
  resetForm();
  visible.value = true;
  await loadEmployeeOptions();
}

async function handleSubmit() {
  await formRef.value?.validate();

  if (formModel.receiverUserIds.length === 0) {
    ElMessage.warning('请选择接收员工');
    return;
  }

  saving.value = true;
  try {
    await sendMessagesApi(
      formModel.receiverUserIds.map((receiverUserId) => ({
        content: formModel.content.trim(),
        dataId: formModel.dataId?.trim() || undefined,
        messageType: formModel.messageType,
        receiverUserId,
        receiverUserType: 1,
        title: formModel.title.trim(),
      })),
    );
    ElMessage.success('消息发送成功');
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
    title="发送消息"
    width="680px"
  >
    <ElForm
      ref="formRef"
      v-loading="loading"
      :model="formModel"
      :rules="rules"
      label-width="92px"
    >
      <ElFormItem label="消息类型" prop="messageType">
        <ElSelect v-model="formModel.messageType" placeholder="请选择消息类型">
          <ElOption
            v-for="item in messageTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="接收员工" prop="receiverUserIds">
        <ElSelect
          v-model="formModel.receiverUserIds"
          filterable
          multiple
          placeholder="请选择接收员工"
        >
          <ElOption
            v-for="item in employeeSelectOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="消息标题" prop="title">
        <ElInput
          v-model="formModel.title"
          maxlength="200"
          placeholder="请输入消息标题"
        />
      </ElFormItem>
      <ElFormItem label="业务 ID">
        <ElInput
          v-model="formModel.dataId"
          maxlength="500"
          placeholder="可选，关联业务数据 ID"
        />
      </ElFormItem>
      <ElFormItem label="消息内容" prop="content">
        <ElInput
          v-model="formModel.content"
          :rows="6"
          maxlength="2000"
          placeholder="请输入消息内容"
          show-word-limit
          type="textarea"
        />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton :loading="saving" type="primary" @click="handleSubmit">
        发送
      </ElButton>
    </template>
  </ElDialog>
</template>
