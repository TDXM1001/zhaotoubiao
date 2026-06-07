<template>
  <div class="pro-table-pagination">
    <ElPagination
      :background="true"
      :current-page="pageable.pageNum"
      layout="total, sizes, prev, pager, next, jumper"
      :page-size="pageable.pageSize"
      :page-sizes="[10, 25, 50, 100]"
      :total="pageable.total"
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<script lang="ts" setup>
import { ElPagination } from 'element-plus';

defineProps<{
  handleCurrentChange: (currentPage: number) => void;
  handleSizeChange: (size: number) => void;
  pageable: {
    pageNum: number;
    pageSize: number;
    total: number;
  };
}>();
</script>

<style scoped>
.pro-table-pagination {
  display: flex;
  justify-content: flex-end;
  width: 100%;
  margin-top: 16px;
}
</style>
