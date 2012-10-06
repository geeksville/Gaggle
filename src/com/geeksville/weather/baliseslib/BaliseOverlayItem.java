package com.geeksville.weather.baliseslib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.util.GeoPoint;
import org.pedro.balises.Balise;
import org.pedro.balises.Releve;
import org.osmdroid.views.overlay.OverlayItem;

import com.geeksville.gaggle.R;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class BaliseOverlayItem extends ExtendedOverlayItem {
	final public String bid;

	public BaliseOverlayItem(Releve releve, Balise balise,
			Context context) {

		super(balise.nom, "NA", new GeoPoint(balise.latitude, balise.longitude), context);
		this.bid = balise.id;
		
		if (releve != null){
			StringBuffer sb = new StringBuffer();
			sb.append("min:" + releve.ventMini + "\n" + "max:" +releve.ventMaxi + "\n");
			DateFormat df1 = new SimpleDateFormat("dd MMMM - HH:mm");

			sb.append("@" + df1.format(releve.date));

			this.setDescription(sb.toString());
		}
		// int dir = releve.directionMoyenne;
		// FIXME create a drawable whose direction matches wind direction :p
		Drawable marker = context.getResources().getDrawable(R.drawable.marker_node);
		setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
		setMarker(marker);
	}
	
	public void updateReleve(Releve releve){

		if (releve != null){
			StringBuffer sb = new StringBuffer();
			sb.append("min:" + releve.ventMini + "\n" + "max:" +releve.ventMaxi + "\n");
			DateFormat df1 = new SimpleDateFormat("dd MMMM - HH:mm");

			sb.append("@" + df1.format(releve.date));

			this.setDescription(sb.toString());
		}
//		Drawable marker = context.getResources().getDrawable(R.drawable.marker_node);
//		setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
//		setMarker(marker);
	}
}
