package com.hoang.air;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class AirApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(AirApplication.class)
                .web(WebApplicationType.NONE)
                .headless(false).run(args);
    }
}
