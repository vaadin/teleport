package com.drone.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.drone.event.property.AbstractDronePropertyEvent;

public class GaugeFinder {
    /**
     * @return available gauges from sub classes of AbstractDronePropertyEvent
     */

    @SuppressWarnings("unchecked")
    public static List<GaugeConfiguration> findAvailableGauges() {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                true);
        provider.addIncludeFilter(new AssignableTypeFilter(
                AbstractDronePropertyEvent.class));

        List<GaugeConfiguration> configurations = new ArrayList<>();

        Set<BeanDefinition> components = provider
                .findCandidateComponents("com/drone/event/property");
        for (BeanDefinition component : components) {
            try {
                Class cls = Class.forName(component.getBeanClassName());
                if (cls.isAnnotationPresent(GaugeConfiguration.class)) {
                    configurations.add((GaugeConfiguration) cls
                            .getAnnotation(GaugeConfiguration.class));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return configurations;
    }
}
