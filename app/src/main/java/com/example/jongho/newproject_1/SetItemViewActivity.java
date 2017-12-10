package com.example.jongho.newproject_1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class SetItemViewActivity extends AppCompatActivity {
    // Firebase 객체 생성
    private FirebaseDatabase mFireDB = FirebaseDatabase.getInstance();
    private StorageReference mStorageRef;

    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_item_view);
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

        Intent getintent = getIntent();
        final String DbRef = getintent.getStringExtra("DbRef");
        final String ImageRef = getintent.getStringExtra("ImageRef");
        Log.d("DB", "ImageRef===" + ImageRef);

        mFireDB.getReference(DbRef)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mStorageRef = FirebaseStorage.getInstance().getReference();
                        StorageReference storageReference = mStorageRef.child(ImageRef+ ".jpg");
//                mStorageRef.child("Item/image/"+mFirebaseAuth.getCurrentUser().getUid()+"/"+dataSnapshot.getKey() +".jpg");

                        Item item = dataSnapshot.getValue(Item.class);

                        EditText title = (EditText) findViewById(R.id.edit_ItemTitle);
                        EditText content = (EditText) findViewById(R.id.edit_ItemContent);
                        EditText time = (EditText) findViewById(R.id.edit_ItemTime);
                        ImageView imgView = (ImageView)findViewById(R.id.imgv_setitem);

                        Toast.makeText(SetItemViewActivity.this, "title"+ item.getTitle(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(SetItemViewActivity.this, "content"+ item.getContent(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(SetItemViewActivity.this, "time"+ item.getTime(), Toast.LENGTH_SHORT).show();

                         // text Item
                        Log.d("haha","item.getTitle() === " + item.getTitle());
                        title.setText(item.getTitle());
                        content.setText(item.getContent());
                        time.setText(item.getTime());
                         // Load the image using Glide
                        Glide.with(SetItemViewActivity.this)
                                .using(new FirebaseImageLoader())
                                .load(storageReference)
                                .into(imgView);

                        EditItem(DbRef, ImageRef, item);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // ...
                    }
                });
    }

    public void EditItem(String Ref,String ImageRef, Item item) {
        EditText title = (EditText) findViewById(R.id.edit_ItemTitle);
        EditText content = (EditText) findViewById(R.id.edit_ItemContent);
        EditText time = (EditText) findViewById(R.id.edit_ItemTime);
        ImageView imgView = (ImageView)findViewById(R.id.imgv_setitem);
        Button BtnEdit = (Button)findViewById(R.id.btn_edit);

        item.setTitle(title.getText().toString());
        item.setContent(content.getText().toString());
        item.setTime(time.getText().toString());
        uploadImage(ImageRef);
    }

    //파이어스토어에 이미지 저장
    public boolean uploadImage(String ImageRef){
        if(ImageRef == null ) {
            return false;
        }

        //파이어스토어 접근 레퍼런스    // Item/image/uid/randomkey.jpg
        StorageReference reference = mStorageRef.child(ImageRef);

        //파이어베이스에 쓰이는 데이터로 이미지 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        } catch ( NullPointerException e ) {
            Toast.makeText(this, "Don't image", Toast.LENGTH_SHORT).show();
            return false;
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
                Toast.makeText(SetItemViewActivity.this, "이미지 저장 성공", Toast.LENGTH_SHORT).show();
            }
        });
        return true;
    }

}
