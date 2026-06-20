import { test, expect } from './fixtures/test-setup.js'

test.describe('登录模块', () => {

  test('显示登录页面并包含测试账号提示', async ({ page }) => {
    await page.goto('/login')
    await expect(page.locator('h2')).toContainText('凡栋超市后台管理')
    await expect(page.getByText('测试账号: admin')).toBeVisible()
  })

  test('空表单提交显示验证提示', async ({ page }) => {
    await page.goto('/login')
    await page.click('button:has-text("登录")')
    await expect(page.locator('.el-form-item__error')).toHaveCount(2)
  })

  test('登录失败显示错误消息', async ({ page }) => {
    await page.route('**/api/employee/login', route => route.fulfill({ json: { code: 0, msg: '用户名或密码错误' } }))
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', 'wrong')
    await page.click('button:has-text("登录")')
    await expect(page.locator('.el-message')).toContainText('用户名或密码错误')
  })

  test('成功登录后跳转工作台', async ({ page }) => {
    await page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 'mock-token', role: 'admin', name: '管理员' } } }))
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await expect(page.getByText('工作台').first()).toBeVisible()
  })

  test('退出登录', async ({ page }) => {
    await page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 't', role: 'admin', name: 'Admin' } } }))
    await page.route('**/api/order/conditionSearch**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } }))
    await page.route('**/api/admin/product/page**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } }))
    await page.route('**/api/employee/page**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } }))
    await page.route('**/api/order/statistics', route => route.fulfill({ json: { code: 1, data: {} } }))
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await page.waitForTimeout(2000)
    await page.getByRole('button', { name: '退出登录' }).click()
    await page.waitForURL('/login')
  })

  test('无token访问页面重定向到登录', async ({ page }) => {
    await page.goto('/dashboard')
    await page.waitForURL('/login')
  })
})
