const api = require('../../utils/api.js')
const { formatPrice, formatDateTime } = require('../../utils/format.js')

Page({
  data: {
    tabs: [
      { status: '', label: '全部' },
      { status: 1, label: '待付款' },
      { status: 2, label: '待接单' },
      { status: 3, label: '已接单' },
      { status: 4, label: '派送中' },
      { status: 5, label: '已完成' },
      { status: 6, label: '已取消' }
    ],
    activeStatus: '',
    list: [],
    page: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
  },

  onLoad(query) {
    if (query.status !== undefined && query.status !== '') {
      this.setData({ activeStatus: Number(query.status) })
    }
    this.loadList(true)
  },

  onPullDownRefresh() {
    this.loadList(true).finally(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadList(false)
    }
  },

  async loadList(reset) {
    if (this.data.loading) return
    this.setData({ loading: true })
    const page = reset ? 1 : this.data.page
    try {
      const res = await api.getOrderHistory(page, this.data.pageSize, this.data.activeStatus || null)
      const records = res.records || res || []
      const list = records.map(o => ({
        ...o,
        statusText: this.statusText(o.status),
        timeText: formatDateTime(o.orderTime)
      }))
      this.setData({
        list: reset ? list : this.data.list.concat(list),
        page: page + 1,
        hasMore: list.length === this.data.pageSize,
        loading: false
      })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  statusText(s) {
    return { 1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已完成', 6: '已取消', 7: '退款' }[s] || '未知'
  },

  onTabChange(e) {
    const status = e.currentTarget.dataset.status
    this.setData({ activeStatus: status === '' ? '' : Number(status) })
    this.loadList(true)
  },

  /** 跳详情（复用 details 页） */
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/details/details?id=${id}` })
  },

  /** 取消订单 */
  async onCancel(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '取消订单',
      content: '确定取消该订单吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.cancelOrder(id)
            wx.showToast({ title: '已取消' })
            this.loadList(true)
          } catch (e) {}
        }
      }
    })
  },

  /** 支付 */
  async onPay(e) {
    const { id, amount } = e.currentTarget.dataset
    wx.redirectTo({ url: `/pages/pay/pay?orderId=${id}&amount=${amount}` })
  },

  /** 再来一单 */
  async onReorder(e) {
    const id = e.currentTarget.dataset.id
    try {
      await api.reorder(id)
      wx.showToast({ title: '已加入购物车' })
      setTimeout(() => wx.switchTab({ url: '/pages/index/index' }), 1000)
    } catch (e) {}
  }
})