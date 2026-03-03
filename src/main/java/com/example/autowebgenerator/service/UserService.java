package com.example.autowebgenerator.service;

import com.example.autowebgenerator.model.dto.user.UserQueryRequest;
import com.example.autowebgenerator.model.entity.User;
import com.example.autowebgenerator.model.vo.LoginUserVO;
import com.example.autowebgenerator.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * User service interface.
 */
public interface UserService extends IService<User> {

    /**
     * Register a new user.
     *
     * @return the new user's id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * Log in and return desensitized user info.
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * Log out — removes the user from the session.
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * Get the currently logged-in user (re-queries DB for freshness).
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * Convert a User entity to the login VO (strips password).
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * Convert a User entity to the admin-facing VO (strips password).
     */
    UserVO getUserVO(User user);

    /**
     * Batch-convert a list of User entities to VOs.
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * Build a QueryWrapper from the admin query request (pagination handled separately).
     */
    QueryWrapper getQueryWrapper(UserQueryRequest request);
}
