package com.campus.exchange.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * COS文件上传服务接口
 */
public interface CosService {

    /**
     * 上传头像
     * @param file 图片文件
     * @param userId 用户ID
     * @return 图片访问URL
     */
    String uploadAvatar(MultipartFile file, Integer userId);
}
