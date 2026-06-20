"""
SSE streaming guide_card event tests.

These tests verify that the ``/user/ai-customer/ask/stream`` endpoint emits
``guide_card`` SSE events in the correct sequence relative to ``text`` and
``end`` events.

All external dependencies are mocked at the ``get_agent()`` level so no
real LLM, MongoDB, or Java backend is required.
"""
import sys
import types

# ---------------------------------------------------------------------------
# Pre-import shim for ``langchain.text_splitter``
# ---------------------------------------------------------------------------
if "langchain.text_splitter" not in sys.modules:
    _ts_stub = types.ModuleType("langchain.text_splitter")
    _ts_stub.RecursiveCharacterTextSplitter = type(
        "RecursiveCharacterTextSplitter", (), {}
    )
    sys.modules["langchain.text_splitter"] = _ts_stub

import json
import pytest
from unittest.mock import MagicMock, AsyncMock, PropertyMock, patch

from fastapi import FastAPI
from fastapi.testclient import TestClient

from app.schemas.shopping_guide import GuideResult, GuideType, ProductInfo
from app.schemas.customer_service import StreamChunk


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_product(
    product_id: int = 1,
    name: str = "薯片",
    price: float = 5.5,
) -> ProductInfo:
    """Build a fully-populated ``ProductInfo`` for assertions."""
    return ProductInfo(
        id=product_id,
        name=name,
        price=price,
        image=f"http://img.example.com/{product_id}.jpg",
        description=f"测试商品 {name}",
        category_id=1,
        category_name="零食",
        sales_volume=100,
        stock=50,
        rating=4.5,
        promo_tag="热卖",
        unit="袋",
    )


def _parse_sse_events(response_text: str) -> list[dict]:
    """Parse SSE ``data: ...`` lines into a list of JSON dicts.

    Handles the ``\\n\\n`` delimiter between SSE events.
    """
    events = []
    for block in response_text.strip().split("\n\n"):
        block = block.strip()
        if not block:
            continue
        for line in block.split("\n"):
            line = line.strip()
            if line.startswith("data: "):
                payload = line[6:]  # strip "data: " prefix
                try:
                    events.append(json.loads(payload))
                except json.JSONDecodeError:
                    pytest.fail(f"Invalid SSE JSON payload: {payload}")
    return events


# ---------------------------------------------------------------------------
# Tests
# ---------------------------------------------------------------------------

class TestStreamGuideCard:
    """SSE guide_card event ordering and content."""

    @pytest.fixture(autouse=True)
    def _setup_app(self):
        """Create a fresh FastAPI app with the ai_customer router mounted."""
        # Must import after the langchain.text_splitter shim is in place
        from app.api.v1.ai_customer import router
        self.app = FastAPI()
        self.app.include_router(router, prefix="/user/ai-customer")
        self.client = TestClient(self.app)

    # --- mock agent builders ------------------------------------------------

    def _build_mock_agent(self, guide_result):
        """Build a mock agent that yields tokens and returns *guide_result*."""
        agent = MagicMock()
        agent.last_guide_result = guide_result

        async def fake_stream(*args, **kwargs):
            for token in ["为", "您", "推荐"]:
                yield token

        agent.execute_stream = fake_stream
        return agent

    def _build_mock_agent_error(self):
        """Build a mock agent whose ``execute_stream`` raises."""
        agent = MagicMock()
        agent.last_guide_result = None

        async def fake_stream(*args, **kwargs):
            raise RuntimeError("LLM timeout")

        agent.execute_stream = fake_stream
        return agent

    # --- tests --------------------------------------------------------------

    def test_emits_guide_card_after_text_before_end(self):
        """
        Given a guide result with products, the SSE stream must emit
        ``text → guide_card → end`` in that order.
        """
        products = [_make_product()]
        guide_result = GuideResult(
            guide_type=GuideType.CATEGORY_RECOMMEND,
            products=products,
            guide_message="为您推荐以下商品",
        )
        mock_agent = self._build_mock_agent(guide_result)

        with patch("app.api.v1.ai_customer.get_agent", return_value=mock_agent):
            resp = self.client.get(
                "/user/ai-customer/ask/stream",
                params={"question": "推荐零食", "session_id": "test"},
            )

        assert resp.status_code == 200
        events = _parse_sse_events(resp.text)

        assert len(events) >= 3, f"Expected at least 3 events, got {len(events)}"

        # Event 0: text
        # Verify ordering: text* → guide_card → end
        event_types = [e["type"] for e in events]
        assert event_types.count("text") == 3  # 3 tokens
        assert event_types.count("guide_card") == 1
        assert event_types.count("end") == 1

        # Find and validate guide_card
        guide_events = [e for e in events if e.get("type") == "guide_card"]
        assert len(guide_events) == 1
        gc = guide_events[0]
        assert gc["guide_type"] == "category_recommend"
        assert gc["guide_message"] == "为您推荐以下商品"
        assert len(gc["products"]) == 1
        assert gc["products"][0]["name"] == "薯片"
        assert gc["products"][0]["price"] == 5.5

        # guide_card must come before end
        end_idx = event_types.index("end")
        guide_idx = event_types.index("guide_card")
        assert guide_idx < end_idx, "guide_card must appear before end"

        # Last event must be end
        assert events[-1]["type"] == "end"

    def test_no_guide_card_when_result_is_none(self):
        """
        When ``last_guide_result`` is ``None`` (non-shopping query), the
        SSE stream emits only ``text → end`` without a ``guide_card`` event.
        """
        mock_agent = self._build_mock_agent(guide_result=None)

        with patch("app.api.v1.ai_customer.get_agent", return_value=mock_agent):
            resp = self.client.get(
                "/user/ai-customer/ask/stream",
                params={"question": "配送费多少钱", "session_id": "test"},
            )

        assert resp.status_code == 200
        events = _parse_sse_events(resp.text)

        assert len(events) >= 2

        # Must NOT contain guide_card
        event_types = [e["type"] for e in events]
        assert "guide_card" not in event_types, (
            f"Unexpected guide_card event: {events}"
        )
        assert events[-1]["type"] == "end"

    def test_empty_guide_card_when_products_empty(self):
        """
        When the backend returns a guide result with an empty products list,
        the endpoint must still emit ``guide_card`` with an empty array
        (so the frontend can show a "nothing found" state).
        """
        guide_result = GuideResult(
            guide_type=GuideType.SEARCH_SUGGEST,
            products=[],
            guide_message="没有找到相关商品",
        )
        mock_agent = self._build_mock_agent(guide_result)

        with patch("app.api.v1.ai_customer.get_agent", return_value=mock_agent):
            resp = self.client.get(
                "/user/ai-customer/ask/stream",
                params={"question": "搜索不存在的商品", "session_id": "test"},
            )

        assert resp.status_code == 200
        events = _parse_sse_events(resp.text)

        # Find guide_card event
        guide_events = [e for e in events if e.get("type") == "guide_card"]
        assert len(guide_events) == 1, "Expected exactly one guide_card event"
        assert guide_events[0]["products"] == []
        assert guide_events[0]["guide_message"] == "没有找到相关商品"

    def test_error_handling(self):
        """
        When ``execute_stream`` raises an exception, the SSE stream must
        emit an ``error`` event and NOT hang.
        """
        mock_agent = self._build_mock_agent_error()

        with patch("app.api.v1.ai_customer.get_agent", return_value=mock_agent):
            resp = self.client.get(
                "/user/ai-customer/ask/stream",
                params={"question": "推荐零食", "session_id": "test"},
            )

        assert resp.status_code == 200
        events = _parse_sse_events(resp.text)

        assert len(events) >= 1
        assert events[-1]["type"] == "error"
        assert "服务异常" in events[-1].get("content", "")

    def test_empty_question_returns_immediate_error(self):
        """
        An empty question must return an SSE ``error`` chunk immediately
        WITHOUT instantiating any agent.
        """
        from app.api.v1.ai_customer import get_agent

        # Spy: wrap the real get_agent to verify it is NOT called
        with patch("app.api.v1.ai_customer.get_agent") as mock_get_agent:
            resp = self.client.get(
                "/user/ai-customer/ask/stream",
                params={"question": "", "session_id": "test"},
            )

            # get_agent must NOT be called for empty questions
            mock_get_agent.assert_not_called()

        assert resp.status_code == 200
        events = _parse_sse_events(resp.text)

        assert len(events) == 1
        assert events[0]["type"] == "error"
        assert "请输入问题" in events[0].get("content", "")

    def test_response_headers(self):
        """Verify SSE response headers are correctly set."""
        mock_agent = self._build_mock_agent(None)

        with patch("app.api.v1.ai_customer.get_agent", return_value=mock_agent):
            resp = self.client.get(
                "/user/ai-customer/ask/stream",
                params={"question": "你好", "session_id": "test"},
            )

        assert resp.status_code == 200
        content_type = resp.headers.get("content-type", "")
        assert "text/event-stream" in content_type, (
            f"Expected text/event-stream, got {content_type}"
        )
