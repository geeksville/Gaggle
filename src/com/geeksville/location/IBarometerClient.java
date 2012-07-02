package com.geeksville.location;

import java.util.Observer;

import android.location.Location;

public interface IBarometerClient {

	/** Cheezy trick to apply preferences changes immediately on view change */
	public abstract void addObserver(Observer observer);

	public abstract void deleteObserver(Observer observer);

	// / Given a GPS based altitude, reverse engineer what the correct reference
	// pressure is
	public abstract void setAltitude(float meters);

  // / Return altitude in meters
  public abstract float getAltitude();

  // / Return pressure in hectoPascal
  public abstract float getPressure();

  // / Return battery charging status in Volt
  public abstract float getBattery();

  // / Return battery charging status in Percent
  public abstract float getBatteryPercent();

  // / Return a status messsage
  public abstract String getStatus();

	// / In m/s
	public abstract float getVerticalSpeed();

	// / If we've been calibrated, override the GPS provided altitude with our
	// aro based alt
	public void improveLocation(Location l);

}