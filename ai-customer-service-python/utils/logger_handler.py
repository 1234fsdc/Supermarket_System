"""
日志处理模块

为什么创建这个文件：
- 配置日志输出格式和级别
- 支持同时输出到文件和控制台
- 按日期生成日志文件

怎么做的：
- 使用logging模块配置日志
- 创建FileHandler写入文件
- 创建StreamHandler输出到控制台
"""

import logging
import os
from datetime import datetime

# 创建logs目录
# 为什么：需要统一存放日志文件
# 怎么做的：使用os.makedirs创建目录，exist_ok=True避免重复创建报错
log_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "logs")
os.makedirs(log_dir, exist_ok=True)

# 日志文件名（按日期）
# 为什么：按日期分割日志文件，便于管理和查找
# 怎么做的：使用datetime生成当前日期作为文件名
log_file = os.path.join(log_dir, f"supermarket_agent_{datetime.now().strftime('%Y%m%d')}.log")

# 配置日志
# 为什么：统一配置日志格式和输出方式
# 怎么做的：使用logging.basicConfig进行全局配置
logging.basicConfig(
    level=logging.INFO,  # 日志级别：INFO及以上级别
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',  # 日志格式
    handlers=[
        logging.FileHandler(log_file, encoding='utf-8'),  # 文件处理器
        logging.StreamHandler()  # 控制台处理器
    ]
)

# 创建日志记录器
# 为什么：其他模块可以通过导入logger使用日志功能
# 怎么做的：使用logging.getLogger获取记录器实例
logger = logging.getLogger(__name__)
