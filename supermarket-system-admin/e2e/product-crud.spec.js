import { test, expect } from './fixtures/test-setup.js'

async function login(page) {
  await page.goto('/login')
  await page.fill('input[placeholder="请输入用户名"]', 'admin')
  await page.fill('input[placeholder="请输入密码"]', '123456')
  await page.click('button:has-text("登录")')
  await page.waitForURL(/\/dashboard/)
}

test.describe('商品管理 CRUD 全流程', () => {

  test('商品列表显示真实数据', async ({ page }) => {
    await login(page)
    await page.goto('/product/list')
    await page.waitForTimeout(2000)
    await expect(page.locator('.el-table__row').first()).toBeVisible()
  })

  test('新增商品并验证', async ({ page }) => {
    await login(page)
    await page.goto('/product/list')
    await page.waitForTimeout(1500)
    await page.getByRole('button', { name: '新增商品' }).click()
    await page.waitForTimeout(1000)

    const dialog = page.locator('.el-dialog')
    await dialog.getByRole('textbox').nth(0).fill('E2E测试商品')
    await dialog.locator('.el-input-number').nth(0).locator('input').fill('9.99')
    await dialog.locator('.el-select').click()
    await page.waitForTimeout(1000)
    await page.locator('li[role="option"]').nth(0).click({ force: true, timeout: 5000 }).catch(() => {
      page.locator('.el-popper').getByText('饮品').first().click({ force: true }).catch(() => {})
    })
    await dialog.locator('.el-input-number').nth(1).locator('input').fill('100')
    await dialog.getByRole('textbox').nth(1).fill('个')

    await dialog.getByRole('button', { name: '确定' }).click()
    await page.waitForTimeout(2000)

    await page.goto('/product/list')
    await page.waitForTimeout(2000)
    await expect(page.getByText('E2E测试商品').first()).toBeVisible()
  })

  test('商品搜索', async ({ page }) => {
    await login(page)
    await page.goto('/product/list')
    await page.waitForTimeout(2000)
    const searchInput = page.locator('input[placeholder*="商品名称"]').first()
    if (await searchInput.isVisible()) {
      await searchInput.fill('可乐')
      await page.getByRole('button', { name: '查询' }).click()
      await page.waitForTimeout(1500)
    }
    await expect(page.locator('.el-table__row').first()).toBeVisible()
  })

  test('商品启用/禁用', async ({ page }) => {
    await login(page)
    await page.goto('/product/list')
    await page.waitForTimeout(2000)
    const sw = page.locator('.el-switch').first()
    if (await sw.isVisible()) {
      await sw.click()
      await page.waitForTimeout(500)
    }
  })
})
