package com.geeksville.location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    
    private BufferedReader reader;
    public CSVReader(File file){
        try {
            this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public List<ExtendedLocation> toLocationList() {
        
        List<ExtendedLocation> returnValue = new ArrayList<ExtendedLocation>();
        ExtendedLocation location;
        try {
            // skip headers:
            reader.readLine();
            while((location = readLocation())!=null){
                returnValue.add(location);
            }
        }
        catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return returnValue;
    }

    public ExtendedLocation readLocation() throws ParseException, IOException {
        ExtendedLocation returnValue = null;
        String line = reader.readLine();
        if(line!=null){
            String[] values = line.split(",");
            returnValue = new ExtendedLocation();
//            returnValue.setBearing(bearing);
            double latitude = Double.parseDouble(values[1]);
            returnValue.setLatitude(latitude);
            double longitude = Double.parseDouble(values[2]);
            returnValue.setLongitude(longitude);
            double altitude = Double.parseDouble(values[3]);
            returnValue.setAltitude(altitude);
            float speed = Float.parseFloat(values[5]);
            returnValue.setSpeed(speed);
            float vertSpeed = Float.parseFloat(values[6]);
            returnValue.setVertSpeed(vertSpeed);
            long time = Long.parseLong(values[0]);
            returnValue.setTime(time);
            float bearing = Float.parseFloat(values[4]);
            returnValue.setBearing(bearing );
            float[] accel = new float[3];
            accel[0] =  Float.parseFloat(values[7]);
            accel[1] =  Float.parseFloat(values[8]);
            accel[2] =  Float.parseFloat(values[9]);
            returnValue.setAccel(accel);
        } else {
            reader.close();
        }
        return returnValue;
    }

}
