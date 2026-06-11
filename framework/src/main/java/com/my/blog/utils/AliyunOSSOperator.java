package com.my.blog.utils;

import com.alibaba.excel.util.StringUtils;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 【AliyunOSSOperator】工具类
 * &lt;p&gt;核心功能：阿里云OSS对象存储操作工具，提供文件上传功能——自动按日期分目录存储、UUID生成唯一文件名、支持V4签名，凭证获取优先级为application.yml配置优先于环境变量&lt;/p&gt;
 * &lt;p&gt;使用场景：被头像上传、帖子图片上传等文件相关的Controller或Service调用，将用户上传的图片文件存储到阿里云OSS并返回公网可访问的URL&lt;/p&gt;
 *
 * @author zcongcong
 * @date 2026-05-27
 */
@Component
public class AliyunOSSOperator {
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.bucketName}")
    private String bucketName;
    @Value("${aliyun.oss.region}")
    private String region;
    @Value("${aliyun.oss.accessKeyId:}")
    private String accessKeyId;
    @Value("${aliyun.oss.accessKeySecret:}")
    private String accessKeySecret;

    /**
     * 获取凭证提供者
     * 优先使用 application.yml 中配置的 accessKeyId/accessKeySecret，
     * 如果配置为空则回退到环境变量 OSS_ACCESS_KEY_ID / OSS_ACCESS_KEY_SECRET
     */
    private CredentialsProvider getCredentialsProvider() throws ClientException {
        if (accessKeyId != null && !StringUtils.isBlank(accessKeyId)
                && accessKeySecret != null && !StringUtils.isBlank(accessKeySecret)) {
            return new DefaultCredentialProvider(accessKeyId, accessKeySecret);
        }
        return CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
    }

    /**
     * 上传文件到阿里云OSS
     * 业务逻辑：
     * 1. 获取凭证提供者（优先配置文件accessKeyId/accessKeySecret，为空则回退环境变量）
     * 2. 生成对象存储路径：yyyy/MM/{UUID}{文件扩展名}（按日期分目录，UUID防重名）
     * 3. 创建OSSClient（使用V4签名版本、指定endpoint和region）
     * 4. 将字节内容以ByteArrayInputStream写入OSS bucket
     * 5. finally块关闭OSSClient释放连接
     * 6. 拼接返回公网访问URL：协议://bucket.endpoint/objectName
     * 异常场景：凭证获取失败、OSS上传失败、bucket不存在等均抛出Exception，由调用方处理
     *
     * @param content          文件字节内容（必填）
     * @param originalFilename 原始文件名（必填，用于提取文件扩展名）
     * @return OSS公网可访问的文件URL
     * @throws Exception 凭证获取失败、OSS连接失败或上传异常时抛出
     */
    public String upload(byte[] content, String originalFilename) throws Exception {
        CredentialsProvider credentialsProvider = getCredentialsProvider();

        // 填写Object完整路径，例如blog-web/202406/1.png。Object完整路径中不能包含Bucket名称。
        // 获取当前系统日期的字符串,格式为 yyyy/MM
        String dir = "blog-web/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        // 生成一个新的不重复的文件名
        String newFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dir + "/" + newFileName;

        // 创建OSSClient实例。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
        } finally {
            ossClient.shutdown();
        }

        return endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + objectName;
    }

}
