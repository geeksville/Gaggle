package com.geeksville.maps;

import java.io.File;

import org.andnav.osm.tileprovider.CloudmadeException;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.tileprovider.OpenStreetMapTileFilesystemProvider;
import org.andnav.osm.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.andnav.osm.tileprovider.util.CloudmadeUtil;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.util.IOpenStreetMapRendererInfo;
import org.andnav.osm.views.util.OpenStreetMapRendererFactory;
import org.andnav.osm.views.util.OpenStreetMapTileProvider;
import org.andnav.osm.views.util.OpenStreetMapTileProviderDirect;

import com.geeksville.gaggle.R;
import com.geeksville.location.LocationUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prefetch_map);

		Bundle extras = getIntent().getExtras();
		center = new GeoPoint(extras.getInt(EXTRA_LATITUDE), extras.getInt(EXTRA_LONGITUDE));
		zoomLevel = extras.getInt(EXTRA_ZOOMLEVEL);
		source = extras.getString(EXTRA_TILESOURCE);

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
	public static OpenStreetMapTile getMapTileFromCoordinates(IOpenStreetMapRendererInfo renderer,
			final int zoom, final double aLat, final double aLon) {
		final int y = (int) Math.floor((1 - Math.log(Math.tan(aLat * Math.PI / 180) + 1
				/ Math.cos(aLat * Math.PI / 180))
				/ Math.PI)
				/ 2 * (1 << zoom));
		final int x = (int) Math.floor((aLon + 180) / 360 * (1 << zoom));

		return new OpenStreetMapTile(renderer, zoom, x, y);
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
			String dirName = OpenStreetMapTileProviderConstants.TILE_PATH_BASE;

			File dir = new File(dirName);
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
			IOpenStreetMapTileProviderCallback {

		// Handler arrivalHandler = new TileArrivalHandler();

		IOpenStreetMapRendererInfo renderer;
		// OpenStreetMapTileProvider tilesource;
		OpenStreetMapTileFilesystemProvider tilesource;

		int numReceived = 0;

		public DownloadTilesTask() {
			renderer = OpenStreetMapRendererFactory.getRenderer(source);

			tilesource = new OpenStreetMapTileFilesystemProvider(this);
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
				case OpenStreetMapTile.MAPTILE_SUCCESS_ID:
					break;
				}
			}
		}

		@Override
		protected Void doInBackground(Void... params) {

			double[] nwPoint = LocationUtils.addDistance(center.getLatitudeE6() * 1e-6, center
					.getLongitudeE6() * 1e-6, distMeters, Math.toRadians(45));
			double[] sePoint = LocationUtils.addDistance(center.getLatitudeE6() * 1e-6, center
					.getLongitudeE6() * 1e-6, distMeters, Math.toRadians(180 + 45));

			// Request a zillion tiles
			int numRequested = 0;
			int minZoom = zoomLevel, maxZoom = zoomLevel;

			for (int zoom = minZoom; zoom <= maxZoom && !isCancelled(); zoom++) {
				int numTilesFullWidth = 1 << zoom; // The total # of tiles in X
				// & Y for the world

				OpenStreetMapTile start = getMapTileFromCoordinates(renderer, zoom, nwPoint[0],
						nwPoint[1]);
				OpenStreetMapTile stop = getMapTileFromCoordinates(renderer, zoom, sePoint[0],
						sePoint[1]);

				int tileStartX = start.getX();
				int tileStopX = (stop.getX() + 1) % numTilesFullWidth;
				int tileStartY = start.getY();
				int tileStopY = (stop.getY() + 1) % numTilesFullWidth;

				int numTilesX = (tileStopX - tileStartX)
						+ ((tileStopX <= tileStartX) ? numTilesFullWidth : 0);
				int numTilesY = (tileStopY - tileStartY)
						+ ((tileStopY <= tileStartY) ? numTilesFullWidth : 0);
				int numTiles = numTilesX * numTilesY;

				for (int tileY = tileStartY; tileY != tileStopY && !isCancelled(); tileY = (tileY + 1)
						% numTilesFullWidth)
					for (int tileX = tileStartX; tileX != tileStopX && !isCancelled(); tileX = (tileX + 1)
							% numTilesFullWidth) {
						final OpenStreetMapTile tile = new OpenStreetMapTile(renderer, zoom,
								tileX, tileY);
						tilesource.loadMapTileAsync(tile);
						numRequested++;

						publishProgress(numRequested * 100 / numTiles);
					}
			}

			return null;
		}

		@Override
		public String getCloudmadeKey() throws CloudmadeException {
			return CloudmadeUtil.getCloudmadeKey(PrefetchMapActivity.this);
		}

		@Override
		public void mapTileRequestCompleted(OpenStreetMapTile pTile, String aTilePath) {
			numReceived++;
		}
	}
}
