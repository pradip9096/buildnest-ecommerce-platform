import type { Page } from '@playwright/test';

// #117: mirrors BaseApiTest.java's per-run-unique test-identity pattern (see
// lesson_mixed_git_tracking_status... / BaseApiTest#seedProduct precedent, #630) — a fresh,
// timestamped username/email per test run avoids colliding with data left behind by a prior run
// against the same shared backend/database.
export function uniqueUser() {
  const suffix = `${Date.now()}${Math.floor(Math.random() * 1000)}`;
  return {
    firstName: 'Playwright',
    lastName: 'Tester',
    username: `pw_user_${suffix}`,
    email: `pw_user_${suffix}@example.com`,
    password: 'PlaywrightTest#12345',
  };
}

export async function registerAndLogin(page: Page, user: ReturnType<typeof uniqueUser>) {
  await page.goto('/register');
  await page.getByTestId('register-firstName').fill(user.firstName);
  await page.getByTestId('register-lastName').fill(user.lastName);
  await page.getByTestId('register-username').fill(user.username);
  await page.getByTestId('register-email').fill(user.email);
  await page.getByTestId('register-password').fill(user.password);
  await page.getByTestId('register-confirmPassword').fill(user.password);
  await page.getByTestId('register-submit').click();
  // Registration redirects to /login (or auto-signs in, depending on backend behavior) —
  // wait for navigation away from /register rather than a fixed URL.
  await page.waitForURL(url => !url.pathname.startsWith('/register'), { timeout: 15_000 });

  if (page.url().includes('/login')) {
    await page.locator('[name="username"]').fill(user.username);
    await page.locator('[name="password"]').fill(user.password);
    await page.getByTestId('login-submit').click();
    await page.waitForURL(url => !url.pathname.startsWith('/login'), { timeout: 15_000 });
  }
}

export const VALID_ADDRESS = {
  fullName: 'Playwright Tester',
  line1: '221B Baker Street',
  line2: '',
  city: 'Mumbai',
  state: 'Maharashtra',
  postalCode: '400001',
  country: 'India',
  phone: '9876543210',
};

export async function fillAddressStep(page: Page) {
  await page.getByTestId('address-fullName').fill(VALID_ADDRESS.fullName);
  await page.getByTestId('address-line1').fill(VALID_ADDRESS.line1);
  await page.getByTestId('address-city').fill(VALID_ADDRESS.city);
  await page.getByTestId('address-state').fill(VALID_ADDRESS.state);
  await page.getByTestId('address-postalCode').fill(VALID_ADDRESS.postalCode);
  await page.getByTestId('address-country').fill(VALID_ADDRESS.country);
  await page.getByTestId('address-phone').fill(VALID_ADDRESS.phone);
  await page.getByRole('button', { name: /continue|next/i }).click();
}
