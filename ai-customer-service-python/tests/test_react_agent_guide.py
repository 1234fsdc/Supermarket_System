"""
Integration tests for ReactAgent + ShoppingGuideService + IntentDetector.

These tests verify the wiring between three layers:

- ``ReactAgent``           - orchestrator (chain of LLM + RAG + guide)
- ``IntentDetector``       - classifies a query as shopping intent or not
- ``ShoppingGuideService`` - fetches products from the Java backend

All external dependencies are mocked:

- ``DASHSCOPE_API_KEY`` is set to a fake value so ``QwenChatModel`` can be
  constructed without a real credential. The real LLM is never called:
  ``agent.chain`` is replaced with a ``MagicMock`` whose ``ainvoke`` /
  ``astream`` methods we configure per test.
- ``get_mongo`` and ``HybridRetriever`` are patched so ``ReactAgent.__init__``
  does not need a real MongoDB, BM25 index, or Chroma store.

The intent detector runs its real implementation (pure keyword logic,
no I/O) and the schema models are real.
"""
import sys
import types

# ---------------------------------------------------------------------------
# Pre-import shim for ``langchain.text_splitter``
# ---------------------------------------------------------------------------
# ``app/core/__init__.py`` unconditionally imports from
# ``app.core.vectorstore``, which in turn does
# ``from langchain.text_splitter import RecursiveCharacterTextSplitter``.
# In langchain >= 1.0 that module was renamed to ``langchain_text_splitters``
# and the old import path no longer exists. We do not exercise the vector
# store in these tests, so we register a minimal stub in ``sys.modules``
# before the import chain runs. This keeps the fix localised to the test
# file - no production code is modified.
if "langchain.text_splitter" not in sys.modules:
    _ts_stub = types.ModuleType("langchain.text_splitter")
    _ts_stub.RecursiveCharacterTextSplitter = type(
        "RecursiveCharacterTextSplitter", (), {}
    )
    sys.modules["langchain.text_splitter"] = _ts_stub

import pytest
from unittest.mock import MagicMock, AsyncMock, PropertyMock

from agent.react_agent import ReactAgent
from app.schemas.shopping_guide import ProductInfo, GuideResult


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture(autouse=True)
def _mock_heavy_dependencies(monkeypatch):
    """
    Auto-mock the heavy dependencies ``ReactAgent.__init__`` touches.

    Without this fixture, ``ReactAgent(guide_service=...)`` would:

    1. raise ``ValueError("DashScope API密钥未配置")`` from
       ``QwenChatModel.__init__``;
    2. attempt a real MongoDB connection (3 s server-selection timeout);
    3. build a real BM25 / Chroma retriever.

    We patch each of those so the test can run in a pure-Python unit-test
    environment with no external services.
    """
    # 1. LLM credential: a fake key is enough to satisfy the constructor.
    monkeypatch.setenv("DASHSCOPE_API_KEY", "test-fake-key")

    # 2. MongoDB: hand back a MagicMock with safe default return values so
    #    ``_format_history`` / ``_save_to_history`` never iterate over a
    #    bare MagicMock (which can recurse on ``__iter__``).
    def _mock_mongo():
        m = MagicMock()
        m.get_history.return_value = []
        m.save_message.return_value = True
        return m

    # 3. HybridRetriever: return a MagicMock that yields an empty context.
    def _mock_hr(*args, **kwargs):
        hr = MagicMock()
        hr.get_context_string.return_value = ""
        return hr

    monkeypatch.setattr("agent.react_agent.get_mongo", _mock_mongo)
    monkeypatch.setattr("agent.react_agent.HybridRetriever", _mock_hr)


def _make_product(
    product_id: int = 1,
    name: str = "薯片",
    price: float = 5.5,
    category_id: int = 1,
    category_name: str = "零食",
) -> ProductInfo:
    """Build a fully-populated ``ProductInfo`` for assertions."""
    return ProductInfo(
        id=product_id,
        name=name,
        price=price,
        image=f"http://img.example.com/{product_id}.jpg",
        description=f"测试商品 {name}",
        category_id=category_id,
        category_name=category_name,
        sales_volume=100,
        stock=50,
        rating=4.5,
        promo_tag="热卖",
        unit="袋",
    )


# ---------------------------------------------------------------------------
# Tests
# ---------------------------------------------------------------------------

class TestReactAgentShoppingGuideIntegration:
    """ReactAgent <-> ShoppingGuideService <-> IntentDetector integration."""

    @pytest.mark.asyncio
    async def test_execute_shopping_intent_populates_guide(self):
        """
        A shopping-intent query triggers ``search_products`` and populates
        ``last_guide_result`` with the returned products and the detected
        ``guide_type``.
        """
        # ---- arrange ----
        products = [_make_product(1, "薯片", 5.5)]
        guide_service = MagicMock()
        guide_service.search_products = AsyncMock(return_value=products)

        agent = ReactAgent(guide_service=guide_service)
        agent.chain = MagicMock()
        agent.chain.ainvoke = AsyncMock(return_value="推荐以下商品")

        # ---- act ----
        answer = await agent.execute("推荐零食", session_id="test")

        # ---- assert: LLM side ----
        agent.chain.ainvoke.assert_awaited_once()
        assert answer == "推荐以下商品"

        # ---- assert: guide service side ----
        guide_service.search_products.assert_awaited_once_with("推荐零食")

        # ---- assert: last_guide_result is populated ----
        assert agent.last_guide_result is not None
        assert isinstance(agent.last_guide_result, GuideResult)
        assert len(agent.last_guide_result.products) == 1
        assert agent.last_guide_result.products[0].name == "薯片"
        assert agent.last_guide_result.guide_type is not None
        assert agent.last_guide_result.query == "推荐零食"

    @pytest.mark.asyncio
    async def test_execute_no_shopping_intent(self):
        """
        A non-shopping query (``"配送费多少钱"``) must NOT call the guide
        service, and ``last_guide_result`` must remain ``None``.
        """
        # ---- arrange ----
        guide_service = MagicMock()
        guide_service.search_products = AsyncMock(return_value=[])

        agent = ReactAgent(guide_service=guide_service)
        agent.chain = MagicMock()
        agent.chain.ainvoke = AsyncMock(return_value="配送费是6元")

        # ---- act ----
        answer = await agent.execute("配送费多少钱", session_id="test")

        # ---- assert: guide service NOT touched ----
        guide_service.search_products.assert_not_called()

        # ---- assert: LLM still runs (with empty guide_context) ----
        agent.chain.ainvoke.assert_awaited_once()
        assert answer == "配送费是6元"

        # ---- assert: no guide result ----
        assert agent.last_guide_result is None

    @pytest.mark.asyncio
    async def test_execute_stream_shopping_intent(self):
        """
        ``execute_stream`` with a shopping intent yields LLM chunks via
        pipeline mode and populates ``last_guide_result``.

        注：使用"好吃的"而非"推荐零食"是因为后者命中 ReAct 路由，
        而此测试验证的是管道模式的流式行为。
        """
        # ---- arrange ----
        products = [_make_product(2, "巧克力", 12.5, category_id=2, category_name="糖果")]
        guide_service = MagicMock()
        guide_service.search_products = AsyncMock(return_value=products)

        agent = ReactAgent(guide_service=guide_service)
        agent.chain = MagicMock()
        # 强制走 pipeline 模式（"好吃的" 可能被 _llm_classify 误判为 complex）
        agent._classify_query = AsyncMock(return_value="simple")

        async def fake_astream(payload):
            for token in ["为", "您", "推荐"]:
                yield token

        agent.chain.astream = fake_astream

        # ---- act ----
        chunks = []
        async for chunk in agent.execute_stream("好吃的", session_id="test"):
            chunks.append(chunk)

        # ---- assert: all chunks received in order ----
        assert chunks == ["为", "您", "推荐"]

        # ---- assert: guide service called ----
        guide_service.search_products.assert_awaited_once_with("好吃的")

        # ---- assert: last_guide_result is populated ----
        assert agent.last_guide_result is not None
        assert len(agent.last_guide_result.products) == 1
        assert agent.last_guide_result.products[0].name == "巧克力"
        assert agent.last_guide_result.guide_type is not None

    @pytest.mark.asyncio
    async def test_execute_stream_no_shopping_intent(self):
        """
        ``execute_stream`` with a non-shopping intent must NOT call the
        guide service and ``last_guide_result`` must remain ``None``.
        """
        # ---- arrange ----
        guide_service = MagicMock()
        guide_service.search_products = AsyncMock(return_value=[])

        agent = ReactAgent(guide_service=guide_service)
        agent.chain = MagicMock()

        async def fake_astream(payload):
            for token in ["营", "业", "时", "间", "9", ":", "0", "0"]:
                yield token

        agent.chain.astream = fake_astream

        # ---- act ----
        chunks = []
        async for chunk in agent.execute_stream("营业时间", session_id="test"):
            chunks.append(chunk)

        # ---- assert: chunks still streamed ----
        assert chunks == ["营", "业", "时", "间", "9", ":", "0", "0"]

        # ---- assert: guide service NOT touched ----
        guide_service.search_products.assert_not_called()

        # ---- assert: no guide result ----
        assert agent.last_guide_result is None

    @pytest.mark.asyncio
    async def test_guide_service_error_graceful_degradation(self):
        """
        When ``search_products`` raises ``ConnectionError`` (Java backend
        unreachable), the agent must NOT propagate the exception. It must
        still answer the user (with an empty ``guide_context``) and leave
        ``last_guide_result`` as ``None`` so the UI does not render an
        empty product card.
        """
        # ---- arrange ----
        guide_service = MagicMock()
        guide_service.search_products = AsyncMock(
            side_effect=ConnectionError("Java backend unreachable")
        )

        agent = ReactAgent(guide_service=guide_service)
        agent.chain = MagicMock()
        agent.chain.ainvoke = AsyncMock(return_value="暂时无法获取商品信息")

        # ---- act: must NOT raise ----
        answer = await agent.execute("推荐零食")

        # ---- assert: LLM still called (with empty guide_context) ----
        agent.chain.ainvoke.assert_awaited_once()
        assert answer == "暂时无法获取商品信息"

        # ---- assert: graceful degradation ----
        # ``_detect_and_fetch`` swallowed the exception and did NOT set
        # ``last_guide_result``, so it must remain None.
        assert agent.last_guide_result is None
