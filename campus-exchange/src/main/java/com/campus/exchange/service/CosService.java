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

    /**
     * 上传主题文件（壁纸或预览图）
     * @param file 文件（图片或视频）
     * @param subDir 子目录（wallpapers/previews）
     * @return 文件访问URL
     */
    String uploadThemeFile(MultipartFile file, String subDir);
}
