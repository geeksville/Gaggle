package com.geeksville.gaggle.prefs;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.geeksville.gaggle.R;

public class AltitudeAdjustmentPref {
    private static float ftToMetersMultiplier = 2.8084f;

    public static float getAltitudeAdjustmentInMeters(Context c, String altSetting) {
        float altitudeAdjustmentMeters = 0.0f;

        if(altSetting.contains("f")) { // matches feet or ft
            altitudeAdjustmentMeters = parseSetting(c, altSetting, "f", ftToMetersMultiplier);
            
        } else if(altSetting.contains("m")) { // matches meters or m
            altitudeAdjustmentMeters = parseSetting(c, altSetting, "m", 1f);
            
        } else { // try assuming meters
            altitudeAdjustmentMeters = parseSetting(c, altSetting, null, 1f);
        }
        Log.d("AltitudeAdjustment", "Using alt adjustment value of " + altitudeAdjustmentMeters + "m");
        return altitudeAdjustmentMeters;
    }

    private static float parseSetting(Context c, String altSetting, String unitLabel,
            float conversionToMetersMultiplier) {
        float altitudeAdjustmentMeters = 0.0f;
        
        String altitudeAdjustmentValue = altSetting;
        if(unitLabel!=null) {
            altitudeAdjustmentValue = altSetting.substring(0, altitudeAdjustmentValue.indexOf(unitLabel));
        }
        float altitudeAdjustmentNative = parseAdjustment(c, altitudeAdjustmentValue);
        altitudeAdjustmentMeters = altitudeAdjustmentNative*conversionToMetersMultiplier;
        return altitudeAdjustmentMeters;
    }

    private static float parseAdjustment(Context c, String altSetting) {
        float altAdjustment = 0.0f;
        try {
            altAdjustment = Float.parseFloat(altSetting);
        } catch (NumberFormatException nfe) {
            if(c!=null)
                Toast.makeText(c, R.string.invalid_altitude_adjustment, Toast.LENGTH_SHORT).show();
        }
        return altAdjustment;
    }
}
