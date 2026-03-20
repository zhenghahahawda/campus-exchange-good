package com.campus.exchange.config.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/good")
public class GoodController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }
}
