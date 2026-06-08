<script lang="ts" setup>
import type { BidPortalApi } from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

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
import {
  formatBidFileSize,
  getAttachmentCategoryText,
  parseRouteNumber,
} from '#/utils/bid-helper';

defineOptions({ name: 'PortalProjectDetail' });

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const projectDetail = ref<BidPortalApi.PortalProjectItem | null>(null);
const projectId = computed(() => parseRouteNumber(route.query.projectId));

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

function openSubmissionForm(row: BidPortalApi.PortalLotItem) {
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
  <section class="portal-page">
    <div class="portal-page__toolbar">
      <div>
        <h1 class="portal-page__title">项目详情</h1>
        <p class="portal-page__subtitle">查看标段信息、招标文件和参与入口。</p>
      </div>
      <ElButton :icon="ArrowLeft" @click="router.push('/bid-portal/project/list')">
        返回
      </ElButton>
    </div>

    <ElCard v-loading="loading" class="portal-card" shadow="never">
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
      <div v-else-if="!loading" class="portal-empty">未查询到项目详情</div>
    </ElCard>

    <ElCard v-if="projectDetail" class="portal-card" shadow="never">
      <template #header>
        <span>可参与标段</span>
      </template>
      <ElTable :data="projectDetail.lots ?? []" border>
        <ElTableColumn label="标段编号" min-width="150" prop="lotCode" />
        <ElTableColumn label="标段名称" min-width="200" prop="lotName" />
        <ElTableColumn label="报名截止" min-width="170" prop="registrationEndTime" />
        <ElTableColumn label="投标截止" min-width="170" prop="bidEndTime" />
        <ElTableColumn label="招标文件" min-width="240">
          <template #default="{ row }">
            <div>{{ row.tenderSummary || '--' }}</div>
            <div
              v-for="attachment in row.tenderAttachments ?? []"
              :key="attachment.attachmentId"
              class="portal-attachment"
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
  </section>
</template>

<style scoped>
.portal-attachment {
  margin-top: 4px;
  font-size: 13px;
  color: #667085;
}
</style>
