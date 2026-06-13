package com.my.blog.config;

import com.my.blog.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    // 注入认证管理器，供登录接口使用
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 密码加密器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关闭csrf防护
                .csrf().disable()
                // 不使用Session存储安全上下文
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 登录接口允许匿名访问
                .antMatchers("/login").anonymous()
                // 注册接口允许匿名访问
                .antMatchers("/user/register").anonymous()
                // 发表评论接口需要认证才能访问
                .antMatchers("/comment").authenticated()
                // 友链接口需授权访问（用于JWT过滤器测试）
                .antMatchers("/link/getAllLink").authenticated()
                // 登出接口必须登录后才能访问
                .antMatchers("/logout").authenticated()
                // 个人信息接口需要认证才能访问
                .antMatchers("/user/userInfo").authenticated()
                // 头像上传接口需要认证才能访问
                .antMatchers("/upload").authenticated()
                // 其余所有请求直接放行
                .anyRequest().permitAll();

        // 配置自定义异常处理器
        http.exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler);

        // 关闭Security默认注销接口
        http.logout().disable();
        // 开启跨域支持
        http.cors();

        // 将JWT过滤器添加到UsernamePasswordAuthenticationFilter之前
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
