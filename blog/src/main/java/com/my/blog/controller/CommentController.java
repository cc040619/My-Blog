package com.my.blog.controller;

import com.my.blog.constants.SystemConstants;
import com.my.blog.domain.ResponseResult;
import com.my.blog.domain.entity.Comment;
import com.my.blog.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 评论表 前端控制器
 * </p>
 *
 * @author WH
 * @since 2025-05-19
 */
@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private ICommentService commentService;

    @GetMapping("/commentList")
    public ResponseResult commentList(Long articleId, Integer pageNum, Integer pageSize) {
        return commentService.commentList(
                SystemConstants.ARTICLE_COMMENT,
                articleId,
                pageNum,
                pageSize
        );
    }

    @GetMapping("/linkCommentList")
    public ResponseResult linkCommentList(Integer pageNum, Integer pageSize) {
        return commentService.commentList(
                SystemConstants.LINK_COMMENT,
                null,
                pageNum,
                pageSize
        );
    }

    @PostMapping
    public ResponseResult comment(@RequestBody Comment comment) {
        return commentService.addComment(comment);
    }
}
