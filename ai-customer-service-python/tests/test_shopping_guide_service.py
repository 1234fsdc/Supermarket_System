"""
Tests for ShoppingGuideService

TDD: 这些测试定义了 ShoppingGuideService 的预期 API。
- 构造函数: ShoppingGuideService(base_url="http://localhost:8080")
- 方法:
    - async search_products(keyword) -> List[ProductInfo]
    - async get_category_products(category_id) -> List[ProductInfo]
    - async get_product_detail(product_id) -> Optional[ProductInfo]

Java 后端约定:
- 响应包装: {"code": 1, "msg": "...", "data": [...]}
- ProductVO 使用 camelCase (categoryId, categoryName, salesVolume, promoTag)
- ProductInfo 使用 snake_case
"""
import logging
import pytest
import pytest_asyncio
import httpx
import respx

from app.schemas.shopping_guide import ProductInfo
from app.services.shopping_guide_service import ShoppingGuideService


BASE_URL = "http://localhost:8080"


# ---------- Helpers ----------

def _product_dict(
    product_id: int = 1,
    name: str = "测试商品",
    price: float = 9.9,
    category_id: int = 1,
    category_name: str = "零食",
) -> dict:
    """构造 Java 后端 ProductVO 格式（camelCase）的字典"""
    return {
        "id": product_id,
        "name": name,
        "price": price,
        "image": f"http://img.example.com/{product_id}.jpg",
        "description": f"测试商品 {name}",
        "categoryId": category_id,
        "categoryName": category_name,
        "salesVolume": 100,
        "stock": 50,
        "rating": 4.5,
        "promoTag": "热卖",
        "unit": "袋",
    }


def _ok_response(data):
    """构造业务成功响应"""
    return {"code": 1, "msg": "ok", "data": data}


def _error_response(msg: str = "error"):
    """构造业务失败响应"""
    return {"code": 0, "msg": msg, "data": None}


# ---------- Fixture ----------

@pytest_asyncio.fixture
async def service():
    """为每个测试提供独立的 ShoppingGuideService 实例"""
    svc = ShoppingGuideService(base_url=BASE_URL)
    yield svc
    await svc.aclose()


# ---------- search_products ----------

@pytest.mark.asyncio
@respx.mock
async def test_search_products_success(service):
    """search_products 成功时返回解析后的 ProductInfo 列表"""
    products = [
        _product_dict(1, "薯片", 5.5),
        _product_dict(2, "饼干", 8.0),
        _product_dict(3, "巧克力", 12.5, category_id=2, category_name="糖果"),
    ]
    respx.get(f"{BASE_URL}/user/product/search").mock(
        return_value=httpx.Response(200, json=_ok_response(products))
    )

    result = await service.search_products("零食")

    assert isinstance(result, list)
    assert len(result) == 3
    assert all(isinstance(p, ProductInfo) for p in result)

    # 验证 snake_case 字段正确解析
    assert result[0].id == 1
    assert result[0].name == "薯片"
    assert result[0].price == 5.5
    assert result[1].name == "饼干"
    assert result[2].category_id == 2
    assert result[2].category_name == "糖果"
    assert result[2].sales_volume == 100
    assert result[2].promo_tag == "热卖"


@pytest.mark.asyncio
@respx.mock
async def test_search_products_empty(service):
    """search_products 在 data 为空时返回空列表"""
    respx.get(f"{BASE_URL}/user/product/search").mock(
        return_value=httpx.Response(200, json=_ok_response([]))
    )

    result = await service.search_products("不存在的商品")

    assert result == []


@pytest.mark.asyncio
@respx.mock
async def test_search_products_error_status(service, caplog):
    """search_products 在 code != 1 时返回空列表并记录警告日志"""
    caplog.set_level(logging.WARNING, logger="app.services.shopping_guide_service")
    respx.get(f"{BASE_URL}/user/product/search").mock(
        return_value=httpx.Response(200, json=_error_response("服务异常"))
    )

    result = await service.search_products("零食")

    assert result == []
    # 验证警告日志被记录
    assert any(
        "服务异常" in record.getMessage() and record.levelno == logging.WARNING
        for record in caplog.records
    )


# ---------- get_category_products ----------

@pytest.mark.asyncio
@respx.mock
async def test_get_category_products(service):
    """get_category_products 按 categoryId 拉取并返回 ProductInfo 列表"""
    products = [
        _product_dict(10, "可乐", 3.0, category_id=1, category_name="饮料"),
        _product_dict(11, "雪碧", 3.0, category_id=1, category_name="饮料"),
    ]
    respx.get(f"{BASE_URL}/user/product/list").mock(
        return_value=httpx.Response(200, json=_ok_response(products))
    )

    result = await service.get_category_products(1)

    assert isinstance(result, list)
    assert len(result) == 2
    assert all(isinstance(p, ProductInfo) for p in result)
    assert result[0].name == "可乐"
    assert result[1].name == "雪碧"
    assert result[0].category_id == 1


# ---------- get_product_detail ----------

@pytest.mark.asyncio
@respx.mock
async def test_get_product_detail(service):
    """get_product_detail 返回单个 ProductInfo（不是列表）"""
    product = _product_dict(123, "限定款零食", 99.9, category_id=5, category_name="限定")
    respx.get(f"{BASE_URL}/user/product/detail").mock(
        return_value=httpx.Response(200, json=_ok_response(product))
    )

    result = await service.get_product_detail(123)

    assert isinstance(result, ProductInfo)
    assert result.id == 123
    assert result.name == "限定款零食"
    assert result.price == 99.9
    assert result.category_id == 5
    assert result.category_name == "限定"


# ---------- 异常容错 ----------

@pytest.mark.asyncio
@respx.mock
async def test_connection_error(service):
    """连接被拒绝时应优雅降级返回空列表"""
    respx.get(f"{BASE_URL}/user/product/search").mock(
        side_effect=httpx.ConnectError("Connection refused")
    )

    result = await service.search_products("零食")

    assert result == []


@pytest.mark.asyncio
@respx.mock
async def test_timeout_error(service):
    """读取超时应优雅降级返回空列表"""
    respx.get(f"{BASE_URL}/user/product/search").mock(
        side_effect=httpx.ReadTimeout("Request timed out")
    )

    result = await service.search_products("零食")

    assert result == []
