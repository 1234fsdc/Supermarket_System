const api = require('../../utils/api.js')
const { formatPriceNumber, formatRelativeTime } = require('../../utils/format.js')

Page({
  data: {
    productId: null,
    product: {},
    cartCount: 0,
    soldCount: 0,
    detailImages: [],
    showSpecPopup: false,
    specCount: 1,
    // 商品详情Tab
    detailTab: 0, // 0=商品 1=评价 2=详情
    // 模拟评价
    mockReviews: [
      { user: '小***猫', rate: 5, avatar: '小', stars: '★★★★★', content: '品质很好，包装精美，配送速度超快！', time: '3天前', spec: '500g装' },
      { user: '张***生', rate: 5, avatar: '张', stars: '★★★★★', content: '第二次买了，性价比高，孩子很喜欢吃', time: '1周前', spec: '500g装' },
      { user: '李***姐', rate: 4, avatar: '李', stars: '★★★★☆', content: '还不错，物流很快，下次还会回购', time: '2周前', spec: '1kg装' }
    ],
  },

  onLoad(query) {
    this.setData({ productId: query.id })
    if (query.id) {
      this.loadDetail(query.id)
    }
    this.loadCartCount()
  },

  onShow() {
    this.loadCartCount()
  },

  async loadDetail(id) {
    try {
      const res = await api.getProductDetail(id)
      const product = res || {}
      const imgs = []
      if (product.image) imgs.push(product.image)
      // 多张图模拟（实际可扩展）
      if (product.image) imgs.push(product.image)

      this.setData({
        product: { ...product, priceText: formatPriceNumber(product.price) },
        detailImages: imgs,
        soldCount: product.salesVolume || Math.floor(Math.random() * 500) + 50
      })
      wx.setNavigationBarTitle({ title: product.name || '商品详情' })
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  async loadCartCount() {
    try {
      const list = await api.getCartList()
      const count = (list || []).reduce((sum, i) => sum + (i.number || 0), 0)
      this.setData({ cartCount: count })
    } catch (e) {}
  },

  async onAdd() {
    const { productId, specCount } = this.data
    if (!productId) return
    try {
      await api.addCart({ productId, number: specCount || 1 })
      wx.showToast({ title: '已加入购物车', icon: 'success' })
      this.loadCartCount()
    } catch (e) {}
  },

  async onBuyNow() {
    const { productId, specCount } = this.data
    if (!productId) return
    try {
      await api.addCart({ productId, number: specCount || 1 })
      wx.navigateTo({ url: '/pages/order/order' })
    } catch (e) {}
  },

  // tab切换
  switchTab(e) {
    const idx = e.currentTarget.dataset.idx
    this.setData({ detailTab: idx })
  },

  // 规格弹窗
  openSpec() {
    this.setData({ showSpecPopup: true, specCount: 1 })
  },
  closeSpec() {
    this.setData({ showSpecPopup: false })
  },
  onSpecMinus() {
    if (this.data.specCount <= 1) return
    this.setData({ specCount: this.data.specCount - 1 })
  },
  onSpecPlus() {
    if (this.data.specCount >= 99) return
    this.setData({ specCount: this.data.specCount + 1 })
  },
  onSpecConfirm() {
    this.setData({ showSpecPopup: false })
    this.onAdd()
  },

  goCart() {
    wx.navigateTo({ url: '/pages/order/order' })
  },

  goHome() {
    wx.switchTab ? wx.switchTab({ url: '/pages/index/index' }) : wx.reLaunch({ url: '/pages/index/index' })
  },

  onShareAppMessage() {
    return {
      title: this.data.product.name || '木东超市',
      path: `/pages/details/details?id=${this.data.productId}`
    }
  }
})
