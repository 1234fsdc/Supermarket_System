"""
向量数据库服务 - 基于ChromaDB
"""
import os
import logging
from typing import List, Optional, Dict, Any
from langchain_chroma import Chroma
from langchain_core.documents import Document
from langchain.text_splitter import RecursiveCharacterTextSplitter

from app.core.embeddings import get_embeddings
from app.config import CHROMA_CONFIG, RAG_CONFIG

logger = logging.getLogger(__name__)


class VectorStore:
    """Chroma向量数据库封装"""
    
    def __init__(self):
        """初始化向量数据库"""
        self.persist_directory = CHROMA_CONFIG.get('persist_directory', 'app/data/chroma_db')
        self.collection_name = CHROMA_CONFIG.get('collection_name', 'supermarket_knowledge')
        self.embeddings = get_embeddings()
        
        # 确保目录存在
        os.makedirs(self.persist_directory, exist_ok=True)
        
        # 初始化Chroma
        self.db = Chroma(
            collection_name=self.collection_name,
            embedding_function=self.embeddings,
            persist_directory=self.persist_directory
        )
        
        logger.info(f"Chroma向量数据库初始化完成，集合: {self.collection_name}")
    
    def add_documents(self, documents: List[Document]) -> None:
        """
        添加文档到向量数据库
        
        Args:
            documents: 文档列表
        """
        if not documents:
            return
        
        try:
            # 文档分片
            text_splitter = RecursiveCharacterTextSplitter(
                chunk_size=RAG_CONFIG.get('chunk_size', 500),
                chunk_overlap=RAG_CONFIG.get('chunk_overlap', 50),
                separators=["\n\n", "\n", "。", "！", "？", " ", ""]
            )
            
            chunks = text_splitter.split_documents(documents)
            logger.info(f"文档分片完成: {len(documents)} -> {len(chunks)} 块")
            
            # 添加到数据库
            self.db.add_documents(chunks)
            logger.info(f"成功添加 {len(chunks)} 个文档块到向量数据库")
            
        except Exception as e:
            logger.error(f"添加文档失败: {e}")
            raise
    
    def add_texts(self, texts: List[str], metadatas: Optional[List[Dict]] = None) -> None:
        """
        添加纯文本文档
        
        Args:
            texts: 文本列表
            metadatas: 元数据列表
        """
        if not texts:
            return
        
        try:
            documents = []
            for i, text in enumerate(texts):
                metadata = metadatas[i] if metadatas and i < len(metadatas) else {}
                documents.append(Document(page_content=text, metadata=metadata))
            
            self.add_documents(documents)
            
        except Exception as e:
            logger.error(f"添加文本失败: {e}")
            raise
    
    def similarity_search(
        self, 
        query: str, 
        k: Optional[int] = None,
        score_threshold: Optional[float] = None
    ) -> List[Document]:
        """
        相似度搜索
        
        Args:
            query: 查询文本
            k: 返回结果数量
            score_threshold: 相似度阈值
            
        Returns:
            相关文档列表
        """
        k = k or RAG_CONFIG.get('top_k', 3)
        score_threshold = score_threshold or RAG_CONFIG.get('score_threshold', 0.7)
        
        try:
            # 使用相似度搜索并返回分数
            results = self.db.similarity_search_with_score(query, k=k)
            
            # 过滤低相似度结果
            filtered_results = []
            for doc, score in results:
                # Chroma返回的是距离，需要转换为相似度（1 - 距离）
                similarity = 1 - score
                if similarity >= score_threshold:
                    doc.metadata['similarity'] = similarity
                    filtered_results.append(doc)
            
            logger.info(f"相似度搜索完成: 找到 {len(filtered_results)} 个相关文档")
            return filtered_results
            
        except Exception as e:
            logger.error(f"相似度搜索失败: {e}")
            return []
    
    def delete_collection(self) -> None:
        """删除整个集合"""
        try:
            self.db.delete_collection()
            logger.info(f"集合 {self.collection_name} 已删除")
        except Exception as e:
            logger.error(f"删除集合失败: {e}")
            raise
    
    def get_collection_stats(self) -> Dict[str, Any]:
        """获取集合统计信息"""
        try:
            count = self.db._collection.count()
            return {
                "collection_name": self.collection_name,
                "document_count": count,
                "persist_directory": self.persist_directory
            }
        except Exception as e:
            logger.error(f"获取统计信息失败: {e}")
            return {}


# 全局向量数据库实例
_vectorstore_instance: Optional[VectorStore] = None


def get_vectorstore() -> VectorStore:
    """获取全局向量数据库实例（单例模式）"""
    global _vectorstore_instance
    if _vectorstore_instance is None:
        _vectorstore_instance = VectorStore()
    return _vectorstore_instance
