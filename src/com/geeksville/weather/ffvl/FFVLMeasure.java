package com.geeksville.weather.ffvl;

import java.util.Date;

import com.geeksville.weather.Measure;

public class FFVLMeasure implements Measure {
	private final Date date;
	private final Float wmin, wmax, wavg;
	private final Integer wdirinst, wdiravg;
	private final Float temp, hydro, press, lumi;

	public FFVLMeasure(Date date, Float wind_min, Float wind_max, Float wind_avg,
		Integer wind_dir, Integer wind_diravg,
		Float temp, Float hydro, Float press, Float lumi){
		this.date = date;
		this.wmin = wind_min;
		this.wmax = wind_max;
		this.wavg = wind_avg;
		this.wdirinst = wind_dir;
		this.wdiravg = wind_diravg;
		this.temp = temp;
		this.hydro = hydro;
		this.press = press;
		this.lumi = lumi;
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public Float getWindSpeedAvg() {
		return wavg;
	}

	@Override
	public Float getWindSpeedMax() {
		return wmax;
	}

	@Override
	public Float getWindSpeedMin() {
		return wmin;
	}

	@Override
	public Integer getWindDirectionAvg() {
		return wdiravg;
	}

	@Override
	public Integer getWindDirectionInst() {
		return wdirinst;
	}

	@Override
	public Float getTemperature() {
		return temp;
	}

	@Override
	public Float getHumidity() {
		return hydro;
	}

	@Override
	public Float getPressure() {
		return press;
	}

	@Override
	public Float getLuminosity() {
		return lumi;
	}
}
