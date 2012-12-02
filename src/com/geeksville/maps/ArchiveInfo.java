package com.geeksville.maps;

public class ArchiveInfo {
    String fileName;
    int maxZoomLevel;
	private int minZoomLevel;
    public ArchiveInfo(String fileName, int minZoom, int maxZoom) {
        super();
        this.fileName = fileName;
        this.minZoomLevel = minZoom;
        this.maxZoomLevel = maxZoom;
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
	public int getMinZoomLevel() {
		return minZoomLevel;
	}
	public void setMinZoomLevel(int minZoomLevel) {
		this.minZoomLevel = minZoomLevel;
	}
    
    
    
}
