"""
智能体工具集模块

定义AI可调用的工具函数
"""
import random
from datetime import datetime
from langchain_core.tools import tool
from rag.rag_service import RagSummarizeService

# 全局初始化RAG服务
rag = RagSummarizeService()


@tool(description="从向量存储中检索超市知识库，获取商品、订单、配送等信息")
def rag_summarize(query: str) -> str:
    """
    RAG知识库检索工具
    
    Args:
        query: 用户的查询问题
        
    Returns:
        基于知识库的回答
    """
    return rag.rag_summarize(query)


@tool(description="获取指定城市的天气信息")
def get_weather(city: str) -> str:
    """
    天气查询工具
    
    Args:
        city: 城市名称
        
    Returns:
        天气信息字符串
    """
    return f"{city}今天天气晴朗，气温25-30℃，适合出行购物~"


@tool(description="获取用户所在城市")
def get_user_location() -> str:
    """
    用户定位工具
    
    Returns:
        城市名称
    """
    return "北京市"


@tool(description="获取当前时间")
def get_current_time() -> str:
    """
    获取当前时间
    
    Returns:
        当前时间字符串
    """
    return datetime.now().strftime("%Y年%m月%d日 %H:%M")


@tool(description="根据需求推荐商品，如零食、水果、饮料等")
def recommend_products(category: str) -> str:
    """
    商品推荐工具
    
    Args:
        category: 商品类别，如"零食"、"水果"、"饮料"、"蔬菜"、"肉类"
        
    Returns:
        推荐商品列表
    """
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
    
    if category in products:
        items = products[category]
        result = f"为您推荐以下{category}：\n"
        for i, item in enumerate(items, 1):
            result += f"{i}. {item['name']} - ¥{item['price']} ({item['desc']})\n"
        return result
    else:
        return f"抱歉，暂时没有{category}的推荐，您可以浏览首页查看更多商品。"
