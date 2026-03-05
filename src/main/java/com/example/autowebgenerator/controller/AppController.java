package com.example.autowebgenerator.controller;

import com.example.autowebgenerator.annotation.AuthCheck;
import com.example.autowebgenerator.common.ApiResponse;
import com.example.autowebgenerator.common.ApiResponseUtils;
import com.example.autowebgenerator.common.DeleteByIdRequest;
import com.example.autowebgenerator.constant.AppConstant;
import com.example.autowebgenerator.constant.UserConstant;
import com.example.autowebgenerator.exception.ErrorCode;
import com.example.autowebgenerator.exception.ExceptionUtils;
import com.example.autowebgenerator.model.dto.app.*;
import com.example.autowebgenerator.model.entity.App;
import com.example.autowebgenerator.model.entity.User;
import com.example.autowebgenerator.model.enums.CodeGenTypeEnum;
import com.example.autowebgenerator.model.vo.AppVO;
import com.example.autowebgenerator.service.AppService;
import com.example.autowebgenerator.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Resource
    private com.example.autowebgenerator.service.ProjectDownloadService projectDownloadService;

    // -------------------------------------------------------------------------
    // User endpoints
    // -------------------------------------------------------------------------

    /** Create an app — only initPrompt is required. */
    @PostMapping("/add")
    public ApiResponse<String> addApp(@RequestBody AppAddRequest request, HttpServletRequest httpRequest) {
        ExceptionUtils.throwIf(request == null, ErrorCode.BAD_REQUEST);
        String initPrompt = request.getInitPrompt();
        ExceptionUtils.throwIf(initPrompt == null || initPrompt.isBlank(), ErrorCode.BAD_REQUEST, "initPrompt cannot be blank");

        User loginUser = userService.getLoginUser(httpRequest);
        App app = new App();
        app.setInitPrompt(initPrompt);
        app.setUserId(loginUser.getId());
        // Default name = first 40 chars of initPrompt
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 40)));
        app.setCodeGenType(CodeGenTypeEnum.VUE_PROJECT.getValue());
        app.setPriority(AppConstant.DEFAULT_APP_PRIORITY);
        app.setIsDelete(0);
        app.setCreateTime(new Date());
        app.setEditTime(new Date());

        boolean saved = appService.save(app);
        ExceptionUtils.throwIf(!saved, ErrorCode.OPERATION_FAILED);
        return ApiResponseUtils.success(String.valueOf(app.getId()));
    }

    /** Update own app (name only). */
    @PostMapping("/update")
    public ApiResponse<Boolean> updateApp(@RequestBody AppUpdateRequest request, HttpServletRequest httpRequest) {
        ExceptionUtils.throwIf(request == null || request.getId() == null, ErrorCode.BAD_REQUEST);
        User loginUser = userService.getLoginUser(httpRequest);

        App old = appService.getById(request.getId());
        ExceptionUtils.throwIf(old == null, ErrorCode.NOT_FOUND);
        ExceptionUtils.throwIf(!old.getUserId().equals(loginUser.getId()), ErrorCode.FORBIDDEN);

        App app = new App();
        app.setId(request.getId());
        app.setAppName(request.getAppName());
        app.setEditTime(new Date());
        boolean updated = appService.updateById(app);
        ExceptionUtils.throwIf(!updated, ErrorCode.OPERATION_FAILED);
        return ApiResponseUtils.success(true);
    }

    /** Delete own app (owner or admin). */
    @PostMapping("/delete")
    public ApiResponse<Boolean> deleteApp(@RequestBody DeleteByIdRequest request, HttpServletRequest httpRequest) {
        ExceptionUtils.throwIf(request == null || request.getId() == null, ErrorCode.BAD_REQUEST);
        User loginUser = userService.getLoginUser(httpRequest);

        App old = appService.getById(request.getId());
        ExceptionUtils.throwIf(old == null, ErrorCode.NOT_FOUND);
        boolean isOwner = old.getUserId().equals(loginUser.getId());
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        ExceptionUtils.throwIf(!isOwner && !isAdmin, ErrorCode.FORBIDDEN);

        boolean removed = appService.removeById(request.getId());
        return ApiResponseUtils.success(removed);
    }

    /** Get app VO by id. */
    @GetMapping("/get/vo")
    public ApiResponse<AppVO> getAppVoById(@RequestParam long id) {
        ExceptionUtils.throwIf(id <= 0, ErrorCode.BAD_REQUEST);
        App app = appService.getById(id);
        ExceptionUtils.throwIf(app == null, ErrorCode.NOT_FOUND);
        return ApiResponseUtils.success(appService.getAppVO(app));
    }

    /** Paginated list of the current user's own apps (max 20 per page). */
    @PostMapping("/my/list/page/vo")
    public ApiResponse<Page<AppVO>> listMyAppVoByPage(@RequestBody AppQueryRequest request, HttpServletRequest httpRequest) {
        ExceptionUtils.throwIf(request == null, ErrorCode.BAD_REQUEST);
        ExceptionUtils.throwIf(request.getSize() > 20, ErrorCode.BAD_REQUEST, "Max 20 per page");

        User loginUser = userService.getLoginUser(httpRequest);
        request.setUserId(loginUser.getId());

        QueryWrapper qw = appService.getQueryWrapper(request);
        Page<App> appPage = appService.page(Page.of(request.getPage(), request.getSize()), qw);

        Page<AppVO> voPage = new Page<>(request.getPage(), request.getSize(), appPage.getTotalRow());
        List<AppVO> voList = appService.getAppVOList(appPage.getRecords());
        voPage.setRecords(voList);
        return ApiResponseUtils.success(voPage);
    }

    /** Paginated list of featured apps (priority = 99, max 20 per page). */
    @PostMapping("/good/list/page/vo")
    public ApiResponse<Page<AppVO>> listGoodAppVoByPage(@RequestBody AppQueryRequest request) {
        ExceptionUtils.throwIf(request == null, ErrorCode.BAD_REQUEST);
        ExceptionUtils.throwIf(request.getSize() > 20, ErrorCode.BAD_REQUEST, "Max 20 per page");

        request.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper qw = appService.getQueryWrapper(request);
        Page<App> appPage = appService.page(Page.of(request.getPage(), request.getSize()), qw);

        Page<AppVO> voPage = new Page<>(request.getPage(), request.getSize(), appPage.getTotalRow());
        voPage.setRecords(appService.getAppVOList(appPage.getRecords()));
        return ApiResponseUtils.success(voPage);
    }

    // -------------------------------------------------------------------------
    // AI chat / deploy
    // -------------------------------------------------------------------------

    /** Stream AI code generation for an app via SSE (owner only). */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        return appService.chatToGenCode(appId, message, loginUser)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)  // chunk is already typed JSON: {"type":"...", ...}
                        .build())
                .onErrorResume(e -> {
                    String clean;
                    try {
                        clean = JSONUtil.parseObj(e.getMessage()).getByPath("error.message", String.class);
                        if (clean == null) clean = e.getMessage();
                    } catch (Exception ignored) {
                        clean = e.getMessage() != null ? e.getMessage() : "AI service error";
                    }
                    return Mono.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data(JSONUtil.toJsonStr(Map.of("msg", clean)))
                            .build());
                })
                .concatWith(Mono.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("")
                        .build()));
    }

    /** Download the source code of an app as a ZIP file (owner only). */
    @GetMapping("/download/{appId}")
    public void downloadAppCode(@PathVariable Long appId,
                                HttpServletRequest httpRequest,
                                HttpServletResponse httpResponse) throws Exception {
        ExceptionUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_REQUEST);
        User loginUser = userService.getLoginUser(httpRequest);

        App app = appService.getById(appId);
        ExceptionUtils.throwIf(app == null, ErrorCode.NOT_FOUND, "App not found");
        ExceptionUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.FORBIDDEN, "No permission");

        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.fromValue(app.getCodeGenType());
        ExceptionUtils.throwIf(codeGenTypeEnum == null, ErrorCode.SYSTEM_ERROR, "Unsupported code gen type");

        String projectDirName = codeGenTypeEnum.getValue() + "_" + appId;
        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/" + projectDirName;
        projectDownloadService.downloadProjectAsZip(projectPath, appId + ".zip", httpResponse);
    }

    /** Deploy an app — generates a deployKey and copies files. */
    @PostMapping("/deploy")
    public ApiResponse<String> deployApp(@RequestParam Long appId, HttpServletRequest httpRequest) {
        ExceptionUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_REQUEST);
        User loginUser = userService.getLoginUser(httpRequest);
        String deployKey = appService.deployApp(appId, loginUser);
        return ApiResponseUtils.success(deployKey);
    }

    // -------------------------------------------------------------------------
    // Admin endpoints
    // -------------------------------------------------------------------------

    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteByIdRequest request) {
        ExceptionUtils.throwIf(request == null || request.getId() == null, ErrorCode.BAD_REQUEST);
        App old = appService.getById(request.getId());
        ExceptionUtils.throwIf(old == null, ErrorCode.NOT_FOUND);
        return ApiResponseUtils.success(appService.removeById(request.getId()));
    }

    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest request) {
        ExceptionUtils.throwIf(request == null || request.getId() == null, ErrorCode.BAD_REQUEST);
        App app = new App();
        app.setId(request.getId());
        app.setAppName(request.getAppName());
        app.setCover(request.getCover());
        app.setPriority(request.getPriority());
        boolean updated = appService.updateById(app);
        ExceptionUtils.throwIf(!updated, ErrorCode.OPERATION_FAILED);
        return ApiResponseUtils.success(true);
    }

    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<AppVO> getAppVoByIdByAdmin(@RequestParam long id) {
        ExceptionUtils.throwIf(id <= 0, ErrorCode.BAD_REQUEST);
        App app = appService.getById(id);
        ExceptionUtils.throwIf(app == null, ErrorCode.NOT_FOUND);
        return ApiResponseUtils.success(appService.getAppVO(app));
    }

    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<AppVO>> listAppVoByPageByAdmin(@RequestBody AppQueryRequest request) {
        ExceptionUtils.throwIf(request == null, ErrorCode.BAD_REQUEST);
        QueryWrapper qw = appService.getQueryWrapper(request);
        Page<App> appPage = appService.page(Page.of(request.getPage(), request.getSize()), qw);
        Page<AppVO> voPage = new Page<>(request.getPage(), request.getSize(), appPage.getTotalRow());
        voPage.setRecords(appService.getAppVOList(appPage.getRecords()));
        return ApiResponseUtils.success(voPage);
    }
}
