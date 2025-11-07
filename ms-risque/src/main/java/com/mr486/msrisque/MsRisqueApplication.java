package com.mr486.msrisque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsRisqueApplication {

  public static void main(String[] args) {
    SpringApplication.run(MsRisqueApplication.class, args);
  }

}
