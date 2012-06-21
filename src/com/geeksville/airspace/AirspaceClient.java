package com.geeksville.airspace;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import android.util.Log;

import com.geeksville.maps.PolygonOverlay;
import com.geeksville.maps.PolygonOverlay.GeoPolygon;

public class AirspaceClient {
	final String base_url;
	final HttpClient httpclient = new DefaultHttpClient();
	final String []classes;
	final int maxfloor;
	
	public AirspaceClient(final String host, final String[] classes, final int maxfloor){
		this.base_url = host;
		this.classes = classes;
		this.maxfloor = maxfloor;
	}
	
	private ArrayList<GeoPolygon> unpackPolyFromFeatureCollection(String json){
		final ArrayList<GeoPolygon> gps = new ArrayList<GeoPolygon>();
		try {
			final JSONObject feature_collection = new JSONObject(json);
			final JSONArray features = feature_collection.getJSONArray("features");

			for (int i=0; i<features.length(); i++){
				final JSONObject feature = features.getJSONObject(i);
				final JSONArray coords = feature.getJSONObject("geometry").getJSONArray("coordinates").getJSONArray(0);
				final GeoPolygon gp = new PolygonOverlay.GeoPolygon();

				for (int j=0; j < coords.length(); j++){
					final JSONArray lon_lat = coords.getJSONArray(j);
					final double lat = lon_lat.getDouble(1);
					final double lon = lon_lat.getDouble(0);
					gp.mPoints.add(new GeoPoint(lat, lon));
				}
				gps.add(gp);
			}
		} catch (Exception e) {
			//
		}
		return gps;
	}

	public ArrayList<GeoPolygon> getAirspaces(double latN, double lonW, double latS, double lonE){
		String req = base_url + "airspaces/bbox/?format=json&q=" +
				lonW + "," + latS + "," + 
				lonE + "," + latN;
		// "?q=5.102019,44.937484,6.337981,45.421488"

		if (this.classes.length > 0){
			req+="&clazz=";
			for (int i=0; i<this.classes.length; i++){
				if (i>0) req+=",";
				req+=this.classes[i];
			}
		}
//		if (this.maxfloor != 0){
//			req += "&limit=" + maxfloor;
//		}
		
		HttpGet get = new HttpGet(req);

		try {
			HttpResponse response = httpclient.execute(get);
			StatusLine statusLine = response.getStatusLine();

			if(statusLine.getStatusCode() == HttpStatus.SC_OK){
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				String responseString = out.toString();
				return unpackPolyFromFeatureCollection(responseString);
			} else{
				//Closes the connection.
				response.getEntity().getContent().close();
				//	        throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (Exception e){
			Log.e("AirspaceC", "Something went wrong", e);
		}
		return new ArrayList<GeoPolygon>();
	}

	public ArrayList<GeoPolygon> getAirspaces(int[] id){
		String req = base_url + "airspaces/set/" + id[0];
		
		for (int i=1; i<id.length; i++){
			req += ";" + id[i];
		}
		req += "/?format=json";
		
		final HttpGet get = new HttpGet(req);

		try {
			HttpResponse response = httpclient.execute(get);
			StatusLine statusLine = response.getStatusLine();
			
			if(statusLine.getStatusCode() == HttpStatus.SC_OK){
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				String responseString = out.toString();
				return unpackPolyFromFeatureCollection(responseString);
			} else{
				//Closes the connection.
				response.getEntity().getContent().close();
				//	        throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (Exception e){
			Log.e("AirspaceC", "Something went wrong", e);
		}
		return new ArrayList<GeoPolygon>();
	}
}
