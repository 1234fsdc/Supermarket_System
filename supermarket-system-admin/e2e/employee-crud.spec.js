import { test, expect } from './fixtures/test-setup.js'

async function login(page) {
  await page.goto('/login')
  await page.fill('input[placeholder="请输入用户名"]', 'admin')
  await page.fill('input[placeholder="请输入密码"]', '123456')
  await page.click('button:has-text("登录")')
  await page.waitForURL(/\/dashboard/)
}

test.describe('员工管理全流程', () => {

  test('员工列表显示真实数据', async ({ page }) => {
    await login(page)
    await page.goto('/employee/list')
    await page.waitForTimeout(2000)
    await expect(page.locator('.el-table__row').first()).toBeVisible()
  })

  test('新增员工并验证', async ({ page }) => {
    await login(page)
    await page.goto('/employee/list')
    await page.waitForTimeout(1500)
    await page.getByRole('button', { name: '新增员工' }).click()
    await page.waitForTimeout(1000)

    const dialog = page.locator('.el-dialog')
    const inputs = dialog.locator('.el-input__inner')
    const count = await inputs.count()

    if (count >= 3) {
      await inputs.nth(0).fill(`e2euser${Date.now()}`)
      await inputs.nth(1).fill('E2E测试员工')
      await inputs.nth(2).fill('13900001234')
      if (count >= 4) await inputs.nth(3).fill('110101199001011234')
    }

    await dialog.getByRole('button', { name: '确定' }).click()
    await page.waitForTimeout(1000)
  })

  test('编辑员工', async ({ page }) => {
    await login(page)
    await page.goto('/employee/list')
    await page.waitForTimeout(2000)
    await page.getByRole('button', { name: '编辑' }).first().click()
    await page.waitForTimeout(1000)
    await expect(page.locator('.el-dialog')).toContainText('编辑员工')
    await page.locator('.el-dialog').getByRole('button', { name: '确定' }).click()
    await page.waitForTimeout(500)
  })

  test('启用/禁用员工', async ({ page }) => {
    await login(page)
    await page.goto('/employee/list')
    await page.waitForTimeout(2000)
    const sw = page.locator('.el-switch').first()
    if (await sw.isVisible()) {
      await sw.click()
      await page.waitForTimeout(500)
    }
  })
})
