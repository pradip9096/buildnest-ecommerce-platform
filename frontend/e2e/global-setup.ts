import { chromium, type FullConfig } from '@playwright/test';
import { uniqueUser, registerAndLogin } from './fixtures';

// #117: register + log in exactly once for the whole suite and reuse the resulting session via
// storageState, instead of every test independently calling registerAndLogin. Each register+login
// pair costs 2 requests against RateLimitHeaderInterceptor's hardcoded AUTH_LIMIT (5 requests per
// window, not property-configurable — see spring/rate-limiting.md) on any /api/auth/** path; 3
// tests each doing their own registerAndLogin (6 requests) exceeded that limit and caused a real,
// reproducible failure once Redis (and therefore real rate limiting) was actually available in CI.
export default async function globalSetup(config: FullConfig) {
  const baseURL = config.projects[0]?.use?.baseURL ?? 'http://localhost:4173';
  const browser = await chromium.launch();
  const page = await browser.newPage({ baseURL });

  const user = uniqueUser();
  await registerAndLogin(page, user);
  await page.context().storageState({ path: 'e2e/.auth/shared-user.json' });

  await browser.close();
}
