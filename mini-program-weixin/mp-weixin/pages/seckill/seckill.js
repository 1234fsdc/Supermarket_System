Page({
  data: {
    productList: [],
    loading: false,
    scrollHeight: 0,
    countdown: {
      hours: '00',
      minutes: '00',
      seconds: '00'
    },
    countdownTimer: null
  },

  onLoad() {
    this.calculateScrollHeight();
    this.loadSeckillProducts();
    this.startCountdown();
  },

  onUnload() {
    if (this.data.countdownTimer) {
      clearInterval(this.data.countdownTimer);
    }
  },

  calculateScrollHeight() {
    const systemInfo = wx.getSystemInfoSync();
    const navHeight = 88;
    const headerHeight = 120;
    this.setData({
      scrollHeight: systemInfo.windowHeight - navHeight - headerHeight
    });
  },

  loadSeckillProducts() {
    this.setData({ loading: true });

    // 模拟数据，对应数据库中的秒杀商品
    const mockData = [
      {
        id: 1,
        name: '农夫山泉水溶C100',
        description: '清新口感，补充维生素C',
        seckillPrice: 0.50,
        originalPrice: 4.50,
        seckillStock: 35,
        soldCount: 15,
        limitPerUser: 2,
        image: ''
      },
      {
        id: 2,
        name: '可口可乐2L',
        description: '经典口味，畅爽无限',
        seckillPrice: 0.50,
        originalPrice: 7.00,
        seckillStock: 20,
        soldCount: 30,
        limitPerUser: 2,
        image: ''
      },
      {
        id: 3,
        name: '小米锅巴',
        description: '香脆可口，休闲零食',
        seckillPrice: 1.00,
        originalPrice: 5.00,
        seckillStock: 0,
        soldCount: 30,
        limitPerUser: 2,
        image: ''
      },
      {
        id: 4,
        name: '达利园软面包',
        description: '松软香甜，早餐首选',
        seckillPrice: 0.50,
        originalPrice: 6.00,
        seckillStock: 15,
        soldCount: 25,
        limitPerUser: 2,
        image: ''
      }
    ];

    setTimeout(() => {
      this.setData({
        productList: mockData,
        loading: false
      });
    }, 300);
  },

  startCountdown() {
    // 设置活动结束时间为当天23:59:59
    const now = new Date();
    const endTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59);
    
    const updateCountdown = () => {
      const current = new Date();
      const diff = endTime - current;
      
      if (diff <= 0) {
        this.setData({
          countdown: { hours: '00', minutes: '00', seconds: '00' }
        });
        clearInterval(this.data.countdownTimer);
        return;
      }
      
      const hours = Math.floor(diff / (1000 * 60 * 60));
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((diff % (1000 * 60)) / 1000);
      
      this.setData({
        countdown: {
          hours: hours.toString().padStart(2, '0'),
          minutes: minutes.toString().padStart(2, '0'),
          seconds: seconds.toString().padStart(2, '0')
        }
      });
    };
    
    updateCountdown();
    const timer = setInterval(updateCountdown, 1000);
    this.setData({ countdownTimer: timer });
  },

  buyNow(e) {
    const productId = e.currentTarget.dataset.id;
    const product = this.data.productList.find(item => item.id === productId);
    
    if (!product || product.seckillStock <= 0) {
      wx.showToast({
        title: '商品已售罄',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({ title: '抢购中...' });
    
    // 模拟抢购请求
    setTimeout(() => {
      wx.hideLoading();
      
      // 更新库存
      const list = this.data.productList.map(item => {
        if (item.id === productId) {
          return {
            ...item,
            seckillStock: item.seckillStock - 1,
            soldCount: item.soldCount + 1
          };
        }
        return item;
      });
      
      this.setData({ productList: list });
      
      wx.showToast({
        title: '抢购成功',
        icon: 'success'
      });
    }, 800);
  },

  goBack() {
    wx.navigateBack();
  }
});
