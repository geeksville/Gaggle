package com.geeksville.maps;

public class ArchiveInfo {
    String fileName;
    int maxZoomLevel;
    public ArchiveInfo(String fileName, int maxZoomLevel) {
        super();
        this.fileName = fileName;
        this.maxZoomLevel = maxZoomLevel;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public int getMaxZoomLevel() {
        return maxZoomLevel;
    }
    public void setMaxZoomLevel(int maxZoomLevel) {
        this.maxZoomLevel = maxZoomLevel;
    }
    
    
    
}
