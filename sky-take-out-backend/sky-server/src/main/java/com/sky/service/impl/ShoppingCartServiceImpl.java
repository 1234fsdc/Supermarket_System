package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Product;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.ProductMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车服务实现类
 */
@Slf4j
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 添加购物车
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        // 判断当前加入到购物车的商品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        // 获取当前登录用户的id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor() != null ? shoppingCartDTO.getDishFlavor() : "");

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // 如果已经存在，只需要数量加一
        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        } else {
            // 如果不存在，需要插入一条购物车数据
            Long productId = shoppingCartDTO.getProductId();
            if (productId != null) {
                // 根据商品id查询商品信息
                Product product = productMapper.selectById(productId);
                shoppingCart.setProductId(productId);
                shoppingCart.setName(product.getName());
                shoppingCart.setImage(product.getImage());
                shoppingCart.setAmount(product.getPrice());
            }

            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查询购物车商品
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
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
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    /**
     * 删除购物车中一个商品
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        if (shoppingCart.getDishFlavor() == null) {
            shoppingCart.setDishFlavor("");
        }

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list != null && !list.isEmpty()) {
            shoppingCart = list.get(0);
            Integer number = shoppingCart.getNumber();
            if (number == 1) {
                // 如果数量等于1，直接删除该商品记录
                shoppingCartMapper.deleteById(shoppingCart.getId());
            } else {
                // 如果数量大于1，将商品数量减1
                shoppingCart.setNumber(number - 1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            }
        }
    }
}
