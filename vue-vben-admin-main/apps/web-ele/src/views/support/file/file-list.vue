<script lang="ts" setup>
import type { SupportFileApi } from '#/api';
import type { ColumnProps } from '#/components/pro-table/types';

import { computed, reactive } from 'vue';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import {
  ElAlert,
  ElButton,
  ElMessage,
  ElTag,
} from 'element-plus';

import { queryFilePageApi } from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';
import ProTable from '#/components/pro-table/index.vue';

defineOptions({
  name: 'SupportFileList',
});

const accessStore = useAccessStore();

const { options: folderTypeOptions, optionMap: folderTypeOptionMap } =
  useDictOptions('FILE_FOLDER_TYPE');
const { optionMap: userTypeOptionMap } = useDictOptions('USER_TYPE');

const tableColumns = reactive<ColumnProps<SupportFileApi.FileItem>[]>([
  {
    label: '文件名称',
    minWidth: 220,
    prop: 'fileName',
    search: {
      el: 'input',
      label: '文件名称',
      props: {
        placeholder: '请输入文件名称',
      },
    },
  },
  {
    label: '文件夹类型',
    minWidth: 120,
    prop: 'folderType',
    search: {
      el: 'select',
      label: '文件夹类型',
    },
    enum: folderTypeOptions,
  },
  {
    label: '文件类型',
    minWidth: 120,
    prop: 'fileType',
    search: {
      el: 'input',
      label: '文件类型',
      props: {
        placeholder: '请输入文件类型',
      },
    },
  },
  {
    label: '文件大小',
    minWidth: 120,
    prop: 'fileSize',
  },
  {
    label: '上传人',
    minWidth: 140,
    prop: 'creatorName',
    search: {
      el: 'input',
      label: '上传人',
      props: {
        placeholder: '请输入上传人',
      },
    },
  },
  {
    label: '上传人类型',
    minWidth: 120,
    prop: 'creatorUserType',
  },
  {
    label: '文件 Key',
    minWidth: 260,
    prop: 'fileKey',
    search: {
      el: 'input',
      label: '文件 Key',
      props: {
        placeholder: '请输入文件 Key',
      },
    },
  },
  {
    label: '创建时间',
    minWidth: 180,
    prop: 'createTime',
    search: {
      el: 'date-picker',
      key: 'createDateRange',
      label: '创建时间',
      props: {
        endPlaceholder: '结束日期',
        startPlaceholder: '开始日期',
        type: 'daterange',
        valueFormat: 'YYYY-MM-DD',
      },
    },
  },
  {
    fixed: 'right',
    label: '操作',
    prop: 'operation',
    width: 160,
  },
]);

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('support:file:query');
});

async function queryFileTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    return {
      list: [],
      total: 0,
    };
  }

  return queryFilePageApi({
    createTimeBegin: params.createDateRange?.[0],
    createTimeEnd: params.createDateRange?.[1],
    creatorName: params.creatorName?.trim() || undefined,
    fileKey: params.fileKey?.trim() || undefined,
    fileName: params.fileName?.trim() || undefined,
    fileType: params.fileType?.trim() || undefined,
    // 文件夹类型可能存在 0 值，不能用 || 误删后端筛选条件。
    folderType:
      params.folderType === '' || params.folderType === undefined
        ? undefined
        : Number(params.folderType),
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    searchCount: true,
  });
}

function formatFileSize(fileSize?: number) {
  if (fileSize === undefined || fileSize === null || Number.isNaN(fileSize)) {
    return '-';
  }

  const value = Number(fileSize);
  if (value < 1024) {
    return `${value} B`;
  }
  if (value < 1024 * 1024) {
    return `${(value / 1024).toFixed(2)} KB`;
  }
  if (value < 1024 * 1024 * 1024) {
    return `${(value / 1024 / 1024).toFixed(2)} MB`;
  }
  return `${(value / 1024 / 1024 / 1024).toFixed(2)} GB`;
}

function getFolderTypeText(folderType?: number) {
  if (folderType === undefined || folderType === null) {
    return '-';
  }
  return (
    folderTypeOptionMap.value.get(String(folderType))?.label ??
    `未知(${folderType})`
  );
}

function getFolderTypeTagType(folderType?: number) {
  const style = folderTypeOptionMap.value.get(String(folderType))?.style;
  const typeMap: Record<
    string,
    'danger' | 'info' | 'primary' | 'success' | 'warning' | undefined
  > = {
    danger: 'danger',
    default: undefined,
    info: 'info',
    primary: 'primary',
    success: 'success',
    warning: 'warning',
  };

  return typeMap[style ?? 'default'];
}

function getUserTypeText(userType?: number) {
  if (userType === undefined || userType === null) {
    return '-';
  }
  return userTypeOptionMap.value.get(String(userType))?.label ?? `未知(${userType})`;
}

function handleSearch() {
  if (!hasQueryPermission.value) {
    ElMessage.warning('当前账号没有文件管理查询权限');
  }
}

function handleOpenFile(row: SupportFileApi.FileItem) {
  if (!row.fileUrl) {
    ElMessage.warning('当前文件没有可访问的预览地址');
    return;
  }

  window.open(row.fileUrl, '_blank', 'noopener,noreferrer');
}
</script>

<template>
  <Page auto-content-height class="file-page">
    <div class="file-page__content">
      <ElAlert
        v-if="!hasQueryPermission"
        :closable="false"
        class="file-page__alert"
        show-icon
        title="当前账号没有文件管理查询权限"
        type="warning"
      />

      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryFileTable"
        row-key="fileId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
        @search="handleSearch"
      >
          <template #folderType="{ row }">
            <ElTag :type="getFolderTypeTagType(row.folderType)">
              {{ getFolderTypeText(row.folderType) }}
            </ElTag>
          </template>

          <template #fileSize="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>

          <template #creatorUserType="{ row }">
            {{ getUserTypeText(row.creatorUserType) }}
          </template>

          <template #operation="{ row }">
            <ElButton link type="primary" @click="handleOpenFile(row)">
              预览文件
            </ElButton>
          </template>
        </ProTable>
    </div>
  </Page>
</template>

<style scoped>
.file-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.file-page__alert {
  margin-bottom: 4px;
}
</style>
