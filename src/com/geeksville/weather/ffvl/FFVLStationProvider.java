//Copyright (C) 2012  Marc Poulhiès
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
package com.geeksville.weather.ffvl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
	private final HashMap<Integer, FFVLStation> hStations= new HashMap<Integer, FFVLStation>();
	
	private final String station_list_url = "http://bordel.kataplop.net/ffvl/balise_list.xml";
	private final String measure_url = "http://bordel.kataplop.net/ffvl/relevemeteo.xml";

	// For parsing balise_list.xml
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

	// For parsing relevemeteo.xml
	private final String RELEVE = "releve";
	private final String IDBALISE_R = "idbalise";
	private final String VITMOY = "vitesseVentMoy";
	private final String VITMAX = "vitesseVentMax";
	private final String VITMIN = "vitesseVentMin";
	private final String DIRMOY = "directVentMoy";
	private final String DIRINST= "directVentInst";
	private final String TEMP = "temperature";
	private final String HYDRO = "hydrometrie";
	private final String PRESS = "pression";
	private final String LUMI = "luminosite";
	private final String DATE = "date";


	protected WeatherStationsOverlay overlay;
	
	private class StationListLoader extends AsyncTask<Context, Void, ArrayList<FFVLStation>> {

		@Override
		protected ArrayList<FFVLStation> doInBackground(Context... params) {
			return getStationsList(params[0]);
		}

		@Override
		protected void onPostExecute(ArrayList<FFVLStation> fstations){
			Log.d(TAG, "post execute");
			stations.clear();
			stations.addAll(fstations);
			ArrayList<Station> buf = new ArrayList<Station>();
			buf.addAll(stations);
			overlay.removeAllItems(false);
			overlay.addItems(buf);
			for (FFVLStation s : fstations){
				hStations.put(s.id, s);
			}

			// retrieve measures
			// this could be done in // with some lazy 
			// data filling.
			new MeasureLoader().execute();
		}
	}

	private class MeasureLoader extends AsyncTask<Void, Void, Map<Integer, FFVLMeasure>> {
		@Override
		protected Map<Integer, FFVLMeasure> doInBackground(Void... arg0) {
			final Map<Integer, FFVLMeasure> ms = getMeasures();
			return ms;
		}

		@Override
		protected void onPostExecute(Map<Integer, FFVLMeasure> measures){
			for (Map.Entry<Integer, FFVLMeasure> me : measures.entrySet()){
				if (hStations.containsKey(me.getKey())){
					hStations.get(me.getKey()).setFFVLMeasure(me.getValue());
				} else {
					Log.d(TAG, "Got measure for unknown stations : " + me.getKey());
				}
			}
		}
	}

	private Map<Integer, FFVLMeasure> getMeasures(){
		final HashMap<Integer, FFVLMeasure> measures = new HashMap<Integer, FFVLMeasure>();

		try {
			final URL req = new URL(measure_url);
			final InputStream xmlin = req.openConnection().getInputStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(xmlin);
			Element root = dom.getDocumentElement();
			NodeList items = root.getElementsByTagName(RELEVE);

			for (int i = 0; i < items.getLength(); i++) {
				Integer bid = null;
				Date bdate = null;
				Float wavg = null, wmax = null, wmin = null;
				Integer wdiravg = -1, wdirinst = -1;
				Float temp = null, hydro = null, press = null, lumi = null;

				Element item = (Element) items.item(i);
				NodeList properties = item.getChildNodes();

				for (int j = 0; j < properties.getLength(); j++) {
					Element property = (Element) properties.item(j);
					String name = property.getNodeName();
					// skipping. Schema does not use attributes:
					// no children => no data
					if(! property.hasChildNodes()) continue;

					try {
						if (name.equalsIgnoreCase(IDBALISE_R)) {
							bid = new Integer(property.getFirstChild()
									.getNodeValue());
						} else if (name.equalsIgnoreCase(DATE)) {
							SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							// may be null in case of error
							bdate = format.parse(property.getFirstChild()
									.getNodeValue());
						} else if (name.equalsIgnoreCase(VITMOY)) {
							wavg = new Float(property.getFirstChild()
									.getNodeValue());
						} else if (name.equalsIgnoreCase(VITMAX)) {
							wmax = new Float(property.getFirstChild()
									.getNodeValue());
						} else if (name.equalsIgnoreCase(VITMIN)) {
							wmin = new Float(property.getFirstChild()
									.getNodeValue());
						} else if (name.equalsIgnoreCase(DIRINST)) {
							wdirinst = new Integer(property.getFirstChild()
									.getNodeValue());
						} else if (name.equalsIgnoreCase(DIRMOY)) {
							wdiravg = new Integer(property.getFirstChild()
									.getNodeValue());
						} else if (name.equalsIgnoreCase(TEMP)) {
							temp = new Float(property.getFirstChild()
									.getNodeValue());
						} else if (name.equalsIgnoreCase(HYDRO)) {
							hydro = new Float(property.getFirstChild()
									.getNodeValue());
						} else if (name.equalsIgnoreCase(PRESS)) {
							press = new Float(property.getFirstChild()
									.getNodeValue());
						} else if (name.equalsIgnoreCase(LUMI)) {
							lumi = new Float(property.getFirstChild()
									.getNodeValue());
						}
					} catch (NumberFormatException nfe) {
						// depending on the field, it may not be a problem...
						// silently ignore that
					}
				}
				if (bid != null){
					FFVLMeasure m = new FFVLMeasure(bdate, wmin, wmax, wavg,
							wdirinst, wdiravg, temp, hydro, press, lumi);
					measures.put(bid, m);
				}
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error in URL for measures", e);
		} catch (IOException e) {
			Log.e(TAG, "Error when reading from " + measure_url
					+ " for measures", e);
		} catch (Exception e) {
			Log.d(TAG, "Exception when reading XML...", e);
		}

        return measures;
	}

	private ArrayList<FFVLStation> getStationsList(Context context){
		InputStream xmlin = null;
		final ArrayList<FFVLStation> mstations = new ArrayList<FFVLStation>();

		try {
			URL req = new URL(station_list_url);
			xmlin = req.openConnection().getInputStream();
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error in URL for stations list", e);
		} catch (IOException e) {
			Log.e(TAG, "Error when reading from " + station_list_url+ " for stations list", e);
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(xmlin);
            Element root = dom.getDocumentElement();
            NodeList items = root.getElementsByTagName(BALISE);

            for (int i=0;i<items.getLength();i++){
            	int bid = -1;
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
                        bid = Integer.parseInt(property.getFirstChild().getNodeValue());
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
                FFVLStation s = new FFVLStation(bid, bname, blocation, bextra, benabled, context);
                mstations.add(s);
            }
        } catch (Exception e) {
        	Log.d(TAG, "Exception when reading XML...",e);
        }
        Log.d(TAG, "Got " + mstations.size() + " weather stations");
        return mstations;
	}

	public FFVLStationProvider(Context context){
		new StationListLoader().execute(context); // this will trigger a refesh for measure data
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
