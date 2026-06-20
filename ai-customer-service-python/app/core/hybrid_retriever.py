"""
混合检索服务 - BM25稀疏检索 + Dense向量检索 + RRF融合

为什么创建这个文件：
- 替代原来的双RAG（JSON关键词 + 向量）独立运行模式
- 将BM25和向量检索在同一查询中融合，提升召回质量
- BM25处理精确关键词匹配（商品名、订单号等）
- Dense向量处理语义相似度（同义词、改述等）

怎么做的：
- BM25：从knowledge_base.json加载文档，jieba分词，建立倒排索引
- Dense：复用现有Chroma向量库
- RRF：倒数排名融合，将两个通道的排名合并为最终得分
"""

import json
import logging
import jieba
from typing import List, Dict, Optional, Tuple
from dataclasses import dataclass, field

import numpy as np
from rank_bm25 import BM25Okapi
from langchain_core.documents import Document

from app.core.embeddings import get_embeddings
from app.config import CHROMA_CONFIG, RAG_CONFIG

logger = logging.getLogger(__name__)


@dataclass
class RetrievedDoc:
    """检索结果文档"""
    content: str
    source: str  # "bm25" | "dense" | "both"
    score: float
    metadata: Dict = field(default_factory=dict)


class HybridRetriever:
    """
    混合检索器：BM25 + Dense向量 + RRF融合

    核心流程：
    1. BM25检索：基于关键词的稀疏检索，擅长精确匹配
    2. Dense检索：基于向量相似度的稠密检索，擅长语义理解
    3. RRF融合：将两个通道的排名结果融合为统一排序
    """

    def __init__(self):
        self.bm25_corpus: List[str] = []
        self.bm25_metadata: List[Dict] = []
        self.bm25_index: Optional[BM25Okapi] = None
        self.dense_store = None
        self._initialized = False

    def initialize(self):
        """初始化BM25索引和Chroma向量库"""
        if self._initialized:
            return

        self._build_bm25_index()
        self._init_dense_store()
        self._initialized = True
        logger.info("混合检索器初始化完成")

    def _build_bm25_index(self):
        """
        从knowledge_base.json构建BM25索引

        为什么用JSON而不是Chroma：
        - BM25需要原始文本建立倒排索引
        - JSON知识库结构清晰，方便提取question+answer+keywords
        """
        try:
            with open('app/data/knowledge_base.json', 'r', encoding='utf-8') as f:
                data = json.load(f)

            corpus = []
            metadata = []

            for category in data.get('categories', []):
                cat_id = category.get('id', '')
                cat_name = category.get('name', '')

                for doc in category.get('documents', []):
                    question = doc.get('question', '')
                    answer = doc.get('answer', '')
                    keywords = ' '.join(doc.get('keywords', []))

                    # 拼接为检索文本：问题 + 关键词 + 答案
                    text = f"{question} {keywords} {answer}"
                    corpus.append(text)

                    metadata.append({
                        'id': doc.get('id', ''),
                        'category_id': cat_id,
                        'category_name': cat_name,
                        'question': question,
                        'answer': answer,
                        'products': doc.get('products', []),
                    })

            # jieba分词
            tokenized_corpus = [list(jieba.cut(text)) for text in corpus]

            # 构建BM25索引
            self.bm25_corpus = corpus
            self.bm25_metadata = metadata
            self.bm25_index = BM25Okapi(tokenized_corpus)

            logger.info(f"BM25索引构建完成，共 {len(corpus)} 条文档")

        except Exception as e:
            logger.error(f"构建BM25索引失败: {e}")

    def _init_dense_store(self):
        """初始化Chroma向量库"""
        try:
            from langchain_chroma import Chroma
            from app.core.embeddings import get_embeddings

            embedder = get_embeddings()
            self.dense_store = Chroma(
                collection_name=CHROMA_CONFIG.get("collection_name", "supermarket_knowledge"),
                embedding_function=embedder,
            )
            logger.info("Dense向量库连接完成")
        except Exception as e:
            logger.error(f"Dense向量库连接失败: {e}")

    def retrieve(self, query: str, top_k: int = 3) -> List[RetrievedDoc]:
        """
        混合检索：BM25 + Dense + RRF融合

        Args:
            query: 用户查询
            top_k: 返回结果数量

        Returns:
            List[RetrievedDoc]: 融合排序后的检索结果
        """
        if not self._initialized:
            self.initialize()

        # 两个通道分别检索
        bm25_results = self._bm25_search(query, top_k=top_k * 2)
        dense_results = self._dense_search(query, top_k=top_k * 2)

        # RRF融合
        fused_results = self._rrf_fusion(bm25_results, dense_results, top_k=top_k)

        logger.info(f"混合检索完成: query='{query}', "
                     f"bm25={len(bm25_results)}, dense={len(dense_results)}, "
                     f"fused={len(fused_results)}")

        return fused_results

    def _bm25_search(self, query: str, top_k: int = 5) -> List[Tuple[int, float]]:
        """
        BM25稀疏检索

        Args:
            query: 查询文本
            top_k: 返回数量

        Returns:
            List[Tuple[文档索引, BM25得分]]
        """
        if self.bm25_index is None:
            return []

        # jieba分词
        tokenized_query = list(jieba.cut(query))
        scores = self.bm25_index.get_scores(tokenized_query)

        # 取top_k
        top_indices = np.argsort(scores)[::-1][:top_k]
        results = [(idx, float(scores[idx])) for idx in top_indices if scores[idx] > 0]

        return results

    def _dense_search(self, query: str, top_k: int = 5) -> List[Tuple[str, float, Dict]]:
        """
        Dense向量检索

        Args:
            query: 查询文本
            top_k: 返回数量

        Returns:
            List[Tuple[文档内容, 相似度得分, 元数据]]
        """
        if self.dense_store is None:
            return []

        try:
            results = self.dense_store.similarity_search_with_relevance_scores(query, k=top_k)
            formatted = []
            for doc, score in results:
                formatted.append((doc.page_content, float(score), doc.metadata))
            return formatted
        except Exception as e:
            logger.error(f"Dense检索失败: {e}")
            return []

    def _rrf_fusion(
        self,
        bm25_results: List[Tuple[int, float]],
        dense_results: List[Tuple[str, float, Dict]],
        top_k: int = 3,
        k: int = 60
    ) -> List[RetrievedDoc]:
        """
        Reciprocal Rank Fusion (RRF) 融合算法

        公式：score(d) = sum(1 / (k + rank_i(d))) for each retrieval channel

        为什么用RRF：
        - 不需要归一化不同通道的分数（BM25得分和余弦相似度量纲不同）
        - 只看排名，鲁棒性强
        - k=60是论文推荐值

        Args:
            bm25_results: BM25检索结果 [(文档索引, 得分)]
            dense_results: Dense检索结果 [(文档内容, 得分, 元数据)]
            top_k: 最终返回数量
            k: RRF参数，控制排名权重衰减

        Returns:
            List[RetrievedDoc]: 融合后的检索结果
        """
        # 文档ID -> RRF得分
        doc_scores: Dict[str, float] = {}
        # 文档ID -> RetrievedDoc（用于去重和存储）
        doc_map: Dict[str, RetrievedDoc] = {}

        # BM25通道：用索引作为文档ID
        for rank, (idx, _score) in enumerate(bm25_results):
            doc_id = f"bm25_{idx}"
            rrf_score = 1.0 / (k + rank + 1)
            doc_scores[doc_id] = doc_scores.get(doc_id, 0) + rrf_score

            if doc_id not in doc_map:
                meta = self.bm25_metadata[idx] if idx < len(self.bm25_metadata) else {}
                doc_map[doc_id] = RetrievedDoc(
                    content=meta.get('answer', self.bm25_corpus[idx] if idx < len(self.bm25_corpus) else ''),
                    source='bm25',
                    score=rrf_score,
                    metadata=meta,
                )

        # Dense通道：用文档内容的前50个字符作为简易ID（Chroma没暴露稳定ID）
        for rank, (content, _score, metadata) in enumerate(dense_results):
            doc_id = f"dense_{content[:50]}"
            rrf_score = 1.0 / (k + rank + 1)
            doc_scores[doc_id] = doc_scores.get(doc_id, 0) + rrf_score

            if doc_id not in doc_map:
                # 标记同时被两个通道召回的文档
                source = 'dense'
                if any(did.startswith('bm25_') and doc_map.get(did, None)
                       and doc_map[did].metadata.get('question') == metadata.get('question')
                       for did in doc_scores):
                    source = 'both'

                doc_map[doc_id] = RetrievedDoc(
                    content=content,
                    source=source,
                    score=rrf_score,
                    metadata=metadata,
                )

        # 按RRF得分降序排序
        sorted_ids = sorted(doc_scores.keys(), key=lambda x: doc_scores[x], reverse=True)

        # 取top_k并更新最终得分
        results = []
        for doc_id in sorted_ids[:top_k]:
            doc = doc_map[doc_id]
            doc.score = doc_scores[doc_id]
            results.append(doc)

        return results

    def get_context_string(self, query: str, top_k: int = 3) -> str:
        """
        获取格式化的上下文字符串（供LLM prompt使用）

        Args:
            query: 用户查询
            top_k: 返回数量

        Returns:
            str: 格式化的上下文字符串
        """
        results = self.retrieve(query, top_k=top_k)

        if not results:
            return ""

        context_parts = []
        for i, doc in enumerate(results, 1):
            source_tag = f"[{doc.source}]" if doc.source != 'both' else "[混合命中]"
            context_parts.append(f"【参考资料{i}】{source_tag}: {doc.content}")

        return "\n".join(context_parts)

    def get_products_from_results(self, query: str, top_k: int = 3) -> List[Dict]:
        """
        从检索结果中提取推荐商品

        Args:
            query: 用户查询
            top_k: 检索数量

        Returns:
            List[Dict]: 商品列表
        """
        results = self.retrieve(query, top_k=top_k)
        products = []
        seen = set()

        for doc in results:
            for p in doc.metadata.get('products', []):
                name = p.get('name', '')
                if name and name not in seen:
                    seen.add(name)
                    products.append(p)

        return products
