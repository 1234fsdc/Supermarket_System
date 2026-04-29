"""
提示词加载模块

为什么创建这个文件：
- 从文件加载系统提示词和RAG提示词
- 支持提示词的独立管理和热更新
- 提供默认提示词作为fallback

怎么做的：
- 从配置文件读取提示词文件路径
- 读取文件内容并返回
- 文件不存在时返回默认提示词
"""

from utils.path_tool import get_abs_path
from utils.config_handler import prompts_conf


def load_system_prompts() -> str:
    """
    加载系统提示词
    
    为什么：系统提示词定义了AI的角色和行为
    怎么做的：从配置文件指定的路径读取，失败则返回默认提示词
    
    Returns:
        str: 系统提示词文本
    """
    prompt_path = get_abs_path(prompts_conf["main_prompt_path"])
    try:
        with open(prompt_path, "r", encoding="utf-8") as f:
            return f.read()
    except FileNotFoundError:
        # 如果文件不存在，返回默认提示词
        # 为什么：确保即使配置文件缺失，服务也能正常运行
        # 怎么做的：返回硬编码的默认提示词
        return """你是凡栋超市的AI智能客服助手。"""


def load_rag_prompts() -> str:
    """
    加载RAG提示词模板
    
    为什么：RAG提示词模板定义了如何组织检索结果和问题
    怎么做的：从配置文件指定的路径读取，失败则返回默认模板
    
    Returns:
        str: RAG提示词模板文本
    """
    prompt_path = get_abs_path(prompts_conf["rag_summarize_prompt_path"])
    try:
        with open(prompt_path, "r", encoding="utf-8") as f:
            return f.read()
    except FileNotFoundError:
        # 如果文件不存在，返回默认模板
        # 为什么：确保即使配置文件缺失，服务也能正常运行
        # 怎么做的：返回硬编码的默认模板
        return """基于以下参考资料回答：\n\n{context}\n\n问题：{input}\n\n回答："""
