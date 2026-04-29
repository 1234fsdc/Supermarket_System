"""
提示词加载模块

从文件加载系统提示词和RAG提示词
"""
from utils.path_tool import get_abs_path
from utils.config_handler import prompts_conf


def load_system_prompts() -> str:
    """加载系统提示词"""
    prompt_path = get_abs_path(prompts_conf["main_prompt_path"])
    try:
        with open(prompt_path, "r", encoding="utf-8") as f:
            return f.read()
    except FileNotFoundError:
        # 如果文件不存在，返回默认提示词
        return """你是凡栋超市的AI智能客服助手。"""


def load_rag_prompts() -> str:
    """加载RAG提示词模板"""
    prompt_path = get_abs_path(prompts_conf["rag_summarize_prompt_path"])
    try:
        with open(prompt_path, "r", encoding="utf-8") as f:
            return f.read()
    except FileNotFoundError:
        # 如果文件不存在，返回默认模板
        return """基于以下参考资料回答：\n\n{context}\n\n问题：{input}\n\n回答："""
