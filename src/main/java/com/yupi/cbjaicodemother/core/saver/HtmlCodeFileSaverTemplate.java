package com.yupi.cbjaicodemother.core.saver;

import com.yupi.cbjaicodemother.ai.model.HtmlCodeResult;
import com.yupi.cbjaicodemother.model.enums.CodeGenTypeEnum;

public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        /// 把保存的方法放在模板类中
        writeToFile(baseDirPath, "index.html",result.getHtmlCode());
    }
}
