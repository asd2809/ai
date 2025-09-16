package com.yupi.cbjaicodemother.ai.model;


import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Description("生成HTML代码文件的结果")
@Data
public class HtmlCodeResult {

    /**
     * 代码
     */
    @Description("HTML 代码")
    private String htmlCode;

    /**
     * 描述
     */
    @Description("生成代码的描述")
    private String description;
}
