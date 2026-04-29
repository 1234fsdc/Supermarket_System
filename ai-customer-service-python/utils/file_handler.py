"""
文件处理模块

提供PDF、TXT文件加载和MD5计算功能
"""
import os
import hashlib
from langchain_core.documents import Document
from langchain_community.document_loaders import PyPDFLoader, TextLoader


def get_file_md5_hex(file_path: str) -> str:
    """计算文件的MD5值"""
    with open(file_path, "rb") as f:
        return hashlib.md5(f.read()).hexdigest()


def listdir_with_allowed_type(directory: str, allowed_types: tuple) -> list:
    """获取目录下指定类型的文件列表"""
    files = []
    if not os.path.exists(directory):
        return files
    
    for filename in os.listdir(directory):
        if filename.lower().endswith(allowed_types):
            files.append(os.path.join(directory, filename))
    return files


def pdf_loader(file_path: str) -> list[Document]:
    """加载PDF文件"""
    try:
        loader = PyPDFLoader(file_path)
        return loader.load()
    except Exception as e:
        print(f"加载PDF失败 {file_path}: {e}")
        return []


def txt_loader(file_path: str) -> list[Document]:
    """加载TXT文件"""
    try:
        loader = TextLoader(file_path, encoding="utf-8")
        return loader.load()
    except Exception as e:
        print(f"加载TXT失败 {file_path}: {e}")
        return []
