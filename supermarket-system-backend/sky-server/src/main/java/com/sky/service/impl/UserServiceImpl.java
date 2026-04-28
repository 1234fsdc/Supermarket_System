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

@Service
@Slf4j
public class UserServiceImpl implements UserService {
        //微信服务接口地址
        private final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

        @Autowired
        private UserMapper userMapper;
        @Autowired
        private WeChatProperties weChatProperties;


        /**
         * 微信登录
         */
        @Override
        public User wxLogin(UserLoginDTO userLoginDTO) {
           String openid = getOpenid(userLoginDTO.getCode());
            //判断openid是否为空，如果为空表示登录失败，抛出业务异常
            if(openid == null){
                throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
            }
            
            //判断当前用户是否为新用户
            User user = userMapper.getByOpenid(openid);
            
            //如果是新用户，自动完成注册
            // 这段代码使用了 Builder 设计模式 ，
            // 主要作用是 创建并初始化 User 对象 ，具体功能如下：
            if(user == null){
        // 当微信登录的用户是首次使用系统时（数据库中不存在该 openid）
        // ，需要创建一个新的 User 对象。
                user = User.builder()
        // - .openid(openid) ：设置微信用户的唯一标识 openid
        // - .createTime(LocalDateTime.now()) ：设置用户创建时间为当前时间
        // - .build() ：最终构建并返回 User 对象
                        .openid(openid)
                        .createTime(LocalDateTime.now())
                        .build();
                userMapper.insert(user);
            }

            //返回这个用户对象
            return user;
        }
        /**
         * 获取微信用户的openid
         */
        private String getOpenid(String code){
             //调用微信接口服务，获得当前微信用户的openid
            Map<String, String> map = new HashMap<>();  
            map.put("appid", weChatProperties.getAppid());
            map.put("secret", weChatProperties.getSecret());
            map.put("js_code", code);
            map.put("grant_type", "authorization_code");
            String json = HttpClientUtil.doGet(WX_LOGIN,map);
            JSONObject jsonObject = JSON.parseObject(json);
            String openid = jsonObject.getString("openid");
            return openid;
        }
}
