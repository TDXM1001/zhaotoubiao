<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type { SystemMenuApi } from '#/api';
import type { VNode } from 'vue';

import { computed, nextTick, reactive, ref, watch } from 'vue';

import { IconPicker } from '@vben/common-ui';
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
  ElSwitch,
  ElTreeSelect,
} from 'element-plus';

import { addMenuApi, getMenuDetailApi, updateMenuApi } from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';

defineOptions({
  name: 'SystemMenuFormModal',
});

interface MenuTreeOption {
  children?: MenuTreeOption[];
  disabled?: boolean;
  label: string;
  value: number;
}

const ROOT_PARENT_ID = 0;

const props = defineProps<{
  authUrlOptions: SystemMenuApi.AuthUrlItem[];
  menuTree: SystemMenuApi.MenuTreeItem[];
}>();

const emit = defineEmits<{
  success: [];
}>();

const formRef = ref<FormInstance>();
const visible = ref(false);
const loading = ref(false);
const currentMenuId = ref<null | number>(null);
const iconInputComponent = ElInput as unknown as VNode;
const dialogTitle = ref('新增菜单');

const formModel = reactive<SystemMenuApi.MenuFormModel>({
  apiPerms: '',
  apiPermsList: [],
  cacheFlag: false,
  component: '',
  contextMenuId: undefined,
  disabledFlag: false,
  frameFlag: false,
  frameUrl: '',
  icon: '',
  menuName: '',
  menuType: 2,
  parentId: ROOT_PARENT_ID,
  path: '',
  permsType: 1,
  sort: 1,
  visibleFlag: true,
  webPerms: '',
});
const { options: menuTypeOptions } = useDictOptions('MENU_TYPE');
const { options: permsTypeOptions } = useDictOptions('MENU_PERMS_TYPE');

const rules: FormRules<typeof formModel> = {
  cacheFlag: [{ message: '请选择是否缓存', required: true, trigger: 'change' }],
  disabledFlag: [
    { message: '请选择禁用状态', required: true, trigger: 'change' },
  ],
  frameFlag: [{ message: '请选择是否外链', required: true, trigger: 'change' }],
  menuName: [
    { message: '请输入菜单名称', required: true, trigger: 'blur' },
    { max: 30, message: '菜单名称最多 30 个字符', trigger: 'blur' },
  ],
  parentId: [{ message: '请选择上级菜单', required: true, trigger: 'change' }],
  visibleFlag: [
    { message: '请选择显示状态', required: true, trigger: 'change' },
  ],
};

const isPointType = computed(() => formModel.menuType === 3);
const isMenuType = computed(() => formModel.menuType === 2);
const shouldShowFrameUrl = computed(() => !isPointType.value && formModel.frameFlag);

function mapMenuTreeOptions(
  menuTree: SystemMenuApi.MenuTreeItem[],
): MenuTreeOption[] {
  return menuTree.map((item) => ({
    children: item.children?.length
      ? mapMenuTreeOptions(item.children)
      : undefined,
    disabled: item.disabledFlag,
    label: item.menuName,
    value: item.menuId,
  }));
}

function filterMenuTreeOptions(
  menuTree: SystemMenuApi.MenuTreeItem[],
  excludedMenuId?: null | number,
): SystemMenuApi.MenuTreeItem[] {
  return menuTree
    .filter((item) => item.menuId !== excludedMenuId)
    .map((item) => ({
      ...item,
      children: item.children?.length
        ? filterMenuTreeOptions(item.children, excludedMenuId)
        : undefined,
    }));
}

const parentMenuOptions = computed(() => {
  const filteredTree = filterMenuTreeOptions(props.menuTree, currentMenuId.value);

  return [
    {
      label: '作为根菜单',
      value: ROOT_PARENT_ID,
    },
    ...mapMenuTreeOptions(filteredTree),
  ];
});

const contextMenuOptions = computed(() => {
  return mapMenuTreeOptions(props.menuTree);
});

const normalizedAuthUrlOptions = computed(() => {
  return props.authUrlOptions.map((item) => ({
    label: item.comment ? `${item.comment}（${item.url}）` : item.url,
    value: item.url,
  }));
});

watch(
  () => formModel.frameFlag,
  (value) => {
    if (!value) {
      formModel.frameUrl = '';
    }
  },
);

watch(
  () => formModel.menuType,
  (value) => {
    if (value !== 3) {
      formModel.apiPerms = '';
      formModel.apiPermsList = [];
      formModel.contextMenuId = undefined;
      formModel.webPerms = '';
      formModel.permsType = 1;
      return;
    }

    formModel.path = '';
    formModel.component = '';
    formModel.frameFlag = false;
    formModel.frameUrl = '';
    formModel.icon = '';
    formModel.cacheFlag = false;
    formModel.visibleFlag = true;
  },
);

watch(
  () => formModel.apiPermsList,
  (value) => {
    formModel.apiPerms = value.join(',');
  },
  {
    deep: true,
  },
);

function resetFormModel() {
  currentMenuId.value = null;
  formModel.apiPerms = '';
  formModel.apiPermsList = [];
  formModel.cacheFlag = false;
  formModel.component = '';
  formModel.contextMenuId = undefined;
  formModel.disabledFlag = false;
  formModel.frameFlag = false;
  formModel.frameUrl = '';
  formModel.icon = '';
  formModel.menuName = '';
  formModel.menuType = 2;
  formModel.parentId = ROOT_PARENT_ID;
  formModel.path = '';
  formModel.permsType = 1;
  formModel.sort = 1;
  formModel.visibleFlag = true;
  formModel.webPerms = '';
}

async function resetFormState() {
  resetFormModel();
  await nextTick();
  formRef.value?.clearValidate();
}

function close() {
  visible.value = false;
}

function validateBusinessFields() {
  if (!isPointType.value && !formModel.path.trim()) {
    ElMessage.warning('请输入路由地址');
    return false;
  }

  if (isMenuType.value && !formModel.component.trim()) {
    ElMessage.warning('请输入组件路径');
    return false;
  }

  if (shouldShowFrameUrl.value && !formModel.frameUrl.trim()) {
    ElMessage.warning('请输入外链地址');
    return false;
  }

  if (isPointType.value && !formModel.webPerms.trim()) {
    ElMessage.warning('请输入前端权限字符串');
    return false;
  }

  if (isPointType.value && !formModel.apiPerms.trim()) {
    ElMessage.warning('请输入后端权限值');
    return false;
  }

  return true;
}

function buildPayload(): SystemMenuApi.MenuAddParams {
  const payload: SystemMenuApi.MenuAddParams = {
    cacheFlag: formModel.cacheFlag,
    disabledFlag: formModel.disabledFlag,
    frameFlag: formModel.frameFlag,
    menuName: formModel.menuName.trim(),
    menuType: formModel.menuType,
    parentId: formModel.parentId ?? ROOT_PARENT_ID,
    permsType: formModel.permsType,
    sort: formModel.sort,
    visibleFlag: formModel.visibleFlag,
  };

  if (formModel.path.trim()) {
    payload.path = formModel.path.trim();
  }

  if (formModel.component.trim()) {
    payload.component = formModel.component.trim();
  }

  if (formModel.icon.trim()) {
    payload.icon = formModel.icon.trim();
  }

  if (formModel.frameUrl.trim()) {
    payload.frameUrl = formModel.frameUrl.trim();
  }

  if (formModel.webPerms.trim()) {
    payload.webPerms = formModel.webPerms.trim();
  }

  if (formModel.apiPerms.trim()) {
    payload.apiPerms = formModel.apiPerms.trim();
  }

  if (formModel.contextMenuId) {
    payload.contextMenuId = formModel.contextMenuId;
  }

  return payload;
}

async function openCreate() {
  dialogTitle.value = '新增菜单';
  visible.value = true;
  await resetFormState();
}

async function openCreateChild(row: Pick<SystemMenuApi.MenuItem, 'menuId' | 'menuName'>) {
  dialogTitle.value = `新增下级菜单 - ${row.menuName}`;
  visible.value = true;
  await resetFormState();
  formModel.parentId = row.menuId;
}

async function openEdit(row: Pick<SystemMenuApi.MenuItem, 'menuId'>) {
  dialogTitle.value = '编辑菜单';
  visible.value = true;
  await resetFormState();
  currentMenuId.value = row.menuId;

  const detail = await getMenuDetailApi(row.menuId);
  formModel.apiPerms = detail.apiPerms ?? '';
  formModel.apiPermsList = detail.apiPerms
    ? detail.apiPerms
        .split(',')
        .map((item) => item.trim())
        .filter(Boolean)
    : [];
  formModel.cacheFlag = Boolean(detail.cacheFlag);
  formModel.component = detail.component ?? '';
  formModel.contextMenuId = detail.contextMenuId ?? undefined;
  formModel.disabledFlag = Boolean(detail.disabledFlag);
  formModel.frameFlag = Boolean(detail.frameFlag);
  formModel.frameUrl = detail.frameUrl ?? '';
  formModel.icon = detail.icon ?? '';
  formModel.menuName = detail.menuName ?? '';
  formModel.menuType = detail.menuType;
  formModel.parentId = detail.parentId ?? ROOT_PARENT_ID;
  formModel.path = detail.path ?? '';
  formModel.permsType = detail.permsType ?? 1;
  formModel.sort = detail.sort ?? 1;
  formModel.visibleFlag = Boolean(detail.visibleFlag);
  formModel.webPerms = detail.webPerms ?? '';
}

async function submit(continueAdding = false) {
  const valid = await formRef.value?.validate().catch(() => false);
 
  if (!valid) {
    return;
  }
 
  if (!validateBusinessFields()) {
    return;
  }
 
  try {
    loading.value = true;
 
    const payload = buildPayload();
 
    if (currentMenuId.value) {
      await updateMenuApi({
        ...payload,
        menuId: currentMenuId.value,
      });
      ElMessage.success('菜单编辑成功');
    } else {
      await addMenuApi(payload);
      ElMessage.success('菜单新增成功');
    }
 
    emit('success');
 
    if (continueAdding) {
      // 保持 parentId 不变，方便连续添加同级菜单
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
    size="800px"
  >
    <ElForm ref="formRef" :model="formModel" :rules="rules" label-width="110px">
      <div class="menu-form-modal__grid">
        <ElFormItem label="菜单名称" prop="menuName">
          <ElInput
            v-model="formModel.menuName"
            maxlength="30"
            placeholder="请输入菜单名称"
          />
        </ElFormItem>

        <ElFormItem label="菜单类型" prop="menuType">
          <ElSelect
            v-model="formModel.menuType"
            placeholder="请选择菜单类型"
            style="width: 100%"
          >
            <ElOption
              v-for="item in menuTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="Number(item.value)"
            />
          </ElSelect>
        </ElFormItem>

        <ElFormItem label="上级菜单" prop="parentId">
          <ElTreeSelect
            v-model="formModel.parentId"
            :data="parentMenuOptions"
            check-strictly
            clearable
            default-expand-all
            node-key="value"
            placeholder="请选择上级菜单"
            style="width: 100%"
          />
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

        <ElFormItem v-if="!isPointType" label="路由地址">
          <ElInput
            v-model="formModel.path"
            maxlength="100"
            placeholder="请输入路由地址"
          />
        </ElFormItem>

        <ElFormItem v-if="isMenuType" label="组件路径">
          <ElInput
            v-model="formModel.component"
            maxlength="150"
            placeholder="请输入组件路径"
          />
        </ElFormItem>

        <ElFormItem v-if="!isPointType" label="菜单图标">
          <IconPicker
            v-model="formModel.icon"
            :input-component="iconInputComponent"
            icon-slot="suffix"
            model-value-prop="model-value"
            placeholder="请选择菜单图标"
            prefix="ep"
          />
        </ElFormItem>

        <ElFormItem v-if="isPointType" label="前端权限">
          <ElInput
            v-model="formModel.webPerms"
            maxlength="100"
            placeholder="请输入前端权限字符串"
          />
        </ElFormItem>

        <ElFormItem v-if="isPointType" label="后端权限候选">
          <ElSelect
            v-model="formModel.apiPermsList"
            allow-create
            clearable
            collapse-tags
            collapse-tags-tooltip
            default-first-option
            filterable
            multiple
            placeholder="请选择或输入后端权限路径"
            style="width: 100%"
          >
            <ElOption
              v-for="item in normalizedAuthUrlOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>

        <ElFormItem v-if="isPointType" label="后端权限值">
          <ElInput
            v-model="formModel.apiPerms"
            maxlength="500"
            placeholder="多个权限用英文逗号分隔"
          />
        </ElFormItem>

        <ElFormItem v-if="isPointType" label="关联菜单">
          <ElTreeSelect
            v-model="formModel.contextMenuId"
            :data="contextMenuOptions"
            check-strictly
            clearable
            default-expand-all
            node-key="value"
            placeholder="请选择关联菜单"
            style="width: 100%"
          />
        </ElFormItem>

        <ElFormItem v-if="!isPointType" label="是否外链" prop="frameFlag">
          <ElSwitch
            v-model="formModel.frameFlag"
            active-text="是"
            inactive-text="否"
            inline-prompt
          />
        </ElFormItem>

        <ElFormItem v-if="isMenuType" label="是否缓存" prop="cacheFlag">
          <ElSwitch
            v-model="formModel.cacheFlag"
            active-text="缓存"
            inactive-text="不缓存"
            inline-prompt
          />
        </ElFormItem>

        <ElFormItem v-if="shouldShowFrameUrl" label="外链地址">
          <ElInput
            v-model="formModel.frameUrl"
            maxlength="255"
            placeholder="请输入外链地址"
          />
        </ElFormItem>

        <ElFormItem v-if="isPointType" label="权限类型">
          <ElSelect
            v-model="formModel.permsType"
            disabled
            style="width: 100%"
          >
            <ElOption
              v-for="item in permsTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="Number(item.value)"
            />
          </ElSelect>
        </ElFormItem>

        <ElFormItem label="显示状态" prop="visibleFlag">
          <ElSwitch
            v-model="formModel.visibleFlag"
            :active-value="true"
            :inactive-value="false"
            active-text="显示"
            inactive-text="隐藏"
            inline-prompt
          />
        </ElFormItem>

        <ElFormItem label="禁用状态" prop="disabledFlag">
          <ElSwitch
            v-model="formModel.disabledFlag"
            :active-value="false"
            :inactive-value="true"
            active-text="启用"
            inactive-text="禁用"
            inline-prompt
          />
        </ElFormItem>
      </div>
    </ElForm>

    <template #footer>
      <div class="flex justify-end gap-3">
        <ElButton @click="close">取消</ElButton>
        <ElButton :loading="loading" type="primary" @click="submit(false)">
          提交
        </ElButton>
        <ElButton
          v-if="!currentMenuId"
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

<style scoped>
.menu-form-modal__grid {
  display: grid;
  gap: 4px 16px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (max-width: 960px) {
  .menu-form-modal__grid {
    grid-template-columns: 1fr;
  }
}
</style>

<style>
/* 全局样式：强制图标选择器弹出层在 Element Plus 弹窗之上 */
.vben-popover-content.z-popup,
[data-reka-popper-content-wrapper] {
  z-index: 10000 !important;
}

/* 优化图标选择器在弹窗内的展示 */
.menu-form-modal__grid .icon-picker-popover {
  width: 100% !important;
}
</style>
