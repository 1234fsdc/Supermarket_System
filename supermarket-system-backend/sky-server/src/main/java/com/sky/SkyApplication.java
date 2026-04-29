package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 凡栋超市系统启动类
 *
 * 为什么创建这个类：
 * - 作为Spring Boot应用的入口
 * - 启用各种Spring功能（缓存、事务等）
 * - 配置组件扫描和自动配置
 *
 * 怎么做的：
 * - 使用@SpringBootApplication标记主类
 * - 使用@EnableCaching启用缓存
 * - 使用@EnableTransactionManagement启用事务管理
 */
@SpringBootApplication
// 开启缓存注解功能
// 为什么：使用Spring Cache进行方法级缓存，提高性能
// 怎么做的：添加@EnableCaching注解，在方法上使用@Cacheable等注解
@EnableCaching
// 开启注解方式的事务管理
// 为什么：使用声明式事务管理数据库操作
// 怎么做的：添加@EnableTransactionManagement注解，在方法上使用@Transactional
@EnableTransactionManagement
// Lombok提供的日志注解，自动生成log变量
@Slf4j
public class SkyApplication {

    /**
     * 应用程序入口方法
     *
     * 为什么：Java应用程序需要main方法作为入口
     * 怎么做的：调用SpringApplication.run启动Spring Boot应用
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 启动Spring Boot应用
        SpringApplication.run(SkyApplication.class, args);

        // 打印启动成功日志
        log.info("server started");
        log.info("---------------------");
        log.info("---------------------");
        log.info("---------------------");
        log.info("---------------------");
        log.info("---------------------");
        log.info("---------------------");
    }
}
