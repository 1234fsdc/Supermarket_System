package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 客服问答结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerServiceResult implements Serializable {

    private String answer;

    private List<ProductRecommend> products;

    /**
     * 推荐商品
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRecommend implements Serializable {
        private String name;
        private Double price;
        private String desc;
    }
}
