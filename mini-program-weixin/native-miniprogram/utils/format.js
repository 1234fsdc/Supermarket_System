/**
 * 格式化工具：价格、时间、倒计时、相对时间
 */

/**
 * 格式化价格：1234.5 → ¥12.35（保留两位）
 */
function formatPrice(price) {
  const num = Number(price) || 0
  return '¥' + num.toFixed(2)
}

/**
 * 价格数字部分（不含单位），用于大字号价格
 */
function formatPriceNumber(price) {
  const num = Number(price) || 0
  return num.toFixed(2)
}

/**
 * 订单状态文案映射
 */
function formatOrderStatus(status) {
  const map = {
    1: '待付款',
    2: '待接单',
    3: '已接单',
    4: '派送中',
    5: '已完成',
    6: '已取消',
    7: '退款'
  }
  return map[status] || '未知'
}

/**
 * 订单状态 tag 类型映射（CSS class 名）
 */
function orderStatusTagClass(status) {
  return `tag-status-${status || 1}`
}

/**
 * 支付方式文案
 */
function formatPayMethod(method) {
  if (method === 1) return '微信支付'
  if (method === 2) return '支付宝'
  return '其他'
}

/**
 * 性别文案
 */
function formatSex(sex) {
  if (sex === '0' || sex === 0) return '先生'
  if (sex === '1' || sex === 1) return '女士'
  return ''
}

/**
 * 地址标签文案
 */
function formatAddressLabel(label) {
  const map = { home: '家', company: '公司', school: '学校' }
  return map[label] || (label || '')
}

/**
 * 优惠券状态文案
 */
function formatCouponStatus(status) {
  const map = { 1: '未使用', 2: '已使用', 3: '已过期', 4: '已作废' }
  return map[status] || '未知'
}

/**
 * 计算倒计时字符串
 * @param {Number} endTime - 结束时间戳（毫秒）或 Date
 * @returns {String} HH:MM:SS
 */
function formatCountdown(endTime) {
  const end = typeof endTime === 'number' ? endTime : new Date(endTime).getTime()
  const now = Date.now()
  let diff = Math.floor((end - now) / 1000)
  if (diff <= 0) return '00:00:00'
  const h = Math.floor(diff / 3600)
  const m = Math.floor((diff % 3600) / 60)
  const s = diff % 60
  const pad = n => String(n).padStart(2, '0')
  return `${pad(h)}:${pad(m)}:${pad(s)}`
}

/**
 * 相对时间：几分钟前 / 几小时前 / 几天前
 */
function formatRelativeTime(date) {
  if (!date) return ''
  const t = typeof date === 'number' ? date : new Date(date.replace(/-/g, '/')).getTime()
  const diff = Math.floor((Date.now() - t) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  if (diff < 86400 * 7) return `${Math.floor(diff / 86400)}天前`
  // 否则显示 yyyy-MM-dd
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/**
 * 标准时间字符串
 */
function formatDateTime(date) {
  if (!date) return ''
  const d = new Date(typeof date === 'string' ? date.replace(/-/g, '/') : date)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function pad(n) { return String(n).padStart(2, '0') }

/**
 * 默认图片占位
 */
function getImage(url) {
  if (!url) return '/static/imgDefault.png'
  return url
}

module.exports = {
  formatPrice,
  formatPriceNumber,
  formatOrderStatus,
  orderStatusTagClass,
  formatPayMethod,
  formatSex,
  formatAddressLabel,
  formatCouponStatus,
  formatCountdown,
  formatRelativeTime,
  formatDateTime,
  getImage
}