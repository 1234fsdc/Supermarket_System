"""
AI客服API接口 - FastAPI版本
提供流式和非流式问答接口
"""
import json
import asyncio
from typing import Optional, AsyncGenerator
from fastapi import APIRouter, Query
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

from agent.react_agent import ReactAgent
from utils.logger_handler import logger

router = APIRouter()

# 全局Agent实例
agent_instance: Optional[ReactAgent] = None


def get_agent() -> ReactAgent:
    """获取或创建Agent实例"""
    global agent_instance
    if agent_instance is None:
        agent_instance = ReactAgent()
        logger.info("Agent实例初始化完成")
    return agent_instance


class AskResponse(BaseModel):
    """问答响应模型"""
    answer: str
    products: list = []


@router.get("/ask", response_model=AskResponse)
async def ask(
    question: Optional[str] = Query(None, description="用户问题"),
    session_id: Optional[str] = Query(None, description="会话ID")
):
    """
    AI客服问答接口（非流式）
    
    Args:
        question: 用户问题
        session_id: 会话ID（可选）
        
    Returns:
        完整回答
    """
    logger.info(f"收到AI客服咨询，问题：{question}")
    
    if not question or not question.strip():
        return AskResponse(answer="请输入问题")
    
    try:
        agent = get_agent()
        
        # 收集完整回答
        full_answer = ""
        async for chunk in agent.execute_stream(question):
            full_answer += chunk
        
        return AskResponse(
            answer=full_answer,
            products=[]
        )
        
    except Exception as e:
        logger.error(f"AI客服处理异常: {e}")
        return AskResponse(answer="服务异常，请稍后重试")


@router.get("/ask/stream")
async def ask_stream(
    question: Optional[str] = Query(None, description="用户问题"),
    session_id: Optional[str] = Query(None, description="会话ID")
):
    """
    AI客服问答接口（流式输出）
    
    Args:
        question: 用户问题
        session_id: 会话ID（可选）
        
    Returns:
        SSE流式响应
    """
    logger.info(f"收到AI客服流式咨询，问题：{question}")
    
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
            
            # 流式生成回答
            async for chunk in agent.execute_stream(question):
                full_content += chunk
                data = {"type": "text", "content": chunk}
                yield f"data: {json.dumps(data, ensure_ascii=False)}\n\n"
                await asyncio.sleep(0.01)  # 控制流速
            
            # 发送结束标记
            end_chunk = {"type": "end"}
            yield f"data: {json.dumps(end_chunk, ensure_ascii=False)}\n\n"
            
            logger.info(f"流式回答完成，长度：{len(full_content)}")
            
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
