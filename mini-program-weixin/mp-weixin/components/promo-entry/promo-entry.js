Component({
  methods: {
    goCoupon() {
      wx.navigateTo({
        url: '/pages/coupon/coupon'
      });
    },
    goSeckill() {
      wx.navigateTo({
        url: '/pages/seckill/seckill'
      });
    },
    goMyCoupon() {
      wx.navigateTo({
        url: '/pages/myCoupon/myCoupon'
      });
    }
  }
});
