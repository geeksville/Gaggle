package com.geeksville.weather;

import java.util.Date;

public interface Measure {
	public Date getDate();
	public float getWindSpeedAvg();
	public float getWindSpeedMax();
	public float getWindSpeedMin();
	public int getWindDirectionAvg();
	public int getWindDirectionInst();
	public float getTemperature();
	public float getHumidity();
	public float getPressure();
	public float getLuminosity();
}
