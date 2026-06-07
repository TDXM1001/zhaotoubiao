<script lang="ts" setup>
import type { SupportOperateLogApi } from '#/api';

import { computed, ref } from 'vue';

import {
  ElDescriptions,
  ElDescriptionsItem,
  ElDrawer,
  ElEmpty,
} from 'element-plus';

import { getOperateLogDetailApi } from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';

defineOptions({
  name: 'SupportOperateLogDetailDrawer',
});

const visible = ref(false);
const loading = ref(false);
const detail = ref<null | SupportOperateLogApi.OperateLogItem>(null);

const { optionMap: userTypeOptionMap } = useDictOptions('USER_TYPE');

const drawerTitle = computed(() => {
  return detail.value?.operateUserName
    ? `操作日志详情 - ${detail.value.operateUserName}`
    : '操作日志详情';
});

function getSuccessText(successFlag?: boolean) {
  return successFlag ? '成功' : '失败';
}

function getUserTypeText(operateUserType?: number) {
  if (operateUserType === undefined || operateUserType === null) {
    return '-';
  }
  return (
    userTypeOptionMap.value.get(String(operateUserType))?.label ??
    `未知(${operateUserType})`
  );
}

// 长文本保持原样展示，避免因为前端加工导致日志信息失真。
function formatMultilineText(value?: string) {
  return value?.trim() ? value : '-';
}

async function open(operateLogId: number | string) {
  visible.value = true;
  loading.value = true;
  detail.value = null;
  try {
    detail.value = await getOperateLogDetailApi(operateLogId);
  } finally {
    loading.value = false;
  }
}

defineExpose({
  open,
});
</script>

<template>
  <ElDrawer v-model="visible" :size="960" :title="drawerTitle">
    <div v-loading="loading" class="operate-log-detail-drawer">
      <ElEmpty
        v-if="!detail && !loading"
        description="暂无操作日志详情数据"
      />

      <template v-else-if="detail">
        <ElDescriptions
          :column="2"
          border
          class="operate-log-detail-drawer__descriptions"
        >
          <ElDescriptionsItem label="操作人">
            {{ detail.operateUserName || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="用户类型">
            {{ getUserTypeText(detail.operateUserType) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="结果">
            {{ getSuccessText(detail.successFlag) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="模块">
            {{ detail.module || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="请求方法">
            {{ detail.method || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="请求地址" :span="2">
            {{ detail.url || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="操作内容" :span="2">
            {{ detail.content || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="IP">
            {{ detail.ip || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="IP 地区">
            {{ detail.ipRegion || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="User-Agent" :span="2">
            {{ detail.userAgent || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="失败原因" :span="2">
            <pre class="operate-log-detail-drawer__text">{{ formatMultilineText(detail.failReason) }}</pre>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="请求参数" :span="2">
            <pre class="operate-log-detail-drawer__text">{{ formatMultilineText(detail.param) }}</pre>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="返回值" :span="2">
            <pre class="operate-log-detail-drawer__text">{{ formatMultilineText(detail.response) }}</pre>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="创建时间">
            {{ detail.createTime || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="更新时间">
            {{ detail.updateTime || '-' }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </template>
    </div>
  </ElDrawer>
</template>

<style scoped>
.operate-log-detail-drawer {
  height: 100%;
  overflow: auto;
}

.operate-log-detail-drawer__descriptions {
  padding-bottom: 12px;
}

.operate-log-detail-drawer__text {
  margin: 0;
  max-width: 100%;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  line-height: 20px;
}
</style>
