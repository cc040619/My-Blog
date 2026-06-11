package com.my.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.my.blog.dao.CommentMapper;
import com.my.blog.dao.UserMapper;
import com.my.blog.domain.ResponseResult;
import com.my.blog.domain.entity.Comment;
import com.my.blog.domain.entity.User;
import com.my.blog.domain.vo.CommentVo;
import com.my.blog.domain.vo.PageVo;
import com.my.blog.service.ICommentService;
import com.my.blog.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 评论表 服务实现类
 * </p>
 *
 * @author WH
 * @since 2025-05-19
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public ResponseResult commentList(String commentType, Long articleId, Integer pageNum, Integer pageSize) {
        // 构造查询条件，查询指定类型的根评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getType, commentType);
        // 对于文章评论，按articleId过滤
        if (articleId != null) {
            queryWrapper.eq(Comment::getArticleId, articleId);
        }
        // 根评论 rootId = -1
        queryWrapper.eq(Comment::getRootId, -1);
        // 根评论按创建时间降序
        queryWrapper.orderByDesc(Comment::getCreateTime);

        // 分页查询
        Page<Comment> page = new Page<>(pageNum, pageSize);
        commentMapper.selectPage(page, queryWrapper);
        List<Comment> rootComments = page.getRecords();

        // 将Comment列表封装为CommentVo列表
        List<CommentVo> commentVoList = buildCommentVoList(rootComments);

        // 查询所有根评论对应的子评论，并赋值给children属性
        for (CommentVo commentVo : commentVoList) {
            List<CommentVo> children = getChildComments(commentVo.getId());
            commentVo.setChildren(children);
        }

        PageVo pageVo = new PageVo(commentVoList, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }

    /**
     * 构造CommentVo列表，填充用户名信息
     */
    private List<CommentVo> buildCommentVoList(List<Comment> commentList) {
        if (commentList == null || commentList.isEmpty()) {
            return Collections.emptyList();
        }

        List<CommentVo> commentVoList = BeanCopyUtils.copyBeanList(commentList, CommentVo.class);

        // 收集所有需要查询用户的ID（createBy 和 toCommentUserId）
        List<Long> userIds = new ArrayList<>();
        for (Comment comment : commentList) {
            userIds.add(comment.getCreateBy());
            if (comment.getToCommentUserId() != null && comment.getToCommentUserId() != -1) {
                userIds.add(comment.getToCommentUserId());
            }
        }

        // 批量查询用户，构建ID->昵称映射
        Map<Long, String> userNickNameMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(
                    userIds.stream().distinct().collect(Collectors.toList()));
            userNickNameMap = users.stream()
                    .collect(Collectors.toMap(User::getId, User::getNickName));
        }

        // 填充用户名
        for (CommentVo commentVo : commentVoList) {
            commentVo.setUsername(userNickNameMap.get(commentVo.getCreateBy()));
            if (commentVo.getToCommentUserId() != null && commentVo.getToCommentUserId() != -1) {
                commentVo.setToCommentUserName(userNickNameMap.get(commentVo.getToCommentUserId()));
            }
        }

        return commentVoList;
    }

    @Override
    public ResponseResult addComment(Comment comment) {
        save(comment);
        return ResponseResult.okResult();
    }

    /**
     * 根据根评论id查询对应的子评论集合
     */
    private List<CommentVo> getChildComments(Long id) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getRootId, id)
                    .orderByAsc(Comment::getCreateTime);

        List<Comment> childComments = commentMapper.selectList(queryWrapper);

        return buildCommentVoList(childComments);
    }
}
