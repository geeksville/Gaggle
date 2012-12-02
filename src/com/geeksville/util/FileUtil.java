package com.geeksville.util;

public class FileUtil {
    public static String stripExtension(String fileName) {
        int indexOfLastDot = getIndexOfLastDot(fileName);
        return fileName.substring(0, indexOfLastDot);
    }

    private static int getIndexOfLastDot(String fileName) {
        int indexOfLastDot = fileName.lastIndexOf('.');
        indexOfLastDot = indexOfLastDot != -1 ? indexOfLastDot : fileName.length();
        return indexOfLastDot;
    }

    public static String getExtension(String fileName) {
        int indexOfLastDot = getIndexOfLastDot(fileName);
        if(indexOfLastDot==fileName.length()){
            indexOfLastDot = fileName.length()-1;
        }
        
        return fileName.substring(indexOfLastDot+1,fileName.length());
    }

}
