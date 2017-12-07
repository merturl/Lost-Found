package com.example.jongho.newproject_1;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by woong on 2017-12-05.
 */

public class getItem {


    private double lat;
    private double lng;
    private String title;
    private String content;
    private String gettime;
    private String location;
    private String uri;

    // 생성자
    public getItem() { }

//    // lat, lng, title, content, time
//    public getItem(double lat, double lng, String title, String content,  String gettime) {
//        this.lat = lat;
//        this.lng = lng;
//        this.title = title;
//        this.content = content;
//        this.gettime = gettime;
//    }

//    // 시간을 입력안했을 때 default로 현재 시각
//    public getItem(double lat, double lng, String title, String content) {
//        this.lat = lat;
//        this.lng = lng;
//        this.title = title;
//        this.content = content;
//        this.gettime = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
//    }

    // 시간을 입력안했을 때 default로 현재 시각
    public getItem(double lat, double lng, String title, String content, String uri) {
        this.lat = lat;
        this.lng = lng;
        this.title = title;
        this.content = content;
        this.gettime = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
        this.uri = uri;
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

    public String getGettime() {
        return gettime;
    }

    public void setGettime(String gettime) {
        this.gettime = gettime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
