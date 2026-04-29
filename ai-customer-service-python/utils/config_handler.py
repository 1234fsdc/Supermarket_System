"""
配置管理模块

为什么创建这个文件：
- 加载YAML配置文件
- 提供全局配置访问
- 统一管理不同模块的配置

怎么做的：
- 使用PyYAML解析YAML文件
- 定义加载函数，每个配置一个函数
- 在模块级别加载所有配置
"""

import yaml
from utils.path_tool import get_abs_path


def load_rag_config(config_path: str = get_abs_path("config/rag.yml"), encoding: str = "utf-8"):
    """
    加载RAG配置
    
    为什么：RAG模块需要独立的配置
    怎么做的：读取config/rag.yml文件
    
    Args:
        config_path: 配置文件路径
        encoding: 文件编码
        
    Returns:
        dict: RAG配置字典
    """
    with open(config_path, "r", encoding=encoding) as f:
        return yaml.load(f, Loader=yaml.FullLoader)


def load_chroma_config(config_path: str = get_abs_path("config/chroma.yml"), encoding: str = "utf-8"):
    """
    加载Chroma配置
    
    为什么：向量数据库需要独立的配置
    怎么做的：读取config/chroma.yml文件
    
    Args:
        config_path: 配置文件路径
        encoding: 文件编码
        
    Returns:
        dict: Chroma配置字典
    """
    with open(config_path, "r", encoding=encoding) as f:
        return yaml.load(f, Loader=yaml.FullLoader)


def load_prompts_config(config_path: str = get_abs_path("config/prompts.yml"), encoding: str = "utf-8"):
    """
    加载提示词配置
    
    为什么：提示词需要独立管理，便于调整
    怎么做的：读取config/prompts.yml文件
    
    Args:
        config_path: 配置文件路径
        encoding: 文件编码
        
    Returns:
        dict: 提示词配置字典
    """
    with open(config_path, "r", encoding=encoding) as f:
        return yaml.load(f, Loader=yaml.FullLoader)


def load_agent_config(config_path: str = get_abs_path("config/agent.yml"), encoding: str = "utf-8"):
    """
    加载Agent配置
    
    为什么：Agent模块需要独立的配置
    怎么做的：读取config/agent.yml文件
    
    Args:
        config_path: 配置文件路径
        encoding: 文件编码
        
    Returns:
        dict: Agent配置字典
    """
    with open(config_path, "r", encoding=encoding) as f:
        return yaml.load(f, Loader=yaml.FullLoader)


# 全局配置
# 为什么：其他模块可以直接导入使用
# 怎么做的：在模块导入时加载所有配置
rag_conf = load_rag_config()
chroma_conf = load_chroma_config()
prompts_conf = load_prompts_config()
agent_conf = load_agent_config()
