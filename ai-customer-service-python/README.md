# 凡栋超市 AI 客服服务 — 智能客服 + 智能导购

基于 **FastAPI** + **LangChain** + **通义千问 (Qwen3-max)** 的 AI 客服系统，支持**智能客服问答**和**智能商品导购**双模式。

## 架构概览

```
┌──────────────────────────────────────────────────────────┐
│                    用户输入 (HTTP/SSE)                      │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────┐
│                    AI 客服服务 (:8083)                       │
│                                                           │
│  ┌──────────────────────┐                                 │
│  │   IntentDetector     │  ← 关键词预过滤购物意图           │
│  │   意图检测 (双层)      │     (比价/推荐/分类/搜索/找一下)   │
│  └──────┬──────┬───────┘    预留 LLM 兜底 (未启用)         │
│         │      │                                            │
│    ┌────┘      └────┐                                       │
│    ▼                ▼                                       │
│ 客服模式          导购模式                                    │
│ (非购物意图)       (购物意图)                                  │
│    │                │                                        │
│    ▼                ▼                                        │
│ ┌────────────┐ ┌─────────────────┐                           │
│ │  Hybrid    │ │  ShoppingGuide  │  ← 调用 Java 后端         │
│ │  Retriever │ │  Service        │     /user/product/        │
│ │  (BM25 +   │ │  (httpx 并发)   │     search/list/detail    │
│ │   Dense)   │ └────────┬────────┘                           │
│ └──────┬─────┘          │                                    │
│        │                │                                    │
│        └───────┬────────┘                                    │
│                ▼                                             │
│  ┌─────────────────────────┐                                 │
│  │  ReactAgent              │                                 │
│  │  LLM (Qwen3-max) 流式生成 │  ← prompt 注入 RAG 上下文        │
│  │  prompt | llm | output   │     或实时商品数据               │
│  └────────────┬────────────┘                                 │
│               ▼                                              │
│  ┌─────────────────────────┐                                 │
│  │  SSE 流式响应             │                                 │
│  │  text / guide_card /    │                                 │
│  │  end / error            │                                 │
│  └─────────────────────────┘                                 │
└──────────────────────────────────────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────────────────────────┐
│  外部依赖                                                  │
│  · MongoDB — 对话历史持久化                                  │
│  · ChromaDB — 向量知识库 (Chroma)                          │
│  · Java 后端 (:8080) — 商品搜索/分类/详情 API              │
│  · DashScope — 通义千问 LLM + text-embedding-v4            │
└──────────────────────────────────────────────────────────┘
```

## 目录结构

```
ai-customer-service-python/
├── agent/
│   ├── react_agent.py              # 核心智能体 — 双模式 (客服 + 导购)
│   ├── intent_detector.py          # 购物意图检测器 (关键词双层)
│   └── tools/
│       ├── agent_tools.py          # 工具函数集
│       └── middleware.py           # 中间件
├── app/
│   ├── api/v1/
│   │   └── ai_customer.py         # FastAPI 路由 — SSE / ask / health
│   ├── config/
│   │   ├── __init__.py             # 配置聚合加载 (YAML + 常量)
│   │   ├── rag.yml                 # RAG / LLM / Embedding / Chroma 配置
│   │   ├── prompts.yml             # 系统提示词 (双模式)
│   │   └── shopping.yml           # 导购配置
│   ├── core/
│   │   ├── embeddings.py           # DashScope text-embedding-v4 封装
│   │   ├── hybrid_retriever.py     # 混合检索 (BM25 + Dense + RRF)
│   │   ├── llm.py                  # 通义千问 LLM 单例
│   │   ├── mongo_client.py         # MongoDB 客户端 (对话历史)
│   │   └── vectorstore.py          # Chroma 向量数据库封装
│   ├── data/
│   │   ├── knowledge_base.json     # 结构化知识库
│   │   └── __init__.py
│   ├── schemas/
│   │   ├── customer_service.py     # 客服数据模型 (AskResponse / StreamChunk)
│   │   └── shopping_guide.py       # 导购数据模型 (GuideType / ProductInfo / GuideResult)
│   ├── services/
│   │   ├── shopping_guide_service.py # 导购服务 — Java 后端商品 API 调用
│   │   └── __init__.py
│   ├── main.py                     # 应用入口 (被外层 main.py 引用)
│   └── __init__.py
├── chroma_db/                      # Chroma 向量库持久化目录
├── data/                           # 原始知识文档 (txt)
│   ├── 售后服务.txt
│   ├── 商品信息.txt
│   ├── 订单支付.txt
│   └── 超市配送.txt
├── prompts/                        # 原始 prompt 模板 (txt)
│   ├── main_prompt.txt
│   └── rag_summarize.txt
├── tests/                          # 33 个测试用例
│   ├── test_health.py              # 健康检查
│   ├── test_ask.py                 # 非流式问答
│   ├── test_user_api_e2e.py        # E2E 集成测试
│   ├── test_shopping_guide_service.py  # 导购服务 (7 tests)
│   ├── test_intent_detector.py         # 意图检测器 (15 tests)
│   ├── test_react_agent_guide.py       # 导购模式智能体 (5 tests)
│   ├── test_stream_guide.py            # SSE 导购流 (6 tests)
│   └── conftest.py                 # Pytest 共享 fixture
├── main.py                         # FastAPI 启动入口 (port 8083)
├── requirements.txt
├── pytest.ini
└── README.md
```

## 核心模块详解

### 1. ReactAgent (`agent/react_agent.py`)

智能体核心，**双模式设计**：

**客服模式**（默认）：
1. `HybridRetriever` 进行混合检索（BM25 + Chroma Dense + RRF 融合）
2. 从 MongoDB 加载对话历史
3. 构建 prompt 模板：`system + history + context(检索结果) + input`
4. LLM 流式生成（`prompt | llm | StrOutputParser`）
5. 保存对话到 MongoDB

**导购模式**（检测到购物意图时）：
1. `IntentDetector` 检测购物意图 → `guide_type`
2. `ShoppingGuideService.search_products()` 并行调用 Java 后端
3. 将商品列表格式化为 `guide_context` 注入 prompt
4. 保存 `last_guide_result` (GuideResult) 供 SSE 推送
5. 正常 LLM 流式生成 + 后续通过 SSE 发送 `guide_card` 事件

### 2. IntentDetector (`agent/intent_detector.py`)

**双层架构**（关键词预过滤 → [预留] LLM 兜底）：

**第一层 — 关键词匹配（已实现）**：
- 优先级链（第一个命中即返回）：
  | 关键词 | guide_type | 置信度 |
  |--------|-----------|--------|
  | 比价 / 性价比 | `price_compare` | 0.9 |
  | 分类 / 推荐 | `category_recommend` | 0.9 |
  | 搜索 / 找一下 | `search_suggest` | 0.9 |
  | 买点 / 有什么 | `multi_turn_guide` | 0.8 |
  | 好吃的 / 想喝 / 想要 / 看看 | `multi_turn_guide` | 0.6 |

**第二层 — LLM 兜底（预留，未启用）**：
- 面向关键词无法判定的模糊查询（如 "我想买点东西"）
- 故意延迟实现以避免测试环境依赖 LLM 凭证

### 3. HybridRetriever (`app/core/hybrid_retriever.py`)

双通道混合检索 + RRF（Reciprocal Rank Fusion）融合：

| 通道 | 方法 | 擅长 |
|------|------|------|
| **BM25** | jieba 分词 + `rank_bm25` 倒排索引 | 精确关键词匹配（商品名、订单号） |
| **Dense** | Chroma 向量库 + text-embedding-v4 | 语义相似度（同义词、改述） |

**RRF 融合**：
- 公式：`score(d) = Σ 1/(k + rankᵢ(d))`，k=60（论文推荐值）
- 不依赖跨通道分数归一化，只看排名，鲁棒性强
- 数据源：`app/data/knowledge_base.json`（结构化 QA + 关键词 + 关联商品）

### 4. ShoppingGuideService (`app/services/shopping_guide_service.py`)

负责与 **Java 后端** 商品 API 通信：

| 方法 | Java 后端路径 | 说明 |
|------|--------------|------|
| `search_products(keyword)` | `GET /user/product/search?keyword=` | 关键词搜索商品 |
| `get_category_products(category_id)` | `GET /user/product/list?categoryId=` | 按分类获取商品 |
| `get_product_detail(product_id)` | `GET /user/product/detail?id=` | 获取商品详情 |

**字段映射**：Java 后端使用 camelCase（`categoryId` / `promoTag`），Python 端使用 snake_case，请求时自动转换。

**异常安全**：所有网络异常（超时/连接失败/HTTP 错误）均被捕获并返回空列表，保证客服模式不受影响。

### 5. SSE 流式协议 (`app/api/v1/ai_customer.py`)

流式响应支持 5 种事件类型：

| 事件类型 | 说明 | 前端处理 |
|---------|------|---------|
| `text` | LLM 生成的文本片段 | 追加到对话气泡 |
| `guide_card` | 导购商品卡片（含 product_id/name/price/image） | 渲染内联商品卡片 |
| `end` | 流式结束标记 | 停止 loading，启用输入 |
| `error` | 错误信息 | 显示错误提示 |

向后兼容：旧版 `products` 事件类型仍被保留。

### 6. 数据模型 (`app/schemas/`)

**客服模型** (`customer_service.py`)：
- `AskResponse` — 非流式响应（含 `guide_products` / `guide_type`）
- `StreamChunk` — 流式块（`type`/`content`/`guide_products`/`guide_type`）
- `Result[T]` — 通用 API 响应封装（`code`/`msg`/`data`）

**导购模型** (`shopping_guide.py`)：
- `GuideType` — 导购类型枚举（`category_recommend` / `multi_turn_guide` / `search_suggest` / `price_compare`）
- `ProductInfo` — 商品信息（id/name/price/image/description/category/sales/stock/rating/promo_tag）
- `GuideResult` — 导购结果（guide_type + products + guide_message）

## API 接口

所有接口前缀：`/user/ai-customer`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/health` | 健康检查 / 组件状态 |
| `GET` | `/ask?question=&session_id=` | 非流式问答 |
| `GET` | `/ask/stream?question=&session_id=` | **SSE 流式问答**（主要接口） |
| `POST` | `/session/clear?session_id=` | 清空会话历史 |

**请求参数**：
- `question` (必填) — 用户输入
- `session_id` (可选) — 会话标识（服务端自动生成）

## 启动与配置

### 环境变量

```bash
# 必填 — 阿里云 DashScope API Key
export DASHSCOPE_API_KEY="your-dashscope-api-key"
```

### 启动服务

```bash
cd ai-customer-service-python

# 安装依赖
pip install -r requirements.txt

# 启动（端口 8083）
python main.py
```

### 配置文件

| 文件 | 内容 |
|------|------|
| `app/config/rag.yml` | LLM 模型/温度/最大 token、Embedding 模型、Chroma 集合名/路径、RAG top_k/阈值/分片参数 |
| `app/config/prompts.yml` | 系统提示词（含双模式 prompt 模板） |
| `app/config/shopping.yml` | 导购服务配置（Java 后端 URL / 超时 / 缓存） |

## 测试

```bash
cd ai-customer-service-python

# 运行全部 33 个测试
pytest -v

# 按模块运行
pytest tests/test_intent_detector.py -v          # 意图检测 (15)
pytest tests/test_shopping_guide_service.py -v   # 导购服务 (7)
pytest tests/test_react_agent_guide.py -v        # 导购智能体 (5)
pytest tests/test_stream_guide.py -v             # SSE 导购流 (6)
pytest tests/test_ask.py -v                      # 问答接口
pytest tests/test_health.py -v                   # 健康检查
pytest tests/test_user_api_e2e.py -v             # E2E 集成
```

## 依赖服务

| 服务 | 默认地址 | 说明 |
|------|---------|------|
| **DashScope API** | `https://dashscope.aliyun.com` | 通义千问 LLM + text-embedding-v4 |
| **MongoDB** | `localhost:27017` | 对话历史持久化 |
| **ChromaDB** | 本地文件 `chroma_db/` | 向量知识库 |
| **Java 后端** | `localhost:8080` | 商品搜索/分类/详情 API |

## 设计决策

| 决策 | 选择 | 理由 |
|------|------|------|
| Prompt 链 | `prompt \| llm \| StrOutputParser` | 保持 LCEL 原生，不引入 LangGraph AgentExecutor 复杂度 |
| 导购服务注入 | 通过 `guide_service` 参数注入 | 保持 ReactAgent 可测试性，不依赖全局单例 |
| 意图检测 | 关键词预过滤 + LLM 兜底（预留） | 关键词层无 I/O、确定性、适合测试；LLM 层仅用于模糊查询 |
| 字段映射 | Python snake_case ↔ Java camelCase | 遵守各语言社区命名规范 |
| 异常降级 | 导购网络异常返回空列表 | 保证客服模式 100% 不受导购模块影响 |
| SSE 事件 | `guide_card`（新）+ `products`（旧） | 新版导购卡片向后兼容旧版推荐格式 |
