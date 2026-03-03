package com.example.autowebgenerator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.example.autowebgenerator.constant.UserConstant;
import com.example.autowebgenerator.exception.ErrorCode;
import com.example.autowebgenerator.exception.ExceptionUtils;
import com.example.autowebgenerator.mapper.UserMapper;
import com.example.autowebgenerator.model.dto.user.UserQueryRequest;
import com.example.autowebgenerator.model.entity.User;
import com.example.autowebgenerator.model.vo.LoginUserVO;
import com.example.autowebgenerator.model.vo.UserVO;
import com.example.autowebgenerator.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User service implementation backed by MyBatis Flex.
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /** Salt prepended to every password before MD5 hashing. */
    private static final String SALT = "autowebgen";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // Validate inputs
        ExceptionUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword),
                ErrorCode.BAD_REQUEST, "Parameters cannot be blank");
        ExceptionUtils.throwIf(userAccount.length() < 4,
                ErrorCode.BAD_REQUEST, "Account must be at least 4 characters");
        ExceptionUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8,
                ErrorCode.BAD_REQUEST, "Password must be at least 8 characters");
        ExceptionUtils.throwIf(!userPassword.equals(checkPassword),
                ErrorCode.BAD_REQUEST, "Passwords do not match");

        // Check for duplicate accounts
        long count = this.count(QueryWrapper.create().eq("user_account", userAccount));
        ExceptionUtils.throwIf(count > 0, ErrorCode.BAD_REQUEST, "Account already exists");

        // Encrypt password and persist
        String encryptedPassword = DigestUtil.md5Hex(SALT + userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        user.setUserName(userAccount);   // default display name = account name
        user.setUserRole(UserConstant.USER_ROLE);
        user.setCreateTime(new Date());
        user.setEditTime(new Date());

        boolean saved = this.save(user);
        ExceptionUtils.throwIf(!saved, ErrorCode.OPERATION_FAILED, "Registration failed, please try again");

        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // Validate inputs
        ExceptionUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword),
                ErrorCode.BAD_REQUEST, "Parameters cannot be blank");
        ExceptionUtils.throwIf(userAccount.length() < 4,
                ErrorCode.BAD_REQUEST, "Account must be at least 4 characters");
        ExceptionUtils.throwIf(userPassword.length() < 8,
                ErrorCode.BAD_REQUEST, "Password must be at least 8 characters");

        // Look up by account + hashed password
        String encryptedPassword = DigestUtil.md5Hex(SALT + userPassword);
        User user = this.getOne(QueryWrapper.create()
                .eq("user_account", userAccount)
                .eq("user_password", encryptedPassword));
        ExceptionUtils.throwIf(user == null, ErrorCode.BAD_REQUEST, "Invalid account or password");

        // Store in session and return desensitized VO
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return getLoginUserVO(user);
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        ExceptionUtils.throwIf(
                request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null,
                ErrorCode.UNAUTHORIZED, "Not logged in");
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ExceptionUtils.throwIf(userObj == null, ErrorCode.UNAUTHORIZED, "Not logged in");

        // Re-query DB for up-to-date info
        User currentUser = (User) userObj;
        User freshUser = this.getById(currentUser.getId());
        ExceptionUtils.throwIf(freshUser == null, ErrorCode.NOT_FOUND, "User not found");
        return freshUser;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO vo = new LoginUserVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return Collections.emptyList();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest request) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (request == null) {
            return queryWrapper;
        }

        Long id = request.getId();
        String userAccount = request.getUserAccount();
        String userName = request.getUserName();
        String userRole = request.getUserRole();
        String sortBy = request.getSortBy();
        String sortOrder = request.getSortOrder();

        if (id != null) {
            queryWrapper.eq("id", id);
        }
        if (StrUtil.isNotBlank(userAccount)) {
            queryWrapper.like("user_account", userAccount);
        }
        if (StrUtil.isNotBlank(userName)) {
            queryWrapper.like("user_name", userName);
        }
        if (StrUtil.isNotBlank(userRole)) {
            queryWrapper.eq("user_role", userRole);
        }

        // Sorting — caller passes camelCase field names; convert to snake_case for PostgreSQL
        boolean isAsc = !"desc".equalsIgnoreCase(sortOrder);
        if (StrUtil.isNotBlank(sortBy)) {
            // Convert camelCase sortBy to snake_case (e.g. "createTime" → "create_time")
            String snakeSortBy = sortBy.replaceAll("([A-Z])", "_$1").toLowerCase();
            queryWrapper.orderBy(snakeSortBy, isAsc);
        } else {
            queryWrapper.orderBy("create_time", false);
        }

        return queryWrapper;
    }
}
