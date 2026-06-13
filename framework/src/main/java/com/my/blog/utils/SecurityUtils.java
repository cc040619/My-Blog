package com.my.blog.utils;

import com.my.blog.domain.entity.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

/**
 * SecurityUtils 工具类
 * 从 SecurityContext 中获取当前登录用户信息
 */
public class SecurityUtils {

    private SecurityUtils() {}

    /**
     * 获取 Authentication 认证对象
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户
     */
    public static LoginUser getLoginUser() {
        Authentication authentication = getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户 ID
     */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return null;
        }
        return loginUser.getUser().getId();
    }

    /**
     * 判断当前登录用户是否为管理员
     */
    public static boolean isAdmin() {
        Long userId = getUserId();
        return Objects.equals(userId, 1L);
    }
}
