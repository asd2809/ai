package com.yupi.cbjaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yupi.cbjaicodemother.model.dto.app.AppQueryRequest;
import com.yupi.cbjaicodemother.model.dto.app.AppVO;
import com.yupi.cbjaicodemother.model.entity.App;
import com.yupi.cbjaicodemother.model.entity.User;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 */
public interface AppService extends IService<App> {

    /**
     * 封装方法
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);


    /**
     * 构建查询条件器
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 聊天生成代码
     * @param appId
     * @param message
     * @param loginUser
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);
}
