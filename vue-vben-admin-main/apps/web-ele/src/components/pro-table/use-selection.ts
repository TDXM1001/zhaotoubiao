import { computed, ref } from 'vue';

export function useSelection(rowKey = 'id') {
  const isSelected = ref(false);
  const selectedList = ref<Record<string, any>[]>([]);

  const selectedListIds = computed(() => {
    return selectedList.value.map((item) => item[rowKey]);
  });

  function selectionChange(rows: Record<string, any>[]) {
    isSelected.value = rows.length > 0;
    selectedList.value = rows;
  }

  function clearSelectedList() {
    isSelected.value = false;
    selectedList.value = [];
  }

  return {
    clearSelectedList,
    isSelected,
    selectedList,
    selectedListIds,
    selectionChange,
  };
}
