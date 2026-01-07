import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080/backend',
        changeOrigin: true
        // 移除rewrite配置，保持/api前缀
      },
      '/uploads': {
        target: 'http://localhost:8080/backend',
        changeOrigin: true
      },
      '/websocket': {
        target: 'ws://localhost:8080/backend',
        changeOrigin: true,
        ws: true
      }
    }
  },
  build: {
    sourcemap: true // 启用source map
  }
})