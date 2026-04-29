"""
客服服务数据模型模块

为什么创建这个文件：
- 定义AI客服相关的数据模型（DTO/VO）
- 使用Pydantic进行数据校验和序列化
- 统一API的请求和响应格式

怎么做的：
- 使用Pydantic的BaseModel定义模型
- 使用TypeVar和Generic支持泛型
"""

from typing import List, Optional, TypeVar, Generic
from pydantic import BaseModel


class ProductRecommend(BaseModel):
    """
    推荐商品模型
    
    为什么：定义推荐商品的数据结构
    怎么做的：继承BaseModel，定义字段和类型
    """
    name: str  # 商品名称
    price: float  # 商品价格
    desc: str  # 商品描述


class CustomerServiceResult(BaseModel):
    """
    客服问答结果DTO
    
    为什么：统一客服问答接口的响应格式
    怎么做的：定义answer和可选的products字段
    """
    answer: str  # AI回答内容
    products: Optional[List[ProductRecommend]] = None  # 推荐商品列表（可选）


class StreamChunk(BaseModel):
    """
    流式输出块
    
    为什么：定义流式响应的数据块格式
    怎么做的：使用type字段区分不同类型的数据块
    
    type字段说明：
    - "text": 文本内容
    - "products": 商品推荐
    - "end": 结束标记
    - "error": 错误信息
    """
    type: str  # 数据块类型
    content: Optional[str] = None  # 文本内容（当type为text或error时）
    products: Optional[List[ProductRecommend]] = None  # 商品列表（当type为products时）


# 定义泛型类型变量
# 为什么：支持通用的响应结果封装
# 怎么做的：使用TypeVar定义类型变量
T = TypeVar('T')


class Result(BaseModel, Generic[T]):
    """
    统一响应结果封装
    
    为什么：统一所有API的响应格式，便于前端处理
    怎么做的：
    - 使用泛型支持不同类型的data字段
    - 提供静态方法快速创建成功/失败响应
    """
    code: int  # 状态码：1成功，0失败
    msg: Optional[str] = None  # 提示信息
    data: Optional[T] = None  # 数据内容

    @staticmethod
    def success(data: T = None, msg: str = "操作成功") -> "Result[T]":
        """
        创建成功响应
        
        为什么：提供便捷的成功响应创建方法
        怎么做的：设置code为1，传入data和msg
        
        Args:
            data: 响应数据
            msg: 成功提示信息
            
        Returns:
            Result[T]: 成功响应对象
        """
        return Result(code=1, msg=msg, data=data)

    @staticmethod
    def error(msg: str = "操作失败", code: int = 0) -> "Result[T]":
        """
        创建失败响应
        
        为什么：提供便捷的失败响应创建方法
        怎么做的：设置code为0（或指定错误码），传入msg
        
        Args:
            msg: 错误提示信息
            code: 错误码（默认为0）
            
        Returns:
            Result[T]: 失败响应对象
        """
        return Result(code=code, msg=msg, data=None)
