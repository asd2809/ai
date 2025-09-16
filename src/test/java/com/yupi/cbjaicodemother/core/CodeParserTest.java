package com.yupi.cbjaicodemother.core;

import com.yupi.cbjaicodemother.ai.model.HtmlCodeResult;
import com.yupi.cbjaicodemother.ai.model.MultiFileCodeResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeParserTest {

    @Test
    void parseHtmlCode() {
        String codeContent = """
                随便写一段描述：
                html 格式
                <!DOCTYPE html>
                <html>
                <head>
                    <title>测试页面</title>
                </head>
                <body>
                    <h1>Hello World!</h1>
                </body>
                </html>

                随便写一段描述
                """;
        HtmlCodeResult result = CodeParser.parseHtmlCode(codeContent);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
    }

    @Test
    void testParseMultiFileCode() {
        String codeContent = """
                这里有一些描述性文字，不重要。

                ```html
                <html>
                <head><title>测试</title></head>
                <body><h1>Hello World</h1></body>
                </html>
                ```

                ```css
                h1 { color: red; }
                ```

                ```js
                alert("Hello from JS");
                ```
                """;

        MultiFileCodeResult result = CodeParser.parseMultiFileCode(codeContent);

        // 验证不为空
        assertNotNull(result);

        // 验证 HTML 提取
        assertNotNull(result.getHtmlCode());
        assertTrue(result.getHtmlCode().contains("<h1>Hello World</h1>"));

        // 验证 CSS 提取
        assertNotNull(result.getCssCode());
        assertTrue(result.getCssCode().contains("h1 { color: red; }"));

        // 验证 JS 提取
        assertNotNull(result.getJsCode());
        assertTrue(result.getJsCode().contains("alert("));
    }
}
