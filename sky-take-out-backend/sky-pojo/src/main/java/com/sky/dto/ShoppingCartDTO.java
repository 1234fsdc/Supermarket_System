package com.sky.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 购物车数据传输对象
 */
@Data
public class ShoppingCartDTO implements Serializable {

    private Long productId;

    private String dishFlavor;
}
