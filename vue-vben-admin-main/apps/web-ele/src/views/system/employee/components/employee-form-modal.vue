<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type { SystemEmployeeApi, SystemPositionApi } from '#/api';

import { computed, nextTick, reactive, ref } from 'vue';

import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElSelect,
  ElSwitch,
  ElTreeSelect,
} from 'element-plus';

import { addEmployeeApi, updateEmployeeApi } from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';

defineOptions({
  name: 'SystemEmployeeFormModal',
});

interface DepartmentOption {
  children?: DepartmentOption[];
  label: string;
  value: number;
}

const PHONE_PATTERN =
  /^1(?:3\d|4[5-9]|5\d|6[2567]|7[0-8]|8\d|9[0-35-9])\d{8}$/;

const props = defineProps<{
  departmentOptions: DepartmentOption[];
  positionOptions: SystemPositionApi.PositionItem[];
  roleOptions: SystemEmployeeApi.RoleOption[];
}>();

const emit = defineEmits<{
  success: [];
}>();

const formRef = ref<FormInstance>();
const loading = ref(false);
const visible = ref(false);
const currentEmployeeId = ref<null | number>(null);

const formModel = reactive({
  actualName: '',
  departmentId: undefined as number | undefined,
  disabledFlag: false,
  email: '',
  gender: undefined as number | undefined,
  loginName: '',
  phone: '',
  positionId: undefined as number | undefined,
  remark: '',
  roleIdList: [] as number[],
});
const { options: genderOptions } = useDictOptions('GENDER');

const rules: FormRules<typeof formModel> = {
  actualName: [
    { message: '请输入员工姓名', required: true, trigger: 'blur' },
  ],
  departmentId: [
    { message: '请选择所属部门', required: true, trigger: 'change' },
  ],
  disabledFlag: [
    { message: '请选择员工状态', required: true, trigger: 'change' },
  ],
  email: [
    { message: '请输入邮箱', required: true, trigger: 'blur' },
    { message: '邮箱格式不正确', type: 'email', trigger: ['blur', 'change'] },
  ],
  loginName: [
    { message: '请输入登录账号', required: true, trigger: 'blur' },
  ],
  phone: [
    { message: '请输入手机号', required: true, trigger: 'blur' },
    {
      message: '手机号格式不正确',
      pattern: PHONE_PATTERN,
      trigger: ['blur', 'change'],
    },
  ],
};

const dialogTitle = computed(() => {
  return currentEmployeeId.value ? '编辑员工' : '新增员工';
});

function resetFormModel() {
  currentEmployeeId.value = null;
  formModel.actualName = '';
  formModel.departmentId = undefined;
  formModel.disabledFlag = false;
  formModel.email = '';
  formModel.gender = undefined;
  formModel.loginName = '';
  formModel.phone = '';
  formModel.positionId = undefined;
  formModel.remark = '';
  formModel.roleIdList = [];
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

async function openEdit(row: SystemEmployeeApi.EmployeeItem) {
  currentEmployeeId.value = row.employeeId;
  visible.value = true;
  await nextTick();
  formRef.value?.clearValidate();
  formModel.actualName = row.actualName;
  formModel.departmentId = row.departmentId;
  formModel.disabledFlag = row.disabledFlag;
  formModel.email = row.email;
  formModel.gender = row.gender;
  formModel.loginName = row.loginName;
  formModel.phone = row.phone;
  formModel.positionId = row.positionId ?? undefined;
  // 后端分页 VO 当前不返回 remark，这里保持可编辑但不伪造旧值。
  formModel.remark = '';
  formModel.roleIdList = [...(row.roleIdList ?? [])];
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);

  if (!valid) {
    return;
  }

  try {
    loading.value = true;

    const payload = {
      actualName: formModel.actualName.trim(),
      departmentId: formModel.departmentId!,
      disabledFlag: formModel.disabledFlag,
      email: formModel.email.trim(),
      gender: formModel.gender,
      loginName: formModel.loginName.trim(),
      phone: formModel.phone.trim(),
      positionId: formModel.positionId,
      remark: formModel.remark.trim() || undefined,
      roleIdList: formModel.roleIdList,
    };

    if (currentEmployeeId.value) {
      await updateEmployeeApi({
        ...payload,
        employeeId: currentEmployeeId.value,
      });
      ElMessage.success('员工编辑成功');
    } else {
      const initialPassword = await addEmployeeApi(payload);
      ElMessage.success('员工新增成功');
      await ElMessageBox.alert(
        `初始密码：${initialPassword}`,
        '新增成功',
        {
          confirmButtonText: '我知道了',
          type: 'success',
        },
      );
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
    <ElForm ref="formRef" :model="formModel" :rules="rules" label-width="100px">
      <div class="employee-form-modal__grid">
        <ElFormItem label="员工姓名" prop="actualName">
          <ElInput
            v-model="formModel.actualName"
            maxlength="30"
            placeholder="请输入员工姓名"
          />
        </ElFormItem>

        <ElFormItem label="登录账号" prop="loginName">
          <ElInput
            v-model="formModel.loginName"
            maxlength="30"
            placeholder="请输入登录账号"
          />
        </ElFormItem>

        <ElFormItem label="所属部门" prop="departmentId">
          <ElTreeSelect
            v-model="formModel.departmentId"
            :data="props.departmentOptions"
            check-strictly
            clearable
            default-expand-all
            node-key="value"
            placeholder="请选择所属部门"
            style="width: 100%"
          />
        </ElFormItem>

        <ElFormItem label="员工状态" prop="disabledFlag">
          <ElSwitch
            v-model="formModel.disabledFlag"
            :active-value="false"
            :inactive-value="true"
            active-text="启用"
            inactive-text="禁用"
            inline-prompt
          />
        </ElFormItem>

        <ElFormItem label="手机号" prop="phone">
          <ElInput
            v-model="formModel.phone"
            maxlength="11"
            placeholder="请输入手机号"
          />
        </ElFormItem>

        <ElFormItem label="邮箱" prop="email">
          <ElInput
            v-model="formModel.email"
            maxlength="50"
            placeholder="请输入邮箱"
          />
        </ElFormItem>

        <ElFormItem label="性别">
          <ElSelect
            v-model="formModel.gender"
            clearable
            placeholder="请选择性别"
            style="width: 100%"
          >
            <ElOption
              v-for="item in genderOptions"
              :key="item.value"
              :label="item.label"
              :value="Number(item.value)"
            />
          </ElSelect>
        </ElFormItem>

        <ElFormItem label="职务">
          <ElSelect
            v-model="formModel.positionId"
            clearable
            filterable
            placeholder="请选择职务"
            style="width: 100%"
          >
            <ElOption
              v-for="position in props.positionOptions"
              :key="position.positionId"
              :label="position.positionName"
              :value="position.positionId"
            />
          </ElSelect>
        </ElFormItem>
      </div>

      <ElFormItem label="角色">
        <ElSelect
          v-model="formModel.roleIdList"
          clearable
          collapse-tags
          collapse-tags-tooltip
          filterable
          multiple
          placeholder="请选择角色"
          style="width: 100%"
        >
          <ElOption
            v-for="role in props.roleOptions"
            :key="role.roleId"
            :label="role.roleName"
            :value="role.roleId"
          />
        </ElSelect>
      </ElFormItem>

      <ElFormItem label="备注">
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

<style scoped>
.employee-form-modal__grid {
  display: grid;
  gap: 4px 16px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (max-width: 900px) {
  .employee-form-modal__grid {
    grid-template-columns: 1fr;
  }
}
</style>
