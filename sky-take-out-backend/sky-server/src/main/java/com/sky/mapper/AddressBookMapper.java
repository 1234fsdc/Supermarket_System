package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.AddressBook;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
    /**
     * 查询当前登录用户的所有地址信息
     * @param addressBook
     * @return
     */
    @Select("select * from address_book where user_id = #{userId}")
    List<AddressBook> selectList(@Param("userId") Long userId);
    
    /**
     * 根据地址id查询地址
     * @param addressBook
     * @return
     */
    @Select("select * from address_book where id = #{id} and user_id = #{userId}")
       AddressBook selectAddressById(AddressBook addressBook);

}
