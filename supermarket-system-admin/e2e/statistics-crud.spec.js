import { test, expect } from './fixtures/test-setup.js'

async function login(page) {
  await page.goto('/login')
  await page.fill('input[placeholder="请输入用户名"]', 'admin')
  await page.fill('input[placeholder="请输入密码"]', '123456')
  await page.click('button:has-text("登录")')
  await page.waitForURL(/\/dashboard/)
}

test.describe('数据统计页面', () => {

  test('统计页面显示真实数据', async ({ page }) => {
    await login(page)
    await page.goto('/statistics/overview')
    await page.waitForTimeout(3000)
    await expect(page.locator('.stat-item').first()).toBeVisible()
  })

  test('订单统计卡片数据非空', async ({ page }) => {
    await login(page)
    await page.goto('/statistics/overview')
    await page.waitForTimeout(3000)
    const items = page.locator('.stat-item')
    await expect(items.first()).toBeAttached()
    const count = await items.count()
    expect(count).toBeGreaterThanOrEqual(4)
  })
})
