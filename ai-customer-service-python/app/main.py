"""
FastAPI应用主入口模块

为什么创建这个文件：
- 作为AI客服服务的启动入口
- 统一配置FastAPI应用实例和中间件
- 集中管理路由注册

怎么做的：
- 使用FastAPI框架创建Web应用
- 配置CORS跨域支持，允许微信小程序访问
- 注册AI客服路由模块
"""

# 从fastapi导入FastAPI类，用于创建Web应用实例
from fastapi import FastAPI
# 导入CORS中间件，用于处理跨域请求
from fastapi.middleware.cors import CORSMiddleware
# 导入AI客服路由模块
from app.api.v1 import ai_customer

# 创建FastAPI应用实例
# 为什么：需要配置应用元数据（标题、描述、版本）供API文档使用
# 怎么做的：使用FastAPI构造函数，传入title、description、version参数
app = FastAPI(
    title="AI客服服务",
    description="凡栋超市AI客服系统 - Python版本",
    version="1.0.0"
)

# 配置CORS跨域支持
# 为什么：微信小程序前端需要调用此后端服务，涉及跨域访问
# 怎么做的：添加CORSMiddleware中间件，允许所有来源、所有方法、所有请求头
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 允许所有来源访问（生产环境应限制为特定域名）
    allow_credentials=True,  # 允许携带凭证（如Cookie）
    allow_methods=["*"],  # 允许所有HTTP方法（GET、POST等）
    allow_headers=["*"],  # 允许所有请求头
)

# 注册AI客服路由
# 为什么：将业务逻辑按模块划分，便于维护
# 怎么做的：使用include_router方法，指定前缀和标签
# prefix: 所有该路由下的接口路径前都会加上/user/ai-customer
# tags: 用于API文档分组显示
app.include_router(ai_customer.router, prefix="/user/ai-customer", tags=["AI客服接口"])

# 根路径健康检查接口
# 为什么：提供简单的服务状态检查端点
# 怎么做的：使用@app.get装饰器定义GET请求处理函数
@app.get("/")
async def root():
    # 返回JSON格式的服务状态信息
    return {"message": "AI客服服务运行中"}

# 主程序入口
# 为什么：允许直接运行此文件启动服务
# 怎么做的：使用uvicorn作为ASGI服务器运行FastAPI应用
if __name__ == "__main__":
    import uvicorn
    # host="0.0.0.0" 允许外部访问
    # port=8081 指定服务端口
    uvicorn.run(app, host="0.0.0.0", port=8081)
