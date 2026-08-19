package com.example.docker.hello_docker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello_docker")
    public String helloDocker() {
        System.out.println("=================hello docker=================");
        return "hello docker...";
    }

}
