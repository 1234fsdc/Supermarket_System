import { test, expect } from './fixtures/test-setup.js'

test.describe('数据统计', () => {

  test('统计页面显示统计卡片', async ({ page }) => {
    await page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 't', role: 'admin', name: 'Admin' } } }))
    await page.route('**/api/order/conditionSearch**', route => route.fulfill({
      json: { code: 1, data: { total: 2, records: [
        { id: 1, number: 'N001', amount: '500.00', status: 5 },
        { id: 2, number: 'N002', amount: '300.00', status: 5 },
      ]}}
    }))
    await page.route('**/api/order/statistics', route => route.fulfill({
      json: { code: 1, data: { totalCount: 200, pendingCount: 15, confirmedCount: 8, deliveredCount: 12, completedCount: 158, cancelledCount: 7 } }
    }))
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await page.goto('/statistics/overview')
    await expect(page.locator('.stat-item').first()).toBeVisible()
  })
})
