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
from typing import Optional, AsyncGenerator
from fastapi import APIRouter, Query
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

from agent.react_agent import ReactAgent
from utils.logger_handler import logger

# 创建API路由实例
# 为什么：将相关接口组织在一起，便于统一管理
# 怎么做的：使用APIRouter创建路由实例
router = APIRouter()

# 全局Agent实例变量
# 为什么：Agent初始化较耗时，使用单例模式避免重复创建
# 怎么做的：使用模块级变量存储Agent实例
agent_instance: Optional[ReactAgent] = None


def get_agent() -> ReactAgent:
    """
    获取或创建Agent实例（单例模式）
    
    为什么：Agent初始化需要加载模型和知识库，耗时较长
    怎么做的：检查全局变量，为空则创建新实例
    
    Returns:
        ReactAgent: Agent实例
    """
    global agent_instance
    if agent_instance is None:
        agent_instance = ReactAgent()
        logger.info("Agent实例初始化完成")
    return agent_instance


class AskResponse(BaseModel):
    """
    问答响应模型
    
    为什么：定义标准化的API响应格式
    怎么做的：使用Pydantic BaseModel定义数据结构和类型
    """
    answer: str  # AI回答内容
    products: list = []  # 推荐商品列表（可选）


@router.get("/ask", response_model=AskResponse)
async def ask(
    question: Optional[str] = Query(None, description="用户问题"),
    session_id: Optional[str] = Query(None, description="会话ID")
):
    """
    AI客服问答接口（非流式）
    
    为什么：提供一次性返回完整答案的接口，适合简单问答场景
    怎么做的：调用Agent流式方法，收集所有片段后一次性返回
    
    Args:
        question: 用户问题
        session_id: 会话ID（可选，用于上下文关联）
        
    Returns:
        AskResponse: 包含完整回答的响应对象
    """
    logger.info(f"收到AI客服咨询，问题：{question}")
    
    # 参数校验
    if not question or not question.strip():
        return AskResponse(answer="请输入问题")
    
    try:
        # 获取Agent实例
        agent = get_agent()
        
        # 收集完整回答
        # 为什么：虽然Agent支持流式，但此接口需要一次性返回
        # 怎么做的：遍历异步生成器，拼接所有内容
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
    
    为什么：提供打字机效果的实时回答，提升用户体验
    怎么做的：使用SSE(Server-Sent Events)协议实现流式传输
    
    Args:
        question: 用户问题
        session_id: 会话ID（可选）
        
    Returns:
        StreamingResponse: SSE流式响应
    """
    logger.info(f"收到AI客服流式咨询，问题：{question}")
    
    # 参数校验
    if not question or not question.strip():
        error_chunk = {"type": "error", "content": "请输入问题"}
        return StreamingResponse(
            iter([f"data: {json.dumps(error_chunk, ensure_ascii=False)}\n\n"]),
            media_type="text/event-stream"
        )
    
    # 定义事件生成器
    # 为什么：StreamingResponse需要接收一个生成器
    # 怎么做的：定义异步生成器函数，yield每个数据块
    async def event_generator() -> AsyncGenerator[str, None]:
        try:
            agent = get_agent()
            full_content = ""
            
            # 流式生成回答
            async for chunk in agent.execute_stream(question):
                full_content += chunk
                # 构造SSE格式的数据
                data = {"type": "text", "content": chunk}
                # SSE格式：data: {...}\n\n
                yield f"data: {json.dumps(data, ensure_ascii=False)}\n\n"
                # 控制流速，模拟自然打字效果
                await asyncio.sleep(0.01)
            
            # 发送结束标记
            end_chunk = {"type": "end"}
            yield f"data: {json.dumps(end_chunk, ensure_ascii=False)}\n\n"
            
            logger.info(f"流式回答完成，长度：{len(full_content)}")
            
        except Exception as e:
            logger.error(f"流式输出异常: {e}")
            error_chunk = {"type": "error", "content": "服务异常，请稍后重试"}
            yield f"data: {json.dumps(error_chunk, ensure_ascii=False)}\n\n"
    
    # 返回流式响应
    # 为什么：需要设置SSE相关的响应头
    # 怎么做的：使用StreamingResponse，指定media_type和headers
    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream; charset=utf-8",
        headers={
            "Cache-Control": "no-cache",  # 禁用缓存
            "Connection": "keep-alive",  # 保持连接
            "X-Accel-Buffering": "no",  # 禁用Nginx缓冲
            "Content-Type": "text/event-stream; charset=utf-8",
        }
    )


@router.get("/health")
async def health_check():
    """
    健康检查接口
    
    为什么：用于监控系统检查服务是否正常运行
    怎么做的：返回简单的状态信息
    """
    return {"status": "ok", "service": "ai-customer-service"}
