import request from '@/utils/request'

// 优惠券模板管理
export const getCouponTemplatePage = (params) => {
  return request({
    url: '/admin/coupon/template/page',
    method: 'get',
    params
  })
}

export const getCouponTemplateById = (id) => {
  return request({
    url: `/admin/coupon/template/${id}`,
    method: 'get'
  })
}

export const createCouponTemplate = (data) => {
  return request({
    url: '/admin/coupon/template',
    method: 'post',
    data
  })
}

export const updateCouponTemplate = (data) => {
  return request({
    url: '/admin/coupon/template',
    method: 'put',
    data
  })
}

export const updateCouponTemplateStatus = (id, status) => {
  return request({
    url: `/admin/coupon/template/status/${id}`,
    method: 'post',
    params: { status }
  })
}

export const deleteCouponTemplate = (id) => {
  return request({
    url: `/admin/coupon/template/${id}`,
    method: 'delete'
  })
}
