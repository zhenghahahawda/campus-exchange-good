package org.example.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.File;

@Service
public class CosService {

    @Autowired
    private COSClient cosClient;

    @Value("${tencent.cos.bucket-name}")
    private String bucketName;

    /**
     * Upload a local file to COS
     * @param localFilePath The absolute path of the local file
     * @param key The key (path) of the file in the bucket (e.g., images/example.jpg)
     * @return The ETag of the uploaded file
     */
    public String uploadFile(String localFilePath, String key) {
        File localFile = new File(localFilePath);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        return putObjectResult.getETag();
    }

    @PreDestroy
    public void shutdown() {
        if (this.cosClient != null) {
            this.cosClient.shutdown();
        }
    }
}
