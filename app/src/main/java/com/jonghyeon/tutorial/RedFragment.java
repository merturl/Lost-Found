package com.jonghyeon.tutorial;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by JongHo on 2017-11-15.
 */

public class RedFragment extends Fragment implements OnMapReadyCallback, LocationListener{
    private final LatLng MJU_CENTER = new LatLng(37.220940, 127.186750);    // 카메라를 명지대 중심에 놓기 위한 불변의 좌표
    private GoogleMap googleMap = null;
    private MapView mapView = null;
//    private GPS_Info gps_info;
    Context context;

    //생성자
    public RedFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Fragment에서 onCreate와 같은 부분
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_red, container, false);

        MapsInitializer.initialize(getActivity().getApplicationContext());

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.getMapAsync(this);

//        gps_info = new GPS_Info(getActivity().getApplicationContext());
//        // 현재위치를 표시함
//        if(gps_info.isGetLocation()) {
//            double lat = gps_info.getLatitude();
//            double lon = gps_info.getLongitude();
//
//            LatLng current_position = new LatLng(lat, lon);
//            this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(current_position));
//            this.googleMap.addMarker(new MarkerOptions().position(current_position).title("ME"));
//        }

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // 나침반이 보이게 설정
        this.googleMap.getUiSettings().setCompassEnabled(true);

        // 지도에 명지대의 범위만 보여주기 위해 결정한 범위
        LatLngBounds MJU_BOUND = new LatLngBounds(new LatLng(37.2172, 127.180), new LatLng(37.2245, 127.1919));

        // 가게들의 좌표를 설정하고 마커를 붙임
        LatLng FIRST_SHOP = new LatLng(37.221120, 127.188593);
        LatLng SECOND_SHOP = new LatLng(37.219210, 127.182930);
        LatLng THIRD_SHOP = new LatLng(37.221940, 127.187650);
        this.googleMap.addMarker(new MarkerOptions().position(FIRST_SHOP).title("함박관 상점"));
        this.googleMap.addMarker(new MarkerOptions().position(SECOND_SHOP).title("3공학관 상점"));
        this.googleMap.addMarker(new MarkerOptions().position(THIRD_SHOP).title("5공학관 상점"));


        // 마커 클릭 이벤트
        this.googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                Toast.makeText(getContext(), marker.getPosition().toString(), Toast.LENGTH_LONG).show();
                Intent inventory = new Intent(getActivity(), Inventory.class);
                inventory.putExtra("MarkerPosition",marker.getPosition().toString());
                startActivity(inventory);
                return true;
            }
        });


        this.googleMap.addCircle(new CircleOptions().center(FIRST_SHOP).radius(25.0f).strokeColor(Color.parseColor("#884169e1")).fillColor(Color.parseColor("#5587cefa")));
        this.googleMap.addCircle(new CircleOptions().center(SECOND_SHOP).radius(20.0f).strokeColor(Color.parseColor("#884169e1")).fillColor(Color.parseColor("#5587cefa")));
        this.googleMap.addCircle(new CircleOptions().center(THIRD_SHOP).radius(20.0f).strokeColor(Color.parseColor("#884169e1")).fillColor(Color.parseColor("#5587cefa")));

        // Camera
        this.googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        this.googleMap.animateCamera(CameraUpdateFactory.zoomOut());
        this.googleMap.setMinZoomPreference(17.0f);
        this.googleMap.setMaxZoomPreference(18.0f);
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(MJU_CENTER));
        this.googleMap.setLatLngBoundsForCameraTarget(MJU_BOUND);
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}