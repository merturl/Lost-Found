package com.example.jongho.newproject_1;

import android.Manifest;
import android.app.LoaderManager;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

//MainAcritivity
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    //PerMissionCode
    private static final int REQUEST_PERMISSIONS_LOCATION_SETTINGS_REQUEST_CODE = 33;
    private static final int REQUEST_PERMISSIONS_LAST_LOCATION_REQUEST_CODE = 34;
    private static final int REQUEST_PERMISSIONS_CURRENT_LOCATION_REQUEST_CODE = 35;
    private static final int REQUEST_PERMISSIONS_GEOFENCE_REQUEST_CODE = 36;
    private static final int CIRCLE_BOUND = 50;
    //Google location API
    private FusedLocationProviderClient mFusedLocationClient;
    //Location Request Interval
    protected static long MIN_UPDATE_INTERVAL = 1 * 1000; // 1  minute is the minimum Android recommends, but we use 30 seconds
    private GeofencingClient geofencingClient;

    //LocationRequest
    LocationRequest locationRequest;
    //LastLocation
    Location lastLocation = null;
    //currentLocation
    Location currentLocation = null;
    //GoogleMap
    GoogleMap googleMap = null;
    //Marker
    Marker currentMarker;
    Marker addMarker;

    List<Zone> zonelist = new ArrayList<>();
    protected ArrayList<Geofence> mGeofenceList;

    Circle mapCircle;

    PendingIntent pendingIntent = null;

    // Firebase 객체 생성
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFireDB = FirebaseDatabase.getInstance();

    // If Activity exit
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("plz", "oncreate");

        display();

        //fuseLocationClient init
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);

        //checkforlocation
        checkForLocationRequest();
        checkForLocationSettings();


        //Init googleMap
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //null handlering
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (result != ConnectionResult.SUCCESS && result != ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            Toast.makeText(this, "Are you running in Emulator ? try a real device.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        callCurrentLocation();

    }

    public void callLastKnownLocation() {
        try {
            if (
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                requestPermissions(REQUEST_PERMISSIONS_LAST_LOCATION_REQUEST_CODE);
                return;
            }
            // if permission is granted => cant get LastLocation
            getLastLocation();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void callCurrentLocation() {
        try {
            if (
                //permission Check
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    ) {

                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.


                //Permissions request to user
                requestPermissions(REQUEST_PERMISSIONS_CURRENT_LOCATION_REQUEST_CODE);
                return;
            } else {
                Log.d("haha", "hshs");
                //currentLocations update time(interval time)
                mFusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        //if currentMarker is already exist then remove marker
                        if (currentMarker != null) {
                            currentMarker.remove();
                        }
                        if (mapCircle != null) {
                            mapCircle.remove();
                        }

                        currentLocation = locationResult.getLastLocation();
                        Log.d("haha", "hshs" + String.valueOf(currentLocation.getLatitude()) + String.valueOf(currentLocation.getLongitude()));
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                        //current location add in googleMap
//                    resultTextView.setText(result);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title("Current Position" + latLng.latitude + "/" + latLng.longitude);
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                        currentMarker = googleMap.addMarker(markerOptions);



                        //Marker trace to camera
                        mapCircle = googleMap.addCircle(new CircleOptions().center(latLng).radius(CIRCLE_BOUND).strokeColor(Color.parseColor("#884169e1")).fillColor(Color.parseColor("#5587cefa")));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    }
                }, Looper.myLooper());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        display();
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            lastLocation = task.getResult();

                            if (currentMarker != null) {
                                currentMarker.remove();
                            }

                            if (lastLocation != null) {
                                Log.d("haha", "addgeofence" + lastLocation.getLongitude()+ "+" +lastLocation.getLatitude());
                                geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent()).addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this, "SuccessAddGeofence", Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(MainActivity.this, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "failaddGeofence", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

//                    resultTextView.setText(result);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            markerOptions.title("Last Position");
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                            currentMarker = googleMap.addMarker(markerOptions);

                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        } else {
                            showSnackbar();
                        }
                    }
                });
    }

    //Toast msg
    private void showSnackbar() {
        View container = findViewById(R.id.container);
        if (container != null) {
            Snackbar.make(container, "No Last known location found. Try current location..!", Snackbar.LENGTH_LONG).show();
        }
    }

    //Toast msg
    private void showSnackbar(View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                "Permission is must to find the location",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok", listener).show();
    }

    private void startLocationPermissionRequest(int requestCode) {
        String[] permissions = new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(MainActivity.this, permissions, requestCode);
    }

    private void requestPermissions(final int requestCode) {
//        Check if user has denied
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            showSnackbar(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest(requestCode);
                        }
                    });

        } else {
            startLocationPermissionRequest(requestCode);
        }
    }

    public void checkForLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(MIN_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //Check for location settings.
    public void checkForLocationSettings() {
        try {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.addLocationRequest(locationRequest);
            SettingsClient settingsClient = LocationServices.getSettingsClient(MainActivity.this);

            settingsClient.checkLocationSettings(builder.build())
                    .addOnSuccessListener(MainActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            //Setting is success...
                            Toast.makeText(MainActivity.this, "Enabled the Location successfully. Now you can press the buttons..", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(MainActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {


                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                                    try {
                                        // Show the dialog by calling startResolutionForResult(), and check the
                                        // result in onActivityResult().
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(MainActivity.this, REQUEST_PERMISSIONS_LOCATION_SETTINGS_REQUEST_CODE);
                                    } catch (IntentSender.SendIntentException sie) {
                                        sie.printStackTrace();
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    Toast.makeText(MainActivity.this, "Setting change is not available.Try in another device.", Toast.LENGTH_LONG).show();
                            }

                        }
                    });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_LAST_LOCATION_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation();
            }
        }

        if (requestCode == REQUEST_PERMISSIONS_CURRENT_LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callCurrentLocation();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Log.d("haha", "onMpaReady");

        callLastKnownLocation();
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
//        this.googleMap.setLatLngBoundsForCameraTarget(MJU_BOUND);

        // 마커 클릭 이벤트
        try{
            this.googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    HashMap<String, Object> tag;
                    tag = (HashMap) marker.getTag();
//                Toast.makeText(MainActivity.this, tag.get("ImageRef").toString() , Toast.LENGTH_LONG).show();
                    Intent ItemView = new Intent(MainActivity.this, ItemViewActivity.class);
                    ItemView.putExtra("DbRef", tag.get("DbRef").toString());
                    ItemView.putExtra("ImageRef", tag.get("ImageRef").toString());
                    startActivity(ItemView);
                    return true;
                }
            });
        } catch ( NullPointerException e ) {
            // 본인 마커 찍었을 때 예외처리
            Toast.makeText(this, "Sorry, Click other point", Toast.LENGTH_SHORT).show();
        }


        this.googleMap.setOnMapClickListener(this);
        this.googleMap.setOnMapLongClickListener(this);



    }

    @Override
    public void onMapClick(LatLng latLng) {
        Point clickPoint = this.googleMap.getProjection().toScreenLocation(latLng);
        LatLng point = this.googleMap.getProjection().fromScreenLocation(clickPoint);
        Toast.makeText(this, "Click Point Lat : " + point.latitude + " Lon : " + point.longitude, Toast.LENGTH_LONG).show();

        // getItemActivity 전환 인텐트
        Intent intent = new Intent(this, TypeActivity.class);
        intent.putExtra("lat", point.latitude);
        intent.putExtra("lng", point.longitude);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_left);

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    // 초기 데이터 수신
    private void initdisplay() {
        Toast.makeText(this, "initDB", Toast.LENGTH_LONG).show();
        DatabaseReference mRef = mFireDB.getReference("getItem/"+mFirebaseAuth.getCurrentUser().getUid());

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Toast.makeText(MainActivity.this, "hi data", Toast.LENGTH_LONG).show();
                Item getitem = dataSnapshot.getValue(Item.class);

                // currentMarker와의 거리 구하기
                Location locationMarker = new Location("marker");
                Location current = new Location("current");

                locationMarker.setLatitude(getitem.getLat());
                locationMarker.setLongitude(getitem.getLng());
                current.setLatitude(currentMarker.getPosition().latitude);
                current.setLongitude(currentMarker.getPosition().longitude);

                float distance = locationMarker.distanceTo(current);    // m 단위

                Zone itemzone = new Zone();
                itemzone.setRef("getItem/"+mFirebaseAuth.getCurrentUser().getUid()+"/"+dataSnapshot.getKey());
                itemzone.setLatlng(new LatLng(getitem.getLat(), getitem.getLng()));
                itemzone.setDistance(distance);
                zonelist.add(itemzone);

                for(Zone zoneitem : zonelist ) {
                    Toast.makeText(MainActivity.this, "zoneitem Distance ==="+ zoneitem.getDistance(), Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // Firebase 변화 수신
    private void display() {
        Log.i(TAG, "display");
        // getItem 수신
        mFireDB.getReference("getItem/"+mFirebaseAuth.getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {
                    // 리스트의 아이템을 검색하거나 아이템 추가가 있을 때 수신
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        // 아이템 받아오기
                        Item getitem = dataSnapshot.getValue(Item.class);

                        // 구글맵에 마커 추가
                        addMarker = MainActivity.this.googleMap.addMarker(new MarkerOptions()
                                
                                .position(new LatLng(getitem.getLat(), getitem.getLng()))
                                .title(getitem.getTitle())
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_getitem)));

                        // currentMarker와의 거리 구하기
                        Location locationMarker = new Location("marker");
                        Location current = new Location("current");

                        locationMarker.setLatitude(getitem.getLat());
                        locationMarker.setLongitude(getitem.getLng());
                        current.setLatitude(currentMarker.getPosition().latitude);
                        current.setLongitude(currentMarker.getPosition().longitude);

                        float distance = locationMarker.distanceTo(current);    // m 단위

                        // 마커에 달 태그
                        HashMap<String, Object> tag = new HashMap<String, Object>();
                        tag.put("DbRef", "getItem/"+mFirebaseAuth.getCurrentUser().getUid()+"/"+dataSnapshot.getKey());
                        tag.put("ImageRef", "Item/image/"+mFirebaseAuth.getCurrentUser().getUid()+"/"+dataSnapshot.getKey());
                        tag.put("distance", distance);

                        addMarker.setTag(tag);

                        Zone itemzone = new Zone();
                        itemzone.setRef("getItem/"+mFirebaseAuth.getCurrentUser().getUid()+"/"+dataSnapshot.getKey());
                        itemzone.setLatlng(new LatLng(getitem.getLat(), getitem.getLng()));
                        itemzone.setDistance(distance);
                        zonelist.add(itemzone);
                        callbacks.onLoadFinished(zonelist);

                        for(Zone zoneitem : zonelist ) {
                            Toast.makeText(MainActivity.this, "zoneitem Distance ==="+ zoneitem.getDistance(), Toast.LENGTH_SHORT).show();
                        }
//                        Toast.makeText(MainActivity.this, "getItem/"+mFirebaseAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();


//                        Collections.sort(zonelist, new CompareDistanceAsc());
//                        for( Zone item : zonelist ) {
////                            Toast.makeText(MainActivity.this, item.getRef(), Toast.LENGTH_SHORT).show();
//                        }
                    }

                    // 아이템 변화가 있을 때 수신
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                    // 아이템이 삭제 되었을 때 수신
                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {}
                    // 순서가 있는 리스트에서 순서가 변경 되었을 때 수신
                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

        // lostItem 수신
        mFireDB.getReference("lostItem/"+mFirebaseAuth.getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {

                    // 리스트의 아이템을 검색하거나 아이템 추가가 있을 때 수신
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Item getitem = dataSnapshot.getValue(Item.class);

                        // 구글맵에 마커 추가
                        addMarker= MainActivity.this.googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(getitem.getLat(), getitem.getLng()))
                                .title(getitem.getTitle())
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_lostitem)));

                        // currentMarker와의 거리 구하기
                        Location locationMarker = new Location("marker");
                        Location current = new Location("current");

                        locationMarker.setLatitude(getitem.getLat());
                        locationMarker.setLongitude(getitem.getLng());
                        current.setLatitude(currentMarker.getPosition().latitude);
                        current.setLongitude(currentMarker.getPosition().longitude);

                        float distance = locationMarker.distanceTo(current);    // m 단위
                        Toast.makeText(MainActivity.this, "distance="+ distance , Toast.LENGTH_SHORT).show();
                        HashMap<String, Object> tag = new HashMap<String, Object>();
                        tag.put("DbRef", "lostItem/"+mFirebaseAuth.getCurrentUser().getUid()+"/"+dataSnapshot.getKey());
                        tag.put("ImageRef", "Item/image/"+mFirebaseAuth.getCurrentUser().getUid()+"/"+dataSnapshot.getKey());
                        tag.put("distance", distance);
                        addMarker.setTag(tag);


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
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(this, "Are you exit?", Toast.LENGTH_SHORT).show();
        }
    }

    private Geofence getGeofence(){
        //지오 펜스 생성
        Geofence geofence = new Geofence.Builder()
                .setRequestId("User")
                .setCircularRegion(lastLocation.getLatitude(), lastLocation.getLongitude(), CIRCLE_BOUND)
                .setExpirationDuration(360000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT).build();    // 지오펜스 발생 시점
        Toast.makeText(this, "region"+ lastLocation.getLatitude(), Toast.LENGTH_SHORT).show();
//        display();

        return geofence;
    }

    // GooglePlayService에 지오 펜스 요청
    private GeofencingRequest getGeofencingRequest() {


        mGeofenceList = new ArrayList<Geofence>();
        // 100m 이내 마커들
//        Toast.makeText(this, zonelist, Toast.LENGTH_SHORT).show();

        for( Zone item : zonelist ) {
            Toast.makeText(this,"zoneitem======="+ item.getLatlng().longitude, Toast.LENGTH_SHORT).show();
            Toast.makeText(this,"zoneitem======="+ item.getDistance(), Toast.LENGTH_SHORT).show();

//                mGeofenceList.add(new Geofence.Builder()
//                        .setRequestId(item.getRef())
//                        .setCircularRegion(
//                                item.getLatlng().latitude,
//                                item.getLatlng().longitude,
//                                CIRCLE_BOUND
//                        )
//                        .setExpirationDuration(360000)
//                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
//                        .build());
        }


        mGeofenceList.add(new Geofence.Builder()
                .setRequestId("User")
                .setCircularRegion(lastLocation.getLatitude(), lastLocation.getLongitude(), CIRCLE_BOUND)
                .setExpirationDuration(360000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT).build());    // 지오펜스 발생 시점

//        Geofencing ReturnCollections.sort(zonelist, new CompareDistanceAsc());
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_DWELL);
//        builder.addGeofence(getGeofence());
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (pendingIntent != null) {
            return pendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    // distance로 오름차순 정렬
    static class CompareDistanceAsc implements Comparator<Zone> {
        @Override
        public int compare(Zone z1, Zone z2) {
            return z1.getDistance() < z2.getDistance() ? -1 : z1.getDistance() > z2.getDistance() ? 1:0 ;
        }
    }
}


