package com.example.ipserver;

public class FileDataStructure {
    private String fileName;
    private boolean enableProgress;
    private int maxProgress = 0, progress = 0;

    public FileDataStructure(String fileName){
        this.fileName = fileName;
    }

    public void setFileName(String s){ this.fileName = s; }
    public String getFileName(){ return fileName; }
    public String toString(){ return fileName; }

    public void setMaxProgress(int max){ maxProgress = max; }
    public int getMaxProgress(){ return maxProgress; }

    public void setProgress(int progress){ this.progress = progress; }
    public int getProgress(){ return progress; }

    public void EnableProgress(boolean enableProgress){ this.enableProgress = enableProgress; }
    public boolean getEnableProgress(){ return enableProgress; }
}
