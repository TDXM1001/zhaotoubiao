<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type { SupportChangeLogApi } from '#/api';

import { computed, nextTick, reactive, ref } from 'vue';

import {
  ElButton,
  ElDatePicker,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElOption,
  ElSelect,
} from 'element-plus';

import { addChangeLogApi, updateChangeLogApi } from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';

defineOptions({
  name: 'SupportChangeLogFormModal',
});

const emit = defineEmits<{
  success: [];
}>();

const formRef = ref<FormInstance>();
const loading = ref(false);
const visible = ref(false);
const currentChangeLogId = ref<null | number>(null);

const { options: typeOptions } = useDictOptions('CHANGE_LOG_TYPE');

const formModel = reactive<SupportChangeLogApi.UpdateParams>({
  changeLogId: 0,
  content: '',
  link: '',
  publicDate: '',
  publishAuthor: '',
  type: 1,
  updateVersion: '',
});

const rules: FormRules<typeof formModel> = {
  content: [
    { message: '请输入更新内容', required: true, trigger: 'blur' },
    { max: 6000, message: '更新内容长度不能超过 6000 个字符', trigger: 'blur' },
  ],
  link: [{ max: 255, message: '跳转链接长度不能超过 255 个字符', trigger: 'blur' }],
  publicDate: [{ message: '请选择发布日期', required: true, trigger: 'change' }],
  publishAuthor: [
    { message: '请输入发布人', required: true, trigger: 'blur' },
    { max: 100, message: '发布人长度不能超过 100 个字符', trigger: 'blur' },
  ],
  type: [{ message: '请选择更新类型', required: true, trigger: 'change' }],
  updateVersion: [
    { message: '请输入版本号', required: true, trigger: 'blur' },
    { max: 100, message: '版本号长度不能超过 100 个字符', trigger: 'blur' },
  ],
};

const dialogTitle = computed(() => {
  return currentChangeLogId.value ? '编辑更新日志' : '新增更新日志';
});

function resetFormModel() {
  currentChangeLogId.value = null;
  formModel.changeLogId = 0;
  formModel.content = '';
  formModel.link = '';
  formModel.publicDate = '';
  formModel.publishAuthor = '';
  formModel.type = Number(typeOptions.value[0]?.value ?? 1);
  formModel.updateVersion = '';
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

async function openEdit(row: SupportChangeLogApi.ChangeLogItem) {
  visible.value = true;
  currentChangeLogId.value = row.changeLogId;
  await nextTick();
  formRef.value?.clearValidate();

  formModel.changeLogId = row.changeLogId;
  formModel.content = row.content ?? '';
  formModel.link = row.link ?? '';
  formModel.publicDate = row.publicDate ?? '';
  formModel.publishAuthor = row.publishAuthor ?? '';
  formModel.type = Number(row.type ?? typeOptions.value[0]?.value ?? 1);
  formModel.updateVersion = row.updateVersion;
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  try {
    loading.value = true;

    const payload: SupportChangeLogApi.CreateParams = {
      content: formModel.content.trim(),
      link: formModel.link?.trim() || undefined,
      publicDate: formModel.publicDate,
      publishAuthor: formModel.publishAuthor.trim(),
      type: Number(formModel.type),
      updateVersion: formModel.updateVersion.trim(),
    };

    if (currentChangeLogId.value) {
      await updateChangeLogApi({
        ...payload,
        changeLogId: currentChangeLogId.value,
      });
      ElMessage.success('更新日志编辑成功');
    } else {
      await addChangeLogApi(payload);
      ElMessage.success('更新日志新增成功');
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
    width="760px"
  >
    <ElForm ref="formRef" :model="formModel" :rules="rules" label-width="96px">
      <ElFormItem label="版本号" prop="updateVersion">
        <ElInput
          v-model="formModel.updateVersion"
          maxlength="100"
          placeholder="请输入版本号"
        />
      </ElFormItem>

      <ElFormItem label="更新类型" prop="type">
        <ElSelect
          v-model="formModel.type"
          placeholder="请选择更新类型"
          style="width: 100%"
        >
          <ElOption
            v-for="item in typeOptions"
            :key="item.value"
            :label="item.label"
            :value="Number(item.value)"
          />
        </ElSelect>
      </ElFormItem>

      <ElFormItem label="发布人" prop="publishAuthor">
        <ElInput
          v-model="formModel.publishAuthor"
          maxlength="100"
          placeholder="请输入发布人"
        />
      </ElFormItem>

      <ElFormItem label="发布日期" prop="publicDate">
        <ElDatePicker
          v-model="formModel.publicDate"
          placeholder="请选择发布日期"
          style="width: 100%"
          type="date"
          value-format="YYYY-MM-DD"
        />
      </ElFormItem>

      <ElFormItem label="跳转链接" prop="link">
        <ElInput
          v-model="formModel.link"
          maxlength="255"
          placeholder="请输入跳转链接，可留空"
        />
      </ElFormItem>

      <ElFormItem label="更新内容" prop="content">
        <ElInput
          v-model="formModel.content"
          :rows="8"
          maxlength="6000"
          placeholder="请输入更新内容"
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
