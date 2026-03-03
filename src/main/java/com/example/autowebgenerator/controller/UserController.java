package com.example.autowebgenerator.controller;

import com.example.autowebgenerator.annotation.AuthCheck;
import com.example.autowebgenerator.common.ApiResponse;
import com.example.autowebgenerator.common.ApiResponseUtils;
import com.example.autowebgenerator.common.DeleteByIdRequest;
import com.example.autowebgenerator.constant.UserConstant;
import com.example.autowebgenerator.exception.ErrorCode;
import com.example.autowebgenerator.exception.ExceptionUtils;
import com.example.autowebgenerator.model.dto.user.*;
import com.example.autowebgenerator.model.entity.User;
import com.example.autowebgenerator.model.vo.LoginUserVO;
import com.example.autowebgenerator.model.vo.UserVO;
import com.example.autowebgenerator.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User-related endpoints: register, login, logout, profile, and admin CRUD.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    // -------------------------------------------------------------------------
    // Public endpoints
    // -------------------------------------------------------------------------

    /** Register a new account. Returns the new user's id. */
    @PostMapping("/register")
    public ApiResponse<Long> userRegister(@RequestBody UserRegisterRequest request) {
        ExceptionUtils.throwIf(request == null, ErrorCode.BAD_REQUEST);
        long userId = userService.userRegister(
                request.getUserAccount(),
                request.getUserPassword(),
                request.getCheckPassword());
        return ApiResponseUtils.success(userId);
    }

    /** Log in and receive a session cookie. */
    @PostMapping("/login")
    public ApiResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest request,
                                              HttpServletRequest httpRequest) {
        ExceptionUtils.throwIf(request == null, ErrorCode.BAD_REQUEST);
        LoginUserVO loginUserVO = userService.userLogin(
                request.getUserAccount(),
                request.getUserPassword(),
                httpRequest);
        return ApiResponseUtils.success(loginUserVO);
    }

    /** Log out — invalidates the session entry for this user. */
    @PostMapping("/logout")
    public ApiResponse<Boolean> userLogout(HttpServletRequest request) {
        ExceptionUtils.throwIf(request == null, ErrorCode.BAD_REQUEST);
        boolean result = userService.userLogout(request);
        return ApiResponseUtils.success(result);
    }

    /** Get the currently logged-in user's desensitized info. Returns 40100 (not a thrown exception) if not logged in. */
    @GetMapping("/get/login")
    public ApiResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null) {
            return new ApiResponse<>(ErrorCode.UNAUTHORIZED.getCode(), null, "Not logged in");
        }
        User loginUser = userService.getLoginUser(request);
        return ApiResponseUtils.success(userService.getLoginUserVO(loginUser));
    }

    // -------------------------------------------------------------------------
    // General user endpoints (any logged-in user)
    // -------------------------------------------------------------------------

    /** Get a user's public VO by id. */
    @GetMapping("/get/vo")
    public ApiResponse<UserVO> getUserVoById(@RequestParam long id) {
        ExceptionUtils.throwIf(id <= 0, ErrorCode.BAD_REQUEST, "Invalid id");
        User user = userService.getById(id);
        ExceptionUtils.throwIf(user == null, ErrorCode.NOT_FOUND, "User not found");
        return ApiResponseUtils.success(userService.getUserVO(user));
    }

    // -------------------------------------------------------------------------
    // Admin-only endpoints
    // -------------------------------------------------------------------------

    /** [Admin] Create a user directly (bypasses register flow). */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Long> addUser(@RequestBody UserAddRequest request) {
        ExceptionUtils.throwIf(request == null, ErrorCode.BAD_REQUEST);
        User user = new User();
        user.setUserAccount(request.getUserAccount());
        user.setUserPassword(request.getUserPassword());
        user.setUserName(request.getUserName());
        user.setUserAvatar(request.getUserAvatar());
        user.setUserProfile(request.getUserProfile());
        user.setUserRole(request.getUserRole());
        boolean saved = userService.save(user);
        ExceptionUtils.throwIf(!saved, ErrorCode.OPERATION_FAILED);
        return ApiResponseUtils.success(user.getId());
    }

    /** [Admin] Get full user entity (including role) by id. */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<User> getUserById(@RequestParam long id) {
        ExceptionUtils.throwIf(id <= 0, ErrorCode.BAD_REQUEST, "Invalid id");
        User user = userService.getById(id);
        ExceptionUtils.throwIf(user == null, ErrorCode.NOT_FOUND, "User not found");
        return ApiResponseUtils.success(user);
    }

    /** [Admin] Delete a user by id (logical delete). */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Boolean> deleteUser(@RequestBody DeleteByIdRequest request) {
        ExceptionUtils.throwIf(request == null || request.getId() == null, ErrorCode.BAD_REQUEST);
        boolean removed = userService.removeById(request.getId());
        return ApiResponseUtils.success(removed);
    }

    /** [Admin] Update a user's profile or role. */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Boolean> updateUser(@RequestBody UserUpdateRequest request) {
        ExceptionUtils.throwIf(request == null || request.getId() == null, ErrorCode.BAD_REQUEST);
        User user = new User();
        user.setId(request.getId());
        user.setUserName(request.getUserName());
        user.setUserAvatar(request.getUserAvatar());
        user.setUserProfile(request.getUserProfile());
        user.setUserRole(request.getUserRole());
        boolean updated = userService.updateById(user);
        ExceptionUtils.throwIf(!updated, ErrorCode.OPERATION_FAILED);
        return ApiResponseUtils.success(true);
    }

    /** [Admin] Paginated list of full user entities. */
    @PostMapping("/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest request) {
        ExceptionUtils.throwIf(request == null, ErrorCode.BAD_REQUEST);
        long pageNum  = request.getPage();
        long pageSize = request.getSize();
        QueryWrapper queryWrapper = userService.getQueryWrapper(request);
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ApiResponseUtils.success(userPage);
    }

    /** [Admin] Paginated list of desensitized user VOs. */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<UserVO>> listUserVoByPage(@RequestBody UserQueryRequest request) {
        ExceptionUtils.throwIf(request == null, ErrorCode.BAD_REQUEST);
        long pageNum  = request.getPage();
        long pageSize = request.getSize();
        QueryWrapper queryWrapper = userService.getQueryWrapper(request);
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize), queryWrapper);

        // Convert entity page to VO page
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ApiResponseUtils.success(userVOPage);
    }
}
