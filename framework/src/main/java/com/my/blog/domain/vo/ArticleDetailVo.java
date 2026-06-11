package com.my.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleDetailVo {
    private Long categoryId;
    private String categoryName;
    private String content;
    private LocalDateTime createTime;
    private Long id;
    private String isComment;
    private String title;
    private Long viewCount;
}
