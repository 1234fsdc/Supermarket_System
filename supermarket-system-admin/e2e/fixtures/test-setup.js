import { test as base, expect } from '@playwright/test'
import { MOCK_LOGIN_RESPONSE, MOCK_DASHBOARD, MOCK_ORDER_STATISTICS, MOCK_PRODUCT_PAGE, MOCK_ORDER_PAGE, MOCK_CATEGORY_PAGE, MOCK_EMPLOYEE_PAGE, MOCK_SHOP_STATUS, MOCK_COUPON_PAGE } from './mock-data.js'

export const test = base.extend({
  loggedInPage: async ({ page }, use) => {
    await page.route('**/api/employee/login', async route => {
      await route.fulfill({ json: MOCK_LOGIN_RESPONSE })
    })

    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', '123456')
    await page.click('button:has-text("登录")')
    await page.waitForURL(/\/dashboard/)

    await use(page)
  },

  mockedApi: async ({ page }, use) => {
    await page.route('**/api/employee/login', async route => {
      await route.fulfill({ json: MOCK_LOGIN_RESPONSE })
    })
    await page.route('**/api/order/conditionSearch*', async route => {
      await route.fulfill({ json: { code: 1, data: { total: 0, records: [] } } })
    })
    await page.route('**/api/order/statistics', async route => {
      await route.fulfill({ json: MOCK_ORDER_STATISTICS })
    })
    await page.route('**/api/admin/product/page*', async route => {
      await route.fulfill({ json: MOCK_PRODUCT_PAGE })
    })
    await page.route('**/api/order/conditionSearch*', async route => {
      await route.fulfill({ json: MOCK_ORDER_PAGE })
    })
    await page.route('**/api/category/page*', async route => {
      await route.fulfill({ json: MOCK_CATEGORY_PAGE })
    })
    await page.route('**/api/employee/page*', async route => {
      await route.fulfill({ json: MOCK_EMPLOYEE_PAGE })
    })
    await page.route('**/api/shop/status', async route => {
      await route.fulfill({ json: MOCK_SHOP_STATUS })
    })
    await page.route('**/api/admin/coupon/template/page*', async route => {
      await route.fulfill({ json: MOCK_COUPON_PAGE })
    })
    await use(page)
  }
})

export { expect } from '@playwright/test'
