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
    ],
    isStreaming: false,  // 是否正在流式输出
    currentStreamMsgId: null  // 当前流式消息ID
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
      
      // 如果正在流式输出，不允许发送新消息
      if (this.data.isStreaming) {
        wx.showToast({
          title: '请等待回复完成',
          icon: 'none'
        });
        return;
      }
      
      this.setData({ inputValue: '', loading: true });
      
      const userMsg = {
        id: Date.now(),
        type: 'user',
        content: question
      };
      
      const newMessages = [...this.data.messages, userMsg];
      const newCount = this.data.msgCount + 1;
      
      this.setData({
        messages: newMessages,
        scrollId: 'msg-' + this.data.msgCount,
        msgCount: newCount
      });
      
      // 使用流式接口
      this.fetchAnswerStream(question);
    },
    
    // 流式获取答案
    fetchAnswerStream(question) {
      const that = this;
      const botMsgId = Date.now();
      const botMsg = {
        id: botMsgId,
        type: 'bot',
        content: '',
        products: []
      };
      
      // 添加空的机器人消息
      const newMessages = [...this.data.messages, botMsg];
      const newCount = this.data.msgCount + 1;
      
      this.setData({
        messages: newMessages,
        isStreaming: true,
        currentStreamMsgId: botMsgId,
        loading: false,
        msgCount: newCount,
        scrollId: 'msg-' + this.data.msgCount
      });
      
      // 使用 wx.request 配合 enableChunked 实现流式接收
      const requestTask = wx.request({
        url: 'http://localhost:8083/user/ai-customer/ask/stream',
        method: 'GET',
        data: { question: question },
        timeout: 60000,
        enableChunked: true,  // 启用分块传输
        success: (res) => {
          // 流结束时的处理
          that.setData({
            isStreaming: false,
            currentStreamMsgId: null
          });
        },
        fail: (err) => {
          console.error('流式请求失败:', err);
          that.setData({
            isStreaming: false,
            currentStreamMsgId: null
          });
          // 使用兜底规则
          that.handleFallback(question, botMsgId);
        }
      });
      
      // 监听分块数据
      requestTask.onChunkReceived((res) => {
        // 将 ArrayBuffer 转换为字符串
        const uint8Array = new Uint8Array(res.data);
        const chunk = that.arrayBufferToString(uint8Array);
        
        // 解析 SSE 格式的数据
        that.parseSSEChunk(chunk, botMsgId);
      });
    },
    
    // ArrayBuffer 转字符串（支持UTF-8中文）
    arrayBufferToString(buffer) {
      // 使用 TextDecoder 正确解码UTF-8
      const decoder = new TextDecoder('utf-8');
      return decoder.decode(buffer);
    },
    
    // 解析 SSE 数据块
    parseSSEChunk(chunk, botMsgId) {
      // SSE 格式: data: {...}\n\n
      const lines = chunk.split('\n');
      
      for (const line of lines) {
        const trimmed = line.trim();
        if (trimmed.startsWith('data: ')) {
          try {
            const jsonStr = trimmed.substring(6); // 去掉 "data: "
            const data = JSON.parse(jsonStr);
            
            this.handleStreamData(data, botMsgId);
          } catch (e) {
            console.error('解析SSE数据失败:', e, line);
          }
        }
      }
    },
    
    // 处理流式数据
    handleStreamData(data, botMsgId) {
      const messages = this.data.messages;
      const msgIndex = messages.findIndex(m => m.id === botMsgId);
      
      if (msgIndex === -1) return;
      
      const msg = messages[msgIndex];
      
      switch (data.type) {
        case 'text':
          // 追加文本
          msg.content += data.content || '';
          break;
        case 'products':
          // 设置商品推荐
          msg.products = data.products || [];
          break;
        case 'end':
          // 流结束
          this.setData({
            isStreaming: false,
            currentStreamMsgId: null
          });
          break;
        case 'error':
          // 错误
          msg.content = data.content || '服务异常，请稍后重试';
          this.setData({
            isStreaming: false,
            currentStreamMsgId: null
          });
          break;
      }
      
      // 更新消息
      messages[msgIndex] = msg;
      this.setData({
        messages: messages,
        scrollId: 'msg-' + (msgIndex + 1)  // +1 因为有欢迎消息
      });
    },
    
    // 兜底规则（流式失败时使用）
    handleFallback(question, botMsgId = null) {
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
      
      // 如果有传入botMsgId，更新该消息；否则创建新消息
      if (botMsgId) {
        const messages = this.data.messages;
        const msgIndex = messages.findIndex(m => m.id === botMsgId);
        if (msgIndex !== -1) {
          messages[msgIndex].content = answer;
          messages[msgIndex].products = products;
          this.setData({ messages });
          return;
        }
      }
      
      // 创建新消息
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
