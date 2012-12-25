package com.geeksville.gaggle.fragments;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.geeksville.android.LifeCycleHandler;
import com.geeksville.android.LifeCyclePublisher;
import com.geeksville.android.LifeCyclePublisherImpl;
import com.geeksville.gaggle.GagglePrefs;
import com.geeksville.gaggle.R;
import com.geeksville.maps.ArchiveInfo;
import com.geeksville.maps.ArchiveTileSource;
import com.geeksville.maps.CenteredMyLocationOverlay;
import com.geeksville.maps.GeeksvilleMapView;
import com.geeksville.maps.MapTileProviderBasic2;
import com.geeksville.util.FileUtil;
import com.geeksville.util.GaggleUncaughtExceptionHandler;

public class AbstractGeeksvilleMapFragment extends Fragment implements LifeCyclePublisher {

	private static final String ARCHIVEMENUNAME = "Archive";
	// private LinearLayout linearLayout;
	protected GeeksvilleMapView mapView;

	private MyLocationOverlay myLocationOverlay;

	private LifeCyclePublisherImpl lifePublish = new LifeCyclePublisherImpl();
	private ITileSource defaultOnlineBackgroundTileSource;

	// There is also TopOSM features, but we don't bother to show that
//	private static final XYTileSource TopOSMRelief =
//			new XYTileSource("Topo Relief (USA)", ResourceProxy.string.unknown, 4, 15, 8, ".jpg",
//					"http://tile1.toposm.com/us/color-relief/",
//					"http://tile2.toposm.com/us/color-relief/",
//					"http://tile3.toposm.com/us/color-relief/");
//
//	private static final XYTileSource TopOSMContours =
//		new XYTileSource("Topo Contours (USA)", ResourceProxy.string.unknown, 12, 15, 8, ".png",
//				"http://tile1.toposm.com/us/contours/",
//				"http://tile2.toposm.com/us/contours/",
//				"http://tile3.toposm.com/us/contours/");
//
//	private static final XYTileSource OpenCycleMap =
//		new XYTileSource("www.opencyclemap.org", ResourceProxy.string.unknown, 1, 18, 8, ".png",
//				"http://a.tile.opencyclemap.org/cycle/",
//				"http://b.tile.opencyclemap.org/cycle/",
//				"http://c.tile.opencyclemap.org/cycle/"
//				);
//	private static final XYTileSource OpenHikingMap =
//		new XYTileSource("maps.refuges.info", ResourceProxy.string.unknown, 1, 18, 8, ".jpeg",
//				"http://maps.refuges.info/tiles/renderer.py/hiking/");
	
	public static final ITileSource Archive = new ArchiveTileSource("Archive");
	
	private static ITileSource supportedRenderers[] = {
		TileSourceFactory.MAPQUESTAERIAL,
		TileSourceFactory.MAPNIK,
		TileSourceFactory.TOPO,
		TileSourceFactory.CYCLEMAP,
		Archive
//			TopOSMContours,
//			TopOSMRelief
	};

    private static Map<String, ITileSource> supportedRendererMap = new HashMap<String, ITileSource>();

    static {
        for (ITileSource tileSource : supportedRenderers) {
            supportedRendererMap.put(tileSource.name(), tileSource);
        }
    }
	
	private static String supportedRendererNames[];

	public AbstractGeeksvilleMapFragment() {
		initExceptionHandler();
		// FIXME - do this someplace better
//		TileSourceFactory.addTileSource(TopOSMContours);
//		TileSourceFactory.addTileSource(TopOSMRelief);
//		TileSourceFactory.addTileSource(OpenCycleMap);
//		TileSourceFactory.addTileSource(OpenHikingMap);
	}

	
	  public static GaggleUncaughtExceptionHandler initExceptionHandler()
	  {
	    GaggleUncaughtExceptionHandler exceptionHandler;
	    final UncaughtExceptionHandler ueh = Thread.getDefaultUncaughtExceptionHandler();

	    if ((ueh != null) && GaggleUncaughtExceptionHandler.class.isAssignableFrom(ueh.getClass())) {
	      exceptionHandler = (GaggleUncaughtExceptionHandler)ueh;
	    } else {
	      exceptionHandler = new GaggleUncaughtExceptionHandler(ueh);
	      Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
	    }

	    return exceptionHandler;
	  }
	
	
	  public void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  supportedRendererNames = new String[] {
					getString(R.string.mapquestaerial_map),
					getString(R.string.mapnik_map),
					getString(R.string.toposm_map),
					getString(R.string.opencyclemap),
					ARCHIVEMENUNAME
//					getString(R.string.openhikingmap),
//					getString(R.string.topo_europe),
//					getString(R.string.topo_us_contour),
//					getString(R.string.topo_us_relief)
			};
		  defaultOnlineBackgroundTileSource = supportedRendererMap.get(TileSourceFactory.CYCLEMAP.name());
	  }
	
	/** Called when the activity is first created. */
	public View onCreateView(Bundle savedInstanceState, LayoutInflater inflater, ViewGroup container, int layoutId, int mapViewId) {
		// Our license key is different for the emulator, otherwise these files
		// should be identical
		View v = inflater.inflate(layoutId, container, false);

		mapView = (GeeksvilleMapView) v.findViewById(mapViewId);

		// init tilesource selected in preferences:
        String tilesourceName = GagglePrefs.getInstance().getSelectedTileSourceName();
        tilesourceName = tilesourceName != null ? tilesourceName : "Archive";
        ITileSource tileSource = supportedRendererMap.get(tilesourceName);
        // ITileSource tileSource = supportedRendererMap.get(tilesourceName);
        MapTileProviderBasic2 tileProvider = (MapTileProviderBasic2) mapView.getTileProvider();
        if (tileSource instanceof ArchiveTileSource) {
            ArchiveTileSource archiveTileSource = (ArchiveTileSource) tileSource;
            archiveTileSource.clearArchiveInfos();
            Set<String> archiveNames = GagglePrefs.getInstance().getSelectedArchiveFileNames();
            Set<String> toBeRemoved = new HashSet<String>();
            for (String archiveName : archiveNames) {
                String filePath = Environment.getExternalStorageDirectory() + MapTileProviderBasic2.osmdroidTilesLocation + archiveName;
                if(new File(filePath).exists()){
                archiveTileSource.addArchiveInfo(MapTileProviderBasic2.makeMBTilesArchiveInfo(archiveName, true));
                } else {
                	toBeRemoved.add(archiveName);
                }
            }
            archiveNames.removeAll(toBeRemoved);
            if(GagglePrefs.getInstance().getUseOnlineSourceAsBackgroundForArchives()) {
            	archiveTileSource.setOnlineBackground(defaultOnlineBackgroundTileSource);
            }
        }
        
        tileProvider.setTileSource(tileSource);

        mapView.getController().setZoom(14);
		mapView.setBuiltInZoomControls(true);
		// Set default map view
		mapView.setMultiTouchControls(true);
		// Default to sat view
		// mapView.setSatellite(true);
		return v;
	}

	/**
	 * Collect app metrics on Flurry
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	public void onStart() {
		super.onStart();
		lifePublish.onStart();
	}

	/**
	 * Collect app metrics on Flurry
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	public void onStop() {
		super.onStop();
		lifePublish.onStop();
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.setHasOptionsMenu(true);
	}

	/**
	 * Create our options menu
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.map_optionmenu, menu);

		// Set action for map prefetch
		// menu.findItem(R.id.prefetch_map).setIntent(
		// PrefetchMapActivity.createIntent(this, mapView.getMapCenter(),
		// mapView
		// .getZoomLevel(), mapView.getRenderer().name()));

//		// Dynamically populate the list of renderers we support (FIXME - only
//		// list known good renderers)
//		MenuItem mapoptions = menu.findItem(R.id.mapmode_menu);
//		SubMenu children = mapoptions.getSubMenu();
//		children.clear();
//
//		MenuItem toCheck = null;
//		for (int i = 0; i < supportedRenderers.length; i++) {
//			final ITileSource info = supportedRenderers[i];
//			String name = supportedRendererNames[i];
//
//			MenuItem item = children.add(1, i, Menu.NONE, name);
//			if (mapView.getTileProvider().getTileSource().name().equals(info.name()))
//				toCheck = item;
//
//			item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
//
//				@Override
//				public boolean onMenuItemClick(MenuItem item) {
//					mapView.setTileSource(info);
//					item.setChecked(true);
//					return true;
//				}
//			});
//		}
//		children.setGroupCheckable(1, true, true);
//		toCheck.setChecked(true);
	}
	
    static Set<String> acceptableArchiveExtensions = new HashSet<String>();
    static {
        acceptableArchiveExtensions.add("mbtiles");

    }

    private static final class ArchivesFileFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String filename) {
            String extension = FileUtil.getExtension(filename);
            if (acceptableArchiveExtensions.contains(extension))
                return true;
            else
                return false;
        }
    }

    static ArchivesFileFilter archivesFileFilter = new ArchivesFileFilter();

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem mapoptions = menu.findItem(R.id.mapmode_menu);
        SubMenu children = mapoptions.getSubMenu();
        children.clear();

        MenuItem toCheck = null;
        String selectedTileSourceName = GagglePrefs.getInstance().getSelectedTileSourceName();
        for (int i = 0; i < supportedRenderers.length; i++) {
            final ITileSource info = supportedRenderers[i];
            String name = supportedRendererNames[i];

            MenuItem item = children.add(1, i, Menu.NONE, name);

            if (mapView.getTileProvider().getTileSource().name().equals(info.name()))
                toCheck = item;
            else if (mapView.getTileProvider().getTileSource() instanceof ArchiveTileSource) {
                toCheck = item;
            }

            item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    item.setChecked(true);
                    ITileSource oldTileSource = mapView.getTileProvider().getTileSource();
                    if(oldTileSource != info){
                    	mapView.getTileProvider().setTileSource(info);
                    }
                    if (item.getTitle().equals(ARCHIVEMENUNAME)) {
                    	FragmentActivity activity = AbstractGeeksvilleMapFragment.this.getActivity();
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle("Select Archives");
                        File archiveLocation =
                            new File(Environment.getExternalStorageDirectory() + MapTileProviderBasic2.osmdroidTilesLocation);
                        final String[] availableArchiveFiles = archiveLocation.list(archivesFileFilter);
                        final Set<String> selectedArchives = GagglePrefs.getInstance().getSelectedArchiveFileNames();
                        // the handler taht does the work when something is selected:
                        DialogInterface.OnMultiChoiceClickListener archiveDialogListener =
                            new ArchiveSelectionListener(selectedArchives, availableArchiveFiles);
                        // make extra option for the background online source:
                        boolean[] checkedArchives = new boolean[availableArchiveFiles.length+1];
                        for (int i = 0; i < availableArchiveFiles.length; i++) {
                            checkedArchives[i] = selectedArchives.contains(availableArchiveFiles[i]);
                        }
                        // make extra string for the background online source: 
                        String[] stringsToBeDisplayed = new String[availableArchiveFiles.length+1];
                        System.arraycopy(availableArchiveFiles, 0, stringsToBeDisplayed, 0, availableArchiveFiles.length);
                        stringsToBeDisplayed[availableArchiveFiles.length] = getActivity().getString(
								R.string.use_background_online_source);
                        checkedArchives[availableArchiveFiles.length] = GagglePrefs.getInstance().getUseOnlineSourceAsBackgroundForArchives();
//						if (availableArchiveFiles.length != 0 || ) {
							builder.setMultiChoiceItems(stringsToBeDisplayed,
									checkedArchives, archiveDialogListener);
//						} else {
//							builder.setMessage(activity.getString(R.string.no_archives_available));
//						}
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        // Intent intent = new Intent(GeeksvilleMapActivity.this, ArchiveFileListActivity.class);
                        // startActivity(intent);
                        // mapView =
                        // new GeeksvilleMapView(GeeksvilleMapActivity.this, 256, new DefaultResourceProxyImpl(
                        // GeeksvilleMapActivity.this), new MapTileProviderBasic2(GeeksvilleMapActivity.this,
                        // getApplicationContext().getAssets()));
                        //
                        // // if (isLive)
                        // showCurrentPosition(true);
                        // mapView.setBuiltInZoomControls(true);
                        // item.setCheckable(true);
                    }
                    ((MapTileProviderBasic2) mapView.getTileProvider()).initTileSource();
                    // else {
                    // }
                    return true;

                }
            });
        }
        children.setGroupCheckable(1, true, true);
        toCheck.setChecked(true);

    }

    private final class ArchiveSelectionListener implements DialogInterface.OnMultiChoiceClickListener {
        private final Set<String> selectedArchives;
        private final String[] archiveFiles;

        private ArchiveSelectionListener(Set<String> selectedArchives, String[] archiveFiles) {
            this.selectedArchives = selectedArchives;
            this.archiveFiles = archiveFiles;
        }

		@Override
		public void onClick(DialogInterface dialog, int which, boolean isChecked) {
			// if it is one of the real displayed archives (not the extra option
			// to display an online tilesource for the rest):
			if (which != archiveFiles.length) {
				if (isChecked){
					selectedArchives.add(archiveFiles[which]);
				}
				else{
					selectedArchives.remove(archiveFiles[which]);
				}
			} else {
				GagglePrefs.getInstance().setUseOnlineSourceAsBackgroundForArchives(isChecked);
				MapTileProviderBasic2 tileProvider = ((MapTileProviderBasic2) mapView.getTileProvider());
				ArchiveTileSource archiveTileSource = ((ArchiveTileSource)tileProvider.getTileSource());
				if(isChecked){
					archiveTileSource.setOnlineBackground(defaultOnlineBackgroundTileSource);
					Toast.makeText(getActivity(), R.string.no_online_background_when_offline_warning, Toast.LENGTH_LONG).show();
				} else {
					archiveTileSource.removeOnlineBackground();
				}
			}
			onChangeSelectedArchives();
		}

        private void onChangeSelectedArchives() {
            GagglePrefs.getInstance().setSelectedArchiveFileNames(selectedArchives);
            ArchiveTileSource tileSource = (ArchiveTileSource) mapView.getTileProvider().getTileSource();
            List<ArchiveInfo> infos = tileSource.getArchiveInfos();
            // tileSource.addArchiveInfo(archiveInfo);
            synchronizeArchives(selectedArchives, infos);
            ((MapTileProviderBasic2) mapView.getTileProvider()).initTileSource();
            mapView.invalidate();
        }

        private void synchronizeArchives(Set<String> selectedArchives, List<ArchiveInfo> infos) {
            // use no iterator here, because 'infos' is potentially modified in the loop (ConcurrenModificationExceptions lurk): 
            for(int i = 0; i<infos.size();i++){
                ArchiveInfo archiveInfo = infos.get(i);
                if (!selectedArchives.contains(archiveInfo.getFileName())) {
                    infos.remove(i);
                }
            }
            for (String fileName : selectedArchives) {
                if (!isContainedIn(infos, fileName)) {
                	String filePath = Environment.getExternalStorageDirectory() + MapTileProviderBasic2.osmdroidTilesLocation + fileName;
                	if(new File(filePath).exists()){
                		infos.add(MapTileProviderBasic2.makeMBTilesArchiveInfo(fileName, false));
                	}
                }
            }
        }

        private boolean isContainedIn(List<ArchiveInfo> infos, String fileName) {
            for (ArchiveInfo archiveInfo : infos) {
                if (archiveInfo.getFileName().equals(fileName)) {
                    return true;
                }
            }
            return false;
        }
    }

	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.myloc_menu) {
			zoomToLocation();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Override if you want a different my location view
	 * 
	 * @return
	 */
	protected MyLocationOverlay createLocationOverlay() {
		return new CenteredMyLocationOverlay(getActivity(), mapView);
	}

	/**
	 * If isLive is set, then add an overlay showing where user is
	 */
	protected void showCurrentPosition(boolean zoomToUser) {
		GagglePrefs prefs = new GagglePrefs(getActivity());
		if (prefs.isFlurryEnabled())
		  FlurryAgent.onEvent("View live position");

		myLocationOverlay = createLocationOverlay();

		if (zoomToUser) {
			// Once we find our position, center on it
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					zoomToLocation();
				}
			};
			myLocationOverlay.runOnFirstFix(runnable);
			
		}

		mapView.getOverlays().add(myLocationOverlay);
	}

	private void zoomToLocation() {
		// Center on where the user is
		MapController control = mapView.getController();

        if (myLocationOverlay != null) {
            // using myLocationOverlay.getLastFix() and myLocationOverlay.getMyLocation() may not retrieve the lastknownloaction
            // especially at startup...
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Location location = org.osmdroid.util.LocationUtils.getLastKnownLocation(locationManager);
            GeoPoint loc = new GeoPoint(location);
            control.animateTo(loc);
        }
	}

	@Override
	public void onPause() {
		super.onPause();
		if (myLocationOverlay != null) {
			myLocationOverlay.disableMyLocation();
			myLocationOverlay.disableFollowLocation();
			// myLocationOverlay.disableCompass();
		}

		lifePublish.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		// Show our cur location
		if (myLocationOverlay != null) {
			myLocationOverlay.enableMyLocation();
			// myLocationOverlay.enableCompass();
		}

		lifePublish.onResume();
	}

	@Override
	public void addLifeCycleHandler(LifeCycleHandler h) {
		lifePublish.addLifeCycleHandler(h);
	}

	@Override
	public void removeLifeCycleHandler(LifeCycleHandler h) {
		lifePublish.removeLifeCycleHandler(h);
	}
}
