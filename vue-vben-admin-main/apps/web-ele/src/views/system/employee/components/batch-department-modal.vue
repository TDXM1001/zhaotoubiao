<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import { nextTick, reactive, ref } from 'vue';

import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElMessage,
  ElTreeSelect,
} from 'element-plus';

import { batchUpdateEmployeeDepartmentApi } from '#/api';

defineOptions({
  name: 'SystemEmployeeBatchDepartmentModal',
});

interface DepartmentOption {
  children?: DepartmentOption[];
  label: string;
  value: number;
}

const props = defineProps<{
  departmentOptions: DepartmentOption[];
}>();

const emit = defineEmits<{
  success: [];
}>();

const formRef = ref<FormInstance>();
const loading = ref(false);
const visible = ref(false);
const selectedEmployeeIds = ref<number[]>([]);

const formModel = reactive({
  departmentId: undefined as number | undefined,
});

const rules: FormRules<typeof formModel> = {
  departmentId: [
    { message: '请选择目标部门', required: true, trigger: 'change' },
  ],
};

async function resetFormState() {
  formModel.departmentId = undefined;
  await nextTick();
  formRef.value?.clearValidate();
}

async function open(employeeIdList: number[]) {
  selectedEmployeeIds.value = [...employeeIdList];
  visible.value = true;
  await resetFormState();
}

function close() {
  visible.value = false;
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);

  if (!valid) {
    return;
  }

  try {
    loading.value = true;
    await batchUpdateEmployeeDepartmentApi({
      departmentId: formModel.departmentId!,
      employeeIdList: selectedEmployeeIds.value,
    });
    ElMessage.success('批量调整部门成功');
    visible.value = false;
    emit('success');
  } finally {
    loading.value = false;
  }
}

defineExpose({
  open,
});
</script>

<template>
  <ElDialog
    v-model="visible"
    :close-on-click-modal="false"
    title="批量调整部门"
    width="560px"
  >
    <ElForm ref="formRef" :model="formModel" :rules="rules" label-width="100px">
      <ElFormItem label="选中人数">
        <span>{{ selectedEmployeeIds.length }} 人</span>
      </ElFormItem>
      <ElFormItem label="目标部门" prop="departmentId">
        <ElTreeSelect
          v-model="formModel.departmentId"
          :data="props.departmentOptions"
          check-strictly
          clearable
          default-expand-all
          node-key="value"
          placeholder="请选择目标部门"
          style="width: 100%"
        />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <div class="flex justify-end gap-3">
        <ElButton @click="close">取消</ElButton>
        <ElButton :loading="loading" type="primary" @click="submit">
          确定
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>
