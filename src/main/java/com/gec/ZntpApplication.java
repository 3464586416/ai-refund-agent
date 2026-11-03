package com.gec;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gec.mapper")
public class ZntpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZntpApplication.class, args);
    }

}
