/**
 * 限时秒杀倒计时横条
 * props: endTime - 结束时间戳（毫秒）
 * events: tap
 */
Component({
  properties: {
    endTime: { type: Number, value: 0 },
    title: { type: String, value: '限时秒杀' }
  },
  data: {
    countdownText: '00:00:00'
  },
  lifetimes: {
    attached() {
      this.tick()
      this.timer = setInterval(() => this.tick(), 1000)
    },
    detached() {
      if (this.timer) clearInterval(this.timer)
    }
  },
  methods: {
    tick() {
      const end = this.data.endTime
      if (!end) return
      let diff = Math.floor((end - Date.now()) / 1000)
      if (diff < 0) diff = 0
      const h = Math.floor(diff / 3600)
      const m = Math.floor((diff % 3600) / 60)
      const s = diff % 60
      const pad = n => String(n).padStart(2, '0')
      this.setData({
        countdownText: `${pad(h)}:${pad(m)}:${pad(s)}`
      })
    },
    onTap() {
      this.triggerEvent('tap')
    }
  }
})