package com.geeksville.maps;

import java.util.ArrayList;
import java.util.List;

public class ArchiveTileSource extends TileSourceAdaptor{

    
    public ArchiveTileSource(String name) {
        super(0, name);
    }

    List<ArchiveInfo> archiveInfos = new ArrayList<ArchiveInfo>();
    
    public void addArchiveInfo(ArchiveInfo archiveInfo) {
		if (archiveInfo != null) {
			this.archiveInfos.add(archiveInfo);
		}
    }

    public List<ArchiveInfo> getArchiveInfos() {
        // TODO Auto-generated method stub
        return archiveInfos;
    }

    public void clearArchiveInfos(){
        archiveInfos.clear();
    }
}