package com.sky.service.impl;

import com.sky.mapper.AddressBookMapper;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.service.AddressBookService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 查询当前登录用户的所有地址信息
     * @param addressBook
     * @return
     */
    @Override
    public List<AddressBook> selectList(AddressBook addressBook) {

        List<AddressBook> addressBookList = addressBookMapper.selectList(addressBook.getUserId());
        return addressBookList;
    }
    
    /**
     * 新增地址
     * @param addressBook
     */
    @Override
    public void insert(AddressBook addressBook) {
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }
    
    /**
     * 根据地址id查询地址
     * @param id
     * @return
     */
    @Override
    public AddressBook selectAddressById(Long id) {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = new AddressBook();
        addressBook.setId(id);
        addressBook.setUserId(userId);
        AddressBook addressBookResult = addressBookMapper.selectAddressById(addressBook);
        
        return addressBookResult;
    }
    
    /**
     * 根据地址id修改地址
     * @param addressBook
     */
    @Override
    public void updateById(AddressBook addressBook) {
        addressBookMapper.updateById(addressBook);   
    }
    
    /**
     * 设置默认地址
     * @param addressBook
     */
    @Override
    public void setDefaultAddress(AddressBook addressBook) {
        /**
            先把当前用户的所有地址，全部改成非默认（0）
            再把当前要设置的这个地址，单独改成默认（1）
        */
        Long userId = BaseContext.getCurrentId();
        List<AddressBook> addressBookList = addressBookMapper.selectList(userId);
        for(AddressBook adddBook : addressBookList){
           adddBook.setIsDefault(0);
           addressBookMapper.updateById(adddBook);
        }
        addressBook.setIsDefault(1);
        addressBookMapper.updateById(addressBook);
    }
    
    /**
     * 根据id删除地址
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }
}
