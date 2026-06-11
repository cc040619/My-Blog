

# SpringBoot+Vue博客系统 - JWT校验、异常处理、登出文档
## 2 博客前台
### 2.7 登录功能实现
#### 2.7.6 登录校验过滤器代码实现
**实现思路**
1. 获取请求头中的 Token
2. 解析 Token 获取用户 ID
3. 根据用户 ID 从 Redis 查询用户信息
4. 将用户信息存入 `SecurityContextHolder`，完成认证

##### 1. JwtAuthenticationTokenFilter 过滤器（framework 模块）
```java
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import java.util.Objects;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private RedisCache redisCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 获取请求头中的token
        String token = null;
        if (!StringUtils.hasText(token)) {
            // 无token，接口无需登录，直接放行
            filterChain.doFilter(request, response);
            return;
        }

        // 解析token获取userid
        Claims claims = null;
        try {
            claims = null; // 补充JWT解析逻辑
        } catch (Exception e) {
            e.printStackTrace();
            // token超时/非法，响应前端重新登录
            ResponseResult result = ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
            WebUtils.renderString(response, JSON.toJSONString(result));
            return;
        }

        // 从redis中获取用户信息
        LoginUser loginUser = null;
        if (Objects.isNull(loginUser)) {
            // 登录已过期，提示重新登录
            ResponseResult result = ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
            WebUtils.renderString(response, JSON.toJSONString(result));
            return;
        }

        // 将用户信息存入SecurityContextHolder
        // 执行后续过滤器
        filterChain.doFilter(request, response);
    }
}
```

##### 2. SecurityConfig 配置类（blog 模块）
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 登录接口允许匿名访问
                .antMatchers("/login").anonymous()
                // 友链接口需授权访问（用于JWT过滤器测试）
                .antMatchers("/link/getAllLink").authenticated()
                // 其余请求直接放行
                .anyRequest().permitAll();

        http.cors();
        // 将JWT过滤器添加到Security过滤器链，置于账号密码认证过滤器之前
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

##### 3. 前端请求携带Token（ptu-blog-vue /api/link.js）
```javascript
export function getAllLink(query) {
  return request({
    url: '/link/getAllLink',
    method: 'get',
    params: query,
    headers: {},
    isToken: true // 开启请求携带Token
  })
}
```

##### 4. 测试步骤
1. 清空浏览器 Cookie 内 `user-Token`、本地存储 `userInfo`。
2. 访问友链页面，未登录状态无法正常展示页面元素。
3. 使用 Postman 测试 `/link/getAllLink` 接口，**请求头携带合法 Token** 才能正常获取数据。

---

### 2.8 认证授权失败处理
Security 默认异常响应格式不符合项目规范，自定义**认证失败处理器**与**授权失败处理器**，统一返回标准格式。

##### 1. 认证失败处理器 AuthenticationEntryPointImpl（framework 模块）
未登录、Token 异常等认证场景触发
```java
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        authException.printStackTrace();
        // 处理各类认证异常：未登录、凭证错误等
        ResponseResult result = ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        WebUtils.renderString(response, JSON.toJSONString(result));
    }
}
```

##### 2. 授权失败处理器 AccessDeniedHandlerImpl（framework 模块）
已登录但无访问权限时触发
```java
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AccessDeniedHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        accessDeniedException.printStackTrace();
        ResponseResult result = ResponseResult.errorResult(AppHttpCodeEnum.NO_OPERATOR_AUTH);
        WebUtils.renderString(response, JSON.toJSONString(result));
    }
}
```

##### 3. 在 SecurityConfig 中配置异常处理器（blog 模块）
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 配置自定义异常处理器
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler);

        // 其余原有配置省略
        return http.build();
    }
}
```

---

### 2.9 全局异常处理
通过**自定义业务异常 + 全局异常捕获**，统一处理系统异常，简化代码校验逻辑。

##### 1. 自定义系统异常 SystemException（framework 模块）
```java
public class SystemException extends RuntimeException {
    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public SystemException(AppHttpCodeEnum httpCodeEnum) {
        super(httpCodeEnum.getMsg());
        this.code = httpCodeEnum.getCode();
        this.msg = httpCodeEnum.getMsg();
    }
}
```

##### 2. 全局异常处理器 GlobalExceptionHandler（framework 模块）
```java
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 处理自定义业务异常
    @ExceptionHandler(SystemException.class)
    public ResponseResult systemExceptionHandler(SystemException e) {
        log.error("出现了异常! {}", e);
        return ResponseResult.errorResult(e.getCode(), e.getMsg());
    }

    // 处理全局未知异常
    @ExceptionHandler(Exception.class)
    public ResponseResult exceptionHandler(Exception e) {
        log.error("出现了异常! {}", e);
        return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(), e.getMessage());
    }
}
```

##### 3. 控制器中使用（BlogLoginController 示例）
主动抛出自定义异常，由全局处理器统一响应
```java
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlogLoginController {

    @PostMapping("/login")
    public ResponseResult login(@RequestBody User user) {
        // 非空校验，不满足则抛出异常
        if (!StringUtils.hasText(user.getUserName())) {
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);
        }
        return blogLoginService.login(user);
    }
}
```

---

### 2.10 退出登录接口
#### 2.10.1 接口设计
| 请求方式 | 请求地址  | 请求头     |
| -------- | --------- | ---------- |
| POST     | `/logout` | 携带 Token |

**响应格式**
```json
{
  "code": 200,
  "msg": "操作成功"
}
```

#### 2.10.2 代码实现
**核心逻辑**：解析 Token 获取用户ID，删除 Redis 中对应的登录用户信息。

##### 1. BlogLoginController（blog 模块）
```java
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

@RestController
public class BlogLoginController {

    @Resource
    private IBlogLoginService blogLoginService;

    @PostMapping("/logout")
    public ResponseResult logout() {
        return blogLoginService.logout();
    }
}
```

##### 2. IBlogLoginService 接口（framework 模块）
```java
public interface IBlogLoginService {
    ResponseResult logout();
}
```

##### 3. BlogLoginServiceImpl 实现类（framework 模块）
```java
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class BlogLoginServiceImpl implements IBlogLoginService {

    @Autowired
    private RedisCache redisCache;

    @Override
    public ResponseResult logout() {
        // 1. 获取请求头Token并解析得到userid
        // 2. 根据userid删除Redis中的登录信息
        return ResponseResult.okResult();
    }
}
```

##### 4. SecurityConfig 配置调整（blog 模块）
1. 关闭 Security 默认登出功能
2. 配置 `/logout` 接口需要登录授权才能访问
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/login").anonymous()
                // 登出接口必须登录后才能访问
                .antMatchers("/logout").authenticated()
                .anyRequest().permitAll();

        // 关闭Security默认注销接口
        http.logout().disable();

        return http.build();
    }
}
```

