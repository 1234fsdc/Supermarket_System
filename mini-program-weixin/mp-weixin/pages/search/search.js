const app = getApp()

Page({
  data: {
    keyword: '',
    historyList: [],
    resultList: [],
    showResult: false,
    loading: false
  },

  onLoad: function() {
    this.loadHistory()
  },

  // 加载搜索历史
  loadHistory: function() {
    const history = wx.getStorageSync('search_history') || []
    this.setData({
      historyList: history
    })
  },

  // 保存搜索历史
  saveHistory: function(keyword) {
    if (!keyword) return
    let history = wx.getStorageSync('search_history') || []
    // 去重
    const index = history.indexOf(keyword)
    if (index > -1) {
      history.splice(index, 1)
    }
    // 插入到开头
    history.unshift(keyword)
    // 最多保留10条
    if (history.length > 10) {
      history = history.slice(0, 10)
    }
    wx.setStorageSync('search_history', history)
    this.setData({
      historyList: history
    })
  },

  // 输入关键词
  onKeywordInput: function(e) {
    this.setData({
      keyword: e.detail.value
    })
  },

  // 清空输入框
  onClear: function() {
    this.setData({
      keyword: ''
    })
  },

  // 搜索
  onSearch: function() {
    const keyword = this.data.keyword.trim()
    if (!keyword) {
      wx.showToast({
        title: '请输入搜索关键词',
        icon: 'none'
      })
      return
    }

    // 保存搜索历史
    this.saveHistory(keyword)

    // 显示搜索结果
    this.setData({
      showResult: true,
      loading: true
    })

    // 调用后端搜索API
    wx.request({
      url: app.globalData.baseUrl + '/user/product/search',
      method: 'GET',
      data: {
        keyword: keyword
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        if (res.data.code === 1) {
          this.setData({
            resultList: res.data.data,
            loading: false
          })
        } else {
          wx.showToast({
            title: res.data.msg || '搜索失败',
            icon: 'none'
          })
          this.setData({
            loading: false
          })
        }
      },
      fail: (err) => {
        wx.showToast({
          title: '网络异常，请重试',
          icon: 'none'
        })
        this.setData({
          loading: false
        })
      }
    })
  },

  // 点击搜索历史标签
  onHistoryTagTap: function(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.setData({
      keyword: keyword
    })
    this.onSearch()
  },

  // 清空搜索历史
  onClearHistory: function() {
    wx.showModal({
      title: '提示',
      content: '确定清空搜索历史吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('search_history')
          this.setData({
            historyList: []
          })
          wx.showToast({
            title: '已清空',
            icon: 'success'
          })
        }
      }
    })
  },

  // 点击商品
  onProductTap: function(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: '/pages/details/index?id=' + id
    })
  }
})
