-- 超市系统数据库
CREATE DATABASE IF NOT EXISTS `sky_supermarket`;
USE `sky_supermarket`;

-- 用户地址簿
DROP TABLE IF EXISTS `address_book`;
CREATE TABLE `address_book` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `consignee` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人',
  `sex` varchar(2) COLLATE utf8_bin DEFAULT NULL COMMENT '性别',
  `phone` varchar(11) COLLATE utf8_bin NOT NULL COMMENT '手机号',
  `province_code` varchar(12) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '省级区划编号',
  `province_name` varchar(32) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '省级名称',
  `city_code` varchar(12) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '市级区划编号',
  `city_name` varchar(32) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '市级名称',
  `district_code` varchar(12) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '区级区划编号',
  `district_name` varchar(32) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '区级名称',
  `detail` varchar(200) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '详细地址',
  `label` varchar(100) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '标签',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '默认 0 否 1是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='地址簿';

-- 商品分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` int DEFAULT NULL COMMENT '类型 1商品分类',
  `name` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` int DEFAULT NULL COMMENT '分类状态 0:禁用，1:启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_category_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='商品分类';

-- 插入超市分类数据
INSERT INTO `category` VALUES (1,1,'日用百货',1,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (2,1,'水/饮料',2,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (3,1,'休闲食品',3,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (4,1,'早餐面包',4,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (5,1,'辣条肉食',5,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (6,1,'文具办公',6,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (7,1,'雪糕冰品',7,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (8,1,'旅行户外',8,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (9,1,'厨房用品',9,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (10,1,'美妆专区',10,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (11,1,'牛奶乳品',11,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (12,1,'体育玩具',12,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (13,1,'手机数码',13,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (14,1,'花卉园艺',14,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (15,1,'五金交电',15,1,NOW(),NOW(),1,1);
INSERT INTO `category` VALUES (16,1,'其他',16,1,NOW(),NOW(),1,1);

-- 商品表（原 dish 表改造）
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) COLLATE utf8_bin NOT NULL COMMENT '商品名称',
  `category_id` bigint NOT NULL COMMENT '商品分类id',
  `price` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
  `image` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '图片',
  `description` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '描述信息',
  `status` int DEFAULT '1' COMMENT '0 停售 1 起售',
  `sales_volume` int DEFAULT '0' COMMENT '销量',
  `rating` int DEFAULT '100' COMMENT '好评率(百分比)',
  `rebuy_count` int DEFAULT '0' COMMENT '近2个月回购人数',
  `promo_tag` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '促销标签',
  `unit` varchar(20) COLLATE utf8_bin DEFAULT NULL COMMENT '单位(支/袋/盒/瓶等)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=150 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='商品表';

-- 插入日用百货商品
INSERT INTO `product` VALUES (1,'宝娜斯 加绒丝袜肉色连裤袜光腿神器加绒打底裤春',1,5.69,'','',1,1200,98,326,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (2,'形象美 维生素E乳滋润保湿水嫩细滑 100ml/瓶',1,0.8,'','',1,850,99,185,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (3,'正彩凡士林成分护手霜小巧便携秋冬补水保湿',1,1.5,'','',1,620,97,142,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (4,'曼秀雷敦 薄荷润唇膏 3.5g/支 护唇膏SPF15男女通用',1,17.8,'','',1,450,99,98,NULL,'支',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (5,'VSEA香水柔润护手霜 1支/份便携留香',1,4.91,'','',1,380,96,85,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (6,'栀子花护手霜 30ml/袋 秋冬便携清爽手部',1,2.9,'','',1,520,95,118,NULL,'袋',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (7,'健美创研 唇膏凡士林润唇膏 3g/支 凡士林男女润唇',1,1.9,'','',1,680,97,156,NULL,'支',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (8,'强生 金盏花婴儿清润保湿霜 25克/袋',1,3.9,'','',1,290,98,67,NULL,'袋',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (9,'雕牌 全效加浓小瓶洗洁精洗涤灵去油清洁',1,1,'','',1,1500,96,425,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (10,'心相印 一次性洗脸巾洁面巾 50抽/包 抽取式',1,4.8,'','',1,780,99,215,NULL,'包',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (11,'超能 离子去油西柚洗涤剂 1kg/瓶',1,7.4,'','',1,350,97,89,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (12,'南孚电池 聚能环5号碱性电池 4节/卡',1,3.8,'','',1,920,99,268,NULL,'卡',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (13,'按压式清洁刷家用工具',1,3.5,'','',1,180,94,42,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (14,'超能 食品用白桃苏打洗洁精 1kg/瓶',1,5.3,'','',1,260,96,65,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (15,'手提式垃圾收纳袋45*50cm厚款抽绳',1,0.75,'','',1,2800,95,756,'爆好价','件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (16,'厨用塑料加厚大号垃圾纸篓',1,3.9,'','',1,420,93,98,NULL,'件',NOW(),NOW(),1,1);

-- 插入水/饮料商品
INSERT INTO `product` VALUES (17,'农夫山泉 水溶C100柠檬味 445ml',2,0.94,'','',1,1850,98,528,'爆好价','瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (18,'名仁 无糖无汽弱碱性苏打水饮料 375ml',2,2.9,'','',1,680,97,185,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (19,'农夫山泉 西柚味水溶C100 445ml',2,1.5,'','',1,920,96,256,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (20,'可口可乐 经典汽水大瓶家庭装 2L/瓶',2,0.79,'','',1,2200,99,685,'爆好价','瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (21,'怡宝 饮用纯净水 1.55L/瓶',2,3.5,'','',1,580,98,142,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (22,'雪碧 清爽柠檬味汽水 2L/瓶',2,1.45,'','',1,890,97,235,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (23,'可口可乐 汽水胖听碳酸饮料',2,7.4,'','',1,320,95,78,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (24,'农夫山泉 东方树叶 茉莉花茶 900ml/瓶',2,4.8,'','',1,450,99,125,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (25,'康师傅 每日C水蜜桃水',2,0.95,'','',1,780,96,198,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (26,'酷儿 橙汁饮料450ml/瓶',2,0.04,'','',1,3500,94,985,'爆好价','瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (27,'美汁源 果粒橙 450ml/瓶',2,0.5,'','',1,1200,97,356,NULL,'瓶',NOW(),NOW(),1,1);

-- 插入休闲食品商品
INSERT INTO `product` VALUES (28,'琥珀 小米锅巴麻辣味 90g/袋',3,2.65,'','',1,650,96,178,NULL,'袋',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (29,'洽洽 百煮入味五香瓜子 200g',3,0.44,'','',1,1800,98,526,'爆好价','袋',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (30,'卫龙 小面筋香辣味 24g/袋',3,1.35,'','',1,980,99,285,NULL,'袋',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (31,'Keittly 牛肉馅酥脆饼干 14g/袋',3,2.25,'','',1,420,95,98,NULL,'袋',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (32,'波力 海苔原味 4.5g/袋',3,1,'','',1,750,97,195,NULL,'袋',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (33,'山姆 韩国进口咸味海苔',3,1.65,'','',1,520,98,142,NULL,'袋',NOW(),NOW(),1,1);

-- 插入早餐面包商品
INSERT INTO `product` VALUES (34,'福乐回头 3+2紫米面包 75g/袋',4,3.5,'','',1,480,96,125,NULL,'袋',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (35,'奥利奥 橙香巧克力味夹心云朵蛋糕 44g/盒',4,4.5,'','',1,320,98,89,NULL,'盒',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (36,'达利园 法式小面包早餐代餐糕点',4,4.4,'','',1,560,97,156,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (37,'达利园 法式软面包香奶味 200g/袋',4,0.8,'','',1,1200,99,358,'爆好价','袋',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (38,'百草恋 轻甜椰蓉奶油厚芙面包',4,2.06,'','',1,380,95,95,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (39,'好丽友巧克力派2枚装 74g/盒',4,0.73,'','',1,1500,98,425,'爆好价','盒',NOW(),NOW(),1,1);

-- 插入辣条肉食商品
INSERT INTO `product` VALUES (40,'霸王丝 爆辣辣条 65g/袋',5,3.19,'','',1,850,97,235,NULL,'袋',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (41,'顺天缘 零食素牛筋袋装 30g/包',5,1,'','',1,620,96,168,NULL,'包',NOW(),NOW(),1,1);

-- 插入文具办公商品
INSERT INTO `product` VALUES (42,'得力 按压式圆珠笔',6,0.85,'','',1,450,95,125,NULL,'支',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (43,'迷你A7上翻线圈笔记本',6,2.5,'','',1,280,97,78,NULL,'本',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (44,'小米 巨能写黑色中性笔0.5mm 10支装',6,24.5,'','',1,180,99,52,NULL,'盒',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (45,'标签贴纸自粘性手写分类标签纸',6,0.5,'','',1,680,94,185,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (46,'A4复印纸打印纸',6,0.11,'','',1,2500,98,756,'爆好价','张',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (47,'晨光 K35按动中性笔0.5mm黑色',6,3,'','',1,520,99,145,NULL,'支',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (48,'晨光 k35按压中性笔黑色',6,3.56,'','',1,420,98,118,NULL,'支',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (49,'磁吸团徽徽章',6,2.5,'','',1,150,96,42,NULL,'件',NOW(),NOW(),1,1);

-- 插入雪糕冰品商品
INSERT INTO `product` VALUES (50,'草莓味雪糕',7,1.5,'','',1,680,96,185,NULL,'支',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (51,'和路雪 麦酷狮绿舌头苹果口味冰棍',7,3.15,'','',1,420,98,115,NULL,'支',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (52,'旺旺 乳酸菌味碎冰冰 78ml/袋',7,1.5,'','',1,580,95,158,NULL,'袋',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (53,'旺旺 草莓味碎冰冰吸吸冻棒冰',7,1.5,'','',1,520,96,142,NULL,'支',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (54,'伊利巧乐兹四个圈蛋奶巧克力味雪糕70g',7,0.9,'','',1,950,99,285,'爆好价','支',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (55,'梦龙松露巧克力味冰淇淋65g/支',7,1.5,'','',1,780,98,215,NULL,'支',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (56,'光明 白雪冰砖香草味冰淇淋115g/盒',7,0.5,'','',1,1200,97,356,'爆好价','盒',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (57,'祐康 大布丁奶油口味雪糕70g/支',7,1.5,'','',1,650,96,178,NULL,'支',NOW(),NOW(),1,1);

-- 插入旅行户外商品
INSERT INTO `product` VALUES (58,'一次性浴巾毛巾旅行便携',8,0.8,'','',1,850,97,235,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (59,'菠萝袜丝袜女防勾丝隐形光腿',8,0.5,'','',1,1200,95,356,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (60,'手动黑胶UV折叠晴雨伞',8,4.26,'','',1,320,98,89,NULL,'件',NOW(),NOW(),1,1);

-- 插入厨房用品商品
INSERT INTO `product` VALUES (61,'海天 鲜味生抽',9,4.68,'','',1,480,99,135,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (62,'海天 味极鲜酱油380ml/瓶',9,0.8,'','',1,950,98,268,'爆好价','瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (63,'四只装钢丝球家用洗锅清洁球',9,1.5,'','',1,620,96,172,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (64,'加厚铁丝卷边吐司面包包装袋',9,0.77,'','',1,380,94,98,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (65,'北欧风陶瓷圆盘菜盘子',9,2.68,'','',1,220,97,62,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (66,'4.5英寸黑边陶瓷日式米饭碗',9,0.5,'','',1,850,98,235,'爆好价','件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (67,'加厚铁丝卷边烘焙面包袋',9,0.5,'','',1,720,95,198,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (68,'不锈钢泡面碗套装',9,10.8,'','',1,180,99,52,NULL,'套',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (69,'家用硅胶带盖六格软底冰格',9,2.8,'','',1,280,96,78,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (70,'家用实木木筷子',9,2,'','',1,520,97,145,NULL,'双',NOW(),NOW(),1,1);

-- 插入美妆专区商品
INSERT INTO `product` VALUES (71,'无耳洞仿珍珠耳夹',10,0.8,'','',1,680,95,185,NULL,'对',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (72,'梦鹿 小雏菊免胶假睫毛',10,10,'','',1,220,98,62,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (73,'全棉时代 一次性洗脸巾80抽/包',10,8.9,'','',1,450,99,128,NULL,'包',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (74,'30簇免胶小奶狗浓密假睫毛',10,3.8,'','',1,320,96,89,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (75,'法式仿珍珠耳钉',10,0.9,'','',1,580,97,162,NULL,'对',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (76,'手持美甲灯光疗机',10,3.8,'','',1,180,94,52,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (77,'透明防堵耳棒耳钉',10,2,'','',1,420,96,118,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (78,'男士磁吸无耳洞耳夹',10,3.5,'','',1,150,95,42,NULL,'对',NOW(),NOW(),1,1);

-- 插入牛奶乳品商品
INSERT INTO `product` VALUES (79,'黄桃味乳味饮料',11,3.9,'','',1,380,96,105,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (80,'李子园 甜牛奶原味450ml/瓶',11,1.66,'','',1,680,98,192,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (81,'娃哈哈 营养快线水蜜桃味500g/瓶',11,0.95,'','',1,850,97,238,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (82,'李子园 草莓风味乳饮料450ml/瓶',11,1.4,'','',1,520,96,145,NULL,'瓶',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (83,'西域春 奶啤300ml/罐',11,0.49,'','',1,980,99,285,'爆好价','罐',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (84,'光明 纯牛奶250ml/盒',11,3.5,'','',1,420,99,118,NULL,'盒',NOW(),NOW(),1,1);

-- 插入体育玩具商品
INSERT INTO `product` VALUES (85,'黑色双线骰盅带5粒骰子',12,1.9,'','',1,280,95,78,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (86,'微颗粒卡通积木公仔',12,2.9,'','',1,520,97,145,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (87,'得力轻型粘土多色可选',12,5.15,'','',1,180,98,52,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (88,'卡通手账贴纸素材包',12,0.35,'','',1,1200,94,356,'爆好价','包',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (89,'合金回力迷你小汽车玩具',12,0.8,'','',1,850,96,238,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (90,'高密度泡沫瑜伽砖',12,4.8,'','',1,120,99,35,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (91,'免烧软陶泥diy粘土',12,3.3,'','',1,220,97,62,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (92,'木制三国华容道益智玩具',12,0.9,'','',1,680,98,192,NULL,'件',NOW(),NOW(),1,1);

-- 插入手机数码商品
INSERT INTO `product` VALUES (93,'Type-C充电数据线',13,3.8,'','',1,520,96,145,NULL,'条',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (94,'三合一多功能充电数据线1.2米',13,2.8,'','',1,680,97,192,NULL,'条',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (95,'双头Type-C快充数据线',13,3.8,'','',1,420,98,118,NULL,'条',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (96,'苹果双Type-C充电器线套装',13,16.8,'','',1,180,99,52,NULL,'套',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (97,'黑色游戏鼠标垫',13,1.5,'','',1,750,95,212,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (98,'6A超级快充Type-C数据线',13,1.8,'','',1,920,97,265,NULL,'条',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (99,'可折叠调节手机支架',13,0.99,'','',1,650,96,182,'爆好价','件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (100,'手机不锈钢取卡针',13,1,'','',1,420,94,118,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (101,'电动车手机导航支架',13,3.56,'','',1,280,98,78,NULL,'件',NOW(),NOW(),1,1);

-- 插入花卉园艺商品
INSERT INTO `product` VALUES (102,'北欧浮雕玻璃花瓶',14,0.9,'','',1,380,97,105,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (103,'室内绿萝盆栽',14,0.8,'','',1,520,98,145,NULL,'盆',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (104,'单支仿真尤加利叶',14,1.5,'','',1,420,96,118,NULL,'支',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (105,'家用多功能种花小锄头',14,7.8,'','',1,150,99,42,NULL,'件',NOW(),NOW(),1,1);

-- 插入五金交电商品
INSERT INTO `product` VALUES (106,'双头开口扳手',15,3.12,'','',1,180,95,52,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (107,'加厚防摔测量米尺',15,0.27,'','',1,620,97,175,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (108,'免打孔壁挂置物架',15,3,'','',1,280,98,78,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (109,'手工细铁丝25cm',15,0.3,'','',1,450,94,125,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (110,'60W家用电烙铁三件套',15,8.8,'','',1,120,99,35,NULL,'套',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (111,'可调节免打孔花洒支架',15,2.5,'','',1,320,96,89,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (112,'瓷砖找平矫正器',15,0.5,'','',1,580,95,162,NULL,'件',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (113,'多功能活动扳手',15,1.68,'','',1,220,97,62,NULL,'件',NOW(),NOW(),1,1);

-- 插入其他商品
INSERT INTO `product` VALUES (114,'海氏海诺透明防水创可贴',16,0.1,'','',1,2800,98,785,'爆好价','贴',NOW(),NOW(),1,1);
INSERT INTO `product` VALUES (115,'固态酒精块20粒/包',16,6.8,'','',1,180,96,52,NULL,'包',NOW(),NOW(),1,1);

-- 员工信息表
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '姓名',
  `username` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '用户名',
  `password` varchar(64) COLLATE utf8_bin NOT NULL COMMENT '密码',
  `phone` varchar(11) COLLATE utf8_bin NOT NULL COMMENT '手机号',
  `sex` varchar(2) COLLATE utf8_bin NOT NULL COMMENT '性别',
  `id_number` varchar(18) COLLATE utf8_bin NOT NULL COMMENT '身份证号',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0:禁用，1:启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='员工信息';

INSERT INTO `employee` VALUES (1,'管理员','admin','123456','13812312312','1','110101199001010047',1,'2022-02-15 15:51:20','2022-02-17 09:16:20',10,1);

-- 订单明细表
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '商品名称',
  `image` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '图片',
  `order_id` bigint NOT NULL COMMENT '订单id',
  `product_id` bigint DEFAULT NULL COMMENT '商品id',
  `dish_flavor` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '规格/口味',
  `number` int NOT NULL DEFAULT '1' COMMENT '数量',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='订单明细表';

-- 订单表
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `number` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '订单号',
  `status` int NOT NULL DEFAULT '1' COMMENT '订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款',
  `user_id` bigint NOT NULL COMMENT '下单用户',
  `address_book_id` bigint NOT NULL COMMENT '地址id',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `checkout_time` datetime DEFAULT NULL COMMENT '结账时间',
  `pay_method` int NOT NULL DEFAULT '1' COMMENT '支付方式 1微信,2支付宝',
  `pay_status` tinyint NOT NULL DEFAULT '0' COMMENT '支付状态 0未支付 1已支付 2退款',
  `amount` decimal(10,2) NOT NULL COMMENT '实收金额',
  `remark` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '备注',
  `phone` varchar(11) COLLATE utf8_bin DEFAULT NULL COMMENT '手机号',
  `address` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '地址',
  `user_name` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT '用户名称',
  `consignee` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人',
  `cancel_reason` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '订单取消原因',
  `rejection_reason` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '订单拒绝原因',
  `cancel_time` datetime DEFAULT NULL COMMENT '订单取消时间',
  `estimated_delivery_time` datetime DEFAULT NULL COMMENT '预计送达时间',
  `delivery_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '配送状态  1立即送出  0选择具体时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '送达时间',
  `pack_amount` int DEFAULT NULL COMMENT '打包费',
  `tableware_number` int DEFAULT NULL COMMENT '餐具数量',
  `tableware_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '餐具数量状态  1按餐量提供  0选择具体数量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='订单表';

-- 购物车表
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '商品名称',
  `image` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '图片',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `product_id` bigint DEFAULT NULL COMMENT '商品id',
  `dish_flavor` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '规格',
  `number` int NOT NULL DEFAULT '1' COMMENT '数量',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='购物车';

-- 用户信息表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` varchar(45) COLLATE utf8_bin DEFAULT NULL COMMENT '微信用户唯一标识',
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT '姓名',
  `phone` varchar(11) COLLATE utf8_bin DEFAULT NULL COMMENT '手机号',
  `sex` varchar(2) COLLATE utf8_bin DEFAULT NULL COMMENT '性别',
  `id_number` varchar(18) COLLATE utf8_bin DEFAULT NULL COMMENT '身份证号',
  `avatar` varchar(500) COLLATE utf8_bin DEFAULT NULL COMMENT '头像',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='用户信息';
