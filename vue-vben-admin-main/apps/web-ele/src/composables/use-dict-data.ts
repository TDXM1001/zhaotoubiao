import type { SupportDictApi } from '#/api';

import { computed, onMounted, ref } from 'vue';

import { getAllDictDataApi } from '#/api';

export interface DictSelectOption {
  label: string;
  style?: SupportDictApi.DictDataStyleValue;
  value: string;
}

const dictDataCache = ref<SupportDictApi.DictDataItem[]>([]);
const dictDataLoading = ref(false);
let dictDataLoadPromise: null | Promise<void> = null;

function isDictDataEnabled(item: SupportDictApi.DictDataItem) {
  return !(item.disabledFlag ?? false);
}

async function loadAllDictData() {
  if (dictDataCache.value.length > 0) {
    return;
  }

  if (!dictDataLoadPromise) {
    // 字典数据统一缓存到内存里，页面只负责按编码取值。
    dictDataLoading.value = true;
    dictDataLoadPromise = getAllDictDataApi()
      .then((result) => {
        dictDataCache.value = Array.isArray(result) ? result : [];
      })
      .finally(() => {
        dictDataLoading.value = false;
        dictDataLoadPromise = null;
      });
  }

  await dictDataLoadPromise;
}

export function useDictOptions(dictCode: string) {
  const options = computed<DictSelectOption[]>(() => {
    return dictDataCache.value
      .filter((item) => item.dictCode === dictCode && isDictDataEnabled(item))
      .slice()
      .sort((left, right) => {
        return (right.sortOrder ?? 0) - (left.sortOrder ?? 0);
      })
      .map((item) => ({
        label: item.dataLabel,
        style: item.dataStyle,
        value: item.dataValue,
      }));
  });

  const optionMap = computed(() => {
    return new Map(options.value.map((item) => [item.value, item]));
  });

  onMounted(() => {
    void loadAllDictData();
  });

  return {
    loading: computed(() => dictDataLoading.value),
    optionMap,
    options,
    refresh: loadAllDictData,
  };
}
