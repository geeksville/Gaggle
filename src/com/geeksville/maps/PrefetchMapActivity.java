package com.geeksville.maps;

import java.io.File;
import java.io.InputStream;

import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

import com.geeksville.gaggle.R;
import com.geeksville.location.LocationUtils;

public class PrefetchMapActivity extends Activity {

	private static final String EXTRA_LATITUDE = "latitude";
	private static final String EXTRA_LONGITUDE = "longitude";
	private static final String EXTRA_ZOOMLEVEL = "zoom";
	private static final String EXTRA_TILESOURCE = "tiles";

	private static final String TAG = "PrefetchMapActivity";

	private GeoPoint center;
	private int zoomLevel;
	private String source;
	private double distMeters = 32186 / 4; // 5 miles for initial testing

	private int bytesPerTile = 2591; // FIXME - generate this better

	private View editOptions;
	private Button startButton, clearButton, cancelButton;
	private ProgressBar progress;

	private AsyncTask<Void, Integer, Void> background;

	private ITileSource renderer;

	/*
	 * The various tile counts generated from our current zoom level by
	 * prepareTileCounts()
	 */
	private int tileStartX;
	private int tileStopX;
	private int tileStartY;
	private int tileStopY;
	private int numTilesX;
	private int numTilesY;
	private int numTiles;
	private int numTilesFullWidth;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prefetch_map);

		Bundle extras = getIntent().getExtras();
		center = new GeoPoint(extras.getInt(EXTRA_LATITUDE), extras.getInt(EXTRA_LONGITUDE));
		zoomLevel = extras.getInt(EXTRA_ZOOMLEVEL);
		source = extras.getString(EXTRA_TILESOURCE);
		renderer = TileSourceFactory.getTileSource(source);

		startButton = (Button) findViewById(R.id.start);
		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				background = new DownloadTilesTask().execute();
			}
		});

		clearButton = (Button) findViewById(R.id.clear);
		clearButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				background = new ClearCacheTask().execute();
			}
		});

		editOptions = findViewById(R.id.editoptions);

		progress = (ProgressBar) findViewById(R.id.progress);
		progress.setVisibility(View.GONE);

		cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setVisibility(View.GONE);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				background.cancel(false);
			}
		});
	}

	public static Intent createIntent(Activity context, GeoPoint center, int zoomLevel,
			String tilesource) {
		Intent i = new Intent(context, PrefetchMapActivity.class);

		i.putExtra(EXTRA_LATITUDE, center.getLatitudeE6());
		i.putExtra(EXTRA_LONGITUDE, center.getLongitudeE6());
		i.putExtra(EXTRA_ZOOMLEVEL, zoomLevel);
		i.putExtra(EXTRA_TILESOURCE, tilesource);

		return i;
	}

	private void prepareTileCounts(int zoom) {
		numTilesFullWidth = 1 << zoom; // The total # of tiles in X
		// & Y for the world

		double[] nwPoint = LocationUtils.addDistance(center.getLatitudeE6() * 1e-6, center
				.getLongitudeE6() * 1e-6, distMeters, Math.toRadians(45));
		double[] sePoint = LocationUtils.addDistance(center.getLatitudeE6() * 1e-6, center
				.getLongitudeE6() * 1e-6, distMeters, Math.toRadians(180 + 45));

		MapTile start = getMapTileFromCoordinates(zoom, nwPoint[0],
				nwPoint[1]);
		MapTile stop = getMapTileFromCoordinates(zoom, sePoint[0],
				sePoint[1]);

		tileStartX = start.getX();
		tileStopX = (stop.getX() + 1) % numTilesFullWidth;
		tileStartY = start.getY();
		tileStopY = (stop.getY() + 1) % numTilesFullWidth;

		numTilesX = (tileStopX - tileStartX)
				+ ((tileStopX <= tileStartX) ? numTilesFullWidth : 0);
		numTilesY = (tileStopY - tileStartY)
				+ ((tileStopY <= tileStartY) ? numTilesFullWidth : 0);
		numTiles = numTilesX * numTilesY;
	}

	/**
	 * Count all files in a directory
	 * 
	 * @param dir
	 */
	private static int countAll(File dir) {

		int count = 1;

		if (dir.isDirectory()) {
			File[] children = dir.listFiles();

			for (File f : children)
				count += countAll(f);
		}
		return count;
	}

	/**
	 * For a description see:
	 * 
	 * @see http://wiki.openstreetmap.org/index.php/Slippy_map_tilenames For a
	 *      code-description see:
	 * @see http://wiki.openstreetmap.org/index.php/Slippy_map_tilenames#
	 *      compute_bounding_box_for_tile_number
	 * @param aLat
	 *            latitude to get the {@link OSMTileInfo} for.
	 * @param aLon
	 *            longitude to get the {@link OSMTileInfo} for.
	 * @return The {@link OSMTileInfo} providing 'x' 'y' and 'z'(oom) for the
	 *         coordinates passed.
	 */
	public static MapTile getMapTileFromCoordinates(final int zoom, final double aLat, final double aLon) {
		final int y = (int) Math.floor((1 - Math.log(Math.tan(aLat * Math.PI / 180) + 1
				/ Math.cos(aLat * Math.PI / 180))
				/ Math.PI)
				/ 2 * (1 << zoom));
		final int x = (int) Math.floor((aLon + 180) / 360 * (1 << zoom));

		return new MapTile(zoom, x, y);
	}

	private abstract class ProgressTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected abstract Void doInBackground(Void... params);

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			progress.setVisibility(View.GONE);
			editOptions.setVisibility(View.VISIBLE);
			cancelButton.setVisibility(View.GONE);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progress.setVisibility(View.VISIBLE);
			editOptions.setVisibility(View.GONE);
			cancelButton.setVisibility(View.VISIBLE);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);

			progress.setIndeterminate(false); // We now know where we are
			progress.setProgress(values[0]);
		}

	}

	private class ClearCacheTask extends ProgressTask {

		@Override
		protected Void doInBackground(Void... params) {
			File dir = OpenStreetMapTileProviderConstants.TILE_PATH_BASE;

			deleteAll(dir);

			return null;
		}

		/**
		 * Recursively delete all files in a directory
		 * 
		 * @param dir
		 */
		private void deleteAll(File dir) {
			if (isCancelled())
				return;

			if (dir.isDirectory()) {
				File[] children = dir.listFiles();

				for (File f : children)
					deleteAll(f);
			}
			if (dir.exists()) {
				Log.d(TAG, "Deleting " + dir);
				dir.delete();
			}
		}
	}

	private class DownloadTilesTask extends ProgressTask implements
			IMapTileProviderCallback {

		MapTileFilesystemProvider tilesource;

		int numReceived = 0, numFailed = 0;

		public DownloadTilesTask() {
			final IRegisterReceiver registerReceiver = new IRegisterReceiver() {
				@Override
				public Intent registerReceiver(final BroadcastReceiver aReceiver,
						final IntentFilter aFilter) {
					return PrefetchMapActivity.this.registerReceiver(aReceiver, aFilter);
				}

				@Override
				public void unregisterReceiver(BroadcastReceiver receiver) {
					PrefetchMapActivity.this.unregisterReceiver(receiver);
				}
			};

			tilesource = new MapTileFilesystemProvider(registerReceiver);
			// tilesource = new OpenStreetMapTileProviderDirect(arrivalHandler,
			// CloudmadeUtil.getCloudmadeKey(PrefetchMapActivity.this));
		}

		/**
		 * This will be invoked in the GUI thread
		 * 
		 * @author kevinh
		 * 
		 */
		private class TileArrivalHandler extends Handler {
			@Override
			public void handleMessage(final Message msg) {
				switch (msg.what) {
				case MapTile.MAPTILE_SUCCESS_ID:
					break;
				}
			}
		}

		@Override
		protected Void doInBackground(Void... params) {

			// Request a zillion tiles
			int numRequested = 0;
			int minZoom = zoomLevel, maxZoom = zoomLevel;

			for (int zoom = minZoom; zoom <= maxZoom && !isCancelled(); zoom++) {
				prepareTileCounts(zoom);

				for (int tileY = tileStartY; tileY != tileStopY && !isCancelled(); tileY = (tileY + 1)
						% numTilesFullWidth)
					for (int tileX = tileStartX; tileX != tileStopX && !isCancelled(); tileX = (tileX + 1)
							% numTilesFullWidth) {
						final MapTile tile = new MapTile(zoom, tileX, tileY);
						MapTileRequestState mrs = new MapTileRequestState(tile, new MapTileModuleProviderBase[]{tilesource}, null);
						tilesource.loadMapTileAsync(mrs);
						numRequested++;

						publishProgress(numRequested * 100 / numTiles);
					}
			}

			return null;
		}
//
//		@Override
//		public String getCloudmadeKey() throws CloudmadeException {
//			return CloudmadeUtil.getCloudmadeKey(PrefetchMapActivity.this);
//		}

		@Override
		public boolean useDataConnection() {
			return true;
		}

		@Override
		public void mapTileRequestCompleted(MapTileRequestState aTile,
				Drawable aDrawable) {
			numReceived++;
		}

		@Override
		public void mapTileRequestFailed(MapTileRequestState aTile) {
			numFailed++;
		}
	}
}
