import { requestClient } from '#/api/request';

export namespace SupportApiEncryptApi {
  /** 接口加解密测试业务载荷 */
  export interface JweForm {
    age: number;
    name: string;
  }

  /** 后端 @ApiDecrypt 接口要求的加密请求体 */
  export interface EncryptRequest {
    encryptData: string;
  }
}

/** 测试请求加密 */
export async function testRequestEncryptApi(encryptData: string) {
  return requestClient.post<SupportApiEncryptApi.JweForm>(
    '/support/apiEncrypt/testRequestEncrypt',
    { encryptData },
  );
}

/** 测试响应加密 */
export async function testResponseEncryptApi(
  data: SupportApiEncryptApi.JweForm,
) {
  return requestClient.post<string>('/support/apiEncrypt/testResponseEncrypt', data);
}

/** 测试请求和响应双向加密 */
export async function testDecryptAndEncryptApi(encryptData: string) {
  return requestClient.post<string>('/support/apiEncrypt/testDecryptAndEncrypt', {
    encryptData,
  });
}

/** 测试数组加解密 */
export async function testEncryptArrayApi(encryptData: string) {
  return requestClient.post<string>('/support/apiEncrypt/testArray', {
    encryptData,
  });
}
