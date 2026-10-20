package com.mr486.msnotes;

import com.mr486.commun.configuration.SecuriteApiConfiguration;
import com.mr486.commun.exception.GlobalExceptionHandler;
import io.mongock.runner.springboot.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableMongock
@Import({
        SecuriteApiConfiguration.class,
        GlobalExceptionHandler.class})
public class MsNotesApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsNotesApplication.class, args);
    }
}
