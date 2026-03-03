package com.example.autowebgenerator.service;

import com.example.autowebgenerator.model.dto.app.AppQueryRequest;
import com.example.autowebgenerator.model.entity.App;
import com.example.autowebgenerator.model.entity.User;
import com.example.autowebgenerator.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AppService extends IService<App> {

    AppVO getAppVO(App app);

    List<AppVO> getAppVOList(List<App> appList);

    QueryWrapper getQueryWrapper(AppQueryRequest request);

    /** Stream AI code generation for an app (owner only). */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /** Deploy an app — copy generated files to the deploy directory. */
    String deployApp(Long appId, User loginUser);
}
