<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type { SupportDictApi } from '#/api';

import { reactive, ref } from 'vue';

import { ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElMessage } from 'element-plus';

import { addDictApi, updateDictApi } from '#/api';

defineOptions({
  name: 'SupportDictFormModal',
});

const emit = defineEmits<{
  success: [];
}>();

const formRef = ref<FormInstance>();
const loading = ref(false);
const visible = ref(false);
const currentDictId = ref<null | number | string>(null);

const formModel = reactive({
  dictCode: '',
  dictName: '',
  remark: '',
});

const rules: FormRules<typeof formModel> = {
  dictCode: [{ message: '请输入字典编码', required: true, trigger: 'blur' }],
  dictName: [{ message: '请输入字典名称', required: true, trigger: 'blur' }],
};

const dialogTitle = ref('新增数据字典');

function resetForm() {
  currentDictId.value = null;
  formModel.dictCode = '';
  formModel.dictName = '';
  formModel.remark = '';
}

function close() {
  visible.value = false;
}

async function openCreate() {
  dialogTitle.value = '新增数据字典';
  resetForm();
  visible.value = true;
}

async function openEdit(row: SupportDictApi.DictItem) {
  dialogTitle.value = '编辑数据字典';
  currentDictId.value = row.dictId;
  formModel.dictCode = row.dictCode;
  formModel.dictName = row.dictName;
  formModel.remark = row.remark ?? '';
  visible.value = true;
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);

  if (!valid) {
    return;
  }

  try {
    loading.value = true;

    if (currentDictId.value) {
      await updateDictApi({
        dictCode: formModel.dictCode.trim(),
        dictId: currentDictId.value,
        dictName: formModel.dictName.trim(),
        remark: formModel.remark.trim() || undefined,
      });
      ElMessage.success('数据字典编辑成功');
    } else {
      await addDictApi({
        dictCode: formModel.dictCode.trim(),
        dictName: formModel.dictName.trim(),
        remark: formModel.remark.trim() || undefined,
      });
      ElMessage.success('数据字典新增成功');
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
    width="680px"
  >
    <ElForm ref="formRef" :model="formModel" :rules="rules" label-width="96px">
      <ElFormItem label="字典名称" prop="dictName">
        <ElInput v-model="formModel.dictName" placeholder="请输入字典名称" />
      </ElFormItem>
      <ElFormItem label="字典编码" prop="dictCode">
        <ElInput v-model="formModel.dictCode" placeholder="请输入字典编码" />
      </ElFormItem>
      <ElFormItem label="备注">
        <ElInput
          v-model="formModel.remark"
          :rows="4"
          placeholder="请输入备注"
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
