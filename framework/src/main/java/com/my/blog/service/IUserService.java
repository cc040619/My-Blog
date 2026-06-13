package com.my.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.my.blog.domain.ResponseResult;
import com.my.blog.domain.entity.User;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author WH
 * @since 2025-05-19
 */
public interface IUserService extends IService<User> {

    ResponseResult userInfo(Long userId);

    ResponseResult updateUserInfo(User user);

    ResponseResult register(User user);

}
