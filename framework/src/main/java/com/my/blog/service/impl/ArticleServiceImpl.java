package com.my.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.my.blog.constants.SystemConstants;
import com.my.blog.dao.ArticleMapper;
import com.my.blog.dao.CategoryMapper;
import com.my.blog.domain.ResponseResult;
import com.my.blog.domain.entity.Article;
import com.my.blog.domain.entity.Category;
import com.my.blog.domain.vo.ArticleDetailVo;
import com.my.blog.domain.vo.ArticleListVo;
import com.my.blog.domain.vo.HotArticleVo;
import com.my.blog.domain.vo.PageVo;
import com.my.blog.service.IArticleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 文章表 服务实现类
 * </p>
 *
 * @author WH
 * @since 2025-05-19
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {
    @Resource
    ArticleMapper articleMapper;
    @Resource
    CategoryMapper categoryMapper;

    @Override
    public ResponseResult hotArticleList() {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        // 查询正式文章，按浏览量降序
        queryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL)
                    .orderByDesc(Article::getViewCount);
        // 分页取前10条
        Page<Article> page = new Page<>(1, 10);
        articleMapper.selectPage(page, queryWrapper);
        List<Article> articles = page.getRecords();

        // 实体转VO
        List<HotArticleVo> articleVos = new ArrayList<>();
        for (Article article : articles) {
            HotArticleVo vo = new HotArticleVo();
            BeanUtils.copyProperties(article, vo);
            articleVos.add(vo);
        }
        return ResponseResult.okResult(articleVos);
    }

    @Override
    public ResponseResult articleList(Integer pageNum, Integer pageSize, Long categoryId) {
        // 构建查询条件
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        // 只查正式发布文章
        queryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL);
        // 如果指定分类ID，按分类过滤
        if (categoryId != null && categoryId > 0) {
            queryWrapper.eq(Article::getCategoryId, categoryId);
        }
        // 置顶文章优先，按创建时间降序
        queryWrapper.orderByDesc(Article::getIsTop)
                    .orderByDesc(Article::getCreateTime);

        // 分页查询
        Page<Article> page = new Page<>(pageNum, pageSize);
        articleMapper.selectPage(page, queryWrapper);
        List<Article> articles = page.getRecords();

        // 收集所有分类ID，批量查询分类
        List<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> categoryNameMap = null;
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
            categoryNameMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
        }

        // 构建VO列表
        List<ArticleListVo> articleListVos = new ArrayList<>();
        for (Article article : articles) {
            ArticleListVo vo = new ArticleListVo();
            BeanUtils.copyProperties(article, vo);
            // 设置分类名称
            if (categoryNameMap != null && categoryNameMap.containsKey(article.getCategoryId())) {
                vo.setCategoryName(categoryNameMap.get(article.getCategoryId()));
            }
            articleListVos.add(vo);
        }

        PageVo pageVo = new PageVo(articleListVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }

    @Override
    public ResponseResult getArticleDetail(Long id) {
        // 查询文章
        Article article = articleMapper.selectById(id);
        if (article == null) {
            return ResponseResult.errorResult(200, "文章不存在");
        }

        // 查询分类名称
        Category category = categoryMapper.selectById(article.getCategoryId());

        // 构建VO
        ArticleDetailVo vo = new ArticleDetailVo();
        BeanUtils.copyProperties(article, vo);
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        return ResponseResult.okResult(vo);
    }
}
