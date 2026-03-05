package com.example.autowebgenerator.controller;

import com.example.autowebgenerator.annotation.AuthCheck;
import com.example.autowebgenerator.common.ApiResponse;
import com.example.autowebgenerator.common.ApiResponseUtils;
import com.example.autowebgenerator.constant.UserConstant;
import com.example.autowebgenerator.exception.ErrorCode;
import com.example.autowebgenerator.exception.ExceptionUtils;
import com.example.autowebgenerator.model.entity.ChatHistory;
import com.example.autowebgenerator.model.entity.User;
import com.example.autowebgenerator.service.ChatHistoryService;
import com.example.autowebgenerator.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat-history")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * Get chat history for an app (cursor-based, owner or admin only).
     * Results are sorted by createTime DESC (newest first).
     *
     * @param appId          app ID
     * @param pageSize       number of messages to return (default 10, max 50)
     * @param lastCreateTime cursor — epoch ms of the oldest message already loaded; null for first load
     */
    @GetMapping("/app/{appId}")
    public ApiResponse<Page<ChatHistory>> listAppChatHistory(
            @PathVariable Long appId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long lastCreateTime,
            HttpServletRequest request) {
        ExceptionUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_REQUEST);
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return ApiResponseUtils.success(result);
    }

    /**
     * Admin: paginate all chat history sorted by createTime DESC.
     */
    @GetMapping("/admin/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<ChatHistory>> listAllChatHistoryForAdmin(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long appId) {
        QueryWrapper qw = QueryWrapper.create().orderBy("create_time", false);
        if (appId != null) qw.eq("app_id", appId);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(page, pageSize), qw);
        return ApiResponseUtils.success(result);
    }
}
