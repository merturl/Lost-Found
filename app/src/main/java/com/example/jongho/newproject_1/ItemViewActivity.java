package com.example.jongho.newproject_1;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class ItemViewActivity extends AppCompatActivity {

    // Firebase 객체 생성
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFireDB = FirebaseDatabase.getInstance();
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_view);
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

        // intent 받아오기
        Intent getintent = getIntent();

        String DbRef = getintent.getStringExtra("DbRef");
        final String ImageRef = getintent.getStringExtra("ImageRef");

        mFireDB.getReference(DbRef)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mStorageRef = FirebaseStorage.getInstance().getReference();
                StorageReference storageReference = mStorageRef.child(ImageRef+ ".jpg");

                Item item = dataSnapshot.getValue(Item.class);

                Log.d("haha", "datasnapshot= "+ item.getTitle());

                TextView title = (TextView)findViewById(R.id.text_ItemTitle);
                TextView content = (TextView)findViewById(R.id.text_ItemContent);
                TextView time = (TextView)findViewById(R.id.text_ItemTime);

                Log.d("image","storageReference="+ storageReference);
                Log.d("image","beforeload="+storageReference.getDownloadUrl());

                ImageView imgView = (ImageView)findViewById(R.id.imgv_item);
                try{
                    Glide.with(ItemViewActivity.this)
                            .using(new FirebaseImageLoader())
                            .load(storageReference)
                            .into(imgView);

                } catch (Throwable e) {
                    Log.d("image",e.getMessage());
                }

                // text Item
                title.setText(item.getTitle());
                content.setText(item.getContent());
                time.setText(item.getTime());

                // Load the image using Glide
                Glide.with(ItemViewActivity.this)
                        .using(new FirebaseImageLoader())
                        .load(storageReference)
                        .into(imgView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });
    }

    public void EditItem(View view) {

    }



    @Override
    protected void onResume() {
        super.onResume();

    }

}
