package com.yupi.cbjaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yupi.cbjaicodemother.constant.AppConstant;
import com.yupi.cbjaicodemother.core.AiCodeGeneratorFacade;
import com.yupi.cbjaicodemother.exception.BusinessException;
import com.yupi.cbjaicodemother.exception.ErrorCode;
import com.yupi.cbjaicodemother.exception.ThrowUtils;
import com.yupi.cbjaicodemother.model.dto.app.AppQueryRequest;
import com.yupi.cbjaicodemother.model.dto.app.AppVO;
import com.yupi.cbjaicodemother.model.entity.App;
import com.yupi.cbjaicodemother.mapper.AppMapper;
import com.yupi.cbjaicodemother.model.entity.User;
import com.yupi.cbjaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.yupi.cbjaicodemother.model.enums.CodeGenTypeEnum;
import com.yupi.cbjaicodemother.model.vo.UserVO;
import com.yupi.cbjaicodemother.service.AppService;
import com.yupi.cbjaicodemother.service.ChatHistoryService;
import com.yupi.cbjaicodemother.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }


    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        /// 1.参数校验
        ThrowUtils.throwIf(appId == null || message == null || loginUser == null,ErrorCode.PARAMS_ERROR);
        /// 2.查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null,ErrorCode.NOT_FOUND_ERROR);
        /// 3.权限校验，仅本人可以与应用对话
        if(!Objects.equals(loginUser.getId(), app.getUserId())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该用户没有权限查看该应用");
        }
        /// 4.获取应用的代码类型
        String codeGenType = app.getCodeGenType();
        ThrowUtils.throwIf(codeGenType == null,ErrorCode.PARAMS_ERROR);
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIf(codeGenTypeEnum == null,ErrorCode.PARAMS_ERROR,"不存在该代码类型");
        /// 5.在调用AI前，先保存用户消息到数据库中
        chatHistoryService.addCHatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getText(), loginUser.getId());
        /// 6.调用AI生成代码
        Flux<String> messageFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        /// 7.存入AI的消息
        StringBuilder aiResponseBuilder = new StringBuilder();
        return messageFlux.map(chunk -> {
            // 实时收集AI响应的内容
            aiResponseBuilder.append(chunk);
            return chunk;
        }).doOnComplete(() -> {
            /// 流失返回之后 保存AI消息到对话历史中
            String aiResponse = aiResponseBuilder.toString();
            chatHistoryService.addCHatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getText(), loginUser.getId());
        }).doOnError(err -> {
            /// 如果AI回复失败，也需要保存记录到数据中
            String aiError = "AI 回复失败" + err.getMessage();
            chatHistoryService.addCHatMessage(appId, aiError, ChatHistoryMessageTypeEnum.AI.getText(), loginUser.getId());
        });
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        /// 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0 ,ErrorCode.PARAMS_ERROR,"应用id错误");
        ThrowUtils.throwIf(loginUser == null,ErrorCode.NOT_LOGIN_ERROR);
        /// 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null,ErrorCode.NOT_FOUND_ERROR);
        /// 3. 权限校验
        if (!Objects.equals(loginUser.getId(), app.getUserId())){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        /// 4. 检查是否已deployKey
        ThrowUtils.throwIf(app.getDeployKey() != null,ErrorCode.OPERATION_ERROR,"已经存在了deployKey");
        /// 如果没有则生成
        String deployKey = RandomUtil.randomString(6);
        /// 5. 获取代码生成类型，获取原始代码生成路径(应用访问目录)
        String codeGenType = app.getCodeGenType();
        /// 拼接文件名
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        /// 6. 检查路径是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用代码路径不存在，请先生成应用");
        }
        /// 7. 复制文件到部署目录
        /// deployDirPath是部署文件下的绝对路径
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try{
            FileUtil.copyContent(sourceDir,new File(deployDirPath),true);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用部署失败{}"+e.getMessage());
        }
        /// 8. 更新数据库
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean result = this.updateById(updateApp);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        /// 9. 返回可访问的Url
        return String.format("%s/%s",AppConstant.CODE_DEPLOY_HOST,deployKey);
    }

    /**
     * 重写方法
     * 是为了在删除应用的同时删除对应的 历史记录
     *
     * @param id
     * @return
     */
    @Override
    public boolean removeById(Serializable id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        long appId = Long.parseLong(id.toString());
        if (appId <= 0) {
            return false;
        }
        ///先删除对应的对话历史记录
        try {
            chatHistoryService.deleteByAppId((Long) appId);
        } catch (Exception e) {
            log.error("删除对应的会话记录失败 {}", e.getMessage(), e);
        }
        /// 删除应用
        return super.removeById(appId);
    }
}
