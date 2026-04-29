"""
Embedding服务 - 基于DashScope的text-embedding-v4模型

为什么创建这个文件：
- 封装文本嵌入模型的调用逻辑
- 实现LangChain兼容的Embeddings接口
- 支持批量和单条文本嵌入

怎么做的：
- 继承Embeddings基类
- 使用DashScope SDK调用text-embedding-v4
- 实现embed_documents和embed_query方法
"""

import logging
from typing import List, Optional
import dashscope
from dashscope import TextEmbedding
from langchain_core.embeddings import Embeddings

from app.config import EMBEDDING_CONFIG, DASHSCOPE_API_KEY

logger = logging.getLogger(__name__)


class DashScopeEmbeddings(Embeddings):
    """
    DashScope Embedding服务封装
    
    为什么：需要与LangChain框架集成
    怎么做的：继承Embeddings基类，实现必要的方法
    """
    
    def __init__(self, api_key: Optional[str] = None, model: Optional[str] = None):
        """
        初始化Embedding服务
        
        为什么：需要配置API密钥和模型参数
        怎么做的：
        - 从配置或参数获取API密钥
        - 设置模型名称
        - 设置dashscope的全局API密钥
        
        Args:
            api_key: DashScope API密钥
            model: 模型名称，默认text-embedding-v4
        """
        self.api_key = api_key or DASHSCOPE_API_KEY or EMBEDDING_CONFIG.get('api_key')
        self.model = model or EMBEDDING_CONFIG.get('model', 'text-embedding-v4')
        
        if not self.api_key:
            raise ValueError("DashScope API密钥未配置")
        
        dashscope.api_key = self.api_key
        logger.info(f"DashScope Embedding服务初始化完成，模型: {self.model}")
    
    def embed_documents(self, texts: List[str]) -> List[List[float]]:
        """
        批量文档嵌入
        
        为什么：需要批量将文本转换为向量
        怎么做的：
        - 分批处理（DashScope每次最多25条）
        - 调用TextEmbedding API
        - 合并结果返回
        
        Args:
            texts: 文本列表
            
        Returns:
            List[List[float]]: 嵌入向量列表
        """
        if not texts:
            return []
        
        try:
            # DashScope每次最多支持25条
            batch_size = 25
            all_embeddings = []
            
            for i in range(0, len(texts), batch_size):
                batch = texts[i:i + batch_size]
                response = TextEmbedding.call(
                    model=self.model,
                    input=batch
                )
                
                if response.status_code == 200:
                    embeddings = [item['embedding'] for item in response.output['embeddings']]
                    all_embeddings.extend(embeddings)
                else:
                    logger.error(f"Embedding调用失败: {response.message}")
                    # 失败时返回零向量作为fallback
                    dim = EMBEDDING_CONFIG.get('dimensions', 1536)
                    all_embeddings.extend([[0.0] * dim] * len(batch))
            
            return all_embeddings
            
        except Exception as e:
            logger.error(f"文档嵌入失败: {e}")
            # 返回零向量作为fallback
            dim = EMBEDDING_CONFIG.get('dimensions', 1536)
            return [[0.0] * dim] * len(texts)
    
    def embed_query(self, text: str) -> List[float]:
        """
        单条查询嵌入
        
        为什么：需要将查询文本转换为向量进行相似度搜索
        怎么做的：调用TextEmbedding API，返回单条结果
        
        Args:
            text: 查询文本
            
        Returns:
            List[float]: 嵌入向量
        """
        try:
            response = TextEmbedding.call(
                model=self.model,
                input=[text]
            )
            
            if response.status_code == 200:
                return response.output['embeddings'][0]['embedding']
            else:
                logger.error(f"Embedding调用失败: {response.message}")
                dim = EMBEDDING_CONFIG.get('dimensions', 1536)
                return [0.0] * dim
                
        except Exception as e:
            logger.error(f"查询嵌入失败: {e}")
            dim = EMBEDDING_CONFIG.get('dimensions', 1536)
            return [0.0] * dim
    
    async def aembed_documents(self, texts: List[str]) -> List[List[float]]:
        """
        异步批量文档嵌入
        
        为什么：支持异步调用，提高并发性能
        怎么做的：当前使用同步方式，后续可改为真正的异步
        
        Args:
            texts: 文本列表
            
        Returns:
            List[List[float]]: 嵌入向量列表
        """
        # 当前使用同步方式，后续可改为真正的异步
        return self.embed_documents(texts)
    
    async def aembed_query(self, text: str) -> List[float]:
        """
        异步单条查询嵌入
        
        为什么：支持异步调用
        怎么做的：调用同步方法
        
        Args:
            text: 查询文本
            
        Returns:
            List[float]: 嵌入向量
        """
        return self.embed_query(text)


# 全局Embedding实例
# 为什么：避免重复创建模型实例
# 怎么做的：使用单例模式
_embeddings_instance: Optional[DashScopeEmbeddings] = None


def get_embeddings() -> DashScopeEmbeddings:
    """
    获取全局Embedding实例（单例模式）
    
    为什么：统一管理模型实例，节省资源
    怎么做的：检查全局变量，为空则创建
    
    Returns:
        DashScopeEmbeddings: Embedding实例
    """
    global _embeddings_instance
    if _embeddings_instance is None:
        _embeddings_instance = DashScopeEmbeddings()
    return _embeddings_instance
