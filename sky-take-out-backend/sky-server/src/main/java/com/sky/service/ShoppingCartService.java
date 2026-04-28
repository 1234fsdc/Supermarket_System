package com.sky.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

public interface ShoppingCartService {

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查询购物车商品
     * @return
     */
    List<ShoppingCart> showShoppingCart();
    /**
     * 清空购物车商品
     */
    void cleanShoppingCart();
    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     */
    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
