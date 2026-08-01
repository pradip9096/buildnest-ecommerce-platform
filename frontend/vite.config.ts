/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  preview: {
    proxy: {
      '/api': {
        target: process.env.VITE_E2E_BACKEND_URL ?? 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    css: false,
    // #117: Vitest's default include glob (**/*.{test,spec}.ts) picks up frontend/e2e/*.spec.ts
    // (Playwright test files) by default, and Playwright's own test.describe() throws when
    // invoked outside Playwright's runner ("did not expect test.describe() to be called here")
    // -- exclude the e2e directory explicitly, on top of Vitest's own default excludes.
    exclude: ['**/node_modules/**', '**/dist/**', 'e2e/**'],
  },
})
