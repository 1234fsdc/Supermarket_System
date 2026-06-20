const api = require('../../utils/api.js')
const { formatDateTime } = require('../../utils/format.js')

Page({
  data: {
    tabs: [
      { status: 1, label: '未使用' },
      { status: 2, label: '已使用' },
      { status: 3, label: '已过期' }
    ],
    activeStatus: 1,
    list: []
  },

  onLoad() {
    this.loadList()
  },

  onShow() {
    this.loadList()
  },

  async loadList() {
    try {
      const res = await api.getMyCoupons(this.data.activeStatus)
      const list = (res || []).map(c => ({
        ...c,
        discountText: c.discountType === 1 ? '¥' + c.discountValue : (c.discountValue * 10).toFixed(1) + '折',
        discountBig: c.discountType === 1 ? '¥' + c.discountValue : (c.discountValue * 10).toFixed(1),
        discountUnit: c.discountType === 1 ? '' : '折',
        conditionText: c.minSpend > 0 ? `满${c.minSpend}元可用` : '无门槛',
        expireText: formatDateTime(c.expireTime)
      }))
      this.setData({ list })
    } catch (e) {
      this.setData({ list: [] })
    }
  },

  onTabChange(e) {
    const status = Number(e.currentTarget.dataset.status)
    this.setData({ activeStatus: status })
    this.loadList()
  },

  goReceive() {
    wx.navigateTo({ url: '/pages/coupon/coupon' })
  },

  goUse() {
    wx.navigateTo({ url: '/pages/index/index' })
  }
})