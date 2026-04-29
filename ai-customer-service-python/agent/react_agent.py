"""
ReAct智能体核心模块

为什么创建这个文件：
- 实现ReAct（Reasoning + Acting）智能体模式
- 封装AI推理和工具调用的逻辑
- 提供流式输出能力

怎么做的：
- 简化版Agent实现，直接调用RAG服务
- 使用LangChain的提示词模板
- 模拟流式输出效果
"""

from langchain_core.prompts import ChatPromptTemplate
from model.factory import chat_model
from utils.prompt_loader import load_system_prompts
from rag.rag_service import RagSummarizeService


class ReactAgent:
    """
    简化的智能体封装类
    
    为什么：封装Agent的核心逻辑，对外提供简洁接口
    怎么做的：
    - 初始化时加载RAG服务和系统提示词
    - 提供流式执行方法
    """
    
    def __init__(self):
        """
        初始化Agent
        
        为什么：需要准备RAG服务、系统提示词和模型
        怎么做的：实例化RAG服务，加载提示词，获取模型
        """
        self.rag_service = RagSummarizeService()
        self.system_prompt = load_system_prompts()
        self.model = chat_model

    async def execute_stream(self, query: str):
        """
        流式执行用户查询
        
        为什么：提供打字机效果的输出，提升用户体验
        怎么做的：
        - 调用RAG服务获取回答
        - 逐字符yield，模拟流式效果
        
        Args:
            query: 用户输入问题
            
        Yields:
            str: AI生成的内容片段（逐字符）
        """
        import time
        
        # 使用RAG服务获取回答
        # 为什么：RAG服务负责检索和生成回答
        # 怎么做的：调用rag_summarize方法
        answer = self.rag_service.rag_summarize(query)
        
        # 模拟流式输出
        # 为什么：实际模型可能不支持真正的流式，需要模拟
        # 怎么做的：遍历答案字符串，每个字符延迟10ms后yield
        for char in answer:
            time.sleep(0.01)  # 10ms延迟，模拟打字速度
            yield char


if __name__ == '__main__':
    # 测试代码
    # 为什么：方便独立测试Agent功能
    # 怎么做的：创建Agent实例，调用execute_stream方法
    agent = ReactAgent()
    
    import asyncio
    async def test():
        async for chunk in agent.execute_stream("配送费是多少？"):
            print(chunk, end="", flush=True)
    
    asyncio.run(test())
