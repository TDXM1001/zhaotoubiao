<script lang="ts" setup>
import type { SupportHelpDocApi } from '#/api';

import { computed, ref } from 'vue';

import {
  ElDescriptions,
  ElDescriptionsItem,
  ElDivider,
  ElDrawer,
  ElEmpty,
  ElTag,
} from 'element-plus';

import { getHelpDocDetailApi } from '#/api';

defineOptions({
  name: 'SupportHelpDocDetailDrawer',
});

const visible = ref(false);
const loading = ref(false);
const detail = ref<null | SupportHelpDocApi.HelpDocDetailItem>(null);

const drawerTitle = computed(() => {
  return detail.value?.title
    ? `帮助文档详情 - ${detail.value.title}`
    : '帮助文档详情';
});

function formatAttachment(attachment?: any) {
  if (typeof attachment !== 'string') {
    return '-';
  }
  return attachment.trim() ? attachment : '-';
}

async function open(helpDocId: number) {
  visible.value = true;
  loading.value = true;
  detail.value = null;

  try {
    detail.value = await getHelpDocDetailApi(helpDocId);
  } finally {
    loading.value = false;
  }
}

defineExpose({
  open,
});
</script>

<template>
  <ElDrawer v-model="visible" :size="1080" :title="drawerTitle">
    <div v-loading="loading" class="help-doc-detail-drawer">
      <ElEmpty
        v-if="!detail && !loading"
        description="暂无帮助文档详情数据"
      />

      <template v-else-if="detail">
        <ElDescriptions :column="2" border class="help-doc-detail-drawer__descriptions">
          <ElDescriptionsItem label="标题" :span="2">
            {{ detail.title }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="目录">
            {{ detail.helpDocCatalogName || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="作者">
            {{ detail.author || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="页面浏览量">
            {{ detail.pageViewCount ?? 0 }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="用户浏览量">
            {{ detail.userViewCount ?? 0 }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="附件 Key" :span="2">
            <pre class="help-doc-detail-drawer__text">{{ formatAttachment(detail.attachment) }}</pre>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="创建时间">
            {{ detail.createTime || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="更新时间">
            {{ detail.updateTime || '-' }}
          </ElDescriptionsItem>
        </ElDescriptions>

        <ElDivider content-position="left">正文文本</ElDivider>
        <pre class="help-doc-detail-drawer__text help-doc-detail-drawer__block">
{{ detail.contentText || '-' }}
        </pre>

        <ElDivider content-position="left">正文 HTML 预览</ElDivider>
        <div
          class="help-doc-detail-drawer__html help-doc-detail-drawer__block"
          v-html="detail.contentHtml || '-'"
        />

        <ElDivider content-position="left">关联项</ElDivider>
        <div v-if="detail.relationList?.length" class="help-doc-detail-drawer__tag-list">
          <ElTag
            v-for="item in detail.relationList"
            :key="`${item.relationName}-${item.relationId}`"
            class="help-doc-detail-drawer__tag"
            type="info"
          >
            {{ item.relationName }} | {{ item.relationId }}
          </ElTag>
        </div>
        <ElEmpty v-else :image-size="80" description="暂无关联项" />
      </template>
    </div>
  </ElDrawer>
</template>

<style scoped>
.help-doc-detail-drawer {
  height: 100%;
  overflow: auto;
  padding-right: 8px;
}

.help-doc-detail-drawer__descriptions {
  padding-bottom: 8px;
}

.help-doc-detail-drawer__block {
  margin-bottom: 16px;
}

.help-doc-detail-drawer__text {
  margin: 0;
  max-width: 100%;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  line-height: 22px;
}

.help-doc-detail-drawer__html {
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  padding: 16px;
  line-height: 1.8;
}

.help-doc-detail-drawer__tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.help-doc-detail-drawer__tag {
  margin-right: 0;
}
</style>
