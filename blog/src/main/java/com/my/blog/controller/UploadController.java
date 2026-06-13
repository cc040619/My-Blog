package com.my.blog.controller;

import com.my.blog.domain.ResponseResult;
import com.my.blog.enums.AppHttpCodeEnum;
import com.my.blog.exception.SystemException;
import com.my.blog.utils.AliyunOSSOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping
public class UploadController {

    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".png", ".jpg", ".jpeg");
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    @PostMapping("/upload")
    public ResponseResult upload(@RequestParam("file") MultipartFile file) {
        // 文件类型校验
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new SystemException(AppHttpCodeEnum.FILE_TYPE_ERROR);
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new SystemException(AppHttpCodeEnum.FILE_TYPE_ERROR);
        }
        // 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new SystemException(AppHttpCodeEnum.FILE_SIZE_ERROR);
        }
        try {
            String url = aliyunOSSOperator.upload(file.getBytes(), originalFilename);
            return ResponseResult.okResult(url);
        } catch (Exception e) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "上传失败：" + e.getMessage());
        }
    }
}
