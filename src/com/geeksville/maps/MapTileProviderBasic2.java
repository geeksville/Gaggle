package com.geeksville.maps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import microsoft.mappoint.TileSystem;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.geeksville.gaggle.GaggleApplication;
import com.geeksville.util.FileUtil;

/**
 * This top-level tile provider implements a basic tile request chain which includes a {@link MapTileFilesystemProvider} (a
 * file-system cache), a {@link MapTileFileArchiveProvider} (archive provider), and a {@link MapTileDownloader} (downloads map tiles
 * via tile source).
 * 
 * @author Marc Kurtz
 */
public class MapTileProviderBasic2 extends MapTileProviderArray implements IMapTileProviderCallback {

    private static final String MAX_ZOOM_STRING = "maxZoom";
	public static final OnlineTileSourceBase DUMMY = new XYTileSource("Dummy", null, 0, 0, 256, null, null);
    private String tileDir;
    private SimpleRegisterReceiver registerReceiver;
	private Context context;
    public static final String osmdroidTilesLocation = "/osmdroid/";
	private static final String MIN_ZOOM_STRING = "minZoom"; 

    public MapTileProviderBasic2(Context myMap, AssetManager assets) {
        super(DUMMY, null);
        // super(TileSourceFactory.DEFAULT_TILE_SOURCE, null);
        // SECOND provider: zip-archives:
        this.tileDir = Environment.getExternalStorageDirectory() + osmdroidTilesLocation;
//        registerSdTilesources(new SimpleRegisterReceiver(myMap), Environment.getExternalStorageDirectory() + osmdroidTilesLocation,
//            secondLevelFileName, 15, FileUtil.stripExtension(secondLevelFileName));
        this.context = myMap;
        this.registerReceiver = new SimpleRegisterReceiver(myMap);
        
    }

    @Override
    public void setTileSource(ITileSource aTileSource) {
        super.setTileSource(aTileSource);
        TileSystem.setTileSize(aTileSource.getTileSizePixels());
        initTileSource();
    }

    public static ArchiveInfo makeMBTilesArchiveInfo(String fileName, boolean checkIfArchiveExists) {
        String filePath = Environment.getExternalStorageDirectory() + MapTileProviderBasic2.osmdroidTilesLocation + fileName;
        File file = new File(filePath);
        if(checkIfArchiveExists && !file.exists()){
        	return null;
        }
        String archiveInfoFilePath = filePath + ".info";
        ArchiveInfo archiveInfo = null;
        try {
			FileInputStream fis = new FileInputStream(archiveInfoFilePath);
			BufferedReader isr = new BufferedReader(new InputStreamReader(fis));
			String jsonString = IOUtils.toString(isr);
			isr.close();
			JSONObject json = new JSONObject(jsonString);
			int maxZoom = json.getInt(MAX_ZOOM_STRING);
			int minZoom = json.getInt(MIN_ZOOM_STRING);
			archiveInfo = new ArchiveInfo(fileName, minZoom, maxZoom); 
			return archiveInfo;
		} catch (JSONException e) {
		} catch (IOException e) {
		}
        SQLiteDatabase dataBase = SQLiteDatabase.openDatabase(
            filePath,
            null,
            SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
        int maxZoom = getZoom(dataBase, "MAX");
        int minZoom = getZoom(dataBase, "MIN");
        dataBase.close();
        archiveInfo = new ArchiveInfo(fileName, minZoom, maxZoom); 
		try {
			JSONObject json = new JSONObject();
			json.put("fileName", archiveInfo.getFileName());
			json.put(MAX_ZOOM_STRING, archiveInfo.getMaxZoomLevel());
			json.put(MIN_ZOOM_STRING, archiveInfo.getMinZoomLevel());
			FileOutputStream fos = new FileOutputStream(archiveInfoFilePath);
			OutputStreamWriter write = new OutputStreamWriter(fos);
			write.write(json.toString());
			write.close();
		} catch (JSONException e) {
		} catch (IOException e) {
		}
		
        return archiveInfo;
    }

	private static int getZoom(SQLiteDatabase dataBase, String string) {
		Cursor cursor = dataBase.rawQuery("SELECT "+string+"(zoom_level) FROM " + MBTilesFileArchive.TABLE_TILES, null);
        cursor.moveToFirst();
        int maxZoom = cursor.getInt(0);
        cursor.close();
		return maxZoom;
	}

    public void initTileSource() {
        mTileProviderList.clear();
        clearTileCache();
        if(getTileSource() instanceof ArchiveTileSource){
            createArchiveTileProviderList();
        }
        else {
            registerDownloadTileSource(registerReceiver, getTileSource());
        }
        
    }

	private void createArchiveTileProviderList() {
		ArchiveTileSource archiveTileSource = (ArchiveTileSource)getTileSource();
		List<ArchiveInfo> archiveFileNames = archiveTileSource.getArchiveInfos();
		List<Entry<MapTileFilesystemProvider, MapTileEnlarger>> enlargementProviderList = new ArrayList<Entry<MapTileFilesystemProvider,MapTileEnlarger>>();
		// first all archives need to bee checked to find a tile.
		for (ArchiveInfo archiveInfo : archiveFileNames) {
		    Entry<MapTileFilesystemProvider, MapTileEnlarger> enlargementProviders = registerSdTilesource(registerReceiver, Environment.getExternalStorageDirectory() + osmdroidTilesLocation, archiveInfo.getFileName(), archiveInfo.getMaxZoomLevel());
			if (enlargementProviders != null) {
				enlargementProviderList.add(enlargementProviders);
			}
		}
		// then the filesystems in which enlargements are stored are searched  
		for (Entry<MapTileFilesystemProvider, MapTileEnlarger> entry : enlargementProviderList) {
			MapTileFilesystemProvider fileSystemProvider = entry.getKey();
			mTileProviderList.add(fileSystemProvider);
		}
		// finally if nothing is found, the enlarging providers are called. They store enlargements in the filesystem if they succeed.
		for (Entry<MapTileFilesystemProvider, MapTileEnlarger> entry : enlargementProviderList) {
			MapTileEnlarger mapTileEnlargeMentProvider = entry.getValue();
			mTileProviderList.add(mapTileEnlargeMentProvider);
		}
		ITileSource onlineBackground = archiveTileSource.getOnlineBackground(); 
		if(onlineBackground!=null){
			registerDownloadTileSource(registerReceiver, onlineBackground);
		}
		Entry<MapTileFilesystemProvider, MapTileEnlarger> enlargementProviders = registerSdTilesource(registerReceiver, context.getFilesDir().getAbsolutePath() + "/", "world.mbtiles", 5);
		mTileProviderList.add(enlargementProviders.getKey());
		mTileProviderList.add(enlargementProviders.getValue());
	}

    private void registerDownloadTileSource(final IRegisterReceiver pRegisterReceiver, ITileSource tileSource) {
        final TileWriter tileWriter = new TileWriter();

        final MapTileFilesystemProvider fileSystemProvider = new MapTileFilesystemProvider(
                pRegisterReceiver, tileSource);
        mTileProviderList.add(fileSystemProvider);

        final MapTileDownloader downloaderProvider = new MapTileDownloader(tileSource, tileWriter,
            new NetworkAvailabliltyCheck(GaggleApplication.getContext()));
        mTileProviderList.add(downloaderProvider);
    }

    private Entry<MapTileFilesystemProvider, MapTileEnlarger> registerSdTilesource(final IRegisterReceiver pRegisterReceiver, String tileDir, String archiveFileName,
            int maxZoomLevel2) {
        String fileSystemSubdirName = FileUtil.stripExtension(archiveFileName);
        String secondLevelFilePath = tileDir + archiveFileName;
        File archiveFile = new File(secondLevelFilePath);
        MBTilesFileArchive fileArchive = null;
        if (archiveFile.exists()) {
            fileArchive = addArchiveFile(pRegisterReceiver, archiveFile, maxZoomLevel2, fileSystemSubdirName);
        } else {
        	return null;
        }

        // THIRD provider: saved blown up tiles:
        final MapTileFilesystemProvider fileSystemProvider =
            new MapTileFilesystemProvider(pRegisterReceiver, new TileSourceAdaptor(19, fileSystemSubdirName));

//        mTileProviderList.add(fileSystemProvider);

        // FOURTH provider: blow up tiles, and then save:
        final TileWriter tileWriter = new TileWriter();

        final MapTileEnlarger blowUpLowResProvider =
            new MapTileEnlarger(tileWriter, fileArchive, mTileProviderList.get(mTileProviderList.size() - 1)
                .getMaximumZoomLevel());
        blowUpLowResProvider.setTileSource(new TileSourceAdaptor(19, fileSystemSubdirName));
//        mTileProviderList.add(blowUpLowResProvider);
        return new AbstractMap.SimpleEntry<MapTileFilesystemProvider, MapTileEnlarger>(fileSystemProvider, blowUpLowResProvider);
    }


    private MBTilesFileArchive addArchiveFile(final IRegisterReceiver pRegisterReceiver, File archiveFile, int maxZoomlevel,
            String fileNameWithoutExtension) {
        IArchiveFile fileArchive = ArchiveFileFactory.getArchiveFile(archiveFile);
        MyMapTileFileArchiveProvider archiveProvider =
            new MyMapTileFileArchiveProvider(pRegisterReceiver, new TileSourceAdaptor(maxZoomlevel, fileNameWithoutExtension),
                new IArchiveFile[] { fileArchive });
        mTileProviderList.add(archiveProvider);
        return (MBTilesFileArchive) fileArchive;
    }

    @Override
    protected MapTileModuleProviderBase findNextAppropriateProvider(MapTileRequestState aState) {
        MapTileModuleProviderBase provider = null;
        int zoomLevel = aState.getMapTile().getZoomLevel();
        // The logic of the while statement is
        // "Keep looping until you get null, or a provider that still exists and has a data connection if it needs one,"
        do {
            provider = aState.getNextProvider();
        }
        while ((provider != null)
            && (!getProviderExists(provider) || provider.getMaximumZoomLevel() < zoomLevel || (!useDataConnection() && provider
                .getUsesDataConnection())));
        return provider;
    }

	@Override
	public int getMinimumZoomLevel() {
		if(mTileProviderList.isEmpty()){
			return 0;
		}
		return super.getMinimumZoomLevel();
	}

	@Override
	public int getMaximumZoomLevel() {
		if(mTileProviderList.isEmpty()){
			return 22;
		}
		return super.getMaximumZoomLevel();
	}

}