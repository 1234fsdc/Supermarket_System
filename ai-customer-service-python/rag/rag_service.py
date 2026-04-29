"""
RAG检索增强生成服务模块

为什么创建这个文件：
- 实现RAG（Retrieval-Augmented Generation）流程
- 结合向量检索和大语言模型生成回答
- 提供完整的问答服务

怎么做的：
- 使用向量存储检索相关文档
- 构建提示词模板，将检索结果作为上下文
- 调用大语言模型生成回答
"""

from langchain_core.documents import Document
from langchain_core.output_parsers import StrOutputParser
from rag.vector_store import VectorStoreService
from utils.prompt_loader import load_rag_prompts
from langchain_core.prompts import PromptTemplate
from model.factory import chat_model


def print_prompt(prompt):
    """
    打印提示词的调试函数
    
    为什么：调试时查看最终发送给模型的提示词
    怎么做的：将提示词转换为字符串并打印
    
    Args:
        prompt: 提示词对象
        
    Returns:
        prompt: 原样返回提示词对象，用于链式调用
    """
    print("="*20)
    print(prompt.to_string())
    print("="*20)
    return prompt


class RagSummarizeService:
    """
    RAG总结服务类
    
    为什么：封装RAG的完整流程
    怎么做的：
    - 初始化向量存储、检索器、提示词模板
    - 构建LangChain处理链
    - 提供rag_summarize主方法
    """
    
    def __init__(self):
        """
        初始化RAG服务
        
        为什么：需要准备所有依赖组件
        怎么做的：
        - 实例化向量存储服务
        - 获取检索器
        - 加载提示词模板
        - 初始化处理链
        """
        self.vector_store = VectorStoreService()
        self.retriever = self.vector_store.get_retriever()
        self.prompt_text = load_rag_prompts()
        self.prompt_template = PromptTemplate.from_template(self.prompt_text)
        self.model = chat_model
        self.chain = self._init_chain()

    def _init_chain(self):
        """
        初始化LangChain处理链
        
        为什么：构建从提示词到模型再到解析器的完整流程
        怎么做的：使用管道操作符|连接各个组件
        
        Returns:
            Chain: LangChain处理链
        """
        # 构建处理链：提示词模板 -> 打印调试 -> 模型 -> 字符串解析器
        chain = self.prompt_template | print_prompt | self.model | StrOutputParser()
        return chain

    def retriever_docs(self, query: str) -> list[Document]:
        """
        检索相关文档
        
        为什么：根据查询找到相关的知识文档
        怎么做的：调用检索器的invoke方法
        
        Args:
            query: 查询文本
            
        Returns:
            list[Document]: 相关文档列表
        """
        return self.retriever.invoke(query)

    def rag_summarize(self, query: str) -> str:
        """
        RAG总结主方法
        
        为什么：提供完整的RAG问答功能
        怎么做的：
        - 检索相关文档
        - 格式化文档为上下文字符串
        - 调用处理链生成回答
        
        Args:
            query: 用户查询
            
        Returns:
            str: AI生成的回答
        """
        # 检索相关文档
        # 为什么：获取与查询相关的背景知识
        # 怎么做的：调用retriever_docs方法
        context_docs = self.retriever_docs(query)
        
        # 格式化文档为上下文字符串
        # 为什么：将文档列表转换为模型可理解的文本格式
        # 怎么做的：遍历文档，添加序号标记
        context = ""
        counter = 0
        for doc in context_docs:
            counter += 1
            context += f"【参考资料{counter}】: {doc.page_content}\n"

        # 调用处理链生成回答
        # 为什么：使用LangChain链简化调用流程
        # 怎么做的：传入input和context变量，执行链
        return self.chain.invoke(
            {
                "input": query,
                "context": context,
            }
        )
