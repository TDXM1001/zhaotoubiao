import { sm4 } from 'sm-crypto';

const SMART_ADMIN_SM4_KEY = Array.from(
  new TextEncoder().encode('1024lab__1024lab'),
);

function base64Encode(text: string) {
  return globalThis.btoa(text);
}

function base64Decode(text: string) {
  return globalThis.atob(text);
}

/** 将明文密码转换为 SmartAdmin 后端可解密的密文。 */
function encryptSmartAdminPassword(password: string) {
  const encryptedHex = sm4.encrypt(password, SMART_ADMIN_SM4_KEY) as string;
  return base64Encode(encryptedHex);
}

/** 加密接口请求体，格式需匹配后端 ApiEncryptForm.encryptData。 */
function encryptSmartAdminPayload(data: unknown) {
  const text = typeof data === 'string' ? data : JSON.stringify(data);
  const encryptedHex = sm4.encrypt(text, SMART_ADMIN_SM4_KEY) as string;
  return base64Encode(encryptedHex);
}

/** 解密后端 @ApiEncrypt 返回的 ResponseDTO.data 密文。 */
function decryptSmartAdminPayload<T = unknown>(payload: string): T {
  const encryptedHex = base64Decode(payload);
  const decryptedText = sm4.decrypt(
    encryptedHex,
    SMART_ADMIN_SM4_KEY,
  ) as string;

  return JSON.parse(decryptedText) as T;
}

export {
  decryptSmartAdminPayload,
  encryptSmartAdminPassword,
  encryptSmartAdminPayload,
};
