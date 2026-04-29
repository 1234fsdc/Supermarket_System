package com.sky.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户服务实现类
 *
 * 为什么创建这个类：
 * - 实现用户相关的业务逻辑
 * - 处理微信小程序登录流程
 * - 管理用户数据的增删改查
 *
 * 怎么做的：
 * - 实现UserService接口
 * - 调用微信API获取openid
 * - 自动为新用户完成注册
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    /**
     * 微信登录接口地址
     * 为什么：微信小程序登录需要调用微信官方接口
     * 怎么做的：使用微信提供的jscode2session接口
     */
    private final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    /**
     * 用户Mapper
     * 为什么：操作用户数据表
     * 怎么做的：使用@Autowired自动注入
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * 微信配置属性
     * 为什么：存储appid、secret等微信配置
     * 怎么做的：从application.yml读取配置
     */
    @Autowired
    private WeChatProperties weChatProperties;

    /**
     * 微信登录
     *
     * 为什么：微信小程序用户需要通过微信授权登录系统
     * 怎么做的：
     * 1. 调用微信接口获取openid
     * 2. 根据openid查询用户是否存在
     * 3. 新用户自动注册
     * 4. 返回用户信息
     *
     * @param userLoginDTO 登录请求DTO，包含微信登录code
     * @return User 用户信息
     * @throws LoginFailedException 登录失败时抛出
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        // 调用微信接口获取openid
        String openid = getOpenid(userLoginDTO.getCode());

        // 判断openid是否为空，如果为空表示登录失败，抛出业务异常
        // 为什么：openid是微信用户的唯一标识，获取失败说明登录有问题
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 判断当前用户是否为新用户
        // 为什么：根据openid查询数据库，判断用户是否已存在
        User user = userMapper.getByOpenid(openid);

        // 如果是新用户，自动完成注册
        // 为什么：提升用户体验，首次登录自动创建账号
        // 怎么做的：使用Builder模式创建User对象
        if (user == null) {
            // 当微信登录的用户是首次使用系统时（数据库中不存在该 openid）
            // 需要创建一个新的 User 对象
            user = User.builder()
                    // 设置微信用户的唯一标识 openid
                    .openid(openid)
                    // 设置用户创建时间为当前时间
                    .createTime(LocalDateTime.now())
                    // 最终构建并返回 User 对象
                    .build();
            // 将新用户插入数据库
            userMapper.insert(user);
        }

        // 返回用户对象
        return user;
    }

    /**
     * 获取微信用户的openid
     *
     * 为什么：openid是微信用户的唯一标识，用于识别用户身份
     * 怎么做的：
     * 1. 构建请求参数（appid、secret、js_code）
     * 2. 调用微信jscode2session接口
     * 3. 解析返回的JSON获取openid
     *
     * @param code 微信登录凭证，由前端调用wx.login获取
     * @return String 微信用户的openid
     */
    private String getOpenid(String code) {
        // 调用微信接口服务，获得当前微信用户的openid
        Map<String, String> map = new HashMap<>();
        // 小程序appid，从配置读取
        map.put("appid", weChatProperties.getAppid());
        // 小程序secret，从配置读取
        map.put("secret", weChatProperties.getSecret());
        // 登录时获取的code
        map.put("js_code", code);
        // 授权类型，固定为authorization_code
        map.put("grant_type", "authorization_code");

        // 发送HTTP GET请求到微信接口
        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        // 解析JSON响应
        JSONObject jsonObject = JSON.parseObject(json);
        // 获取openid字段
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
