const api = require('../../utils/api.js')

Page({
  data: {
    orderId: '',
    amount: 0,
    payMethod: 1, // 1=微信 2=支付宝
    countdown: 15 * 60,
    paying: false
  },

  onLoad(query) {
    this.setData({
      orderId: query.orderId || '',
      amount: query.amount || 0
    })
    this.tick()
  },

  onUnload() {
    if (this.timer) clearInterval(this.timer)
  },

  tick() {
    this.timer = setInterval(() => {
      const c = this.data.countdown - 1
      const cdText = this.formatText(c)
      this.setData({ countdown: c, cdText })
      if (c <= 0) {
        clearInterval(this.timer)
        wx.showToast({ title: '支付超时', icon: 'none' })
        setTimeout(() => wx.redirectTo({ url: '/pages/index/index' }), 1000)
      }
    }, 1000)
  },

  formatText(c) {
    const m = Math.floor(c / 60)
    const s = c % 60
    const pad = n => String(n).padStart(2, '0')
    return `${pad(m)}:${pad(s)}`
  },

  selectMethod(e) {
    const m = Number(e.currentTarget.dataset.method)
    this.setData({ payMethod: m })
  },

  async onPay() {
    if (this.data.paying || !this.data.orderId) return
    this.setData({ paying: true })
    try {
      await api.payOrder(Number(this.data.orderId))
      clearInterval(this.timer)
      wx.redirectTo({
        url: `/pages/success/success?orderId=${this.data.orderId}&amount=${this.data.amount}`
      })
    } catch (e) {
      this.setData({ paying: false })
    }
  },

  onCancel() {
    wx.showModal({
      title: '取消支付',
      content: '确定要取消支付吗？订单将保留在"待付款"列表',
      success: res => {
        if (res.confirm) {
          wx.redirectTo({ url: '/pages/index/index' })
        }
      }
    })
  }
})