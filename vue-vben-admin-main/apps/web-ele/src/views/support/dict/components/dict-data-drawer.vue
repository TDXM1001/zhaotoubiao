<script lang="ts" setup>
import type { SupportDictApi } from '#/api';
import type DictDataFormModal from './dict-data-form-modal.vue';

import { computed, reactive, ref } from 'vue';

import { useAccessStore } from '@vben/stores';

import {
  ElButton,
  ElDrawer,
  ElEmpty,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElSpace,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {
  deleteDictDataApi,
  queryDictDataListApi,
  updateDictDataDisabledApi,
} from '#/api';

import DictDataFormModalView from './dict-data-form-modal.vue';

defineOptions({
  name: 'SupportDictDataDrawer',
});

const accessStore = useAccessStore();
const dataFormModalRef = ref<InstanceType<typeof DictDataFormModal>>();
const loading = ref(false);
const visible = ref(false);
const dataList = ref<SupportDictApi.DictDataItem[]>([]);
const filterText = ref('');

const filteredDataList = computed(() => {
  const text = filterText.value.trim().toLowerCase();
  if (!text) {
    return dataList.value;
  }
  return dataList.value.filter(
    (item) =>
      item.dataLabel.toLowerCase().includes(text) ||
      item.dataValue.toLowerCase().includes(text),
  );
});

const currentDict = reactive({
  dictCode: '',
  dictId: '' as number | string,
  dictName: '',
});

const hasAddPermission = computed(() => {
  return accessStore.accessCodes.includes('support:dictData:add');
});

const hasDeletePermission = computed(() => {
  return accessStore.accessCodes.includes('support:dictData:delete');
});

const hasQueryPermission = computed(() => {
  return accessStore.accessCodes.includes('support:dictData:query');
});

const hasUpdateDisabledPermission = computed(() => {
  return accessStore.accessCodes.includes('support:dictData:updateDisabled');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('support:dictData:update');
});

function getStatusText(disabledFlag?: boolean) {
  return disabledFlag ? '已禁用' : '已启用';
}

function getStyleLabel(dataStyle?: SupportDictApi.DictDataStyleValue) {
  const labelMap: Record<
    SupportDictApi.DictDataStyleValue,
    string
  > = {
    danger: '危险',
    default: '默认',
    info: '信息',
    primary: '主要',
    success: '成功',
    warning: '警告',
  };

  return labelMap[dataStyle ?? 'default'];
}

function getTagType(dataStyle?: SupportDictApi.DictDataStyleValue) {
  const typeMap: Partial<
    Record<
      SupportDictApi.DictDataStyleValue,
      'danger' | 'info' | 'primary' | 'success' | 'warning'
    >
  > = {
    danger: 'danger',
    info: 'info',
    primary: 'primary',
    success: 'success',
    warning: 'warning',
  };

  return typeMap[dataStyle ?? 'default'];
}

async function loadList() {
  if (!currentDict.dictId || !hasQueryPermission.value) {
    dataList.value = [];
    return;
  }

  try {
    loading.value = true;
    // 字典项接口返回的就是普通列表，不需要再做分页映射。
    dataList.value = await queryDictDataListApi(currentDict.dictId);
  } finally {
    loading.value = false;
  }
}

async function open(payload: {
  dictCode: string;
  dictId: number | string;
  dictName: string;
}) {
  currentDict.dictCode = payload.dictCode;
  currentDict.dictId = payload.dictId;
  currentDict.dictName = payload.dictName;
  visible.value = true;
  filterText.value = '';
  await loadList();
}

function openCreate() {
  dataFormModalRef.value?.openCreate({
    dictCode: currentDict.dictCode,
    dictId: currentDict.dictId,
  });
}

function openEdit(row: SupportDictApi.DictDataItem) {
  dataFormModalRef.value?.openEdit({
    ...row,
    dictCode: currentDict.dictCode,
  });
}

async function handleToggle(row: SupportDictApi.DictDataItem) {
  const actionText = row.disabledFlag ? '启用' : '禁用';

  await ElMessageBox.confirm(
    `确定要${actionText}字典项“${row.dataLabel}”吗？`,
    `${actionText}确认`,
    {
      type: 'warning',
    },
  );

  await updateDictDataDisabledApi(row.dictDataId);
  ElMessage.success(`字典项${actionText}成功`);
  await loadList();
}

async function handleDelete(row: SupportDictApi.DictDataItem) {
  await ElMessageBox.confirm(
    `确定要删除字典项“${row.dataLabel}”吗？`,
    '删除确认',
    {
      type: 'warning',
    },
  );

  await deleteDictDataApi(row.dictDataId);
  ElMessage.success('字典项删除成功');
  await loadList();
}

defineExpose({
  open,
});
</script>

<template>
  <ElDrawer v-model="visible" :size="1080" title="字典项管理">
    <div class="dict-data-drawer">
      <div class="dict-data-drawer__header">
        <div>
          <div class="dict-data-drawer__title">{{ currentDict.dictName }}</div>
          <div class="dict-data-drawer__desc">
            字典编码：{{ currentDict.dictCode }}
          </div>
        </div>

        <ElSpace>
          <ElInput
            v-model="filterText"
            clearable
            placeholder="搜索字典项名称或值"
            style="width: 240px"
          />
          <ElButton v-if="hasAddPermission" round type="primary" @click="openCreate">
            新增字典项
          </ElButton>
          <ElButton @click="loadList">刷新</ElButton>
        </ElSpace>
      </div>

      <ElTable v-loading="loading" :data="filteredDataList" border height="100%">
        <template #empty>
          <ElEmpty description="暂无字典项数据" />
        </template>

        <ElTableColumn label="显示名称" min-width="180" prop="dataLabel" />
        <ElTableColumn label="字典项值" min-width="160" prop="dataValue" />
        <ElTableColumn label="样式" min-width="120">
          <template #default="{ row }">
            <ElTag :type="getTagType(row.dataStyle)">
              {{ getStyleLabel(row.dataStyle) }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="排序值" min-width="110" prop="sortOrder" />
        <ElTableColumn label="状态" min-width="120">
          <template #default="{ row }">
            <ElTag :type="row.disabledFlag ? 'info' : 'success'">
              {{ getStatusText(row.disabledFlag) }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="备注" min-width="180" prop="remark" />
        <ElTableColumn label="创建时间" min-width="180" prop="createTime" />
        <ElTableColumn label="更新时间" min-width="180" prop="updateTime" />
        <ElTableColumn fixed="right" label="操作" min-width="240">
          <template #default="{ row }">
            <ElButton
              v-if="hasUpdatePermission"
              link
              type="primary"
              @click="openEdit(row)"
            >
              编辑
            </ElButton>
            <ElButton
              v-if="hasUpdateDisabledPermission"
              link
              type="primary"
              @click="handleToggle(row)"
            >
              {{ row.disabledFlag ? '启用' : '禁用' }}
            </ElButton>
            <ElButton
              v-if="hasDeletePermission"
              link
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </div>

    <DictDataFormModalView ref="dataFormModalRef" @success="loadList" />
  </ElDrawer>
</template>

<style scoped>
.dict-data-drawer {
  display: flex;
  height: 100%;
  flex-direction: column;
  gap: 16px;
}

.dict-data-drawer__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.dict-data-drawer__title {
  font-size: 18px;
  font-weight: 700;
  line-height: 28px;
}

.dict-data-drawer__desc {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 20px;
}

@media (max-width: 960px) {
  .dict-data-drawer__header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
