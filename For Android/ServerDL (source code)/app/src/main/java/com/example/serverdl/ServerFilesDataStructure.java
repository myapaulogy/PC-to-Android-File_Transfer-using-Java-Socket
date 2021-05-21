package com.example.serverdl;

import android.webkit.MimeTypeMap;

public class ServerFilesDataStructure {
    private final String fileName;
    private final String filetype;
    private String mimeType = null;
    private String status;
    private boolean enableProgress;
    private int maxProgress = 0, progress = 0;

    public ServerFilesDataStructure(String fileName, String type){
        this.fileName = fileName;
        this.filetype = type;

        // Get file MIME type
        if(type.equals("File")){
            String[] split = fileName.split("\\.");
            this.mimeType = MimeTypeMap.getSingleton().
                    getMimeTypeFromExtension(split[split.length - 1]);
        }
    }

    public String getFileName(){ return fileName; }
    public String getType(){ return filetype; }
    public String getMimeTypeMap(){ return mimeType; }

    public void setMaxProgress(int max){ maxProgress = max; }
    public int getMaxProgress(){ return maxProgress; }

    public void setProgress(int progress){ this.progress = progress; }
    public int getProgress(){ return progress; }

    public void setEnableProgress(boolean enableProgress){ this.enableProgress = enableProgress; }
    public boolean getEnableProgress(){ return enableProgress; }

    public void setSyncStatus(String status){ this.status = status; }
    public String getSyncStatus(){ return status; }
}
