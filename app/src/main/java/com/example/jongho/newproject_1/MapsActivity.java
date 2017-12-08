package com.example.jongho.newproject_1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, ResultCallback<Status> {
    private GoogleApiClient mGoogleApiClient;
    protected ArrayList<Geofence> mGeofenceList;
    private  GoogleMap googleMap;
    private Location mLocation = null;
    private final int scope = 10000;
    private String TAG = "googlemap";

    // Firebase 객체 생성
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFireDB = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected() == false){
            Log.d(TAG, "onStart: mGoogleApiClient connect");
            mGoogleApiClient.connect();
        }
        super.onStart();
    }
    

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // 지도에 명지대의 범위만 보여주기 위해 결정한 범위
        LatLngBounds MJU_BOUND = new LatLngBounds(new LatLng(37.2172, 127.180), new LatLng(37.2245, 127.1919));

        // 나침반이 보이게 설정
        this.googleMap.getUiSettings().setCompassEnabled(true);
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        this.googleMap.getUiSettings().setMapToolbarEnabled(true);

        // Camera
        this.googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        this.googleMap.animateCamera(CameraUpdateFactory.zoomOut());
        this.googleMap.setMinZoomPreference(17.0f);
        this.googleMap.setMaxZoomPreference(18.0f);
        this.googleMap.setLatLngBoundsForCameraTarget(MJU_BOUND);

        // Click이벤트
        this.googleMap.setOnMapClickListener(this);
        this.googleMap.setOnMapLongClickListener(this);

        display();
    }


    @Override
    protected void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        super.onStop();
    }


    public void pressGeofence(View view) {
        String[] permissions = new String[] {Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            for(String permission:permissions) {
                int result = PermissionChecker.checkSelfPermission(this, permission);
                if(result == PermissionChecker.PERMISSION_GRANTED) ;
                else {
                    ActivityCompat.requestPermissions(this, permissions, 1);
                }
            }
        }else{

            LocationRequest locreq = new LocationRequest();
            locreq.setInterval(5000);
            locreq.setFastestInterval(4000);
            locreq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locreq, this);

            mGeofenceList = new ArrayList<Geofence>();
            for(Map.Entry<String, LatLng> entry : Constants.zones.entrySet()) {
                mGeofenceList.add(new Geofence.Builder().setRequestId(entry.getKey()).setCircularRegion(entry.getValue().latitude, entry.getValue().longitude, 5000).setExpirationDuration(360000).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT).build());

                Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
                PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
                builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
                builder.addGeofences(mGeofenceList);
                GeofencingRequest georeq = builder.build();

                try {
                    LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, georeq, pendingIntent).setResultCallback(this);
                } catch (SecurityException s) {

                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationRequest request = new LocationRequest();
                    request.setInterval(3000);
                    request.setFastestInterval(3000);
                    request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    public void pressGps(View view) {
        Log.d("Loation", "pressgps1");
        String[] permissions = new String[] {Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            for(String permission:permissions) {
                int result = PermissionChecker.checkSelfPermission(this, permission);
                if(result == PermissionChecker.PERMISSION_GRANTED) {
                    Log.d("Loation", "pressgps3");
                    LocationRequest request = new LocationRequest();
                    request.setInterval(3000);
                    request.setFastestInterval(3000);
                    request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
                }
                else {
                    ActivityCompat.requestPermissions(this, permissions, 1);
                }
            }
        }else {
            Log.d("Loation", "pressgps2");
            LocationRequest request = new LocationRequest();
            request.setInterval(3000);
            request.setFastestInterval(3000);
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
        }
    }

    public void pressEnd(View view) {
        try {
            Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, pendingIntent).setResultCallback(this);
        }
        catch (SecurityException s) {

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.mLocation = location;
        Log.e("Location",  "Lat : " + mLocation.getLatitude() + ", Lon : " + mLocation.getLongitude());
        this.googleMap.clear();
        MarkerOptions currentMarker = new MarkerOptions();
        currentMarker.position(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())).title("Current Position!")
                .title("내위치")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_announcement));
        this.googleMap.addMarker(currentMarker);
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentMarker.getPosition()));
    }
    // 맵 클릭 이벤트
    @Override
    public void onMapClick(LatLng latLng) {
        Point clickPoint = this.googleMap.getProjection().toScreenLocation(latLng);
        LatLng point = this.googleMap.getProjection().fromScreenLocation(clickPoint);

//        Toast.makeText(this, "Click Point Lat : " + point.latitude + " Lon : " + point.longitude, Toast.LENGTH_LONG).show();

        // getItemActivity 전환 인텐트
        Intent intent = new Intent(this, TypeActivity.class);
        intent.putExtra("lat", point.latitude);
        intent.putExtra("lng", point.longitude);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_left);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Toast.makeText(this, "LongClick", Toast.LENGTH_LONG).show();
    }

    // Firebase 변화 수신
    private void display() {
        mFireDB.getReference("getItem/"+mFirebaseAuth.getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {

                    // 리스트의 아이템을 검색하거나 아이템 추가가 있을 때 수신
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        getItem getitem = dataSnapshot.getValue(com.example.jongho.newproject_1.getItem.class);

                        // 구글맵에 마커 추가
                        MapsActivity.this.googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(getitem.getLat(), getitem.getLng()))
                                .title(getitem.getTitle())
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_announcement)));

//                        Toast.makeText(MapsActivity.this, getitem.getTitle(), Toast.LENGTH_SHORT).show();
                    }

                    // 아이템 변화가 있을 때 수신
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    // 아이템이 삭제 되었을 때 수신
                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    // 순서가 있는 리스트에서 순서가 변경 되었을 때 수신
                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onResult(@NonNull Status status) {

    }
}
