const api = require('../../utils/api.js')

Page({
  data: {
    id: null,
    consignee: '',
    phone: '',
    sex: '0',
    provinceName: '北京市',
    cityName: '北京市',
    districtName: '朝阳区',
    detail: '',
    label: '',
    isDefault: 0
  },

  onLoad(query) {
    if (query.id) {
      this.setData({ id: query.id })
      this.loadDetail(query.id)
      wx.setNavigationBarTitle({ title: '编辑地址' })
    } else {
      wx.setNavigationBarTitle({ title: '新增地址' })
    }
  },

  async loadDetail(id) {
    try {
      const res = await api.getAddressById(id)
      if (res) this.setData(res)
    } catch (e) {}
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [field]: e.detail.value })
  },

  onSexChange(e) {
    this.setData({ sex: e.detail.value })
  },

  onLabelChange(e) {
    const v = e.currentTarget.dataset.label
    this.setData({ label: this.data.label === v ? '' : v })
  },

  onSwitchDefault(e) {
    this.setData({ isDefault: e.detail.value ? 1 : 0 })
  },

  onSave() {
    const { consignee, phone, detail, id, isDefault, label, sex, provinceName, cityName, districtName } = this.data
    if (!consignee) {
      wx.showToast({ title: '请输入收货人', icon: 'none' })
      return
    }
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '手机号格式错误', icon: 'none' })
      return
    }
    if (!detail) {
      wx.showToast({ title: '请输入详细地址', icon: 'none' })
      return
    }

    const data = {
      consignee, phone, sex, provinceName, cityName, districtName, detail, label, isDefault
    }
    if (id) data.id = id

    const action = id ? api.updateAddress(data) : api.addAddress(data)
    action.then(() => {
      // 如果是默认，先调默认接口
      if (isDefault === 1 && id) {
        return api.setDefaultAddress({ id })
      }
    }).then(() => {
      wx.showToast({ title: '保存成功' })
      setTimeout(() => wx.navigateBack(), 600)
    }).catch(() => {})
  }
})