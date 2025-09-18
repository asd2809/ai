package com.yupi.cbjaicodemother.service;


import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yupi.cbjaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yupi.cbjaicodemother.model.entity.ChatHistory;
import com.yupi.cbjaicodemother.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 加载历史对话到内存
     * @param appId
     * @param chatMemory
     * @param maxCount 返回的条数
     * @return 加载成功的条数
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 添加聊天记录
     * @param appId
     * @param message
     * @param messageType
     * @param userId
     * @return
     */
    boolean addCHatMessage(Long appId,String message,String messageType,Long userId);

    /**
     * 分页查询某APP的对话记录
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param loginUser
     * @return
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     * 根据应用Id 删除对话
     * @param appId
     * @return
     */
    boolean deleteByAppId(Long appId);

    /**
     * 封装条件查询器
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}
