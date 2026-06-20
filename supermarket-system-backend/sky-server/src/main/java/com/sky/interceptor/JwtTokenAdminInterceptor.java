package com.sky.interceptor;

import com.sky.annotation.RequireRole;
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
import java.util.Arrays;

@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("当前线程的id:"+ Thread.currentThread().getId());

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            String role = claims.get(JwtClaimsConstant.EMP_ROLE).toString();
            log.info("当前员工id：{}，角色：{}", empId, role);
            BaseContext.setCurrentId(empId);
            BaseContext.setCurrentRole(role);

            //3、检查 @RequireRole 注解
            RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
            if (requireRole == null) {
                requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
            }
            if (requireRole != null) {
                String[] allowedRoles = requireRole.value();
                boolean hasPermission = Arrays.asList(allowedRoles).contains(role);
                if (!hasPermission) {
                    log.warn("权限不足：员工{}角色{}无法访问{}", empId, role, request.getRequestURI());
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":0,\"msg\":\"权限不足\"}");
                    return false;
                }
            }

            return true;
        } catch (Exception ex) {
            response.setStatus(401);
            return false;
        }
    }
}
