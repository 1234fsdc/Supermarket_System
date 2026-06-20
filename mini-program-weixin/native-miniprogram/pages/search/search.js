const api = require('../../utils/api.js')

Page({
  data: {
    keyword: '',
    history: [],
    hot: ['矿泉水', '薯片', '方便面', '纯牛奶', '抽纸', '面包', '洗面奶', '洗衣液'],
    results: [],
    loading: false,
    searched: false
  },

  onLoad() {
    try {
      const his = wx.getStorageSync('searchHistory') || []
      this.setData({ history: his })
    } catch (e) {}
  },

  onInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onConfirm() {
    this.doSearch()
  },

  onHotTap(e) {
    const kw = e.currentTarget.dataset.kw
    this.setData({ keyword: kw })
    this.doSearch()
  },

  onHistoryTap(e) {
    const kw = e.currentTarget.dataset.kw
    this.setData({ keyword: kw })
    this.doSearch()
  },

  clearHistory() {
    wx.showModal({
      title: '清空历史',
      content: '确定清空搜索历史？',
      success: res => {
        if (res.confirm) {
          wx.removeStorageSync('searchHistory')
          this.setData({ history: [] })
        }
      }
    })
  },

  async doSearch() {
    const kw = this.data.keyword.trim()
    if (!kw) return
    let his = this.data.history.filter(k => k !== kw)
    his.unshift(kw)
    if (his.length > 10) his = his.slice(0, 10)
    wx.setStorageSync('searchHistory', his)
    this.setData({ history: his, loading: true, searched: true })
    try {
      const list = await api.searchProduct(kw)
      this.setData({ results: list || [], loading: false })
    } catch (e) {
      this.setData({ results: [], loading: false })
    }
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/details/details?id=${id}` })
  },

  goBack() {
    wx.navigateBack()
  }
})