import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

const trimTrailingSlash = (value) => value.replace(/\/+$/, '')

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const backendEnv = (env.VITE_BACKEND_URL || env.VITE_API_BASE_URL || 'http://localhost:8080').trim()
  const normalizedBackend = trimTrailingSlash(backendEnv)
  const backendTarget = normalizedBackend.endsWith('/api')
    ? normalizedBackend.slice(0, -4)
    : normalizedBackend

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 3000,
      proxy: {
        '/api': {
          target: backendTarget || 'http://localhost:8080',
          changeOrigin: true
        },
        '/uploads': {
          target: backendTarget || 'http://localhost:8080',
          changeOrigin: true
        }
      }
    }
  }
})



