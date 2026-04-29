"""
文件处理模块

为什么创建这个文件：
- 提供PDF、TXT文件加载功能
- 计算文件MD5值用于去重
- 统一文件操作接口

怎么做的：
- 使用LangChain的文档加载器
- 使用hashlib计算MD5
- 提供文件列表过滤功能
"""

import os
import hashlib
from langchain_core.documents import Document
from langchain_community.document_loaders import PyPDFLoader, TextLoader


def get_file_md5_hex(file_path: str) -> str:
    """
    计算文件的MD5值
    
    为什么：用于文件去重和完整性校验
    怎么做的：读取文件二进制内容，使用hashlib.md5计算
    
    Args:
        file_path: 文件路径
        
    Returns:
        str: MD5十六进制字符串
    """
    with open(file_path, "rb") as f:
        return hashlib.md5(f.read()).hexdigest()


def listdir_with_allowed_type(directory: str, allowed_types: tuple) -> list:
    """
    获取目录下指定类型的文件列表
    
    为什么：需要筛选特定类型的文件
    怎么做的：遍历目录，检查文件扩展名是否在允许列表中
    
    Args:
        directory: 目录路径
        allowed_types: 允许的文件类型元组（如('.txt', '.pdf')）
        
    Returns:
        list: 符合条件的文件路径列表
    """
    files = []
    if not os.path.exists(directory):
        return files
    
    for filename in os.listdir(directory):
        if filename.lower().endswith(allowed_types):
            files.append(os.path.join(directory, filename))
    return files


def pdf_loader(file_path: str) -> list[Document]:
    """
    加载PDF文件
    
    为什么：需要从PDF中提取文本内容
    怎么做的：使用LangChain的PyPDFLoader
    
    Args:
        file_path: PDF文件路径
        
    Returns:
        list[Document]: 文档对象列表
    """
    try:
        loader = PyPDFLoader(file_path)
        return loader.load()
    except Exception as e:
        print(f"加载PDF失败 {file_path}: {e}")
        return []


def txt_loader(file_path: str) -> list[Document]:
    """
    加载TXT文件
    
    为什么：需要从文本文件中读取内容
    怎么做的：使用LangChain的TextLoader
    
    Args:
        file_path: TXT文件路径
        
    Returns:
        list[Document]: 文档对象列表
    """
    try:
        loader = TextLoader(file_path, encoding="utf-8")
        return loader.load()
    except Exception as e:
        print(f"加载TXT失败 {file_path}: {e}")
        return []
