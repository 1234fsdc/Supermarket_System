from typing import List, Optional, TypeVar, Generic
from pydantic import BaseModel


class ProductRecommend(BaseModel):
    """推荐商品"""
    name: str
    price: float
    desc: str


class CustomerServiceResult(BaseModel):
    """客服问答结果DTO"""
    answer: str
    products: Optional[List[ProductRecommend]] = None


class StreamChunk(BaseModel):
    """流式输出块"""
    type: str  # "text" | "products" | "end" | "error"
    content: Optional[str] = None
    products: Optional[List[ProductRecommend]] = None


T = TypeVar('T')


class Result(BaseModel, Generic[T]):
    """统一响应结果"""
    code: int
    msg: Optional[str] = None
    data: Optional[T] = None

    @staticmethod
    def success(data: T = None, msg: str = "操作成功") -> "Result[T]":
        return Result(code=1, msg=msg, data=data)

    @staticmethod
    def error(msg: str = "操作失败", code: int = 0) -> "Result[T]":
        return Result(code=code, msg=msg, data=None)
