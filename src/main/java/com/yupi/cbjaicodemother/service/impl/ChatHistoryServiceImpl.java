package com.yupi.cbjaicodemother.service.impl;

import cn.hutool.core.util.StrUtil;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yupi.cbjaicodemother.constant.UserConstant;
import com.yupi.cbjaicodemother.exception.ErrorCode;
import com.yupi.cbjaicodemother.exception.ThrowUtils;
import com.yupi.cbjaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yupi.cbjaicodemother.model.entity.App;
import com.yupi.cbjaicodemother.model.entity.ChatHistory;
import com.yupi.cbjaicodemother.mapper.ChatHistoryMapper;
import com.yupi.cbjaicodemother.model.entity.User;
import com.yupi.cbjaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.yupi.cbjaicodemother.service.AppService;
import com.yupi.cbjaicodemother.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.mybatisflex.core.paginate.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

    @Resource
    @Lazy
    private AppService appService;

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount){
        try{
            QueryWrapper qw = QueryWrapper.create()
                    .eq("appId", appId)
                    .orderBy(ChatHistory::getAppId,false)
                    /// 从1开始加载，防止重复加载用户刚发的消息
                    .limit(1,maxCount);
            List<ChatHistory> list = this.list(qw);
            if (list.isEmpty()){
                return 0;
            }
            /// 反转列表，确保按照时间顺序(老的在前，新的在后)
            list = list.reversed();
            /// 按照时间顺序添加到记忆中
            int loadedCount = 0 ;
            /// 先清理redis中的历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory chatHistory : list) {
                if(ChatHistoryMessageTypeEnum.USER.getValue().equals(chatHistory.getMessageType())){
                    chatMemory.add(UserMessage.from(chatHistory.getMessage()));
                }else if(ChatHistoryMessageTypeEnum.AI.getValue().equals(chatHistory.getMessageType())){
                    chatMemory.add(AiMessage.from(chatHistory.getMessage()));
                }
                loadedCount++;
            }
            log.info("成功为 appId :{} 加载了 {} 条历史记录", appId, loadedCount);
            return loadedCount;
        }catch (Exception e){
            log.info("加载历史对话失败{}",e.getMessage());
            /// 加载失败不影响运行，只是没有上下文
            return 0;
        }
    }


    @Override
    public boolean addCHatMessage(Long appId, String message, String messageType, Long userId) {
        /// 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0 , ErrorCode.PARAMS_ERROR,"应用ID不能为空");
        ThrowUtils.throwIf(message == null , ErrorCode.PARAMS_ERROR,"消息不能为空");
        ThrowUtils.throwIf(messageType == null , ErrorCode.PARAMS_ERROR,"消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0 , ErrorCode.PARAMS_ERROR,"用户ID不能为空");
        /// 验证消息类型是否有效
        ChatHistoryMessageTypeEnum enumByValue = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(enumByValue == null , ErrorCode.PARAMS_ERROR,"错误的消息类型");
        /// 数据插入
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();

        return this.save(chatHistory);
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    /**
     * 根据appId查询对话历史
     * @param appId
     * @return
     */
    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0 , ErrorCode.PARAMS_ERROR);
        ///根据appId删除对话记录
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("appId", appId);
        return this.remove(queryWrapper);
    }

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        /// 可以根据前端发来的字段进行动态排序
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

}
