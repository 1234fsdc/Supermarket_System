import request from '@/utils/request'

export function categoryPageQuery(params) {
  return request({
    url: '/category/page',
    method: 'get',
    params
  })
}

export function addCategory(data) {
  return request({
    url: '/category',
    method: 'post',
    data
  })
}

export function updateCategory(data) {
  return request({
    url: '/category',
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: '/category',
    method: 'delete',
    params: { id }
  })
}

export function enableOrDisableCategory(params) {
  return request({
    url: `/category/status/${params.status}`,
    method: 'post',
    params: { id: params.id }
  })
}

export function getCategoryList() {
  return request({
    url: '/category/list',
    method: 'get'
  })
}
