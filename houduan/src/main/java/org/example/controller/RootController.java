package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String index() {
        return "<h1>校园换物平台后端API服务</h1>" +
               "<p>服务正在运行中...</p>" +
               "<p>API文档参考: <a href='/api/goods'>/api/goods</a></p>";
    }
}
