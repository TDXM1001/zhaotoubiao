<script lang="ts" setup>
import type { SupportProtectApi } from '#/api';

import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  ElAlert,
  ElButton,
  ElCard,
  ElTable,
  ElTableColumn,
} from 'element-plus';

import { queryDataMaskingDemoApi } from '#/api';

defineOptions({
  name: 'SupportDataMaskingList',
});

const loading = ref(false);
const tableData = ref<SupportProtectApi.DataMaskingItem[]>([]);

async function loadData() {
  loading.value = true;
  try {
    tableData.value = await queryDataMaskingDemoApi();
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadData();
});
</script>

<template>
  <Page auto-content-height class="data-masking-page">
    <div class="data-masking-page__content">
      <ElCard class="data-masking-page__card" shadow="never">
        <ElAlert
          :closable="false"
          class="data-masking-page__alert"
          show-icon
          title="当前数据来自后端脱敏演示接口，用于验证后端字段脱敏效果。"
          type="info"
        />

        <div class="data-masking-page__toolbar">
          <ElButton :loading="loading" type="primary" @click="loadData">
            刷新数据
          </ElButton>
        </div>

        <ElTable
          v-loading="loading"
          :data="tableData"
          border
          class="data-masking-page__table"
          height="100%"
        >
          <ElTableColumn
            label="用户 ID"
            min-width="150"
            prop="userId"
            show-overflow-tooltip
          />
          <ElTableColumn label="手机号" min-width="140" prop="phone" />
          <ElTableColumn
            label="身份证"
            min-width="180"
            prop="idCard"
            show-overflow-tooltip
          />
          <ElTableColumn
            label="地址"
            min-width="240"
            prop="address"
            show-overflow-tooltip
          />
          <ElTableColumn label="密码" min-width="120" prop="password" />
          <ElTableColumn
            label="邮箱"
            min-width="180"
            prop="email"
            show-overflow-tooltip
          />
          <ElTableColumn label="车牌" min-width="120" prop="carLicense" />
          <ElTableColumn
            label="银行卡"
            min-width="180"
            prop="bankCard"
            show-overflow-tooltip
          />
          <ElTableColumn label="其它" min-width="140" prop="other" />
        </ElTable>
      </ElCard>
    </div>
  </Page>
</template>

<style scoped>
.data-masking-page__content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.data-masking-page__card {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  border: none;
}

.data-masking-page__card :deep(.el-card__body) {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.data-masking-page__alert,
.data-masking-page__toolbar {
  flex: none;
}

.data-masking-page__table {
  flex: 1;
  min-height: 0;
}
</style>
