import { test, expect } from './fixtures/test-setup.js'

async function login(page) {
  await page.goto('/login')
  await page.fill('input[placeholder="请输入用户名"]', 'admin')
  await page.fill('input[placeholder="请输入密码"]', '123456')
  await page.click('button:has-text("登录")')
  await page.waitForURL(/\/dashboard/)
}

test.describe('店铺状态管理', () => {

  test('店铺状态页面显示当前状态', async ({ page }) => {
    await login(page)
    await page.goto('/shop/status')
    await page.waitForTimeout(2000)
    await expect(page.getByText('营业中').or(page.getByText('已打烊')).first()).toBeVisible()
  })

  test('切换店铺状态', async ({ page }) => {
    await login(page)
    await page.goto('/shop/status')
    await page.waitForTimeout(2000)
    const toggleBtn = page.locator('.el-button').filter({ hasText: /切换|打烊|营业/ }).first()
    if (await toggleBtn.isVisible() && await toggleBtn.isEnabled()) {
      await toggleBtn.click()
      await page.waitForTimeout(1000)
    }
  })
})
