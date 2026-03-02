package com.example.autowebgenerator.controller;

import com.example.autowebgenerator.common.ApiResponse;
import com.example.autowebgenerator.common.ApiResponseUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    public ApiResponse<String> healthCheck() {
        return ApiResponseUtils.success("ok");
    }
}
