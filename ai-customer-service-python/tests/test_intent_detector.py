"""
Tests for IntentDetector - Shopping Intent Detection

TDD approach: this file defines the expected behavior of
``agent.intent_detector``. The module should be implemented to make
these tests pass.

Expected interface
------------------
::

    from agent.intent_detector import detect_shopping_intent, IntentResult

    result: IntentResult = detect_shopping_intent(query: str)

``IntentResult`` fields
~~~~~~~~~~~~~~~~~~~~~~~
- ``is_shopping_intent`` (bool)
- ``guide_type`` (``Optional[str]``) - one of
  ``"price_compare"``, ``"category_recommend"``, ``"search_suggest"``,
  ``"multi_turn_guide"`` or ``None``.
- ``confidence`` (float) - ``0.0`` for non-intent, ``> 0.0`` otherwise.
- ``query`` (str) - the original query, echoed back unchanged.

Keyword matching (case-sensitive Chinese substring match)
---------------------------------------------------------
Primary keywords (high confidence):
    推荐, 有什么, 找一下, 买点, 分类, 比价, 性价比, 搜索

Secondary keywords (lower confidence):
    好吃的, 想喝, 想要, 看看

Guide type resolution priority
------------------------------
1. ``比价`` / ``性价比`` present       -> ``"price_compare"``
2. ``分类`` / ``推荐`` present         -> ``"category_recommend"``
3. ``搜索`` / ``找一下`` present       -> ``"search_suggest"``
4. Other primary keyword (``买点``/``有什么``) or any
   secondary keyword                  -> ``"multi_turn_guide"``
5. No keyword matched                 -> ``guide_type is None``
"""

import pytest

from agent.intent_detector import detect_shopping_intent, IntentResult


class TestShoppingIntentPositive:
    """Queries that should be detected as shopping intent."""

    @pytest.mark.parametrize(
        "query,expected_guide_type",
        [
            # Single primary keyword "推荐" -> category_recommend
            # (also contains secondary "好吃的" but primary wins).
            ("推荐一些好吃的零食", "category_recommend"),

            # Two primary keywords ("有什么" + "推荐"); "推荐" wins by
            # the priority chain, so guide_type is category_recommend.
            ("有什么水果推荐吗", "category_recommend"),

            # Single primary keyword "找一下" -> search_suggest.
            ("帮我找一下牛奶", "search_suggest"),

            # Single primary keyword "买点" -> multi_turn_guide
            # (generic shopping, no specific guide category).
            ("我想买点饮料", "multi_turn_guide"),

            # Single primary keyword "比价" -> price_compare.
            ("比价一下薯片", "price_compare"),

            # Single primary keyword "性价比" -> price_compare.
            ("哪个牌子的饼干性价比高", "price_compare"),

            # Primary "分类" wins the priority chain -> category_recommend.
            ("分类看看有什么零食", "category_recommend"),

            # Single primary keyword "搜索" -> search_suggest.
            ("搜索巧克力", "search_suggest"),
        ],
    )
    def test_shopping_intent_detected(self, query, expected_guide_type):
        """Each positive case must be flagged as shopping intent with
        the correct guide_type and a positive confidence score."""
        result = detect_shopping_intent(query)

        # Return type contract.
        assert isinstance(result, IntentResult)

        # Intent detection.
        assert result.is_shopping_intent is True
        assert result.guide_type == expected_guide_type

        # Confidence must be strictly positive for an intent hit.
        assert result.confidence > 0.0

        # The original query is echoed back unchanged.
        assert result.query == query


class TestShoppingIntentNegative:
    """Queries that should NOT be detected as shopping intent."""

    @pytest.mark.parametrize(
        "query",
        [
            "配送费多少钱",   # customer-service: delivery fee
            "你们营业到几点",  # customer-service: business hours
            "怎么退款",        # customer-service: refund policy
            "你好",            # plain greeting, no shopping cue
        ],
    )
    def test_no_shopping_intent(self, query):
        """None of these queries contain a primary or secondary
        keyword, so the detector must report no shopping intent."""
        result = detect_shopping_intent(query)

        # Return type contract.
        assert isinstance(result, IntentResult)

        # No intent.
        assert result.is_shopping_intent is False
        assert result.guide_type is None
        assert result.confidence == 0.0

        # The original query is echoed back unchanged.
        assert result.query == query


class TestShoppingIntentEdgeCases:
    """Edge cases: borderline, empty and whitespace-only inputs."""

    def test_borderline_short_recommendation(self):
        """``"推荐一下"`` still contains the primary keyword ``"推荐"``
        and must therefore be detected as a shopping intent with
        ``guide_type == "category_recommend"``.

        This guards against a regression where a too-short minimum-length
        heuristic would incorrectly strip out otherwise valid signals.
        """
        result = detect_shopping_intent("推荐一下")

        assert isinstance(result, IntentResult)
        assert result.is_shopping_intent is True
        assert result.guide_type == "category_recommend"
        assert result.confidence > 0.0
        assert result.query == "推荐一下"

    def test_empty_query(self):
        """An empty string is not a shopping intent."""
        result = detect_shopping_intent("")

        assert isinstance(result, IntentResult)
        assert result.is_shopping_intent is False
        assert result.guide_type is None
        assert result.confidence == 0.0
        assert result.query == ""

    def test_whitespace_only_query(self):
        """A whitespace-only string is not a shopping intent.

        The detector must strip whitespace before matching, otherwise a
        stray space (e.g. from a misconfigured UI) would leak through.
        """
        result = detect_shopping_intent("   ")

        assert isinstance(result, IntentResult)
        assert result.is_shopping_intent is False
        assert result.guide_type is None
        assert result.confidence == 0.0
        assert result.query == "   "
