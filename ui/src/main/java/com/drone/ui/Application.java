package com.drone.ui;

import java.net.UnknownHostException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.vaadin.spring.boot.EnableTouchKitServlet;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBusScope;
import org.vaadin.spring.events.EventScope;

import com.drone.DroneStateChangeCallback;
import com.drone.DroneTemplate;
import com.drone.event.DroneControlUpdateEvent;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableTouchKitServlet
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args).start();
    }

    @Bean
    TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    DroneTemplate provideTemplate(TaskExecutor taskExecutor,
            DroneStateChangeCallback[] callbacks) throws UnknownHostException {
        return new DroneTemplate(taskExecutor, callbacks);
    }

    @Bean
    Drone provideDrone() {
        return new Drone();
    }

    @Aspect
    @Component
    public static class DroneControlUpdateBroadcaster {

        @Autowired
        @EventBusScope(EventScope.APPLICATION)
        private EventBus applicationEventbus;

        @Around("@annotation(com.drone.ui.BroadcastDroneCommand)")
        public Object broadcastDroneControl(ProceedingJoinPoint invocation)
                throws Throwable {
            Object proceed = invocation.proceed();
            applicationEventbus.publish(this, new DroneControlUpdateEvent());
            return proceed;
        }
    }
}
