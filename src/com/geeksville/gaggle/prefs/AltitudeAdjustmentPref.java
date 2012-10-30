package com.geeksville.gaggle.prefs;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.geeksville.gaggle.R;

/**
 * Make an adjustment to the reported GPS for a specific long/lat.
 * 
 * The standard GPS model uses height above mean sea level - where sea level is assumed to be a uniform 
 * ellipse around the world. The differences between satellite calculation for long/lat combinations is
 * described by the EGM96 system, however for convenience we provide a manual adjustment here.
 * 
 * Allow users to provide adjustment in feet or meters (ft, feet, f or m, meters) and convert from ft to m.
 * If no unit is given, assume meters specified.
 * 
 * @author chanoch
 *
 */
public class AltitudeAdjustmentPref {
    private static float ftToMetersMultiplier = 2.8084f;

    /**
     * Calculate the adjustment entered by parsing the input for a unit of measure and parsing
     * the remainder for a float value (-ve or +ve). If no unit is given, assume meters.
     * 
     * @param c - used to show an error message if any problems with parsing
     * @param altSetting - the new setting to use
     * @return the parsed altitude adjustment in meters or 0.0f if parsing fails
     */
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

    /*
     * Parse the given altitude adjustment value and convert to meters if required
     * 
     * @assertion assumes that unit label is contained by altSetting if it is provided in unitLabel
     */
    private static float parseSetting(Context c, String altSetting, String unitLabel,
            float conversionToMetersMultiplier) {
        float altitudeAdjustmentMeters = 0.0f;
        
        String altitudeAdjustmentValue = altSetting;
        if(unitLabel!=null) { // if unitLabel, then the altSetting is a pure number
            altitudeAdjustmentValue = altSetting.substring(0, altitudeAdjustmentValue.indexOf(unitLabel));
        }
        float altitudeAdjustmentNative = parseAdjustment(c, altitudeAdjustmentValue);
        // convert to meters. The conversion multiplier for meters is 1 (no effect)
        altitudeAdjustmentMeters = altitudeAdjustmentNative*conversionToMetersMultiplier; 
        return altitudeAdjustmentMeters;
    }

    /*
     * Try and get a float from the value passed. Show an error message if can't be parsed.
     * 
     * return The float value contained by setting or 0 if parsing fails
     */
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
