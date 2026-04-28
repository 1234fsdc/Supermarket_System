package com.sky.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor  
public class OrderSubmitDTO {
    private Long id;
    private String orderNumber;
    private BigDecimal orderAmount;
    private LocalDateTime orderTime;
}
