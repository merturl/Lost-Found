package com.example.jongho.newproject_1;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

public class LostItemActivity extends AppCompatActivity {

    private ImageView imageViewgetItem;
    private StorageReference mStorageRef;
    private Bitmap bitmap;

    // Firebase 객체 생성
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFireDB = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //외부 저장소 사진 읽기, 쓰기 권한 체크
        checkPerssions();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //imgview에 리스너를 달아 이미지뷰 클릭시 이미지 추가를 함
        imageViewgetItem = (ImageView) findViewById(R.id.lostImage);
        imageViewgetItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //이미지 선택할 수 있는 엑티비티 창 호출
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });
    }

    // DB에 아이템 저장
    public void saveLostItem(View v) {
        Intent getIntent = getIntent();


        // 입력한 정보 가져오기
        EditText edittitle = (EditText)findViewById(R.id.edit_Losttitle);
        EditText editcontent = (EditText)findViewById(R.id.edit_Lostcontent);
        EditText edittime = (EditText)findViewById(R.id.edit_Losttime);

        // 저장할 정보
        double i = 0;
        double lat = getIntent.getDoubleExtra("lat", i);
        double lng = getIntent.getDoubleExtra("lng", i);
        String title = edittitle.getText().toString();
        String content = editcontent.getText().toString();
        String time = edittime.getText().toString();


        // 아이템 저장 lat, lng,  title, content, time
        lostItem saveitem = new lostItem(lat, lng, title, content);
        DatabaseReference mFireRef = mFireDB.getReference("lostItem/"+mFirebaseAuth.getCurrentUser().getUid()).push();
        mFireRef.setValue(saveitem);
        String postId = mFireRef.getKey();
//                mFireDB.getReference("getItem/"+mFirebaseAuth.getCurrentUser().getUid()+"").push().setValue(saveitem)

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

    //파이어스토어에 이미지 저장
    public void uploadImage(String lostitemkey){
        if(lostitemkey == null ) {
            return;
        }


        Toast.makeText(this, "start uploadImages == " + lostitemkey, Toast.LENGTH_SHORT).show();
        //파이어스토어 접근 레퍼런스    // getItem/image/uid/randomkey.jpg
        Toast.makeText(this, "start uploadImages == " + mFirebaseAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
        StorageReference reference = mStorageRef.child("lostItem/image/"+mFirebaseAuth.getCurrentUser().getUid()+"/"+lostitemkey +".jpg");


        //파이어베이스스에 쓰이는 데이터로 이미지 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = reference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                Uri downloadUrl = taskSnapshot.getDownloadUrl(); //이미지가 저장된 주소의 URL
                Toast.makeText(LostItemActivity.this, "이미지 저장 성공", Toast.LENGTH_SHORT).show();
            }
        });
        Toast.makeText(LostItemActivity.this, "return uri== " + lostitemkey, Toast.LENGTH_LONG).show();

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

//                    // firestorage 에 이미지 업로드
//                    uploadImage();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
