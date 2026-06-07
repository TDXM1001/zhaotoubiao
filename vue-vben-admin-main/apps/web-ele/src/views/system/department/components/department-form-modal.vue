<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type { SystemDepartmentApi, SystemEmployeeApi } from '#/api';

import { computed, nextTick, reactive, ref } from 'vue';

import {
  ElButton,
  ElDrawer,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElOption,
  ElSelect,
  ElTreeSelect,
} from 'element-plus';

import { addDepartmentApi, updateDepartmentApi } from '#/api';

defineOptions({
  name: 'SystemDepartmentFormModal',
});

interface DepartmentTreeOption {
  children?: DepartmentTreeOption[] | null;
  label: string;
  value: number;
}

const props = defineProps<{
  departmentTree: SystemDepartmentApi.DepartmentItem[];
  employeeOptions: SystemEmployeeApi.EmployeeOption[];
}>();

const emit = defineEmits<{
  success: [];
}>();

const ROOT_PARENT_ID = 0;

const formRef = ref<FormInstance>();
const loading = ref(false);
const visible = ref(false);
const currentDepartmentId = ref<null | number>(null);
const dialogTitle = ref('新增部门');

const formModel = reactive({
  departmentName: '',
  managerId: undefined as null | number | undefined,
  parentId: ROOT_PARENT_ID as number,
  sort: 1,
});

const rules: FormRules<typeof formModel> = {
  departmentName: [
    { message: '请输入部门名称', required: true, trigger: 'blur' },
  ],
  sort: [{ message: '请输入排序值', required: true, trigger: 'blur' }],
};

function mapDepartmentTreeOptions(
  departmentList: SystemDepartmentApi.DepartmentItem[],
): DepartmentTreeOption[] {
  return departmentList.map((item) => ({
    children: item.children?.length
      ? mapDepartmentTreeOptions(item.children)
      : undefined,
    label: item.departmentName,
    value: item.departmentId,
  }));
}

function filterDepartmentTreeOptions(
  departmentList: SystemDepartmentApi.DepartmentItem[],
  excludedDepartmentId?: null | number,
): SystemDepartmentApi.DepartmentItem[] {
  return departmentList
    .filter((item) => item.departmentId !== excludedDepartmentId)
    .map((item) => ({
      ...item,
      children: item.children?.length
        ? filterDepartmentTreeOptions(item.children, excludedDepartmentId)
        : undefined,
    }));
}

const parentDepartmentOptions = computed(() => {
  const filteredTree = filterDepartmentTreeOptions(
    props.departmentTree,
    currentDepartmentId.value,
  );

  return [
    {
      label: '作为根部门',
      value: ROOT_PARENT_ID,
    },
    ...mapDepartmentTreeOptions(filteredTree),
  ];
});

function resetForm() {
  currentDepartmentId.value = null;
  formModel.departmentName = '';
  formModel.managerId = undefined;
  formModel.parentId = ROOT_PARENT_ID;
  formModel.sort = 1;
}

async function resetFormState() {
  resetForm();
  await nextTick();
  formRef.value?.clearValidate();
}

function close() {
  visible.value = false;
}

async function openCreate() {
  dialogTitle.value = '新增部门';
  visible.value = true;
  await resetFormState();
}

async function openCreateChild(row: SystemDepartmentApi.DepartmentItem) {
  dialogTitle.value = `新增下级部门 - ${row.departmentName}`;
  visible.value = true;
  await resetFormState();
  formModel.parentId = row.departmentId;
}

async function openEdit(row: SystemDepartmentApi.DepartmentItem) {
  dialogTitle.value = '编辑部门';
  currentDepartmentId.value = row.departmentId;
  visible.value = true;
  await nextTick();
  formRef.value?.clearValidate();
  formModel.departmentName = row.departmentName;
  formModel.managerId = row.managerId ?? undefined;
  formModel.parentId =
    row.parentId === null || row.parentId === undefined
      ? ROOT_PARENT_ID
      : row.parentId;
  formModel.sort = row.sort;
}

function getSubmitParentId() {
  return formModel.parentId ?? ROOT_PARENT_ID;
}

async function submit(continueAdding = false) {
  const valid = await formRef.value?.validate().catch(() => false);
 
  if (!valid) {
    return;
  }
 
  try {
    loading.value = true;
 
    const payload = {
      departmentName: formModel.departmentName.trim(),
      managerId: formModel.managerId || undefined,
      parentId: getSubmitParentId(),
      sort: formModel.sort,
    };
 
    if (currentDepartmentId.value) {
      await updateDepartmentApi({
        ...payload,
        departmentId: currentDepartmentId.value,
      });
      ElMessage.success('部门编辑成功');
    } else {
      await addDepartmentApi(payload);
      ElMessage.success('部门新增成功');
    }
 
    emit('success');
 
    if (continueAdding) {
      const savedParentId = formModel.parentId;
      await resetFormState();
      formModel.parentId = savedParentId;
    } else {
      visible.value = false;
    }
  } finally {
    loading.value = false;
  }
}

defineExpose({
  openCreate,
  openCreateChild,
  openEdit,
});
</script>

<template>
  <ElDrawer
    v-model="visible"
    :close-on-click-modal="false"
    :title="dialogTitle"
    size="720px"
  >
    <ElForm ref="formRef" :model="formModel" :rules="rules" label-width="100px">
      <ElFormItem label="部门名称" prop="departmentName">
        <ElInput
          v-model="formModel.departmentName"
          maxlength="50"
          placeholder="请输入部门名称"
        />
      </ElFormItem>
 
      <ElFormItem label="上级部门" prop="parentId">
        <ElTreeSelect
          v-model="formModel.parentId"
          :data="parentDepartmentOptions"
          check-strictly
          clearable
          default-expand-all
          node-key="value"
          placeholder="请选择上级部门"
          style="width: 100%"
        />
      </ElFormItem>
 
      <ElFormItem label="负责人">
        <ElSelect
          v-model="formModel.managerId"
          clearable
          filterable
          placeholder="请选择负责人"
          style="width: 100%"
        >
          <ElOption
            v-for="employee in props.employeeOptions"
            :key="employee.employeeId"
            :label="employee.actualName"
            :value="employee.employeeId"
          />
        </ElSelect>
      </ElFormItem>
 
      <ElFormItem label="排序" prop="sort">
        <ElInputNumber
          v-model="formModel.sort"
          :min="0"
          controls-position="right"
          placeholder="请输入排序值"
          style="width: 100%"
        />
      </ElFormItem>
    </ElForm>
 
    <template #footer>
      <div class="flex justify-end gap-3">
        <ElButton @click="close">取消</ElButton>
        <ElButton :loading="loading" type="primary" @click="submit(false)">
          提交
        </ElButton>
        <ElButton
          v-if="!currentDepartmentId"
          :loading="loading"
          type="primary"
          @click="submit(true)"
        >
          提交并添加下一个
        </ElButton>
      </div>
    </template>
  </ElDrawer>
</template>
