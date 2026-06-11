package com.my.blog.controller;

import com.my.blog.domain.ResponseResult;
import com.my.blog.service.ILinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 友链 前端控制器
 * </p>
 *
 * @author WH
 * @since 2025-05-19
 */
@RestController
@RequestMapping("/link")
public class LinkController {
    @Autowired
    private ILinkService linkService;

    @GetMapping("/getAllLink")
    public ResponseResult getAllLink() {
        return linkService.getAllLink();
    }
}
