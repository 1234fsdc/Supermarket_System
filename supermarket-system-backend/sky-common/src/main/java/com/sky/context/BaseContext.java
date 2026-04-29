package com.sky.context;

/**
 * 基础上下文工具类
 *
 * 为什么创建这个类：
 * - 使用ThreadLocal存储当前登录用户的ID
 * - 实现线程隔离，保证多线程环境下数据安全
 * - 在请求处理过程中共享用户信息，避免重复传递
 *
 * 怎么做的：
 * - 使用ThreadLocal<Long>存储用户ID
 * - 提供set、get、remove方法
 * - 在拦截器中设置，在业务代码中获取
 */
public class BaseContext {

    /**
     * ThreadLocal变量，用于存储当前线程的用户ID
     *
     * 为什么使用ThreadLocal：
     * - 每个线程有自己的独立副本，互不干扰
     * - 避免线程安全问题
     * - 无需在方法参数中传递用户ID
     *
     * 怎么做的：
     * - 创建ThreadLocal<Long>实例
     * - 在线程开始时set值
     * - 在线程结束时remove值，防止内存泄漏
     */
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     *
     * 为什么：在拦截器中解析JWT后，将用户ID存入ThreadLocal
     * 怎么做的：调用ThreadLocal的set方法
     *
     * @param id 当前登录用户的ID
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取当前用户ID
     *
     * 为什么：业务代码需要获取当前登录用户的信息
     * 怎么做的：调用ThreadLocal的get方法
     *
     * @return Long 当前登录用户的ID
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    /**
     * 移除当前用户ID
     *
     * 为什么：防止内存泄漏，请求处理完成后需要清理
     * 怎么做的：调用ThreadLocal的remove方法
     */
    public static void removeCurrentId() {
        threadLocal.remove();
    }
}
