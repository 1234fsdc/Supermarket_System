import request from '@/utils/request'

export function getShopStatus() {
  return request({
    url: '/shop/status',
    method: 'get'
  })
}

export function setShopStatus(status) {
  return request({
    url: `/shop/${status}`,
    method: 'put'
  })
}
