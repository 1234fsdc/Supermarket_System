"""
智能体工具集模块

为什么创建这个文件：
- 定义AI可调用的工具函数
- 实现工具的描述和参数定义
- 支持LangChain的工具调用机制

怎么做的：
- 使用@tool装饰器定义工具
- 每个工具包含描述、参数和实现
- 提供多种类型的工具（RAG、天气、商品推荐等）
"""

import random
from datetime import datetime
from langchain_core.tools import tool
from rag.rag_service import RagSummarizeService

# 全局初始化RAG服务
# 为什么：避免每次工具调用都重新初始化
# 怎么做的：模块级别实例化RAG服务
rag = RagSummarizeService()


@tool(description="从向量存储中检索超市知识库，获取商品、订单、配送等信息")
def rag_summarize(query: str) -> str:
    """
    RAG知识库检索工具
    
    为什么：让AI能够检索超市知识库回答用户问题
    怎么做的：调用RAG服务的rag_summarize方法
    
    Args:
        query: 用户的查询问题
        
    Returns:
        str: 基于知识库的回答
    """
    return rag.rag_summarize(query)


@tool(description="获取指定城市的天气信息")
def get_weather(city: str) -> str:
    """
    天气查询工具
    
    为什么：提供天气信息，增强AI的实用性
    怎么做的：返回模拟的天气数据（实际可对接天气API）
    
    Args:
        city: 城市名称
        
    Returns:
        str: 天气信息字符串
    """
    return f"{city}今天天气晴朗，气温25-30℃，适合出行购物~"


@tool(description="获取用户所在城市")
def get_user_location() -> str:
    """
    用户定位工具
    
    为什么：获取用户位置信息，提供本地化服务
    怎么做的：返回默认城市（实际可对接定位服务）
    
    Returns:
        str: 城市名称
    """
    return "北京市"


@tool(description="获取当前时间")
def get_current_time() -> str:
    """
    获取当前时间
    
    为什么：让AI知道当前时间，回答时效性问题
    怎么做的：使用datetime获取当前时间并格式化
    
    Returns:
        str: 当前时间字符串
    """
    return datetime.now().strftime("%Y年%m月%d日 %H:%M")


@tool(description="根据需求推荐商品，如零食、水果、饮料等")
def recommend_products(category: str) -> str:
    """
    商品推荐工具
    
    为什么：根据用户需求推荐相关商品
    怎么做的：维护商品字典，根据分类返回推荐列表
    
    Args:
        category: 商品类别，如"零食"、"水果"、"饮料"、"蔬菜"、"肉类"
        
    Returns:
        str: 推荐商品列表
    """
    # 商品推荐数据
    # 为什么：硬编码常用商品数据，快速响应推荐请求
    # 怎么做的：使用字典按分类组织商品数据
    products = {
        "零食": [
            {"name": "乐事薯片", "price": 12.9, "desc": "经典原味大包装"},
            {"name": "奥利奥饼干", "price": 9.9, "desc": "夹心美味"},
            {"name": "三只松鼠坚果", "price": 39.9, "desc": "健康营养礼包"},
        ],
        "水果": [
            {"name": "红富士苹果", "price": 8.9, "desc": "脆甜多汁 500g"},
            {"name": "进口香蕉", "price": 5.9, "desc": "营养方便 500g"},
            {"name": "阳光玫瑰葡萄", "price": 29.9, "desc": "香甜爽口 500g"},
        ],
        "饮料": [
            {"name": "可口可乐", "price": 18.9, "desc": "冰爽6瓶装"},
            {"name": "农夫山泉", "price": 2.5, "desc": "纯净550ml"},
            {"name": "伊利纯牛奶", "price": 5.9, "desc": "营养250ml"},
        ],
        "蔬菜": [
            {"name": "有机西红柿", "price": 6.9, "desc": "自然成熟 500g"},
            {"name": "新鲜生菜", "price": 3.9, "desc": "清脆爽口 1颗"},
            {"name": "嫩黄瓜", "price": 4.9, "desc": "脆嫩可口 500g"},
        ],
        "肉类": [
            {"name": "新鲜五花肉", "price": 29.9, "desc": "肥瘦相间 500g"},
            {"name": "鸡腿肉", "price": 19.9, "desc": "鲜嫩多汁 500g"},
            {"name": "精品牛肉", "price": 49.9, "desc": "优质蛋白 500g"},
        ],
    }
    
    # 根据分类返回推荐结果
    if category in products:
        items = products[category]
        result = f"为您推荐以下{category}：\n"
        for i, item in enumerate(items, 1):
            result += f"{i}. {item['name']} - ¥{item['price']} ({item['desc']})\n"
        return result
    else:
        return f"抱歉，暂时没有{category}的推荐，您可以浏览首页查看更多商品。"
