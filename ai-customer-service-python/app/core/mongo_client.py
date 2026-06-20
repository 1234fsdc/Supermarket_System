"""
MongoDB客户端 - 用于持久化存储对话历史

为什么创建这个文件：
- 替代Redis，实现对话历史的持久化存储
- MongoDB支持更灵活的查询和数据分析
- 数据不会因重启而丢失

怎么做的：
- 使用pymongo连接MongoDB
- 使用集合(Collection)按session_id存储消息
- 支持TTL自动过期和消息数量限制
"""

import logging
import time
from typing import Optional, List, Dict, Any
from pymongo import MongoClient
from pymongo.errors import ConnectionFailure

from app.config import MONGODB_CONFIG

logger = logging.getLogger(__name__)


class MongoDBClient:
    """
    MongoDB客户端封装

    为什么：统一管理MongoDB操作
    怎么做的：
    - 封装连接逻辑
    - 提供消息存储和查询方法
    - 处理连接异常
    """

    def __init__(self):
        """
        初始化MongoDB连接

        为什么：需要建立与MongoDB服务器的连接
        怎么做的：
        - 读取配置参数
        - 创建MongoDB连接
        - 获取数据库和集合
        """
        self.host = MONGODB_CONFIG.get('host', 'localhost')
        self.port = MONGODB_CONFIG.get('port', 27017)
        self.db_name = MONGODB_CONFIG.get('database', 'ai_customer_service')
        self.collection_name = MONGODB_CONFIG.get('collection', 'chat_history')
        self.session_ttl = MONGODB_CONFIG.get('session_ttl', 86400)  # 默认24小时
        self.max_history = MONGODB_CONFIG.get('max_history', 20)  # 最大历史消息数

        try:
            self.client = MongoClient(
                host=self.host,
                port=self.port,
                serverSelectionTimeoutMS=3000
            )
            # 测试连接
            self.client.admin.command('ping')
            self.db = self.client[self.db_name]
            self.collection = self.db[self.collection_name]

            # 创建索引：按session_id查询 + TTL自动过期
            self.collection.create_index("session_id")
            self.collection.create_index("timestamp")

            logger.info(f"MongoDB连接成功: {self.host}:{self.port}")
        except Exception as e:
            logger.error(f"MongoDB连接失败: {e}")
            self.client = None
            self.db = None
            self.collection = None

    def is_connected(self) -> bool:
        """检查连接状态"""
        if not self.client:
            return False
        try:
            self.client.admin.command('ping')
            return True
        except:
            return False

    def save_message(self, session_id: str, role: str, content: str) -> bool:
        """
        保存单条消息

        为什么：记录用户和AI的对话历史
        怎么做的：
        - 插入文档到MongoDB
        - 超过限制时删除旧消息
        - 设置过期时间

        Args:
            session_id: 会话ID
            role: 角色 (user/assistant)
            content: 消息内容

        Returns:
            bool: 是否保存成功
        """
        if not self.is_connected():
            logger.warning("MongoDB未连接，无法保存消息")
            return False

        try:
            document = {
                "session_id": session_id,
                "role": role,
                "content": content,
                "timestamp": int(time.time())
            }

            self.collection.insert_one(document)

            # 限制历史消息数量：删除该session最旧的消息
            count = self.collection.count_documents({"session_id": session_id})
            if count > self.max_history:
                # 删除最旧的消息（保留最新的max_history条）
                excess = count - self.max_history
                oldest_docs = self.collection.find(
                    {"session_id": session_id}
                ).sort("timestamp", 1).limit(excess)

                oldest_ids = [doc["_id"] for doc in oldest_docs]
                if oldest_ids:
                    self.collection.delete_many({"_id": {"$in": oldest_ids}})

            return True

        except Exception as e:
            logger.error(f"保存消息失败: {e}")
            return False

    def get_history(self, session_id: str, limit: Optional[int] = None) -> List[Dict[str, Any]]:
        """
        获取对话历史

        为什么：需要获取之前的对话作为上下文
        怎么做的：
        - 按session_id查询
        - 按时间正序排列
        - 返回指定数量

        Args:
            session_id: 会话ID
            limit: 返回消息数量限制

        Returns:
            List[Dict[str, Any]]: 消息列表（按时间正序）
        """
        if not self.is_connected():
            return []

        try:
            limit = limit or self.max_history

            # 查询并按时间正序排列
            cursor = self.collection.find(
                {"session_id": session_id}
            ).sort("timestamp", 1).limit(limit)

            messages = []
            for doc in cursor:
                messages.append({
                    "role": doc.get("role"),
                    "content": doc.get("content"),
                    "timestamp": doc.get("timestamp")
                })

            return messages

        except Exception as e:
            logger.error(f"获取历史失败: {e}")
            return []

    def clear_history(self, session_id: str) -> bool:
        """
        清空对话历史

        为什么：用户可能需要清空对话重新开始
        怎么做的：删除该session_id的所有文档

        Args:
            session_id: 会话ID

        Returns:
            bool: 是否清空成功
        """
        if not self.is_connected():
            return False

        try:
            result = self.collection.delete_many({"session_id": session_id})
            logger.info(f"会话 {session_id} 历史已清空，删除 {result.deleted_count} 条消息")
            return True

        except Exception as e:
            logger.error(f"清空历史失败: {e}")
            return False

    def get_session_ids(self) -> List[str]:
        """
        获取所有会话ID

        为什么：管理或统计所有活跃会话
        怎么做的：使用distinct查询去重

        Returns:
            List[str]: 会话ID列表
        """
        if not self.is_connected():
            return []

        try:
            session_ids = self.collection.distinct("session_id")
            return session_ids

        except Exception as e:
            logger.error(f"获取会话列表失败: {e}")
            return []


# 全局MongoDB客户端实例
_mongo_instance: Optional[MongoDBClient] = None


def get_mongo() -> MongoDBClient:
    """
    获取全局MongoDB客户端实例（单例模式）

    为什么：统一管理MongoDB连接
    怎么做的：检查全局变量，为空则创建

    Returns:
        MongoDBClient: MongoDB客户端实例
    """
    global _mongo_instance
    if _mongo_instance is None:
        _mongo_instance = MongoDBClient()
    return _mongo_instance
