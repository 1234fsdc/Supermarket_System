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
              content: res.data.data.answer,
              products: res.data.data.products || []
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
      let products = [];
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
        { keys: ['规格', '口味'], answer: '部分商品有多种规格或口味可选。点击商品图片进入详情页，选择您需要的规格或口味后加入购物车。' },
        { keys: ['零食', '好吃'], answer: '推荐热销零食：乐事薯片12.9元、奥利奥饼干9.9元、三只松鼠坚果39.9元。', products: [
          {name: '乐事薯片', price: 12.9, desc: '经典原味大包装'},
          {name: '奥利奥饼干', price: 9.9, desc: '夹心美味'},
          {name: '三只松鼠坚果', price: 39.9, desc: '健康营养礼包'}
        ]},
        { keys: ['水果', '新鲜'], answer: '推荐当季新鲜水果：红富士苹果8.9元、进口香蕉5.9元、阳光玫瑰葡萄29.9元。', products: [
          {name: '红富士苹果', price: 8.9, desc: '脆甜多汁 500g'},
          {name: '进口香蕉', price: 5.9, desc: '营养方便 500g'},
          {name: '阳光玫瑰葡萄', price: 29.9, desc: '香甜爽口 500g'}
        ]},
        { keys: ['饮料', '喝的'], answer: '热销饮品：可口可乐18.9元(6瓶装)、农夫山泉2.5元、伊利纯牛奶5.9元。', products: [
          {name: '可口可乐', price: 18.9, desc: '冰爽6瓶装'},
          {name: '农夫山泉', price: 2.5, desc: '纯净550ml'},
          {name: '伊利纯牛奶', price: 5.9, desc: '营养250ml'}
        ]},
        { keys: ['蔬菜', '菜'], answer: '新鲜蔬菜推荐：有机西红柿6.9元、新鲜生菜3.9元、嫩黄瓜4.9元。', products: [
          {name: '有机西红柿', price: 6.9, desc: '自然成熟 500g'},
          {name: '新鲜生菜', price: 3.9, desc: '清脆爽口 1颗'},
          {name: '嫩黄瓜', price: 4.9, desc: '脆嫩可口 500g'}
        ]},
        { keys: ['肉'], answer: '精选肉类：新鲜五花肉29.9元、鸡腿肉19.9元、精品牛肉49.9元。', products: [
          {name: '新鲜五花肉', price: 29.9, desc: '肥瘦相间 500g'},
          {name: '鸡腿肉', price: 19.9, desc: '鲜嫩多汁 500g'},
          {name: '精品牛肉', price: 49.9, desc: '优质蛋白 500g'}
        ]},
        { keys: ['早餐', '早点'], answer: '美味早餐搭配：全麦面包12.9元、伊利纯牛奶5.9元、农家土鸡蛋15.9元。', products: [
          {name: '全麦面包', price: 12.9, desc: '健康之选'},
          {name: '伊利纯牛奶', price: 5.9, desc: '营养250ml'},
          {name: '农家土鸡蛋', price: 15.9, desc: '新鲜营养 10枚'}
        ]},
        { keys: ['便宜', '性价比', '实惠'], answer: '高性价比好物：农夫山泉2.5元、新鲜生菜3.9元、鸡蛋15.9元/10枚。', products: [
          {name: '农夫山泉', price: 2.5, desc: '纯净550ml'},
          {name: '新鲜生菜', price: 3.9, desc: '清脆爽口 1颗'},
          {name: '农家土鸡蛋', price: 15.9, desc: '新鲜营养 10枚'}
        ]}
      ];
      for (const rule of fallbackRules) {
        if (rule.keys.some(k => lowerQ.includes(k))) {
          answer = rule.answer;
          products = rule.products || [];
          break;
        }
      }
      const botMsg = {
        id: Date.now(),
        type: 'bot',
        content: answer,
        products: products
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
