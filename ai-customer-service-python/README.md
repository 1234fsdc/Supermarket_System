# AI客服系统 - 凡栋超市

基于LangChain + 通义千问 + Chroma + Redis的新一代AI客服系统

## 技术栈

### 1. 基础框架
- **Python 3.13** - 核心开发语言
- **FastAPI** - Web框架
- **Uvicorn** - ASGI服务器

### 2. AI框架与大模型
- **LangChain** - AI应用开发框架
- **通义千问 (Qwen3-max)** - 阿里云大语言模型
- **DashScope** - 阿里云AI服务，提供text-embedding-v4嵌入模型

### 3. 向量数据库与RAG
- **Chroma** - 开源向量数据库
- **LangChain-Chroma** - Chroma的LangChain集成
- **RecursiveCharacterTextSplitter** - 文档分片处理

### 4. 数据存储
- **Redis** - 对话历史存储
- **JSON** - 知识库文件

### 5. 配置管理
- **YAML** - 配置文件格式

## 项目结构

```
ai-customer-service-python/
├── app/
│   ├── api/v1/
│   │   └── ai_customer.py      # API接口
│   ├── config/
│   │   ├── __init__.py         # 配置加载
│   │   ├── rag.yml             # RAG配置
│   │   └── prompts.yml         # 提示词配置
│   ├── core/
│   │   ├── __init__.py
│   │   ├── embeddings.py       # DashScope Embedding
│   │   ├── llm.py              # 通义千问LLM
│   │   ├── vectorstore.py      # Chroma向量数据库
│   │   └── redis_client.py     # Redis客户端
│   ├── data/
│   │   ├── knowledge_base.json # 知识库
│   │   └── chroma_db/          # 向量数据库持久化
│   ├── schemas/
│   │   └── customer_service.py # 数据模型
│   ├── services/
│   │   ├── ai_customer_service.py  # 旧版服务（保留）
│   │   └── rag_service.py          # 新版RAG服务
│   ├── main.py                 # 应用入口
│   └── __init__.py
├── requirements.txt
└── README.md
```

## 核心功能

### 1. 语义检索（RAG）
- 使用DashScope text-embedding-v4进行文本向量化
- Chroma向量数据库存储和检索
- 相似度搜索替代关键词匹配

### 2. 大模型生成
- 通义千问Qwen3-max生成回答
- 支持流式输出（打字机效果）
- 支持多轮对话上下文

### 3. 对话历史
- Redis存储多轮对话
- 会话过期时间：1小时
- 最大历史消息数：10条

### 4. 商品推荐
- 知识库关联商品信息
- 自动提取推荐商品

## API接口

### 非流式接口
```
GET /user/ai-customer/ask?question=配送费是多少&session_id=xxx
```

### 流式接口
```
GET /user/ai-customer/ask/stream?question=配送费是多少&session_id=xxx
```

### 清空会话
```
POST /user/ai-customer/session/clear?session_id=xxx
```

### 健康检查
```
GET /user/ai-customer/health
```

## 配置说明

### 环境变量
```bash
# 必须配置
export DASHSCOPE_API_KEY="your-dashscope-api-key"

# 可选配置
export REDIS_HOST="localhost"
export REDIS_PORT="6379"
```

### 配置文件
- `app/config/rag.yml` - RAG、Embedding、LLM、Chroma、Redis配置
- `app/config/prompts.yml` - 系统提示词配置

## 启动服务

```bash
# 安装依赖
pip install -r requirements.txt

# 启动服务
python -m app.main

# 或使用uvicorn
uvicorn app.main:app --host 0.0.0.0 --port 8082 --reload
```

## 依赖服务

1. **Redis** - 用于对话历史存储
   ```bash
   # Docker启动Redis
   docker run -d -p 6379:6379 --name redis redis:latest
   ```

2. **DashScope API Key** - 阿里云大模型服务
   - 访问 https://dashscope.aliyun.com/ 获取API Key

## 升级说明

### 从旧版升级
1. 安装新依赖（langchain, chromadb, dashscope, redis等）
2. 配置环境变量 `DASHSCOPE_API_KEY`
3. 启动Redis服务
4. 运行服务，自动初始化向量数据库

### 数据迁移
- 知识库JSON格式不变，自动导入到Chroma
- 向量数据库自动持久化到 `app/data/chroma_db`

## 性能优化

- Embedding批量处理（每次最多25条）
- 向量检索Top-K + 相似度阈值过滤
- Redis连接池
- 流式输出减少等待时间

## 监控

访问 `/user/ai-customer/health` 查看各组件状态：
- embeddings: Embedding服务状态
- vectorstore: 向量数据库状态
- redis: Redis连接状态
- llm: 大模型服务状态
