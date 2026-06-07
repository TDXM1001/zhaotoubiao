<script lang="ts" setup>
import type { SupportLoginLogApi } from '#/api';
import type { ColumnProps } from '#/components/pro-table/types';

import { computed, reactive } from 'vue';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import { ElAlert, ElButton, ElMessage, ElTag } from 'element-plus';

import { queryLoginLogPageApi } from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';
import ProTable from '#/components/pro-table/index.vue';

defineOptions({
  name: 'SupportLoginLogList',
});

type LoginLogItem = SupportLoginLogApi.LoginLogItem;

const accessStore = useAccessStore();
const { optionMap: loginResultOptionMap } = useDictOptions('LOGIN_LOG_RESULT');

const tableColumns = reactive<ColumnProps<LoginLogItem>[]>([
  {
    label: '登录人',
    minWidth: 140,
    prop: 'userName',
    search: {
      el: 'input',
      label: '登录人',
      props: {
        placeholder: '请输入登录人',
      },
    },
  },
  {
    label: '登录 IP',
    minWidth: 150,
    prop: 'loginIp',
    search: {
      el: 'input',
      key: 'ip',
      label: '登录 IP',
      props: {
        placeholder: '请输入登录 IP',
      },
    },
  },
  {
    label: 'IP 地区',
    minWidth: 180,
    prop: 'loginIpRegion',
  },
  {
    label: '登录设备',
    minWidth: 140,
    prop: 'loginDevice',
  },
  {
    label: '结果',
    minWidth: 100,
    prop: 'loginResult',
  },
  {
    label: '备注',
    minWidth: 220,
    prop: 'remark',
  },
  {
    label: 'User-Agent',
    minWidth: 260,
    prop: 'userAgent',
  },
  {
    label: '登录时间',
    minWidth: 180,
    prop: 'createTime',
    search: {
      el: 'date-picker',
      key: 'dateRange',
      label: '登录时间',
      props: {
        endPlaceholder: '结束日期',
        startPlaceholder: '开始日期',
        type: 'daterange',
        valueFormat: 'YYYY-MM-DD',
      },
    },
  },
]);

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('support:loginLog:query');
});

async function queryLoginLogTable(params: Record<string, any>) {
  if (!hasQueryPermission.value) {
    return {
      list: [],
      total: 0,
    };
  }

  return queryLoginLogPageApi({
    endDate: params.dateRange?.[1],
    ip: params.ip?.trim() || undefined,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    searchCount: true,
    startDate: params.dateRange?.[0],
    userName: params.userName?.trim() || undefined,
  });
}

function getLoginResultText(loginResult?: number) {
  return loginResultOptionMap.value.get(String(loginResult ?? ''))?.label ?? '未知';
}

function getLoginResultTagType(loginResult?: number) {
  switch (Number(loginResult)) {
    case 0: {
      return 'success';
    }
    case 1: {
      return 'danger';
    }
    case 2: {
      return 'info';
    }
    default: {
      return 'warning';
    }
  }
}

function handleSearch() {
  if (!hasQueryPermission.value) {
    ElMessage.warning('当前账号没有登录日志查询权限');
  }
}
</script>

<template>
  <Page auto-content-height class="login-log-page">
    <div class="login-log-page__content">
      <ElAlert
        v-if="!hasQueryPermission"
        :closable="false"
        class="login-log-page__alert"
        show-icon
        title="当前账号没有登录日志查询权限"
        type="warning"
      />

      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryLoginLogTable"
        row-key="loginLogId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
        @search="handleSearch"
      >
          <template #loginResult="{ row }">
            <ElTag :type="getLoginResultTagType(row.loginResult)">
              {{ getLoginResultText(row.loginResult) }}
            </ElTag>
          </template>
        </ProTable>
    </div>
  </Page>
</template>

<style scoped>
.login-log-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.login-log-page__alert {
  margin-bottom: 4px;
}
</style>
