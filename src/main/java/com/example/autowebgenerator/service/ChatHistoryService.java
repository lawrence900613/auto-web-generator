package com.example.autowebgenerator.service;

import com.example.autowebgenerator.model.entity.ChatHistory;
import com.example.autowebgenerator.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

public interface ChatHistoryService extends IService<ChatHistory> {

    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    boolean deleteByAppId(Long appId);

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, Long lastCreateTime, User loginUser);

    /**
     * Load recent DB chat history into a MessageWindowChatMemory.
     * Starts from offset 1 (skips the newest user message) to avoid duplication —
     * LangChain4j adds the current user message automatically when generating.
     *
     * @return number of messages loaded
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
