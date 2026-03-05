package com.example.autowebgenerator.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.example.autowebgenerator.constant.UserConstant;
import com.example.autowebgenerator.exception.ErrorCode;
import com.example.autowebgenerator.exception.ExceptionUtils;
import com.example.autowebgenerator.mapper.ChatHistoryMapper;
import com.example.autowebgenerator.model.entity.App;
import com.example.autowebgenerator.model.entity.ChatHistory;
import com.example.autowebgenerator.model.entity.User;
import com.example.autowebgenerator.model.enums.MessageTypeEnum;
import com.example.autowebgenerator.service.AppService;
import com.example.autowebgenerator.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Lazy
    @Resource
    private AppService appService;

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ExceptionUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_REQUEST, "App ID cannot be blank");
        ExceptionUtils.throwIf(StrUtil.isBlank(message), ErrorCode.BAD_REQUEST, "Message cannot be blank");
        ExceptionUtils.throwIf(MessageTypeEnum.fromValue(messageType) == null, ErrorCode.BAD_REQUEST, "Invalid message type");

        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setAppId(appId);
        chatHistory.setMessage(message);
        chatHistory.setMessageType(messageType);
        chatHistory.setUserId(userId);
        chatHistory.setCreateTime(new Date());
        chatHistory.setUpdateTime(new Date());
        return this.save(chatHistory);
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        if (appId == null || appId <= 0) return false;
        QueryWrapper queryWrapper = QueryWrapper.create().eq("app_id", appId);
        return this.remove(queryWrapper);
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, Long lastCreateTime, User loginUser) {
        ExceptionUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_REQUEST, "App ID cannot be blank");
        ExceptionUtils.throwIf(loginUser == null, ErrorCode.UNAUTHORIZED);
        ExceptionUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.BAD_REQUEST, "Page size must be between 1 and 50");

        App app = appService.getById(appId);
        ExceptionUtils.throwIf(app == null, ErrorCode.NOT_FOUND, "App not found");

        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ExceptionUtils.throwIf(!isAdmin && !isCreator, ErrorCode.FORBIDDEN, "No permission to view this app's chat history");

        QueryWrapper qw = QueryWrapper.create()
                .eq("app_id", appId)
                .orderBy("create_time", false);  // DESC: newest first

        if (lastCreateTime != null) {
            qw.lt("create_time", new Date(lastCreateTime));
        }

        return this.page(Page.of(1, pageSize), qw);
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // Offset 1 to skip the most recent user message:
            // LangChain4j will add the current user message automatically when generating,
            // so loading it here would cause duplication.
            QueryWrapper qw = QueryWrapper.create()
                    .eq("app_id", appId)
                    .orderBy("create_time", false)  // DESC: newest first
                    .limit(1, maxCount);             // OFFSET 1, LIMIT maxCount
            List<ChatHistory> historyList = this.list(qw);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // Reverse to chronological order (oldest first) before loading into memory
            Collections.reverse(historyList);
            chatMemory.clear();
            int loadedCount = 0;
            for (ChatHistory history : historyList) {
                if (MessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (MessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }
            log.info("Loaded {} history messages into memory for appId: {}", loadedCount, appId);
            return loadedCount;
        } catch (Exception e) {
            log.error("Failed to load chat history into memory for appId: {}, error: {}", appId, e.getMessage(), e);
            return 0;
        }
    }
}
