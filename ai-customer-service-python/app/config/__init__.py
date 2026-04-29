import os
import yaml
from typing import Dict, Any

# 配置文件路径
CONFIG_DIR = os.path.dirname(os.path.abspath(__file__))


def load_yaml_config(filename: str) -> Dict[str, Any]:
    """加载YAML配置文件"""
    filepath = os.path.join(CONFIG_DIR, filename)
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            return yaml.safe_load(f)
    except FileNotFoundError:
        return {}
    except Exception as e:
        print(f"加载配置文件 {filename} 失败: {e}")
        return {}


# 加载配置
rag_config = load_yaml_config('rag.yml')
prompts_config = load_yaml_config('prompts.yml')

# 获取嵌套配置值的辅助函数
def get_config(config: Dict, *keys, default=None):
    """获取嵌套配置值"""
    try:
        value = config
        for key in keys:
            value = value[key]
        return value
    except (KeyError, TypeError):
        return default


# RAG配置
RAG_CONFIG = rag_config.get('rag', {})
EMBEDDING_CONFIG = rag_config.get('embedding', {})
LLM_CONFIG = rag_config.get('llm', {})
CHROMA_CONFIG = rag_config.get('chroma', {})
REDIS_CONFIG = rag_config.get('redis', {})

# 提示词配置
PROMPTS = prompts_config.get('prompts', {})

# API密钥（从环境变量读取）
DASHSCOPE_API_KEY = os.getenv('DASHSCOPE_API_KEY', '')
