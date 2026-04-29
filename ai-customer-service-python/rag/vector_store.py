"""
向量存储服务模块

为什么创建这个文件：
- 基于Chroma的向量数据库服务
- 提供文档存储和语义检索能力
- 支持文档去重和增量更新

怎么做的：
- 使用Chroma作为向量数据库
- 使用MD5校验避免重复加载
- 支持PDF和TXT文件
"""

import os
from langchain_chroma import Chroma
from langchain_core.documents import Document
from utils.config_handler import chroma_conf
from model.factory import embed_model
from langchain_text_splitters import RecursiveCharacterTextSplitter
from utils.path_tool import get_abs_path
from utils.file_handler import pdf_loader, txt_loader, listdir_with_allowed_type, get_file_md5_hex
from utils.logger_handler import logger


class VectorStoreService:
    """
    向量存储服务类
    
    为什么：封装向量数据库的所有操作
    怎么做的：
    - 初始化Chroma向量存储
    - 配置文本分割器
    - 提供文档加载和检索方法
    """
    
    def __init__(self):
        """
        初始化向量存储服务
        
        为什么：需要配置向量数据库和文本分割器
        怎么做的：
        - 使用配置创建Chroma实例
        - 配置RecursiveCharacterTextSplitter分割器
        """
        # 初始化Chroma向量存储
        # 为什么：需要持久化存储向量数据
        # 怎么做的：指定collection_name、embedding_function和persist_directory
        self.vector_store = Chroma(
            collection_name=chroma_conf["collection_name"],
            embedding_function=embed_model,
            persist_directory=get_abs_path(chroma_conf["persist_directory"]),
        )
        
        # 初始化文本分割器
        # 为什么：长文档需要分割成小块以便检索
        # 怎么做的：配置RecursiveCharacterTextSplitter参数
        self.spliter = RecursiveCharacterTextSplitter(
            chunk_size=chroma_conf["chunk_size"],  # 每块最大字符数
            chunk_overlap=chroma_conf["chunk_overlap"],  # 块之间重叠字符数
            separators=chroma_conf["separators"],  # 分隔符优先级列表
            length_function=len,  # 计算长度的函数
        )

    def get_retriever(self):
        """
        获取检索器
        
        为什么：对外提供文档检索能力
        怎么做的：将向量存储转换为检索器，配置返回数量k
        
        Returns:
            Retriever: 文档检索器
        """
        return self.vector_store.as_retriever(search_kwargs={"k": chroma_conf["k"]})

    def load_document(self):
        """
        加载文档到向量库
        
        为什么：将知识文档导入向量数据库
        怎么做的：
        - 遍历数据目录
        - 使用MD5校验去重
        - 分割文档并存储
        """
        
        def check_md5_hex(md5_for_check: str) -> bool:
            """
            检查MD5是否已处理过
            
            为什么：避免重复加载相同文件
            怎么做的：读取MD5记录文件，检查是否包含给定MD5
            
            Args:
                md5_for_check: 待检查的MD5值
                
            Returns:
                bool: 是否已处理过
            """
            md5_file = get_abs_path(chroma_conf["md5_hex_store"])
            if not os.path.exists(md5_file):
                # 文件不存在则创建空文件
                open(md5_file, "w", encoding="utf-8").close()
                return False
            
            with open(md5_file, "r", encoding="utf-8") as f:
                for line in f.readlines():
                    if line.strip() == md5_for_check:
                        return True
            return False

        def save_md5_hex(md5_for_check: str):
            """
            保存MD5到记录文件
            
            为什么：记录已处理的文件
            怎么做的：追加写入MD5值到文件
            
            Args:
                md5_for_check: MD5值
            """
            md5_file = get_abs_path(chroma_conf["md5_hex_store"])
            with open(md5_file, "a", encoding="utf-8") as f:
                f.write(md5_for_check + "\n")

        def get_file_documents(read_path: str):
            """
            根据文件类型选择加载器
            
            为什么：不同文件类型需要不同的加载器
            怎么做的：根据文件扩展名选择对应的加载函数
            
            Args:
                read_path: 文件路径
                
            Returns:
                list[Document]: 文档列表
            """
            if read_path.endswith("txt"):
                return txt_loader(read_path)
            if read_path.endswith("pdf"):
                return pdf_loader(read_path)
            return []

        # 获取待处理文件列表
        # 为什么：需要知道哪些文件需要加载
        # 怎么做的：遍历数据目录，筛选允许的文件类型
        data_path = get_abs_path(chroma_conf["data_path"])
        allowed_files_path = listdir_with_allowed_type(
            data_path,
            tuple(chroma_conf["allow_knowledge_file_type"]),
        )

        # 遍历处理每个文件
        for path in allowed_files_path:
            # 计算文件MD5
            md5_hex = get_file_md5_hex(path)
            
            # 检查是否已处理过
            if check_md5_hex(md5_hex):
                logger.info(f"[加载知识库]{path}内容已存在，跳过")
                continue

            try:
                # 加载文档
                documents = get_file_documents(path)
                if not documents:
                    logger.warning(f"[加载知识库]{path}无有效内容，跳过")
                    continue

                # 文档分片
                # 为什么：长文档需要分割以便检索
                # 怎么做的：使用RecursiveCharacterTextSplitter分割
                split_document = self.spliter.split_documents(documents)
                if not split_document:
                    logger.warning(f"[加载知识库]{path}分片后无有效内容，跳过")
                    continue

                # 存入向量库
                self.vector_store.add_documents(split_document)
                # 保存MD5记录
                save_md5_hex(md5_hex)
                logger.info(f"[加载知识库]{path}加载成功")
                
            except Exception as e:
                logger.error(f"[加载知识库]{path}加载失败：{e}", exc_info=True)
                continue
