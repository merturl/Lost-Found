package com.jonghyeon.tutorial;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Store extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFireDB = FirebaseDatabase.getInstance();


    private ListView listView;
    List storeList = new ArrayList<>();
    ListViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        // Basic FloatingActionButton
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //  인텐트 값 받아오기
        Intent getintent = getIntent();
        String storeName = getintent.getStringExtra("StoreName");
//        Toast.makeText(this, storeName, Toast.LENGTH_SHORT).show();
//        displayStore(storeName);


        // Adapter 생성
        adapter = new ListViewAdapter();


        // 리스트뷰 참조 및 Adapter 달기
        listView = (ListView) findViewById(R.id.store_list);
        listView.setAdapter(adapter);


//        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_item_24dp),
//                mFireRef.child("store").push().setValue();


//        adapter = new ArrayAdapter<String>(this, R.layout.content_store, storeList );
//        listView.setAdapter(adapter);

//        display("getItem");

    }

    public void addItem(View view){
        EditText editText = (EditText)findViewById(R.id.tv_send);
        String s = editText.getText().toString();
        StoreItem item = new StoreItem(s);
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();


//        mFireDB.getReference("store/"+mFirebaseAuth.getCurrentUser().getUid());
//
        Map<String, StoreItem> items = new HashMap<String, StoreItem>();
        items.put("uid", new StoreItem(s));

        mFireDB.getReference("store").setValue(items)
        .addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // 첫 번째 아이템 추가
                adapter.addItem(
                        ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_item_24dp),
                        "Store",
                        "time"
                );
                Toast.makeText(getBaseContext(), "저장성공", Toast.LENGTH_LONG).show();
            }
        });
        editText.setText("");
    }

    private void display(String storeName){
        Toast.makeText(this, storeName, Toast.LENGTH_SHORT).show();
        mFireDB.getReference("store/"+storeName)
                .addChildEventListener(new ChildEventListener() {
                    // 리스트의 아이템을 검색하거나 아이템 추가가 있을 때 수신
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            StoreItem item = dataSnapshot.getValue(StoreItem.class);
                            adapter.addItem(
                                    ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_item_24dp),
                                    "item.",
                                    "time"
                            );
                    }

                    // 아이템 변화가 있을 때 수신
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    // 아이템이 삭제 되었을 때 수신
                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    // 순서가 있는 리스트에서 순서가 변경 되었을 때 수신신
                   @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


}
