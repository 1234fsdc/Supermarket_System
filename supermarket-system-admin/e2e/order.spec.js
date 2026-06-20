import { test, expect } from './fixtures/test-setup.js'

test.describe('订单管理', () => {

  function setupMocks(page) {
    return Promise.all([
      page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 't', role: 'admin', name: 'Admin' } } })),
      page.route('**/api/order/conditionSearch**', route => route.fulfill({ json: { code: 1, data: { total: 2, records: [
        { id: 101, number: '20260530001', userName: '张三', phone: '13800138001', address: '测试地址', amount: 88.50, status: 5, orderTime: '2026-05-30 10:00:00', remark: '' },
        { id: 102, number: '20260530002', userName: '李四', phone: '13800138002', address: '测试地址2', amount: 156.00, status: 2, orderTime: '2026-05-30 11:00:00', remark: '' },
      ]}}})),
      page.route('**/api/order/statistics', route => route.fulfill({ json: { code: 1, data: { toBeConfirmed: 1, confirmed: 2, deliveryInProgress: 0, completed: 2, cancelled: 0 } } })),
      page.route('**/api/admin/product/page**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } })),
      page.route('**/api/employee/page**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } })),
    ])
  }

  test('订单列表显示数据', async ({ page }) => {
    await setupMocks(page)
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await page.goto('/order/list')
    await expect(page.getByText('20260530001').first()).toBeVisible()
    await expect(page.getByText('20260530002').first()).toBeVisible()
  })
})
