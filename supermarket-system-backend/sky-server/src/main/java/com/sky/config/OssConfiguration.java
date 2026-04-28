package com.sky.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 配置类,用于创建AliOssUtil对象
 */
@Configuration
@Slf4j
public class OssConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "aliOssUtil")
    public AliOssUtil aliOssUtil(AliOssProperties aliossProperties) {
        log.info("开始创建阿里云文件上传工具类AliOssUtil的对象");
        return new AliOssUtil(aliossProperties.getEndpoint(), 
        aliossProperties.getAccessKeyId(), 
        aliossProperties.getAccessKeySecret(), 
        aliossProperties.getBucketName());
    }
}
