const app = getApp();

Component({
  properties: {},
  data: {
    showChat: false,
    inputValue: '',
    messages: [],
    loading: false,
    scrollId: '',
    msgCount: 0,
    quickQuestions: [
      '配送费是多少？',
      '下单后多久需要付款？',
      '如何查看订单状态？',
      '如何取消订单？',
      '支持哪些支付方式？',
      '收到商品有问题怎么办？'
    ]
  },
  methods: {
    openChat() {
      this.setData({ showChat: true });
    },
    closeChat() {
      this.setData({ showChat: false });
    },
    onInput(e) {
      this.setData({ inputValue: e.detail.value });
    },
    sendMessage() {
      const question = this.data.inputValue.trim();
      if (!question) return;
      this.setData({ inputValue: '', loading: true });
      const userMsg = {
        id: Date.now(),
        type: 'user',
        content: question
      };
      this.setData({
        messages: [...this.data.messages, userMsg],
        scrollId: 'msg-' + this.data.msgCount
      });
      this.data.msgCount++;
      this.fetchAnswer(question);
    },
    fetchAnswer(question) {
      wx.request({
        url: 'http://localhost:8080/user/ai-customer/ask',
        method: 'GET',
        data: { question: question },
        timeout: 10000,
        success: (res) => {
          if (res.statusCode === 200 && res.data.code === 1) {
            const botMsg = {
              id: Date.now(),
              type: 'bot',
              content: res.data.data
            };
            this.setData({
              messages: [...this.data.messages, botMsg],
              loading: false,
              scrollId: 'msg-' + this.data.msgCount
            });
            this.data.msgCount++;
          } else {
            this.handleFallback(question);
          }
        },
        fail: () => {
          this.handleFallback(question);
        }
      });
    },
    handleFallback(question) {
      const lowerQ = question.toLowerCase();
      let answer = '抱歉，我暂时无法回答您的问题。您可以尝试以下方式获取帮助：\n1. 拨打商家电话咨询\n2. 查看订单详情联系客服\n3. 重新描述您的问题';
      const fallbackRules = [
        { keys: ['配送费', '运费', '送货费'], answer: '本超市配送费为6元。' },
        { keys: ['多久', '时间', '分钟'], answer: '正常情况下预计配送时长约12分钟。' },
        { keys: ['付款', '支付', '15分钟'], answer: '下单后请在15分钟内完成支付，超时订单将自动取消。' },
        { keys: ['取消', '退单'], answer: '待支付订单可直接取消。已接单或配送中的订单如需取消，请先联系商家协商处理。' },
        { keys: ['退款', '退货'], answer: '已完成订单可在订单详情页申请退款。申请后商家会进行审核。' },
        { keys: ['地址', '收货'], answer: '进入"我的"页面，点击"收货地址"，然后添加新的收货地址。' },
        { keys: ['支付', '微信', '支付宝'], answer: '本超市支持微信支付和支付宝两种支付方式。' },
        { keys: ['订单', '状态'], answer: '您可以在"我的"页面点击"历史订单"查看订单详情和状态。' },
        { keys: ['有问题', '损坏', '质量'], answer: '如收到商品存在质量问题或损坏，请在订单详情页申请退款，或联系商家客服处理。' },
        { keys: ['客服', '帮助', '联系'], answer: '您可以通过以下方式联系客服：1. 使用AI智能客服（就是我）；2. 在订单详情页联系商家电话。' },
        { keys: ['在哪', '地址', '位置'], answer: '凡栋超市位于北京市朝阳区新街大道一号楼8层。距离您约1.5km。' },
        { keys: ['规格', '口味'], answer: '部分商品有多种规格或口味可选。点击商品图片进入详情页，选择您需要的规格或口味后加入购物车。' }
      ];
      for (const rule of fallbackRules) {
        if (rule.keys.some(k => lowerQ.includes(k))) {
          answer = rule.answer;
          break;
        }
      }
      const botMsg = {
        id: Date.now(),
        type: 'bot',
        content: answer
      };
      this.setData({
        messages: [...this.data.messages, botMsg],
        loading: false,
        scrollId: 'msg-' + this.data.msgCount
      });
      this.data.msgCount++;
    },
    sendQuickQuestion(e) {
      const question = e.currentTarget.dataset.question;
      this.setData({ inputValue: question });
      this.sendMessage();
    }
  }
});
