package com.geeksville.maps;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;

public class TileSourceAdaptor extends BitmapTileSourceBase{


//    public TileSourceAdaptor() {
//        super("", ResourceProxy.string.unknown, 0, 0, 256, ".jpg");
//        // TODO Auto-generated constructor stub
//    }

    private String subDir;

    public TileSourceAdaptor(int maximumZoomLevel, String subDir) {
        super(subDir, ResourceProxy.string.unknown, 0, maximumZoomLevel, 256, ".jpg");
//        this.maximumZoomLevel = maximumZoomLevel;
//        this.subDir = subDir;
    }

//    private int maximumZoomLevel;
//    private int minimumZoomLevel;
//
//    @Override
//    public String getTileRelativeFilenameString(MapTile aTile) {
//        return subDir + "/" + aTile.getZoomLevel() + "/" + aTile.getX() + "/" + aTile.getY() + ".jpg";
//    }
//
//    @Override
//    public int getMinimumZoomLevel() {
//        // TODO Auto-generated method stub
//        return minimumZoomLevel;
//    }
//
//    public void setMinimumZoomLevel(int minimumZoomLevel) {
//        this.minimumZoomLevel = minimumZoomLevel;
//    }
//
//    @Override
//    public int getMaximumZoomLevel() {
//        // TODO Auto-generated method stub
//        return maximumZoomLevel;
//    }
//
//    public void setMaximumZoomLevel(int maximumZoomLevel) {
//        this.maximumZoomLevel = maximumZoomLevel;
//    }

}
