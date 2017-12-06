package com.example.jongho.newproject_1;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by JongHo on 2017-12-06.
 */

public final class Constants {
    public static final HashMap<String, LatLng> zones = new HashMap<String, LatLng>();
    static {
        zones.put("Mju Univ", new LatLng(37.222206, 127.1875443));
    }
}
