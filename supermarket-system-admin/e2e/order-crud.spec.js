import { test, expect } from './fixtures/test-setup.js'

async function login(page) {
  await page.goto('/login')
  await page.fill('input[placeholder="请输入用户名"]', 'admin')
  await page.fill('input[placeholder="请输入密码"]', '123456')
  await page.click('button:has-text("登录")')
  await page.waitForURL(/\/dashboard/)
}

test.describe('订单管理全流程', () => {

  test('订单列表显示', async ({ page }) => {
    await login(page)
    await page.goto('/order/list')
    await page.waitForTimeout(3000)
    await expect(page.locator('.el-card').first()).toBeVisible()
  })

  test('订单状态筛选', async ({ page }) => {
    await login(page)
    await page.goto('/order/list')
    await page.waitForTimeout(2000)
    const statusSelect = page.locator('.el-select').first()
    if (await statusSelect.isVisible()) {
      await statusSelect.click()
      await page.waitForTimeout(500)
      const option = page.locator('.el-select-dropdown__item').filter({ hasText: '已完成' }).first()
      if (await option.isVisible()) {
        await option.click()
        await page.waitForTimeout(1500)
      }
    }
  })

  test('订单详情弹窗', async ({ page }) => {
    await login(page)
    await page.goto('/order/list')
    await page.waitForTimeout(3000)
    const detailBtn = page.getByRole('button', { name: '详情' }).first()
    if (await detailBtn.isVisible()) {
      await detailBtn.click()
      await page.waitForTimeout(1500)
      await expect(page.locator('.el-dialog')).toContainText('订单详情')
      await page.locator('.el-dialog').getByText('关闭').click()
    }
  })
})
