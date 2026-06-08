<script lang="ts" setup>
import type { BidPortalApi } from '#/api';

import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Refresh, Search, View } from '@element-plus/icons-vue';
import {
  ElButton,
  ElCard,
  ElForm,
  ElFormItem,
  ElInput,
  ElPagination,
  ElTable,
  ElTableColumn,
} from 'element-plus';

import { queryBidPortalProjectPageApi } from '#/api';

defineOptions({ name: 'PortalProjectList' });

const router = useRouter();
const loading = ref(false);
const projectList = ref<BidPortalApi.PortalProjectItem[]>([]);
const total = ref(0);
const queryForm = reactive({
  keyword: '',
  pageNum: 1,
  pageSize: 20,
  searchCount: true,
});

async function loadData() {
  loading.value = true;
  try {
    const result = await queryBidPortalProjectPageApi(queryForm);
    projectList.value = result.list ?? [];
    total.value = result.total ?? 0;
  } finally {
    loading.value = false;
  }
}

function resetQuery() {
  queryForm.keyword = '';
  queryForm.pageNum = 1;
  void loadData();
}

function handlePageChange(pageNum: number) {
  queryForm.pageNum = pageNum;
  void loadData();
}

function handleSizeChange(pageSize: number) {
  queryForm.pageNum = 1;
  queryForm.pageSize = pageSize;
  void loadData();
}

function openDetail(row: BidPortalApi.PortalProjectItem) {
  router.push({
    path: '/bid-portal/project/detail',
    query: { projectId: String(row.projectId) },
  });
}

onMounted(() => {
  void loadData();
});
</script>

<template>
  <section class="portal-page">
    <div class="portal-page__toolbar">
      <div>
        <h1 class="portal-page__title">项目大厅</h1>
        <p class="portal-page__subtitle">查看可参与的招标项目和标段。</p>
      </div>
    </div>

    <ElCard class="portal-card" shadow="never">
      <ElForm inline @submit.prevent>
        <ElFormItem label="关键字">
          <ElInput
            v-model="queryForm.keyword"
            clearable
            placeholder="项目名称 / 编号"
            @keyup.enter="loadData"
          />
        </ElFormItem>
        <ElFormItem>
          <ElButton :icon="Search" type="primary" @click="loadData">
            查询
          </ElButton>
          <ElButton :icon="Refresh" @click="resetQuery">重置</ElButton>
        </ElFormItem>
      </ElForm>

      <ElTable v-loading="loading" :data="projectList" border>
        <ElTableColumn label="项目编号" min-width="160" prop="projectCode" />
        <ElTableColumn label="项目名称" min-width="220" prop="projectName" />
        <ElTableColumn label="采购方式" min-width="120" prop="procurementMode" />
        <ElTableColumn label="可报名标段" min-width="120" prop="lotCount" />
        <ElTableColumn label="发布时间" min-width="180" prop="publishTime" />
        <ElTableColumn fixed="right" label="操作" width="110">
          <template #default="{ row }">
            <ElButton :icon="View" link type="primary" @click="openDetail(row)">
              查看
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

      <div class="portal-table-footer">
        <ElPagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          background
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </ElCard>
  </section>
</template>
