package com.lcs.beautybackend.controller;

import com.lcs.beautybackend.common.BaseResponse;
import com.lcs.beautybackend.common.ResultUtils;
import com.lcs.beautybackend.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@Tag(name = "文件上传", description = "文件上传相关接口")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final OssService ossService;

    /**
     * 上传头像
     */
    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = ossService.uploadFile(file, "avatar");
        return ResultUtils.success(url);
    }

    /**
     * 上传服务图片
     */
    @Operation(summary = "上传服务图片")
    @PostMapping("/service")
    public BaseResponse<String> uploadServiceImage(@RequestParam("file") MultipartFile file) {
        String url = ossService.uploadFile(file, "service");
        return ResultUtils.success(url);
    }

    /**
     * 通用图片上传
     */
    @Operation(summary = "通用图片上传")
    @PostMapping("/image")
    public BaseResponse<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", defaultValue = "common") String directory) {
        String url = ossService.uploadFile(file, directory);
        return ResultUtils.success(url);
    }
}
