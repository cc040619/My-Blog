package com.my.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVo {
    private Long id;
    private String content;
    private Long toCommentUserId;
    private String toCommentUserName;
    private Long createBy;
    private String username;
    private LocalDateTime createTime;
    private List<CommentVo> children;
}
