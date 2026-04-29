"""
凡栋超市AI智能客服系统 - Streamlit应用入口
"""
import time
import streamlit as st
from agent.react_agent import ReactAgent
from rag.vector_store import VectorStoreService

# 页面标题
st.title("🏪 凡栋超市智能客服")
st.divider()

# 初始化向量库（首次运行时加载文档）
if "vector_store_initialized" not in st.session_state:
    with st.spinner("正在加载知识库..."):
        vs = VectorStoreService()
        vs.load_document()
    st.session_state["vector_store_initialized"] = True

# Session State 初始化
if "agent" not in st.session_state:
    st.session_state["agent"] = ReactAgent()

if "messages" not in st.session_state:
    st.session_state["messages"] = []

# 渲染历史消息
for message in st.session_state["messages"]:
    with st.chat_message(message["role"]):
        st.write(message["content"])

# 用户输入
prompt = st.chat_input("请输入您的问题...")

if prompt:
    # 添加用户消息
    st.session_state["messages"].append({"role": "user", "content": prompt})
    st.rerun()

# AI回复处理
if st.session_state["messages"] and st.session_state["messages"][-1]["role"] == "user":
    user_message = st.session_state["messages"][-1]["content"]
    
    with st.chat_message("assistant"):
        with st.spinner("智能客服思考中..."):
            # 获取流式输出
            response_stream = st.session_state["agent"].execute_stream(user_message)
            
            # 显示流式回复
            response = st.write_stream(response_stream)
    
    # 保存AI回复到历史
    st.session_state["messages"].append({"role": "assistant", "content": response})
    st.rerun()
