package com.lcs.beautybackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {
    /**
     * OSS服务的Endpoint
     */
    private String endpoint;

    /**
     * 区域
     */
    private String region;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * Access Key ID
     */
    private String accessKeyId;

    /**
     * Access Key Secret
     */
    private String accessKeySecret;
}
