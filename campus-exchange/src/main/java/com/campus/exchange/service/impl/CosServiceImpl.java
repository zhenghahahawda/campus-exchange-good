package com.campus.exchange.service.impl;

import com.campus.exchange.config.CosConfig;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.service.CosService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 腾讯云COS文件上传服务实现
 */
@Service
public class CosServiceImpl implements CosService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Autowired
    private COSClient cosClient;

    @Autowired
    private CosConfig cosConfig;

    @Override
    public String uploadAvatar(MultipartFile file, Integer userId) {
        validateImageFile(file);
        String key = "avatar/" + userId + "/" + generateFileName(file);
        return uploadToCos(file, key);
    }

    /**
     * 上传商品图片
     */
    public String uploadGoodsImage(MultipartFile file) {
        validateImageFile(file);
        String key = "goods/" + generateFileName(file);
        return uploadToCos(file, key);
    }

    /**
     * 校验图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("图片大小不能超过5MB");
        }
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString().replace("-", "") + ext;
    }

    /**
     * 上传文件到COS
     */
    private String uploadToCos(MultipartFile file, String key) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest putRequest = new PutObjectRequest(
                    cosConfig.getBucketName(), key, file.getInputStream(), metadata);
            cosClient.putObject(putRequest);

            return cosConfig.getBaseUrl() + "/" + key;
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
}
