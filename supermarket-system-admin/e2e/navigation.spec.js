import { test, expect } from './fixtures/test-setup.js'

test.describe('导航与权限', () => {

  test('admin角色看到所有菜单项', async ({ page }) => {
    await page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 't', role: 'admin', name: 'Admin' } } }))
    await page.route('**/api/order/conditionSearch**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } }))
    await page.route('**/api/order/statistics', route => route.fulfill({ json: { code: 1, data: {} } }))
    await page.route('**/api/admin/product/page**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } }))
    await page.route('**/api/employee/page**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } }))
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    const items = ['工作台', '数据统计', '订单管理', '商品管理', '分类管理', '员工管理', '店铺状态']
    for (const item of items) {
      await expect(page.locator('.menu-text').getByText(item).first()).toBeAttached()
    }
  })

  test('侧边栏菜单点击跳转正确页面', async ({ page }) => {
    await page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 't', role: 'admin', name: 'Admin' } } }))
    await page.route('**/api/order/conditionSearch**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } }))
    await page.route('**/api/order/statistics', route => route.fulfill({ json: { code: 1, data: {} } }))
    await page.route('**/api/admin/product/page**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } }))
    await page.route('**/api/employee/page**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } }))
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)

    await page.locator('.menu-text').getByText('订单管理').first().click()
    await page.waitForURL(/\/order/)
    await page.locator('.menu-text').getByText('工作台').first().click()
    await page.waitForURL(/\/dashboard/)
  })

  test('面包屑导航显示当前页面标题', async ({ page }) => {
    await page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 't', role: 'admin', name: 'Admin' } } }))
    await page.route('**/api/order/conditionSearch**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } }))
    await page.route('**/api/order/statistics', route => route.fulfill({ json: { code: 1, data: {} } }))
    await page.route('**/api/admin/product/page**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } }))
    await page.route('**/api/employee/page**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } }))
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await page.locator('.menu-text').getByText('商品管理').first().click()
    await page.waitForURL(/\/product/)
    await expect(page.locator('.el-breadcrumb')).toContainText('商品管理')
  })
})
