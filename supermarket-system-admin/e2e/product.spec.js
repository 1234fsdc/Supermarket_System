import { test, expect } from './fixtures/test-setup.js'

test.describe('商品管理', () => {

  function setupMocks(page) {
    return Promise.all([
      page.route('**/api/employee/login', route => route.fulfill({ json: { code: 1, data: { token: 't', role: 'admin', name: 'Admin' } } })),
      page.route('**/api/order/conditionSearch**', route => route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } })),
      page.route('**/api/admin/product/page**', route => route.fulfill({ json: { code: 1, data: { total: 2, records: [
        { id: 1, name: '可乐', categoryName: '饮品', price: 3.50, status: 1, stock: 100, image: '', description: '' },
        { id: 2, name: '薯片', categoryName: '零食', price: 8.00, status: 1, stock: 50, image: '', description: '' },
      ]}}})),
      page.route('**/api/category/list', route => route.fulfill({ json: { code: 1, data: [] } })),
    ])
  }

  test('商品列表显示分页数据', async ({ page }) => {
    await setupMocks(page)
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await page.goto('/product/list')
    await expect(page.getByText('可乐').first()).toBeVisible()
    await expect(page.getByText('薯片').first()).toBeVisible()
  })

  test('点击新增商品打开弹窗', async ({ page }) => {
    await setupMocks(page)
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)
    await page.goto('/product/list')
    await page.waitForTimeout(1000)
    await page.getByRole('button', { name: '新增商品' }).click()
    await expect(page.locator('.el-dialog')).toContainText('新增商品')
  })
})
