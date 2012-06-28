package com.geeksville.weather.dummy;

import java.util.Date;

import com.geeksville.weather.Measure;

public class DummyMeasure implements Measure {

	private final Date date;
	private final float wavg, wmax, wmin, temp, humidity, ps, lumi;
	private final int wdiravg, wdir;
	
	public DummyMeasure(Date date, float wavg, float wmax, float wmin,
			int wdir, int wdiravg, float temp, float humidity, float ps, float lumi){
		this.date = date;
		this.wavg = wavg;
		this.wmax = wmax;
		this.wmin = wmin;
		this.wdir = wdir;
		this.wdiravg = wdiravg;
		this.temp = temp;
		this.humidity = humidity;
		this.ps = ps;
		this.lumi = lumi;
	}
	
	@Override
	public final Date getDate() {
		return date;
	}

	@Override
	public float getWindSpeedAvg() {
		return wavg;
	}

	@Override
	public float getWindSpeedMax() {
		return wmax;
	}

	@Override
	public float getWindSpeedMin() {
		return wmin;
	}

	@Override
	public int getWindDirectionAvg() {
		return wdiravg;
	}

	@Override
	public int getWindDirectionInst() {
		return wdir;
	}

	@Override
	public float getTemperature() {
		return temp;
	}

	@Override
	public float getHumidity() {
		return humidity;
	}

	@Override
	public float getPressure() {
		return ps;
	}

	@Override
	public float getLuminosity() {
		return lumi;
	}
}
