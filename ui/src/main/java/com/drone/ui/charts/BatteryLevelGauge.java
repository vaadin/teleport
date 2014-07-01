package com.drone.ui.charts;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

public class BatteryLevelGauge extends CustomField<Double> {
	private static final long serialVersionUID = -8976340317146040255L;
	private Chart batterlyLevelChart;

	private static final int MAX_VALUE = 100;

	private ListSeries dataSeries;

	public BatteryLevelGauge() {
		dataSeries = new ListSeries();
		dataSeries.addData(0);
		batterlyLevelChart = setupBatteryLevelChart();
	}

	private Chart setupBatteryLevelChart() {
		Chart batteryLevel = new Chart();

		Configuration configuration = new Configuration();
		configuration.getChart().setType(ChartType.GAUGE);
		configuration.getChart().setAlignTicks(false);
		configuration.getChart().setPlotBackgroundColor(null);
		configuration.getChart().setPlotBackgroundImage(null);
		configuration.getChart().setPlotBorderWidth(0);
		configuration.getChart().setPlotShadow(false);
		configuration.setTitle("Battery level");

		configuration.getPane().setStartAngle(-150);
		configuration.getPane().setEndAngle(150);

		YAxis yAxis = new YAxis();
		yAxis.setMin(0);
		yAxis.setMax(MAX_VALUE);
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

	@Override
	protected Component initContent() {
		return batterlyLevelChart;
	}

	@Override
	public Class<? extends Double> getType() {
		return Double.class;
	}

	@Override
	protected void setInternalValue(Double newValue) {
		dataSeries.updatePoint(0, newValue);
	}
}
