package com.example.jongho.newproject_1;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
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
import com.google.firebase.database.FirebaseDatabase;

//MainAcritivity
public class MainActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    //PerMissionCode
    private static final int REQUEST_PERMISSIONS_LOCATION_SETTINGS_REQUEST_CODE = 33;
    private static final int REQUEST_PERMISSIONS_LAST_LOCATION_REQUEST_CODE = 34;
    private static final int REQUEST_PERMISSIONS_CURRENT_LOCATION_REQUEST_CODE = 35;
    //Google location API
    private FusedLocationProviderClient mFusedLocationClient;
    //Location Request Interval
    protected static long MIN_UPDATE_INTERVAL = 1 * 1000; // 1  minute is the minimum Android recommends, but we use 30 seconds

    //LocationRequest
    LocationRequest locationRequest;
    //LastLocation
    Location lastLocation = null;
    //currentLocation
    Location currentLocation = null;
    //GoogleMap
    GoogleMap googleMap = null;
    //Marker
    Marker currentMaker;

    // Firebase 객체 생성
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFireDB = FirebaseDatabase.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fuseLocationClient init
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
            }
            Log.d("haha","hshs");
            //currentLocations update time(interval time)
            mFusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    //if currentMaker is already exist then remove marker
                    if (currentMaker != null) {
                        currentMaker.remove();
                    }

                    currentLocation = locationResult.getLastLocation();
                    Log.d("haha","hshs"+  String.valueOf(currentLocation.getLatitude()) + String.valueOf(currentLocation.getLongitude()));
                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                    //current location add in googleMap
//                    resultTextView.setText(result);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Current Position"+latLng.latitude + "/" +latLng.longitude);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    currentMaker = googleMap.addMarker(markerOptions);

                    //Marker trace to camera
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }, Looper.myLooper());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {

        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            lastLocation = task.getResult();

                            if (currentMaker != null) {
                                currentMaker.remove();
                            }
                            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

//                    resultTextView.setText(result);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            markerOptions.title("Last Position");
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                            currentMaker = googleMap.addMarker(markerOptions);

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
        String[] permissions = new String[] {Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
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

    public void checkForLocationRequest(){
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

        // Click이벤트
        this.googleMap.setOnMapClickListener(this);
        this.googleMap.setOnMapLongClickListener(this);

        display();

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

    @Override
    public void onLocationChanged(Location location) {
        callCurrentLocation();
        Log.d("haha", "HHHH");
    }

    // Firebase 변화 수신
    private void display() {
        // getItem 수신
        mFireDB.getReference("getItem/"+mFirebaseAuth.getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {

                    // 리스트의 아이템을 검색하거나 아이템 추가가 있을 때 수신
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        getItem getitem = dataSnapshot.getValue(com.example.jongho.newproject_1.getItem.class);

                        // 구글맵에 마커 추가
                        MainActivity.this.googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(getitem.getLat(), getitem.getLng()))
                                .title(getitem.getTitle())
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_getitem)));
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

        // lostItem 수신
        mFireDB.getReference("lostItem/"+mFirebaseAuth.getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {

                    // 리스트의 아이템을 검색하거나 아이템 추가가 있을 때 수신
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        getItem getitem = dataSnapshot.getValue(com.example.jongho.newproject_1.getItem.class);

                        // 구글맵에 마커 추가
                        MainActivity.this.googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(getitem.getLat(), getitem.getLng()))
                                .title(getitem.getTitle())
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_lostitem)));
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

}


