"""
向量存储服务模块

基于Chroma的向量数据库服务，提供文档存储和语义检索
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
    """向量存储服务类"""
    
    def __init__(self):
        """初始化向量存储服务"""
        self.vector_store = Chroma(
            collection_name=chroma_conf["collection_name"],
            embedding_function=embed_model,
            persist_directory=get_abs_path(chroma_conf["persist_directory"]),
        )
        
        self.spliter = RecursiveCharacterTextSplitter(
            chunk_size=chroma_conf["chunk_size"],
            chunk_overlap=chroma_conf["chunk_overlap"],
            separators=chroma_conf["separators"],
            length_function=len,
        )

    def get_retriever(self):
        """获取检索器"""
        return self.vector_store.as_retriever(search_kwargs={"k": chroma_conf["k"]})

    def load_document(self):
        """加载文档到向量库"""
        
        def check_md5_hex(md5_for_check: str) -> bool:
            """检查MD5是否已处理过"""
            md5_file = get_abs_path(chroma_conf["md5_hex_store"])
            if not os.path.exists(md5_file):
                open(md5_file, "w", encoding="utf-8").close()
                return False
            
            with open(md5_file, "r", encoding="utf-8") as f:
                for line in f.readlines():
                    if line.strip() == md5_for_check:
                        return True
            return False

        def save_md5_hex(md5_for_check: str):
            """保存MD5到记录文件"""
            md5_file = get_abs_path(chroma_conf["md5_hex_store"])
            with open(md5_file, "a", encoding="utf-8") as f:
                f.write(md5_for_check + "\n")

        def get_file_documents(read_path: str):
            """根据文件类型选择加载器"""
            if read_path.endswith("txt"):
                return txt_loader(read_path)
            if read_path.endswith("pdf"):
                return pdf_loader(read_path)
            return []

        # 获取待处理文件列表
        data_path = get_abs_path(chroma_conf["data_path"])
        allowed_files_path = listdir_with_allowed_type(
            data_path,
            tuple(chroma_conf["allow_knowledge_file_type"]),
        )

        # 遍历处理每个文件
        for path in allowed_files_path:
            md5_hex = get_file_md5_hex(path)
            
            if check_md5_hex(md5_hex):
                logger.info(f"[加载知识库]{path}内容已存在，跳过")
                continue

            try:
                documents = get_file_documents(path)
                if not documents:
                    logger.warning(f"[加载知识库]{path}无有效内容，跳过")
                    continue

                # 文档分片
                split_document = self.spliter.split_documents(documents)
                if not split_document:
                    logger.warning(f"[加载知识库]{path}分片后无有效内容，跳过")
                    continue

                # 存入向量库
                self.vector_store.add_documents(split_document)
                save_md5_hex(md5_hex)
                logger.info(f"[加载知识库]{path}加载成功")
                
            except Exception as e:
                logger.error(f"[加载知识库]{path}加载失败：{e}", exc_info=True)
                continue
