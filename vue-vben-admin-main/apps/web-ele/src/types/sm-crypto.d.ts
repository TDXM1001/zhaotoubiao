declare module 'sm-crypto' {
  export const sm4: {
    decrypt(
      data: number[] | string,
      key: number[] | string,
      options?: {
        iv?: number[] | string;
        mode?: 'cbc';
        output?: 'array';
        padding?: 'none' | 'pkcs#5' | 'pkcs#7';
      },
    ): number[] | string;
    encrypt(
      data: number[] | string,
      key: number[] | string,
      options?: {
        iv?: number[] | string;
        mode?: 'cbc';
        output?: 'array';
        padding?: 'none' | 'pkcs#5' | 'pkcs#7';
      },
    ): number[] | string;
  };
}
