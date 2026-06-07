<script lang="ts" setup>
import type { SupportConfigApi } from '#/api';
import type { VbenFormSchema } from '#/adapter/form';
import type {
  ColumnProps,
  ProTableInstance,
} from '#/components/pro-table/types';

import { computed, nextTick, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import { Plus } from '@vben/icons';

import {
  ElButton,
  ElDialog,
  ElMessage,
} from 'element-plus';

import {
  addConfigApi,
  queryConfigPageApi,
  updateConfigApi,
} from '#/api';
import { useVbenForm, z } from '#/adapter/form';
import ProTable from '#/components/pro-table/index.vue';

defineOptions({
  name: 'SupportConfigList',
});

type ConfigItem = SupportConfigApi.ConfigItem;

const accessStore = useAccessStore();
const proTable = ref<ProTableInstance>();

const dialogVisible = ref(false);
const dialogLoading = ref(false);
const currentConfigId = ref<null | number | string>(null);

const tableColumns = reactive<ColumnProps<ConfigItem>[]>([
  {
    label: '参数名称',
    minWidth: 180,
    prop: 'configName',
  },
  {
    label: '参数 Key',
    minWidth: 180,
    prop: 'configKey',
    search: {
      el: 'input',
      label: '参数 Key',
      props: {
        placeholder: '请输入参数 Key',
      },
    },
  },
  {
    label: '参数值',
    minWidth: 220,
    prop: 'configValue',
  },
  {
    label: '备注',
    minWidth: 220,
    prop: 'remark',
  },
  {
    label: '创建时间',
    minWidth: 180,
    prop: 'createTime',
  },
  {
    label: '更新时间',
    minWidth: 180,
    prop: 'updateTime',
  },
  {
    fixed: 'right',
    label: '操作',
    prop: 'operation',
    width: 120,
  },
]);

const hasAddPermission = computed(() => {
  return accessStore.accessCodes.includes('support:config:add');
});

const hasUpdatePermission = computed(() => {
  return accessStore.accessCodes.includes('support:config:update');
});

const dialogTitle = computed(() => {
  return currentConfigId.value ? '编辑参数配置' : '新增参数配置';
});

const formSchema: VbenFormSchema[] = [
  {
    component: 'Input',
    componentProps: {
      placeholder: '请输入参数 Key',
    },
    fieldName: 'configKey',
    label: '参数 Key',
    rules: z.string().min(1, { message: '请输入参数 Key' }),
  },
  {
    component: 'Input',
    componentProps: {
      placeholder: '请输入参数名称',
    },
    fieldName: 'configName',
    label: '参数名称',
    rules: z.string().min(1, { message: '请输入参数名称' }),
  },
  {
    component: 'Textarea',
    componentProps: {
      placeholder: '请输入参数值',
      rows: 4,
    },
    fieldName: 'configValue',
    label: '参数值',
    rules: z.string().min(1, { message: '请输入参数值' }),
  },
  {
    component: 'Textarea',
    componentProps: {
      placeholder: '请输入备注',
      rows: 3,
    },
    fieldName: 'remark',
    label: '备注',
  },
];

const [ConfigForm, configFormApi] = useVbenForm({
  commonConfig: {
    labelWidth: 100,
    componentProps: {
      class: 'w-full',
    },
  },
  layout: 'horizontal',
  schema: formSchema,
  showDefaultActions: false,
});

async function queryConfigTable(params: Record<string, any>) {
  return queryConfigPageApi({
    configKey: params.configKey?.trim() || undefined,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    searchCount: true,
  });
}

async function reloadTable() {
  await proTable.value?.getTableList();
}

async function resetDialogForm(values?: Partial<SupportConfigApi.UpdateParams>) {
  await configFormApi.resetForm({
    values: {
      configKey: '',
      configName: '',
      configValue: '',
      remark: '',
    },
  });

  if (values) {
    await configFormApi.setValues(values);
  }
}

async function openCreateDialog() {
  currentConfigId.value = null;
  dialogVisible.value = true;
  await nextTick();
  await resetDialogForm();
}

async function openEditDialog(row: ConfigItem) {
  currentConfigId.value = row.configId;
  dialogVisible.value = true;
  await nextTick();
  await resetDialogForm({
    configId: row.configId,
    configKey: row.configKey,
    configName: row.configName,
    configValue: row.configValue,
    remark: row.remark,
  });
}

function closeDialog() {
  dialogVisible.value = false;
}

async function submitForm() {
  const { valid } = await configFormApi.validate();

  if (!valid) {
    return;
  }

  const values = await configFormApi.getValues<SupportConfigApi.CreateParams>();

  try {
    dialogLoading.value = true;

    if (currentConfigId.value) {
      await updateConfigApi({
        ...values,
        configId: currentConfigId.value,
      });
      ElMessage.success('参数配置编辑成功');
    } else {
      await addConfigApi(values);
      ElMessage.success('参数配置新增成功');
    }

    dialogVisible.value = false;
    await reloadTable();
  } finally {
    dialogLoading.value = false;
  }
}
</script>

<template>
  <Page auto-content-height class="config-page">
    <div class="config-page__content">
      <ProTable
        ref="proTable"
        :columns="tableColumns"
        :request-api="queryConfigTable"
        row-key="configId"
        :search-col="{ lg: 3, md: 2, sm: 1, xl: 3, xs: 1 }"
        :search-default-collapsed="false"
        :tool-button="['refresh', 'setting']"
      >
        <template #tableHeader>
          <div class="config-page__toolbar">
            <ElButton
              v-if="hasAddPermission"
              :icon="Plus"
              type="primary"
              @click="openCreateDialog"
            >
              新增参数
            </ElButton>
          </div>
        </template>

        <template #operation="{ row }">
          <ElButton
            v-if="hasUpdatePermission"
            link
            type="primary"
            @click="openEditDialog(row)"
          >
            编辑
          </ElButton>
        </template>
      </ProTable>
    </div>

    <ElDialog
      v-model="dialogVisible"
      :close-on-click-modal="false"
      :title="dialogTitle"
      width="720px"
    >
      <ConfigForm />

      <template #footer>
        <div class="flex justify-end gap-3">
          <ElButton @click="closeDialog">取消</ElButton>
          <ElButton :loading="dialogLoading" type="primary" @click="submitForm">
            确定
          </ElButton>
        </div>
      </template>
    </ElDialog>
  </Page>
</template>

<style scoped>
.config-page__content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.config-page__toolbar {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
</style>
