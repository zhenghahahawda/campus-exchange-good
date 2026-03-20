package com.campus.exchange;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {
        HibernateJpaAutoConfiguration.class
})
@MapperScan("com.campus.exchange.mapper")
public class CampusExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusExchangeApplication.class, args);
    }
}
