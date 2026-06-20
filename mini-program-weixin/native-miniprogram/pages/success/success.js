Page({
  data: {
    orderId: '',
    amount: 0,
    countdown: 5
  },
  onLoad(query) {
    this.setData({
      orderId: query.orderId || '',
      amount: query.amount || 0
    })
    this.tick()
  },
  tick() {
    this.timer = setInterval(() => {
      const c = this.data.countdown - 1
      this.setData({ countdown: c })
      if (c <= 0) {
        clearInterval(this.timer)
        this.goHome()
      }
    }, 1000)
  },
  onUnload() {
    if (this.timer) clearInterval(this.timer)
  },
  goHome() {
    wx.reLaunch({ url: '/pages/index/index' })
  },
  goOrderDetail() {
    if (this.data.orderId) {
      wx.redirectTo({ url: `/pages/historyOrder/historyOrder` })
    } else {
      wx.redirectTo({ url: '/pages/index/index' })
    }
  },
  goContinue() {
    wx.reLaunch({ url: '/pages/index/index' })
  }
})