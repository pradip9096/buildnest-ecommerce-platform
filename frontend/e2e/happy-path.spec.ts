import { test, expect, type Page } from '@playwright/test';
import { uniqueUser, registerAndLogin, fillAddressStep } from './fixtures';

// #117: full user-journey E2E — register -> browse -> search -> add-to-cart -> checkout ->
// order confirmation -> order history. Mirrors the scenario coverage of the existing Selenium
// suite (backend/src/test/java/.../e2e/E2ETest.java, TC-E2E-001..007) but owned by the
// frontend stack per Playwright's industry-standard status (see #117's PR description /
// CHANGELOG for the migration rationale) instead of a JVM-hosted browser test.
//
// Split into two tests sharing one browser context (test.describe.serial + a describe-scoped
// page) rather than one long test: register->browse->search->add-to-cart->cart passes reliably
// in CI; checkout->confirmation->order-history was blocked by a checkout gap (#652, root-caused
// and fixed: the CI job's ddl-auto=create-drop wiped the Liquibase-seeded default shipping
// method after Liquibase ran but before E2ESeedDataRunner executed, leaving zero active shipping
// methods and a permanently-disabled "Continue to Payment" button — not a district/shipping-
// method matching issue). Kept as two tests since the split still preserves the reliable prefix
// as independently asserted coverage.
test.describe.serial('Full user journey', () => {
  let page: Page;

  test.beforeAll(async ({ browser }) => {
    page = await browser.newPage();
  });

  test.afterAll(async () => {
    await page.close();
  });

  test('register, browse, search, and add to cart', async () => {
    const user = uniqueUser();

    await test.step('register and sign in', async () => {
      await registerAndLogin(page, user);
    });

    await test.step('browse product catalog', async () => {
      await page.goto('/products');
      await expect(page.getByTestId('product-grid')).toBeVisible();
      await expect(page.getByTestId('product-grid').locator('a').first()).toBeVisible({ timeout: 15_000 });
    });

    await test.step('search for a product', async () => {
      await page.getByTestId('navbar-search-input').fill('a');
      await page.getByTestId('navbar-search-input').press('Enter');
      await page.waitForURL(/\/products\?.*search=/);
      await expect(page.getByTestId('product-grid')).toBeVisible();
    });

    await test.step('open product detail and add to cart', async () => {
      await page.getByTestId('product-grid').locator('a').first().click();
      await page.waitForURL(/\/products\/\d+/);
      const addToCartButton = page.getByTestId('product-detail-add-to-cart');
      await expect(addToCartButton).toBeEnabled({ timeout: 15_000 });
      await addToCartButton.click();
      await expect(page.getByText(/^added \d+ item/i)).toBeVisible({ timeout: 10_000 });
    });

    await test.step('go to cart and proceed to checkout', async () => {
      await page.goto('/cart');
      await expect(page.getByTestId('order-summary')).toBeVisible();
      await page.getByTestId('checkout-button').click();
      await page.waitForURL('/checkout');
    });
  });

  test('checkout, order confirmation, and order history', async () => {
    await test.step('complete checkout: address, shipping, payment', async () => {
      await fillAddressStep(page);

      await expect(page.getByRole('button', { name: /continue to payment/i })).toBeEnabled({ timeout: 15_000 });
      await page.getByRole('button', { name: /continue to payment/i }).click();

      const payButton = page.getByRole('button', { name: /^pay ₹/i });
      await expect(payButton).toBeVisible({ timeout: 15_000 });
      await payButton.click();
    });

    await test.step('order confirmation is shown', async () => {
      await page.waitForURL(/\/orders\/\d+/, { timeout: 20_000 });
      await expect(page.getByRole('heading', { name: /order placed successfully/i })).toBeVisible();
    });

    await test.step('order appears in order history', async () => {
      await page.goto('/account');
      await page.getByRole('button', { name: /orders/i }).click();
      await expect(page.getByText(/order #/i).first()).toBeVisible({ timeout: 15_000 });
    });
  });
});
