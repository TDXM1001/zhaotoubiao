<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type { SupportHelpDocApi } from '#/api';

import { computed, nextTick, reactive, ref } from 'vue';

import {
  ElButton,
  ElDrawer,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElLink,
  ElMessage,
  ElOption,
  ElRadioButton,
  ElRadioGroup,
  ElSelect,
  ElUpload,
} from 'element-plus';
 
import { VbenTiptap } from '@vben/plugins/tiptap';

import { uploadFileApi } from '#/api/support/file';
import {
  addHelpDocApi,
  getHelpDocDetailApi,
  updateHelpDocApi,
} from '#/api';

defineOptions({
  name: 'SupportHelpDocFormModal',
});

interface CatalogOption {
  label: string;
  value: number;
}

const props = defineProps<{
  catalogList: SupportHelpDocApi.CatalogItem[];
}>();

const emit = defineEmits<{
  success: [];
}>();

const formRef = ref<FormInstance>();
const loading = ref(false);
const visible = ref(false);
const uploading = ref(false);
const currentHelpDocId = ref<null | number>(null);
const fileList = ref<Array<{ fileName: string; fileUrl: string; key: string }>>([]);

const formModel = reactive({
  attachment: '',
  author: '',
  contentHtml: '',
  contentText: '',
  helpDocCatalogId: undefined as number | undefined,
  homePageFlag: false,
  relationText: '',
  sort: 1,
  title: '',
});

const rules: FormRules<typeof formModel> = {
  author: [
    { message: '请输入作者', required: true, trigger: 'blur' },
    { max: 100, message: '作者长度不能超过 100 个字符', trigger: 'blur' },
  ],
  attachment: [
    { max: 1000, message: '附件字段长度不能超过 1000 个字符', trigger: 'blur' },
  ],
  contentHtml: [
    { message: '请输入正文内容', required: true, trigger: 'blur' },
  ],
  helpDocCatalogId: [
    { message: '请选择所属目录', required: true, trigger: 'change' },
  ],
  sort: [{ message: '请输入排序值', required: true, trigger: 'change' }],
  title: [
    { message: '请输入标题', required: true, trigger: 'blur' },
    { max: 200, message: '标题长度不能超过 200 个字符', trigger: 'blur' },
  ],
};

const drawerTitle = computed(() => {
  return currentHelpDocId.value ? '编辑帮助文档' : '新增帮助文档';
});

const catalogOptions = computed<CatalogOption[]>(() => {
  const childrenMap = new Map<number, SupportHelpDocApi.CatalogItem[]>();

  for (const item of props.catalogList) {
    const parentId = Number(item.parentId ?? 0);
    const children = childrenMap.get(parentId) ?? [];
    children.push(item);
    childrenMap.set(parentId, children);
  }

  const result: CatalogOption[] = [];

  function appendOptions(parentId: number, depth: number) {
    const children = (childrenMap.get(parentId) ?? []).slice().sort((left, right) => {
      return (left.sort ?? 0) - (right.sort ?? 0);
    });

    for (const item of children) {
      result.push({
        label: `${'　'.repeat(depth)}${item.name}`,
        value: item.helpDocCatalogId,
      });
      appendOptions(item.helpDocCatalogId, depth + 1);
    }
  }

  appendOptions(0, 0);
  return result;
});

function escapeHtml(content: string) {
  return content
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function buildHtmlFromText(contentText: string) {
  return escapeHtml(contentText).replaceAll(/\r?\n/g, '<br/>');
}

function parseRelationText(relationText: string) {
  const lineList = relationText
    .split(/\r?\n/)
    .map((item) => item.trim())
    .filter(Boolean);

  const relationList: SupportHelpDocApi.RelationItem[] = [];

  for (const line of lineList) {
    const [relationName, relationIdText] = line.split('|');
    const relationId = Number(relationIdText?.trim());

    if (!relationName?.trim() || !Number.isFinite(relationId)) {
      throw new Error(`关联项格式错误：${line}`);
    }

    relationList.push({
      relationId,
      relationName: relationName.trim(),
    });
  }

  return relationList;
}

function buildRelationText(relationList?: SupportHelpDocApi.RelationItem[]) {
  if (!relationList?.length) {
    return '';
  }
  return relationList
    .map((item) => `${item.relationName}|${item.relationId}`)
    .join('\n');
}

function resetFormModel() {
  currentHelpDocId.value = null;
  fileList.value = [];
  formModel.attachment = '';
  formModel.author = '';
  formModel.contentHtml = '';
  formModel.contentText = '';
  formModel.helpDocCatalogId = undefined;
  formModel.homePageFlag = false;
  formModel.relationText = '';
  formModel.sort = 1;
  formModel.title = '';
}

async function resetFormState() {
  resetFormModel();
  await nextTick();
  formRef.value?.clearValidate();
}

function close() {
  visible.value = false;
}

async function openCreate(defaultCatalogId?: number) {
  visible.value = true;
  await resetFormState();
  fileList.value = [];
  if (defaultCatalogId) {
    formModel.helpDocCatalogId = defaultCatalogId;
  }
}

async function openEdit(row: SupportHelpDocApi.HelpDocItem) {
  visible.value = true;
  loading.value = true;
  currentHelpDocId.value = row.helpDocId;
  await nextTick();
  formRef.value?.clearValidate();

  try {
    const detail = await getHelpDocDetailApi(row.helpDocId);

    formModel.attachment = detail.attachment ?? '';
    formModel.author = detail.author ?? '';
    formModel.contentHtml = detail.contentHtml ?? '';
    formModel.contentText = detail.contentText ?? '';
    formModel.helpDocCatalogId = Number(detail.helpDocCatalogId ?? 0) || undefined;
    formModel.relationText = buildRelationText(detail.relationList);
    formModel.sort = Number(row.sort ?? 1);
    formModel.title = detail.title;
 
    if (detail.attachment) {
      if (typeof detail.attachment === 'string') {
        try {
          // 尝试按照 JSON 解析附件，如果不是 JSON 则按逗号分割
          const parsed = JSON.parse(detail.attachment);
          fileList.value = Array.isArray(parsed) ? parsed : [];
        } catch {
          fileList.value = detail.attachment.split(',').filter(Boolean).map((key: string) => ({
            fileName: key.split('/').pop() || key,
            fileUrl: key,
            key: key,
          }));
        }
      } else if (Array.isArray(detail.attachment)) {
        fileList.value = detail.attachment;
      } else {
        fileList.value = [];
      }
    } else {
      fileList.value = [];
    }
  } finally {
    loading.value = false;
  }
}

async function handleUpload(options: any) {
  try {
    uploading.value = true;
    const result = await uploadFileApi(options.file);
    fileList.value.push({
      fileName: result.fileName,
      fileUrl: result.fileUrl || '',
      key: result.fileKey || ''
    });
    ElMessage.success('上传成功');
  } catch (error) {
    ElMessage.error('上传失败');
  } finally {
    uploading.value = false;
  }
}
 
function removeFile(index: number) {
  fileList.value.splice(index, 1);
}
 
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  let relationList: SupportHelpDocApi.RelationItem[] | undefined;
  try {
    relationList = parseRelationText(formModel.relationText);
  } catch (error) {
    ElMessage.warning(
      error instanceof Error
        ? error.message
        : '关联项格式错误，请按“名称|ID”逐行填写',
    );
    return;
  }

  try {
    loading.value = true;

    // 附件存为 JSON 字符串以保留文件名
    const attachment = fileList.value.length ? JSON.stringify(fileList.value) : undefined;

    const payload: SupportHelpDocApi.CreateParams = {
      attachment,
      author: formModel.author.trim(),
      // 正文 HTML 为空时，统一根据纯文本生成基础 HTML，避免后端校验失败。
      contentHtml:
        formModel.contentHtml?.trim() || buildHtmlFromText(formModel.contentText.trim()),
      contentText: formModel.contentText.trim(),
      helpDocCatalogId: Number(formModel.helpDocCatalogId),
      relationList: relationList.length ? relationList : undefined,
      sort: Number(formModel.sort),
      title: formModel.title.trim(),
    };

    if (currentHelpDocId.value) {
      await updateHelpDocApi({
        ...payload,
        helpDocId: currentHelpDocId.value,
      });
      ElMessage.success('帮助文档编辑成功');
    } else {
      await addHelpDocApi(payload);
      ElMessage.success('帮助文档新增成功');
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
  <ElDrawer
    v-model="visible"
    :close-on-click-modal="false"
    :size="960"
    :title="drawerTitle"
  >
    <ElForm
      ref="formRef"
      :model="formModel"
      :rules="rules"
      class="help-doc-form-modal__form"
      label-width="110px"
    >
      <ElFormItem label="标题" prop="title">
        <ElInput
          v-model="formModel.title"
          maxlength="200"
          placeholder="请输入标题"
        />
      </ElFormItem>

      <ElFormItem label="所属目录" prop="helpDocCatalogId">
        <ElSelect
          v-model="formModel.helpDocCatalogId"
          filterable
          placeholder="请选择所属目录"
          style="width: 100%"
        >
          <ElOption
            v-for="item in catalogOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </ElSelect>
      </ElFormItem>

      <ElFormItem label="作者" prop="author">
        <ElInput
          v-model="formModel.author"
          maxlength="100"
          placeholder="请输入作者"
        />
      </ElFormItem>

      <ElFormItem label="排序" prop="sort">
        <div class="flex items-center gap-2">
          <ElInputNumber
            v-model="formModel.sort"
            :min="0"
            controls-position="right"
            placeholder="请输入排序值"
            style="width: 120px"
          />
          <span class="help-doc-form-modal__hint">(值越小越靠前)</span>
        </div>
      </ElFormItem>
 
      <ElFormItem label="是否首页显示">
        <ElRadioGroup v-model="formModel.homePageFlag">
          <ElRadioButton :label="true">首页显示</ElRadioButton>
          <ElRadioButton :label="false">首页不用显示</ElRadioButton>
        </ElRadioGroup>
      </ElFormItem>

      <ElFormItem label="附件">
        <div class="flex flex-col items-start gap-2">
          <ElUpload
            :auto-upload="true"
            :http-request="handleUpload"
            :show-file-list="false"
            action=""
          >
            <ElButton :loading="uploading" type="primary">
              上传附件
            </ElButton>
          </ElUpload>
 
          <div v-if="fileList.length" class="w-full flex flex-col gap-2">
            <div
              v-for="(file, index) in fileList"
              :key="index"
              class="help-doc-form-modal__file-item"
            >
              <div class="flex items-center gap-2 overflow-hidden">
                <span class="help-doc-form-modal__file-icon">📎</span>
                <ElLink
                  :href="file.fileUrl"
                  class="help-doc-form-modal__file-link"
                  target="_blank"
                  type="primary"
                >
                  {{ file.fileName }}
                </ElLink>
              </div>
              <ElButton
                class="help-doc-form-modal__file-delete"
                link
                type="danger"
                @click="removeFile(index)"
              >
                删除
              </ElButton>
            </div>
          </div>
        </div>
      </ElFormItem>
 
      <ElFormItem label="关联菜单">
        <ElInput
          v-model="formModel.relationText"
          :rows="2"
          placeholder="请输入关联菜单"
          type="textarea"
        />
      </ElFormItem>
 
      <ElFormItem label="公告内容" prop="contentHtml">
        <VbenTiptap
          v-model="formModel.contentHtml"
          placeholder="请输入正文内容"
          @change="(val) => formModel.contentText = val.text"
        />
      </ElFormItem>
    </ElForm>

    <div class="help-doc-form-modal__footer">
      <div class="flex justify-end gap-3">
        <ElButton @click="close">取消</ElButton>
        <ElButton :loading="loading" type="primary" @click="submit">
          确定
        </ElButton>
      </div>
    </div>
  </ElDrawer>
</template>

<style scoped>
.help-doc-form-modal__form {
  padding-right: 12px;
}

.help-doc-form-modal__footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}
 
.help-doc-form-modal__hint {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
 
.help-doc-form-modal__file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background-color: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  transition: all 0.2s;
}
 
.help-doc-form-modal__file-item:hover {
  background-color: var(--el-fill-color);
  border-color: var(--el-color-primary-light-5);
}
 
.help-doc-form-modal__file-icon {
  flex-shrink: 0;
  font-size: 16px;
  color: var(--el-text-color-secondary);
}
 
.help-doc-form-modal__file-link {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
 
.help-doc-form-modal__file-delete {
  flex-shrink: 0;
  padding: 0;
  margin-left: 12px;
}
</style>
