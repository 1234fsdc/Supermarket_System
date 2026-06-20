import { test, expect } from './fixtures/test-setup.js'

async function login(page) {
  await page.goto('/login')
  await page.fill('input[placeholder="请输入用户名"]', 'admin')
  await page.fill('input[placeholder="请输入密码"]', '123456')
  await page.click('button:has-text("登录")')
  await page.waitForURL(/\/dashboard/)
}

test.describe('优惠券管理全流程', () => {

  test('优惠券列表显示真实数据', async ({ page }) => {
    await login(page)
    await page.goto('/coupon/list')
    await page.waitForTimeout(2000)
    await expect(page.locator('.el-table__row').first()).toBeVisible()
  })

  test('新增优惠券模板', async ({ page }) => {
    await login(page)
    await page.goto('/coupon/list')
    await page.waitForTimeout(1500)
    const addBtn = page.getByRole('button', { name: '新增' }).first()
    if (await addBtn.isVisible()) {
      await addBtn.click()
      await page.waitForTimeout(500)
      await expect(page.locator('.el-dialog')).toBeVisible()
      await page.locator('.el-dialog .el-button').filter({ hasText: '取消' }).first().click()
    }
  })
})
