Page({
  data: {
    activeTab: 1,
    couponList: [],
    loading: false,
    scrollHeight: 0,
    unusedCount: 0
  },

  onLoad() {
    this.calculateScrollHeight();
    this.loadMyCoupons();
  },

  onShow() {
    this.loadMyCoupons();
  },

  calculateScrollHeight() {
    const systemInfo = wx.getSystemInfoSync();
    const navHeight = 88;
    const tabHeight = 88;
    this.setData({
      scrollHeight: systemInfo.windowHeight - navHeight - tabHeight
    });
  },

  switchTab(e) {
    const index = parseInt(e.currentTarget.dataset.index);
    this.setData({ activeTab: index });
    this.loadMyCoupons();
  },

  loadMyCoupons() {
    this.setData({ loading: true });

    // 模拟数据
    const mockData = [
      {
        id: 1,
        couponName: '新用户专享满10减5',
        couponType: 3,
        typeName: '新人券',
        discountValue: 5,
        minSpend: 10,
        status: 1,
        statusName: '未使用',
        expireTime: '2026-06-27',
        scopeName: '全场通用'
      },
      {
        id: 2,
        couponName: '全场满30减8',
        couponType: 1,
        typeName: '满减券',
        discountValue: 8,
        minSpend: 30,
        status: 1,
        statusName: '未使用',
        expireTime: '2026-06-15',
        scopeName: '全场通用'
      },
      {
        id: 3,
        couponName: '周末折扣券8.5折',
        couponType: 2,
        typeName: '折扣券',
        discountValue: 8.5,
        minSpend: 20,
        status: 2,
        statusName: '已使用',
        expireTime: '2026-06-10',
        scopeName: '全场通用'
      },
      {
        id: 4,
        couponName: '会员满50减15',
        couponType: 1,
        typeName: '满减券',
        discountValue: 15,
        minSpend: 50,
        status: 3,
        statusName: '已过期',
        expireTime: '2026-05-20',
        scopeName: '全场通用'
      }
    ];

    const filteredList = mockData.filter(item => item.status === this.data.activeTab);
    const unusedCount = mockData.filter(item => item.status === 1).length;

    setTimeout(() => {
      this.setData({
        couponList: filteredList,
        unusedCount: unusedCount,
        loading: false
      });
    }, 300);
  },

  useCoupon(e) {
    const couponId = e.currentTarget.dataset.id;
    wx.showToast({
      title: '跳转首页使用',
      icon: 'none'
    });
    setTimeout(() => {
      wx.switchTab({
        url: '/pages/index/index'
      });
    }, 1000);
  },

  goCouponCenter() {
    wx.navigateTo({
      url: '/pages/coupon/coupon'
    });
  },

  goBack() {
    wx.navigateBack();
  }
});
