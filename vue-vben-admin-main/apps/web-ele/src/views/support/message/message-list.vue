<script lang="ts" setup>
import type MessageFormModal from './components/message-form-modal.vue';

import type { SupportMessageApi, SystemEmployeeApi } from '#/api';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import { Plus } from '@vben/icons';

import {
  ElAlert,
  ElButton,
  ElMessage,
  ElMessageBox,
  ElTag,
} from 'element-plus';

import {
  deleteMessageApi,
  queryAllEmployeeApi,
  queryMessagePageApi,
} from '#/api';
import ProTable from '#/components/pro-table/index.vue';

import MessageFormModalView from './components/message-form-modal.vue';

defineOptions({
  name: 'SupportMessageList',
});

const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();
const messageFormModalRef = ref<InstanceType<typeof MessageFormModal>>();
const employeeOptions = ref<SystemEmployeeApi.EmployeeOption[]>([]);

const messageTypeOptions = [
  { label: '站内信', tagType: 'primary', value: 1 },
  { label: '订单', tagType: 'warning', value: 2 },
];

const readFlagOptions = [
  { label: '已读', tagType: 'success', value: true },
  { label: '未读', tagType: 'info', value: false },
];

const tableColumns = reactive<ColumnProps<SupportMessageApi.MessageItem>[]>([
  {
    label: '标题',
    minWidth: 220,
    prop: 'title',
    search: {
      el: 'input',
      key: 'searchWord',
      label: '关键字',
      props: {
        placeholder: '请输入标题或内容关键字',
      },
    },
  },
  {
    enum: messageTypeOptions,
    label: '消息类型',
    minWidth: 120,
    prop: 'messageType',
    search: {
      el: 'select',
      label: '消息类型',
    },
  },
  {
    label: '接收员工',
    minWidth: 140,
    prop: 'receiverUserId',
    search: {
      el: 'input',
      label: '接收员工 ID',
      props: {
        placeholder: '请输入员工 ID',
      },
    },
  },
  {
    label: '内容',
    minWidth: 280,
    prop: 'content',
  },
  {
    enum: readFlagOptions,
    label: '阅读状态',
    minWidth: 110,
    prop: 'readFlag',
    search: {
      el: 'select',
      label: '阅读状态',
    },
  },
  {
    label: '已读时间',
    minWidth: 180,
    prop: 'readTime',
  },
  {
    label: '创建时间',
    minWidth: 180,
    prop: 'createTime',
    search: {
      el: 'date-picker',
      key: 'dateRange',
      label: '创建日期',
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
    width: 120,
  },
]);

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('system:message:query');
});

const hasSendPermission = computed(() => {
  return accessStore.accessCodes.includes('system:message:send');
});

const hasDeletePermission = computed(() => {
  return accessStore.accessCodes.includes('system:message:delete');
});

const employeeMap = computed(() => {
  return new Map(employeeOptions.value.map((item) => [item.employeeId, item]));
});

async function queryMessageTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    return {
      list: [],
      total: 0,
    };
  }

  return queryMessagePageApi({
    endDate: params.dateRange?.[1],
    messageType:
      params.messageType === '' || params.messageType === undefined
        ? undefined
        : (Number(params.messageType) as SupportMessageApi.MessageType),
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    readFlag: params.readFlag,
    receiverUserId: params.receiverUserId
      ? Number(params.receiverUserId)
      : undefined,
    receiverUserType: 1,
    searchCount: true,
    searchWord: params.searchWord?.trim() || undefined,
    startDate: params.dateRange?.[0],
  });
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

async function loadEmployeeOptions() {
  employeeOptions.value = await queryAllEmployeeApi();
}

function getMessageTypeText(messageType?: number) {
  return (
    messageTypeOptions.find((item) => item.value === messageType)?.label ??
    `未知(${messageType ?? '--'})`
  );
}

function getMessageTypeTagType(messageType?: number) {
  return messageType === 2 ? 'warning' : 'primary';
}

function getReadText(readFlag?: boolean) {
  return readFlag ? '已读' : '未读';
}

function getReadTagType(readFlag?: boolean) {
  return readFlag ? 'success' : 'info';
}

function getReceiverText(row: SupportMessageApi.MessageItem) {
  const employee = row.receiverUserId
    ? employeeMap.value.get(row.receiverUserId)
    : undefined;
  if (!employee) {
    return row.receiverUserId ? `员工 ${row.receiverUserId}` : '--';
  }

  return employee.departmentName
    ? `${employee.actualName} / ${employee.departmentName}`
    : employee.actualName;
}

function handleSearch() {
  if (!hasQueryPermission.value) {
    ElMessage.warning('当前账号没有消息查询权限');
  }
}

function openMessageModal() {
  messageFormModalRef.value?.open();
}

async function handleDeleteRow(row: SupportMessageApi.MessageItem) {
  await ElMessageBox.confirm(
    `确定要删除消息“${row.title ?? row.messageId}”吗？`,
    '删除确认',
    {
      type: 'warning',
    },
  );

  await deleteMessageApi(row.messageId);
  ElMessage.success('消息删除成功');
  await reloadTable();
}

onMounted(() => {
  void loadEmployeeOptions();
});
</script>

<template>
  <Page auto-content-height class="message-page">
    <div class="message-page__content">
      <ElAlert
        v-if="!hasQueryPermission"
        :closable="false"
        class="message-page__alert"
        show-icon
        title="当前账号没有消息查询权限"
        type="warning"
      />

      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryMessageTable"
        row-key="messageId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
        @search="handleSearch"
      >
        <template #tableHeader>
          <div class="message-page__toolbar">
            <ElButton
              v-if="hasSendPermission"
              :icon="Plus"
              type="primary"
              @click="openMessageModal"
            >
              发送消息
            </ElButton>
          </div>
        </template>

        <template #messageType="{ row }">
          <ElTag :type="getMessageTypeTagType(row.messageType)">
            {{ getMessageTypeText(row.messageType) }}
          </ElTag>
        </template>

        <template #receiverUserId="{ row }">
          {{ getReceiverText(row) }}
        </template>

        <template #readFlag="{ row }">
          <ElTag :type="getReadTagType(row.readFlag)">
            {{ getReadText(row.readFlag) }}
          </ElTag>
        </template>

        <template #operation="{ row }">
          <ElButton
            v-if="hasDeletePermission"
            link
            type="danger"
            @click="handleDeleteRow(row)"
          >
            删除
          </ElButton>
        </template>
      </ProTable>
    </div>

    <MessageFormModalView ref="messageFormModalRef" @success="reloadTable" />
  </Page>
</template>

<style scoped>
.message-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.message-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.message-page__alert {
  margin-bottom: 4px;
}
</style>
