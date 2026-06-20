"""
AI客服API接口模块 - FastAPI版本

为什么创建这个文件：
- 提供AI客服的HTTP API接口
- 支持流式和非流式两种回答模式
- 与微信小程序前端对接

怎么做的：
- 使用FastAPI的APIRouter组织路由
- 使用Pydantic模型定义请求/响应数据结构
- 使用StreamingResponse实现SSE流式输出
"""

import json
import asyncio
import logging
from typing import Optional, AsyncGenerator
from fastapi import APIRouter, Query
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

from agent.react_agent import ReactAgent
from app.services.shopping_guide_service import ShoppingGuideService

logger = logging.getLogger(__name__)

router = APIRouter()

agent_instance: Optional[ReactAgent] = None


def get_agent() -> ReactAgent:
    global agent_instance
    if agent_instance is None:
        guide_service = ShoppingGuideService()
        agent_instance = ReactAgent(guide_service=guide_service)
        logger.info("Agent实例初始化完成（含导购服务）")
    return agent_instance


class AskResponse(BaseModel):
    answer: str
    products: list = []


@router.get("/ask", response_model=AskResponse)
async def ask(
    question: Optional[str] = Query(None, description="用户问题"),
    session_id: Optional[str] = Query(None, description="会话ID")
):
    """AI客服问答接口（非流式）"""
    logger.info(f"收到AI客服咨询，问题：{question}，会话：{session_id}")

    if not question or not question.strip():
        return AskResponse(answer="请输入问题")

        try:
            agent = get_agent()
            full_answer = await agent.execute(question, session_id=session_id)
            return AskResponse(answer=full_answer, products=[])

        except Exception as e:
            logger.error(f"AI客服处理异常: {e}")
            return AskResponse(answer="服务异常，请稍后重试")


@router.get("/ask/stream")
async def ask_stream(
    question: Optional[str] = Query(None, description="用户问题"),
    session_id: Optional[str] = Query(None, description="会话ID")
):
    """AI客服问答接口（流式输出）"""
    logger.info(f"收到AI客服流式咨询，问题：{question}，会话：{session_id}")

    if not question or not question.strip():
        error_chunk = {"type": "error", "content": "请输入问题"}
        return StreamingResponse(
            iter([f"data: {json.dumps(error_chunk, ensure_ascii=False)}\n\n"]),
            media_type="text/event-stream"
        )

    async def event_generator() -> AsyncGenerator[str, None]:
        try:
            agent = get_agent()
            full_content = ""
            react_mode = False  # 标记是否走 ReAct 模式（自行管理事件流）

            # 流式生成回答
            async for chunk in agent.execute_stream(question, session_id=session_id):
                # 检测结构化 JSON 事件（ReAct 模式产出的 JSON 字符串）
                if chunk.startswith('{') and chunk.endswith('}'):
                    try:
                        parsed = json.loads(chunk)
                        if isinstance(parsed, dict) and "type" in parsed:
                            react_mode = True
                            yield f"data: {chunk}\n\n"
                            if parsed["type"] == "end":
                                return  # ReAct 已发结束标记，提前退出
                            continue
                    except json.JSONDecodeError:
                        pass

                # 普通文本（管道模式）
                full_content += chunk
                data = {"type": "text", "content": chunk}
                yield f"data: {json.dumps(data, ensure_ascii=False)}\n\n"

            # ── 管道模式后处理（仅当非 ReAct 时执行） ──
            if not react_mode:
                # 发送导购商品卡片（如果有）
                guide = agent.last_guide_result
                if guide and guide.products:
                    card_data = {
                        "type": "guide_card",
                        "guide_type": guide.guide_type.value if guide.guide_type else "",
                        "guide_message": guide.guide_message,
                        "products": [
                            {
                                "id": p.id,
                                "name": p.name,
                                "price": p.price,
                                "image": p.image,
                                "description": p.description,
                                "category_name": p.category_name,
                                "sales_volume": p.sales_volume,
                                "stock": p.stock,
                                "promo_tag": p.promo_tag,
                                "unit": p.unit,
                            }
                            for p in guide.products
                        ],
                    }
                    yield f"data: {json.dumps(card_data, ensure_ascii=False)}\n\n"
                elif guide and not guide.products:
                    empty_data = {
                        "type": "guide_card",
                        "guide_type": guide.guide_type.value if guide.guide_type else "",
                        "guide_message": guide.guide_message,
                        "products": [],
                    }
                    yield f"data: {json.dumps(empty_data, ensure_ascii=False)}\n\n"

                # 发送结束标记
                yield f"data: {json.dumps({'type': 'end'}, ensure_ascii=False)}\n\n"

            logger.info(f"流式回答完成，模式={'ReAct' if react_mode else '管道'}，长度：{len(full_content)}")

        except Exception as e:
            logger.error(f"流式输出异常: {e}")
            error_chunk = {"type": "error", "content": "服务异常，请稍后重试"}
            yield f"data: {json.dumps(error_chunk, ensure_ascii=False)}\n\n"

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream; charset=utf-8",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
            "Content-Type": "text/event-stream; charset=utf-8",
        }
    )


@router.get("/health")
async def health_check():
    """健康检查接口"""
    return {"status": "ok", "service": "ai-customer-service"}


@router.delete("/session/{session_id}")
async def clear_session(session_id: str):
    """清空指定会话的对话历史"""
    from app.core.mongo_client import get_mongo
    mongo = get_mongo()
    success = mongo.clear_history(session_id)
    return {"status": "ok" if success else "error", "session_id": session_id}
