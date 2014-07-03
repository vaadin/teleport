package com.drone;

import java.net.UnknownHostException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.vaadin.spring.boot.EnableTouchKitServlet;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableTouchKitServlet
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    DroneTemplate provideTemplate(DroneStateChangeCallback[] callback)
            throws UnknownHostException {
        return new DroneTemplate(callback);
    }

    @Bean
    @Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
    Drone provideDrone() {
        return new Drone();
    }
}
