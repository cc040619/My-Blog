package com.my.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleListVo {
    private Long id;
    private String title;
    private String summary;
    private String categoryName;
    private String thumbnail;
    private Long viewCount;
    private LocalDateTime createTime;
}
