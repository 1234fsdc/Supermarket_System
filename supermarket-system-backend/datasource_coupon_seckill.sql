-- ============================================================
-- 优惠券系统 + 限时秒杀活动 数据库表结构
-- ============================================================

-- ----------------------------
-- 1. 优惠券模板表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `coupon_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '优惠券模板ID',
  `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '优惠券类型：1满减券 2折扣券 3新人券',
  `discount_type` TINYINT NOT NULL DEFAULT 1 COMMENT '优惠方式：1固定金额 2百分比折扣',
  `discount_value` DECIMAL(10,2) NOT NULL COMMENT '优惠值：满减券为减免金额，折扣券为折扣率(0.85表示85折)',
  `min_spend` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最低消费金额',
  `max_discount` DECIMAL(10,2) DEFAULT NULL COMMENT '最大优惠金额（折扣券用）',
  `total_count` INT NOT NULL DEFAULT 0 COMMENT '发放总量：0表示不限量',
  `remain_count` INT NOT NULL DEFAULT 0 COMMENT '剩余数量',
  `limit_per_user` INT NOT NULL DEFAULT 1 COMMENT '每人限领数量',
  `valid_days` INT DEFAULT NULL COMMENT '领取后有效天数（null表示按固定时间）',
  `start_time` DATETIME DEFAULT NULL COMMENT '有效期开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '有效期结束时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  `scope_type` TINYINT NOT NULL DEFAULT 1 COMMENT '适用范围：1全场通用 2指定分类 3指定商品',
  `scope_ids` VARCHAR(500) DEFAULT NULL COMMENT '适用范围ID列表，逗号分隔',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_user` BIGINT DEFAULT NULL,
  `update_user` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_type_status` (`type`, `status`),
  KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

-- ----------------------------
-- 2. 用户优惠券表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户优惠券ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券模板ID',
  `coupon_name` VARCHAR(100) NOT NULL COMMENT '优惠券名称（快照）',
  `coupon_type` TINYINT NOT NULL COMMENT '优惠券类型（快照）',
  `discount_value` DECIMAL(10,2) NOT NULL COMMENT '优惠值（快照）',
  `min_spend` DECIMAL(10,2) NOT NULL COMMENT '最低消费（快照）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1未使用 2已使用 3已过期 4已作废',
  `receive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间',
  `order_id` BIGINT DEFAULT NULL COMMENT '使用的订单ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_coupon` (`coupon_id`),
  KEY `idx_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- ----------------------------
-- 3. 订单优惠券关联表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `order_coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `user_coupon_id` BIGINT NOT NULL COMMENT '用户优惠券ID',
  `coupon_name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
  `discount_amount` DECIMAL(10,2) NOT NULL COMMENT '优惠金额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order` (`order_id`),
  KEY `idx_user_coupon` (`user_coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单优惠券关联表';

-- ----------------------------
-- 4. 秒杀活动表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `seckill_activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `name` VARCHAR(100) NOT NULL COMMENT '活动名称',
  `start_time` DATETIME NOT NULL COMMENT '活动开始时间',
  `end_time` DATETIME NOT NULL COMMENT '活动结束时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0未开始 1进行中 2已结束 3已停用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_user` BIGINT DEFAULT NULL,
  `update_user` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_time` (`start_time`, `end_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

-- ----------------------------
-- 5. 秒杀商品表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `seckill_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '秒杀商品ID',
  `activity_id` BIGINT NOT NULL COMMENT '活动ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
  `seckill_stock` INT NOT NULL DEFAULT 0 COMMENT '秒杀库存',
  `sold_count` INT NOT NULL DEFAULT 0 COMMENT '已售数量',
  `limit_per_user` INT NOT NULL DEFAULT 1 COMMENT '每人限购数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_product` (`activity_id`, `product_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- ----------------------------
-- 6. 订单表扩展：增加优惠券相关字段
-- ----------------------------
ALTER TABLE `orders` 
ADD COLUMN `coupon_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠券抵扣金额' AFTER `amount`,
ADD COLUMN `original_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '订单原价（优惠前）' AFTER `coupon_amount`;

-- ----------------------------
-- 初始化测试数据：优惠券模板
-- ----------------------------
INSERT INTO `coupon_template` (`name`, `type`, `discount_type`, `discount_value`, `min_spend`, `max_discount`, `total_count`, `remain_count`, `limit_per_user`, `valid_days`, `start_time`, `end_time`, `status`, `scope_type`) VALUES
('新用户专享满10减5', 3, 1, 5.00, 10.00, NULL, 1000, 1000, 1, 7, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 1),
('全场满30减8', 1, 1, 8.00, 30.00, NULL, 500, 500, 2, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), 1, 1),
('周末折扣券8.5折', 2, 2, 0.85, 20.00, 15.00, 200, 200, 1, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1, 1),
('会员满50减15', 1, 1, 15.00, 50.00, NULL, 300, 300, 1, 14, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 1, 1);

-- ----------------------------
-- 初始化测试数据：秒杀活动
-- ----------------------------
INSERT INTO `seckill_activity` (`name`, `start_time`, `end_time`, `status`) VALUES
('每日限时秒杀', DATE_ADD(CURDATE(), INTERVAL 0 HOUR), DATE_ADD(CURDATE(), INTERVAL 23 HOUR + 59 MINUTE), 1);

-- ----------------------------
-- 初始化测试数据：秒杀商品（关联现有商品）
-- ----------------------------
INSERT INTO `seckill_product` (`activity_id`, `product_id`, `seckill_price`, `seckill_stock`, `sold_count`, `limit_per_user`, `status`) VALUES
(1, 17, 0.50, 50, 0, 2, 1),  -- 农夫山泉水溶C100
(1, 20, 0.50, 50, 0, 2, 1),  -- 可口可乐2L
(1, 28, 1.00, 30, 0, 2, 1),  -- 小米锅巴
(1, 37, 0.50, 40, 0, 2, 1);  -- 达利园软面包
