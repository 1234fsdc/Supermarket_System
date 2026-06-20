/**
 * 底部购物车条（仿淘宝闪购悬浮条）
 * props: totalCount, totalPrice, deliveryFee, minOrder
 * events: carttap, checkouttap
 */
Component({
  properties: {
    totalCount: { type: Number, value: 0 },
    totalPrice: { type: Number, value: 0 },
    deliveryFee: { type: Number, value: 6 },
    minOrder: { type: Number, value: 18 }
  },
  observers: {
    'totalPrice, totalCount, minOrder'(totalPrice, totalCount, minOrder) {
      const meetsMinOrder = totalPrice >= minOrder
      let diffText
      if (totalPrice === 0) {
        diffText = `¥${minOrder}起送`
      } else if (totalPrice < minOrder) {
        const diff = (minOrder - totalPrice).toFixed(2)
        diffText = `还差¥${diff}可起送`
      } else {
        diffText = '已满足起送'
      }
      this.setData({ meetsMinOrder, diffText })
    }
  },
  data: {
    meetsMinOrder: false,
    diffText: '¥18起送'
  },
  methods: {
    onCartTap() {
      this.triggerEvent('carttap')
    },
    onCheckout() {
      this.triggerEvent('checkouttap')
    }
  }
})