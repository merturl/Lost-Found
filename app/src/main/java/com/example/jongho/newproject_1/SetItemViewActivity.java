package com.example.jongho.newproject_1;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Ref;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SetItemViewActivity extends AppCompatActivity {
    // Firebase 객체 생성
    private FirebaseDatabase mFireDB = FirebaseDatabase.getInstance();
    private StorageReference mStorageRef;

    private Bitmap bitmap;
    private ImageView imageViewgetItem;

    EditText title;
    EditText content;
    EditText time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_item_view);
        Log.d("acac", "onCreate");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //외부 저장소 사진 읽기, 쓰기 권한 체크
        checkPerssions();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        Intent getintent = getIntent();
        final String DbRef = getintent.getStringExtra("DbRef");
        final String ImageRef = getintent.getStringExtra("ImageRef");

        //imgview에 리스너를 달아 이미지뷰 클릭시 이미지 추가를 함
        imageViewgetItem = (ImageView) findViewById(R.id.imgv_setitem);
        imageViewgetItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //이미지 선택할 수 있는 엑티비티 창 호출
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });

        mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference storageReference = mStorageRef.child(ImageRef+ ".jpg");
        Glide.with(SetItemViewActivity.this)
                .using(new FirebaseImageLoader())
                .load(storageReference)
                .into(imageViewgetItem);


        mFireDB.getReference(DbRef)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mStorageRef = FirebaseStorage.getInstance().getReference();
                        StorageReference storageReference = mStorageRef.child(ImageRef+ ".jpg");
                        Log.d("acac", "addValueEventListener"+ storageReference);

                        Item item = dataSnapshot.getValue(Item.class);
                        if(  item.getTitle().matches("") ) {
                            finish();
                        }


                        title = (EditText) findViewById(R.id.edit_ItemTitle);
                        content = (EditText) findViewById(R.id.edit_ItemContent);
                        time = (EditText) findViewById(R.id.edit_ItemTime);
                        ImageView imgview = (ImageView)findViewById(R.id.imgv_setitem);

                        title.setText(item.getTitle());
                        content.setText(item.getContent());
                        time.setText(item.getTime());

//                        // SharedPreferences 사용,
//                        SharedPreferences sharedPreferences = getSharedPreferences("prefItemData", MODE_PRIVATE);
//
//                        // 기록이 남아있으면 SharedPreferences에서, 아니라면 기존의 데이터로 초기화화
//                        title.setText(sharedPreferences.getString("edit_ItemTitle", item.getTitle()));
//                        content.setText(sharedPreferences.getString("edit_ItemContent",item.getContent()));
//                        time.setText(sharedPreferences.getString("edit_ItemTime",item.getTime()));

                        Log.d("acac", "storageRef=="+ storageReference);
                        // Load the image using Glide
                        Glide.with(SetItemViewActivity.this)
                                .using(new FirebaseImageLoader())
                                .load(storageReference)
                                .into(imgview);


                        Log.d("acac", "//////////before EditItem/////////");
                        Log.d("acac", "DbRef == "+ DbRef);
                        Log.d("acac", "ImageRef == "+ ImageRef);
                        Log.d("acac", "item.getTitle() == "+ item.getTitle());
                        EditItem(DbRef, ImageRef, item);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // ...
                    }
                });
    }

    public void onStop() {
        super.onStop();

//        title = (EditText) findViewById(R.id.edit_ItemTitle);
//        content = (EditText) findViewById(R.id.edit_ItemContent);
//        time = (EditText) findViewById(R.id.edit_ItemTime);
//
//        // SharedPreferences 사용,
//        SharedPreferences sharedPreferences = getSharedPreferences("prefItemData", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("edit_ItemTitle",title.getText().toString());
//        editor.putString("edit_ItemContent",content.getText().toString());
//        editor.putString("edit_ItemTime",time.getText().toString());
//
//        editor.commit();
    }

    public void EditItem(final String Ref, final String ImageRef, final Item item) {
        Log.d("acac", "///////////In EditItem ///////// ");
        Log.d("acac", "DbRef == " + Ref);
        Log.d("acac", "ImageRef == " + ImageRef);
        Log.d("acac", "Item.getTitle() == " + item.getTitle());

        final EditText etitle = (EditText) findViewById(R.id.edit_ItemTitle);
        final EditText econtent = (EditText) findViewById(R.id.edit_ItemContent);
        final EditText etime = (EditText) findViewById(R.id.edit_ItemTime);
        ImageView imgView = (ImageView) findViewById(R.id.imgv_setitem);
        Button BtnEdit = (Button) findViewById(R.id.btn_edit);


//        final String title = etitle.getText().toString();
//        final String content = econtent.getText().toString();
//        final String time = etime.getText().toString();

        Log.d("acac", "item.getTitle == " + item.getTitle());

        // Edit 버튼 클릭시
        BtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SetItemViewActivity.this, "Hi btn", Toast.LENGTH_SHORT).show();

                EditText title = (EditText) findViewById(R.id.edit_ItemTitle);
                EditText content = (EditText) findViewById(R.id.edit_ItemContent);
                EditText time = (EditText) findViewById(R.id.edit_ItemTime);

                Log.d("acac", "/////onClick///////");
                Log.d("acac", "item.title==" + item.getTitle());
                Log.d("acac", "Ref=====" + Ref);

                Map<String, Object> itemUpdates = new HashMap<String, Object>();
                itemUpdates.put("title", title.getText().toString());
                itemUpdates.put("content", content.getText().toString());
                itemUpdates.put("time", time.getText().toString());

                // 아이템 저장
                DatabaseReference mFireRef = mFireDB.getReference(Ref);
                mFireRef.updateChildren(itemUpdates);

                // 이미지 저장
                StorageReference reference = mStorageRef.child(ImageRef+".jpg");
                Log.d("acac", "reference" + reference);
                //파이어베이스에 쓰이는 데이터로 이미지 변환
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Log.d("acac", "baos== " + baos);
                try {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                } catch (NullPointerException e) {
                    Log.d("acac", "baos== " + baos);
                    return;
                }
                byte[] data = baos.toByteArray();
                Log.d("acac", "data== " + data);

                UploadTask uploadTask = reference.putBytes(data);
                if(uploadTask == null ){
                    finish();
                }
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.d("acac", "ADDoNfAILURER ");
                        finish();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("acac", "in listenr");
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getDownloadUrl(); //이미지가 저장된 주소의 URL

                        Log.d("acac", "onSucess== " + item.getTitle());
                        Log.d("DB", "success listiner" + item.getContent());
//                        uploadImage(ImageRef);
                        Toast.makeText(SetItemViewActivity.this, "이미지 저장 성공", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });

    }

    public void Itemdelete(View v){
        Intent getintent = getIntent();
        String DbRef = getintent.getStringExtra("DbRef");
        String ImageRef = getintent.getStringExtra("ImageRef");
        Log.d("acac", "in delete ImageRef= "+ImageRef);
        Log.d("acac","ImageRef== " + ImageRef);
//        Toast.makeText(this, "ImageRef=="+ImageRef, Toast.LENGTH_LONG).show();

        DatabaseReference mFireRef = mFireDB.getReference(DbRef);
        mFireRef.removeValue();

        StorageReference reference = mStorageRef.child(ImageRef+".jpg");
        Log.d("Image","Image == " + ImageRef+".jpg");
        Toast.makeText(this, "Image == " + ImageRef+".jpg", Toast.LENGTH_LONG).show();
        reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("acac", "onSuccess: delete ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("acac","onFailure:"+ e.getMessage());
            }
        });

        finish();
    }


    //파이어스토어에 이미지 저장
    public void uploadImage(final String ImageRef){
        Log.d("DB","uploadImage start");
        Log.d("acac","/////////in upload///////// ");
        Log.d("acac","ImageRef== " + ImageRef);
        if(ImageRef == null ) {
            return;
        }

        //파이어스토어 접근 레퍼런스    // Item/image/uid/randomkey.jpg
        final StorageReference reference = mStorageRef.child(ImageRef);
        Log.d("acac","reference== " + reference);
        //파이어베이스에 쓰이는 데이터로 이미지 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        } catch ( NullPointerException e ) {
            Toast.makeText(this, "Don't image", Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] data = baos.toByteArray();


        UploadTask uploadTask = reference.putBytes(data);
        Log.d("DB","success ImageRef"+  mStorageRef.child(ImageRef).toString());
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
                Toast.makeText(SetItemViewActivity.this, "이미지 저장 성공", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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
        Log.d("acac", "/////////onActivityResult/////////");

        if(requestCode ==1 ){
            if ( data == null) {
                Toast.makeText(this, "Sorry, Don't find image", Toast.LENGTH_SHORT).show();
            } else {
                //img를 받기 위한 Uri
                Uri image = data.getData();
                Log.d("acac", "image=== " +image);
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
