/**
 * AI 客服浮窗（木东小蜜风格）
 */
Component({
  data: {
    visible: false,
    messages: [],
    inputText: '',
    sending: false,
    quickQuestions: [
      '配送费多少？',
      '多久能送达？',
      '支持哪些支付方式？',
      '如何申请退款？',
      '怎么领优惠券？'
    ]
  },
  methods: {
    toggle() {
      this.setData({ visible: !this.data.visible })
      if (this.data.visible && this.data.messages.length === 0) {
        this.pushBot('您好！我是木东超市的智能助手小蜜，可以问我关于商品、订单、配送等问题。')
      }
    },
    close() {
      this.setData({ visible: false })
    },
    onInput(e) {
      this.setData({ inputText: e.detail.value })
    },
    pickQuestion(e) {
      const q = e.currentTarget.dataset.q
      this.setData({ inputText: q })
      this.send()
    },
    async send() {
      const text = this.data.inputText.trim()
      if (!text || this.data.sending) return
      this.pushUser(text)
      this.setData({ inputText: '', sending: true })

      try {
        const res = await new Promise((resolve, reject) => {
          wx.request({
            url: 'http://localhost:8083/user/ai-customer/ask',
            method: 'POST',
            data: { question: text },
            success: r => {
              if (r.data && (r.data.code === 1 || r.data.code === 200)) {
                resolve(r.data.data)
              } else {
                reject(r.data)
              }
            },
            fail: reject
          })
        })
        const reply = (res && (res.answer || res.text)) || ''
        this.pushBot(reply || this.localAnswer(text))
      } catch (e) {
        this.pushBot(this.localAnswer(text))
      }
      this.setData({ sending: false })
    },
    pushUser(text) {
      this.setData({
        messages: [...this.data.messages, { role: 'user', text, time: this.now() }]
      })
    },
    pushBot(text) {
      this.setData({
        messages: [...this.data.messages, { role: 'bot', text, time: this.now() }]
      })
    },
    now() {
      const d = new Date()
      return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    },
    localAnswer(q) {
      if (/配送费|运费/.test(q)) return '木东超市满29元免配送费，不满收¥6配送费。'
      if (/多久|送达|时间|时长/.test(q)) return '下单后预计30分钟内送达，由蜂鸟即配配送。'
      if (/支付|付款/.test(q)) return '支持微信支付、支付宝。下单后请在15分钟内完成支付。'
      if (/发票/.test(q)) return '请在下单时备注开票信息，电子发票在订单完成后1个工作日内发送。'
      if (/退款|退货/.test(q)) return '未发货前可直接申请退款；已发货请在订单详情提交退款申请。'
      if (/优惠|券|折扣/.test(q)) return '首页可领新人满减券、满99减20券等，下单时自动抵扣最优优惠。'
      if (/客服|电话|联系/.test(q)) return '您可以拨打 400-888-8888 客服热线，或继续问我~'
      return '抱歉，我暂时没理解这个问题。请换个问法试试，或在"我的"页面中留言。'
    }
  }
})