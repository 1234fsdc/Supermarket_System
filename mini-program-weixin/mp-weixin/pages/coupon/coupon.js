Page({
  data: {
    activeTab: 0,
    couponList: [],
    loading: false,
    scrollHeight: 0
  },

  onLoad() {
    this.calculateScrollHeight();
    this.loadCouponList();
  },

  onShow() {
    this.loadCouponList();
  },

  calculateScrollHeight() {
    const systemInfo = wx.getSystemInfoSync();
    const navHeight = 88; // rpx
    const tabHeight = 88; // rpx
    const windowHeight = systemInfo.windowHeight;
    this.setData({
      scrollHeight: windowHeight - navHeight - tabHeight
    });
  },

  // 切换标签
  switchTab(e) {
    const index = parseInt(e.currentTarget.dataset.index);
    this.setData({ activeTab: index });
    this.loadCouponList();
  },

  // 加载优惠券列表
  loadCouponList() {
    this.setData({ loading: true });
    
    // 模拟数据，实际应调用后端接口
    const mockData = [
      {
        id: 1,
        name: '新用户专享满10减5',
        type: 3,
        typeName: '新人券',
        discountValue: 5,
        minSpend: 10,
        remainCount: 100,
        endTime: '2026-06-27',
        scopeType: 1,
        received: false
      },
      {
        id: 2,
        name: '全场满30减8',
        type: 1,
        typeName: '满减券',
        discountValue: 8,
        minSpend: 30,
        remainCount: 50,
        endTime: '2026-06-15',
        scopeType: 1,
        received: false
      },
      {
        id: 3,
        name: '周末折扣券8.5折',
        type: 2,
        typeName: '折扣券',
        discountValue: 8.5,
        minSpend: 20,
        remainCount: 30,
        endTime: '2026-06-10',
        scopeType: 1,
        received: true
      },
      {
        id: 4,
        name: '会员满50减15',
        type: 1,
        typeName: '满减券',
        discountValue: 15,
        minSpend: 50,
        remainCount: 0,
        endTime: '2026-07-27',
        scopeType: 1,
        received: false
      }
    ];

    // 根据标签筛选
    let filteredList = mockData;
    if (this.data.activeTab === 1) {
      filteredList = mockData.filter(item => item.type === 1);
    } else if (this.data.activeTab === 2) {
      filteredList = mockData.filter(item => item.type === 2);
    } else if (this.data.activeTab === 3) {
      filteredList = mockData.filter(item => item.type === 3);
    }

    setTimeout(() => {
      this.setData({
        couponList: filteredList,
        loading: false
      });
    }, 300);
  },

  // 领取优惠券
  receiveCoupon(e) {
    const couponId = e.currentTarget.dataset.id;
    const coupon = this.data.couponList.find(item => item.id === couponId);
    
    if (!coupon || coupon.received || coupon.remainCount <= 0) {
      return;
    }

    wx.showLoading({ title: '领取中...' });
    
    // 模拟领取请求
    setTimeout(() => {
      wx.hideLoading();
      
      // 更新本地状态
      const list = this.data.couponList.map(item => {
        if (item.id === couponId) {
          return { ...item, received: true, remainCount: item.remainCount - 1 };
        }
        return item;
      });
      
      this.setData({ couponList: list });
      
      wx.showToast({
        title: '领取成功',
        icon: 'success'
      });
    }, 500);
  },

  // 返回上一页
  goBack() {
    wx.navigateBack();
  }
});
