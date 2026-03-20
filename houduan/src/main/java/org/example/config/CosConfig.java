package org.example.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosConfig {

    @Value("${tencent.cos.secret-id}")
    private String secretId;

    @Value("${tencent.cos.secret-key}")
    private String secretKey;

    @Value("${tencent.cos.region}")
    private String regionName;

    @Bean
    public COSClient cosClient() {
        // Initialize user identity information (secretId, secretKey).
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);

        // Initialize client configuration
        Region region = new Region(regionName);
        ClientConfig clientConfig = new ClientConfig(region);

        // Generate cos client
        return new COSClient(cred, clientConfig);
    }
}
