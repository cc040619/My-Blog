package com.my.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.my.blog.constants.SystemConstants;
import com.my.blog.dao.LinkMapper;
import com.my.blog.domain.ResponseResult;
import com.my.blog.domain.entity.Link;
import com.my.blog.domain.vo.LinkVo;
import com.my.blog.service.ILinkService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 友链 服务实现类
 * </p>
 *
 * @author WH
 * @since 2025-05-19
 */
@Service
public class LinkServiceImpl extends ServiceImpl<LinkMapper, Link> implements ILinkService {

    @Override
    public ResponseResult getAllLink() {
        LambdaQueryWrapper<Link> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Link::getStatus, SystemConstants.LINK_STATUS_PASS);

        List<Link> links = baseMapper.selectList(queryWrapper);

        List<LinkVo> linkVos = new ArrayList<>();
        for (Link link : links) {
            LinkVo vo = new LinkVo();
            BeanUtils.copyProperties(link, vo);
            linkVos.add(vo);
        }
        return ResponseResult.okResult(linkVos);
    }
}
