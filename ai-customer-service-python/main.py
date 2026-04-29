"""
凡栋超市AI客服服务 - 主入口
启动FastAPI服务，加载知识库
"""
import os
import sys

# 添加项目根目录到路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

from app.api.v1.ai_customer import router as ai_customer_router
from rag.vector_store import VectorStoreService
from utils.logger_handler import logger


def load_knowledge_base():
    """加载知识库到向量数据库"""
    logger.info("正在加载知识库...")
    try:
        vs = VectorStoreService()
        vs.load_document()
        logger.info("知识库加载完成")
    except Exception as e:
        logger.error(f"知识库加载失败: {e}")


def create_app() -> FastAPI:
    """创建FastAPI应用"""
    app = FastAPI(
        title="凡栋超市AI客服服务",
        description="基于RAG的智能客服系统",
        version="1.0.0"
    )
    
    # 配置CORS
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )
    
    # 注册路由
    app.include_router(
        ai_customer_router,
        prefix="/user/ai-customer",
        tags=["AI客服"]
    )
    
    @app.on_event("startup")
    async def startup_event():
        """启动时加载知识库"""
        load_knowledge_base()
    
    return app


app = create_app()


if __name__ == "__main__":
    # 启动服务
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8083,
        reload=False,
        log_level="info"
    )
