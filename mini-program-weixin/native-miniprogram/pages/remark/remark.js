const app = getApp()

Page({
  data: {
    text: ''
  },

  onLoad(query) {
    if (query.text) {
      this.setData({ text: decodeURIComponent(query.text) })
    }
  },

  onInput(e) {
    this.setData({ text: e.detail.value })
  },

  onSave() {
    app.globalData.orderRemark = this.data.text
    wx.navigateBack()
  }
})