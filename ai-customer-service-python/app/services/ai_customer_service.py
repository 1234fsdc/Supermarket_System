import json
import logging
import asyncio
from typing import List, Optional, AsyncGenerator
from dataclasses import dataclass, field

from app.schemas.customer_service import CustomerServiceResult, ProductRecommend, StreamChunk

logger = logging.getLogger(__name__)


@dataclass
class ProductInfo:
    """商品信息"""
    name: str
    price: float
    desc: str


@dataclass
class KnowledgeDocument:
    """知识库文档实体"""
    id: str
    question: str
    answer: str
    keywords: List[str]
    category_id: str
    products: List[ProductInfo] = field(default_factory=list)


class AiCustomerService:
    """AI客服服务实现类 - 基于自定义RAG方案"""
    
    def __init__(self):
        self.knowledge_base: List[KnowledgeDocument] = []
        self._load_knowledge_base()
    
    def _load_knowledge_base(self):
        """从JSON文件加载知识库"""
        try:
            with open('app/data/knowledge_base.json', 'r', encoding='utf-8') as f:
                root = json.load(f)
            
            categories = root.get('categories', [])
            
            for category in categories:
                category_id = category.get('id')
                documents = category.get('documents', [])
                
                for doc in documents:
                    knowledge_doc = KnowledgeDocument(
                        id=doc.get('id'),
                        question=doc.get('question'),
                        answer=doc.get('answer'),
                        keywords=doc.get('keywords', []),
                        category_id=category_id,
                        products=[]
                    )
                    
                    # 加载推荐商品
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
        
        Args:
            question: 用户问题
            
        Returns:
            匹配的答案（含推荐商品）
        """
        if not question or not question.strip():
            return CustomerServiceResult(
                answer="请输入您的问题，我将为您解答。"
            )
        
        query = question.strip().lower()
        
        # 检测是否为无关消息
        if self._is_irrelevant(query):
            logger.info(f"检测到无关消息: {query}")
            return CustomerServiceResult(
                answer="我主要是凡栋超市的AI客服，可以为您解答商品、订单、配送等问题，还可以为您推荐超市商品。请问有什么我可以帮您的吗？"
            )
        
        # RAG检索：关键词匹配
        max_score = 0
        best_match: Optional[KnowledgeDocument] = None
        
        for doc in self.knowledge_base:
            score = self._calculate_relevance(query, doc)
            if score > max_score:
                max_score = score
                best_match = doc
        
        if best_match and max_score > 0:
            logger.info(f"匹配到答案: {best_match.question} (得分: {max_score})")
            
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
        # 每个字符间隔30-80ms，模拟自然打字速度
        buffer = ""
        char_delay = 0.05  # 基础延迟50ms
        
        for i, char in enumerate(answer):
            buffer += char
            
            # 每5个字符或遇到标点符号时输出一次
            # 或者最后一个字符
            should_yield = (
                len(buffer) >= 5 or 
                char in '。！？.!?\n' or 
                i == len(answer) - 1
            )
            
            if should_yield:
                yield StreamChunk(type="text", content=buffer)
                buffer = ""
                # 标点符号后稍微停顿
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
        """检测是否为无关消息"""
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
        """计算查询与文档的相关度分数"""
        score = 0
        
        # 1. 关键词匹配（最高权重）- 单个关键词匹配即可
        for keyword in doc.keywords:
            if keyword.lower() in query:
                score += 15
        
        # 2. 问题文本相似度
        if query in doc.question.lower() or doc.question.lower() in query:
            score += 20
        
        # 3. 模糊匹配：任意关键词的部分字符匹配
        for keyword in doc.keywords:
            if len(keyword) >= 2:
                prefix = keyword[:2]
                if prefix in query:
                    score += 3
        
        # 4. 推荐类问题的通用词匹配
        recommend_triggers = ["来点", "买点", "看看", "推荐点", "介绍一下", "有什么", "推荐", "介绍"]
        for trigger in recommend_triggers:
            if trigger in query and doc.category_id == "recommend":
                score += 10
        
        return score
