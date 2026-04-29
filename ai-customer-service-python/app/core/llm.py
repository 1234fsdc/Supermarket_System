"""
大语言模型服务 - 基于通义千问Qwen3-max
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
    """通义千问大模型封装"""
    
    model: str = "qwen3-max"
    api_key: Optional[str] = None
    temperature: float = 0.7
    max_tokens: int = 2000
    streaming: bool = True
    
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.api_key = self.api_key or DASHSCOPE_API_KEY or LLM_CONFIG.get('api_key')
        
        if not self.api_key:
            raise ValueError("DashScope API密钥未配置")
        
        dashscope.api_key = self.api_key
        logger.info(f"通义千问模型初始化完成，模型: {self.model}")
    
    @property
    def _llm_type(self) -> str:
        return "qwen"
    
    def _convert_messages(self, messages: list[BaseMessage]) -> list[dict]:
        """转换消息格式"""
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
        """同步生成"""
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
        """异步生成"""
        # 当前使用同步方式
        return self._generate(messages, stop, run_manager, **kwargs)
    
    async def astream(
        self,
        messages: list[BaseMessage],
        stop: Optional[list[str]] = None,
        **kwargs: Any
    ) -> AsyncIterator[BaseMessage]:
        """流式生成"""
        try:
            response = Generation.call(
                model=self.model,
                messages=self._convert_messages(messages),
                temperature=self.temperature,
                max_tokens=self.max_tokens,
                result_format='message',
                stream=True
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
_llm_instance: Optional[QwenChatModel] = None


def get_llm() -> QwenChatModel:
    """获取全局LLM实例（单例模式）"""
    global _llm_instance
    if _llm_instance is None:
        _llm_instance = QwenChatModel(
            model=LLM_CONFIG.get('model', 'qwen3-max'),
            temperature=LLM_CONFIG.get('temperature', 0.7),
            max_tokens=LLM_CONFIG.get('max_tokens', 2000),
            streaming=LLM_CONFIG.get('streaming', True)
        )
    return _llm_instance
