package com.sky.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 *
 * 为什么创建这个类：
 * - 提供JWT令牌的生成和解析功能
 * - 用于用户身份认证和会话管理
 * - 实现无状态的身份验证机制
 *
 * 怎么做的：
 * - 使用jjwt库操作JWT
 * - 使用HS256算法签名
 * - 支持自定义claims和过期时间
 */
public class JwtUtil {

    /**
     * 生成JWT令牌
     *
     * 为什么：为用户创建身份认证令牌，用于后续请求的身份验证
     * 怎么做的：
     * 1. 使用HS256算法签名
     * 2. 设置自定义claims（如用户ID）
     * 3. 设置过期时间
     * 4. 生成紧凑格式的JWT字符串
     *
     * @param secretKey JWT密钥，用于签名和验证
     * @param ttlMillis 令牌过期时间（毫秒）
     * @param claims 自定义声明信息，如用户ID、角色等
     * @return String 生成的JWT令牌
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 指定签名算法为HS256（HMAC-SHA256）
        // 为什么：HS256是对称加密算法，服务端用同一密钥签名和验证
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        // 计算过期时间
        // 为什么：令牌需要有过期时间，防止长期有效带来的安全风险
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 构建JWT
        // 为什么：jjwt提供了流畅的API来构建JWT
        JwtBuilder builder = Jwts.builder()
                // 设置自定义claims
                // 注意：如果有私有声明，一定要先设置，否则会覆盖标准声明
                .setClaims(claims)
                // 设置签名算法和密钥
                // 密钥使用UTF-8编码的字节数组
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                // 设置过期时间
                .setExpiration(exp);

        // 生成紧凑格式的JWT字符串（header.payload.signature）
        return builder.compact();
    }

    /**
     * 解析JWT令牌
     *
     * 为什么：验证用户请求中的令牌，获取其中存储的用户信息
     * 怎么做的：
     * 1. 使用密钥解析令牌
     * 2. 验证签名是否正确
     * 3. 检查令牌是否过期
     * 4. 返回claims（包含用户信息）
     *
     * @param secretKey JWT密钥，必须与生成时使用的密钥相同
     * @param token 要解析的JWT令牌
     * @return Claims 解析后的声明信息
     * @throws Exception 令牌无效或过期时抛出异常
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 创建JWT解析器
        // 为什么：jjwt提供了DefaultJwtParser来解析JWT
        Claims claims = Jwts.parser()
                // 设置签名密钥，用于验证令牌的签名
                // 此秘钥一定要保留好在服务端, 不能暴露出去
                // 如果对接多个客户端建议改造成多个密钥
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                // 解析JWT令牌
                // parseClaimsJws方法会验证签名和过期时间
                .parseClaimsJws(token)
                // 获取body部分的claims
                .getBody();
        return claims;
    }
}
