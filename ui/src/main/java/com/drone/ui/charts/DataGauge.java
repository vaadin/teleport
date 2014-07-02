package com.drone.ui.charts;

import java.math.BigDecimal;

import com.drone.DroneProperty;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.ChartClickListener;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

public class DataGauge extends CustomField<Float> {
    private static final long serialVersionUID = -8976340317146040255L;

    private DroneProperty property;
    private int maxValue;
    private String title;
    private int precision;

    private Chart gauge;

    private ListSeries dataSeries;

    public DataGauge(DroneProperty property, int maxValue, int precision,
            String title) {
        this.property = property;
        this.maxValue = maxValue;
        this.precision = precision;
        this.title = title;
        setWidth(200, Unit.PIXELS);
        setHeight(200, Unit.PIXELS);

        dataSeries = new ListSeries();
        dataSeries.addData(0);
        gauge = setupGauge();
    }

    public void addClickListener(ChartClickListener clickListener) {
        gauge.addChartClickListener(clickListener);
    }

    private Chart setupGauge() {
        Chart batteryLevel = new Chart();
        batteryLevel.setSizeFull();

        Configuration configuration = new Configuration();

        configuration.getChart().setType(ChartType.GAUGE);
        configuration.getChart().setAlignTicks(false);
        configuration.getChart().setPlotBackgroundColor(null);
        configuration.getChart().setPlotBackgroundImage(null);
        configuration.getChart().setPlotBorderWidth(0);
        configuration.getChart().setPlotShadow(false);
        configuration.setTitle(title);

        configuration.getPane().setStartAngle(-150);
        configuration.getPane().setEndAngle(150);

        YAxis yAxis = new YAxis();
        yAxis.setMin(0);
        yAxis.setMax(maxValue);
        yAxis.setLineColor(new SolidColor("#339"));
        yAxis.setTickColor(new SolidColor("#339"));
        yAxis.setMinorTickColor(new SolidColor("#339"));
        yAxis.setOffset(-25);
        yAxis.setLineWidth(2);
        yAxis.setLabels(new Labels());
        yAxis.getLabels().setDistance(-20);
        yAxis.getLabels().setRotationPerpendicular();
        yAxis.setTickLength(5);
        yAxis.setMinorTickLength(5);
        yAxis.setEndOnTick(false);

        configuration.setSeries(dataSeries);

        configuration.addyAxis(yAxis);

        batteryLevel.drawChart(configuration);
        return batteryLevel;
    }

    /**
     * Sets the value of the gauge between [0 - 1]
     * 
     * @param value
     */
    public void setValue(float value) {
        BigDecimal bigDecimalValue = BigDecimal.valueOf(value);
        bigDecimalValue = bigDecimalValue
                .multiply(BigDecimal.valueOf(maxValue));
        bigDecimalValue = bigDecimalValue.setScale(precision,
                BigDecimal.ROUND_HALF_EVEN);

        dataSeries.updatePoint(0, bigDecimalValue);
    }

    @Override
    protected Component initContent() {
        return gauge;
    }

    @Override
    public Class<? extends Float> getType() {
        return Float.class;
    }

    public DroneProperty getProperty() {
        return property;
    }
}
