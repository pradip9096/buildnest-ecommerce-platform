import { test, expect } from '@playwright/test';
import { uniqueUser, registerAndLogin, fillAddressStep } from './fixtures';

// #117: critical error-path coverage — out-of-stock products cannot be added to cart, and a
// payment/order-confirmation failure surfaces an error instead of silently succeeding. Both
// scenarios are driven via real API responses rather than seeded backend state, since neither
// an out-of-stock product nor a forced Razorpay failure can be reliably reproduced against a
// shared, already-seeded backend without a dedicated test fixture — request interception
// keeps the scenario deterministic and isolated from other tests' data.
test.describe('Critical error paths', () => {
  test('out-of-stock product cannot be added to cart', async ({ page }) => {
    const user = uniqueUser();
    await registerAndLogin(page, user);

    await page.goto('/products');
    await expect(page.getByTestId('product-grid').locator('a').first()).toBeVisible({ timeout: 15_000 });
    const firstProductHref = await page.getByTestId('product-grid').locator('a').first().getAttribute('href');
    const productId = firstProductHref?.match(/\/products\/(\d+)/)?.[1];
    expect(productId).toBeTruthy();

    await page.route(`**/api/public/products/${productId}`, async route => {
      const response = await route.fetch();
      const body = await response.json();
      await route.fulfill({
        response,
        json: { ...body, stockQuantity: 0 },
      });
    });

    await page.goto(`/products/${productId}`);
    await expect(page.getByTestId('product-detail-add-to-cart')).toHaveCount(0);
    await expect(page.getByText(/out of stock/i)).toBeVisible({ timeout: 15_000 });
  });

  test('a failed order confirmation surfaces an error instead of navigating away', async ({ page }) => {
    const user = uniqueUser();
    await registerAndLogin(page, user);

    await page.goto('/products');
    await expect(page.getByTestId('product-grid').locator('a').first()).toBeVisible({ timeout: 15_000 });
    await page.getByTestId('product-grid').locator('a').first().click();
    await page.waitForURL(/\/products\/\d+/);
    const addToCartButton = page.getByTestId('product-detail-add-to-cart');
    await expect(addToCartButton).toBeEnabled({ timeout: 15_000 });
    await addToCartButton.click();
    await expect(page.getByText(/^added \d+ item/i)).toBeVisible({ timeout: 10_000 });

    await page.goto('/cart');
    await page.getByTestId('checkout-button').click();
    await page.waitForURL('/checkout');
    await fillAddressStep(page);

    await expect(page.getByRole('button', { name: /continue to payment/i })).toBeEnabled({ timeout: 15_000 });
    await page.getByRole('button', { name: /continue to payment/i }).click();

    // Simulate a payment-gateway failure: the real Razorpay confirmation call fails server-side
    // (declined card, gateway timeout, etc.) — the frontend must surface the error and stay on
    // the payment step, not silently navigate to an order-confirmation page for an order that
    // was never actually placed.
    await page.route('**/api/v1/checkout/confirm', route =>
      route.fulfill({
        status: 402,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Payment declined by gateway' }),
      })
    );

    const payButton = page.getByRole('button', { name: /^pay ₹/i });
    await expect(payButton).toBeVisible({ timeout: 15_000 });
    await payButton.click();

    await expect(page.getByText(/failed to confirm order/i)).toBeVisible({ timeout: 15_000 });
    expect(page.url()).not.toMatch(/\/orders\/\d+/);
  });
});
