package com.MotoForge.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/public/hello")
    public String publicHello() {
        return "Public endpoint works";
    }

    @GetMapping("/private/hello")
    public String privateHello() {
        return "Private endpoint secured";
    }
}

