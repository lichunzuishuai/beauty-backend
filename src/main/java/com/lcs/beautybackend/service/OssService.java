package com.lcs.beautybackend.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * OSS文件上传服务接口
 */
public interface OssService {
    /**
     * 上传文件
     * 
     * @param file      文件
     * @param directory 目录（如: avatar, service）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String directory);
}
