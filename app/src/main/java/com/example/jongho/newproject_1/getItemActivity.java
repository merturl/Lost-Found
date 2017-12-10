package com.example.jongho.newproject_1;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class getItemActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView imageViewgetItem;
    private StorageReference mStorageRef;
    private Bitmap bitmap;

    // Firebase 객체 생성
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFireDB = FirebaseDatabase.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //외부 저장소 사진 읽기, 쓰기 권한 체크
        checkPerssions();
        mStorageRef = FirebaseStorage.getInstance().getReference();


        //imgview에 리스너를 달아 이미지뷰 클릭시 이미지 추가를 함
        imageViewgetItem = (ImageView) findViewById(R.id.getImage);
        imageViewgetItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //이미지 선택할 수 있는 엑티비티 창 호출
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy","onDestroy");
    }

    // DB에 아이템 저장
    public void saveGetItem(View v) {
        Intent getIntent = getIntent();


        // 입력한 정보 가져오기
        EditText edittitle = (EditText)findViewById(R.id.edit_Gettitle);
        EditText editcontent = (EditText)findViewById(R.id.edit_Getcontent);
        EditText edittime = (EditText)findViewById(R.id.edit_Gettime);

        // 저장할 정보
        double i = 0;
        double lat = getIntent.getDoubleExtra("lat", i);
        double lng = getIntent.getDoubleExtra("lng", i);
        String title = edittitle.getText().toString();
        String content = editcontent.getText().toString();
        String time = edittime.getText().toString();


        // 아이템 저장 lat, lng,  title, content, time
        Item saveitem = new Item(true, lat, lng, title, content);
        DatabaseReference mFireRef = mFireDB.getReference("Item/"+mFirebaseAuth.getCurrentUser().getUid()).push();
        mFireRef.setValue(saveitem);
        String postId = mFireRef.getKey();

        // firestorage 에 이미지 업로드
        uploadImage(postId);

        // 저장 후 입력 내용 초기화
        edittitle.setText("");
        editcontent.setText("");
        edittime.setText("");

        finish();
        // 화면전환 애니메이션 효과
       overridePendingTransition(R.anim.anim_slide_in_left,R.anim.anim_slide_out_right);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.get_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //파이어스토어에 이미지 저장
    public void uploadImage(String getitemkey){
        if(getitemkey == null ) {
            return;
        }

        //파이어스토어 접근 레퍼런스    // Item/image/uid/randomkey.jpg
        StorageReference reference = mStorageRef.child("Item/image/"+mFirebaseAuth.getCurrentUser().getUid()+"/"+getitemkey +".jpg");


        //파이어베이스에 쓰이는 데이터로 이미지 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            Toast.makeText(this, "Don't image", Toast.LENGTH_SHORT).show();

        } catch ( NullPointerException e ) {
            return ;
        }
        byte[] data = baos.toByteArray();


        UploadTask uploadTask = reference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                return;
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                Uri downloadUrl = taskSnapshot.getDownloadUrl(); //이미지가 저장된 주소의 URL
                Toast.makeText(getItemActivity.this, "이미지 저장 성공", Toast.LENGTH_SHORT).show();
            }
        });
        Toast.makeText(getItemActivity.this, "return uri== " + getitemkey, Toast.LENGTH_LONG).show();

    }


    //퍼미션 처리 함수
    private void checkPerssions(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    //외부 저장소 권한 처리
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
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

    //이미지 선택할 수 있는 엑티비티 창 수행 결과
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==1 ){

            if ( data == null) {
                Toast.makeText(this, "Sorry, Don't find image", Toast.LENGTH_SHORT).show();
            } else {
                //img를 받기 위한 Uri
                Uri image = data.getData();
                Log.i(image.toString(), "select image////"+image.toString());
                try {
                    //사진을 비트맵이미지로 변환
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);

                    //이미지 뷰에 비트맵 부착
                    imageViewgetItem.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
