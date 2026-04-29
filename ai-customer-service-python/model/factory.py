"""
模型工厂模块

使用工厂模式创建聊天模型和嵌入模型
"""
from abc import ABC, abstractmethod
from typing import Optional
from langchain_core.embeddings import Embeddings
from langchain_community.chat_models.tongyi import BaseChatModel
from langchain_community.embeddings import DashScopeEmbeddings
from langchain_community.chat_models.tongyi import ChatTongyi
from utils.config_handler import rag_conf


class BaseModelFactory(ABC):
    """模型工厂抽象基类"""
    
    @abstractmethod
    def generator(self) -> Optional[Embeddings | BaseChatModel]:
        """生成模型实例"""
        pass


class ChatModelFactory(BaseModelFactory):
    """聊天模型工厂 - 创建通义千问模型"""
    
    def generator(self) -> Optional[Embeddings | BaseChatModel]:
        """创建聊天模型实例"""
        return ChatTongyi(model=rag_conf["chat_model_name"])


class EmbeddingsFactory(BaseModelFactory):
    """嵌入模型工厂 - 创建DashScope嵌入模型"""
    
    def generator(self) -> Optional[Embeddings | BaseChatModel]:
        """创建嵌入模型实例"""
        return DashScopeEmbeddings(model=rag_conf["embedding_model_name"])


# 全局模型实例
chat_model = ChatModelFactory().generator()
embed_model = EmbeddingsFactory().generator()
