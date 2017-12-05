package com.example.jongho.newproject_1;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by woong on 2017-12-05.
 */

public class getItem {


    private String point;
    private String title;
    private String content;
    private String gettime;
    private String location;
    // 사진 추가

    // 생성자
    public getItem() { }

    // lat, lng, title, content, time
    public getItem(String point, String title, String content,  String gettime) {
        this.point = point;
        this.title = title;
        this.content = content;
        this.gettime = gettime;
    }

    // 시간을 입력안했을 때 default로 현재 시각
    public getItem(String point, String title, String content) {
        this.point = point;
        this.title = title;
        this.content = content;
        this.gettime = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));

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

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }


}
