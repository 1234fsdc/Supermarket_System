"""
智能导购服务模块

与 Java 后端商品 API 通信，搜索 / 分类 / 详情商品信息。
Java 后端使用 camelCase 字段，Python 端使用 snake_case 字段（ProductInfo），
因此在解析时需要做一次 key 转换。

TDD 实现：最小可用实现，用以满足 tests/test_shopping_guide_service.py 的测试合约。
"""
import logging
from typing import List, Optional

import httpx

from app.schemas.shopping_guide import ProductInfo


logger = logging.getLogger(__name__)


# Java 后端 camelCase -> Python snake_case
_CAMEL_TO_SNAKE = {
    "categoryId": "category_id",
    "categoryName": "category_name",
    "salesVolume": "sales_volume",
    "promoTag": "promo_tag",
}


def _to_snake(item: dict) -> dict:
    """将 camelCase 的商品 dict 转为 ProductInfo 接受的 snake_case keys"""
    return {_CAMEL_TO_SNAKE.get(k, k): v for k, v in item.items()}


class ShoppingGuideService:
    """智能导购服务 - 负责调用 Java 后端商品接口"""

    def __init__(self, base_url: str = "http://localhost:8080", timeout: float = 10.0):
        self.base_url = base_url
        self._client = httpx.AsyncClient(base_url=base_url, timeout=timeout)

    async def aclose(self) -> None:
        await self._client.aclose()

    async def _get_payload(self, path: str, params: dict) -> dict:
        """发起 GET 请求并返回 JSON 字典；非 2xx 会抛 HTTPError"""
        response = await self._client.get(path, params=params)
        response.raise_for_status()
        return response.json()

    async def search_products(self, keyword: str) -> List[ProductInfo]:
        try:
            payload = await self._get_payload(
                "/user/product/search", {"keyword": keyword}
            )
        except (httpx.ConnectError, httpx.ReadTimeout, httpx.HTTPError) as e:
            logger.warning("search_products 网络异常: %s", e)
            return []

        if payload.get("code") != 1:
            logger.warning("search_products 业务异常: %s", payload.get("msg"))
            return []

        data = payload.get("data") or []
        return [ProductInfo(**_to_snake(item)) for item in data]

    async def get_category_products(self, category_id: int) -> List[ProductInfo]:
        try:
            payload = await self._get_payload(
                "/user/product/list", {"categoryId": category_id}
            )
        except (httpx.ConnectError, httpx.ReadTimeout, httpx.HTTPError) as e:
            logger.warning("get_category_products 网络异常: %s", e)
            return []

        if payload.get("code") != 1:
            logger.warning("get_category_products 业务异常: %s", payload.get("msg"))
            return []

        data = payload.get("data") or []
        return [ProductInfo(**_to_snake(item)) for item in data]

    async def get_product_detail(self, product_id: int) -> Optional[ProductInfo]:
        try:
            payload = await self._get_payload(
                "/user/product/detail", {"id": product_id}
            )
        except (httpx.ConnectError, httpx.ReadTimeout, httpx.HTTPError) as e:
            logger.warning("get_product_detail 网络异常: %s", e)
            return None

        if payload.get("code") != 1:
            logger.warning("get_product_detail 业务异常: %s", payload.get("msg"))
            return None

        item = payload.get("data")
        if not item:
            return None
        return ProductInfo(**_to_snake(item))
