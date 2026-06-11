package com.my.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.my.blog.constants.SystemConstants;
import com.my.blog.dao.ArticleMapper;
import com.my.blog.dao.CategoryMapper;
import com.my.blog.domain.ResponseResult;
import com.my.blog.domain.entity.Article;
import com.my.blog.domain.entity.Category;
import com.my.blog.domain.vo.CategoryVo;
import com.my.blog.service.ICategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 分类表 服务实现类
 * </p>
 *
 * @author WH
 * @since 2025-05-19
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {
    @Resource
    private ArticleMapper articleMapper;

    @Override
    public ResponseResult getCategoryList() {
        // 查询所有已发布文章的categoryId（去重）
        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL)
                      .select(Article::getCategoryId);
        List<Article> articles = articleMapper.selectList(articleWrapper);
        List<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        // 如果没有分类，返回空列表
        if (categoryIds.isEmpty()) {
            return ResponseResult.okResult(new ArrayList<>());
        }

        // 查询分类，只查正常状态
        LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.in(Category::getId, categoryIds)
                       .eq(Category::getStatus, "0");
        List<Category> categories = baseMapper.selectList(categoryWrapper);

        // 实体转VO
        List<CategoryVo> categoryVos = new ArrayList<>();
        for (Category category : categories) {
            CategoryVo vo = new CategoryVo();
            BeanUtils.copyProperties(category, vo);
            categoryVos.add(vo);
        }
        return ResponseResult.okResult(categoryVos);
    }
}
