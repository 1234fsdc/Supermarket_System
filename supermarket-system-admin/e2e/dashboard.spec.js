import { test, expect } from './fixtures/test-setup.js'

test.describe('工作台', () => {

  function setupAllMocks(page) {
    return Promise.all([
      page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 't', role: 'admin', name: 'Admin' } } })),
      page.route('**/api/order/conditionSearch**', route => route.fulfill({ json: { code: 1, data: { total: 5, records: [
        { id: 1, number: 'N001', amount: '500.00', status: 5, orderTime: '2026-05-30 10:00:00' },
        { id: 2, number: 'N002', amount: '300.00', status: 5, orderTime: '2026-05-30 11:00:00' },
      ]}}})),
      page.route('**/api/order/statistics', route => route.fulfill({ json: { code: 1, data: { toBeConfirmed: 2, confirmed: 3, deliveryInProgress: 1 } } })),
      page.route('**/api/admin/product/page**', route => route.fulfill({ json: { code: 1, data: { total: 50, records: [] } } })),
      page.route('**/api/employee/page**', route => route.fulfill({ json: { code: 1, data: { total: 10, records: [] } } })),
    ])
  }

  test('工作台页面显示统计卡片', async ({ page }) => {
    await setupAllMocks(page)
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await expect(page.getByText('今日营业额').first()).toBeVisible()
    await expect(page.getByText('今日订单数').first()).toBeVisible()
    await expect(page.getByText('商品总数').first()).toBeVisible()
    await expect(page.getByText('员工总数').first()).toBeVisible()
  })
})
