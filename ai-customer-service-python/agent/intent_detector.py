"""
Intent detector - shopping intent classification.

Two-stage architecture
----------------------
1. Keyword pre-filter (implemented here). Fast, deterministic, no I/O.
2. LLM fallback (not implemented; reserved for ambiguous queries that
   the keyword pass cannot classify with high confidence).

The keyword pass is sufficient for the unit tests in
``tests/test_intent_detector.py``. The LLM stage is intentionally
deferred to avoid pulling LLM credentials into unit tests.
"""

from dataclasses import dataclass
from typing import Optional


@dataclass
class IntentResult:
    """Result returned by :func:`detect_shopping_intent`.

    Attributes
    ----------
    is_shopping_intent:
        ``True`` when the query expresses a shopping intent,
        ``False`` otherwise.
    guide_type:
        The shopping guide the agent should follow, or ``None`` when
        ``is_shopping_intent`` is ``False``. Valid values:
        ``"price_compare"``, ``"category_recommend"``,
        ``"search_suggest"``, ``"multi_turn_guide"``.
    confidence:
        ``0.0`` for non-intent hits; a positive score otherwise.
    query:
        The original query string, echoed back unchanged so callers can
        pass the result object around without losing context.
    """

    is_shopping_intent: bool
    guide_type: Optional[str] = None
    confidence: float = 0.0
    query: str = ""


# Primary keywords - substring match in the query indicates shopping
# intent with high confidence. The tuple order is *not* significant for
# the priority chain below (priority is hard-coded by guide type).
_PRIMARY_KEYWORDS: tuple = (
    "推荐",
    "有什么",
    "找一下",
    "买点",
    "分类",
    "比价",
    "性价比",
    "搜索",
)

# Secondary keywords - lower confidence shopping cues that do not map
# to a specific guide type. They still trigger intent, defaulting to
# ``multi_turn_guide`` so the agent can ask a follow-up question.
_SECONDARY_KEYWORDS: tuple = (
    "好吃的",
    "想喝",
    "想要",
    "看看",
)


def detect_shopping_intent(query: str) -> IntentResult:
    """Classify ``query`` as a shopping intent or not.

    Parameters
    ----------
    query:
        The raw user input. May be empty or whitespace-only.

    Returns
    -------
    IntentResult
        Always populated. ``is_shopping_intent`` is ``False`` and
        ``guide_type`` is ``None`` for empty / whitespace / unmatched
        inputs.
    """
    if not query or not query.strip():
        return IntentResult(
            is_shopping_intent=False,
            guide_type=None,
            confidence=0.0,
            query=query,
        )

    # Priority chain - first hit wins. This keeps the mapping explicit
    # and prevents order-dependent surprises (e.g. a query that
    # contains both ``推荐`` and ``找一下`` resolves to
    # ``category_recommend`` because ``推荐`` is checked first).
    if "比价" in query or "性价比" in query:
        guide_type = "price_compare"
        confidence = 0.9
    elif "分类" in query or "推荐" in query:
        guide_type = "category_recommend"
        confidence = 0.9
    elif "搜索" in query or "找一下" in query:
        guide_type = "search_suggest"
        confidence = 0.9
    elif any(kw in query for kw in _PRIMARY_KEYWORDS):
        # ``买点`` / ``有什么`` - generic shopping with no specific
        # guide category. Route to multi-turn dialog so the agent can
        # ask the user to narrow down.
        guide_type = "multi_turn_guide"
        confidence = 0.8
    elif any(kw in query for kw in _SECONDARY_KEYWORDS):
        guide_type = "multi_turn_guide"
        confidence = 0.6
    else:
        return IntentResult(
            is_shopping_intent=False,
            guide_type=None,
            confidence=0.0,
            query=query,
        )

    return IntentResult(
        is_shopping_intent=True,
        guide_type=guide_type,
        confidence=confidence,
        query=query,
    )
