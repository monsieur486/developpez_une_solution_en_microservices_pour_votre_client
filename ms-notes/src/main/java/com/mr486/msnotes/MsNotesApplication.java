package com.mr486.msnotes;

import io.mongock.runner.springboot.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMongock
public class MsNotesApplication {

  public static void main(String[] args) {
    SpringApplication.run(MsNotesApplication.class, args);
  }

}
