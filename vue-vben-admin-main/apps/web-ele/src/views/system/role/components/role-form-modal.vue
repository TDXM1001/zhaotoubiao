<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type { SystemRoleApi } from '#/api';

import { computed, nextTick, reactive, ref } from 'vue';

import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
} from 'element-plus';

import { addRoleApi, getRoleDetailApi, updateRoleApi } from '#/api';

defineOptions({
  name: 'SystemRoleFormModal',
});

const emit = defineEmits<{
  success: [];
}>();

const formRef = ref<FormInstance>();
const loading = ref(false);
const visible = ref(false);
const currentRoleId = ref<null | number>(null);

const formModel = reactive<SystemRoleApi.RoleFormModel>({
  remark: '',
  roleCode: '',
  roleName: '',
});

const rules: FormRules<typeof formModel> = {
  roleCode: [
    { message: '请输入角色编码', required: true, trigger: 'blur' },
    { max: 20, message: '角色编码长度不能超过 20 个字符', trigger: 'blur' },
  ],
  roleName: [
    { message: '请输入角色名称', required: true, trigger: 'blur' },
    { max: 20, message: '角色名称长度不能超过 20 个字符', trigger: 'blur' },
  ],
  remark: [
    { max: 255, message: '备注长度不能超过 255 个字符', trigger: 'blur' },
  ],
};

const dialogTitle = computed(() => {
  return currentRoleId.value ? '编辑角色' : '新增角色';
});

function resetFormModel() {
  currentRoleId.value = null;
  formModel.remark = '';
  formModel.roleCode = '';
  formModel.roleName = '';
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

async function openEdit(row: Pick<SystemRoleApi.RoleItem, 'roleId'>) {
  visible.value = true;
  currentRoleId.value = row.roleId;
  await nextTick();
  formRef.value?.clearValidate();

  const detail = await getRoleDetailApi(row.roleId);
  formModel.remark = detail.remark ?? '';
  formModel.roleCode = detail.roleCode ?? '';
  formModel.roleName = detail.roleName ?? '';
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  try {
    loading.value = true;

    const payload = {
      remark: formModel.remark?.trim() || undefined,
      roleCode: formModel.roleCode.trim(),
      roleName: formModel.roleName.trim(),
    };

    if (currentRoleId.value) {
      await updateRoleApi({
        ...payload,
        roleId: currentRoleId.value,
      });
      ElMessage.success('角色编辑成功');
    } else {
      await addRoleApi(payload);
      ElMessage.success('角色新增成功');
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
      <ElFormItem label="角色名称" prop="roleName">
        <ElInput
          v-model="formModel.roleName"
          maxlength="20"
          placeholder="请输入角色名称"
        />
      </ElFormItem>

      <ElFormItem label="角色编码" prop="roleCode">
        <ElInput
          v-model="formModel.roleCode"
          maxlength="20"
          placeholder="请输入角色编码"
        />
      </ElFormItem>

      <ElFormItem label="备注" prop="remark">
        <ElInput
          v-model="formModel.remark"
          :rows="4"
          maxlength="255"
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
