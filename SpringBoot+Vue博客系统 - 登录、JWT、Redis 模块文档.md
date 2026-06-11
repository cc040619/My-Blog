

# SpringBoot+Vue博客系统 - 登录、JWT、Redis 模块文档
## 2 博客前台
### 2.7 登录功能实现
本项目前台与后台统一使用 **Spring Security** 实现安全防护、用户认证与权限校验。

#### 2.7.0 前置知识 - Spring Security
##### 1. 登录校验整体流程
1. 前端携带用户名、密码请求登录接口
2. 服务端校验数据库中的账号密码
3. 校验通过后，基于用户名/用户ID生成 JWT
4. 服务端将 JWT 响应给前端
5. 后续请求：前端在**请求头**携带 Token
6. 服务端拦截器解析 Token，获取用户ID
7. 根据用户ID查询用户信息，校验权限，放行/拦截资源
8. 服务端响应数据给前端

##### 2. 核心接口说明
| 核心接口              | 职责概述                                 |
| --------------------- | ---------------------------------------- |
| Authentication        | 实现类代表当前登录用户，封装用户相关信息 |
| AuthenticationManager | 定义用户认证的核心方法                   |
| UserDetailsService    | 根据用户名查询用户数据的核心接口         |
| UserDetails           | 封装用户核心信息，交由认证流程使用       |

---

#### 2.7.1 需求
1. 实现用户登录接口，完成账号密码校验。
2. 部分功能需登录后才可访问，未登录状态禁止使用。

#### 2.7.2 接口设计
- 请求方式：`POST`
- 请求路径：`/login`

**请求体**
```json
{
  "userName":"test",
  "password":"1234"
}
```

**响应格式**
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI0ODBmOThmYmJkNmI0NjM0OWUyZjY2NTM0NGNjZWY2NSIsInN1YiI6IjEiLCJpc3MiOiJzZyIsImlhdCI6MTY0Mzg3NDMxNiwiZXhwIjoxNjQzOTYwNzE2fQ.ldLBUvNIxQCGemkCoMgT_0YsjsWndTg5tqfJb77pabk",
    "userInfo": {
      "id": 1,
      "nickName": "test",
      "avatar": "http://i0.hdslb.com/bfs/article/3bf9c263bc0f2ac5c3a7feb9e218d07475573ec8.gif",
      "email": "test@ptu.edu.cn",
      "sex": "1"
    }
  },
  "msg": "操作成功"
}
```

---

#### 2.7.3 思路分析
##### 1. 用户登录认证链路
前端请求 → 自定义登录控制器 → `AuthenticationManager` 认证器 → `UserDetailsService` 查询用户 → 密码比对
1. 自定义登录接口：接收账号密码，封装为 `UsernamePasswordAuthenticationToken`，调用 `AuthenticationManager` 执行认证。
2. 认证成功：生成 JWT，将用户信息存入 Redis；认证失败则抛出异常。
3. 密码加密：容器注入 `BCryptPasswordEncoder`，框架自动完成密码密文比对。

##### 2. Token 校验链路
携带 Token 的请求 → JWT 过滤器 → 解析 Token 获取用户ID → 读取 Redis 用户信息 → 封装认证信息到 `SecurityContext`
1. 自定义 JWT 过滤器：拦截请求，提取请求头中的 Token，解析得到用户ID。
2. 根据用户ID查询 Redis，获取登录用户信息。
3. 封装为认证对象，存入 `SecurityContextHolder`，供后续权限校验使用。

---

#### 2.7.4 准备工作
##### 1. 引入依赖（framework 模块 pom.xml）
放开 Spring Security 相关注释，完整依赖如下：
```xml
<!-- SpringSecurity 启动器 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<!-- Redis 依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<!-- fastjson -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.33</version>
</dependency>
<!-- JWT 依赖 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.0</version>
</dependency>
```

##### 2. 资源准备
1. 将登录功能所需工具类、配置类解压复制到 `framework` 模块对应包下。
2. 执行 Maven `lifecycle-install` 完成依赖加载。

##### 3. Redis 环境
1. 本地安装并启动 Redis、Redis Insight。
2. 项目默认连接**本机 6379 端口**，无需额外配置。

---

#### 2.7.5 登录接口代码实现
整体步骤：
1. 自定义登录接口，调用认证器完成校验，成功后生成 JWT、存入 Redis
2. 自定义 `UserDetailsService` 实现数据库用户查询
3. 配置密码加密器、Security 安全规则

##### 1. SecurityConfig 配置类（blog 模块）
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 注入认证管理器，供登录接口使用
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 密码加密器
    @Bean
    public PasswordEncoder passwordEncoder(){
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
                // 其余所有请求直接放行
                .anyRequest().permitAll();

        // 关闭默认登出接口
        http.logout().disable();
        // 开启跨域支持
        http.cors();

        return http.build();
    }
}
```

##### 2. BlogLoginController 控制器（blog 模块）
```java
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlogLoginController {
    // 登录接口逻辑
}
```

##### 3. IBlogLoginService 接口（framework 模块）
```java
public interface IBlogLoginService {
    ResponseResult login(User user);
}
```

##### 4. BlogLoginServiceImpl 业务实现类（framework 模块）
> Redis Key 命名规则：`bloglogin:用户id`
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

@Service
public class BlogLoginServiceImpl implements IBlogLoginService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Override
    public ResponseResult login(User user) {
        // 1. 封装用户名密码为认证对象，执行认证
        // 2. 判断认证结果
        // 3. 获取用户ID，生成JWT Token
        // 4. 将用户信息存入Redis
        // 5. 实体转为 UserInfoVo，封装 Token 与用户信息并返回
        return null;
    }
}
```

##### 5. UserDetailsServiceImpl 用户查询实现类（framework 模块）
```java
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 根据用户名查询数据库用户
        // 2. 判断用户是否存在，不存在则抛出异常
        // 3. 封装用户信息并返回
        return null;
    }
}
```

##### 6. LoginUser 登录用户实体（实现 UserDetails，framework 模块）
```java
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser implements UserDetails {

    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    // 账号未过期
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 账号未锁定
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 凭证未过期
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 账号可用
    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

##### 7. BlogUserLoginVo 登录返回VO（framework 模块）
```java
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogUserLoginVo {
    private String token;
    private UserInfoVo userInfo;
}
```

##### 8. UserInfoVo 用户信息VO（framework 模块）
```java
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVo {
    private Long id;
    private String nickName;
    private String avatar;
    private String sex;
    private String email;
}
```

##### 9. 测试步骤
1. **Postman 测试**
   - 请求方式：`POST`
   - 请求地址：`/login`
   - 请求体：`{"userName":"test","password":"1234"}`
2. 前台页面登录功能联调测试。
3. 打开 Redis Insight，查看 Redis 中是否成功写入用户登录数据。

