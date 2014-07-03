package com.drone;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.UnknownHostException;

public class Main {

    @Configuration
    public static class DroneClientConfiguration {

        @Bean
        DroneTemplate provideDroneTemplate() throws UnknownHostException {
            DroneStateChangeCallback listener =
                    latestState -> System.out.println("the latest state is: " + latestState);
            return new DroneTemplate(listener);
        }
    }

    public static void main(String[] args) throws Throwable {
        ApplicationContext applicationContext =
                new AnnotationConfigApplicationContext(DroneClientConfiguration.class);
    }
}
