package com.geeksville.weather.ffvl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.osmdroid.util.GeoPoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.geeksville.weather.Station;
import com.geeksville.weather.StationProviderable;
import com.geeksville.weather.overlay.WeatherStationsOverlay;

public class FFVLStationProvider implements StationProviderable {

	private static final String TAG = "FFVLStationP";

	private final ArrayList<Station> stations = new ArrayList<Station>();
	private final String http_url = "http://bordel.kataplop.net/ffvl/balise_list.xml";

	private final String BALISE = "balise";
	private final String IDBALISE = "idBalise";
	private final String NOM = "nom";
	private final String DEPT = "departement";
	private final String COORD = "coord";
	private final String P_COORD_LAT = "lat";
	private final String P_COORD_LON = "lon";
	private final String ALT = "altitude";
	private final String DESC = "description";
	private final String RQ = "remarques";
	private final String BURL = "url";
	private final String BURL_HIST = "url_histo";
	private final String ACTIVE = "active";
	private final String FORKYTE = "forKyte";

	protected WeatherStationsOverlay overlay;
	
	private class DataLoader extends AsyncTask<Context, Void, ArrayList<Station>> {

		@Override
		protected ArrayList<Station> doInBackground(Context... params) {
			return getStationsList(params[0]);
		}

		@Override
		protected void onPostExecute(ArrayList<Station> fstations){
			Log.d(TAG, "post execute");
			stations.clear();
			overlay.removeAllItems(false);
			overlay.addItems(fstations);
			
			stations.addAll(fstations);
		}
	}

	private ArrayList<Station> getStationsList(Context context){
		InputStream xmlin = null;
		final ArrayList<Station> mstations = new ArrayList<Station>();

		try {
			URL req = new URL(http_url);
			xmlin = req.openConnection().getInputStream();
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error in URL for stations list", e);
		} catch (IOException e) {
			Log.e(TAG, "Error when reading from " + http_url+ " for stations list", e);
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(xmlin);
            Element root = dom.getDocumentElement();
            NodeList items = root.getElementsByTagName(BALISE);

            for (int i=0;i<items.getLength();i++){
            	String bid = null;
            	String bname = null;
            	int balt = 0;
            	double blat=0, blon=0;
            	GeoPoint blocation = null;
            	Map<String,String> bextra = new HashMap<String,String>();
            	boolean benabled = false;
            	
            	Element item = (Element)items.item(i);
                NodeList properties = item.getChildNodes();
                
                for (int j=0;j<properties.getLength();j++){
                    Element property = (Element)properties.item(j);
                    String name = property.getNodeName();

                    if (name.equalsIgnoreCase(IDBALISE)){
                        bid = property.getFirstChild().getNodeValue();
                    } else if (name.equalsIgnoreCase(NOM)){
                    	bname = property.getFirstChild().getNodeValue();
                    } else if (name.equalsIgnoreCase(DESC)){
                    	bextra.put(DESC, property.getFirstChild().getNodeValue());
                    } else if (name.equalsIgnoreCase(DEPT)){
                    	bextra.put(DEPT, property.getAttribute("value"));
                    } else if (name.equalsIgnoreCase(RQ)){
                    	bextra.put(RQ, property.getFirstChild().getNodeValue());
                    } else if (name.equalsIgnoreCase(BURL)){
                    	bextra.put(BURL, property.getAttribute("value"));
                    } else if (name.equalsIgnoreCase(BURL_HIST)){
                    	bextra.put(BURL_HIST, property.getAttribute("value"));
                    } else if (name.equalsIgnoreCase(FORKYTE)){
                    	bextra.put(FORKYTE, property.getAttribute("value"));
                    } else if (name.equalsIgnoreCase(ALT)){
                    	balt = Integer.parseInt(property.getAttribute("value"));
                    } else if (name.equalsIgnoreCase(ACTIVE)){
                    	benabled = (1 == Integer.parseInt(property.getFirstChild().getNodeValue()));
                    } else if (name.equalsIgnoreCase(COORD)){
                    	blat = Double.parseDouble(property.getAttribute(P_COORD_LAT));
                    	blon = Double.parseDouble(property.getAttribute(P_COORD_LON));
                    }
                }

                blocation = new GeoPoint(blat, blon, balt);
                Station s = new FFVLStation(bid, bname, blocation, bextra, benabled, context);
                mstations.add(s);
            }
        } catch (Exception e) {
        	Log.d(TAG, "Exception when reading XML...",e);
        }
        Log.d(TAG, "Got " + mstations.size() + " weather stations");
        return mstations;
	}

	public FFVLStationProvider(Context context){
		new DataLoader().execute(context);
	}

	@Override
	public List<Station> getAllStations() {
		return stations;
	}

	@Override
	public List<Station> getStationsBBox(GeoPoint topleft, GeoPoint bottomright) {
		return stations;
	}

	@Override
	public WeatherStationsOverlay getOverlay() {
		return this.overlay;
	}

	@Override
	public void setOverlay(WeatherStationsOverlay overlay){
		this.overlay = overlay;
	}
}
