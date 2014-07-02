package com.drone;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GaugeConfiguration {

    public int minValue() default 0;

    public int maxValue();

    public DroneProperty property();

    public int precision() default 2;
}
