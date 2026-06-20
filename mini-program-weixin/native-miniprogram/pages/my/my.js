const api = require('../../utils/api.js')

Page({
  data: {
    statusBarHeight: 20,
    userInfo: null,
    stats: { unpaid: 0, pending: 0, shipping: 0, completed: 0 },
    recentOrder: null,
    isLoggedIn: false,
    loggingIn: false,
    // 功能入口 - 仅已实现的功能
    funcEntries: [
      { id: 1, icon: '🎫', label: '优惠券', color: '#FF0036', bg: '#FFF0F3', page: '/pages/myCoupon/myCoupon' },
      { id: 2, icon: '📍', label: '收货地址', color: '#1677FF', bg: '#E8F5FF', page: '/pages/address/address' },
      { id: 3, icon: '⚡', label: '限时秒杀', color: '#FF6A00', bg: '#FFF5EB', page: '/pages/seckill/seckill' },
      { id: 4, icon: '🎁', label: '领券中心', color: '#FFD700', bg: '#FFF8E1', page: '/pages/coupon/coupon' },
      { id: 5, icon: '📞', label: '智能客服', color: '#00B578', bg: '#E8F8F0', page: 'service' },
      { id: 6, icon: '⚙️', label: '设置', color: '#666', bg: '#F0F0F0', page: 'settings' }
    ],
    userStats: [
      { id: 1, label: '优惠券', value: 0, page: '/pages/myCoupon/myCoupon' },
      { id: 2, label: '订单', value: 0, page: '/pages/historyOrder/historyOrder' }
    ]
  },

  onLoad() {
    try {
      const sysInfo = wx.getSystemInfoSync()
      this.setData({ statusBarHeight: sysInfo.statusBarHeight || 20 })
    } catch (e) {}
  },

  onShow() {
    const app = getApp()
    const token = wx.getStorageSync('token')
    this.setData({
      userInfo: app.globalData.userInfo,
      isLoggedIn: !!token
    })
    if (this.data.isLoggedIn) {
      this.loadOrderStats()
      this.loadCouponCount()
    }
  },

  async loadOrderStats() {
    try {
      const all = await api.getOrderHistory(1, 100)
      const list = all.records || all || []
      const stats = { unpaid: 0, pending: 0, shipping: 0, completed: 0 }
      list.forEach(o => {
        if (o.status === 1) stats.unpaid++
        else if (o.status === 2 || o.status === 3) stats.pending++
        else if (o.status === 4) stats.shipping++
        else if (o.status === 5) stats.completed++
      })
      const recent = list[0] || null
      this.setData({
        stats,
        recentOrder: recent,
        'userStats[1].value': stats.completed
      })
    } catch (e) {
      this.setData({ stats: { unpaid: 0, pending: 0, shipping: 0, completed: 0 } })
    }
  },

  async loadCouponCount() {
    try {
      const list = await api.getMyCoupons(1)
      this.setData({ 'userStats[0].value': (list || []).length })
    } catch (e) {}
  },

  goOrder(e) {
    const status = e.currentTarget.dataset.status
    wx.navigateTo({ url: `/pages/historyOrder/historyOrder?status=${status}` })
  },

  goAllOrders() {
    wx.navigateTo({ url: '/pages/historyOrder/historyOrder' })
  },

  // 功能入口跳转 - 仅已实现的功能
  onFuncTap(e) {
    const page = e.currentTarget.dataset.page
    if (!page) return
    if (page === 'settings') {
      this.goSettings()
    } else if (page === 'service') {
      wx.navigateTo({ url: '/pages/aiService/aiService' })
    } else {
      wx.navigateTo({ url: page })
    }
  },

  goSettings() {
    wx.showActionSheet({
      itemList: ['清除缓存', '关于木东超市'],
      success: res => {
        if (res.tapIndex === 0) {
          wx.clearStorage()
          wx.showToast({ title: '已清除' })
        } else if (res.tapIndex === 1) {
          wx.showModal({
            title: '木东超市 v2.0',
            content: '品质优选 · 极速达 · 正品保障',
            showCancel: false
          })
        }
      }
    })
  },

  goLogin() {
    if (this.data.loggingIn) return
    this.setData({ loggingIn: true })
    const app = getApp()
    app.doLogin().then(() => {
      this.setData({
        userInfo: app.globalData.userInfo,
        isLoggedIn: true,
        loggingIn: false
      })
      this.loadOrderStats()
      wx.showToast({ title: '登录成功', icon: 'success' })
    }).catch(e => {
      this.setData({ loggingIn: false })
      console.error('登录失败详情：', e)
      wx.showModal({
        title: '登录失败',
        content: '无法连接后端服务，请确认后端已启动（localhost:8080）',
        showCancel: false
      })
    })
  },

  onLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确定退出当前账号吗？',
      success: res => {
        if (res.confirm) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('userId')
          wx.removeStorageSync('openid')
          const app = getApp()
          app.globalData.token = ''
          app.globalData.userId = null
          app.globalData.userInfo = null
          app.globalData.cartCount = 0
          this.setData({
            userInfo: null,
            isLoggedIn: false,
            stats: { unpaid: 0, pending: 0, shipping: 0, completed: 0 },
            recentOrder: null,
            'userStats[0].value': 0,
            'userStats[1].value': 0
          })
          wx.showToast({ title: '已退出' })
        }
      }
    })
  },

  goBack() {
    wx.navigateBack({ fail: () => wx.reLaunch({ url: '/pages/index/index' }) })
  },

  goHome() {
    wx.reLaunch({ url: '/pages/index/index' })
  }
})
