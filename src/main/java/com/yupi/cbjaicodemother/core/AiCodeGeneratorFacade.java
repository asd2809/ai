package com.yupi.cbjaicodemother.core;


import com.yupi.cbjaicodemother.ai.AiCodeGeneratorService;
import com.yupi.cbjaicodemother.ai.AiCodeGeneratorServiceFactory;
import com.yupi.cbjaicodemother.ai.model.HtmlCodeResult;
import com.yupi.cbjaicodemother.ai.model.MultiFileCodeResult;
import com.yupi.cbjaicodemother.core.parser.CodeParseExecutor;
import com.yupi.cbjaicodemother.core.saver.CodeFileSaverExecutor;
import com.yupi.cbjaicodemother.exception.BusinessException;
import com.yupi.cbjaicodemother.exception.ErrorCode;
import com.yupi.cbjaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成门面类，组合代码生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {


    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;
    /**
     * 非流式输出
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId){
        if(codeGenTypeEnum == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum){
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield  CodeFileSaverExecutor.executeSaver(htmlCodeResult,codeGenTypeEnum,appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult= aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield  CodeFileSaverExecutor.executeSaver(multiFileCodeResult,codeGenTypeEnum,appId);

            }
            default -> {
                String errorMessage = "不支持的生成系统" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        };
    }
    /**
     * 流式输出
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId){
        if(codeGenTypeEnum == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum){
            case HTML -> {
                Flux<String> stringFlux = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(stringFlux, codeGenTypeEnum,appId);
            }
            case MULTI_FILE -> {
                Flux<String> stringFlux = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(stringFlux, codeGenTypeEnum,appId);
            }
            default -> {
                String errorMessage = "不支持的生成系统" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        };
    }

    /**
     *
     * @param codeStream
     * @param codeGenTypeEnum
     * @return
     */
    private Flux<String> processCodeStream(Flux<String> codeStream,CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        /// 字符串拼接 用于当流式返回所有的代码之后可保存代码
        StringBuilder codeBuilder = new StringBuilder();
        /// 每个元素发出时
        return codeStream.doOnNext(chun -> {
            /// 实时收集代码片段
            codeBuilder.append(chun);
            /// 正常完成时
        }).doOnComplete(() -> {
            try {
                /// 流式完成之后返回代码
                String completeCode = codeBuilder.toString();
                /// 解析代码为对象
                Object htmlCodeResult = CodeParseExecutor.executeParser(completeCode,codeGenTypeEnum);
                /// 保存代码到文件
                File file = CodeFileSaverExecutor.executeSaver(htmlCodeResult,codeGenTypeEnum,appId);
                log.info("多文件目录创建完成，目录为:{}" , file.getAbsolutePath());
            }catch(Exception e){
                log.error("多文件目录创建失败：{}", e.getMessage());
            }
        });
    }
//    /**
////     * 非流式输出处理html文件
////     * @param userMessage
////     * @return
////     */
////    private File generateAndSaveMultiFileCode(String userMessage) {
////        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
////        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
////    }
////    /**
////     * 非流式输出处理多文件
////     * @param userMessage
////     * @return
////     */
////    private File generateAndSaveHtmlCode(String userMessage) {
////        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
////        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
////    }
//    /**
//     * 流式输出处理html文件
//     *
//     * @param userMessage
//     * @return
//     */
//    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
//        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
//        /// 字符串拼接 用于当流式返回所有的代码之后可保存代码
//        StringBuilder codeBuilder = new StringBuilder();
//        /// 每个元素发出时
//        return result.doOnNext(chun -> {
//            /// 实时收集代码片段
//            codeBuilder.append(chun);
//            /// 正常完成时
//        }).doOnComplete(() -> {
//            try {
//                /// 流式完成之后返回代码
//                String completeCode = codeBuilder.toString();
//                /// 解析代码为对象
//                HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeCode);
//                /// 保存代码到文件
//                File file = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
//                log.info("多文件目录创建完成，目录为:{}" , file.getAbsolutePath());
//            }catch(Exception e){
//                log.error("保存失败：{}", e.getMessage());
//            }
//        });
//    }
//
//    /**
//     * 流式输出处理多文件
//     * @param userMessage
//     * @return
//     */
//    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
//        Flux<String> stringFlux = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
//        StringBuilder codeBuilder = new StringBuilder();
//        return stringFlux.doOnNext(chun ->{
//            codeBuilder.append(chun);
//        }).doOnComplete(()->{
//            try{
//                String completeCode = codeBuilder.toString();
//                /// 解析代码为对象
//                MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeCode);
//                /// 保存代码文件
//                File file = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
//                log.info("多文件目录创建完成:{}" , file.getAbsolutePath());
//            }catch (Exception e){
//                log.error("错误消息:{}", e.getMessage());
//            }
//
//        });
//    }
//


}
