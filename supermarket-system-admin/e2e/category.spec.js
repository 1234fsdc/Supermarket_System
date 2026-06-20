import { test, expect } from './fixtures/test-setup.js'

test.describe('分类管理', () => {

  test('分类列表显示数据', async ({ page }) => {
    await page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 't', role: 'admin', name: 'Admin' } } }))
    await page.route('**/api/category/page**', route => route.fulfill({ json: { code: 1, data: { total: 2, records: [{ id: 1, name: '饮品', type: 1, sort: 1, status: 1, createTime: '2026-01-01', updateTime: '2026-01-01' }, { id: 2, name: '零食', type: 1, sort: 2, status: 1, createTime: '2026-01-01', updateTime: '2026-01-01' }] } } }))
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await page.goto('/category/list')
    await expect(page.getByText('饮品').first()).toBeVisible()
    await expect(page.getByText('零食').first()).toBeVisible()
  })

  test('编辑分类', async ({ page }) => {
    await page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 't', role: 'admin', name: 'Admin' } } }))
    await page.route('**/api/category/page**', route => route.fulfill({ json: { code: 1, data: { total: 1, records: [{ id: 1, name: '饮品', type: 1, sort: 1, status: 1 }] } } }))
    let updated = false
    await page.route('**/api/category', route => {
      if (route.request().method() === 'PUT') updated = true
      route.fulfill({ json: { code: 1, msg: '修改成功' } })
    })
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await page.goto('/category/list')
    await page.locator('.el-button--primary').filter({ hasText: '编辑' }).first().click()
    await page.locator('.el-dialog .el-button--primary').filter({ hasText: '确定' }).click()
    await expect(updated).toBeTruthy()
  })

  test('删除分类', async ({ page }) => {
    await page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 't', role: 'admin', name: 'Admin' } } }))
    await page.route('**/api/category/page**', route => route.fulfill({ json: { code: 1, data: { total: 1, records: [{ id: 1, name: '饮品', type: 1, sort: 1, status: 1 }] } } }))
    let deleted = false
    await page.route('**/api/category**', async route => {
      if (route.request().method() === 'DELETE') {
        deleted = true
        await route.fulfill({ json: { code: 1, msg: '删除成功' } })
      } else {
        await route.fallback()
      }
    })
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await page.goto('/category/list')
    await page.locator('.el-button--danger').filter({ hasText: '删除' }).first().click()
    await page.locator('.el-message-box .el-button--primary').filter({ hasText: '确定' }).click()
    await expect(deleted).toBeTruthy()
  })
})
