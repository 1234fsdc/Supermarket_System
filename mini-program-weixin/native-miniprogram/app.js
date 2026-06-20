// app.js
App({
  async onLaunch() {
    // 强制登录：检查登录态，未登录则立即登录
    const token = wx.getStorageSync('token')
    if (!token) {
      await this.forceLogin()
    }
    if (wx.getStorageSync('token')) {
      this.globalData.token = wx.getStorageSync('token')
      this.globalData.userId = wx.getStorageSync('userId')
      this.refreshCartCount()
    }
  },

  // 强制登录，最多重试3次
  async forceLogin() {
    for (let i = 0; i < 3; i++) {
      try {
        await this.doLogin()
        return
      } catch (e) {
        console.error(`登录第${i + 1}次失败：`, e)
        if (i < 2) {
          await new Promise(r => setTimeout(r, 1000))
        }
      }
    }
    wx.showToast({ title: '登录失败，请检查后端服务', icon: 'none', duration: 3000 })
  },

  doLogin() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: (loginRes) => {
          if (!loginRes.code) {
            reject(new Error('wx.login 未获取到 code'))
            return
          }
          wx.request({
            url: 'http://localhost:8080/user/user/login',
            method: 'POST',
            header: { 'Content-Type': 'application/json' },
            data: { code: loginRes.code },
            success: (res) => {
              console.log('登录响应：', JSON.stringify(res.data))
              if (res.statusCode !== 200) {
                reject(new Error(`HTTP ${res.statusCode}`))
                return
              }
              if (res.data && res.data.code === 1) {
                const data = res.data.data
                wx.setStorageSync('token', data.token)
                wx.setStorageSync('userId', data.id)
                wx.setStorageSync('openid', data.openid || '')
                this.globalData.token = data.token
                this.globalData.userId = data.id
                this.globalData.userInfo = data
                resolve(data)
              } else {
                reject(new Error(JSON.stringify(res.data)))
              }
            },
            fail: (err) => {
              reject(err)
            }
          })
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  },

  refreshCartCount() {
    wx.request({
      url: 'http://localhost:8080/user/shoppingCart/list',
      header: { authentication: wx.getStorageSync('token') || '' },
      success: (res) => {
        if (res.data && res.data.code === 1) {
          const list = res.data.data || []
          const count = list.reduce((sum, item) => sum + (item.number || 0), 0)
          this.globalData.cartCount = count
        }
      }
    })
  },

  globalData: {
    token: '',
    userId: null,
    userInfo: null,
    openid: '',
    cartCount: 0,
    currentAddress: null,
    orderRemark: '',
    shopInfo: {
      id: 1,
      name: '木东超市',
      rating: 4.8,
      monthlySales: 1200,
      deliveryMinutes: 30,
      deliveryFee: 0,
      minOrder: 29,
      status: 1
    },
    theme: {
      red: '#FF0036',
      redLight: '#FF6A81',
      gold: '#FFD700'
    }
  }
})
