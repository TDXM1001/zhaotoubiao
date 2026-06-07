<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type { SupportProtectApi } from '#/api';

import { onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  ElAlert,
  ElButton,
  ElCard,
  ElForm,
  ElFormItem,
  ElInputNumber,
  ElMessage,
  ElSpace,
  ElSwitch,
} from 'element-plus';

import {
  getLevel3ProtectConfigApi,
  updateLevel3ProtectConfigApi,
} from '#/api';

defineOptions({
  name: 'SupportLevel3ProtectConfigIndex',
});

const loading = ref(false);
const saving = ref(false);
const parseError = ref('');
const formRef = ref<FormInstance>();

const formModel = reactive<SupportProtectApi.Level3ProtectConfig>({
  fileDetectFlag: true,
  loginActiveTimeoutMinutes: 30,
  loginFailLockMinutes: 30,
  loginFailMaxTimes: 3,
  maxUploadFileSizeMb: 30,
  passwordComplexityEnabled: true,
  regularChangePasswordMonths: 3,
  regularChangePasswordNotAllowRepeatTimes: 3,
  twoFactorLoginEnabled: false,
});

const rules: FormRules<SupportProtectApi.Level3ProtectConfig> = {
  loginActiveTimeoutMinutes: [
    { message: '请输入最低活跃时间', required: true, trigger: 'change' },
  ],
  loginFailLockMinutes: [
    { message: '请输入登录失败锁定时间', required: true, trigger: 'change' },
  ],
  loginFailMaxTimes: [
    { message: '请输入连续登录失败次数', required: true, trigger: 'change' },
  ],
  maxUploadFileSizeMb: [
    { message: '请输入文件大小限制', required: true, trigger: 'change' },
  ],
  regularChangePasswordMonths: [
    { message: '请输入定期修改密码间隔', required: true, trigger: 'change' },
  ],
  regularChangePasswordNotAllowRepeatTimes: [
    { message: '请输入密码不可重复次数', required: true, trigger: 'change' },
  ],
};

function applyConfig(config: SupportProtectApi.Level3ProtectConfig) {
  Object.assign(formModel, config);
}

async function loadConfig() {
  loading.value = true;
  parseError.value = '';
  try {
    const configText = await getLevel3ProtectConfigApi();
    const config = JSON.parse(
      configText,
    ) as SupportProtectApi.Level3ProtectConfig;
    applyConfig(config);
  } catch (error) {
    parseError.value =
      error instanceof Error ? error.message : '三级等保配置解析失败';
    ElMessage.error('三级等保配置解析失败，请检查后端配置值');
  } finally {
    loading.value = false;
  }
}

async function handleSubmit() {
  if (parseError.value) {
    ElMessage.warning('当前配置解析失败，不能保存覆盖后端配置');
    return;
  }

  await formRef.value?.validate();

  saving.value = true;
  try {
    await updateLevel3ProtectConfigApi({ ...formModel });
    ElMessage.success('三级等保配置保存成功');
    await loadConfig();
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadConfig();
});
</script>

<template>
  <Page auto-content-height class="level3-page">
    <div class="level3-page__content">
      <!-- <div class="level3-page__alert-wrapper">
        <ElAlert :closable="false" type="info">
          <template #title>
            <div class="level3-page__alert-title">三级等保：</div>
          </template>
          <div class="level3-page__alert-content">
            <p>
              1.
              三级等保是中国国家等级保护认证中的最高级别认证，该认证包含了五个等级保护安全技术要求和五个安全管理要求，共涉及测评分类73类，要求非常严格。
            </p>
            <p>
              2.
              三级等保是地市级以上国家机关、重要企事业单位需要达成的认证，在金融行业中，可以看作是除了银行机构以外最高级别的信息安全等级保护。
            </p>
            <p>
              3. 具体三级等保要求，请查看“1024创新实验室”写的相关文档
              <a
                class="level3-page__link"
                href="https://1024lab.net"
                target="_blank"
              >
                三级等保文档
              </a>
            </p>
          </div>
        </ElAlert>
      </div> -->

      <ElCard class="level3-page__card" shadow="never">
        <div class="level3-page__header">三级等保配置</div>

        <ElAlert
          v-if="parseError"
          :closable="false"
          class="level3-page__alert"
          show-icon
          :title="`配置解析失败：${parseError}`"
          type="error"
        />

        <ElForm
          ref="formRef"
          v-loading="loading"
          :model="formModel"
          :rules="rules"
          class="level3-page__form"
          label-width="260px"
        >
          <ElFormItem label="配置双因子登录模式：">
            <ElSwitch
              v-model="formModel.twoFactorLoginEnabled"
              active-text="开启"
              inactive-text="关闭"
              inline-prompt
            />
            <div class="form-item-desc">
              在用户登录时，需要同时提供用户名和密码以及其他形式的身份验证信息，例如短信验证码等
            </div>
          </ElFormItem>

          <ElFormItem label="最大连续登录失败次数：" prop="loginFailMaxTimes">
            <ElInputNumber
              v-model="formModel.loginFailMaxTimes"
              :min="0"
              controls-position="right"
            />
            <span class="form-item-unit">次</span>
            <div class="form-item-desc">
              连续登录失败超过一定次数，则需要锁定；默认5次；0则不锁定；
            </div>
          </ElFormItem>

          <ElFormItem label="连续登录失败锁定分钟：" prop="loginFailLockMinutes">
            <ElInputNumber
              v-model="formModel.loginFailLockMinutes"
              :min="0"
              controls-position="right"
            />
            <span class="form-item-unit">分钟</span>
            <div class="form-item-desc">
              连续登录失败锁定的时间；默认30分钟，0则不锁定
            </div>
          </ElFormItem>

          <ElFormItem
            label="登录后无操作自动退出的分钟："
            prop="loginActiveTimeoutMinutes"
          >
            <ElInputNumber
              v-model="formModel.loginActiveTimeoutMinutes"
              :min="0"
              controls-position="right"
            />
            <span class="form-item-unit">分钟</span>
            <div class="form-item-desc">
              如：登录1小时没操作自动退出当前登录状态；默认30分钟
            </div>
          </ElFormItem>

          <ElFormItem label="开启密码复杂度：">
            <ElSwitch
              v-model="formModel.passwordComplexityEnabled"
              active-text="开启"
              inactive-text="关闭"
              inline-prompt
            />
            <div class="form-item-desc">
              开启后，密码必须包含大写字母、小写字母、数字和特殊字符中的三种
            </div>
          </ElFormItem>

          <ElFormItem label="定期修改密码间隔：" prop="regularChangePasswordMonths">
            <ElInputNumber
              v-model="formModel.regularChangePasswordMonths"
              :min="0"
              controls-position="right"
            />
            <span class="form-item-unit">月</span>
            <div class="form-item-desc">
              定期要求用户修改密码的间隔时间；默认3个月
            </div>
          </ElFormItem>

          <ElFormItem
            label="密码不可重复次数："
            prop="regularChangePasswordNotAllowRepeatTimes"
          >
            <ElInputNumber
              v-model="formModel.regularChangePasswordNotAllowRepeatTimes"
              :min="0"
              controls-position="right"
            />
            <span class="form-item-unit">次</span>
            <div class="form-item-desc">
              修改密码时，不允许与前几次密码重复；默认3次
            </div>
          </ElFormItem>

          <ElFormItem label="开启文件检测：">
            <ElSwitch
              v-model="formModel.fileDetectFlag"
              active-text="开启"
              inactive-text="关闭"
              inline-prompt
            />
            <div class="form-item-desc">
              上传文件时是否进行安全检测（如病毒扫描、文件头校验等）
            </div>
          </ElFormItem>

          <ElFormItem label="文件大小限制：" prop="maxUploadFileSizeMb">
            <ElInputNumber
              v-model="formModel.maxUploadFileSizeMb"
              :min="1"
              controls-position="right"
            />
            <span class="form-item-unit">MB</span>
            <div class="form-item-desc">限制上传文件的最大大小；默认30MB</div>
          </ElFormItem>
        </ElForm>

        <div class="level3-page__footer">
          <ElSpace>
            <ElButton :loading="loading" @click="loadConfig">重新加载</ElButton>
            <ElButton
              :disabled="Boolean(parseError)"
              :loading="saving"
              type="primary"
              @click="handleSubmit"
            >
              保存配置
            </ElButton>
          </ElSpace>
        </div>
      </ElCard>
    </div>
  </Page>
</template>

<style scoped>
.level3-page__content {
  height: 100%;
  /* padding: 20px; */
}

.level3-page__alert-wrapper {
  margin-bottom: 20px;
}

.level3-page__alert-title {
  margin-bottom: 8px;
  font-size: 16px;
  font-weight: bold;
}

.level3-page__alert-content {
  line-height: 1.6;
}

.level3-page__alert-content p {
  margin: 4px 0;
}

.level3-page__link {
  color: #409eff;
  text-decoration: none;
}

.level3-page__link:hover {
  text-decoration: underline;
}

.level3-page__card {
  min-height: 100%;
  border: none;
}

.level3-page__header {
  margin-bottom: 24px;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.level3-page__card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  padding: 16px 32px 16px;
}

.level3-page__form :deep(.el-input-number) {
  width: 180px;
}

.form-item-desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.4;
  color: #909399;
}

.form-item-unit {
  margin-left: 12px;
  color: #606266;
}

.level3-page__alert {
  margin-bottom: 24px;
}

.level3-page__footer {
  margin-top: 32px;
  padding-left: 260px;
}

@media (max-width: 720px) {
  .level3-page__footer {
    padding-left: 0;
  }
}
</style>
