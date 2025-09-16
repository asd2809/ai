package com.yupi.cbjaicodemother.core.parser;


import com.yupi.cbjaicodemother.exception.BusinessException;
import com.yupi.cbjaicodemother.exception.ErrorCode;
import com.yupi.cbjaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 代码解析器
 * 根据代码生成类型执行相应的解析逻辑
 */
public class CodeParseExecutor {


    private static final HtmlCodeParser htmlParser = new HtmlCodeParser();

    private static final MultiFileCodeParser codeParser = new MultiFileCodeParser();
    /// 这个是返回的参数类型不同
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum){
        return switch (codeGenTypeEnum){
            case HTML -> htmlParser.parseCode(codeContent);
            case MULTI_FILE -> codeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的代码生成类型");
        };
    }
}
