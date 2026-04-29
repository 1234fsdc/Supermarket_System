"""
RAG检索增强生成服务模块

提供基于向量检索的问答服务
"""
from langchain_core.documents import Document
from langchain_core.output_parsers import StrOutputParser
from rag.vector_store import VectorStoreService
from utils.prompt_loader import load_rag_prompts
from langchain_core.prompts import PromptTemplate
from model.factory import chat_model


def print_prompt(prompt):
    """打印提示词的调试函数"""
    print("="*20)
    print(prompt.to_string())
    print("="*20)
    return prompt


class RagSummarizeService:
    """RAG总结服务类"""
    
    def __init__(self):
        """初始化RAG服务"""
        self.vector_store = VectorStoreService()
        self.retriever = self.vector_store.get_retriever()
        self.prompt_text = load_rag_prompts()
        self.prompt_template = PromptTemplate.from_template(self.prompt_text)
        self.model = chat_model
        self.chain = self._init_chain()

    def _init_chain(self):
        """初始化LangChain处理链"""
        chain = self.prompt_template | print_prompt | self.model | StrOutputParser()
        return chain

    def retriever_docs(self, query: str) -> list[Document]:
        """检索相关文档"""
        return self.retriever.invoke(query)

    def rag_summarize(self, query: str) -> str:
        """RAG总结主方法"""
        # 检索相关文档
        context_docs = self.retriever_docs(query)
        
        # 格式化文档为上下文字符串
        context = ""
        counter = 0
        for doc in context_docs:
            counter += 1
            context += f"【参考资料{counter}】: {doc.page_content}\n"

        # 调用处理链生成回答
        return self.chain.invoke(
            {
                "input": query,
                "context": context,
            }
        )
