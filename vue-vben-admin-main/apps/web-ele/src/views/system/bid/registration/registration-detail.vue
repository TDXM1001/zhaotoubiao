<script lang="ts" setup>
import type { SystemBidRegistrationApi } from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import {
  ArrowLeft,
  Check,
  Close,
} from '@element-plus/icons-vue';
import {
  ElButton,
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElMessage,
  ElMessageBox,
  ElTag,
} from 'element-plus';

import {
  approveBidRegistrationApi,
  cancelBidRegistrationApi,
  getBidRegistrationDetailApi,
  rejectBidRegistrationApi,
} from '#/api';
import { useDictOptions } from '#/composables/use-dict-data';

import {
  getRegistrationStatusText,
  getRegistrationStatusTagType,
  parseRouteNumber,
} from '../shared/bid-helper';

defineOptions({
  name: 'SystemBidRegistrationDetail',
});

const route = useRoute();
const router = useRouter();
const accessStore = useAccessStore();

const loading = ref(false);
const registrationDetail =
  ref<null | SystemBidRegistrationApi.RegistrationItem>(null);
const registrationId = computed(() => parseRouteNumber(route.query.registrationId));

const { optionMap: registrationTypeMap } = useDictOptions(
  'BID_REGISTRATION_TYPE',
);

const canApprove = computed(() => {
  return accessStore.accessCodes.includes('bid:registration:approve')
    && registrationDetail.value?.status === 'SUBMITTED';
});

const canReject = computed(() => {
  return accessStore.accessCodes.includes('bid:registration:reject')
    && registrationDetail.value?.status === 'SUBMITTED';
});

const canCancel = computed(() => {
  return accessStore.accessCodes.includes('bid:registration:cancel')
    && (registrationDetail.value?.allowedActions?.includes(
      'cancel-registration',
    ) ?? false);
});

function getDictLabel(map: Map<string, { label: string }>, value?: string) {
  if (!value) {
    return '--';
  }
  return map.get(value)?.label ?? value;
}

function getListQuery() {
  if (!registrationDetail.value) {
    return undefined;
  }

  return {
    lotId: String(registrationDetail.value.lotId),
    lotName: registrationDetail.value.lotName,
    projectId: String(registrationDetail.value.projectId),
    projectName: registrationDetail.value.projectName,
  };
}

async function loadDetail() {
  if (!registrationId.value) {
    ElMessage.warning('缺少报名ID，无法查看详情');
    return;
  }

  loading.value = true;
  try {
    registrationDetail.value = await getBidRegistrationDetailApi(
      registrationId.value,
    );
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push({
    path: '/system/bid/registration/list',
    query: getListQuery(),
  });
}

async function handleApprove() {
  if (!registrationDetail.value) {
    return;
  }

  await ElMessageBox.confirm(
    `确定通过供应商“${registrationDetail.value.supplierNameSnapshot}”的报名申请吗？`,
    '报名审核确认',
    {
      type: 'warning',
    },
  );

  await approveBidRegistrationApi({
    registrationId: registrationDetail.value.registrationId,
    version: registrationDetail.value.version,
  });
  ElMessage.success('供应商报名已审核通过');
  await loadDetail();
}

async function handleReject() {
  if (!registrationDetail.value) {
    return;
  }

  const { value } = await ElMessageBox.prompt(
    `请输入驳回供应商“${registrationDetail.value.supplierNameSnapshot}”报名申请的原因`,
    '驳回报名',
    {
      confirmButtonText: '确认驳回',
      inputErrorMessage: '驳回原因不能为空',
      inputPattern: /\S+/,
      inputPlaceholder: '请输入驳回原因',
      type: 'warning',
    },
  );

  await rejectBidRegistrationApi({
    registrationId: registrationDetail.value.registrationId,
    remark: String(value).trim(),
    version: registrationDetail.value.version,
  });
  ElMessage.success('供应商报名已驳回');
  await loadDetail();
}

async function handleCancel() {
  if (!registrationDetail.value) {
    return;
  }

  const { value } = await ElMessageBox.prompt(
    `请输入取消供应商“${registrationDetail.value.supplierNameSnapshot}”报名申请的原因`,
    '取消报名',
    {
      confirmButtonText: '确认取消',
      inputErrorMessage: '取消原因不能为空',
      inputPattern: /\S+/,
      inputPlaceholder: '请输入取消原因',
      type: 'warning',
    },
  );

  await cancelBidRegistrationApi({
    registrationId: registrationDetail.value.registrationId,
    remark: String(value).trim(),
    version: registrationDetail.value.version,
  });
  ElMessage.success('供应商报名已取消');
  await loadDetail();
}

onMounted(() => {
  void loadDetail();
});
</script>

<template>
  <Page auto-content-height class="bid-registration-detail-page">
    <div class="bid-registration-detail-page__toolbar">
      <div class="bid-registration-detail-page__title">供应商报名详情</div>
      <div class="bid-registration-detail-page__actions">
        <ElButton :icon="ArrowLeft" @click="goBack">返回</ElButton>
        <ElButton
          v-if="canApprove"
          :icon="Check"
          type="success"
          @click="handleApprove"
        >
          通过
        </ElButton>
        <ElButton
          v-if="canReject"
          :icon="Close"
          type="danger"
          @click="handleReject"
        >
          驳回
        </ElButton>
        <ElButton
          v-if="canCancel"
          :icon="Close"
          type="warning"
          @click="handleCancel"
        >
          取消
        </ElButton>
      </div>
    </div>

    <ElCard v-loading="loading" shadow="never">
      <ElDescriptions v-if="registrationDetail" :column="2" border>
        <ElDescriptionsItem label="供应商名称">
          {{ registrationDetail.supplierNameSnapshot }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="统一社会信用代码">
          {{ registrationDetail.supplierCreditCode }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="所属项目">
          {{ registrationDetail.projectName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="所属标段">
          {{ registrationDetail.lotName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="报名方式">
          {{ getDictLabel(registrationTypeMap, registrationDetail.registrationType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="报名状态">
          <ElTag :type="getRegistrationStatusTagType(registrationDetail.status)">
            {{ getRegistrationStatusText(registrationDetail.status) }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="联系人">
          {{ registrationDetail.contactName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="联系电话">
          {{ registrationDetail.contactPhone || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="联系邮箱">
          {{ registrationDetail.contactEmail || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="提交时间">
          {{ registrationDetail.submitTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审核时间">
          {{ registrationDetail.qualifiedTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审核人ID">
          {{ registrationDetail.qualifiedBy || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="资格结果">
          {{ registrationDetail.qualifiedResult || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="取消时间">
          {{ registrationDetail.cancelTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="更新时间">
          {{ registrationDetail.updateTime || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="驳回原因" :span="2">
          {{ registrationDetail.rejectReason || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="取消原因" :span="2">
          {{ registrationDetail.cancelReason || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注" :span="2">
          {{ registrationDetail.remark || '--' }}
        </ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>
  </Page>
</template>

<style scoped>
.bid-registration-detail-page__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.bid-registration-detail-page__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.bid-registration-detail-page__actions {
  display: inline-flex;
  gap: 8px;
}
</style>
