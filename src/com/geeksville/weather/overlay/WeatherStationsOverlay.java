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

package com.geeksville.weather.overlay;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.views.MapView;
import org.pedro.balises.Balise;
import org.pedro.balises.Releve;

import com.geeksville.weather.baliseslib.BaliseOverlayItem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class WeatherStationsOverlay extends ItemizedOverlayWithBubble<BaliseOverlayItem> {
	private final MapView mapView;
	
	private MobiBalisesBroadcastReceiver receiver = new MobiBalisesBroadcastReceiver();

	/**
	 *
	 * @author Pedro M <pedro.pub@free.fr>
	 * @author Marc Poulhiès <dkm@kataplop.net>
	 */
	private class MobiBalisesBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(final Context context, final Intent intent) {

			final String action = intent.getAction();
			boolean refresh = false;

			if ("mobibalises.balisesUpdate".equals(action)) {
				final Object[] balises = (Object[])intent.getSerializableExtra("balises");

				WeatherStationsOverlay.this.removeAllItems();
				if (balises != null){
					for (Object bo : balises){
						final Balise b = (Balise)bo;

						BaliseOverlayItem boi = new BaliseOverlayItem(null, b, context);
						WeatherStationsOverlay.this.addItem(boi);
					}
					refresh = true;
				}
			} else if ("mobibalises.relevesUpdate".equals(action)) {
				final Object[] releves = (Object[])intent.getSerializableExtra("releves");
				if (releves != null) {
					for (Object ro : releves){
						final Releve r = (Releve) ro;
						for (BaliseOverlayItem b : WeatherStationsOverlay.this.mItemsList){
							if (r.id.equals(b.bid)){
								b.updateReleve(r);
							}
						}
					}
				}
			}
			if (refresh){
				WeatherStationsOverlay.this.mapView.invalidate();
			}
		}
	}

	public WeatherStationsOverlay(
			Context pContext, MapView mapView) {
		super(pContext, new ArrayList<BaliseOverlayItem>(), mapView);
		this.mapView = mapView;

	    final IntentFilter filter = new IntentFilter("mobibalises.relevesUpdate");
	    filter.addAction("mobibalises.balisesUpdate");
	    pContext.registerReceiver(receiver, filter);

	    // Ask Mobibalise to start sending updates
	    final Intent intent = new Intent("mobibalises.start");
	    intent.putExtra("client", "gaggle");
	    pContext.sendOrderedBroadcast(intent, null);
	}

	public void tearDown(Context pContext){
		pContext.unregisterReceiver(receiver);

		// Ask Mobibalise to stop sending updates 
	    final Intent intent = new Intent("mobibalises.stop");
	    intent.putExtra("client", "gaggle");
	    pContext.sendOrderedBroadcast(intent, null);
	}

	@Override
	public boolean addItems(List<BaliseOverlayItem> items){
		final boolean r = super.addItems(items);
		// FIXME this should not be needed, as populate() called
		// in and by ItemizedIconOverlay should do the job.
		this.mapView.invalidate();
		return r;
	}
}
