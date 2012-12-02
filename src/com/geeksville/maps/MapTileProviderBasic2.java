package com.geeksville.maps;

import java.io.File;
import java.util.List;

import microsoft.mappoint.TileSystem;

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

    public static final OnlineTileSourceBase DUMMY = new XYTileSource("Dummy", null, 0, 0, 256, null, null);
    private String tileDir;
    private SimpleRegisterReceiver registerReceiver;
    public static final String osmdroidTilesLocation = "/osmdroid/"; 

    public MapTileProviderBasic2(Context myMap, AssetManager assets) {
        super(DUMMY, null);
        // super(TileSourceFactory.DEFAULT_TILE_SOURCE, null);
        // SECOND provider: zip-archives:
        this.tileDir = Environment.getExternalStorageDirectory() + osmdroidTilesLocation;
//        registerSdTilesources(new SimpleRegisterReceiver(myMap), Environment.getExternalStorageDirectory() + osmdroidTilesLocation,
//            secondLevelFileName, 15, FileUtil.stripExtension(secondLevelFileName));
        this.registerReceiver = new SimpleRegisterReceiver(myMap);
        
    }

    @Override
    public void setTileSource(ITileSource aTileSource) {
        super.setTileSource(aTileSource);
        TileSystem.setTileSize(aTileSource.getTileSizePixels());
        initTileSource();
    }

    public static ArchiveInfo makeMBTilesArchiveInfo(String fileName) {
        String filePath = Environment.getExternalStorageDirectory() + MapTileProviderBasic2.osmdroidTilesLocation + fileName;
        SQLiteDatabase dataBase = SQLiteDatabase.openDatabase(
            filePath,
            null,
            SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = dataBase.rawQuery("SELECT MAX(zoom_level) FROM " + MBTilesFileArchive.TABLE_TILES, null);
        cursor.moveToFirst();
        int maxZoom = cursor.getInt(0);
        cursor.close();
        dataBase.close();
        return new ArchiveInfo(fileName, maxZoom);
    }

    public void initTileSource() {
        mTileProviderList.clear();
        if(getTileSource() instanceof ArchiveTileSource){
            ArchiveTileSource archiveTileSource = (ArchiveTileSource)getTileSource();
            List<ArchiveInfo> archiveFileNames = archiveTileSource.getArchiveInfos();
            for (ArchiveInfo archiveInfo : archiveFileNames) {
                registerSdTilesources(registerReceiver, Environment.getExternalStorageDirectory() + osmdroidTilesLocation, archiveInfo.getFileName(), archiveInfo.getMaxZoomLevel());
            }
        }
        else {
            registerDownloadTileSource(registerReceiver);
        }
    }

    private void registerDownloadTileSource(final IRegisterReceiver pRegisterReceiver) {
        final TileWriter tileWriter = new TileWriter();

        final MapTileFilesystemProvider fileSystemProvider = new MapTileFilesystemProvider(
                pRegisterReceiver, getTileSource());
        mTileProviderList.add(fileSystemProvider);

        final MapTileDownloader downloaderProvider = new MapTileDownloader(getTileSource(), tileWriter,
            new NetworkAvailabliltyCheck(GaggleApplication.getContext()));
        mTileProviderList.add(downloaderProvider);
    }

    private void registerSdTilesources(final IRegisterReceiver pRegisterReceiver, String tileDir, String archiveFileName,
            int maxZoomLevel2) {
        String fileSystemSubdirName = FileUtil.stripExtension(archiveFileName);
        String secondLevelFilePath = tileDir + archiveFileName;
        File archiveFile = new File(secondLevelFilePath);
        MBTilesFileArchive fileArchive = null;
        if (new File(secondLevelFilePath).exists()) {
            fileArchive = addArchiveFile(pRegisterReceiver, archiveFile, maxZoomLevel2, fileSystemSubdirName);
        }

        // THIRD provider: saved blown up tiles:
        final MapTileFilesystemProvider fileSystemProvider =
            new MapTileFilesystemProvider(pRegisterReceiver, new TileSourceAdaptor(19, fileSystemSubdirName));
        mTileProviderList.add(fileSystemProvider);

        // FOURTH provider: blow up tiles, and then save:
        final TileWriter tileWriter = new TileWriter();

        final MapTileEnlarger blowUpLowResProvider =
            new MapTileEnlarger(tileWriter, fileArchive, mTileProviderList.get(mTileProviderList.size() - 2)
                .getMaximumZoomLevel());
        blowUpLowResProvider.setTileSource(new TileSourceAdaptor(19, fileSystemSubdirName));
        mTileProviderList.add(blowUpLowResProvider);
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

}
