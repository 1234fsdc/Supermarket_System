import { test, expect } from './fixtures/test-setup.js'

test.describe('店铺状态', () => {

  test('显示当前店铺状态', async ({ page }) => {
    await page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 't', role: 'admin', name: 'Admin' } } }))
    await page.route('**/api/order/conditionSearch**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } }))
    await page.route('**/api/shop/status', route => route.fulfill({ json: { code: 1, data: 1 } }))
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await page.goto('/shop/status')
    await expect(page.getByText('营业中').first()).toBeVisible()
  })
})
