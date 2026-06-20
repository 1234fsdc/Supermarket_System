/**
 * 左侧分类竖向滚动栏
 * props: list [{ id, name }], activeId
 * events: change(id)
 */
Component({
  properties: {
    list: { type: Array, value: [] },
    activeId: { type: Number, value: 0 }
  },
  methods: {
    onTap(e) {
      const id = e.currentTarget.dataset.id
      this.triggerEvent('change', { id })
    }
  }
})