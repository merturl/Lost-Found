package com.example.jongho.newproject_1;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by woong on 2017-12-05.
 */

public class Item {


    private double lat;
    private double lng;
    private String title;
    private String content;
    private String time;
    private boolean type;   // getItem: T, lostItem: F

    // 생성자
    public Item() { }

    // lat, lng, title, content, time
    public Item(boolean type, double lat, double lng, String title, String content, String time) {
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.title = title;
        this.content = content;
        this.time = time;
    }

    // 시간을 입력안했을 때 default로 현재 시각
    public Item(boolean type, double lat, double lng, String title, String content) {
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.title = title;
        this.content = content;
        this.time = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public boolean getType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }
}
