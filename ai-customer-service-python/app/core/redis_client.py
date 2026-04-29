"""
Redis客户端 - 用于存储对话历史

为什么创建这个文件：
- 封装Redis操作，存储用户对话历史
- 支持会话管理和消息持久化
- 实现对话上下文的维护

怎么做的：
- 使用redis-py库连接Redis
- 使用List结构存储消息历史
- 设置TTL自动过期
"""

import json
import logging
from typing import Optional, List, Dict, Any
import redis
from redis import Redis

from app.config import REDIS_CONFIG

logger = logging.getLogger(__name__)


class RedisClient:
    """
    Redis客户端封装
    
    为什么：统一管理Redis操作
    怎么做的：
    - 封装连接逻辑
    - 提供消息存储和查询方法
    - 处理连接异常
    """
    
    def __init__(self):
        """
        初始化Redis连接
        
        为什么：需要建立与Redis服务器的连接
        怎么做的：
        - 读取配置参数
        - 创建Redis连接
        - 测试连接可用性
        """
        self.host = REDIS_CONFIG.get('host', 'localhost')
        self.port = REDIS_CONFIG.get('port', 6379)
        self.db = REDIS_CONFIG.get('db', 0)
        self.password = REDIS_CONFIG.get('password') or None
        self.session_ttl = REDIS_CONFIG.get('session_ttl', 3600)  # 会话过期时间（秒）
        self.max_history = REDIS_CONFIG.get('max_history', 10)  # 最大历史消息数
        
        try:
            self.client = Redis(
                host=self.host,
                port=self.port,
                db=self.db,
                password=self.password,
                decode_responses=True  # 自动解码响应为字符串
            )
            self.client.ping()  # 测试连接
            logger.info(f"Redis连接成功: {self.host}:{self.port}")
        except Exception as e:
            logger.error(f"Redis连接失败: {e}")
            self.client = None
    
    def is_connected(self) -> bool:
        """
        检查连接状态
        
        为什么：操作前需要确认连接可用
        怎么做的：发送ping命令测试
        
        Returns:
            bool: 是否已连接
        """
        if not self.client:
            return False
        try:
            self.client.ping()
            return True
        except:
            return False
    
    def _get_key(self, session_id: str) -> str:
        """
        生成Redis键
        
        为什么：统一键名格式，避免冲突
        怎么做的：使用前缀+会话ID的格式
        
        Args:
            session_id: 会话ID
            
        Returns:
            str: Redis键名
        """
        return f"chat:session:{session_id}"
    
    def save_message(self, session_id: str, role: str, content: str) -> bool:
        """
        保存单条消息
        
        为什么：记录用户和AI的对话历史
        怎么做的：
        - 使用LPUSH将消息添加到列表头部
        - 使用LTRIM限制列表长度
        - 设置过期时间
        
        Args:
            session_id: 会话ID
            role: 角色 (user/assistant)
            content: 消息内容
            
        Returns:
            bool: 是否保存成功
        """
        if not self.is_connected():
            logger.warning("Redis未连接，无法保存消息")
            return False
        
        try:
            key = self._get_key(session_id)
            message = {
                "role": role,
                "content": content,
                "timestamp": int(__import__('time').time())
            }
            
            # 使用LPUSH添加消息到列表头部
            self.client.lpush(key, json.dumps(message, ensure_ascii=False))
            
            # 限制历史消息数量
            # 为什么：避免存储过多历史消息
            # 怎么做的：使用LTRIM保留最新的N条
            self.client.ltrim(key, 0, self.max_history - 1)
            
            # 设置过期时间
            # 为什么：自动清理过期会话，节省空间
            # 怎么做的：使用EXPIRE设置TTL
            self.client.expire(key, self.session_ttl)
            
            return True
            
        except Exception as e:
            logger.error(f"保存消息失败: {e}")
            return False
    
    def get_history(self, session_id: str, limit: Optional[int] = None) -> List[Dict[str, Any]]:
        """
        获取对话历史
        
        为什么：需要获取之前的对话作为上下文
        怎么做的：
        - 使用LRANGE获取列表元素
        - 解析JSON并反转顺序
        
        Args:
            session_id: 会话ID
            limit: 返回消息数量限制
            
        Returns:
            List[Dict[str, Any]]: 消息列表（按时间正序）
        """
        if not self.is_connected():
            return []
        
        try:
            key = self._get_key(session_id)
            limit = limit or self.max_history
            
            # 获取列表（LRANGE返回的是从头部开始的，需要反转）
            messages_raw = self.client.lrange(key, 0, limit - 1)
            
            # 解析并反转顺序（ oldest -> newest）
            messages = []
            for msg_raw in reversed(messages_raw):
                try:
                    msg = json.loads(msg_raw)
                    messages.append(msg)
                except json.JSONDecodeError:
                    continue
            
            return messages
            
        except Exception as e:
            logger.error(f"获取历史失败: {e}")
            return []
    
    def clear_history(self, session_id: str) -> bool:
        """
        清空对话历史
        
        为什么：用户可能需要清空对话重新开始
        怎么做的：使用DEL删除键
        
        Args:
            session_id: 会话ID
            
        Returns:
            bool: 是否清空成功
        """
        if not self.is_connected():
            return False
        
        try:
            key = self._get_key(session_id)
            self.client.delete(key)
            logger.info(f"会话 {session_id} 历史已清空")
            return True
            
        except Exception as e:
            logger.error(f"清空历史失败: {e}")
            return False
    
    def get_session_ids(self, pattern: str = "chat:session:*") -> List[str]:
        """
        获取所有会话ID
        
        为什么：管理或统计所有活跃会话
        怎么做的：使用KEYS命令匹配键名
        
        Args:
            pattern: 匹配模式
            
        Returns:
            List[str]: 会话ID列表
        """
        if not self.is_connected():
            return []
        
        try:
            keys = self.client.keys(pattern)
            # 提取session_id（去掉前缀）
            session_ids = [key.replace("chat:session:", "") for key in keys]
            return session_ids
            
        except Exception as e:
            logger.error(f"获取会话列表失败: {e}")
            return []


# 全局Redis客户端实例
# 为什么：避免重复创建连接
# 怎么做的：使用单例模式
_redis_instance: Optional[RedisClient] = None


def get_redis() -> RedisClient:
    """
    获取全局Redis客户端实例（单例模式）
    
    为什么：统一管理Redis连接
    怎么做的：检查全局变量，为空则创建
    
    Returns:
        RedisClient: Redis客户端实例
    """
    global _redis_instance
    if _redis_instance is None:
        _redis_instance = RedisClient()
    return _redis_instance
