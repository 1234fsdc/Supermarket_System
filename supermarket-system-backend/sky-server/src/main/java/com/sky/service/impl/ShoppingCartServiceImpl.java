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
 *
 * 为什么创建这个类：
 * - 实现购物车相关的业务逻辑
 * - 处理购物车商品的增删改查
 * - 管理购物车商品数量的增减
 *
 * 怎么做的：
 * - 实现ShoppingCartService接口
 * - 使用@Autowired注入Mapper
 * - 使用BaseContext获取当前登录用户ID
 */
@Slf4j
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    /**
     * 购物车Mapper
     * 为什么：操作购物车数据表
     * 怎么做的：使用@Autowired注入
     */
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 商品Mapper
     * 为什么：查询商品信息（名称、价格、图片等）
     */
    @Autowired
    private ProductMapper productMapper;

    /**
     * 添加商品到购物车
     *
     * 为什么：用户将商品加入购物车
     * 怎么做的：
     * 1. 判断商品是否已在购物车中
     * 2. 如果存在，数量加1
     * 3. 如果不存在，查询商品信息并插入新记录
     *
     * @param shoppingCartDTO 购物车商品信息
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        // 判断当前加入到购物车的商品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        // 属性拷贝：将DTO中的属性复制到实体类
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        // 获取当前登录用户的id
        // 为什么：每个用户有自己的购物车，需要区分用户
        // 怎么做的：从BaseContext中获取当前线程的用户ID
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        // 处理口味字段，防止null值
        shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor() != null ? shoppingCartDTO.getDishFlavor() : "");

        // 查询购物车中是否已存在该商品
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

            // 设置初始数量为1
            shoppingCart.setNumber(1);
            // 设置创建时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查询购物车商品列表
     *
     * 为什么：用户查看购物车中的商品
     * 怎么做的：
     * 1. 获取当前用户ID
     * 2. 查询该用户的所有购物车商品
     *
     * @return List<ShoppingCart> 购物车商品列表
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        // 获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        // 构建查询条件
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        // 查询购物车列表
        return shoppingCartMapper.list(shoppingCart);
    }

    /**
     * 清空购物车
     *
     * 为什么：用户下单后或需要清空购物车
     * 怎么做的：删除当前用户的所有购物车记录
     */
    @Override
    public void cleanShoppingCart() {
        // 获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        // 删除该用户的所有购物车商品
        shoppingCartMapper.deleteByUserId(userId);
    }

    /**
     * 删除购物车中的一个商品
     *
     * 为什么：用户减少商品数量或删除商品
     * 怎么做的：
     * 1. 查询商品在购物车中的记录
     * 2. 如果数量等于1，删除记录
     * 3. 如果数量大于1，数量减1
     *
     * @param shoppingCartDTO 要操作的商品信息
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        // 获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        // 处理口味字段，防止null值
        if (shoppingCart.getDishFlavor() == null) {
            shoppingCart.setDishFlavor("");
        }

        // 查询购物车中的商品记录
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
