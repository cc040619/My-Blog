package com.my.blog.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    // 有效期 24 小时
    private static final long JWT_TTL = 24 * 60 * 60 * 1000;
    // 密钥（Base64编码）
    private static final String JWT_KEY = "c2FuZ2VuZw==";
    // 签发者
    private static final String ISSUER = "sg";

    /**
     * 创建 JWT
     */
    public String createJWT(String subject) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        JwtBuilder builder = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(subject)
                .setIssuer(ISSUER)
                .setIssuedAt(now)
                .signWith(signatureAlgorithm, JWT_KEY);
        if (JWT_TTL >= 0) {
            long expMillis = nowMillis + JWT_TTL;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    /**
     * 解析 JWT
     */
    public Claims parseJWT(String jwt) throws Exception {
        return Jwts.parser()
                .setSigningKey(JWT_KEY)
                .parseClaimsJws(jwt)
                .getBody();
    }
}
