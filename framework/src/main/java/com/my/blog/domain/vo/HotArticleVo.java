package com.my.blog.domain.vo;

import lombok.Data;

@Data
public class HotArticleVo {
    private Long id;
    private String title;
    private Long viewCount;
}
