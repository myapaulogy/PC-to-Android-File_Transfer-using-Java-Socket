package com.example.ipserver;

import android.webkit.MimeTypeMap;

class FileDataStructure {
    private final String fileName;
    private final String type;
    private String mimeTypeMap;
    private String syncStatus;
    private boolean enableProgress;
    private int maxProgress = 0, progress = 0;

    public FileDataStructure(String fileName, String type){
        this.fileName = fileName;
        this.type = type;

        if(type.equals("File")){
            String[] split = fileName.split("\\.");
            this.mimeTypeMap = fileType(split[split.length - 1]);
        }
    }

    public String getFileName(){ return fileName; }
    public String getType(){ return type; }
    public String getMimeTypeMap(){ return mimeTypeMap; }

    public void setMaxProgress(int max){ maxProgress = max; }
    public int getMaxProgress(){ return maxProgress; }

    public void setProgress(int progress){ this.progress = progress; }
    public int getProgress(){ return progress; }

    public void EnableProgress(boolean enableProgress){ this.enableProgress = enableProgress; }
    public boolean getEnableProgress(){ return enableProgress; }

    private String fileType(String extension){
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getMimeTypeFromExtension(extension);
    }

    public void setSyncStatus(String status){ this.syncStatus = status; }
    public String getSyncStatus(){ return syncStatus; }
}
