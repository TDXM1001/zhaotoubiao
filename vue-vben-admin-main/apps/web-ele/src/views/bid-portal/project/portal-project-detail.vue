<script lang="ts" setup>
import type { SystemBidPortalApi } from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import { ArrowLeft, Edit } from '@element-plus/icons-vue';
import {
  ElButton,
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElMessage,
  ElTable,
  ElTableColumn,
} from 'element-plus';

import { getBidPortalProjectDetailApi } from '#/api';

import { formatBidFileSize, getAttachmentCategoryText } from '../../system/bid/shared/bid-helper';

defineOptions({
  name: 'BidPortalProjectDetail',
});

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const projectDetail = ref<null | SystemBidPortalApi.PortalProjectItem>(null);
const projectId = computed(() => {
  const value = route.query.projectId;
  return value ? Number(value) : undefined;
});

async function loadDetail() {
  if (!projectId.value) {
    ElMessage.warning('缺少项目ID，无法查看详情');
    return;
  }
  loading.value = true;
  try {
    projectDetail.value = await getBidPortalProjectDetailApi(projectId.value);
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push('/bid-portal/project/list');
}

function openSubmissionForm(row: SystemBidPortalApi.PortalLotItem) {
  if (!projectDetail.value) {
    return;
  }
  router.push({
    path: '/bid-portal/submission/form',
    query: {
      lotId: String(row.lotId),
      projectId: String(projectDetail.value.projectId),
    },
  });
}

onMounted(() => {
  void loadDetail();
});
</script>

<template>
  <Page auto-content-height class="bid-portal-project-detail-page">
    <div class="bid-portal-project-detail-page__toolbar">
      <div class="bid-portal-project-detail-page__title">项目详情</div>
      <ElButton :icon="ArrowLeft" @click="goBack">返回</ElButton>
    </div>

    <ElCard v-loading="loading" shadow="never">
      <ElDescriptions v-if="projectDetail" :column="2" border>
        <ElDescriptionsItem label="项目编号">
          {{ projectDetail.projectCode || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="项目名称">
          {{ projectDetail.projectName }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="采购方式">
          {{ projectDetail.procurementMode || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="预算金额">
          {{ projectDetail.budgetAmount ?? '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="发布时间">
          {{ projectDetail.publishTime || '--' }}
        </ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>

    <ElCard
      v-if="projectDetail"
      class="bid-portal-project-detail-page__section"
      shadow="never"
    >
      <template #header>
        <span>可参与标段</span>
      </template>
      <ElTable :data="projectDetail.lots ?? []" border>
        <ElTableColumn label="标段编号" min-width="150" prop="lotCode" />
        <ElTableColumn label="标段名称" min-width="200" prop="lotName" />
        <ElTableColumn label="报名截止" min-width="170" prop="registrationEndTime" />
        <ElTableColumn label="投标截止" min-width="170" prop="bidEndTime" />
        <ElTableColumn label="招标文件" min-width="220">
          <template #default="{ row }">
            <div>{{ row.tenderSummary || '--' }}</div>
            <div
              v-for="attachment in row.tenderAttachments ?? []"
              :key="attachment.attachmentId"
              class="bid-portal-project-detail-page__attachment"
            >
              {{ getAttachmentCategoryText(attachment.fileCategory) }} /
              {{ attachment.fileName || '--' }} /
              {{ formatBidFileSize(attachment.fileSize) }}
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn fixed="right" label="操作" width="120">
          <template #default="{ row }">
            <ElButton
              :icon="Edit"
              link
              type="primary"
              @click="openSubmissionForm(row)"
            >
              参与
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-portal-project-detail-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-portal-project-detail-page__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.bid-portal-project-detail-page__section {
  margin-top: 12px;
}

.bid-portal-project-detail-page__attachment {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
