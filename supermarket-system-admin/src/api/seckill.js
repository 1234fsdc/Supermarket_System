import request from '@/utils/request'

// 分页查询秒杀活动
export function getSeckillPage(params) {
  return request({
    url: '/admin/seckill/activity/page',
    method: 'get',
    params
  })
}

// 创建秒杀活动
export function createSeckillActivity(data) {
  return request({
    url: '/admin/seckill/activity',
    method: 'post',
    data
  })
}

// 更新秒杀活动
export function updateSeckillActivity(data) {
  return request({
    url: '/admin/seckill/activity',
    method: 'put',
    data
  })
}

// 删除秒杀活动
export function deleteSeckillActivity(id) {
  return request({
    url: `/admin/seckill/activity/${id}`,
    method: 'delete'
  })
}

// 查询进行中的秒杀活动
export function getInProgressActivities() {
  return request({
    url: '/admin/seckill/activity/inProgress',
    method: 'get'
  })
}

// 添加秒杀商品
export function addSeckillProduct(data) {
  return request({
    url: '/admin/seckill/product',
    method: 'post',
    data
  })
}

// 更新秒杀商品
export function updateSeckillProduct(data) {
  return request({
    url: '/admin/seckill/product',
    method: 'put',
    data
  })
}

// 删除秒杀商品
export function deleteSeckillProduct(id) {
  return request({
    url: `/admin/seckill/product/${id}`,
    method: 'delete'
  })
}

// 根据活动ID查询秒杀商品
export function getSeckillProductsByActivity(activityId) {
  return request({
    url: `/admin/seckill/product/${activityId}`,
    method: 'get'
  })
}
