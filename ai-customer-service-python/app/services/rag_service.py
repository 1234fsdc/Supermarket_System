"""
RAG服务 - 基于LangChain的检索增强生成
"""
import json
import logging
from typing import List, Optional, Dict, Any, AsyncGenerator
from langchain_core.documents import Document
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage
from langchain.chains import RetrievalQA
from langchain.prompts import PromptTemplate

from app.core import get_embeddings, get_llm, get_vectorstore, get_redis
from app.config import PROMPTS, RAG_CONFIG
from app.schemas.customer_service import CustomerServiceResult, ProductRecommend

logger = logging.getLogger(__name__)


class RAGService:
    """RAG服务 - 语义检索 + 大模型生成"""
    
    def __init__(self):
        """初始化RAG服务"""
        self.embeddings = get_embeddings()
        self.llm = get_llm()
        self.vectorstore = get_vectorstore()
        self.redis = get_redis()
        
        # 加载知识库到向量数据库
        self._init_knowledge_base()
        
        logger.info("RAG服务初始化完成")
    
    def _init_knowledge_base(self):
        """初始化知识库"""
        try:
            # 检查向量数据库是否已有数据
            stats = self.vectorstore.get_collection_stats()
            if stats.get('document_count', 0) > 0:
                logger.info(f"向量数据库已有 {stats['document_count']} 条数据，跳过初始化")
                return
            
            # 从JSON加载知识库
            kb_path = RAG_CONFIG.get('knowledge_base_path', 'app/data/knowledge_base.json')
            with open(kb_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
            
            documents = []
            for category in data.get('categories', []):
                category_name = category.get('name', '')
                
                for doc in category.get('documents', []):
                    # 构建文档内容
                    content = f"问题：{doc.get('question', '')}\n答案：{doc.get('answer', '')}"
                    
                    # 构建元数据
                    metadata = {
                        'id': doc.get('id'),
                        'category': category_name,
                        'category_id': category.get('id'),
                        'question': doc.get('question'),
                        'keywords': ','.join(doc.get('keywords', [])),
                        'has_products': 'products' in doc
                    }
                    
                    # 如果有商品，添加到元数据
                    if 'products' in doc:
                        metadata['products'] = json.dumps(doc['products'], ensure_ascii=False)
                    
                    documents.append(Document(page_content=content, metadata=metadata))
            
            # 添加到向量数据库
            if documents:
                self.vectorstore.add_documents(documents)
                logger.info(f"知识库初始化完成，共 {len(documents)} 条数据")
                
        except Exception as e:
            logger.error(f"知识库初始化失败: {e}")
    
    def _format_history(self, messages: List[Dict[str, str]]) -> str:
        """格式化对话历史"""
        formatted = []
        for msg in messages:
            role = msg.get('role', '')
            content = msg.get('content', '')
            if role == 'user':
                formatted.append(f"顾客：{content}")
            elif role == 'assistant':
                formatted.append(f"客服：{content}")
        return "\n".join(formatted)
    
    def _parse_products(self, products_json: str) -> List[ProductRecommend]:
        """解析商品信息"""
        try:
            products = json.loads(products_json)
            return [ProductRecommend(**p) for p in products]
        except:
            return []
    
    async def get_answer(
        self, 
        question: str, 
        session_id: Optional[str] = None
    ) -> CustomerServiceResult:
        """
        获取答案（非流式）
        
        Args:
            question: 用户问题
            session_id: 会话ID
            
        Returns:
            客服回答结果
        """
        try:
            # 1. 获取对话历史
            history = []
            if session_id:
                history = self.redis.get_history(session_id)
            
            # 2. 检索相关知识
            retrieved_docs = self.vectorstore.similarity_search(question, k=3)
            
            # 3. 构建上下文
            context = "\n\n".join([
                f"[{doc.metadata.get('category', '未知')}] {doc.page_content}"
                for doc in retrieved_docs
            ])
            
            # 4. 构建提示词
            system_prompt = PROMPTS.get('system', '')
            
            messages = [SystemMessage(content=system_prompt)]
            
            # 添加历史对话
            if history:
                history_text = self._format_history(history)
                messages.append(HumanMessage(content=f"之前的对话：\n{history_text}"))
            
            # 添加当前问题和上下文
            user_prompt = f"""基于以下知识回答顾客问题：

{context}

顾客问题：{question}

请给出专业、友好的回答："""
            
            messages.append(HumanMessage(content=user_prompt))
            
            # 5. 调用大模型
            response = await self.llm._agenerate(messages)
            answer = response.generations[0].message.content
            
            # 6. 提取推荐商品
            products = []
            for doc in retrieved_docs:
                if doc.metadata.get('has_products') and doc.metadata.get('products'):
                    products.extend(self._parse_products(doc.metadata['products']))
            
            # 7. 保存对话历史
            if session_id:
                self.redis.save_message(session_id, 'user', question)
                self.redis.save_message(session_id, 'assistant', answer)
            
            return CustomerServiceResult(
                answer=answer,
                products=products if products else None
            )
            
        except Exception as e:
            logger.error(f"获取答案失败: {e}")
            return CustomerServiceResult(
                answer="抱歉，服务暂时不可用，请稍后重试。"
            )
    
    async def get_answer_stream(
        self, 
        question: str, 
        session_id: Optional[str] = None
    ) -> AsyncGenerator[Dict[str, Any], None]:
        """
        流式获取答案
        
        Args:
            question: 用户问题
            session_id: 会话ID
            
        Yields:
            流式数据块
        """
        try:
            # 1. 获取对话历史
            history = []
            if session_id:
                history = self.redis.get_history(session_id)
            
            # 2. 检索相关知识
            retrieved_docs = self.vectorstore.similarity_search(question, k=3)
            
            # 3. 构建上下文
            context = "\n\n".join([
                f"[{doc.metadata.get('category', '未知')}] {doc.page_content}"
                for doc in retrieved_docs
            ])
            
            # 4. 构建提示词
            system_prompt = PROMPTS.get('system', '')
            
            messages = [SystemMessage(content=system_prompt)]
            
            # 添加历史对话
            if history:
                history_text = self._format_history(history)
                messages.append(HumanMessage(content=f"之前的对话：\n{history_text}"))
            
            # 添加当前问题和上下文
            user_prompt = f"""基于以下知识回答顾客问题：

{context}

顾客问题：{question}

请给出专业、友好的回答："""
            
            messages.append(HumanMessage(content=user_prompt))
            
            # 5. 流式生成
            full_answer = ""
            async for chunk in self.llm.astream(messages):
                content = chunk.content
                full_answer += content
                yield {"type": "text", "content": content}
            
            # 6. 发送推荐商品
            products = []
            for doc in retrieved_docs:
                if doc.metadata.get('has_products') and doc.metadata.get('products'):
                    products.extend(self._parse_products(doc.metadata['products']))
            
            if products:
                yield {"type": "products", "products": [p.dict() for p in products]}
            
            # 7. 发送结束标记
            yield {"type": "end"}
            
            # 8. 保存对话历史
            if session_id:
                self.redis.save_message(session_id, 'user', question)
                self.redis.save_message(session_id, 'assistant', full_answer)
                
        except Exception as e:
            logger.error(f"流式生成失败: {e}")
            yield {"type": "error", "content": "服务异常，请稍后重试"}


# 全局RAG服务实例
_rag_service_instance: Optional[RAGService] = None


def get_rag_service() -> RAGService:
    """获取全局RAG服务实例（单例模式）"""
    global _rag_service_instance
    if _rag_service_instance is None:
        _rag_service_instance = RAGService()
    return _rag_service_instance
