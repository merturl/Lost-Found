package com.jonghyeon.tutorial;

import android.graphics.drawable.Drawable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by woong on 2017-12-01.
 */

public class StoreItem extends ListViewAdapter{

    // uid, 사진, 내용
    private Drawable icon;
    private String msg;
    long now = System.currentTimeMillis();
    Date date = new Date(now);
    SimpleDateFormat datetype = new SimpleDateFormat("yyyy-MM-dd");
    String lostTime = datetype.format(date);



    public StoreItem() {}

    public StoreItem(String msg){
        this.msg = msg;
    }


    public String getmsg() {
        return msg;
    }

    public void setmsg(String msg) {
        this.msg = msg;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getLostTime() {
        return lostTime;
    }

    public void setLostTime(String lostTime) {
        this.lostTime = lostTime;
    }


}
