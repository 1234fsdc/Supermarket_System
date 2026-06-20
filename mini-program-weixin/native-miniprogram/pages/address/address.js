const api = require('../../utils/api.js')

Page({
  data: {
    list: [],
    from: '' // order=从订单页选择
  },

  onLoad(query) {
    if (query.from) this.setData({ from: query.from })
  },

  onShow() {
    this.loadList()
  },

  async loadList() {
    try {
      const list = await api.getAddressList()
      this.setData({ list: list || [] })
    } catch (e) {
      this.setData({ list: [] })
    }
  },

  /** 从订单页选择 */
  onSelect(e) {
    const addr = e.currentTarget.dataset.addr
    if (this.data.from === 'order') {
      const app = getApp()
      app.globalData.selectedAddress = addr
      wx.navigateBack()
    }
  },

  /** 设为默认 */
  async setDefault(e) {
    const id = e.currentTarget.dataset.id
    try {
      await api.setDefaultAddress({ id })
      wx.showToast({ title: '已设为默认' })
      this.loadList()
    } catch (e) {}
  },

  /** 编辑 */
  goEdit(e) {
    const { id } = e.currentTarget.dataset
      wx.navigateTo({ url: `/pages/addOrEditAddress/addOrEditAddress?id=${id}` })
  },

  /** 删除 */
  async onDelete(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '删除地址',
      content: '确定要删除这个地址吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.deleteAddress(id)
            wx.showToast({ title: '已删除' })
            this.loadList()
          } catch (e) {}
        }
      }
    })
  },

  goAdd() {
    wx.navigateTo({ url: '/pages/addOrEditAddress/addOrEditAddress' })
  }
})