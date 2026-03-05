package com.example.autowebgenerator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.autowebgenerator.constant.UserConstant;
import com.example.autowebgenerator.core.AiCodeGeneratorFacade;
import com.example.autowebgenerator.core.CodeFileSaver;
import com.example.autowebgenerator.core.builder.VueProjectBuilder;
import com.example.autowebgenerator.exception.ErrorCode;
import com.example.autowebgenerator.exception.ExceptionUtils;
import com.example.autowebgenerator.mapper.AppMapper;
import com.example.autowebgenerator.model.dto.app.AppQueryRequest;
import com.example.autowebgenerator.model.entity.App;
import com.example.autowebgenerator.model.entity.User;
import com.example.autowebgenerator.model.enums.CodeGenTypeEnum;
import com.example.autowebgenerator.model.enums.MessageTypeEnum;
import com.example.autowebgenerator.model.vo.AppVO;
import com.example.autowebgenerator.model.vo.UserVO;
import com.example.autowebgenerator.service.AppCoverService;
import com.example.autowebgenerator.service.AppService;
import com.example.autowebgenerator.service.ChatHistoryService;
import com.example.autowebgenerator.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Lazy
    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private AppCoverService appCoverService;

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) return null;
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            appVO.setUser(userService.getUserVO(user));
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) return new ArrayList<>();
        Set<Long> userIds = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            appVO.setUser(userVOMap.get(app.getUserId()));
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest request) {
        ExceptionUtils.throwIf(request == null, ErrorCode.BAD_REQUEST, "Request cannot be null");
        String sortBy = request.getSortBy();
        String sortOrder = request.getSortOrder();
        String snakeSortBy = StrUtil.isNotBlank(sortBy)
                ? sortBy.replaceAll("([A-Z])", "_$1").toLowerCase()
                : "create_time";
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        QueryWrapper qw = QueryWrapper.create();
        if (request.getId() != null) qw.eq("id", request.getId());
        if (StrUtil.isNotBlank(request.getAppName())) qw.like("app_name", request.getAppName());
        if (StrUtil.isNotBlank(request.getInitPrompt())) qw.like("init_prompt", request.getInitPrompt());
        if (StrUtil.isNotBlank(request.getCodeGenType())) qw.eq("code_gen_type", request.getCodeGenType());
        if (StrUtil.isNotBlank(request.getDeployKey())) qw.eq("deploy_key", request.getDeployKey());
        if (request.getPriority() != null) qw.eq("priority", request.getPriority());
        if (request.getUserId() != null) qw.eq("user_id", request.getUserId());
        qw.orderBy(snakeSortBy, isAsc);
        return qw;
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        ExceptionUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_REQUEST, "App ID cannot be blank");
        ExceptionUtils.throwIf(StrUtil.isBlank(message), ErrorCode.BAD_REQUEST, "Message cannot be blank");

        App app = this.getById(appId);
        ExceptionUtils.throwIf(app == null, ErrorCode.NOT_FOUND, "App not found");
        ExceptionUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.FORBIDDEN, "No permission");

        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.fromValue(codeGenTypeStr);
        ExceptionUtils.throwIf(codeGenTypeEnum == null, ErrorCode.SYSTEM_ERROR, "Unsupported code gen type");

        // Save user message to history
        chatHistoryService.addChatMessage(appId, message, MessageTypeEnum.USER.getValue(), loginUser.getId());

        StringBuilder aiResponseBuilder = new StringBuilder();
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId)
                .map(chunk -> {
                    // Each chunk is typed JSON: {"type":"ai_response","data":"..."} or
                    // {"type":"tool_executed","id":"...","name":"...","result":"OK: path"}
                    // Build a clean DB string while passing the original typed chunk downstream.
                    try {
                        var json = JSONUtil.parseObj(chunk);
                        String type = json.getStr("type");
                        if ("ai_response".equals(type)) {
                            aiResponseBuilder.append(json.getStr("data"));
                        } else if ("tool_executed".equals(type)) {
                            String path    = json.getStr("path");
                            String content = json.getStr("content");
                            String lang    = fileLang(path);
                            aiResponseBuilder.append("\n[Tool] Writing file: ").append(path)
                                    .append("\n```").append(lang).append("\n")
                                    .append(content)
                                    .append("\n```\n");
                        }
                    } catch (Exception ignored) {
                        aiResponseBuilder.append(chunk);
                    }
                    return chunk;
                })
                .doOnComplete(() -> {
                    String aiResponse = aiResponseBuilder.toString();
                    if (!aiResponse.isBlank()) {
                        chatHistoryService.addChatMessage(appId, aiResponse, MessageTypeEnum.AI.getValue(), loginUser.getId());
                    }
                })
                .doOnError(error -> {
                    String errMsg = "AI generation failed: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errMsg, MessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        App app = this.getById(appId);
        ExceptionUtils.throwIf(app == null, ErrorCode.NOT_FOUND, "App not found");
        boolean isOwner = app.getUserId().equals(loginUser.getId());
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        ExceptionUtils.throwIf(!isOwner && !isAdmin, ErrorCode.FORBIDDEN, "No permission");

        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.fromValue(app.getCodeGenType());

        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
            while (this.count(QueryWrapper.create().eq("deploy_key", deployKey)) > 0) {
                deployKey = RandomUtil.randomString(6);
            }
        }

        if (CodeGenTypeEnum.VUE_PROJECT == codeGenTypeEnum) {
            // Build the Vue project synchronously before copying dist/
            vueProjectBuilder.buildProject(VueProjectBuilder.projectPath(appId));
            CodeFileSaver.deployVueProjectForApp(appId, deployKey);
        } else {
            CodeFileSaver.deployCodeForApp(appId, deployKey);
        }

        App update = new App();
        update.setId(appId);
        update.setDeployKey(deployKey);
        update.setDeployedTime(new Date());
        this.updateById(update);

        // Async: generate cover screenshot from the deployed site
        appCoverService.generateCoverAsync(appId, deployKey);

        return deployKey;
    }

    @Override
    public boolean removeById(Serializable id) {
        if (id == null) return false;
        Long appId = Long.valueOf(id.toString());
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            log.error("Failed to cascade-delete chat history for app {}: {}", appId, e.getMessage());
        }
        // Delete cover image file if it exists
        java.io.File coverFile = new java.io.File(
                System.getProperty("user.dir") + "/tmp/covers/" + appId + ".jpg");
        if (coverFile.exists()) {
            boolean deleted = coverFile.delete();
            if (!deleted) log.warn("Failed to delete cover file for app {}", appId);
        }
        return super.removeById(id);
    }

    /** Maps a file extension to a fenced-code-block language tag. */
    private static String fileLang(String path) {
        if (path == null) return "";
        int dot = path.lastIndexOf('.');
        if (dot < 0) return "";
        return switch (path.substring(dot + 1).toLowerCase()) {
            case "vue"  -> "vue";
            case "js"   -> "javascript";
            case "ts"   -> "typescript";
            case "html" -> "html";
            case "css"  -> "css";
            case "json" -> "json";
            default     -> path.substring(dot + 1);
        };
    }
}
