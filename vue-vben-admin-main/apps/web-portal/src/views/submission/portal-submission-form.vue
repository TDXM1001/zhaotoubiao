<script lang="ts" setup>
import type { BidPortalApi } from '#/api';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';

import {
  Check,
  Lock,
  Plus,
  RefreshLeft,
  SwitchButton,
} from '@element-plus/icons-vue';
import {
  ElButton,
  ElCard,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
} from 'element-plus';

import {
  createBidPortalRegistrationApi,
  createBidPortalSubmissionApi,
  getBidPortalAccessToken,
  getBidPortalCurrentSupplierApi,
  loginBidPortalAccountApi,
  logoutBidPortalAccountApi,
  registerBidPortalAccountApi,
  submitBidPortalSubmissionApi,
  withdrawBidPortalSubmissionApi,
} from '#/api';
import { parseRouteNumber } from '#/utils/bid-helper';

defineOptions({ name: 'PortalSubmissionForm' });

const route = useRoute();
const authMode = ref<'login' | 'register'>('login');
const authLoading = ref(false);
const portalAuth = ref<null | BidPortalApi.PortalAuthInfo>(null);

const authForm = reactive({
  contactPhone: '',
  loginName: '',
  password: '',
  supplierCreditCode: '',
});

const baseForm = reactive({
  contactName: '',
  contactPhone: '',
  lotId: parseRouteNumber(route.query.lotId) ?? 0,
  projectId: parseRouteNumber(route.query.projectId) ?? 0,
  registrationId: undefined as number | undefined,
  registrationType: 'PUBLIC',
  remark: '',
  submissionId: undefined as number | undefined,
  version: undefined as number | undefined,
});

const submitForm = reactive({
  fileManifestJson: '',
  priceAmount: undefined as number | undefined,
});

const isPortalLoggedIn = computed(() => Boolean(portalAuth.value));

function ensurePortalLogin() {
  if (isPortalLoggedIn.value) {
    return true;
  }
  ElMessage.warning('请先登录供应商门户账号');
  return false;
}

async function loadCurrentSupplier() {
  if (!getBidPortalAccessToken()) {
    portalAuth.value = null;
    return;
  }
  try {
    portalAuth.value = await getBidPortalCurrentSupplierApi();
  } catch {
    portalAuth.value = null;
  }
}

async function handlePortalLogin() {
  if (!authForm.loginName || !authForm.password) {
    ElMessage.warning('请填写登录名和密码');
    return;
  }
  authLoading.value = true;
  try {
    portalAuth.value = await loginBidPortalAccountApi({
      loginName: authForm.loginName.trim(),
      password: authForm.password,
    });
    ElMessage.success('登录成功');
  } finally {
    authLoading.value = false;
  }
}

async function handlePortalRegister() {
  if (
    !authForm.loginName ||
    !authForm.password ||
    !authForm.supplierCreditCode ||
    !authForm.contactPhone
  ) {
    ElMessage.warning('请填写注册信息');
    return;
  }
  authLoading.value = true;
  try {
    portalAuth.value = await registerBidPortalAccountApi({
      contactPhone: authForm.contactPhone.trim(),
      loginName: authForm.loginName.trim(),
      password: authForm.password,
      supplierCreditCode: authForm.supplierCreditCode.trim(),
    });
    ElMessage.success('注册成功');
  } finally {
    authLoading.value = false;
  }
}

async function handlePortalLogout() {
  await logoutBidPortalAccountApi();
  portalAuth.value = null;
  ElMessage.success('已退出');
}

async function handleCreateRegistration() {
  if (!ensurePortalLogin()) {
    return;
  }
  await createBidPortalRegistrationApi({
    contactName: baseForm.contactName,
    contactPhone: baseForm.contactPhone,
    lotId: baseForm.lotId,
    projectId: baseForm.projectId,
    registrationType: baseForm.registrationType,
    remark: baseForm.remark,
  });
  ElMessage.success('报名已提交');
}

async function handleCreateSubmission() {
  if (!ensurePortalLogin()) {
    return;
  }
  if (!baseForm.registrationId) {
    ElMessage.warning('请先填写报名ID');
    return;
  }
  await createBidPortalSubmissionApi({
    lotId: baseForm.lotId,
    projectId: baseForm.projectId,
    registrationId: baseForm.registrationId,
    remark: baseForm.remark,
  });
  ElMessage.success('投标记录已创建');
}

async function handleSubmitBid() {
  if (!ensurePortalLogin()) {
    return;
  }
  if (!baseForm.submissionId || baseForm.version === undefined) {
    ElMessage.warning('请填写投标ID和版本号');
    return;
  }
  await submitBidPortalSubmissionApi({
    contactName: baseForm.contactName,
    contactPhone: baseForm.contactPhone,
    fileManifestJson: submitForm.fileManifestJson,
    priceAmount: submitForm.priceAmount,
    remark: baseForm.remark,
    submissionId: baseForm.submissionId,
    version: baseForm.version,
  });
  ElMessage.success('投标已提交');
}

async function handleWithdrawBid() {
  if (!ensurePortalLogin()) {
    return;
  }
  if (!baseForm.submissionId || baseForm.version === undefined) {
    ElMessage.warning('请填写投标ID和版本号');
    return;
  }
  await withdrawBidPortalSubmissionApi({
    remark: baseForm.remark,
    submissionId: baseForm.submissionId,
    version: baseForm.version,
  });
  ElMessage.success('投标已撤回');
}

onMounted(() => {
  void loadCurrentSupplier();
});
</script>

<template>
  <section class="portal-page">
    <div class="portal-page__toolbar">
      <div>
        <h1 class="portal-page__title">投标办理</h1>
        <p class="portal-page__subtitle">登录供应商账号后提交报名和投标信息。</p>
      </div>
    </div>

    <ElCard class="portal-card" shadow="never">
      <template #header>
        <span>门户账号</span>
      </template>
      <div v-if="portalAuth" class="portal-auth-info">
        <div>
          <div class="portal-auth-info__name">{{ portalAuth.supplierName }}</div>
          <div class="portal-auth-info__meta">
            {{ portalAuth.supplierCreditCode }} / {{ portalAuth.loginName }}
          </div>
        </div>
        <ElButton :icon="SwitchButton" @click="handlePortalLogout">
          退出
        </ElButton>
      </div>
      <ElForm v-else label-width="120px">
        <div class="portal-auth-tabs">
          <ElButton
            :type="authMode === 'login' ? 'primary' : 'default'"
            @click="authMode = 'login'"
          >
            登录
          </ElButton>
          <ElButton
            :type="authMode === 'register' ? 'primary' : 'default'"
            @click="authMode = 'register'"
          >
            注册
          </ElButton>
        </div>
        <ElFormItem label="登录名">
          <ElInput v-model="authForm.loginName" />
        </ElFormItem>
        <ElFormItem label="密码">
          <ElInput v-model="authForm.password" show-password type="password" />
        </ElFormItem>
        <template v-if="authMode === 'register'">
          <ElFormItem label="信用代码">
            <ElInput v-model="authForm.supplierCreditCode" />
          </ElFormItem>
          <ElFormItem label="联系电话">
            <ElInput v-model="authForm.contactPhone" />
          </ElFormItem>
        </template>
        <ElFormItem>
          <ElButton
            v-if="authMode === 'login'"
            :icon="Lock"
            :loading="authLoading"
            type="primary"
            @click="handlePortalLogin"
          >
            登录
          </ElButton>
          <ElButton
            v-else
            :icon="Plus"
            :loading="authLoading"
            type="primary"
            @click="handlePortalRegister"
          >
            注册
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="portal-card" shadow="never">
      <template #header>
        <span>供应商参与信息</span>
      </template>
      <ElForm label-width="120px">
        <ElFormItem label="项目ID">
          <ElInputNumber v-model="baseForm.projectId" :min="1" />
        </ElFormItem>
        <ElFormItem label="标段ID">
          <ElInputNumber v-model="baseForm.lotId" :min="1" />
        </ElFormItem>
        <ElFormItem label="联系人">
          <ElInput v-model="baseForm.contactName" />
        </ElFormItem>
        <ElFormItem label="联系电话">
          <ElInput v-model="baseForm.contactPhone" />
        </ElFormItem>
        <ElFormItem label="备注">
          <ElInput v-model="baseForm.remark" type="textarea" />
        </ElFormItem>
        <ElFormItem>
          <ElButton :icon="Plus" type="primary" @click="handleCreateRegistration">
            提交报名
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="portal-card" shadow="never">
      <template #header>
        <span>投标操作</span>
      </template>
      <ElForm label-width="120px">
        <ElFormItem label="报名ID">
          <ElInputNumber v-model="baseForm.registrationId" :min="1" />
        </ElFormItem>
        <ElFormItem label="投标ID">
          <ElInputNumber v-model="baseForm.submissionId" :min="1" />
        </ElFormItem>
        <ElFormItem label="版本号">
          <ElInputNumber v-model="baseForm.version" :min="0" />
        </ElFormItem>
        <ElFormItem label="报价金额">
          <ElInputNumber v-model="submitForm.priceAmount" :min="0" />
        </ElFormItem>
        <ElFormItem label="文件清单JSON">
          <ElInput v-model="submitForm.fileManifestJson" type="textarea" />
        </ElFormItem>
        <ElFormItem>
          <ElButton :icon="Plus" @click="handleCreateSubmission">
            创建投标记录
          </ElButton>
          <ElButton :icon="Check" type="success" @click="handleSubmitBid">
            提交投标
          </ElButton>
          <ElButton :icon="RefreshLeft" type="warning" @click="handleWithdrawBid">
            撤回投标
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>
  </section>
</template>

<style scoped>
.portal-auth-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.portal-auth-info__name {
  font-size: 16px;
  font-weight: 600;
}

.portal-auth-info__meta {
  margin-top: 4px;
  color: #667085;
}

.portal-auth-tabs {
  margin: 0 0 16px 120px;
}

@media (max-width: 640px) {
  .portal-auth-info {
    align-items: flex-start;
    flex-direction: column;
  }

  .portal-auth-tabs {
    margin-left: 0;
  }
}
</style>
