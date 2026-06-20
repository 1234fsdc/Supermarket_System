const api = require('../../utils/api.js')
const { formatPrice } = require('../../utils/format.js')

Page({
  data: {
    activities: [],
    products: [],
    loading: false,
    countdownText: '00:00:00'
  },

  onLoad() {
    this.loadActivities()
  },

  onShow() {
    this.loadActivities()
  },

  onUnload() {
    if (this.timer) clearInterval(this.timer)
  },

  async loadActivities() {
    this.setData({ loading: true })
    try {
      const list = await api.getSeckillActivities()
      const ongoing = (list || []).find(a => a.status === 1) || (list || [])[0]
      if (ongoing) {
        const endTime = new Date(ongoing.endTime).getTime()
        this.setData({
          activities: list || [],
          products: (ongoing.products || []).map(p => ({
            ...p,
            seckillPriceText: p.seckillPrice,
            originalPriceText: p.price,
            saved: (p.price - p.seckillPrice).toFixed(2)
          }))
        })
        this.startCountdown(endTime)
      } else {
        this.setData({ activities: [], products: [] })
      }
    } catch (e) {
      this.setData({ activities: [], products: [] })
    }
    this.setData({ loading: false })
  },

  startCountdown(endTime) {
    if (this.timer) clearInterval(this.timer)
    this.tick(endTime)
    this.timer = setInterval(() => this.tick(endTime), 1000)
  },

  tick(endTime) {
    let diff = Math.floor((endTime - Date.now()) / 1000)
    if (diff < 0) diff = 0
    const h = Math.floor(diff / 3600)
    const m = Math.floor((diff % 3600) / 60)
    const s = diff % 60
    const pad = n => String(n).padStart(2, '0')
    this.setData({ countdownText: `${pad(h)}:${pad(m)}:${pad(s)}` })
  },

  async onSeckill(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '⚡ 确认抢购',
      content: '秒杀商品限购 2 件，确定要抢购吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.seckillBuy(id)
            wx.showToast({ title: '🎉 抢购成功' })
          } catch (e) {}
        }
      }
    })
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/details/details?id=${id}` })
  }
})