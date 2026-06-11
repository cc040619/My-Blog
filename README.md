# MyBlog

SpringBoot + Vue 个人博客系统，支持文章浏览、分类筛选、评论互动、友情链接等功能。

## 技术栈

### 后端

| 技术 | 说明 |
|------|------|
| Java 8 | 运行环境 |
| Spring Boot 2.7.10 | 应用框架 |
| Spring Security | 安全认证与授权 |
| MyBatis-Plus 3.5.3 | ORM 框架 |
| MySQL | 关系型数据库 |
| Redis | 缓存 & Token 存储 |
| JWT (jjwt 0.9.0) | 无状态身份认证 |
| 阿里云 OSS | 文件上传存储 |
| Swagger (SpringFox) | API 文档 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 2.5 | 前端框架 |
| Vue Router | 路由管理 |
| Vuex | 状态管理 |
| Element UI | UI 组件库 |
| Axios | HTTP 请求 |
| Mavon Editor | Markdown 编辑器 |

## 项目结构

```
MyBlog
├── blog/                # 博客前台模块（Spring Boot 入口 + Controller）
├── framework/           # 公共模块（Entity、Mapper、Service、Utils）
├── admin/               # 后台管理模块（规划中）
├── ptu-blog-vue/        # Vue 前端项目
├── pom.xml              # Maven 父 POM
└── docs/                # 开发文档
```

## 项目接口

### 文章接口 `/article`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/article/hotArticleList` | 获取浏览量 Top10 热门文章 | 否 |
| GET | `/article/articleList` | 分页获取文章列表（支持分类筛选） | 否 |
| GET | `/article/{id}` | 获取文章详情 | 否 |

### 分类接口 `/category`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/category/getCategoryList` | 获取所有分类列表 | 否 |

### 评论接口 `/comment`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/comment/commentList` | 获取文章评论（分页） | 否 |
| GET | `/comment/linkCommentList` | 获取友链评论（分页） | 否 |
| POST | `/comment` | 发表评论 | 是 |

### 友链接口 `/link`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/link/getAllLink` | 获取全部友情链接 | 否 |

### 登录 & 用户

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/login` | 用户登录，返回 JWT Token | 否 |
| POST | `/logout` | 退出登录 | 是 |
| GET | `/user/userInfo` | 获取当前用户信息 | 是 |
| PUT | `/user/userInfo` | 更新用户信息 | 是 |

### 文件上传

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/upload` | 上传文件到阿里云 OSS | 否 |

> 认证方式：登录后获取 JWT Token，后续请求在 Header 中携带 `token`。

## 项目启动

### 环境要求

- JDK 8+
- Maven 3.x
- MySQL 5.7+
- Redis
- Node.js（前端）

### 数据库初始化

1. 创建数据库 `my_blog`
2. 执行 SQL 初始化脚本建表（待补充）

### 后端启动

1. 确保 MySQL 和 Redis 已启动
2. 在 `blog/src/main/resources/` 下创建 `application-dev.yml`，填入你的数据库密码和 OSS 凭证：

```yaml
spring:
  datasource:
    password: 你的数据库密码

aliyun:
  oss:
    endpoint: https://oss-cn-xxx.aliyuncs.com
    bucketName: 你的Bucket
    region: cn-xxx
    accessKeyId: 你的AccessKey
    accessKeySecret: 你的Secret
```

3. 启动后端：

```bash
mvn install
cd blog
mvn spring-boot:run
```

服务运行在 `http://localhost:7777`

### 前端启动

```bash
cd ptu-blog-vue
npm install
npm run dev
```

前端运行在 `http://localhost:8080`，API 请求自动代理到后端 `7777` 端口。

## 待完成

- [ ] 后台管理模块（admin）
- [ ] 用户注册接口
- [ ] 文章浏览量自动统计
- [ ] 数据库初始化 SQL 脚本

---

持续更新中...
