const api = require('../../utils/api.js')

Page({
  data: {
    cartList: [],
    address: null,
    defaultAddress: null,
    remark: '',
    deliveryFee: 0,
    totalPrice: 0,
    finalPrice: 0,
    cartCount: 0,
    deliveryType: 1,
    estimatedTime: '',
    selectedCoupon: null,
    couponDiscount: 0,
    usableCoupons: [],
    submitting: false
  },

  onLoad() {
    const d = new Date(Date.now() + 30 * 60 * 1000)
    const pad = n => String(n).padStart(2, '0')
    const time = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    this.setData({ estimatedTime: time })
    // 从 app 读取配送费
    const app = getApp()
    this.setData({ deliveryFee: app.globalData.shopInfo.deliveryFee || 0 })
  },

  onShow() {
    this.loadCart()
    this.loadDefaultAddress()
    this.loadCoupons()
    const app = getApp()
    if (app.globalData.orderRemark) {
      this.setData({ remark: app.globalData.orderRemark })
      delete app.globalData.orderRemark
    }
    if (app.globalData.selectedAddress) {
      this.setData({ address: app.globalData.selectedAddress })
      delete app.globalData.selectedAddress
    }
  },

  calcFinal() {
    const final = Math.max(0, Number((this.data.totalPrice + this.data.deliveryFee - this.data.couponDiscount).toFixed(2)))
    this.setData({ finalPrice: final })
  },

  async loadCart() {
    try {
      const list = await api.getCartList()
      const items = list || []
      const total = items.reduce((sum, item) => sum + (item.amount || 0) * (item.number || 0), 0)
      const count = items.reduce((sum, item) => sum + (item.number || 0), 0)
      this.setData({ cartList: items, totalPrice: Number(total.toFixed(2)), cartCount: count })
      this.calcFinal()
    } catch (e) {
      this.setData({ cartList: [], totalPrice: 0, cartCount: 0 })
    }
  },

  async loadDefaultAddress() {
    try {
      const addr = await api.getDefaultAddress()
      this.setData({ defaultAddress: addr, address: addr || this.data.address })
    } catch (e) {}
  },

  async loadCoupons() {
    try {
      const list = await api.getUsableCoupons()
      this.setData({ usableCoupons: list || [] })
    } catch (e) {}
  },

  chooseAddress() {
    wx.navigateTo({ url: '/pages/address/address?from=order' })
  },

  goRemark() {
    wx.navigateTo({ url: `/pages/remark/remark?text=${encodeURIComponent(this.data.remark || '')}` })
  },

  chooseCoupon() {
    if (this.data.usableCoupons.length === 0) {
      wx.showToast({ title: '暂无可用优惠券', icon: 'none' })
      return
    }
    const list = this.data.usableCoupons.map(c => ({
      id: c.id,
      name: c.couponName,
      discount: c.discountValue,
      condition: c.minSpend
    }))
    wx.showActionSheet({
      itemList: list.map(c => `${c.name} (¥${c.discount} 抵扣)`).concat(['不使用优惠券']),
      success: async (res) => {
        if (res.tapIndex === list.length) {
          this.setData({ selectedCoupon: null, couponDiscount: 0 })
        } else {
          const c = list[res.tapIndex]
          this.setData({ selectedCoupon: c })
          try {
            const result = await api.calculateCoupon(c.id, this.data.totalPrice)
            this.setData({ couponDiscount: result.discountAmount || c.discount })
          } catch (e) {
            this.setData({ couponDiscount: c.discount })
          }
        }
        this.calcFinal()
      }
    })
  },

  toggleDelivery() {
    wx.showActionSheet({
      itemList: ['尽快送达', '选择时间'],
      success: res => {
        this.setData({ deliveryType: res.tapIndex === 0 ? 1 : 0 })
      }
    })
  },

  async onSubmit() {
    if (this.data.submitting) return
    if (!this.data.address) {
      wx.showToast({ title: '请选择收货地址', icon: 'none' })
      return
    }
    if (this.data.cartList.length === 0) {
      wx.showToast({ title: '购物车为空', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      const submitData = {
        addressBookId: this.data.address.id,
        amount: this.data.finalPrice,
        remark: this.data.remark,
        estimatedDeliveryTime: this.data.deliveryType === 1 ? null : this.data.estimatedTime,
        deliveryStatus: this.data.deliveryType,
        userCouponId: this.data.selectedCoupon ? this.data.selectedCoupon.id : null,
        originalAmount: this.data.totalPrice,
        couponAmount: this.data.couponDiscount
      }
      const res = await api.submitOrder(submitData)
      wx.showToast({ title: '订单已提交' })
      setTimeout(() => {
        wx.redirectTo({ url: `/pages/pay/pay?orderId=${res.id || res.orderId || ''}&amount=${this.data.finalPrice}` })
      }, 800)
    } catch (e) {
      this.setData({ submitting: false })
    }
  },

  goHome() { wx.switchTab ? wx.switchTab({ url: '/pages/index/index' }) : wx.reLaunch({ url: '/pages/index/index' }) }
})