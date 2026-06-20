import { test, expect } from './fixtures/test-setup.js'

test.describe('优惠券管理', () => {

  test('优惠券列表显示数据', async ({ page }) => {
    await page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 't', role: 'admin', name: 'Admin' } } }))
    await page.route('**/api/order/conditionSearch**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } }))
    await page.route('**/api/admin/coupon/template/page**', route => route.fulfill({
      json: { code: 1, data: { total: 2, records: [
        { id: 1, name: '满100减20', type: 1, discountType: 1, discountValue: 20.00, minSpend: 100.00, totalCount: 500, status: 1, validDays: 30 },
        { id: 2, name: '全场8折', type: 1, discountType: 2, discountValue: 20.00, minSpend: 0, totalCount: 1000, status: 0, validDays: 7 }
      ]}}
    }))
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await page.goto('/coupon/list')
    await expect(page.getByText('满100减20').first()).toBeVisible()
    await expect(page.getByText('全场8折').first()).toBeVisible()
  })
})
