package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT令牌校验拦截器（用户端）
 *
 * 为什么创建这个类：
 * - 拦截用户端的请求，验证JWT令牌的有效性
 * - 实现无状态的身份认证机制
 * - 将用户信息存入ThreadLocal，供后续使用
 *
 * 怎么做的：
 * - 实现HandlerInterceptor接口
 * - 在preHandle方法中校验JWT
 * - 校验通过则将用户ID存入BaseContext
 * - 校验失败返回401状态码
 */
@Component  // 标记为Spring组件，由Spring管理
@Slf4j      // Lombok日志注解
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    /**
     * JWT配置属性
     * 为什么：读取JWT密钥、令牌名称等配置
     * 怎么做的：使用@Autowired注入配置类
     */
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 预处理：在Controller方法执行前进行JWT校验
     *
     * 为什么：确保只有携带有效令牌的用户才能访问受保护的接口
     * 怎么做的：
     * 1. 判断拦截的是否是Controller方法
     * 2. 从请求头中获取JWT令牌
     * 3. 使用JwtUtil解析并验证令牌
     * 4. 验证通过则放行，否则返回401
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 被拦截的处理器
     * @return boolean true表示放行，false表示拦截
     * @throws Exception 处理异常
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 打印当前线程ID，用于调试
        System.out.println("当前线程的id:"+ Thread.currentThread().getId());

        // 判断当前拦截到的是Controller的方法还是其他资源（如静态资源）
        // 为什么：静态资源不需要JWT校验
        if (!(handler instanceof HandlerMethod)) {
            // 当前拦截到的不是动态方法，直接放行
            return true;
        }

        // 1、从请求头中获取令牌
        // 为什么：JWT令牌通常放在请求头的Authorization字段中
        String token = request.getHeader(jwtProperties.getUserTokenName());

        // 2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            // 使用JwtUtil解析JWT令牌
            // 如果令牌无效或过期，会抛出异常
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);

            // 从claims中获取用户ID
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户id：{}", userId);

            // 将用户ID存入BaseContext（ThreadLocal）
            // 为什么：后续业务逻辑可能需要获取当前用户ID
            // 怎么做的：使用ThreadLocal存储，保证线程安全
            BaseContext.setCurrentId(userId);

            // 3、校验通过，放行
            return true;
        } catch (Exception ex) {
            // 4、校验不通过，响应401状态码
            // 为什么：令牌无效或过期，用户未授权
            response.setStatus(401);
            return false;
        }
    }
}
