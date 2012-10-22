package com.geeksville.altitude;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class GeoidCalculator {
    public static void main(String[] args) {
        String lat = "50.945";
        String lon = "2.63";
        
        GeoidCalculator geoidCalculator = new GeoidCalculator();
        
        System.out.println(":"+geoidCalculator.calculateGeoidHeight(lat, lon)+":");
    }
    
    public String calculateGeoidHeight(String lat, String lon) {
        String calculatedGeoidHeightMeters = null;
        try {
            // Construct data
            String data = encodeFormFieldUtf8("lat", lat);
            data += "&";
            data += encodeFormFieldUtf8("lon", lon);

            // Send data
            URL url = new URL("http://jules.unavco.org/Geoid/Geoid");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                if(line.contains("Geoid height =")) {
                    int indexOfGeoidHeight = line.indexOf("Geoid height =");
                    indexOfGeoidHeight+="Geoid height =".length();
                    if(indexOfGeoidHeight<line.length()) {
                        calculatedGeoidHeightMeters = line.substring(indexOfGeoidHeight).trim();
                    }
                } else {
                    continue;
                }
            }
            wr.close();
            rd.close();
        } catch (Exception e) {
            return null;
        }
        return calculatedGeoidHeightMeters;
    }

    private String encodeFormFieldUtf8(String latLabel, String lat) throws UnsupportedEncodingException {
        return URLEncoder.encode(latLabel, "UTF-8") + "=" + URLEncoder.encode(lat, "UTF-8");
    }
}
