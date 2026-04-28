package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.sky.config.RedisConfiguration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RedisConfiguration {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建redis的模板对象。。。");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        //设置redis的连接工厂对象
        template.setConnectionFactory(redisConnectionFactory);
        //设置 redis key 的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        // //设置 redis value 的序列化器
        // template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
