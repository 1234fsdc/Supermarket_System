const api = require('../../utils/api.js')

Page({
  data: {
    statusBarHeight: 20,
    shopInfo: {},
    categories: [],
    activeCategoryId: 0,
    products: [],
    productMap: {},
    cartList: [],
    cartCount: 0,
    cartTotal: 0,
    seckillActivity: null,
    seckillProducts: [],
    userInfo: null,
    isLoggedIn: false,
    loading: false,
    page: 1
  },

  onLoad() {
    try {
      const sysInfo = wx.getSystemInfoSync()
      this.setData({ statusBarHeight: sysInfo.statusBarHeight || 20 })
    } catch (e) {}
  },

  onShow() {
    const app = getApp()
    const token = wx.getStorageSync('token')
    this.setData({
      userInfo: app.globalData.userInfo,
      isLoggedIn: !!token
    })
    this.loadShopInfo()
    this.loadCategories()
    this.loadSeckillActivity()
    this.loadCartList()
  },

  onPullDownRefresh() {
    Promise.all([
      this.loadCategories(),
      this.loadCartList(),
      this.loadSeckillActivity()
    ]).finally(() => wx.stopPullDownRefresh())
  },

  async loadShopInfo() {
    try {
      const status = await api.getShopStatus()
      const app = getApp()
      this.setData({
        'shopInfo.status': status || 1,
        ...app.globalData.shopInfo
      })
    } catch (e) {
      const app = getApp()
      this.setData({ shopInfo: { ...app.globalData.shopInfo, status: 1 } })
    }
  },

  async loadCategories() {
    try {
      const list = await api.getCategoryList(1)
      const cats = list || []
      if (cats.length > 0) {
        const firstId = cats[0].id
        this.setData({ categories: cats, activeCategoryId: firstId })
        this.loadProducts(firstId)
      }
    } catch (e) {
      this.setData({ categories: [] })
    }
  },

  async loadProducts(categoryId) {
    if (!categoryId) return
    this.setData({ loading: true })
    try {
      const list = await api.getProductList(categoryId)
      this.setData({ products: list || [], loading: false })
    } catch (e) {
      this.setData({ products: [], loading: false })
    }
  },

  async loadSeckillActivity() {
    try {
      const list = await api.getSeckillActivities()
      const ongoing = (list || []).find(a => a.status === 1) || (list || [])[0]
      if (ongoing) {
        this.setData({
          seckillActivity: {
            id: ongoing.id,
            name: ongoing.name,
            endTime: new Date(ongoing.endTime).getTime()
          },
          seckillProducts: (ongoing.products || []).slice(0, 4)
        })
      }
    } catch (e) {
      this.setData({ seckillActivity: null, seckillProducts: [] })
    }
  },

  onCategoryChange(e) {
    const id = e.detail.id
    if (id === this.data.activeCategoryId) return
    this.setData({ activeCategoryId: id, page: 1 })
    this.loadProducts(id)
  },

  async loadCartList() {
    try {
      const list = await api.getCartList()
      const map = {}
      let totalCount = 0
      let totalPrice = 0
      ;(list || []).forEach(item => {
        map[item.productId] = item.number || 0
        totalCount += item.number || 0
        totalPrice += (item.amount || 0) * (item.number || 0)
      })
      this.setData({
        cartList: list || [],
        productMap: map,
        cartCount: totalCount,
        cartTotal: Number(totalPrice.toFixed(2))
      })
      const app = getApp()
      app.globalData.cartCount = totalCount
    } catch (e) {
      this.setData({ cartList: [], productMap: {}, cartCount: 0, cartTotal: 0 })
    }
  },

  async onAdd(e) {
    const product = e.detail.product
    if (!product) return
    try {
      await api.addCart({ productId: product.id, number: 1 })
      this.loadCartList()
    } catch (err) {}
  },

  async onSub(e) {
    const product = e.detail.product
    if (!product) return
    try {
      await api.subCart({ productId: product.id, number: 1 })
      this.loadCartList()
    } catch (err) {}
  },

  onCardTap(e) {
    const product = e.detail.product
    if (!product) return
    wx.navigateTo({ url: `/pages/details/details?id=${product.id}` })
  },

  onLoadMore() {},

  // 导航
  onSearchTap() {
    wx.navigateTo({ url: '/pages/search/search' })
  },

  onSeckillTap() {
    wx.navigateTo({ url: '/pages/seckill/seckill' })
  },


  onCartTap() {
    if (this.data.cartCount > 0) {
      this.showCartPreview()
    } else {
      wx.showToast({ title: '购物车是空的', icon: 'none' })
    }
  },

  showCartPreview() {
    const items = this.data.cartList.map(c => `${c.name} ×${c.number}`).join('\n')
    wx.showModal({
      title: `购物车（${this.data.cartCount}件）`,
      content: items || '空',
      showCancel: false,
      confirmText: '去结算',
      success: r => { if (r.confirm) this.onCheckout() }
    })
  },

  onCheckout() {
    if (this.data.cartCount === 0) return
    if (this.data.cartTotal < this.data.shopInfo.minOrder) {
      wx.showToast({ title: `差¥${(this.data.shopInfo.minOrder - this.data.cartTotal).toFixed(2)}起送`, icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/order/order' })
  },

  goMy() { wx.navigateTo({ url: '/pages/my/my' }) }
})
