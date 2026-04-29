"""
中间件模块

提供工具调用监控和日志记录功能
"""
from langchain_core.callbacks import BaseCallbackHandler
from langchain_core.outputs import LLMResult
from typing import Any


class ToolMonitor(BaseCallbackHandler):
    """工具调用监控"""
    
    def on_tool_start(self, serialized: dict[str, Any], input_str: str, **kwargs: Any) -> Any:
        """工具开始调用时触发"""
        print(f"[工具调用] {serialized.get('name', 'unknown')}: {input_str}")
    
    def on_tool_end(self, output: str, **kwargs: Any) -> Any:
        """工具调用结束时触发"""
        print(f"[工具返回] {output[:100]}...")


class ModelLogger(BaseCallbackHandler):
    """模型调用日志"""
    
    def on_llm_start(self, serialized: dict[str, Any], prompts: list[str], **kwargs: Any) -> Any:
        """模型开始生成时触发"""
        print(f"[LLM调用] 开始生成...")
    
    def on_llm_end(self, response: LLMResult, **kwargs: Any) -> Any:
        """模型生成结束时触发"""
        print(f"[LLM调用] 生成完成")


# 中间件实例
monitor_tool = ToolMonitor()
log_before_model = ModelLogger()
