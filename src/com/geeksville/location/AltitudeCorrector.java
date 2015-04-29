package com.geeksville.location;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.util.Log;
import android.util.Pair;

public class AltitudeCorrector implements PositionWriter {

	private PositionWriter writer;
	private float delta = 0.0f;
	private boolean isInited = false;
	
	private boolean	setStartAltitudeFromGoogle;
	private boolean setStartAltitudeManualy;
	private float	manualStartPointAltitude;

	public static Pair<Boolean, Double> GetAltitudeFromGoogleFor (double latitude, double longitude) {
	  String result = null;
	  Boolean success = false;
	  double altitude = 0.0f;
	  try {
		  URL url = new URL(
				  "http://maps.googleapis.com/maps/api/elevation/"
				  + "json?locations=" + String.valueOf(latitude)
				  + "," + String.valueOf(longitude)
				  + "&sensor=true");

		  HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		  InputStream in = new BufferedInputStream(connection.getInputStream());
		  BufferedReader reader = new BufferedReader(new InputStreamReader(in), 512);
		  StringBuilder builder = new StringBuilder();

		  String line = null;
		  while((line = reader.readLine()) != null)
		  {
			  builder.append(line);
		  }

		  result = builder.toString();
		  in.close();
	  } catch (MalformedURLException e)
	  {
	  } catch (IOException e)
	  {
		  Log.d( "XXX", "!!! Can not call Google.", e );
	  }

	  if(result != null)
	  {
		  try {
			  JSONObject obj = new JSONObject(result);
			  String status = obj.getString("status");
			  if(status.equals("OK"))
			  {
				  JSONArray results = obj.getJSONArray("results");
				  if(results.length() > 0)
				  {
					  JSONObject values = results.getJSONObject(0);
					  altitude = values.getDouble("elevation");
					  success = true;
				  }
			  }

		  } catch (JSONException e) {
		  }
	  }


	  return Pair.create(success, altitude);
  }
	
	public AltitudeCorrector(PositionWriter obj, boolean setStartAltitudeFromGoogle_, boolean setStartAltitudeManualy_, float startPointAltitude_) {
		writer = obj;
		setStartAltitudeFromGoogle = setStartAltitudeFromGoogle_;
		setStartAltitudeManualy = setStartAltitudeManualy_;
		manualStartPointAltitude = startPointAltitude_;
	}

	@Override
	public void emitProlog() {
		writer.emitProlog();
	}

	@Override
	public void emitPosition(long time, double latitude, double longitude,
			float altitude, int bearing, float groundSpeed, float[] accel,
			float vspd) {
		
		if (!isInited) {
			isInited = true;
			if (setStartAltitudeFromGoogle) {
				Pair<Boolean, Double> result = GetAltitudeFromGoogleFor (latitude, longitude);
				if (result.first) {
					delta = (float) (result.second - altitude);
					Log.d( "XXX", "Altitude corrected from Gooogle. Altitude = " + result.second);
					setStartAltitudeManualy = false;
				} else {
					Log.d( "XXX", "!!! Can not correct altitude. " );
				}
			}
			
			if (setStartAltitudeManualy){
				delta = manualStartPointAltitude - altitude;
				Log.d( "XXX", "Altitude corrected manualy. Altitude = " + manualStartPointAltitude);
			}

			Log.d( "XXX", "Altitude delta is:  " + delta);
		}
		
		writer.emitPosition(time, latitude, longitude, altitude + delta, bearing, groundSpeed, accel, vspd);
	}

	@Override
	public void emitEpilog() {
		writer.emitEpilog();
	}

}
