<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type { SupportDictApi } from '#/api';

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
} from 'element-plus';

import {
  addDictDataApi,
  updateDictDataApi,
} from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';

defineOptions({
  name: 'SupportDictDataFormModal',
});

const emit = defineEmits<{
  success: [];
}>();

const formRef = ref<FormInstance>();
const loading = ref(false);
const visible = ref(false);
const currentDictDataId = ref<null | number | string>(null);

const formModel = reactive<{
  dataLabel: string;
  dataStyle: SupportDictApi.DictDataStyleValue;
  dataValue: string;
  dictCode: string;
  dictId: number | string;
  remark: string;
  sortOrder: number;
}>({
  dataLabel: '',
  dataStyle: 'default',
  dataValue: '',
  dictCode: '',
  dictId: '',
  remark: '',
  sortOrder: 0,
});

const rules: FormRules<typeof formModel> = {
  dataLabel: [{ message: '请输入字典项显示名称', required: true, trigger: 'blur' }],
  dataValue: [{ message: '请输入字典项值', required: true, trigger: 'blur' }],
  sortOrder: [{ message: '请输入排序值', required: true, trigger: 'change' }],
};

const dialogTitle = ref('新增字典项');
const { options: dictDataStyleOptions } = useDictOptions('DICT_DATA_STYLE');

function resetForm() {
  currentDictDataId.value = null;
  formModel.dataLabel = '';
  formModel.dataStyle = 'default';
  formModel.dataValue = '';
  formModel.dictCode = '';
  formModel.dictId = '';
  formModel.remark = '';
  formModel.sortOrder = 0;
}

function close() {
  visible.value = false;
}

async function openCreate(payload: {
  dictCode: string;
  dictId: number | string;
}) {
  dialogTitle.value = '新增字典项';
  resetForm();
  formModel.dictCode = payload.dictCode;
  formModel.dictId = payload.dictId;
  visible.value = true;
}

async function openEdit(row: SupportDictApi.DictDataItem) {
  dialogTitle.value = '编辑字典项';
  currentDictDataId.value = row.dictDataId;
  formModel.dataLabel = row.dataLabel;
  formModel.dataStyle = row.dataStyle ?? 'default';
  formModel.dataValue = row.dataValue;
  formModel.dictCode = row.dictCode;
  formModel.dictId = row.dictId;
  formModel.remark = row.remark ?? '';
  formModel.sortOrder = row.sortOrder ?? 0;
  visible.value = true;
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);

  if (!valid) {
    return;
  }

  try {
    loading.value = true;

    if (currentDictDataId.value) {
      await updateDictDataApi({
        dataLabel: formModel.dataLabel.trim(),
        dataStyle: formModel.dataStyle,
        dataValue: formModel.dataValue.trim(),
        dictCode: formModel.dictCode,
        dictDataId: currentDictDataId.value,
        dictId: formModel.dictId,
        remark: formModel.remark.trim() || undefined,
        sortOrder: formModel.sortOrder,
      });
      ElMessage.success('字典项编辑成功');
    } else {
      await addDictDataApi({
        dataLabel: formModel.dataLabel.trim(),
        dataStyle: formModel.dataStyle,
        dataValue: formModel.dataValue.trim(),
        dictId: formModel.dictId,
        remark: formModel.remark.trim() || undefined,
        sortOrder: formModel.sortOrder,
      });
      ElMessage.success('字典项新增成功');
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
    <ElForm ref="formRef" :model="formModel" :rules="rules" label-width="112px">
      <ElFormItem label="显示名称" prop="dataLabel">
        <ElInput v-model="formModel.dataLabel" placeholder="请输入字典项显示名称" />
      </ElFormItem>
      <ElFormItem label="字典项值" prop="dataValue">
        <ElInput v-model="formModel.dataValue" placeholder="请输入字典项值" />
      </ElFormItem>
      <ElFormItem label="样式" prop="dataStyle">
        <ElSelect v-model="formModel.dataStyle" placeholder="请选择样式">
          <ElOption
            v-for="item in dictDataStyleOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="排序值" prop="sortOrder">
        <ElInputNumber
          v-model="formModel.sortOrder"
          :min="0"
          class="w-full"
          placeholder="请输入排序值"
        />
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
