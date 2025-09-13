package com.yupi.cbjaicodemother.controller;


import com.yupi.cbjaicodemother.common.BaseResponse;
import com.yupi.cbjaicodemother.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    public String health() {
        return "Hello World!";
    }

    @GetMapping("/dd")
    public BaseResponse<String> dd() {
        return ResultUtils.success("运行成功");
    }
}
