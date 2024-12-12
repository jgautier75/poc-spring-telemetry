package com.acme.jga.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableConfigurationProperties(value = {VaultSecrets.class})
public class PocApplication {

    public static void main(String[] args) {
        SpringApplication.run(PocApplication.class, args);
    }

}