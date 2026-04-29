"""
模型工厂模块

为什么创建这个文件：
- 使用工厂模式创建聊天模型和嵌入模型
- 统一管理模型的创建和配置
- 便于切换不同的模型提供商

怎么做的：
- 定义抽象基类BaseModelFactory
- 实现具体的模型工厂类
- 在模块级别创建全局模型实例
"""

from abc import ABC, abstractmethod
from typing import Optional
from langchain_core.embeddings import Embeddings
from langchain_community.chat_models.tongyi import BaseChatModel
from langchain_community.embeddings import DashScopeEmbeddings
from langchain_community.chat_models.tongyi import ChatTongyi
from utils.config_handler import rag_conf


class BaseModelFactory(ABC):
    """
    模型工厂抽象基类
    
    为什么：定义模型工厂的通用接口
    怎么做的：使用ABC抽象基类，定义抽象方法generator
    """
    
    @abstractmethod
    def generator(self) -> Optional[Embeddings | BaseChatModel]:
        """
        生成模型实例
        
        为什么：子类必须实现此方法
        怎么做的：定义为抽象方法，强制子类重写
        
        Returns:
            Optional[Embeddings | BaseChatModel]: 模型实例
        """
        pass


class ChatModelFactory(BaseModelFactory):
    """
    聊天模型工厂 - 创建通义千问模型
    
    为什么：封装通义千问聊天模型的创建逻辑
    怎么做的：继承BaseModelFactory，实现generator方法
    """
    
    def generator(self) -> Optional[Embeddings | BaseChatModel]:
        """
        创建聊天模型实例
        
        为什么：提供通义千问模型的统一创建方式
        怎么做的：使用配置中的模型名称创建ChatTongyi实例
        
        Returns:
            Optional[Embeddings | BaseChatModel]: 聊天模型实例
        """
        return ChatTongyi(model=rag_conf["chat_model_name"])


class EmbeddingsFactory(BaseModelFactory):
    """
    嵌入模型工厂 - 创建DashScope嵌入模型
    
    为什么：封装文本嵌入模型的创建逻辑
    怎么做的：继承BaseModelFactory，实现generator方法
    """
    
    def generator(self) -> Optional[Embeddings | BaseChatModel]:
        """
        创建嵌入模型实例
        
        为什么：提供DashScope嵌入模型的统一创建方式
        怎么做的：使用配置中的模型名称创建DashScopeEmbeddings实例
        
        Returns:
            Optional[Embeddings | BaseChatModel]: 嵌入模型实例
        """
        return DashScopeEmbeddings(model=rag_conf["embedding_model_name"])


# 全局模型实例
# 为什么：避免重复创建模型实例，提高性能
# 怎么做的：在模块导入时创建单例实例
chat_model = ChatModelFactory().generator()
embed_model = EmbeddingsFactory().generator()
