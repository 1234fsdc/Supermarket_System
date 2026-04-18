package com.sky.controller.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.result.Result;
import com.sky.service.AddressBookService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "C端地址簿接口")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询当前登录用户的所有地址信息
     *
     * @return
     */
    @GetMapping("list")
    @ApiOperation("查询当前登录用户的所有地址信息")
    public Result<List<AddressBook>> getAddressBookList() {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(userId);
        List<AddressBook> addressBookList = addressBookService.selectList(addressBook);
        return Result.success(addressBookList);
    }
    
    /**
     * 新增地址
     *
     * @param addressBook
     * @return
     */
    @PostMapping
    @ApiOperation("新增地址")
    public Result addAddressBook(@RequestBody AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        addressBookService.insert(addressBook);
        return Result.success();
    }

    /**
     * 根据地址id查询地址
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据地址id查询地址") 
    public Result<AddressBook> getAddressBookById(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = addressBookService.selectAddressById(id);
        return Result.success(addressBook);
    }
     /**
     * 根据id修改地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping
    @ApiOperation("根据地址id修改地址")
    //addressBook是请求参数而不是id是因为：
    // 只是根据id寻找需要修改的地址，然后再去更新地址、手机号、收件人姓名等信息
    public Result updateAddressBook(@RequestBody AddressBook addressBook){
        Long userId = BaseContext.getCurrentId();
        addressBookService.updateById(addressBook);
        return Result.success();
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefaultAddress(@RequestBody AddressBook addressBook){
        addressBookService.setDefaultAddress(addressBook);
        return Result.success();
    }

     /**
     * 根据id删除地址
     *
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result deleteAddressBook(@RequestParam Long id){
        addressBookService.deleteById(id);
        return Result.success();
    }

        /**
     * 查询默认地址
     */
    @GetMapping("default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefaultAddress(){
        /**
         * 思路 
            创建查询条件对象
            把 isDefault=1（默认地址）、userId=当前登录用户ID 封装进 AddressBook 对象。
            调用 service 进行条件查询
            根据上面两个条件，查询符合的地址列表。
            判断结果
            如果查到 1 条 数据 → 返回这条默认地址
            没查到 或 查到多条 → 返回 “没有查询到默认地址”
         */
        AddressBook addressBook = new AddressBook();
        List<AddressBook> addressBookList = addressBookService.selectList(addressBook);
        addressBook.setIsDefault(1);
        addressBook.setUserId(BaseContext.getCurrentId());
        if(addressBookList != null && addressBookList.size() == 1){
            addressBook = addressBookList.get(0);
            return Result.success(addressBook);
        }
        return Result.error("没有查询到默认地址");
    }
}
