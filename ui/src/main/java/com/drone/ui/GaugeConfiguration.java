package com.drone.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.drone.DroneProperty;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GaugeConfiguration {

    public int minValue() default 0;

    public int maxValue();

    public DroneProperty property();

    public int precision() default 2;
}
