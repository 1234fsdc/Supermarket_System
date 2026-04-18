package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {

        /**
         * 思路：
         * 1. 先判断当前加入到购物车的商品是否已经存在
         * 2. 如果已经存在，只需要数量加一,并更新购物车数据
         * 3. 如果不存在，需要插入一条购物车数据
         *    3.1 先判断添加的是套餐还是菜品
         *    3.2 如果是的是菜品，需要根据菜品id查询菜品信息
         *    3.3 如果是的是套餐，需要根据套餐id查询套餐信息
         */

        //判断当前加入到购物车的商品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //获取当前登录用户的id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor() != null ? shoppingCartDTO.getDishFlavor() : "");
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //如果已经存在，只需要数量加一,并更新购物车数据
        if (list != null && list.size() > 0) {
            // list 是一个查询结果列表，里面包含了用户购物车中已存在的同一种商品的信息
            ShoppingCart cart = list.get(0);
            //在原先的数量基础上 +1
            cart.setNumber(cart.getNumber() + 1);
            //根据购物车id更新购物车数据
            shoppingCartMapper.updateNumberById(cart);
        }else{
            //如果不存在，需要插入一条购物车数据
            //先判断添加的是套餐还是菜品
            Long dishId = shoppingCartDTO.getDishId();
            if(dishId != null){
                //添加的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setDishId(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else{
                //添加的是套餐
                //根据套餐id查询套餐信息
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setSetmealId(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查询购物车商品
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        //获取当前登录用户的id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();   
        return shoppingCartMapper.list(shoppingCart);

    }

    /**
     * 清空购物车商品
     */
    @Override
    public void cleanShoppingCart() {
        //获取当前登录用户的id
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
    */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        /**
         * 思路：
            - 创建 ShoppingCart对象，将 shoppingCartDTO 中的属性拷贝到新对象中
            - 设置当前登录用户ID作为查询条件，确保只查询当前用户的购物车数据
            - 查询购物车中是否存在该商品
            - 通过 list != null && list.size() > 0 判断商品是否存在
            - 如果数量等于1 ：直接删除该商品记录
            - 如果数量大于1 ：将商品数量减1并更新数据库
         */
        //创建 ShoppingCart对象，将 shoppingCartDTO 中的属性拷贝到新对象中
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //获取当前登录用户的id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        // 处理口味字段，避免null导致SQL条件不匹配
        if (shoppingCart.getDishFlavor() == null) {
            shoppingCart.setDishFlavor("");
        }
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list != null && list.size() > 0){
            shoppingCart= list.get(0);
            Integer number = shoppingCart.getNumber();
            if(number == 1){
                //如果数量等于1，直接删除该商品记录
                shoppingCartMapper.deleteById(shoppingCart.getId());
            }else{
                //如果数量大于1，将商品数量减1并更新数据库
                shoppingCart.setNumber(number - 1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            }
            return ;
        }
    }
}
