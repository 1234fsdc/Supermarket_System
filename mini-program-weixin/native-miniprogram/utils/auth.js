/**
 * 登录态与用户信息管理
 */
const api = require('./api')

/**
 * 检查是否已登录（本地有 token）
 */
function isLoggedIn() {
  return !!wx.getStorageSync('token')
}

/**
 * 获取当前 token
 */
function getToken() {
  return wx.getStorageSync('token') || ''
}

/**
 * 触发登录（如未登录会走微信 code → 后端登录）
 */
function ensureLogin() {
  return new Promise((resolve, reject) => {
    if (isLoggedIn()) {
      resolve(getToken())
      return
    }
    const app = getApp()
    app.doLogin().then(() => resolve(getToken())).catch(reject)
  })
}

/**
 * 退出登录
 */
function logout() {
  wx.removeStorageSync('token')
  wx.removeStorageSync('userId')
  wx.removeStorageSync('openid')
  const app = getApp()
  if (app) {
    app.globalData.token = ''
    app.globalData.userId = null
    app.globalData.userInfo = null
    app.globalData.cartCount = 0
  }
}

/**
 * 获取当前用户 ID
 */
function getUserId() {
  return wx.getStorageSync('userId') || null
}

module.exports = {
  isLoggedIn,
  getToken,
  ensureLogin,
  logout,
  getUserId
}