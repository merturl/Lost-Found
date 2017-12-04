package com.jonghyeon.tutorial;

import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by woong on 2017-12-01.
 */

public class StoreItem extends ListViewAdapter{



    // uid, 사진, 내용
    private Drawable icon;
    private String msg;
    private String lostTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
    private String location;



    public StoreItem() {}

    public StoreItem(String msg, String location){
        this.msg = msg;
        this.location = location;
    }


    public String getmsg() {
        return msg;
    }

    public void setmsg(String msg) {
        this.msg = msg;
    }



    public String getLostTime() {
        return lostTime;
    }

    public void setLostTime(String lostTime) {
        this.lostTime = lostTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}
