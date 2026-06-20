import request from '@/utils/request'

/**
 * 获取工作台概览数据
 * 包含：今日营业额、今日订单数、商品总数、员工总数、订单状态统计
 */
export function getDashboardOverview() {
  return request({
    url: '/dashboard/overview',
    method: 'get'
  })
}

/**
 * 获取数据统计报表
 * 包含：今日/昨日营业额、有效订单、完成率、客单价、环比趋势
 */
export function getStatisticsReport() {
  return request({
    url: '/dashboard/report',
    method: 'get'
  })
}

/**
 * 获取每日营业额趋势
 * @param {number} days - 查询天数
 */
export function getTurnoverTrend(days = 7) {
  return request({
    url: '/dashboard/turnoverTrend',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取每日订单数量趋势
 * @param {number} days - 查询天数
 */
export function getOrderCountTrend(days = 7) {
  return request({
    url: '/dashboard/orderCountTrend',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取订单状态统计
 */
export function getOrderStatistics() {
  return request({
    url: '/dashboard/orderStatistics',
    method: 'get'
  })
}
