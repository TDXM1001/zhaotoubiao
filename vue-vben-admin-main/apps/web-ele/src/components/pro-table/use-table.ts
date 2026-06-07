import type { TableState } from './types';

import { computed, reactive, toRefs } from 'vue';

import { cleanTableParams, normalizeTableResponse } from './utils';

export function useTable(
  api?: (params: any) => Promise<any>,
  initParam: Record<string, any> = {},
  isPageable = true,
  dataCallback?: (data: any) => any,
  requestError?: (error: any) => void,
) {
  const state = reactive<TableState>({
    pageable: {
      pageNum: 1,
      pageSize: 10,
      total: 0,
    },
    searchInitParam: {},
    searchParam: {},
    tableData: [],
    totalParam: {},
  });

  const pageParam = computed(() => ({
    pageNum: state.pageable.pageNum,
    pageSize: state.pageable.pageSize,
  }));

  function updateTotalParam() {
    state.totalParam = {
      ...cleanTableParams(state.searchParam),
    };
  }

  async function getTableList() {
    if (!api) {
      return;
    }

    try {
      const params = {
        ...state.searchInitParam,
        ...cleanTableParams(initParam),
        ...state.totalParam,
        ...(isPageable ? pageParam.value : {}),
      };
      const response = await api(params);
      const normalized = normalizeTableResponse(
        dataCallback?.(response) ?? response,
      );

      state.tableData = normalized.list;

      if (isPageable) {
        state.pageable.total = normalized.total;
      }
    } catch (error) {
      requestError?.(error);
      throw error;
    }
  }

  function search() {
    state.pageable.pageNum = 1;
    updateTotalParam();
    return getTableList();
  }

  function reset() {
    state.pageable.pageNum = 1;
    state.searchParam = { ...state.searchInitParam };
    updateTotalParam();
    return getTableList();
  }

  function handleSizeChange(size: number) {
    state.pageable.pageNum = 1;
    state.pageable.pageSize = size;
    return getTableList();
  }

  function handleCurrentChange(currentPage: number) {
    state.pageable.pageNum = currentPage;
    return getTableList();
  }

  return {
    ...toRefs(state),
    getTableList,
    handleCurrentChange,
    handleSizeChange,
    reset,
    search,
    updateTotalParam,
  };
}
