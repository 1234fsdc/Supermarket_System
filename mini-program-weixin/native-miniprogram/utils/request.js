/**
 * 网络请求封装
 * - 统一 BASE_URL、token header
 * - 自动加载提示、错误处理
 * - 401 自动尝试重新登录
 * - Promise 风格，支持 async/await
 */

const BASE_URL = 'http://localhost:8080'

/** 是否正在重新登录中 */
let isRefreshing = false
/** 等待重新登录完成的回调队列 */
let refreshQueue = []

/**
 * 过滤掉 undefined / null 的查询参数
 */
function cleanParams(obj) {
  if (!obj) return undefined
  const result = {}
  Object.keys(obj).forEach(key => {
    if (obj[key] !== undefined && obj[key] !== null) {
      result[key] = obj[key]
    }
  })
  return Object.keys(result).length > 0 ? result : undefined
}

/**
 * 重新登录并刷新 token
 */
function refreshLogin() {
  return new Promise((resolve, reject) => {
    wx.login({
      success: (res) => {
        if (res.code) {
          wx.request({
            url: BASE_URL + '/user/user/login',
            method: 'POST',
            data: { code: res.code },
            success: (loginRes) => {
              if (loginRes.data && loginRes.data.code === 1) {
                const data = loginRes.data.data
                wx.setStorageSync('token', data.token)
                wx.setStorageSync('userId', data.id)
                wx.setStorageSync('openid', data.openid)
                resolve(data.token)
              } else {
                reject(loginRes.data)
              }
            },
            fail: reject
          })
        } else {
          reject(res)
        }
      },
      fail: reject
    })
  })
}

/**
 * 发起请求
 * @param {Object} options
 * @param {String} options.url - 接口路径（如 /user/user/login）
 * @param {String} [options.method='GET']
 * @param {Object} [options.data] - POST/PUT 请求体
 * @param {Object} [options.params] - GET 查询参数
 * @param {Boolean} [options.showLoading=false]
 * @param {String} [options.loadingText='加载中...']
 * @param {Number} [options.retryCount=0] - 内部用，重试次数
 * @returns {Promise} resolve(data)
 */
function request({ url, method = 'GET', data, params, header = {}, showLoading = false, loadingText = '加载中...', retryCount = 0 }) {
  return new Promise((resolve, reject) => {
    if (showLoading) {
      wx.showLoading({ title: loadingText, mask: true })
    }

    const token = wx.getStorageSync('token')
    wx.request({
      url: BASE_URL + url,
      method,
      data: method === 'GET' ? cleanParams(params) : data,
      header: {
        'content-type': 'application/json',
        'authentication': token || '',
        ...header
      },
      success: (res) => {
        if (showLoading) wx.hideLoading()

        // 401 → 尝试重新登录后重试
        if (res.statusCode === 401 && retryCount < 2) {
          if (!isRefreshing) {
            isRefreshing = true
            refreshLogin()
              .then(() => {
                isRefreshing = false
                const queue = refreshQueue.slice()
                refreshQueue = []
                queue.forEach(fn => fn(true))
              })
              .catch(() => {
                isRefreshing = false
                const queue = refreshQueue.slice()
                refreshQueue = []
                queue.forEach(fn => fn(false))
              })
          }
          // 加入等待队列，登录完成后自动重试
          refreshQueue.push((loginOk) => {
            if (loginOk) {
              resolve(request({ url, method, data, params, header, showLoading: false, retryCount: retryCount + 1 }))
            } else {
              wx.showToast({ title: '登录失效', icon: 'none' })
              reject(res)
            }
          })
          return
        }

        if (res.statusCode !== 200) {
          wx.showToast({ title: '网络错误', icon: 'none' })
          reject(res)
          return
        }

        // 业务层错误
        const body = res.data || {}
        if (body.code === 1 || body.code === 200) {
          resolve(body.data)
        } else {
          wx.showToast({ title: body.msg || '操作失败', icon: 'none' })
          reject(body)
        }
      },
      fail: (err) => {
        if (showLoading) wx.hideLoading()
        wx.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

module.exports = {
  request,
  BASE_URL
}