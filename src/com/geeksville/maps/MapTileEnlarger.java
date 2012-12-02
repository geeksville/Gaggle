package com.geeksville.maps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.osmdroid.tileprovider.LRUMapTileCache;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * enlarges (subsamples) tiles from a lower zoomlevel and saves them to the filesystem.
 * @author Hans
 *
 */
public class MapTileEnlarger extends MapTileModuleProviderBase {

    // ===========================================================
    // Constants
    // ===========================================================

    private LRUMapTileCache baseCache = new LRUMapTileCache(6);
    // ===========================================================
    // Fields
    // ===========================================================

    private final IFilesystemCache mFilesystemCache;

    private ITileSource mTileSource;

    private MapTile previousBaseTile;
    private BitmapDrawable previousBaseBitmapDrawable;

    // ===========================================================
    // Constructors
    // ===========================================================

    // public MapTileEnlarger(MapTileZipFileArchiveProvider baseTileProvider) {
    // this(null,baseTileProvider);
    // }

    private MBTilesFileArchive baseTileProvider;

    // private MapTileCache mTileCache;

    private int baseMaxZoomLevel;

    public MapTileEnlarger(final IFilesystemCache pFilesystemCache, MBTilesFileArchive baseFileArchive, int baseMaxZoomLevel) {
        super(NUMBER_OF_TILE_DOWNLOAD_THREADS, TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE);
        this.baseTileProvider = baseFileArchive;
        this.baseMaxZoomLevel = baseMaxZoomLevel;
        mFilesystemCache = pFilesystemCache;
        // this.mTileCache = new MapTileCache(9);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public boolean getUsesDataConnection() {
        return true;
    }

    @Override
    protected String getName() {
        return "Online Tile Download Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return "downloader";
    }

    EnlargerTileLoader enlargerTileLoader = new EnlargerTileLoader();

    @Override
    protected Runnable getTileLoader() {
        return enlargerTileLoader;
    };

    @Override
    public int getMinimumZoomLevel() {
        return (mTileSource != null ? mTileSource.getMinimumZoomLevel() : MINIMUM_ZOOMLEVEL);
    }

    @Override
    public int getMaximumZoomLevel() {
        return (mTileSource != null ? mTileSource.getMaximumZoomLevel() : MAXIMUM_ZOOMLEVEL);
    }

    @Override
    public void setTileSource(final ITileSource tileSource) {
        this.mTileSource = tileSource;
    }

    Matrix matrix = new Matrix();

    private Set<MapTile> notAvailableTileList = new HashSet<MapTile>();

    private BitmapDrawable createBitmapFromLowerResTileBitmap(MapTile tile, int extraMag) {
        double extraMag2 = Math.pow(2, extraMag);
        final int baseBitMapX = (int) ((double) tile.getX() / extraMag2);
        final int baseBitMapY = (int) ((double) tile.getY() / extraMag2);
        final int baseZoom = tile.getZoomLevel() - extraMag;
        final int sizeSubTile = (int) ((double) 256 / extraMag2);
        final int xPositionWithin = (int) (tile.getX() % extraMag2 * sizeSubTile);
        final int yPositionWithin = (int) (tile.getY() % extraMag2 * sizeSubTile);
        synchronized (matrix) {
            // if (previousBaseTile == null || previousBaseTile.getZoomLevel() != baseZoom) {
            matrix.reset();
            matrix.postScale((float) extraMag2, (float) extraMag2);
            // }
            MapTile baseTile = new MapTile(baseZoom, baseBitMapX, baseBitMapY);
            BitmapDrawable baseDrawable = (BitmapDrawable) baseCache.get(baseTile);
            if (baseDrawable == null) {
                if (!notAvailableTileList.contains(baseTile)) {
                    baseDrawable = getBitMapDrawableForBaseTile(baseTile);
                }
                if (baseDrawable == null) {
                    notAvailableTileList.add(baseTile);
                }
                else {
                    baseCache.put(baseTile, baseDrawable);
                }
            }
            // if (previousBaseTileCoordsNotEqualToBaseBitMapCoords(baseBitMapX, baseBitMapY)
            // || (previousBaseBitmapDrawable != null && previousBaseBitmapDrawable.getBitmap().isRecycled())) {
            // previousBaseTile = new MapTile(baseZoom, baseBitMapX, baseBitMapY);
            // previousBaseBitmapDrawable = getBitMapDrawableForBaseTile(previousBaseTile);
            // }
            if (baseDrawable != null) {
                return createBitMapDrawableByUpsampling(baseDrawable, matrix, sizeSubTile, xPositionWithin, yPositionWithin);
            }
        }
         return null;
    }

    private BitmapDrawable createBitMapDrawableByUpsampling(BitmapDrawable previousBaseBitmapDrawable, Matrix matrix,
            final int sizeSubTile, final int xPositionWithin, final int yPositionWithin) {
        BitmapDrawable bitmapDrawable;
        Bitmap bitmap =
            Bitmap.createBitmap(previousBaseBitmapDrawable.getBitmap(), xPositionWithin, yPositionWithin, sizeSubTile,
                sizeSubTile, matrix, true);
        bitmapDrawable = new BitmapDrawable(bitmap);
        return bitmapDrawable;
    }

    // private BitmapDrawable getBitMapDrawableForBaseTile(MapTileCache mTileCache, MapTile baseTile) {
    // BitmapDrawable returnBitmapDrawable = (BitmapDrawable) mTileCache.getMapTile(baseTile);
    // if (returnBitmapDrawable == null) {
    // returnBitmapDrawable = getBitMapDrawableForBaseTile(baseTile);
    // // mTileCache.putTile(baseTile, returnBitmapDrawable);
    // }
    // return returnBitmapDrawable;
    // }

    private BitmapDrawable getBitMapDrawableForBaseTile(MapTile tile) {
        BitmapDrawable bitmapDrawable = null;
        final InputStream inputStream = getInputStreamForBaseTile(tile);
        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        if (bitmap != null) {
            bitmapDrawable = new BitmapDrawable(bitmap);
        }
        return bitmapDrawable;
    }

    private InputStream getInputStreamForBaseTile(MapTile tile) {
        // final int zoomlevel = tile.getZoomLevel();
        // final int x = tile.getX();
        // final int y = tile.getY();
        // InputStream returnValue = null;
        return baseTileProvider.getInputStream(null, tile);
    }

    private boolean previousBaseTileCoordsNotEqualToBaseBitMapCoords(final int baseBitMapX, final int baseBitMapY) {
        return previousBaseTile == null || previousBaseTile.getX() != baseBitMapX || previousBaseTile.getY() != baseBitMapY;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private class EnlargerTileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final MapTileRequestState aState) throws CantContinueException {

            final MapTile tile = aState.getMapTile();

            int extraMag = tile.getZoomLevel() - baseMaxZoomLevel;

            BitmapDrawable bitmapDrawable = null;
            try {
                bitmapDrawable = createBitmapFromLowerResTileBitmap(tile, extraMag);
            }
            catch (final OutOfMemoryError e) {
                System.gc();
            }

            // Save the data to the filesystem cache

            if (bitmapDrawable != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmapDrawable.getBitmap().compress(CompressFormat.JPEG, 60, bos);
                byte[] bitmapdata = bos.toByteArray();
                ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

                if (mFilesystemCache != null) {
                    mFilesystemCache.saveFile(mTileSource, tile, bs);
                    bs.reset();
                }
            }
            return bitmapDrawable;
        }

    }
}
