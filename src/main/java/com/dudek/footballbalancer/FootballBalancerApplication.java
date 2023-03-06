package com.dudek.footballbalancer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FootballBalancerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FootballBalancerApplication.class, args);
    }

}
