export const MOCK_LOGIN_RESPONSE = {
  code: 1,
  data: {
    id: 1,
    userName: 'admin',
    name: '管理员',
    token: 'mock-jwt-token-xxxxx',
    role: 'admin'
  }
}

export const MOCK_LOGIN_FAILURE = {
  code: 0,
  msg: '用户名或密码错误'
}

export const MOCK_DASHBOARD = {
  code: 1,
  data: {
    todayTurnover: '15860.50',
    validOrders: 42,
    completionRate: '95.2',
    avgOrderAmount: '377.63'
  }
}

export const MOCK_ORDER_STATISTICS = {
  code: 1,
  data: {
    totalCount: 200,
    pendingCount: 15,
    confirmedCount: 8,
    deliveredCount: 12,
    completedCount: 158,
    cancelledCount: 7
  }
}

export const MOCK_PRODUCT_PAGE = {
  code: 1,
  data: {
    total: 3,
    records: [
      { id: 1, name: '测试商品A', categoryName: '饮品', price: 25.00, status: 1, stock: 100, unit: '份', image: '', description: '测试描述A' },
      { id: 2, name: '测试商品B', categoryName: '零食', price: 15.50, status: 1, stock: 200, unit: '袋', image: '', description: '测试描述B' },
      { id: 3, name: '测试商品C', categoryName: '饮品', price: 8.00, status: 0, stock: 0, unit: '瓶', image: '', description: '测试描述C' }
    ]
  }
}

export const MOCK_ORDER_PAGE = {
  code: 1,
  data: {
    total: 2,
    records: [
      { id: 1001, orderNumber: '20260530001', amount: 88.50, status: 5, userName: '张三', phone: '13800138001', address: '测试地址1', orderTime: '2026-05-30 10:00:00', remark: '' },
      { id: 1002, orderNumber: '20260530002', amount: 156.00, status: 2, userName: '李四', phone: '13800138002', address: '测试地址2', orderTime: '2026-05-30 11:00:00', remark: '尽快配送' }
    ]
  }
}

export const MOCK_ORDER_DETAIL = {
  code: 1,
  data: {
    id: 1001,
    orderNumber: '20260530001',
    amount: 88.50,
    status: 5,
    userName: '张三',
    phone: '13800138001',
    address: '测试地址1',
    orderTime: '2026-05-30 10:00:00',
    remark: '',
    orderDetails: [
      { id: 1, productName: '测试商品A', quantity: 2, amount: 50.00 },
      { id: 2, productName: '测试商品B', quantity: 1, amount: 38.50 }
    ]
  }
}

export const MOCK_CATEGORY_PAGE = {
  code: 1,
  data: {
    total: 3,
    records: [
      { id: 1, name: '饮品', type: 1, sort: 1, status: 1 },
      { id: 2, name: '零食', type: 1, sort: 2, status: 1 },
      { id: 3, name: '日用品', type: 1, sort: 3, status: 0 }
    ]
  }
}

export const MOCK_EMPLOYEE_PAGE = {
  code: 1,
  data: {
    total: 2,
    records: [
      { id: 1, username: 'admin', name: '管理员', phone: '13800000001', sex: '1', idNumber: '110101199001011234', status: 1, role: 'admin' },
      { id: 2, username: 'clerk', name: '收银员', phone: '13800000002', sex: '0', idNumber: '110101199001011235', status: 1, role: 'clerk' }
    ]
  }
}

export const MOCK_SHOP_STATUS = {
  code: 1,
  data: 1
}

export const MOCK_COUPON_PAGE = {
  code: 1,
  data: {
    total: 2,
    records: [
      { id: 1, name: '满100减20', type: 1, discountType: 1, discountValue: 20.00, minSpend: 100.00, totalCount: 500, status: 1, validDays: 30 },
      { id: 2, name: '全场8折', type: 1, discountType: 2, discountValue: 20.00, minSpend: 0, maxDiscount: 50.00, totalCount: 1000, status: 0, validDays: 7 }
    ]
  }
}
