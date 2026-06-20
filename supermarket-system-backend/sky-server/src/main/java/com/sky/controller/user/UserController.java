package com.sky.controller.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * C端用户控制器
 *
 * 为什么创建这个类：
 * - 处理微信小程序用户的登录请求
 * - 提供用户相关的RESTful API接口
 * - 生成JWT令牌用于身份认证
 *
 * 怎么做的：
 * - 使用@RestController标记为REST控制器
 * - 使用@RequestMapping定义基础路径
 * - 注入UserService处理业务逻辑
 * - 使用JWT生成认证令牌
 */
@RestController
@RequestMapping("/user/user")
@Slf4j
@Api(tags = "C端用户相关接口")
public class UserController {

    /**
     * 用户服务
     * 为什么：处理用户相关的业务逻辑
     * 怎么做的：使用@Autowired自动注入Spring管理的Bean
     */
    @Autowired
    private UserService userService;

    /**
     * JWT配置属性
     * 为什么：读取JWT密钥和过期时间等配置
     * 怎么做的：使用@Autowired注入配置类
     */
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 微信登录接口
     *
     * 为什么：微信小程序用户需要通过微信授权登录
     * 怎么做的：
     * - 接收前端传来的登录凭证（code）
     * - 调用微信接口获取openid
     * - 查询或创建用户
     * - 生成JWT令牌返回给前端
     *
     * @param userLoginDTO 登录请求DTO，包含微信登录code
     * @return Result<UserLoginVO> 登录结果，包含用户信息和JWT令牌
     */
    @PostMapping("/login")
    @ApiOperation("微信登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        // 记录登录日志
        log.info("微信登录，用户信息：{}", userLoginDTO);

        // 微信登录
        // 为什么：获取微信用户的openid，用于标识用户
        // 怎么做的：调用UserService的wxLogin方法
        User user = userService.wxLogin(userLoginDTO);

        // 为微信用户生成JWT令牌
        // 为什么：后续请求需要携带令牌进行身份认证
        // 怎么做的：
        // 1. 创建claims存储用户ID
        // 2. 调用JwtUtil生成令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );

        // 封装返回结果
        // 为什么：前端需要用户ID、openid和令牌
        // 怎么做的：使用Builder模式创建VO对象
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }
}
