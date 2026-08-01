import { defineConfig, devices } from '@playwright/test';

// #117: baseURL points at the built SPA served via `vite preview` in CI (see
// .github/workflows/ci-cd-pipeline.yml's `playwright-e2e` job), matching the pattern already
// used by the Selenium suite (E2ETest.java) — both drive a real built frontend, not the dev
// server, against a real backend instance.
const baseURL = process.env.PLAYWRIGHT_BASE_URL ?? 'http://localhost:4173';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false, // #117: tests share one seeded backend/DB state — run serially per file
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: process.env.CI ? [['html', { open: 'never' }], ['github']] : 'list',
  timeout: 30_000,
  use: {
    baseURL,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
