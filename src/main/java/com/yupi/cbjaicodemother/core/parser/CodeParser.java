package com.yupi.cbjaicodemother.core.parser;


/**
 * 代码解析器策略接口
 */
public interface CodeParser<T>{

    /**
     * 解释代码内容
     * @param codeContent
     * @return
     */
    T parseCode(String codeContent);
}
