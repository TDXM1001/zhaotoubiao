<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type { SystemPositionApi } from '#/api';

import { computed, nextTick, reactive, ref } from 'vue';

import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
} from 'element-plus';

import { addPositionApi, updatePositionApi } from '#/api';

defineOptions({
  name: 'SystemPositionFormModal',
});

const emit = defineEmits<{
  success: [];
}>();

const formRef = ref<FormInstance>();
const loading = ref(false);
const visible = ref(false);
const currentPositionId = ref<null | number>(null);

const formModel = reactive<SystemPositionApi.PositionUpdateParams>({
  positionId: 0,
  positionLevel: '',
  positionName: '',
  remark: '',
  sort: 1,
});

const rules: FormRules<typeof formModel> = {
  positionName: [
    { message: '请输入职务名称', required: true, trigger: 'blur' },
    { max: 200, message: '职务名称长度不能超过 200 个字符', trigger: 'blur' },
  ],
  sort: [{ message: '请输入排序', required: true, trigger: 'change' }],
  positionLevel: [
    { max: 200, message: '职级长度不能超过 200 个字符', trigger: 'blur' },
  ],
  remark: [
    { max: 200, message: '备注长度不能超过 200 个字符', trigger: 'blur' },
  ],
};

const dialogTitle = computed(() => {
  return currentPositionId.value ? '编辑职务' : '新增职务';
});

function resetFormModel() {
  currentPositionId.value = null;
  formModel.positionId = 0;
  formModel.positionLevel = '';
  formModel.positionName = '';
  formModel.remark = '';
  formModel.sort = 1;
}

async function resetFormState() {
  resetFormModel();
  await nextTick();
  formRef.value?.clearValidate();
}

function close() {
  visible.value = false;
}

async function openCreate() {
  visible.value = true;
  await resetFormState();
}

async function openEdit(row: SystemPositionApi.PositionItem) {
  visible.value = true;
  currentPositionId.value = row.positionId;
  await nextTick();
  formRef.value?.clearValidate();

  formModel.positionId = row.positionId;
  // 当前后端尚未把职级定义成字典字段，这里先保持文本回填。
  formModel.positionLevel = row.positionLevel ?? '';
  formModel.positionName = row.positionName;
  formModel.remark = row.remark ?? '';
  formModel.sort = row.sort;
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  try {
    loading.value = true;

    const payload: SystemPositionApi.PositionAddParams = {
      positionLevel: formModel.positionLevel?.trim() || undefined,
      positionName: formModel.positionName.trim(),
      remark: formModel.remark?.trim() || undefined,
      sort: Number(formModel.sort),
    };

    if (currentPositionId.value) {
      await updatePositionApi({
        ...payload,
        positionId: currentPositionId.value,
      });
      ElMessage.success('职务编辑成功');
    } else {
      await addPositionApi(payload);
      ElMessage.success('职务新增成功');
    }

    visible.value = false;
    emit('success');
  } finally {
    loading.value = false;
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
    :close-on-click-modal="false"
    :title="dialogTitle"
    width="640px"
  >
    <ElForm ref="formRef" :model="formModel" :rules="rules" label-width="96px">
      <ElFormItem label="职务名称" prop="positionName">
        <ElInput
          v-model="formModel.positionName"
          maxlength="200"
          placeholder="请输入职务名称"
        />
      </ElFormItem>

      <ElFormItem label="职级" prop="positionLevel">
        <ElInput
          v-model="formModel.positionLevel"
          maxlength="200"
          placeholder="请输入职级"
        />
      </ElFormItem>

      <ElFormItem label="排序" prop="sort">
        <ElInputNumber
          v-model="formModel.sort"
          :min="0"
          :step="1"
          controls-position="right"
          placeholder="请输入排序"
          style="width: 100%"
        />
      </ElFormItem>

      <ElFormItem label="备注" prop="remark">
        <ElInput
          v-model="formModel.remark"
          :rows="4"
          maxlength="200"
          placeholder="请输入备注"
          show-word-limit
          type="textarea"
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
