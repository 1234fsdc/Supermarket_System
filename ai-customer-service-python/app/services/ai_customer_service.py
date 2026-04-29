"""
AI客服服务实现模块 - 基于自定义RAG方案

为什么创建这个文件：
- 实现AI客服的核心业务逻辑
- 基于关键词匹配的轻量级RAG方案
- 支持流式输出打字机效果

怎么做的：
- 从JSON文件加载知识库
- 使用关键词匹配算法检索相关答案
- 异步生成器实现流式输出
"""

import json
import logging
import asyncio
from typing import List, Optional, AsyncGenerator
from dataclasses import dataclass, field

from app.schemas.customer_service import CustomerServiceResult, ProductRecommend, StreamChunk

logger = logging.getLogger(__name__)


@dataclass
class ProductInfo:
    """
    商品信息数据类
    
    为什么：定义商品的数据结构，便于类型提示和代码提示
    怎么做的：使用@dataclass装饰器自动生成__init__等方法
    """
    name: str  # 商品名称
    price: float  # 商品价格
    desc: str  # 商品描述


@dataclass
class KnowledgeDocument:
    """
    知识库文档实体
    
    为什么：定义知识库中每条知识的数据结构
    怎么做的：使用@dataclass，products字段使用default_factory避免可变默认参数问题
    """
    id: str  # 知识条目ID
    question: str  # 问题文本
    answer: str  # 答案文本
    keywords: List[str]  # 关键词列表，用于匹配
    category_id: str  # 分类ID
    products: List[ProductInfo] = field(default_factory=list)  # 关联的推荐商品


class AiCustomerService:
    """
    AI客服服务实现类
    
    为什么：封装AI客服的所有业务逻辑
    怎么做的：
    - 初始化时加载知识库
    - 提供关键词匹配检索
    - 支持流式输出
    """
    
    def __init__(self):
        """
        初始化AI客服服务
        
        为什么：需要加载知识库到内存中
        怎么做的：调用_load_knowledge_base方法加载JSON文件
        """
        self.knowledge_base: List[KnowledgeDocument] = []
        self._load_knowledge_base()
    
    def _load_knowledge_base(self):
        """
        从JSON文件加载知识库
        
        为什么：知识库以JSON格式存储，需要解析为Python对象
        怎么做的：
        - 打开JSON文件
        - 解析分类和文档结构
        - 转换为KnowledgeDocument对象列表
        """
        try:
            # 打开知识库JSON文件
            with open('app/data/knowledge_base.json', 'r', encoding='utf-8') as f:
                root = json.load(f)
            
            # 获取分类列表
            categories = root.get('categories', [])
            
            # 遍历每个分类
            for category in categories:
                category_id = category.get('id')
                documents = category.get('documents', [])
                
                # 遍历分类下的每个文档
                for doc in documents:
                    # 创建知识文档对象
                    knowledge_doc = KnowledgeDocument(
                        id=doc.get('id'),
                        question=doc.get('question'),
                        answer=doc.get('answer'),
                        keywords=doc.get('keywords', []),
                        category_id=category_id,
                        products=[]
                    )
                    
                    # 加载推荐商品信息
                    if 'products' in doc:
                        for p in doc['products']:
                            knowledge_doc.products.append(ProductInfo(
                                name=p.get('name'),
                                price=p.get('price'),
                                desc=p.get('desc')
                            ))
                    
                    self.knowledge_base.append(knowledge_doc)
            
            logger.info(f"知识库加载完成，共加载 {len(self.knowledge_base)} 条知识")
        except Exception as e:
            logger.error(f"加载知识库失败: {e}")
    
    def get_answer(self, question: Optional[str]) -> CustomerServiceResult:
        """
        根据用户问题从知识库中检索答案（非流式）
        
        为什么：提供同步接口获取完整答案
        怎么做的：
        - 参数校验
        - 检测无关消息
        - 关键词匹配检索
        - 返回最佳匹配结果
        
        Args:
            question: 用户问题
            
        Returns:
            CustomerServiceResult: 包含答案和推荐商品的结果
        """
        # 参数校验
        if not question or not question.strip():
            return CustomerServiceResult(
                answer="请输入您的问题，我将为您解答。"
            )
        
        query = question.strip().lower()
        
        # 检测是否为无关消息
        # 为什么：避免AI回答与超市无关的问题
        # 怎么做的：检查查询中是否包含无关关键词
        if self._is_irrelevant(query):
            logger.info(f"检测到无关消息: {query}")
            return CustomerServiceResult(
                answer="我主要是凡栋超市的AI客服，可以为您解答商品、订单、配送等问题，还可以为您推荐超市商品。请问有什么我可以帮您的吗？"
            )
        
        # RAG检索：关键词匹配
        # 为什么：需要找到与用户问题最相关的知识条目
        # 怎么做的：遍历知识库，计算每个条目的相关度分数
        max_score = 0
        best_match: Optional[KnowledgeDocument] = None
        
        for doc in self.knowledge_base:
            score = self._calculate_relevance(query, doc)
            if score > max_score:
                max_score = score
                best_match = doc
        
        # 如果找到匹配且分数大于0
        if best_match and max_score > 0:
            logger.info(f"匹配到答案: {best_match.question} (得分: {max_score})")
            
            # 转换推荐商品格式
            product_recommends = [
                ProductRecommend(name=p.name, price=p.price, desc=p.desc)
                for p in best_match.products
            ]
            
            return CustomerServiceResult(
                answer=best_match.answer,
                products=product_recommends if product_recommends else None
            )
        
        # 未匹配到答案时的默认回复
        return CustomerServiceResult(
            answer="抱歉，我暂时无法回答您的问题。您可以尝试以下方式获取帮助：\n1. 拨打商家电话咨询\n2. 查看订单详情联系客服\n3. 重新描述您的问题"
        )
    
    async def get_answer_stream(self, question: Optional[str]) -> AsyncGenerator[StreamChunk, None]:
        """
        流式获取答案，逐字输出
        
        为什么：提供打字机效果，提升用户体验
        怎么做的：
        - 先获取完整答案
        - 逐字符输出，遇到标点停顿
        - 最后输出推荐商品
        
        Args:
            question: 用户问题
            
        Yields:
            StreamChunk: 流式数据块
        """
        # 先获取完整答案
        result = self.get_answer(question)
        answer = result.answer
        products = result.products
        
        # 模拟打字机效果，逐字输出
        buffer = ""
        char_delay = 0.05  # 基础延迟50ms
        
        for i, char in enumerate(answer):
            buffer += char
            
            # 判断是否应该输出缓冲区
            # 为什么：过于频繁的输出会增加网络开销
            # 怎么做的：每5个字符或遇到标点符号时输出
            should_yield = (
                len(buffer) >= 5 or 
                char in '。！？.!?\n' or 
                i == len(answer) - 1
            )
            
            if should_yield:
                yield StreamChunk(type="text", content=buffer)
                buffer = ""
                # 标点符号后稍微停顿，模拟自然阅读停顿
                if char in '。！？.!?\n':
                    await asyncio.sleep(0.15)
                else:
                    await asyncio.sleep(char_delay * len(buffer) if buffer else char_delay)
        
        # 如果有推荐商品，最后输出商品信息
        if products:
            await asyncio.sleep(0.2)
            yield StreamChunk(type="products", products=products)
        
        # 发送结束标记
        yield StreamChunk(type="end")
    
    def _is_irrelevant(self, query: str) -> bool:
        """
        检测是否为无关消息
        
        为什么：避免AI回答与超市业务无关的问题
        怎么做的：检查查询中是否包含预定义的无关关键词列表
        
        Args:
            query: 用户查询（小写）
            
        Returns:
            bool: 是否为无关消息
        """
        irrelevant_keywords = [
            "天气", "股票", "新闻", "电影", "游戏", "彩票", "八卦",
            "唱歌", "跳舞", "运动", "健身", "减肥", "编程", "代码", "学习", "考试",
            "工作", "老板", "同事", "爱情", "恋爱", "结婚", "生孩子", "政治", "战争",
            "基金", "比特币", "区块链", "ai", "人工智能", "机器人", "聊天",
            "画画", "写字", "读书", "小说", "电视剧", "综艺",
            "笑话", "段子", "绯闻", "明星", "娱乐圈", "体育", "篮球", "足球"
        ]
        
        return any(keyword in query for keyword in irrelevant_keywords)
    
    def _calculate_relevance(self, query: str, doc: KnowledgeDocument) -> int:
        """
        计算查询与文档的相关度分数
        
        为什么：需要量化查询与知识条目的匹配程度
        怎么做的：
        - 关键词匹配（最高权重）
        - 问题文本相似度
        - 模糊匹配（前缀匹配）
        - 推荐类问题特殊处理
        
        Args:
            query: 用户查询
            doc: 知识文档
            
        Returns:
            int: 相关度分数
        """
        score = 0
        
        # 1. 关键词匹配（最高权重）
        # 为什么：关键词是预先定义的重要匹配词
        # 怎么做的：每个匹配的关键词加15分
        for keyword in doc.keywords:
            if keyword.lower() in query:
                score += 15
        
        # 2. 问题文本相似度
        # 为什么：用户可能直接输入问题文本
        # 怎么做的：检查查询是否包含问题或被问题包含
        if query in doc.question.lower() or doc.question.lower() in query:
            score += 20
        
        # 3. 模糊匹配：任意关键词的部分字符匹配
        # 为什么：处理用户输入不完全匹配的情况
        # 怎么做的：检查关键词前2个字符是否在查询中
        for keyword in doc.keywords:
            if len(keyword) >= 2:
                prefix = keyword[:2]
                if prefix in query:
                    score += 3
        
        # 4. 推荐类问题的通用词匹配
        # 为什么：推荐类问题有特殊的触发词
        # 怎么做的：检查查询中是否包含推荐触发词且文档属于推荐分类
        recommend_triggers = ["来点", "买点", "看看", "推荐点", "介绍一下", "有什么", "推荐", "介绍"]
        for trigger in recommend_triggers:
            if trigger in query and doc.category_id == "recommend":
                score += 10
        
        return score
