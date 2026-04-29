package com.sky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.interceptor.UserInterceptor;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Web MVC配置类
 *
 * 为什么创建这个类：
 * - 注册自定义拦截器（JWT校验）
 * - 配置Swagger/Knife4j接口文档
 * - 配置静态资源映射
 * - 扩展Spring MVC消息转换器
 *
 * 怎么做的：
 * - 继承WebMvcConfigurationSupport
 * - 重写addInterceptors注册拦截器
 * - 使用@Bean创建Docket实例生成文档
 * - 重写addResourceHandlers配置静态资源
 */
@Configuration  // 标记为配置类
@Slf4j          // Lombok日志注解
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    /**
     * 管理员JWT拦截器
     * 为什么：校验管理端请求的JWT令牌
     */
    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 用户JWT拦截器
     * 为什么：校验用户端请求的JWT令牌
     */
    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 用户上下文拦截器
     * 为什么：清理ThreadLocal中的用户ID，防止内存泄漏
     */
    @Autowired
    private UserInterceptor userInterceptor;

    /**
     * 注册自定义拦截器
     *
     * 为什么：对请求进行拦截处理，实现权限控制
     * 怎么做的：
     * 1. 注册管理员JWT拦截器，拦截/admin/**路径
     * 2. 注册用户JWT拦截器，拦截/user/**路径
     * 3. 注册用户上下文拦截器，清理ThreadLocal
     *
     * @param registry 拦截器注册器
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        // 管理员JWT拦截器
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")           // 拦截所有/admin路径
                .excludePathPatterns("/admin/employee/login");  // 排除登录接口

        // 用户JWT拦截器
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")            // 拦截所有/user路径
                .excludePathPatterns("/user/user/login")        // 排除登录接口
                .excludePathPatterns("/user/shop/status")       // 排除店铺状态接口
                .excludePathPatterns("/user/ai-customer/**");   // 排除AI客服接口

        // 用户上下文拦截器
        // 为什么：在所有请求之后执行，清理ThreadLocal
        registry.addInterceptor(userInterceptor)
                .addPathPatterns("/**");
    }

    /**
     * 创建管理端接口文档
     *
     * 为什么：使用Swagger/Knife4j生成API文档，方便前后端对接
     * 怎么做的：
     * - 使用Docket配置文档信息
     * - 指定扫描的包路径
     * - 设置文档标题、版本等信息
     *
     * @return Docket 文档配置对象
     */
    @Bean
    public Docket docket1() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("凡栋超市项目接口文档")
                .version("2.0")
                .description("凡栋超市项目接口文档")
                .build();
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("管理端接口")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.admin"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 创建用户端接口文档
     *
     * 为什么：为用户端单独生成接口文档
     * 怎么做的：与docket1类似，但扫描不同的包
     *
     * @return Docket 文档配置对象
     */
    @Bean
    public Docket docket2() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("超市外卖项目接口文档")
                .version("2.0")
                .description("超市外卖项目接口文档")
                .build();
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("用户端接口")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.user"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 设置静态资源映射
     *
     * 为什么：
     * - Swagger/Knife4j需要访问静态资源
     * - 前端打包后的资源需要映射
     * 怎么做的：
     * - 配置/doc.html入口页
     * - 配置/webjars资源
     * - 配置根路径静态资源
     *
     * @param registry 资源处理器注册器
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始配置静态资源映射...");

        // 1. 映射 Knife4j 文档入口页
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        // 2. 映射 webjars 资源（如 swagger-ui 的 js/css）
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        // 3. 映射根路径的静态资源
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/META-INF/resources/")
                .addResourceLocations("classpath:/static/")
                .addResourceLocations("classpath:/public/");
    }

    /**
     * 扩展Spring MVC消息转换器
     *
     * 为什么：
     * - 自定义JSON序列化/反序列化规则
     * - 处理LocalDateTime类型的格式化
     * - 忽略未知字段，避免400错误
     * 怎么做的：
     * - 创建自定义ObjectMapper
     * - 配置JavaTimeModule处理时间类型
     * - 将自定义转换器添加到列表首位
     *
     * @param converters 消息转换器列表
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器");

        // 创建Jackson消息转换器
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();

        // 忽略未知字段，避免400错误
        objectMapper.configure(
            com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
        );

        // 配置Java时间模块，处理LocalDateTime的序列化
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 添加序列化器和反序列化器
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        objectMapper.registerModule(javaTimeModule);

        // 设置ObjectMapper到转换器
        converter.setObjectMapper(objectMapper);

        // 将自定义转换器添加到列表首位（优先级最高）
        converters.add(0, converter);
    }
}
