package com.pocketwriter.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        // Print the working directory at startup
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        SpringApplication.run(BackendApplication.class, args);
    }

}
