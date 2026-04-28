package com.sky.interceptor;

import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户上下文拦截器
 * 负责设置和清理用户上下文
 */
@Component
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    /**
     * 在请求处理之前设置用户上下文
     */
    // @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("用户上下文拦截器开始处理请求: {}", request.getRequestURI());
        
        // 这里可以添加额外的用户上下文处理逻辑
        // 比如从session、cookie或其他地方获取用户信息
        
        // 注意：用户ID已经在JwtTokenUserInterceptor中设置到BaseContext
        // 这个拦截器主要用于后续的用户上下文处理
        
        return true;
    }

    /**
     * 请求完成后清理用户上下文，防止内存泄漏
     */

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("用户上下文拦截器清理线程上下文");
        BaseContext.removeCurrentId();
    }
}