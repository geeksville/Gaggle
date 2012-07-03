package com.geeksville.weather;

import java.util.Date;

public interface Measure {
	public Date getDate();
	public Float getWindSpeedAvg();
	public Float getWindSpeedMax();
	public Float getWindSpeedMin();
	public Integer getWindDirectionAvg();
	public Integer getWindDirectionInst();
	public Float getTemperature();
	public Float getHumidity();
	public Float getPressure();
	public Float getLuminosity();
}
