package com.kunsubin.pdfreader;

import java.util.Date;

public class Recently {
    private String mFileName;
    private String mPath;
    private Date mDate;
    
    public String getFileName() {
        return mFileName;
    }
    
    public void setFileName(String fileName) {
        mFileName = fileName;
    }
    
    public String getPath() {
        return mPath;
    }
    
    public void setPath(String path) {
        mPath = path;
    }
    
    public Date getDate() {
        return mDate;
    }
    
    public void setDate(Date date) {
        mDate = date;
    }
    
    @Override
    public String toString() {
        return "Recently{" +
               "mFileName='" + mFileName + '\'' +
               ", mPath='" + mPath + '\'' +
               ", mDate=" + mDate +
               '}';
    }
}
