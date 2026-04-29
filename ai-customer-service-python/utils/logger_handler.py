"""
日志处理模块

配置日志输出格式和级别
"""
import logging
import os
from datetime import datetime

# 创建logs目录
log_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "logs")
os.makedirs(log_dir, exist_ok=True)

# 日志文件名（按日期）
log_file = os.path.join(log_dir, f"supermarket_agent_{datetime.now().strftime('%Y%m%d')}.log")

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(log_file, encoding='utf-8'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)
