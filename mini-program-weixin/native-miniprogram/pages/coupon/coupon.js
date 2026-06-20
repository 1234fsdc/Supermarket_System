const api = require('../../utils/api.js')

Page({
  data: {
    coupons: [],
    loading: false,
    receivedIds: {}
  },

  onLoad() {
    this.loadCoupons()
  },

  onShow() {
    this.loadReceived()
  },

  async loadCoupons() {
    this.setData({ loading: true })
    try {
      const list = await api.getAvailableCouponTemplates()
      const items = (list || []).map(c => ({
        ...c,
        discountBig: c.discountType === 1 ? c.discountValue : (c.discountValue * 10).toFixed(1),
        discountUnit: c.discountType === 2 ? '折' : '',
        conditionText: c.minSpend > 0 ? '满' + c.minSpend + '元可用' : '无门槛'
      }))
      this.setData({ coupons: items, loading: false })
    } catch (e) {
      this.setData({ loading: false, coupons: [] })
    }
  },

  async loadReceived() {
    try {
      const list = await api.getMyCoupons(1)
      const map = {}
      ;(list || []).forEach(c => { map[c.couponId] = true })
      this.setData({ receivedIds: map })
    } catch (e) {}
  },

  async onReceive(e) {
    const id = e.currentTarget.dataset.id
    if (this.data.receivedIds[id]) {
      wx.showToast({ title: '已领取', icon: 'none' })
      return
    }
    try {
      await api.receiveCoupon(id)
      wx.showToast({ title: '领取成功' })
      this.loadReceived()
    } catch (e) {}
  },

  goMyCoupon() {
    wx.navigateTo({ url: '/pages/myCoupon/myCoupon' })
  }
})