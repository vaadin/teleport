package com.drone;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.net.UnknownHostException;

public class Main {

    @Configuration
    public static class DroneClientConfiguration {

        @Bean
        TaskExecutor taskExecutor (){
            return new SimpleAsyncTaskExecutor();
        }

        @Bean
        DroneTemplate provideDroneTemplate(TaskExecutor taskExecutor ) throws UnknownHostException {
            DroneStateChangeCallback listener = latestState -> System.out.println("the latest state is: " + latestState);
            return new DroneTemplate (  taskExecutor );
        }
    }

    public static void main(String[] args) throws Throwable {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(DroneClientConfiguration.class);
        applicationContext.start();
    }
}
