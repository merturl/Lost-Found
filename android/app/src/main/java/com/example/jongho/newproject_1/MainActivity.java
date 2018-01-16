package com.example.jongho.newproject_1;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

//MainAcritivity
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    //PerMissionCode
    private static final int REQUEST_PERMISSIONS_LOCATION_SETTINGS_REQUEST_CODE = 33;
    private static final int REQUEST_PERMISSIONS_LAST_LOCATION_REQUEST_CODE = 34;
    private static final int REQUEST_PERMISSIONS_CURRENT_LOCATION_REQUEST_CODE = 35;
    private static final int REQUEST_PERMISSIONS_MYLOCATION_CODE = 36;
    private static final int CIRCLE_BOUND = 50;
    //Google location API
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    //Location Request Interval
    protected static long MIN_UPDATE_INTERVAL = 10 * 1000; // 1  minute is the minimum Android recommends, but we use 30 seconds
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

    private TextView addr;

    public static SQLiteDatabase mDB;
    private Cursor mCursor;
    private ContentValues v = new ContentValues();


    protected ArrayList<Geofence> mGeofenceList;
    static List<Circle> mCircleList;

    Circle mapCircle;

    PendingIntent pendingIntent = null;

    // Firebase 객체 생성
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFireDB = FirebaseDatabase.getInstance();

    // If Activity exit
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    List<Zone> zonelist;


    //For search activity
    private Intent search;

    //For ContractsActivity
    private Intent contracts;
    private Intent tutorial;

    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Log.i("haha", "oncreate");

            //init SearchAddress data record
            //fuseLocationClient init
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            geofencingClient = LocationServices.getGeofencingClient(this);
            mGeofenceList = new ArrayList<Geofence>();
            search = new Intent(this, SearchActivity.class);
            contracts = new Intent(this, ContractsActivity.class);
            tutorial = new Intent(this, TutorialActivity.class);

            //checkforlocation
            checkForLocationRequest();
            checkForLocationSettings();
            createLocationCallBack();

        initDB();


        //Init googleMap
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info = am.getRunningTasks(1);

        switch (item.getItemId()) {
            case R.id.contractActivity:
                startActivity(contracts);
                return true;
            case R.id.tutorialActivitiy:
                startActivity(tutorial);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void initDB() {
        addr = (TextView) findViewById(R.id.addr);
        Button tran = (Button) findViewById(R.id.tran);

        // DB를 위한 부분
        FeedReaderDbHelper mHendler = new FeedReaderDbHelper(this);
        mDB = mHendler.getWritableDatabase();
        mHendler.onCreate(mDB);

        final Geocoder geo = new Geocoder(this);

        tran.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFusedLocationClient != null) {
                    mFusedLocationClient.removeLocationUpdates(locationCallback).addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFusedLocationClient = null;
                        }
                    });
                }
                startActivityForResult(search, 0);
            }
        });

//        tran.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                List<Address> list = null;
//                String str = addr.getText().toString();
//
//                if(mBound){
//                    try{
//                        Toast.makeText(getApplicationContext(), mService.add(11,22)+","+ mService.sub(5,9),Toast.LENGTH_SHORT).show();
//                        Log.i("haha", "/"+mService.add(11,22));
//                    }catch (RemoteException e){
//
//                    }
//                }
//
//                //Stop My currentLocation when users search location to use Address
//                if (mFusedLocationClient != null) {
//                    mFusedLocationClient.removeLocationUpdates(locationCallback).addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            mFusedLocationClient = null;
//                        }
//                    });
//                }
//
//                try {
//                    list = geo.getFromLocationName(str, 10);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.e("HAHA", "서버에서 주소 못찾음");
//                }
//
//                if (list != null) {
//                    if (list.size() == 0) {
//                        Toast.makeText(MainActivity.this, "해당 주소정보 없음", Toast.LENGTH_SHORT).show();
//                    } else {
//
//                        Toast.makeText(MainActivity.this, String.valueOf(list.get(0).getLatitude()) + ", " + String.valueOf(list.get(0).getLongitude()), Toast.LENGTH_LONG).show();
//                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(list.get(0).getLatitude(), list.get(0).getLongitude())));
//                        v.put("addr", addr.getText().toString());
//                        v.put("lat", list.get(0).getLatitude());
//                        v.put("lon", list.get(0).getLongitude());
//                        mDB.insert("my_Geo", null, v);
//                        loadSearchRecord();
//                    }
//                }
//            }
//        });
    }

    @Override
    protected void onActivityResult(int req, int res, Intent intent) {
        super.onActivityResult(req, res, intent);
        if(req == 0) {
            if(res == Activity.RESULT_OK) {
                Double lat = intent.getDoubleExtra("lat", 0.0);
                Double lon = intent.getDoubleExtra("lon", 0.0);
                String addr = intent.getStringExtra("addr");
                this.addr.setText(addr);
                //Stop My currentLocation when users search location to use Address
                if (mFusedLocationClient != null) {
                    mFusedLocationClient.removeLocationUpdates(locationCallback).addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFusedLocationClient = null;
                        }
                    });
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
                v.put("addr", this.addr.getText().toString());
                v.put("lat", lat);
                v.put("lon", lon);
                mDB.insert("my_Geo", null, v);
                loadSearchRecord();
            }
            else if(res == 444) {
                Double lat = Double.valueOf(intent.getStringExtra("lat"));
                Double lon = Double.valueOf(intent.getStringExtra("lon"));
                String addr = intent.getStringExtra("addr");
                this.addr.setText(addr);

                //Stop My currentLocation when users search location to use Address
                if (mFusedLocationClient != null) {
                    mFusedLocationClient.removeLocationUpdates(locationCallback).addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFusedLocationClient = null;
                        }
                    });
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("haha", "resume");
        //null handlering
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (result != ConnectionResult.SUCCESS && result != ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            Toast.makeText(this, "Are you running in Emulator ? try a real device.", Toast.LENGTH_SHORT).show();
        }

    }

    private void loadSearchRecord(){
        mCursor = mDB.query("my_Geo", new String[]{"addr", "lat", "lon"}, null, null, null, null, "_id");
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    Log.e("HEY", mCursor.getString(0) + ", " + mCursor.getString(1) + ", " + mCursor.getString(2));
                } while (mCursor.moveToNext());
            }
        }
    }

    @Override
    public void onStart() {

        Log.d("haha", "onStart");

        callLastKnownLocation();
        super.onStart();
    }

    public void callLastKnownLocation() {
        Log.i("haha", "callLast");
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

    private void createLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //if currentMarker is already exist then remove marker
                if (currentMarker != null) {
                    currentMarker.remove();
                }

                currentLocation = locationResult.getLastLocation();
                Log.d("haha", "hshs" + String.valueOf(currentLocation.getLatitude()) + String.valueOf(currentLocation.getLongitude()));
                LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                //current location add in googleMap
//                    resultTextView.setText(result);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position" + latLng.latitude + "/" + latLng.longitude);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_redmarker));
                googleMap.clear();
                deleteCircles();
                currentMarker = googleMap.addMarker(markerOptions);
                display();
//                display();
                //Marker trace to camera
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        };
    }

    public void callCurrentLocation() {
        Log.d("haha", "callcurrent");
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
                //currentLocations update time(interval time)
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
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

                            if (currentMarker != null) {
                                currentMarker.remove();
                            }

                            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            markerOptions.title("Last Position");
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_redmarker));
                            currentMarker = googleMap.addMarker(markerOptions);
                            Log.d("haha", "lastcurrent" + currentMarker);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            callCurrentLocation();
                        } else {
                            showSnackbar();
//                            callCurrentLocation();
                        }
                    }
                });
//        if(currentLocation  == null){
//
//        }
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
                Log.d("haha", "permisson");
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
        // 마커 클릭 이벤트
        this.googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d("haha","asdfsf");
                if( marker.getTag() == null ) {
                    Toast.makeText(MainActivity.this, "Sorry, pick other point", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    HashMap<String, Object> tag;
                    tag = (HashMap) marker.getTag();

//                    //테스트용 to ItemViewActivity
//                    Intent ItemView = new Intent(MainActivity.this, ItemViewActivity.class);
//                    ItemView.putExtra("DbRef", tag.get("DbRef").toString());
//                    ItemView.putExtra("ImageRef", tag.get("ImageRef").toString());
//                    ItemView.putExtra("Uid", tag.get("Uid").toString());
//                    ItemView.putExtra("MarkerId", tag.get("MarkerId").toString());
//                    Log.d("acac", "Uid="+tag.get("Uid").toString());
//                    Log.d("acac", "MarkerId="+tag.get("MarkerId").toString());
//                    startActivity(ItemView);
//                    return true;

                    // 내가 생성한 마커면 수정, 아니면 읽기만 가능
                    Log.d("uid", "uid=="+tag.get("Uid"));
                    Log.d("uid", "muid=="+mFirebaseAuth.getCurrentUser().getUid());
                    if(tag.get("Uid") == mFirebaseAuth.getCurrentUser().getUid()){
                        Intent SetItemView = new Intent(MainActivity.this, SetItemViewActivity.class);
                        SetItemView.putExtra("DbRef", tag.get("DbRef").toString());
                        SetItemView.putExtra("ImageRef", tag.get("ImageRef").toString());
                        SetItemView.putExtra("Uid", tag.get("Uid").toString());
                        SetItemView.putExtra("MarkerId", tag.get("MarkerId").toString());
                        Log.d("acac", "Uid="+tag.get("Uid").toString());
                        Log.d("acac", "MarkerId="+tag.get("MarkerId").toString());
                        startActivity(SetItemView);
                        return true;
                    } else {
                        Intent ItemView = new Intent(MainActivity.this, ItemViewActivity.class);
                        ItemView.putExtra("DbRef", tag.get("DbRef").toString());
                        ItemView.putExtra("ImageRef", tag.get("ImageRef").toString());
                        ItemView.putExtra("Uid", tag.get("Uid").toString());
                        ItemView.putExtra("MarkerId", tag.get("MarkerId").toString());
                        Log.d("acac", "Uid="+tag.get("Uid").toString());
                        Log.d("acac", "MarkerId="+tag.get("MarkerId").toString());
                        startActivity(ItemView);
                        return true;

                    }
                }
            }
        });
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);

        this.googleMap.setOnMapClickListener(this);
    }

    public void customMyLocationButton(View view){
        if(mFusedLocationClient == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            callCurrentLocation();
        }else{
            Log.d("haha", "mfused Notnull");
        }
        if(lastLocation != null || currentLocation != null){
            Log.d("haha", "mfunotnull2"+ currentLocation.getLatitude() +"/"+lastLocation.getLatitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Point clickPoint = this.googleMap.getProjection().toScreenLocation(latLng);
        LatLng point = this.googleMap.getProjection().fromScreenLocation(clickPoint);

        // getItemActivity 전환 인텐트
        Intent intent = new Intent(this, TypeActivity.class);
        intent.putExtra("lat", point.latitude);
        intent.putExtra("lng", point.longitude);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
    }

    // Firebase 변화 수신
    private void display() {
        Log.i(TAG, "display");
        Log.i("haha", "display");
        // Item 수신
        if(zonelist != null){
            zonelist.clear();
        }else{
            zonelist = new ArrayList<>();
        }
        mFireDB.getReference("Item")
                .addChildEventListener(new ChildEventListener() {
                    // 리스트의 아이템을 검색하거나 아이템 추가가 있을 때 수신
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) { // s 하위키, dataSnapshot 밑의 key-value
                        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
//                            Log.d("DB", "dataSnapshot.child===="+itemSnapshot.getKey());
                            Item item = itemSnapshot.getValue(Item.class);

                            // 구글맵에 마커 추가
                            if (item.getType() == true) {
                                addMarker = MainActivity.this.googleMap.addMarker(new MarkerOptions()

                                        .position(new LatLng(item.getLat(), item.getLng()))
                                        .title(item.getTitle())
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_getitem)));
                            } else {
                                addMarker = MainActivity.this.googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(item.getLat(), item.getLng()))
                                        .title(item.getTitle())
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_lostitem)));
                            }


                            // currentMarker와의 거리 구하기
                            Location locationMarker = new Location("marker");
                            Location current = new Location("current");

                            locationMarker.setLatitude(item.getLat());
                            locationMarker.setLongitude(item.getLng());
                            current.setLatitude(currentMarker.getPosition().latitude);
                            current.setLongitude(currentMarker.getPosition().longitude);
                            Log.d("haha", "display"+String.valueOf(currentMarker.getPosition().latitude));

                            float distance = locationMarker.distanceTo(current);    // m 단위

                            // 마커에 달 태그
                            HashMap<String, Object> tag = new HashMap<String, Object>();
                            tag.put("DbRef", "Item/" + item.getUid() + "/" + itemSnapshot.getKey());
                            tag.put("ImageRef", "Item/image/" + item.getUid() + "/" + itemSnapshot.getKey());
                            tag.put("Uid",item.getUid());
                            tag.put("MarkerId", itemSnapshot.getKey());
                            tag.put("distance", distance);
                            addMarker.setTag(tag);

                            if (distance < 100) {
                                Zone itemzone = new Zone();
                                itemzone.setRef("item/" + item.getUid() + "/" + dataSnapshot.getKey());
                                itemzone.setLatlng(new LatLng(item.getLat(), item.getLng()));
                                itemzone.setDistance(distance);
                                zonelist.add(itemzone);

                                if (currentLocation != null || lastLocation != null) {
                                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                                        if (zonelist.size() > 0) {
                                            geofencingClient.addGeofences(getGeofencingRequest(zonelist), getGeofencePendingIntent()).addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
//                                                    Toast.makeText(MainActivity.this, "SuccessAddGeofence", Toast.LENGTH_LONG).show();
                                                }
                                            }).addOnFailureListener(MainActivity.this, new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(MainActivity.this, "failaddGeofence", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    }
                                }
                            }
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
    public void deleteCircles(){
        if(mCircleList != null) {
            for (int i = 0; i <= mCircleList.size() - 1; i++) {
                Log.d("haha", "deleteCircles");
                mCircleList.get(i).remove();
            }
            mCircleList.clear();
        }
    }
    // GooglePlayService에 지오 펜스 요청
    private GeofencingRequest getGeofencingRequest(List<Zone> zonelist) {
        if (mCircleList==null){
            mCircleList = new ArrayList<>();
        }
        Collections.sort(zonelist, new CompareDistanceAsc());
        for(Zone zone : zonelist ) {
            mapCircle = googleMap.addCircle(new CircleOptions().center(new LatLng(zone.getLatlng().latitude, zone.getLatlng().longitude))
                    .radius(CIRCLE_BOUND)
                    .strokeColor(Color.parseColor("#884169e1"))
                    .fillColor(Color.parseColor("#5587cefa")));
            mCircleList.add(mapCircle);
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(zone.getRef())
                    .setCircularRegion(zone.getLatlng().latitude, zone.getLatlng().longitude, CIRCLE_BOUND)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT).build());    // 지오펜스 발생 시점
        }

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT);
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

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(locationCallback).addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    mFusedLocationClient = null;
                }
            });
        }
    }
}


