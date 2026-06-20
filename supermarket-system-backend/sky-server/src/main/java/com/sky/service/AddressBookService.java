package com.sky.service;

import java.util.List;

import com.sky.entity.AddressBook;

public interface AddressBookService {
    /**
     * 查询当前登录用户的所有地址信息
     * @param addressBook
     * @return
     */
    List<AddressBook> selectList(AddressBook addressBook);
    
    /**
     * 新增地址
     * @param addressBook
     */
    void insert(AddressBook addressBook);
    
    /**
     * 根据地址id查询地址
     * @param addressBook
     * @return
     */
    AddressBook selectAddressById(Long id);
    
    /**
     * 根据地址id修改地址
     * @param addressBook
     */
    void updateById(AddressBook addressBook);
    
    /**
     * 设置默认地址
     * @param addressBook
     */
    void setDefaultAddress(AddressBook addressBook);
    
    /**
     * 根据id删除地址
     * @param id
     */
    void deleteById(Long id);

}
