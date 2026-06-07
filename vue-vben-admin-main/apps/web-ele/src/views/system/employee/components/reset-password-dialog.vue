<script lang="ts" setup>
import { ref } from 'vue';

import { ElButton, ElDialog, ElMessageBox } from 'element-plus';

import { resetEmployeePasswordApi } from '#/api';

defineOptions({
  name: 'SystemEmployeeResetPasswordDialog',
});

const emit = defineEmits<{
  success: [];
}>();

const visible = ref(false);
const loading = ref(false);
const currentEmployeeId = ref<null | number>(null);
const currentEmployeeName = ref('');

function open(employeeId: number, actualName: string) {
  currentEmployeeId.value = employeeId;
  currentEmployeeName.value = actualName;
  visible.value = true;
}

function close() {
  visible.value = false;
}

async function submit() {
  if (!currentEmployeeId.value) {
    return;
  }

  try {
    loading.value = true;
    const newPassword = await resetEmployeePasswordApi(currentEmployeeId.value);
    visible.value = false;
    await ElMessageBox.alert(
      `员工“${currentEmployeeName.value}”的新密码为：${newPassword}`,
      '重置成功',
      {
        confirmButtonText: '我知道了',
        type: 'success',
      },
    );
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
    title="重置密码"
    width="480px"
  >
    <div class="reset-password-dialog__content">
      确定要重置员工“{{ currentEmployeeName }}”的登录密码吗？
    </div>

    <template #footer>
      <div class="flex justify-end gap-3">
        <ElButton @click="close">取消</ElButton>
        <ElButton :loading="loading" type="primary" @click="submit">
          确定重置
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<style scoped>
.reset-password-dialog__content {
  line-height: 1.8;
  color: var(--el-text-color-primary);
}
</style>
