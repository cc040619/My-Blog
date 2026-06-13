

# OSS上传头像、个人信息、注册接口文档
## 2.15 头像上传接口
### 2.15.1 需求
在个人中心点击编辑时可上传头像图片，上传完成后，可结合更新个人信息接口使用。

### 2.15.2 OSS
#### ① 为什么要使用OSS
若将图片、视频等文件直接上传至Web应用服务器，读取文件会占用大量服务器资源，影响服务性能。因此通常使用**OSS（Object Storage Service，对象存储服务）** 存储图片、视频等静态资源。

**交互流程**：
1. 用户上传图片/视频
2. Web应用服务器将文件上传到OSS
3. OSS返回文件存储路径
4. 服务器拼接访问URL返回给用户
5. 用户直接访问OSS获取文件

#### ② 七牛云 OSS 注册使用
1. **注册认证**
    进入七牛云控制台，完成注册、绑定邮箱、实名认证；认证后可领取优惠券，同时获得**每月10GB存储&CDN免费额度**，额度使用后自动抵扣。
2. **创建存储空间**
    控制台 → 对象存储 → 空间管理 → 新建空间；选择存储区域、设置访问控制（公开/私有），创建后**记录Bucket名称**。
3. **生成密钥**
    个人中心 → 密钥管理，获取并记录 **AccessKey（AK）** 和 **SecretKey（SK）**。

#### ③ 示例代码测试
项目已引入七牛云Maven依赖，可直接修改官方示例代码，在单元测试中测试文件上传功能。
```java
public class QiniuTest {
    @Test 
    void uploadTest() { 
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = Configuration.create(Region.huanan());
        // 指定分片上传版本
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
        UploadManager uploadManager = new UploadManager(cfg);
        
        // 七牛云密钥、存储空间
        String accessKey = "Klf0OGFpthQoslC_zIg3wxaVkeQjc-WHhKb4c4uo"; 
        String secretKey = "YAIZEGvgdWiuB8bQTilv0ck518vmpgLMEPP-ww7g";
        String bucket = "ptu-blog-test";
        String key = null;
        
        File file = new File("E:\\图片\\photo.png"); // 本地文件路径
        try {
            FileInputStream byteInputStream = new FileInputStream(file);
            key = file.getName();
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);
            
            Response response = uploadManager.put(byteInputStream, key, upToken, null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            ex.printStackTrace();
            if (ex.response != null) {
                try {
                    String body = ex.response.toString();
                    System.err.println(body);
                } catch (Exception ignored) {}
            }
        } catch (FileNotFoundException ex) {
            //ignore
        }
    }
}
```

### 2.15.3 接口设计
| 请求方式 | 请求地址 | 请求头                                       |
| -------- | -------- | -------------------------------------------- |
| POST     | /upload  | Content-Type: multipart/form-data; 无需Token |

**请求参数**
- `img`：待上传的文件

**响应格式**
```json
{
    "code": 200,
    "data": "OSS文件访问链接",
    "msg": "操作成功"
}
```

### 2.15.4 配置
#### ① 依赖引入
```xml
<!-- 七牛云OSS -->
<dependency>
    <groupId>com.qiniu</groupId>
    <artifactId>qiniu-java-sdk</artifactId>
    <version>[7.19.0, 7.19.99]</version>
</dependency>
<!-- Gson 解析返回结果 -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
</dependency>
```

#### ② 配置文件（application.yml）
```yaml
oss:
  accessKey: xxxx
  secretKey: xxxx
  bucket: ptu-blog-xxx
  domain: http://xxx.clouddn.com/
```

### 2.15.5 代码实现
#### 1. UploadController（blog模块）
```java
@RestController
public class UploadController {
    @Autowired
    private UploadService uploadService;

    @PostMapping("/upload")
    public ResponseResult uploadImg(MultipartFile img){
        return uploadService.uploadImg(img);
    }
}
```

#### 2. UploadService（framework模块，接口）
```java
public interface UploadService {
    ResponseResult uploadImg(MultipartFile img);
}
```

#### 3. OSSUploadServiceImpl（framework模块，实现类）
```java
@Service
@Data
@ConfigurationProperties(prefix = "oss")
public class UploadServiceImpl implements UploadService {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String domain;

    // 允许上传的文件后缀（统一小写）
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".png", ".jpg", ".jpeg");

    @Override
    public ResponseResult uploadImg(MultipartFile img) {
        // 1. 文件合法性校验
        // 2. 生成云上文件路径
        String filePath = PathUtils.generateFilePath(img.getOriginalFilename());
        // 3. 上传至OSS并获取访问链接
        String url = uploadOSS(img, filePath);
        return ResponseResult.okResult(url);
    }

    // 文件上传至七牛云OSS核心方法
    private String uploadOSS(MultipartFile imgFile, String filePath) {
        // 参考官方示例代码，基于数据流实现上传
    }
}
```

#### 4. 枚举类 AppHttpCodeEnum（framework模块，异常码）
```java
public enum AppHttpCodeEnum {
    FILE_TYPE_ERROR(507, "文件类型错误,请上传png|jpg文件"),
    FILE_SIZE_ERROR(508, "文件大小超出限制");
}
```

#### 5. 工具类 PathUtils（framework模块，生成文件路径）
```java
public class PathUtils {
    public static String generateFilePath(String fileName) {
        // 按日期生成目录路径
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
        String datePath = sdf.format(new Date());
        // 使用UUID作为文件名，去除横杠
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        // 截取文件后缀
        int index = fileName.lastIndexOf(".");
        String fileType = fileName.substring(index);
        // 拼接完整路径
        return datePath + uuid + fileType;
    }
}
```

---

## 2.16 更新个人信息接口
### 2.16.1 需求
编辑个人资料后点击保存，完成个人信息数据更新。

### 2.16.2 接口设计
| 请求方式 | 请求地址       | 请求头          |
| -------- | -------------- | --------------- |
| PUT      | /user/userInfo | 携带Token请求头 |

**请求体（JSON格式）**
```json
{
    "avatar":"https://****/2026/06/12/948597e164614902ab1662ba8452e106.png",
    "email":"test@qq.com",
    "id":"3",
    "nickName":"ptu",
    "sex":"1"
}
```

**响应格式**
```json
{
    "code":200,
    "msg":"操作成功"
}
```

### 2.16.3 代码实现
#### 1. UserController（blog模块）
```java
@PutMapping("/userInfo")
public ResponseResult updateUserInfo(@RequestBody User user){
    return userService.updateUserInfo(user);
}
```

#### 2. UserService（framework模块，接口）
```java
ResponseResult updateUserInfo(User user);
```

#### 3. UserServiceImpl（framework模块，实现类）
```java
@Override
public ResponseResult updateUserInfo(User user) {
    userMapper.updateById(user);
    return ResponseResult.okResult();
}
```

---

## 2.17 用户注册
### 2.17.1 需求
1. 用户名、昵称、邮箱**不可重复**，重复则注册失败并返回对应提示；
2. 用户名、密码、昵称、邮箱**非空校验**；
3. 密码采用**密文**存入数据库。

### 2.17.2 接口设计
| 请求方式 | 请求地址       | 请求头    |
| -------- | -------------- | --------- |
| POST     | /user/register | 无需Token |

**请求体（JSON格式）**
```json
{
    "email": "string",
    "nickName": "string",
    "password": "string",
    "userName": "string"
}
```

**响应格式**
```json
{
    "code":200,
    "msg":"操作成功"
}
```

### 2.17.3 代码实现
#### 1. UserController（blog模块）
```java
@PostMapping("/register")
public ResponseResult register(@RequestBody User user) {
    return userService.register(user);
}
```

#### 2. UserService（framework模块，接口）
```java
ResponseResult register(User user);
```

#### 3. UserServiceImpl（framework模块，实现类）
```java
@Autowired
private PasswordEncoder passwordEncoder;

@Override
public ResponseResult register(User user) {
    // 1. 非空合法性校验
    // 2. 用户名、邮箱、昵称重复性校验
    // 3. 密码加密
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    // 4. 插入数据库
    userMapper.insert(user);
    return ResponseResult.okResult();
}

// 校验用户名是否存在
private boolean userNameExist(String userName) {
    // 数据库查询逻辑
}

// 校验邮箱是否存在
private boolean emailExist(String email) {
    // 数据库查询逻辑
}
```

#### 4. 枚举类 AppHttpCodeEnum（framework模块，新增注册异常码）
```java
public enum AppHttpCodeEnum {
    NICKNAME_NOT_NULL(511, "昵称不能为空"),
    USERNAME_NOT_NULL(510, "用户名不能为空"),
    PASSWORD_NOT_NULL(512, "密码不能为空"),
    EMAIL_NOT_NULL(513, "邮箱不能为空"),
    NICKNAME_EXIST(514, "昵称已存在");
}
```

#### 5. User实体类（framework模块，自动填充时间）
```java
public class User implements Serializable {
    // 新增时自动填充创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    // 新增/修改时自动填充更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

