"""
智能体核心模块 - 双模式：智能客服 + 智能导购

职责：
1. 客服模式：混合检索（BM25 + Dense）+ LLM 生成
2. 导购模式：检测购物意图 → 并行调用 Java 后端 API → 注入实时商品数据
3. 流式输出：从 LLM 获取 token 级流式响应，结合导购结果
"""

import json
import asyncio
import logging
import re
from datetime import datetime
from typing import Optional, AsyncGenerator, List, Dict, Any

from langchain_core.prompts import PromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage

from app.core.hybrid_retriever import HybridRetriever
from app.core.mongo_client import get_mongo
from app.core.llm import get_llm
from app.config import PROMPTS
from agent.intent_detector import detect_shopping_intent
from app.schemas.shopping_guide import ProductInfo, GuideResult

logger = logging.getLogger(__name__)


class ReactAgent:
    """
    智能体封装类

    双模式设计：
    - 智能客服：RAG 检索 + LLM 生成（现有能力）
    - 智能导购：意图检测 → 并行商品搜索 → LLM 生成 → 商品卡片展示
    """

    def __init__(self, guide_service=None):
        self.hybrid_retriever = HybridRetriever()
        self.mongo = get_mongo()
        self.llm = get_llm()
        self.guide_service = guide_service
        self.last_guide_result: Optional[GuideResult] = None
        self.prompt_template = self._build_prompt_template()
        self.chain = self.prompt_template | self.llm | StrOutputParser()

    # ── 查询复杂度分类 ──────────────────────────────────────────────
    # 简单关键词：命中则走管道（快速、低成本）
    _SIMPLE_PATTERNS = [
        "配送费", "运费", "多久", "时间", "付款", "支付",
        "取消", "退款", "退货", "地址", "订单", "状态",
        "客服", "帮助", "在哪", "电话", "15分钟",
    ]

    # 复杂关键词：命中则走 ReAct（灵活、多步推理）
    _COMPLEX_PATTERNS = [
        "推荐", "比价", "对比", "区别", "哪个好",
        "有什么", "找一下", "买点", "性价比",
        "零食", "水果", "饮料", "蔬菜",
    ]

    async def _classify_query(self, query: str) -> str:
        """判断 query 复杂度，返回 'simple' 或 'complex'"""
        q = query.strip()
        if not q:
            return "simple"

        # 简单关键词优先匹配（确定性高，不走 LLM）
        for kw in self._SIMPLE_PATTERNS:
            if kw in q:
                return "simple"

        # 复杂关键词匹配
        for kw in self._COMPLEX_PATTERNS:
            if kw in q:
                return "complex"

        # 模棱两可的 → 用 LLM 快速分类
        return await self._llm_classify(q)

    async def _llm_classify(self, query: str) -> str:
        """用 LLM 快速判断 query 是否为复杂购物意图"""
        classify_prompt = (
            f"判断顾客问题是否需要搜索商品或比价推荐。\n"
            f"只需回复「simple」或「complex」。\n"
            f"顾客问题：{query}\n"
            f"分类："
        )
        try:
            msg = HumanMessage(content=classify_prompt)
            resp = await self.llm.ainvoke([msg])
            label = resp.content.strip().lower()
            return "complex" if "complex" in label else "simple"
        except Exception as e:
            logger.warning("LLM 分类失败，默认走管道: %s", e)
            return "simple"

    def _build_prompt_template(self) -> PromptTemplate:
        """
        构建带对话历史和导购数据的 prompt 模板
        """
        template = PROMPTS.get('system', '你是凡栋超市的AI智能客服助手。') + """

{history}

【参考资料】
{context}

{guide_context}

【顾客问题】
{input}

请根据以上参考资料回答顾客问题。如果参考资料中没有相关信息，请友好地引导顾客联系人工客服。回答要简洁，控制在200字以内。"""

        return PromptTemplate.from_template(template)

    def _format_history(self, session_id: str) -> str:
        """
        从 MongoDB 加载对话历史并格式化

        Args:
            session_id: 会话ID

        Returns:
            str: 格式化的对话历史文本
        """
        if not session_id:
            return ""

        history = self.mongo.get_history(session_id, limit=6)
        if not history:
            return ""

        lines = []
        for msg in history:
            role = "顾客" if msg.get("role") == "user" else "客服"
            lines.append(f"{role}：{msg.get('content', '')}")

        return "【对话历史】\n" + "\n".join(lines)

    def _build_guide_context(self, products: List[ProductInfo]) -> str:
        """
        构建导购商品上下文（注入到 prompt 中供 LLM 参考）

        Args:
            products: 商品列表

        Returns:
            str: 格式化的商品信息文本
        """
        if not products:
            return ""

        lines = ["【实时商品库】以下是从商品库中搜索到的相关商品："]
        for i, p in enumerate(products[:5], 1):
            tag = f"[{p.promo_tag}]" if p.promo_tag else ""
            lines.append(
                f"  {i}. {p.name} {tag} - ¥{p.price}"
                f"{' (描述: ' + p.description[:30] + ')' if p.description else ''}"
            )
        return "\n".join(lines)

    def _save_to_history(self, session_id: str, question: str, answer: str):
        """
        保存本次对话到 MongoDB
        """
        if not session_id:
            return

        self.mongo.save_message(session_id, "user", question)
        self.mongo.save_message(session_id, "assistant", answer)

    async def _detect_and_fetch(
        self,
        query: str,
        session_id: Optional[str] = None
    ) -> Optional[List[ProductInfo]]:
        """
        检测购物意图并在有意图时并行获取商品数据

        Returns:
            Optional[List[ProductInfo]]: 商品列表，无意图时返回 None
        """
        if not self.guide_service:
            return None

        intent = detect_shopping_intent(query)
        if not intent.is_shopping_intent:
            return None

        try:
            products = await self.guide_service.search_products(query)
            if products:
                self.last_guide_result = GuideResult(
                    guide_type=intent.guide_type,
                    products=products,
                    query=query,
                )
                return products
        except Exception as e:
            logger.warning("导购服务调用异常（降级处理）: %s", e)

        return None

    async def execute_stream(
        self,
        query: str,
        session_id: Optional[str] = None
    ) -> AsyncGenerator[str, None]:
        """
        流式执行用户查询 - 自动路由

        根据 query 复杂度判定路由：
        - simple（配送费/退换货等）→ 管道模式（快速、低成本）
        - complex（推荐/比价/搜索商品等）→ ReAct 循环（灵活、多步推理）
        """
        route = await self._classify_query(query)
        logger.info("路由判定: query='%s' → %s", query, route)

        if route == "complex":
            async for chunk in self._react_loop(query, session_id):
                yield chunk
        else:
            async for chunk in self._pipeline_stream(query, session_id):
                yield chunk

    async def _pipeline_stream(
        self,
        query: str,
        session_id: Optional[str] = None
    ) -> AsyncGenerator[str, None]:
        """
        [原 execute_stream 逻辑] 管道模式

        流程：
        1. 混合检索获取上下文
        2. 检测购物意图，并行获取商品数据
        3. 构建 prompt（含导购数据）
        4. 调用 LLM 流式生成
        5. 保存到对话历史
        """
        # 1. 混合检索
        context = self.hybrid_retriever.get_context_string(query, top_k=3)

        # 2. 加载对话历史
        history_text = self._format_history(session_id)

        # 3. 检测购物意图 + 并发获取商品
        guide_products = await self._detect_and_fetch(query, session_id)
        guide_context = self._build_guide_context(guide_products) if guide_products else ""

        # 4. 流式调用 LLM
        full_answer = ""
        try:
            async for chunk in self.chain.astream({
                "input": query,
                "context": context,
                "history": history_text,
                "guide_context": guide_context,
            }):
                if hasattr(chunk, 'content'):
                    text = chunk.content
                else:
                    text = str(chunk)
                full_answer += text
                yield text

        except Exception as e:
            logger.error(f"LLM流式调用失败: {e}")
            error_msg = "服务异常，请稍后重试"
            full_answer = error_msg
            yield error_msg

        # 5. 保存到对话历史
        self._save_to_history(session_id, query, full_answer)

    # ── ReAct 循环 ──────────────────────────────────────────────────

    _REACT_SYSTEM_PROMPT = """你是凡栋超市的AI智能客服助手，通过「思考→行动→观察」循环来回答问题。

可用工具：
- knowledge_retrieve({{"query": "..."}}): 从知识库检索配送/退换货/支付等规则
- search_products({{"keyword": "..."}}): 搜索商品（支持模糊匹配名称/分类）
- get_product_detail({{"product_id": 数字}}): 查看指定商品的详细信息
- get_current_time({{}}): 获取当前时间

每轮必须严格按照以下格式回复：

Thought: 我现在的推理...
Action: 工具名称
Action Input: {{"参数名": "参数值"}}

如果已有足够信息回答：

Thought: 我已掌握足够信息
Final Answer: 给顾客的最终回答（简洁，200字以内）

注意：
- 最多调用 3 次工具，不要重复调用
- 如果工具返回空结果，换一个关键词再试一次
- 回答要亲切友好"""

    async def _react_loop(
        self,
        query: str,
        session_id: Optional[str] = None
    ) -> AsyncGenerator[str, None]:
        """
        ReAct 思考-行动-观察循环

        Yields:
            str: 结构化 SSE 事件（JSON 格式），与管道模式的纯文本不同
        """
        # 1. 构建初始消息
        messages = [
            SystemMessage(content=self._REACT_SYSTEM_PROMPT),
            HumanMessage(content=query),
        ]

        # 2. 加载最近对话历史（最近 4 轮）
        if session_id:
            history = self.mongo.get_history(session_id, limit=4)
            for msg in history:
                role = "user" if msg.get("role") == "user" else "assistant"
                messages.append(HumanMessage(content=msg["content"]) if role == "user"
                                else AIMessage(content=msg["content"]))

        max_steps = 5
        final_answer = ""
        products_found = []

        for step in range(max_steps):
            # 调用 LLM
            try:
                response_text = await self._llm_react_step(messages)
            except Exception as e:
                logger.error("ReAct 循环 LLM 调用失败 (step %d): %s", step, e)
                # 降级到管道
                async for chunk in self._pipeline_stream(query, session_id):
                    yield chunk
                return

            # 2. 解析 LLM 输出
            # 尝试提取 Final Answer
            fa_match = re.search(
                r"Final Answer:\s*(.*?)(?=(?:Thought:|Action:|\Z))",
                response_text, re.DOTALL
            )

            # 尝试提取 Action
            action_match = re.search(r"Action:\s*(\w+)", response_text)
            input_match = re.search(r"Action Input:\s*(\{.*?\})", response_text, re.DOTALL)

            if fa_match and not action_match:
                # ── 给出最终答案 ──
                final_answer = fa_match.group(1).strip()

                # 流式输出最终答案
                yield json.dumps({"type": "text", "content": final_answer}, ensure_ascii=False)

                # 如果有商品数据，发 guide_card
                if products_found:
                    yield json.dumps({
                        "type": "guide_card",
                        "guide_type": "react_recommend",
                        "guide_message": "为您找到以下商品：",
                        "products": products_found,
                    }, ensure_ascii=False)

                # 结束标记
                yield json.dumps({"type": "end"}, ensure_ascii=False)

                self._save_to_history(session_id, query, final_answer)
                logger.info("ReAct 完成，共 %d 步，回答长度 %d", step + 1, len(final_answer))
                return

            elif action_match:
                # ── 执行工具 ──
                tool_name = action_match.group(1)
                tool_args = {}
                if input_match:
                    try:
                        tool_args = json.loads(input_match.group(1))
                    except json.JSONDecodeError:
                        tool_args = {}

                # 提取并发送思考过程
                thought = response_text
                if "Thought:" in thought:
                    thought = thought.split("Thought:", 1)[1]
                if "Action:" in thought:
                    thought = thought.split("Action:")[0].strip()
                yield json.dumps({"type": "thought", "content": thought}, ensure_ascii=False)

                # 执行工具
                result = await self._execute_tool(tool_name, tool_args)

                # 如果搜到商品，保存起来后续发卡片
                if tool_name == "search_products" and result and result != "[]":
                    try:
                        parsed = json.loads(result)
                        if isinstance(parsed, list):
                            products_found.extend(parsed)
                    except json.JSONDecodeError:
                        pass

                # 将结果加入消息列表
                messages.append(AIMessage(content=response_text))
                messages.append(AIMessage(
                    content=f"工具 {tool_name} 返回结果：{result}"
                ))

            else:
                # ── 格式不对，降级 ──
                logger.warning("ReAct 输出格式无法解析: %.200s", response_text)
                async for chunk in self._pipeline_stream(query, session_id):
                    yield chunk
                return

        # 超步数限制，降级
        logger.warning("ReAct 超步数限制 (%d)，降级到管道", max_steps)
        async for chunk in self._pipeline_stream(query, session_id):
            yield chunk

    async def _llm_react_step(self, messages: list) -> str:
        """单步 ReAct 推理"""
        full = ""
        async for chunk in self.llm.astream(messages):
            if hasattr(chunk, 'content'):
                full += chunk.content
            else:
                full += str(chunk)
        return full

    async def _execute_tool(self, name: str, args: dict) -> str:
        """执行工具调用，返回 JSON 字符串"""
        try:
            if name == "knowledge_retrieve":
                q = args.get("query", "")
                result = self.hybrid_retriever.get_context_string(q, top_k=3)
                return json.dumps({"found": bool(result), "content": result}, ensure_ascii=False)

            elif name == "search_products":
                if not self.guide_service:
                    return json.dumps({"error": "导购服务不可用"})
                keyword = args.get("keyword", "")
                products = await self.guide_service.search_products(keyword)
                if not products:
                    return "[]"
                return json.dumps(
                    [p.model_dump() for p in products],
                    ensure_ascii=False
                )

            elif name == "get_product_detail":
                if not self.guide_service:
                    return json.dumps({"error": "导购服务不可用"})
                pid = args.get("product_id", 0)
                product = await self.guide_service.get_product_detail(pid)
                if not product:
                    return "null"
                return json.dumps(product.model_dump(), ensure_ascii=False)

            elif name == "get_current_time":
                return json.dumps({
                    "time": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                    "date": datetime.now().strftime("%Y年%m月%d日"),
                }, ensure_ascii=False)

            else:
                return json.dumps({"error": f"未知工具: {name}"})

        except Exception as e:
            logger.error("工具 %s 执行异常: %s", name, e)
            return json.dumps({"error": str(e)})

    async def execute(self, query: str, session_id: Optional[str] = None) -> str:
        """
        非流式执行（一次性返回完整答案）

        Args:
            query: 用户问题
            session_id: 会话ID

        Returns:
            str: 完整回答
        """
        context = self.hybrid_retriever.get_context_string(query, top_k=3)
        history_text = self._format_history(session_id)

        guide_products = await self._detect_and_fetch(query, session_id)
        guide_context = self._build_guide_context(guide_products) if guide_products else ""

        try:
            answer = await self.chain.ainvoke({
                "input": query,
                "context": context,
                "history": history_text,
                "guide_context": guide_context,
            })
        except Exception as e:
            logger.error(f"LLM调用失败: {e}")
            answer = "服务异常，请稍后重试"

        self._save_to_history(session_id, query, answer)
        return answer
