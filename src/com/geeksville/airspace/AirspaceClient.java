package com.geeksville.airspace;

import java.io.ByteArrayOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class AirspaceClient {
	final String base_url = "http://airspace.kataplop.net:8888/api/v1/";
	final HttpClient httpclient = new DefaultHttpClient();
	
	public AirspaceClient(){
//		getAirspace(new int[]{903});
	}
	
	public void getAirspace(int[] id){
		String req = base_url + "airspaces/set/" + id[0];
		
		for (int i=1; i<id.length; i++){
			req += ";" + id[i];
		}
		req += "/?format=json";
		
		try {
			HttpGet get = new HttpGet(req);
			HttpResponse response = httpclient.execute(get);
			StatusLine statusLine = response.getStatusLine();
			
			if(statusLine.getStatusCode() == HttpStatus.SC_OK){
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				String responseString = out.toString();
				//..more logic
			} else{
				//Closes the connection.
				response.getEntity().getContent().close();
				//	        throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (Exception e){

		}
	}
}
