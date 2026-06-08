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
  ElAlert,
  ElButton,
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElStep,
  ElSteps,
  ElTag,
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
import {
  parseCreatedRecordId,
  parseRouteNumber,
} from '#/utils/bid-helper';

defineOptions({ name: 'PortalSubmissionForm' });

const route = useRoute();
const authMode = ref<'login' | 'register'>('login');
const authLoading = ref(false);
const registrationLoading = ref(false);
const submissionLoading = ref(false);
const submitLoading = ref(false);
const withdrawLoading = ref(false);
const bidSubmitted = ref(false);
const portalAuth = ref<BidPortalApi.PortalAuthInfo | null>(null);

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
  registrationType: 'SELF_REGISTER',
  remark: '',
  submissionId: undefined as number | undefined,
  version: undefined as number | undefined,
});

const submitForm = reactive({
  fileManifestJson: '',
  priceAmount: undefined as number | undefined,
});

const isPortalLoggedIn = computed(() => Boolean(portalAuth.value));

const activeStep = computed(() => {
  if (!isPortalLoggedIn.value) {
    return 0;
  }
  if (!baseForm.registrationId) {
    return 1;
  }
  if (!baseForm.submissionId) {
    return 2;
  }
  return bidSubmitted.value ? 4 : 3;
});

const registrationStatusType = computed(() =>
  baseForm.registrationId ? 'success' : 'info',
);

const submissionStatusType = computed(() => {
  if (bidSubmitted.value) {
    return 'success';
  }
  return baseForm.submissionId ? 'warning' : 'info';
});

const submissionStatusText = computed(() => {
  if (bidSubmitted.value) {
    return '已提交';
  }
  return baseForm.submissionId ? '待提交' : '未创建';
});

function ensurePortalLogin() {
  if (isPortalLoggedIn.value) {
    return true;
  }
  ElMessage.warning('请先登录供应商门户账号');
  return false;
}

function ensureProjectAndLot() {
  if (!baseForm.projectId || !baseForm.lotId) {
    ElMessage.warning('请先填写项目ID和标段ID');
    return false;
  }
  return true;
}

function syncSupplierContact(authInfo: BidPortalApi.PortalAuthInfo) {
  if (!baseForm.contactName && authInfo.contactName) {
    baseForm.contactName = authInfo.contactName;
  }
  if (!baseForm.contactPhone && authInfo.contactPhone) {
    baseForm.contactPhone = authInfo.contactPhone;
  }
}

function bumpVersion() {
  baseForm.version = (baseForm.version ?? 0) + 1;
}

function absorbHandledRequestError() {
  // 请求层已统一提示错误，事件处理器只负责避免 Vue 记录未处理异常。
}

async function loadCurrentSupplier() {
  if (!getBidPortalAccessToken()) {
    portalAuth.value = null;
    return;
  }
  try {
    const currentSupplier = await getBidPortalCurrentSupplierApi();
    portalAuth.value = currentSupplier;
    syncSupplierContact(currentSupplier);
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
    const loginResult = await loginBidPortalAccountApi({
      loginName: authForm.loginName.trim(),
      password: authForm.password,
    });
    portalAuth.value = loginResult;
    syncSupplierContact(loginResult);
    ElMessage.success('登录成功');
  } catch {
    absorbHandledRequestError();
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
    const registerResult = await registerBidPortalAccountApi({
      contactPhone: authForm.contactPhone.trim(),
      loginName: authForm.loginName.trim(),
      password: authForm.password,
      supplierCreditCode: authForm.supplierCreditCode.trim(),
    });
    portalAuth.value = registerResult;
    syncSupplierContact(registerResult);
    ElMessage.success('注册成功');
  } catch {
    absorbHandledRequestError();
  } finally {
    authLoading.value = false;
  }
}

async function handlePortalLogout() {
  try {
    await logoutBidPortalAccountApi();
    ElMessage.success('已退出');
  } catch {
    absorbHandledRequestError();
  } finally {
    portalAuth.value = null;
    bidSubmitted.value = false;
  }
}

async function handleCreateRegistration() {
  if (!ensurePortalLogin() || !ensureProjectAndLot()) {
    return;
  }
  registrationLoading.value = true;
  try {
    const createdId = parseCreatedRecordId(
      await createBidPortalRegistrationApi({
        contactName: baseForm.contactName,
        contactPhone: baseForm.contactPhone,
        lotId: baseForm.lotId,
        projectId: baseForm.projectId,
        registrationType: baseForm.registrationType,
        remark: baseForm.remark,
      }),
    );
    if (createdId) {
      baseForm.registrationId = createdId;
    }
    ElMessage.success(
      createdId ? `报名已提交，报名ID ${createdId}` : '报名已提交',
    );
  } catch {
    absorbHandledRequestError();
  } finally {
    registrationLoading.value = false;
  }
}

async function handleCreateSubmission() {
  if (!ensurePortalLogin() || !ensureProjectAndLot()) {
    return;
  }
  if (!baseForm.registrationId) {
    ElMessage.warning('请先提交报名并等待后台审核通过');
    return;
  }
  submissionLoading.value = true;
  try {
    const createdId = parseCreatedRecordId(
      await createBidPortalSubmissionApi({
        lotId: baseForm.lotId,
        projectId: baseForm.projectId,
        registrationId: baseForm.registrationId,
        remark: baseForm.remark,
      }),
    );
    if (createdId) {
      baseForm.submissionId = createdId;
      baseForm.version = 0;
      bidSubmitted.value = false;
    }
    ElMessage.success(
      createdId ? `投标记录已创建，投标ID ${createdId}` : '投标记录已创建',
    );
  } catch {
    absorbHandledRequestError();
  } finally {
    submissionLoading.value = false;
  }
}

async function handleSubmitBid() {
  if (!ensurePortalLogin()) {
    return;
  }
  if (!baseForm.submissionId || baseForm.version === undefined) {
    ElMessage.warning('请先创建投标记录');
    return;
  }
  submitLoading.value = true;
  try {
    await submitBidPortalSubmissionApi({
      contactName: baseForm.contactName,
      contactPhone: baseForm.contactPhone,
      fileManifestJson: submitForm.fileManifestJson,
      priceAmount: submitForm.priceAmount,
      remark: baseForm.remark,
      submissionId: baseForm.submissionId,
      version: baseForm.version,
    });
    bumpVersion();
    bidSubmitted.value = true;
    ElMessage.success('投标已提交');
  } catch {
    absorbHandledRequestError();
  } finally {
    submitLoading.value = false;
  }
}

async function handleWithdrawBid() {
  if (!ensurePortalLogin()) {
    return;
  }
  if (!baseForm.submissionId || baseForm.version === undefined) {
    ElMessage.warning('请先创建投标记录');
    return;
  }
  withdrawLoading.value = true;
  try {
    await withdrawBidPortalSubmissionApi({
      remark: baseForm.remark,
      submissionId: baseForm.submissionId,
      version: baseForm.version,
    });
    bumpVersion();
    bidSubmitted.value = false;
    ElMessage.success('投标已撤回');
  } catch {
    absorbHandledRequestError();
  } finally {
    withdrawLoading.value = false;
  }
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
        <p class="portal-page__subtitle">供应商报名、资格确认、投标提交按流程办理。</p>
      </div>
    </div>

    <ElCard class="portal-card portal-flow-card" shadow="never">
      <ElSteps :active="activeStep" finish-status="success">
        <ElStep title="账号确认" />
        <ElStep title="提交报名" />
        <ElStep title="创建投标" />
        <ElStep title="提交投标" />
      </ElSteps>
    </ElCard>

    <div class="portal-process-grid">
      <ElCard class="portal-card" shadow="never">
        <template #header>
          <span>门户账号</span>
        </template>
        <div v-if="portalAuth" class="portal-auth-info">
          <div>
            <div class="portal-auth-info__name">
              {{ portalAuth.supplierName }}
            </div>
            <div class="portal-auth-info__meta">
              {{ portalAuth.supplierCreditCode }} / {{ portalAuth.loginName }}
            </div>
          </div>
          <ElButton :icon="SwitchButton" @click="handlePortalLogout">
            退出
          </ElButton>
        </div>
        <ElForm v-else label-width="104px">
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
          <span>流程状态</span>
        </template>
        <ElDescriptions :column="1" border size="small">
          <ElDescriptionsItem label="报名">
            <ElTag :type="registrationStatusType">
              {{ baseForm.registrationId ? '已提交' : '未提交' }}
            </ElTag>
            <span v-if="baseForm.registrationId" class="portal-inline-id">
              ID {{ baseForm.registrationId }}
            </span>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="投标">
            <ElTag :type="submissionStatusType">
              {{ submissionStatusText }}
            </ElTag>
            <span v-if="baseForm.submissionId" class="portal-inline-id">
              ID {{ baseForm.submissionId }} / 版本 {{ baseForm.version }}
            </span>
          </ElDescriptionsItem>
        </ElDescriptions>
        <ElAlert
          class="portal-flow-alert"
          :closable="false"
          show-icon
          title="报名提交后需管理端审核为资格通过，才能创建投标记录。"
          type="info"
        />
      </ElCard>
    </div>

    <ElCard class="portal-card" shadow="never">
      <template #header>
        <span>报名信息</span>
      </template>
      <ElForm label-width="104px">
        <div class="portal-form-grid">
          <ElFormItem label="项目ID">
            <ElInputNumber
              v-model="baseForm.projectId"
              class="portal-number-input"
              :min="1"
            />
          </ElFormItem>
          <ElFormItem label="标段ID">
            <ElInputNumber
              v-model="baseForm.lotId"
              class="portal-number-input"
              :min="1"
            />
          </ElFormItem>
          <ElFormItem label="联系人">
            <ElInput v-model="baseForm.contactName" />
          </ElFormItem>
          <ElFormItem label="联系电话">
            <ElInput v-model="baseForm.contactPhone" />
          </ElFormItem>
        </div>
        <ElFormItem label="备注">
          <ElInput v-model="baseForm.remark" type="textarea" />
        </ElFormItem>
        <ElFormItem>
          <ElButton
            :disabled="!isPortalLoggedIn"
            :icon="Plus"
            :loading="registrationLoading"
            type="primary"
            @click="handleCreateRegistration"
          >
            提交报名
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="portal-card" shadow="never">
      <template #header>
        <span>投标信息</span>
      </template>
      <ElForm label-width="104px">
        <div class="portal-form-grid">
          <ElFormItem label="报名ID">
            <ElInputNumber
              v-model="baseForm.registrationId"
              class="portal-number-input"
              :min="1"
            />
          </ElFormItem>
          <ElFormItem label="投标ID">
            <ElInputNumber
              v-model="baseForm.submissionId"
              class="portal-number-input"
              :min="1"
            />
          </ElFormItem>
          <ElFormItem label="版本号">
            <ElInputNumber
              v-model="baseForm.version"
              class="portal-number-input"
              :min="0"
            />
          </ElFormItem>
          <ElFormItem label="报价金额">
            <ElInputNumber
              v-model="submitForm.priceAmount"
              class="portal-number-input"
              :min="0"
            />
          </ElFormItem>
        </div>
        <ElFormItem label="文件清单">
          <ElInput
            v-model="submitForm.fileManifestJson"
            :autosize="{ minRows: 3, maxRows: 6 }"
            type="textarea"
          />
        </ElFormItem>
        <ElFormItem>
          <div class="portal-action-row">
            <ElButton
              :disabled="!isPortalLoggedIn || !baseForm.registrationId"
              :icon="Plus"
              :loading="submissionLoading"
              @click="handleCreateSubmission"
            >
              创建投标记录
            </ElButton>
            <ElButton
              :disabled="!isPortalLoggedIn || !baseForm.submissionId"
              :icon="Check"
              :loading="submitLoading"
              type="success"
              @click="handleSubmitBid"
            >
              提交投标
            </ElButton>
            <ElButton
              :disabled="!isPortalLoggedIn || !baseForm.submissionId"
              :icon="RefreshLeft"
              :loading="withdrawLoading"
              type="warning"
              @click="handleWithdrawBid"
            >
              撤回投标
            </ElButton>
          </div>
        </ElFormItem>
      </ElForm>
    </ElCard>
  </section>
</template>

<style scoped>
.portal-flow-card :deep(.el-card__body) {
  padding: 20px 24px;
}

.portal-process-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 0.8fr);
  gap: 16px;
}

.portal-auth-info {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
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
  margin: 0 0 16px 104px;
}

.portal-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 16px;
}

.portal-number-input {
  width: 100%;
}

.portal-inline-id {
  margin-left: 8px;
  color: #667085;
}

.portal-flow-alert {
  margin-top: 16px;
}

.portal-action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.portal-action-row :deep(.el-button + .el-button) {
  margin-left: 0;
}

@media (max-width: 760px) {
  .portal-flow-card :deep(.el-card__body) {
    padding: 16px;
  }

  .portal-process-grid,
  .portal-form-grid {
    grid-template-columns: 1fr;
  }

  .portal-auth-info {
    flex-direction: column;
    align-items: flex-start;
  }

  .portal-auth-tabs {
    margin-left: 0;
  }
}
</style>
