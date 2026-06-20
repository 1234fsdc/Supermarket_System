/**
 * API 接口集中导出
 * 所有后端 /user/** 端点
 * 后端实际端口：8080
 */
const { request } = require('./request')

// ===== 认证 =====
function login(code) {
  return request({ url: '/user/user/login', method: 'POST', data: { code } })
}

// ===== 店铺 =====
function getShopStatus() {
  return request({ url: '/user/shop/status' })
}

// ===== 分类 =====
function getCategoryList(type = 1) {
  return request({ url: '/user/category/list', params: { type } })
}

// ===== 商品 =====
function getProductList(categoryId) {
  return request({ url: '/user/product/list', params: { categoryId } })
}
function getProductDetail(id) {
  return request({ url: '/user/product/detail', params: { id } })
}
function searchProduct(keyword) {
  return request({ url: '/user/product/search', params: { keyword } })
}

// ===== 购物车 =====
function addCart(data) {
  return request({ url: '/user/shoppingCart/add', method: 'POST', data, showLoading: true, loadingText: '加入中...' })
}
function subCart(data) {
  return request({ url: '/user/shoppingCart/sub', method: 'POST', data, showLoading: true, loadingText: '处理中...' })
}
function getCartList() {
  return request({ url: '/user/shoppingCart/list' })
}
function clearCart() {
  return request({ url: '/user/shoppingCart/clean', method: 'DELETE', showLoading: true, loadingText: '清空中...' })
}

// ===== 地址 =====
function getAddressList() {
  return request({ url: '/user/addressBook/list' })
}
function getAddressById(id) {
  return request({ url: `/user/addressBook/${id}` })
}
function getDefaultAddress() {
  return request({ url: '/user/addressBook/default' })
}
function addAddress(data) {
  return request({ url: '/user/addressBook', method: 'POST', data, showLoading: true, loadingText: '保存中...' })
}
function updateAddress(data) {
  return request({ url: '/user/addressBook', method: 'PUT', data, showLoading: true, loadingText: '保存中...' })
}
function setDefaultAddress(data) {
  return request({ url: '/user/addressBook/default', method: 'PUT', data })
}
function deleteAddress(id) {
  return request({ url: '/user/addressBook', method: 'DELETE', params: { id } })
}

// ===== 订单 =====
function submitOrder(data) {
  return request({ url: '/user/order/submit', method: 'POST', data, showLoading: true, loadingText: '提交中...' })
}
function payOrder(orderId) {
  return request({ url: '/user/order/payment', method: 'POST', data: orderId, showLoading: true, loadingText: '支付中...' })
}
function getOrderHistory(page, pageSize, status) {
  return request({ url: '/user/order/historyOrders', params: { page, pageSize, status } })
}
function getOrderDetail(id) {
  return request({ url: `/user/order/orderDetail/${id}` })
}
function cancelOrder(id) {
  return request({ url: `/user/order/cancel/${id}`, method: 'PUT', showLoading: true, loadingText: '取消中...' })
}
function reorder(id) {
  return request({ url: `/user/order/repetition/${id}`, method: 'POST', showLoading: true, loadingText: '加入购物车中...' })
}

// ===== 优惠券 =====
function receiveCoupon(couponId) {
  return request({ url: `/user/coupon/receive/${couponId}`, method: 'POST', showLoading: true, loadingText: '领取中...' })
}
function getMyCoupons(status) {
  return request({ url: '/user/coupon/list', params: { status } })
}
function getUsableCoupons() {
  return request({ url: '/user/coupon/usable' })
}
function calculateCoupon(userCouponId, orderAmount) {
  return request({ url: '/user/coupon/calculate', params: { userCouponId, orderAmount } })
}
/**
 * 领券中心：查询可领取的优惠券模板列表
 * 新增：用于 coupon 页替代原来的 /admin/couponTemplate/page
 */
function getAvailableCouponTemplates() {
  return request({ url: '/user/coupon/templates' })
}

// ===== 秒杀 =====
function getSeckillActivities() {
  return request({ url: '/user/seckill/activities' })
}
function seckillBuy(productId) {
  return request({ url: `/user/seckill/buy/${productId}`, method: 'POST', showLoading: true, loadingText: '抢购中...' })
}
function seckillResult(productId) {
  return request({ url: `/user/seckill/result/${productId}` })
}

module.exports = {
  login, getShopStatus,
  getCategoryList,
  getProductList, getProductDetail, searchProduct,
  addCart, subCart, getCartList, clearCart,
  getAddressList, getAddressById, getDefaultAddress, addAddress, updateAddress, setDefaultAddress, deleteAddress,
  submitOrder, payOrder, getOrderHistory, getOrderDetail, cancelOrder, reorder,
  receiveCoupon, getMyCoupons, getUsableCoupons, calculateCoupon, getAvailableCouponTemplates,
  getSeckillActivities, seckillBuy, seckillResult
}