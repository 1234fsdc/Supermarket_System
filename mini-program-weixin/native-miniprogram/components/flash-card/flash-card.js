/**
 * flash-card 爆款商品卡
 * props: product { id, name, image, price, originalPrice?, salesVolume?, description? }
 * events: addtap (加号), subtap (减号), cardtap (整卡)
 */
Component({
  properties: {
    product: { type: Object, value: {} },
    count: { type: Number, value: 0 },
    showBadge: { type: Boolean, value: true }
  },
  methods: {
    onAdd(e) {
      this.triggerEvent('addtap', { product: this.data.product })
    },
    onSub(e) {
      this.triggerEvent('subtap', { product: this.data.product })
    },
    onCard(e) {
      this.triggerEvent('cardtap', { product: this.data.product })
    }
  }
})