package com.my.blog.controller;

import com.my.blog.domain.ResponseResult;
import com.my.blog.enums.AppHttpCodeEnum;
import com.my.blog.utils.AliyunOSSOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
public class UploadController {

    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;

    @PostMapping("/upload")
    public ResponseResult upload(@RequestParam("file") MultipartFile file) {
        try {
            String url = aliyunOSSOperator.upload(file.getBytes(), file.getOriginalFilename());
            return ResponseResult.okResult(url);
        } catch (Exception e) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "上传失败：" + e.getMessage());
        }
    }
}
