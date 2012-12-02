package com.geeksville.maps;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;

public class MyMapTileFileArchiveProvider extends MapTileFileArchiveProvider {

    public MyMapTileFileArchiveProvider(IRegisterReceiver pRegisterReceiver, ITileSource pTileSource, IArchiveFile[] pArchives) {
        super(pRegisterReceiver, pTileSource, pArchives);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public MapTileModuleProviderBase.TileLoader getTileLoader() {
        // TODO Auto-generated method stub
        return (org.osmdroid.tileprovider.modules.MapTileModuleProviderBase.TileLoader) super.getTileLoader();
    }

}
