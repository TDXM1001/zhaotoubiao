import process from 'node:process';
import { fileURLToPath, URL } from 'node:url';

import vue from '@vitejs/plugin-vue';
import ElementPlus from 'unplugin-element-plus/vite';
import { defineConfig, loadEnv } from 'vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');

  return {
    base: env.VITE_PORTAL_BASE_PATH || '/',
    plugins: [
      vue(),
      ElementPlus({
        format: 'esm',
      }),
    ],
    resolve: {
      alias: {
        '#': fileURLToPath(new URL('src', import.meta.url)),
      },
    },
    server: {
      proxy: {
        '/api': {
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
          target: 'http://localhost:1029',
          ws: true,
        },
      },
    },
  };
});
