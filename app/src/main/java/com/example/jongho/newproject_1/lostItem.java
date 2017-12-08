package com.example.jongho.newproject_1;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by woong on 2017-12-08.
 */

public class lostItem {
    private double lat;
    private double lng;
    private String title;
    private String content;
    private String losttime;
    private String uri;

    // 생성자
    public lostItem() { }

    // lat, lng, title, content, time
    public lostItem(double lat, double lng, String title, String content,  String losttime) {
        this.lat = lat;
        this.lng = lng;
        this.title = title;
        this.content = content;
        this.losttime = losttime;
    }

    // 시간을 입력안했을 때 default로 현재 시각
    public lostItem(double lat, double lng, String title, String content) {
        this.lat = lat;
        this.lng = lng;
        this.title = title;
        this.content = content;
        this.losttime = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
    }



    public String lostTitle() {
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

    public String getlosttime() {
        return losttime;
    }

    public void setlosttime(String losttime) {
        this.losttime = losttime;
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
}
