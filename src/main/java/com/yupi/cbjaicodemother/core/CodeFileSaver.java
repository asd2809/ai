package com.yupi.cbjaicodemother.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.yupi.cbjaicodemother.ai.model.HtmlCodeResult;
import com.yupi.cbjaicodemother.ai.model.MultiFileCodeResult;
import com.yupi.cbjaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 文件保存器
 */
public class CodeFileSaver {

    /**
     *
     *1.文件保存目录
     *用来存放“你准备把生成的代码文件保存到哪个文件夹”的绝对路径字符串。
     */
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 2.保存 HTML 网页代码
     * @param htmlCodeResult
     * @return
     */
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeToFile(baseDirPath,"index.html",htmlCodeResult.getHtmlCode());
        return new File(baseDirPath);
    }

    /**
     * 3.保存多文件代码
     * @param multiFileCodeResult
     * @return
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(baseDirPath,"index.html",multiFileCodeResult.getHtmlCode());
        writeToFile(baseDirPath,"style.css",multiFileCodeResult.getCssCode());
        writeToFile(baseDirPath,"script.js",multiFileCodeResult.getJsCode());
        return new File(baseDirPath);
    }

    /**
     * 4.构建文件的唯一目录：tmp/code_output/bizType_雪花ID
     * @param bizType
     * @return
     */
    private static String buildUniqueDir(String bizType){
        /// 通过雪花算法生成唯一目录
        String  uniqueDirName = StrUtil.format("{}_{}",bizType, IdUtil.getSnowflakeNextIdStr());
        /// File.separator是用来添加路径分隔符
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        /// 一次性创建多级目录的
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 5.保存单个文件
     * @param dirPath
     * @param fileName
     * @param content
     */
    public static void writeToFile(String dirPath, String fileName, String content) {
        String filePath = dirPath + File.separator + fileName;
        /// 把内容content存入指定文件中
        FileUtil.writeString(content,filePath, StandardCharsets.UTF_8);
    }



}
