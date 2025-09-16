package com.yupi.cbjaicodemother.core.saver;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.yupi.cbjaicodemother.exception.BusinessException;
import com.yupi.cbjaicodemother.exception.ErrorCode;
import com.yupi.cbjaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象代码文件保存器 - 模板方法模式
 */
public abstract  class CodeFileSaverTemplate<T> {

    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 模板
     * 流程不能被子类复写
     * @param result
     * @return
     */
    public final File saveCode(T result){
        /// 1.验证输入
        validateInput(result);
        /// 2.构建唯一目录
        String baseDirPath = buildUniqueDir();
        /// 3.保存文件
        saveFiles(result,baseDirPath);
        /// 4.返回目录对象
        return new File(baseDirPath);
    }

    public static void writeToFile(String dirPath, String fileName, String content) {
        String filePath = dirPath + File.separator + fileName;
        /// 把内容content存入指定文件中
        FileUtil.writeString(content,filePath, StandardCharsets.UTF_8);
    }

    /**
     * 验证参数，子类可以重写
     * @param result
     */
    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"代码结果不能为空");
        }
    }

    /**
     * 返回目录路径
     * @return
     */
    protected String buildUniqueDir(){
        String bizType = getCodeType().getValue();
        /// 通过雪花算法生成唯一目录
        String  uniqueDirName = StrUtil.format("{}_{}",bizType, IdUtil.getSnowflakeNextIdStr());
        /// File.separator是用来添加路径分隔符
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        /// 一次性创建多级目录的
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 获取代码生成类型
     * @return
     */
    protected abstract CodeGenTypeEnum getCodeType();
    /**
     * 保存文件
     * @param result
     * @param baseDirPath
     */
    protected abstract void saveFiles(T result, String baseDirPath) ;

}
