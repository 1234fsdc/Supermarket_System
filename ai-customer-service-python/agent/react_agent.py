"""
ReAct智能体核心模块

简化的Agent实现，直接调用RAG服务
"""
from langchain_core.prompts import ChatPromptTemplate
from model.factory import chat_model
from utils.prompt_loader import load_system_prompts
from rag.rag_service import RagSummarizeService


class ReactAgent:
    """简化的智能体封装类"""
    
    def __init__(self):
        """初始化Agent"""
        self.rag_service = RagSummarizeService()
        self.system_prompt = load_system_prompts()
        self.model = chat_model

    async def execute_stream(self, query: str):
        """
        流式执行用户查询
        
        Args:
            query: 用户输入问题
            
        Yields:
            AI生成的内容片段
        """
        import time
        
        # 使用RAG服务获取回答
        answer = self.rag_service.rag_summarize(query)
        
        # 模拟流式输出
        for char in answer:
            time.sleep(0.01)
            yield char


if __name__ == '__main__':
    # 测试
    agent = ReactAgent()
    
    import asyncio
    async def test():
        async for chunk in agent.execute_stream("配送费是多少？"):
            print(chunk, end="", flush=True)
    
    asyncio.run(test())
