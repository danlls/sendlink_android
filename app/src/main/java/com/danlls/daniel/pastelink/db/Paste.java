package com.danlls.daniel.pastelink.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;

/**
 * Created by danieL on 1/22/2018.
 */

@Entity
public class Paste {

    @PrimaryKey(autoGenerate = true)
    private int pid;

    @ColumnInfo(name = "paste_string")
    private String pasteString;

    @ColumnInfo(name = "received_time")
    @TypeConverters(DateConverter.class)
    private Date receivedTime;

    @ColumnInfo(name = "device_name")
    private String deviceName;

    public Paste(String pasteString, Date receivedTime, String deviceName){
        this.pasteString = pasteString;
        this.receivedTime = receivedTime;
        this.deviceName = deviceName;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getPasteString(){
        return pasteString;
    }

    public Date getReceivedTime() {
        return receivedTime;
    }

    public String getDeviceName() {
        return deviceName;
    }
}


