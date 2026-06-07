<script lang="ts" setup>
import type { SupportHeartBeatApi } from '#/api';
import type { ColumnProps } from '#/components/pro-table/types';

import { reactive } from 'vue';

import { Page } from '@vben/common-ui';

import { ElCard } from 'element-plus';

import { queryHeartBeatPageApi } from '#/api';
import ProTable from '#/components/pro-table/index.vue';

defineOptions({
  name: 'SupportHeartBeatList',
});

const tableColumns = reactive<
  ColumnProps<SupportHeartBeatApi.HeartBeatRecord>[]
>([
  {
    label: '项目路径',
    minWidth: 260,
    prop: 'projectPath',
    search: {
      el: 'input',
      key: 'keywords',
      label: '关键字',
      props: {
        placeholder: '请输入项目路径、服务器 IP 或进程号',
      },
    },
  },
  {
    label: '服务器ip',
    minWidth: 150,
    prop: 'serverIp',
  },
  {
    label: '进程号',
    minWidth: 110,
    prop: 'processNo',
  },
  {
    label: '进程开启时间',
    minWidth: 180,
    prop: 'processStartTime',
  },
  {
    label: '心跳当前时间',
    minWidth: 180,
    prop: 'heartBeatTime',
    search: {
      el: 'date-picker',
      key: 'dateRange',
      label: '心跳时间',
      props: {
        endPlaceholder: '结束日期',
        startPlaceholder: '开始日期',
        type: 'daterange',
        valueFormat: 'YYYY-MM-DD',
      },
    },
  },
]);

async function queryHeartBeatTable(params: Record<string, any>) {
  return queryHeartBeatPageApi({
    endDate: params.dateRange?.[1],
    keywords: params.keywords?.trim() || undefined,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    searchCount: true,
    startDate: params.dateRange?.[0],
  });
}
</script>

<template>
  <Page auto-content-height class="heart-beat-page">
    <div class="heart-beat-page__content">
      <!-- <div class="heart-beat-intro">
        <div class="intro-title">Smart-Heart-Beat 心跳服务介绍：</div>
        <div class="intro-item">
          <span class="intro-label">简介:</span>
          Smart-Heart-Beat 是心跳服务，用于监测 Java 应用的状态等其他信息。
        </div>
        <div class="intro-item">
          <span class="intro-label">原理:</span>
          Java 后端会在项目启动的时候开启一个线程，每隔一段时间将该应用的 IP、进程号更新到数据库 t_heart_beat_record 表中。
        </div>
        <div class="intro-usage">
          <div class="intro-label">用途:</div>
          <div>1) 在各个环境（无论开发、测试、生产）能统一看到所有启动的服务列表；</div>
          <div>2) 检测 Java 应用是否存活；</div>
          <div>3) 当某些业务只允许有一个服务启动的时候，用于排查是否别人也启动的服务；</div>
          <div class="recommend">※强烈推荐※</div>
        </div>
      </div> -->

      <ElCard class="heart-beat-page__card" shadow="never">
        <ProTable
          :columns="tableColumns"
          :request-api="queryHeartBeatTable"
          :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
          row-key="heartBeatRecordId"
        />
      </ElCard>
    </div>
  </Page>
</template>

<style scoped>
.heart-beat-page__content {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 16px;
}

.heart-beat-intro {
  padding: 16px 20px;
  background-color: var(--el-color-primary-light-9);
  border: 1px solid var(--el-color-primary-light-8);
  border-radius: 4px;
  color: var(--el-text-color-primary);
  font-size: 14px;
  line-height: 1.6;
}

.intro-title {
  margin-bottom: 12px;
  font-weight: bold;
  font-size: 16px;
}

.intro-item {
  margin-bottom: 8px;
}

.intro-label {
  font-weight: bold;
  margin-right: 4px;
}

.intro-usage {
  margin-top: 12px;
}

.recommend {
  margin-top: 8px;
  font-weight: bold;
}

.heart-beat-page__card {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  border: none;
}

.heart-beat-page__card :deep(.el-card__body) {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}
</style>
