"""
路径工具模块

提供获取绝对路径的辅助函数，确保在不同工作目录下都能正确找到文件
"""
import os


def get_abs_path(relative_path: str) -> str:
    """
    获取相对于项目根目录的绝对路径
    
    Args:
        relative_path: 相对路径（如"config/rag.yml"）
        
    Returns:
        绝对路径
    """
    # 获取当前文件所在目录（utils/）
    current_dir = os.path.dirname(os.path.abspath(__file__))
    # 回到项目根目录
    project_root = os.path.dirname(current_dir)
    # 拼接路径
    return os.path.join(project_root, relative_path)
