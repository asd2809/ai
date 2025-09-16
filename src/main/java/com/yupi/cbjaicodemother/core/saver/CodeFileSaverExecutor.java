package com.yupi.cbjaicodemother.core.saver;

import com.jfinal.template.stat.ast.Switch;
import com.yupi.cbjaicodemother.ai.model.HtmlCodeResult;
import com.yupi.cbjaicodemother.ai.model.MultiFileCodeResult;
import com.yupi.cbjaicodemother.core.parser.HtmlCodeParser;
import com.yupi.cbjaicodemother.exception.BusinessException;
import com.yupi.cbjaicodemother.exception.ErrorCode;
import com.yupi.cbjaicodemother.model.enums.CodeGenTypeEnum;

import javax.swing.text.html.HTML;
import java.io.File;

/**
 * 代码文件保存执行器
 * 根据代码生成类型执行相应的保存逻辑
 */
public class CodeFileSaverExecutor {

    private static final HtmlCodeFileSaverTemplate HTML_CODE_FILE_SAVER_TEMPLATE = new HtmlCodeFileSaverTemplate();

    private static final MultiFileCodeFileSaverTemplate MULTI_FILE_CODE_FILE_SAVER_TEMPLATE = new MultiFileCodeFileSaverTemplate();
    /// 这个是输入的参数类型不同
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenTypeEnum) {
        return switch(codeGenTypeEnum){
            case HTML -> HTML_CODE_FILE_SAVER_TEMPLATE.saveCode((HtmlCodeResult) codeResult);
            case MULTI_FILE -> MULTI_FILE_CODE_FILE_SAVER_TEMPLATE.saveCode((MultiFileCodeResult) codeResult);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR,"没有找到匹配的代码文件存储类型 ");
        };


    }

}
