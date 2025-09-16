package com.yupi.cbjaicodemother.core.saver;

import com.yupi.cbjaicodemother.ai.model.MultiFileCodeResult;
import com.yupi.cbjaicodemother.model.enums.CodeGenTypeEnum;

public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath,"index.html", result.getHtmlCode());
        writeToFile(baseDirPath,"style.css", result.getCssCode());
        writeToFile(baseDirPath,"script.js", result.getJsCode());
    }

}
