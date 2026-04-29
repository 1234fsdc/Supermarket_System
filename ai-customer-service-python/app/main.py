from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.v1 import ai_customer

app = FastAPI(
    title="AI客服服务",
    description="凡栋超市AI客服系统 - Python版本",
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
app.include_router(ai_customer.router, prefix="/user/ai-customer", tags=["AI客服接口"])

@app.get("/")
async def root():
    return {"message": "AI客服服务运行中"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8081)
