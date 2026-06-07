<script lang="ts" setup>
import type { SupportApiEncryptApi } from '#/api';

import { computed, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  ElAlert,
  ElButton,
  ElCard,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElTabPane,
  ElTabs,
} from 'element-plus';

import {
  testDecryptAndEncryptApi,
  testEncryptArrayApi,
  testRequestEncryptApi,
  testResponseEncryptApi,
} from '#/api';
import {
  decryptSmartAdminPayload,
  encryptSmartAdminPayload,
} from '#/api/core/smart-admin-encrypt';

defineOptions({
  name: 'SupportApiEncryptIndex',
});

const activeTab = ref('request');
const loading = ref(false);
const arrayText = ref(
  JSON.stringify(
    [
      { age: 18, name: '张三' },
      { age: 20, name: '李四' },
    ],
    null,
    2,
  ),
);

const singleForm = reactive<SupportApiEncryptApi.JweForm>({
  age: 18,
  name: '张三',
});

const requestResult = ref('');
const responseEncrypted = ref('');
const responseDecrypted = ref('');
const bothEncrypted = ref('');
const bothDecrypted = ref('');
const arrayEncrypted = ref('');
const arrayDecrypted = ref('');

const singlePayloadPreview = computed(() =>
  JSON.stringify(singleForm, null, 2),
);

function formatResult(data: unknown) {
  return JSON.stringify(data, null, 2);
}

function parseArrayPayload() {
  const data = JSON.parse(arrayText.value) as SupportApiEncryptApi.JweForm[];
  if (!Array.isArray(data)) {
    throw new Error('数组测试数据必须是 JSON 数组');
  }

  return data;
}

async function handleTestRequestEncrypt() {
  loading.value = true;
  try {
    const encryptData = encryptSmartAdminPayload(singleForm);
    const result = await testRequestEncryptApi(encryptData);
    requestResult.value = formatResult(result);
    ElMessage.success('请求加密验证成功');
  } finally {
    loading.value = false;
  }
}

async function handleTestResponseEncrypt() {
  loading.value = true;
  try {
    const encryptData = await testResponseEncryptApi({ ...singleForm });
    responseEncrypted.value = encryptData;
    responseDecrypted.value = formatResult(
      decryptSmartAdminPayload<SupportApiEncryptApi.JweForm>(encryptData),
    );
    ElMessage.success('响应加密验证成功');
  } finally {
    loading.value = false;
  }
}

async function handleTestBothEncrypt() {
  loading.value = true;
  try {
    const requestEncryptData = encryptSmartAdminPayload(singleForm);
    const responseEncryptData =
      await testDecryptAndEncryptApi(requestEncryptData);
    bothEncrypted.value = responseEncryptData;
    bothDecrypted.value = formatResult(
      decryptSmartAdminPayload<SupportApiEncryptApi.JweForm>(
        responseEncryptData,
      ),
    );
    ElMessage.success('双向加解密验证成功');
  } finally {
    loading.value = false;
  }
}

async function handleTestArrayEncrypt() {
  loading.value = true;
  try {
    const payload = parseArrayPayload();
    const responseEncryptData = await testEncryptArrayApi(
      encryptSmartAdminPayload(payload),
    );
    arrayEncrypted.value = responseEncryptData;
    arrayDecrypted.value = formatResult(
      decryptSmartAdminPayload<SupportApiEncryptApi.JweForm[]>(
        responseEncryptData,
      ),
    );
    ElMessage.success('数组加解密验证成功');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '数组数据解析失败');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <Page auto-content-height class="api-encrypt-page">
    <div class="api-encrypt-page__content">
      <ElCard class="api-encrypt-page__card" shadow="never">
        <ElAlert
          :closable="false"
          class="api-encrypt-page__alert"
          show-icon
          title="此页面只验证接口加解密能力，不修改全局请求拦截器。"
          type="info"
        />

        <div class="api-encrypt-page__form">
          <ElForm :model="singleForm" label-width="72px">
            <ElFormItem label="姓名">
              <ElInput v-model="singleForm.name" maxlength="30" />
            </ElFormItem>
            <ElFormItem label="年龄">
              <ElInputNumber v-model="singleForm.age" :min="1" :step="1" />
            </ElFormItem>
          </ElForm>

          <div class="api-encrypt-page__preview">
            <span>当前单对象载荷</span>
            <pre>{{ singlePayloadPreview }}</pre>
          </div>
        </div>

        <ElTabs v-model="activeTab" class="api-encrypt-page__tabs">
          <ElTabPane label="请求加密" name="request">
            <div class="api-encrypt-page__pane">
              <ElButton
                :loading="loading"
                type="primary"
                @click="handleTestRequestEncrypt"
              >
                测试请求加密
              </ElButton>
              <pre>{{ requestResult || '等待测试结果' }}</pre>
            </div>
          </ElTabPane>

          <ElTabPane label="响应加密" name="response">
            <div class="api-encrypt-page__pane">
              <ElButton
                :loading="loading"
                type="primary"
                @click="handleTestResponseEncrypt"
              >
                测试响应加密
              </ElButton>
              <span>响应密文</span>
              <pre>{{ responseEncrypted || '等待密文' }}</pre>
              <span>解密结果</span>
              <pre>{{ responseDecrypted || '等待解密结果' }}</pre>
            </div>
          </ElTabPane>

          <ElTabPane label="双向加密" name="both">
            <div class="api-encrypt-page__pane">
              <ElButton
                :loading="loading"
                type="primary"
                @click="handleTestBothEncrypt"
              >
                测试双向加密
              </ElButton>
              <span>响应密文</span>
              <pre>{{ bothEncrypted || '等待密文' }}</pre>
              <span>解密结果</span>
              <pre>{{ bothDecrypted || '等待解密结果' }}</pre>
            </div>
          </ElTabPane>

          <ElTabPane label="数组加密" name="array">
            <div class="api-encrypt-page__pane">
              <ElInput
                v-model="arrayText"
                :rows="8"
                type="textarea"
              />
              <ElButton
                :loading="loading"
                type="primary"
                @click="handleTestArrayEncrypt"
              >
                测试数组加解密
              </ElButton>
              <span>响应密文</span>
              <pre>{{ arrayEncrypted || '等待密文' }}</pre>
              <span>解密结果</span>
              <pre>{{ arrayDecrypted || '等待解密结果' }}</pre>
            </div>
          </ElTabPane>
        </ElTabs>
      </ElCard>
    </div>
  </Page>
</template>

<style scoped>
.api-encrypt-page__content {
  height: 100%;
}

.api-encrypt-page__card {
  min-height: 100%;
  border: none;
}

.api-encrypt-page__card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.api-encrypt-page__form {
  display: grid;
  grid-template-columns: minmax(280px, 420px) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.api-encrypt-page__preview,
.api-encrypt-page__pane {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.api-encrypt-page__tabs {
  min-height: 0;
}

.api-encrypt-page pre {
  max-height: 240px;
  padding: 12px;
  overflow: auto;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-primary);
  white-space: pre-wrap;
  word-break: break-all;
  background: var(--el-fill-color-light);
  border-radius: 6px;
}

@media (max-width: 840px) {
  .api-encrypt-page__form {
    grid-template-columns: 1fr;
  }
}
</style>
