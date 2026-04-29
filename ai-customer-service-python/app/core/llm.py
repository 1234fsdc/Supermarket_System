"""
大语言模型服务 - 基于通义千问Qwen3-max

为什么创建这个文件：
- 封装通义千问大模型的调用逻辑
- 实现LangChain兼容的ChatModel接口
- 支持同步、异步和流式生成

怎么做的：
- 继承BaseChatModel实现自定义模型类
- 使用DashScope SDK调用通义千问API
- 实现_generate、_agenerate、astream方法
"""

import logging
from typing import Optional, AsyncIterator, Any
import dashscope
from dashscope import Generation
from langchain_core.language_models import BaseChatModel
from langchain_core.messages import BaseMessage, HumanMessage, AIMessage, SystemMessage
from langchain_core.outputs import ChatResult, ChatGeneration
from langchain_core.callbacks import CallbackManagerForLLMRun

from app.config import LLM_CONFIG, DASHSCOPE_API_KEY, PROMPTS

logger = logging.getLogger(__name__)


class QwenChatModel(BaseChatModel):
    """
    通义千问大模型封装
    
    为什么：需要与LangChain框架集成
    怎么做的：继承BaseChatModel，实现必要的方法
    """
    
    # 模型配置参数
    model: str = "qwen3-max"  # 默认模型名称
    api_key: Optional[str] = None  # API密钥
    temperature: float = 0.7  # 温度参数，控制随机性
    max_tokens: int = 2000  # 最大生成token数
    streaming: bool = True  # 是否使用流式输出
    
    def __init__(self, **kwargs):
        """
        初始化模型
        
        为什么：需要配置API密钥和模型参数
        怎么做的：
        - 从配置或参数获取API密钥
        - 设置dashscope的全局API密钥
        """
        super().__init__(**kwargs)
        self.api_key = self.api_key or DASHSCOPE_API_KEY or LLM_CONFIG.get('api_key')
        
        if not self.api_key:
            raise ValueError("DashScope API密钥未配置")
        
        dashscope.api_key = self.api_key
        logger.info(f"通义千问模型初始化完成，模型: {self.model}")
    
    @property
    def _llm_type(self) -> str:
        """
        返回模型类型标识
        
        为什么：LangChain需要识别模型类型
        怎么做的：返回"qwen"作为类型标识
        """
        return "qwen"
    
    def _convert_messages(self, messages: list[BaseMessage]) -> list[dict]:
        """
        转换消息格式
        
        为什么：LangChain消息格式与DashScope格式不同
        怎么做的：将BaseMessage转换为DashScope需要的字典格式
        
        Args:
            messages: LangChain消息列表
            
        Returns:
            list[dict]: DashScope格式的消息列表
        """
        converted = []
        for msg in messages:
            if isinstance(msg, SystemMessage):
                converted.append({"role": "system", "content": msg.content})
            elif isinstance(msg, HumanMessage):
                converted.append({"role": "user", "content": msg.content})
            elif isinstance(msg, AIMessage):
                converted.append({"role": "assistant", "content": msg.content})
        return converted
    
    def _generate(
        self,
        messages: list[BaseMessage],
        stop: Optional[list[str]] = None,
        run_manager: Optional[CallbackManagerForLLMRun] = None,
        **kwargs: Any,
    ) -> ChatResult:
        """
        同步生成
        
        为什么：LangChain需要同步生成方法
        怎么做的：调用DashScope的Generation.call方法
        
        Args:
            messages: 消息列表
            stop: 停止词列表
            run_manager: 回调管理器
            
        Returns:
            ChatResult: 生成结果
        """
        try:
            response = Generation.call(
                model=self.model,
                messages=self._convert_messages(messages),
                temperature=self.temperature,
                max_tokens=self.max_tokens,
                result_format='message'
            )
            
            if response.status_code == 200:
                content = response.output.choices[0].message.content
                message = AIMessage(content=content)
                generation = ChatGeneration(message=message)
                return ChatResult(generations=[generation])
            else:
                logger.error(f"模型调用失败: {response.message}")
                raise Exception(f"模型调用失败: {response.message}")
                
        except Exception as e:
            logger.error(f"生成失败: {e}")
            raise
    
    async def _agenerate(
        self,
        messages: list[BaseMessage],
        stop: Optional[list[str]] = None,
        run_manager: Optional[CallbackManagerForLLMRun] = None,
        **kwargs: Any,
    ) -> ChatResult:
        """
        异步生成
        
        为什么：支持异步调用，提高并发性能
        怎么做的：当前使用同步方式，后续可优化为真正的异步
        
        Args:
            messages: 消息列表
            stop: 停止词列表
            run_manager: 回调管理器
            
        Returns:
            ChatResult: 生成结果
        """
        # 当前使用同步方式
        return self._generate(messages, stop, run_manager, **kwargs)
    
    async def astream(
        self,
        messages: list[BaseMessage],
        stop: Optional[list[str]] = None,
        **kwargs: Any
    ) -> AsyncIterator[BaseMessage]:
        """
        流式生成
        
        为什么：支持打字机效果的实时输出
        怎么做的：调用DashScope的流式API，逐块返回
        
        Args:
            messages: 消息列表
            stop: 停止词列表
            
        Yields:
            BaseMessage: 生成的消息块
        """
        try:
            response = Generation.call(
                model=self.model,
                messages=self._convert_messages(messages),
                temperature=self.temperature,
                max_tokens=self.max_tokens,
                result_format='message',
                stream=True  # 启用流式输出
            )
            
            for chunk in response:
                if chunk.status_code == 200:
                    content = chunk.output.choices[0].message.content
                    yield AIMessage(content=content)
                else:
                    logger.error(f"流式生成失败: {chunk.message}")
                    
        except Exception as e:
            logger.error(f"流式生成异常: {e}")
            raise


# 全局LLM实例
# 为什么：避免重复创建模型实例
# 怎么做的：使用单例模式
_llm_instance: Optional[QwenChatModel] = None


def get_llm() -> QwenChatModel:
    """
    获取全局LLM实例（单例模式）
    
    为什么：统一管理模型实例，节省资源
    怎么做的：检查全局变量，为空则创建
    
    Returns:
        QwenChatModel: LLM实例
    """
    global _llm_instance
    if _llm_instance is None:
        _llm_instance = QwenChatModel(
            model=LLM_CONFIG.get('model', 'qwen3-max'),
            temperature=LLM_CONFIG.get('temperature', 0.7),
            max_tokens=LLM_CONFIG.get('max_tokens', 2000),
            streaming=LLM_CONFIG.get('streaming', True)
        )
    return _llm_instance
