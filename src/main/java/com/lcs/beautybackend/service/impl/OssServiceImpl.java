package com.lcs.beautybackend.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.lcs.beautybackend.config.OssConfig;
import com.lcs.beautybackend.exception.BusinessException;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * OSS文件上传服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final OssConfig ossConfig;

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        // 校验文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }

        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!".jpg".equals(suffix) && !".jpeg".equals(suffix) && !".png".equals(suffix) && !".gif".equals(suffix)
                && !".webp".equals(suffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅支持jpg、jpeg、png、gif、webp格式的图片");
        }

        // 校验文件大小（最大5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小不能超过5MB");
        }

        // 生成唯一文件名
        String fileName = directory + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;

        OSS ossClient = null;
        try {
            // 创建OSSClient实例
            DefaultCredentialProvider credentialsProvider = CredentialsProviderFactory.newDefaultCredentialProvider(
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret());
            ossClient = new OSSClientBuilder().build(ossConfig.getEndpoint(), credentialsProvider);

            // 上传文件
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(ossConfig.getBucketName(), fileName, inputStream);

            // 返回文件访问URL
            String url = "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint().replace("https://", "")
                    + "/" + fileName;
            log.info("文件上传成功: {}", url);
            return url;

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
