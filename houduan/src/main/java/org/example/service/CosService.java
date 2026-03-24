package org.example.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class CosService {

    @Autowired
    private COSClient cosClient;

    @Value("${tencent.cos.bucket-name}")
    private String bucketName;

    @Value("${tencent.cos.region}")
    private String region;

    public String uploadFile(String localFilePath, String key) {
        File localFile = new File(localFilePath);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        return putObjectResult.getETag();
    }

    public String uploadThemeFile(MultipartFile file, String folder) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            String key = "themes/" + folder + "/" + filename;

            File tempFile = File.createTempFile("upload-", extension);
            file.transferTo(tempFile);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, tempFile);
            cosClient.putObject(putObjectRequest);
            tempFile.delete();

            return "https://" + bucketName + ".cos." + region + ".myqcloud.com/" + key;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (this.cosClient != null) {
            this.cosClient.shutdown();
        }
    }
}
