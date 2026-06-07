<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type { SupportReloadApi } from '#/api';

import { computed, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import {
  ElAlert,
  ElButton,
  ElCard,
  ElDialog,
  ElDrawer,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElSpace,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {
  queryReloadListApi,
  queryReloadResultApi,
  updateReloadApi,
} from '#/api';

defineOptions({
  name: 'SupportReloadList',
});

const accessStore = useAccessStore();

const loading = ref(false);
const resultLoading = ref(false);
const saving = ref(false);
const reloadList = ref<SupportReloadApi.ReloadItem[]>([]);
const resultList = ref<SupportReloadApi.ReloadResult[]>([]);
const resultDrawerVisible = ref(false);
const resultTag = ref('');
const updateDialogVisible = ref(false);
const updateFormRef = ref<FormInstance>();

const updateForm = reactive<SupportReloadApi.UpdateParams>({
  args: '',
  identification: '',
  tag: '',
});

const updateRules: FormRules<SupportReloadApi.UpdateParams> = {
  identification: [
    { message: '请输入状态标识', required: true, trigger: 'blur' },
  ],
  tag: [{ message: '缺少 Reload 标签', required: true, trigger: 'blur' }],
};

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('support:reload:query');
});

const hasResultPermission = computed(() => {
  return accessStore.accessCodes.includes('support:reload:result');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('support:reload:update');
});

async function loadReloadList() {
  if (!hasQueryPermission.value) {
    reloadList.value = [];
    return;
  }

  loading.value = true;
  try {
    reloadList.value = await queryReloadListApi();
  } finally {
    loading.value = false;
  }
}

async function handleRefresh() {
  await loadReloadList();
}

async function handleOpenResult(row: SupportReloadApi.ReloadItem) {
  if (!hasResultPermission.value) {
    ElMessage.warning('当前账号没有查看 Reload 结果权限');
    return;
  }

  resultTag.value = row.tag;
  resultDrawerVisible.value = true;
  resultLoading.value = true;
  try {
    resultList.value = await queryReloadResultApi(row.tag);
  } finally {
    resultLoading.value = false;
  }
}

function handleOpenUpdate(row: SupportReloadApi.ReloadItem) {
  updateForm.tag = row.tag;
  updateForm.identification = row.identification ?? '';
  updateForm.args = row.args ?? '';
  updateDialogVisible.value = true;
}

async function handleSubmitUpdate() {
  await updateFormRef.value?.validate();

  await ElMessageBox.confirm(
    `确定要更新 Reload「${updateForm.tag}」的状态标识吗？`,
    '更新确认',
    {
      type: 'warning',
    },
  );

  saving.value = true;
  try {
    await updateReloadApi({
      args: updateForm.args?.trim() || undefined,
      identification: updateForm.identification.trim(),
      tag: updateForm.tag,
    });
    ElMessage.success('Reload 更新成功');
    updateDialogVisible.value = false;
    await loadReloadList();
  } finally {
    saving.value = false;
  }
}

function getResultTagType(result?: boolean) {
  return result ? 'success' : 'danger';
}

function getResultText(result?: boolean) {
  return result ? '成功' : '失败';
}

onMounted(() => {
  void loadReloadList();
});
</script>

<template>
  <Page auto-content-height class="reload-page">
    <div class="reload-page__content">
      <!-- 介绍卡片 -->
      <!-- <ElCard class="reload-intro-card" shadow="never">
        <div class="intro-title">Smart-Reload 心跳服务介绍:</div>
        <div class="intro-content">
          <p>
            <span class="label">简介:</span>
            SmartReload是一个可以在不重启进程的情况下动态重新加载配置或者执行某些预先设置的代码。
          </p>
          <div class="intro-section">
            <div class="label">原理:</div>
            <ul>
              <li>
                Java后端会在项目启动的时候开启一个Daemon线程，这个Daemon线程会每隔几秒轮询t_smart_item表的状态。
              </li>
              <li>
                如果【状态标识】与【上次状态标识】比较发生变化，会将参数传入SmartReload实现类，进行自定义操作。
              </li>
            </ul>
          </div>
          <div class="intro-section">
            <div class="label">用途:</div>
            <ul>
              <li>用于刷新内存中的缓存</li>
              <li>用于执行某些后门代码</li>
              <li>用于进行Java热加载（前提是类结构不发生变化）</li>
              <li>其他不能重启服务的应用</li>
            </ul>
          </div>
        </div>
      </ElCard> -->

      <ElCard class="reload-table-card" shadow="never">
        <ElAlert
          v-if="!hasQueryPermission"
          :closable="false"
          class="reload-page__alert"
          show-icon
          title="当前账号没有 Reload 查询权限"
          type="warning"
        />

        <div class="reload-page__toolbar">
          <div class="toolbar-left">
            <span class="table-title">数据列表</span>
          </div>
          <div class="toolbar-right">
            <ElSpace>
              <ElButton
                :disabled="!hasQueryPermission"
                :loading="loading"
                type="primary"
                @click="handleRefresh"
              >
                刷新
              </ElButton>
            </ElSpace>
          </div>
        </div>

        <ElTable
          v-loading="loading"
          :data="reloadList"
          border
          class="reload-page__table"
          row-key="tag"
        >
          <ElTableColumn label="标签" min-width="150" prop="tag" />
          <ElTableColumn
            label="运行标识"
            min-width="150"
            prop="identification"
            show-overflow-tooltip
          />
          <ElTableColumn
            label="参数"
            min-width="200"
            prop="args"
            show-overflow-tooltip
          />
          <ElTableColumn label="更新时间" min-width="180" prop="updateTime" />
          <ElTableColumn label="创建时间" min-width="180" prop="createTime" />
          <ElTableColumn align="center" fixed="right" label="操作" width="180">
            <template #default="{ row }">
              <ElButton
                v-if="hasUpdatePermission"
                link
                type="primary"
                @click="handleOpenUpdate(row)"
              >
                执行
              </ElButton>
              <ElButton
                v-if="hasResultPermission"
                link
                type="primary"
                @click="handleOpenResult(row)"
              >
                查看结果
              </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </ElCard>
    </div>

    <ElDrawer
      v-model="resultDrawerVisible"
      :title="`Reload 结果 - ${resultTag}`"
      size="760px"
    >
      <ElTable
        v-loading="resultLoading"
        :data="resultList"
        border
        height="100%"
        row-key="createTime"
      >
        <ElTableColumn label="参数" min-width="180" prop="args" />
        <ElTableColumn align="center" label="结果" width="110">
          <template #default="{ row }">
            <ElTag :type="getResultTagType(row.result)">
              {{ getResultText(row.result) }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn
          label="异常"
          min-width="260"
          prop="exception"
          show-overflow-tooltip
        />
        <ElTableColumn label="创建时间" min-width="180" prop="createTime" />
      </ElTable>
    </ElDrawer>

    <ElDialog
      v-model="updateDialogVisible"
      append-to-body
      title="执行 Reload"
      width="560px"
    >
      <ElForm
        ref="updateFormRef"
        :model="updateForm"
        :rules="updateRules"
        label-width="92px"
      >
        <ElFormItem label="标签" prop="tag">
          <ElInput v-model="updateForm.tag" disabled />
        </ElFormItem>
        <ElFormItem label="运行标识" prop="identification">
          <ElInput
            v-model="updateForm.identification"
            maxlength="100"
            placeholder="请输入运行标识"
          />
        </ElFormItem>
        <ElFormItem label="参数">
          <ElInput
            v-model="updateForm.args"
            :rows="4"
            placeholder="请输入参数"
            type="textarea"
          />
        </ElFormItem>
      </ElForm>

      <template #footer>
        <ElButton @click="updateDialogVisible = false">取消</ElButton>
        <ElButton :loading="saving" type="primary" @click="handleSubmitUpdate">
          提交执行
        </ElButton>
      </template>
    </ElDialog>
  </Page>
</template>

<style scoped>
.reload-page__content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.reload-intro-card {
  background-color: #f0f7ff;
  border: 1px solid #c2e0ff;
}

.intro-title {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.intro-content {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

.intro-content p {
  margin: 0 0 8px;
}

.intro-section {
  margin-top: 12px;
}

.intro-content .label {
  font-weight: bold;
  color: #303133;
}

.intro-content ul {
  margin: 4px 0 0;
  padding-left: 20px;
}

.intro-content li {
  list-style-type: disc;
}

.reload-table-card {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.reload-table-card :deep(.el-card__body) {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.reload-page__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.table-title {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
}

.reload-page__table {
  flex: 1;
  min-height: 0;
}
</style>

