package com.example.autowebgenerator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.example.autowebgenerator.constant.AppConstant;
import com.example.autowebgenerator.constant.UserConstant;
import com.example.autowebgenerator.core.AiCodeGeneratorFacade;
import com.example.autowebgenerator.core.CodeFileSaver;
import com.example.autowebgenerator.exception.ErrorCode;
import com.example.autowebgenerator.exception.ExceptionUtils;
import com.example.autowebgenerator.mapper.AppMapper;
import com.example.autowebgenerator.model.dto.app.AppQueryRequest;
import com.example.autowebgenerator.model.entity.App;
import com.example.autowebgenerator.model.entity.User;
import com.example.autowebgenerator.model.enums.CodeGenTypeEnum;
import com.example.autowebgenerator.model.vo.AppVO;
import com.example.autowebgenerator.model.vo.UserVO;
import com.example.autowebgenerator.service.AppService;
import com.example.autowebgenerator.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

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
        // Batch-fetch users to avoid N+1
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
        return QueryWrapper.create()
                .eq("id", request.getId())
                .like("app_name", request.getAppName())
                .like("init_prompt", request.getInitPrompt())
                .eq("code_gen_type", request.getCodeGenType())
                .eq("deploy_key", request.getDeployKey())
                .eq("priority", request.getPriority())
                .eq("user_id", request.getUserId())
                .orderBy(snakeSortBy, isAsc);
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

        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        App app = this.getById(appId);
        ExceptionUtils.throwIf(app == null, ErrorCode.NOT_FOUND, "App not found");
        boolean isOwner = app.getUserId().equals(loginUser.getId());
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        ExceptionUtils.throwIf(!isOwner && !isAdmin, ErrorCode.FORBIDDEN, "No permission");

        // Generate or reuse deploy key
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
            // Ensure uniqueness
            while (this.count(QueryWrapper.create().eq("deploy_key", deployKey)) > 0) {
                deployKey = RandomUtil.randomString(6);
            }
        }

        // Copy generated files to deploy directory
        CodeFileSaver.deployCodeForApp(appId, deployKey);

        // Persist deploy key and time
        App update = new App();
        update.setId(appId);
        update.setDeployKey(deployKey);
        update.setDeployedTime(new Date());
        this.updateById(update);

        return deployKey;
    }
}
