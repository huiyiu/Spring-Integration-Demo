package com.hyu.si;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;

@SpringBootApplication
@IntegrationComponentScan
public class SiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiApplication.class, args);
    }

}
