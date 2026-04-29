"""
中间件模块

为什么创建这个文件：
- 提供工具调用监控和日志记录功能
- 实现LangChain回调机制
- 便于调试和追踪Agent行为

怎么做的：
- 继承BaseCallbackHandler实现自定义回调
- 在工具调用和LLM调用的关键节点插入日志
"""

from langchain_core.callbacks import BaseCallbackHandler
from langchain_core.outputs import LLMResult
from typing import Any


class ToolMonitor(BaseCallbackHandler):
    """
    工具调用监控类
    
    为什么：监控工具调用的输入输出，便于调试
    怎么做的：继承BaseCallbackHandler，重写on_tool_start和on_tool_end方法
    """
    
    def on_tool_start(self, serialized: dict[str, Any], input_str: str, **kwargs: Any) -> Any:
        """
        工具开始调用时触发
        
        为什么：记录工具调用的开始，包括工具名称和输入参数
        怎么做的：打印工具名称和输入参数
        
        Args:
            serialized: 工具序列化信息，包含工具名称
            input_str: 工具输入参数
        """
        print(f"[工具调用] {serialized.get('name', 'unknown')}: {input_str}")
    
    def on_tool_end(self, output: str, **kwargs: Any) -> Any:
        """
        工具调用结束时触发
        
        为什么：记录工具调用的返回结果
        怎么做的：打印工具返回结果的前100个字符
        
        Args:
            output: 工具返回结果
        """
        print(f"[工具返回] {output[:100]}...")


class ModelLogger(BaseCallbackHandler):
    """
    模型调用日志类
    
    为什么：监控LLM的调用过程，便于调试和性能分析
    怎么做的：继承BaseCallbackHandler，重写on_llm_start和on_llm_end方法
    """
    
    def on_llm_start(self, serialized: dict[str, Any], prompts: list[str], **kwargs: Any) -> Any:
        """
        模型开始生成时触发
        
        为什么：记录LLM调用开始
        怎么做的：打印开始生成日志
        
        Args:
            serialized: 模型序列化信息
            prompts: 输入提示词列表
        """
        print(f"[LLM调用] 开始生成...")
    
    def on_llm_end(self, response: LLMResult, **kwargs: Any) -> Any:
        """
        模型生成结束时触发
        
        为什么：记录LLM调用完成
        怎么做的：打印生成完成日志
        
        Args:
            response: LLM生成结果
        """
        print(f"[LLM调用] 生成完成")


# 中间件实例
# 为什么：提供预创建的中间件实例，方便直接使用
# 怎么做的：实例化监控和日志类
monitor_tool = ToolMonitor()
log_before_model = ModelLogger()
