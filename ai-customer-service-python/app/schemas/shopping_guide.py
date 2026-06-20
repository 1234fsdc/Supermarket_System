"""
智能导购数据模型模块

为什么创建这个文件：
- 定义导购服务相关的数据模型（DTO/VO）
- 定义导购类型枚举，区分不同导购场景
- 标准化Java后端商品数据的表示

怎么做的：
- 使用Pydantic的BaseModel定义模型
- 使用枚举类定义导购类型
"""

from enum import Enum
from typing import List, Optional
from pydantic import BaseModel


class GuideType(str, Enum):
    """导购类型枚举"""
    CATEGORY_RECOMMEND = "category_recommend"      # 分类商品推荐
    MULTI_TURN_GUIDE = "multi_turn_guide"           # 多轮对话导购
    SEARCH_SUGGEST = "search_suggest"               # 搜索联想推荐
    PRICE_COMPARE = "price_compare"                 # 比价/性价比推荐


class ProductInfo(BaseModel):
    """
    商品信息模型
    
    对应Java后端 ProductVO 的核心字段
    """
    id: int                                          # 商品ID
    name: str                                        # 商品名称
    price: float                                     # 商品价格
    image: str = ""                                  # 商品图片URL
    description: str = ""                            # 商品描述
    category_id: int = 0                             # 分类ID
    category_name: str = ""                          # 分类名称
    sales_volume: int = 0                            # 销量
    stock: int = 0                                   # 库存
    rating: float = 0.0                              # 评分
    promo_tag: str = ""                              # 促销标签
    unit: str = "件"                                 # 单位


class GuideResult(BaseModel):
    """
    导购结果模型
    
    封装导购服务的完整响应，包含导购类型、商品列表和引导消息
    """
    guide_type: GuideType                            # 导购类型
    products: List[ProductInfo] = []                  # 推荐商品列表
    guide_message: str = ""                          # 导购引导消息（如"为您找到以下商品"）
    query: str = ""                                  # 用户的原始查询
