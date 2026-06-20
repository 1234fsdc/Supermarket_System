import { test, expect } from './fixtures/test-setup.js'

test.describe('员工管理 (仅admin)', () => {

  function setupMocks(page) {
    return Promise.all([
      page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 't', role: 'admin', name: 'Admin' } } })),
      page.route('**/api/order/conditionSearch**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } })),
      page.route('**/api/order/statistics', route => route.fulfill({ json: { code: 1, data: {} } })),
      page.route('**/api/admin/product/page**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } })),
      page.route('**/api/employee/page**', route => route.fulfill({ json: { code: 1, data: { total: 2, records: [
        { id: 1, username: 'admin', name: '管理员', phone: '13800000001', sex: '1', idNumber: '110101199001011234', status: 1, role: 'admin' },
        { id: 2, username: 'clerk', name: '收银员', phone: '13800000002', sex: '0', idNumber: '110101199001011235', status: 1, role: 'clerk' },
      ]}}})),
    ])
  }

  test('员工列表显示数据', async ({ page }) => {
    await setupMocks(page)
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await page.goto('/employee/list')
    await expect(page.getByText('管理员').first()).toBeVisible()
    await expect(page.getByText('收银员').first()).toBeVisible()
  })

  test('启用/禁用员工', async ({ page }) => {
    await setupMocks(page)
    let statusUpdated = false
    await page.route('**/api/employee/status/**', route => {
      statusUpdated = true
      route.fulfill({ json: { code: 1, msg: '操作成功' } })
    })
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await page.goto('/employee/list')
    await page.waitForTimeout(1000)
    const sw = page.locator('.el-switch').first()
    if (await sw.isVisible()) {
      await sw.click()
      await page.waitForTimeout(500)
      await expect(statusUpdated).toBeTruthy()
    }
  })
})
