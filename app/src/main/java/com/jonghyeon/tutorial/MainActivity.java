package com.jonghyeon.tutorial;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
     private FirebaseAuth mFirebaseAuth;
     private FirebaseUser mFirebaseUser;
     private FirebaseDatabase mFirebaseDatabase;

     private EditText etContent;
     private TextView txtEmail, txtName;
     private NavigationView mNavigationView;

     private String selectedMemoKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        etContent = (EditText)findViewById(R.id.content);

        if(mFirebaseUser == null){
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
            return;
        }

        setSupportActionBar(toolbar);

        FloatingActionButton fabNewMemo = (FloatingActionButton) findViewById(R.id.new_memo);
        FloatingActionButton fabSaveMemo = (FloatingActionButton) findViewById(R.id.save_memo);


        fabNewMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                initMemo();
            }
        });
        fabSaveMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedMemoKey == null){
                    saveMemo();
                }else{
                    updateMemo();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        mNavigationView = (NavigationView)findViewById(R.id.nav_view);
        View headerVIew = mNavigationView.getHeaderView(0);
        //헤더의 id이기 때문에 헤더의 R.id를 호출 한다.
        txtEmail = (TextView)headerVIew.findViewById(R.id.txtEmail);
        txtName = (TextView)headerVIew.findViewById(R.id.txtName);
        mNavigationView.setNavigationItemSelectedListener(this);

        profileUpdate();
        displayMemos();
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
        getMenuInflater().inflate(R.menu.main, menu);
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
        Memo selectedMemo = (Memo)item.getActionView().getTag();
        etContent.setText(selectedMemo.getTxt());
        selectedMemoKey = selectedMemo.getKey();
//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initMemo(){
        etContent.setText("");
    }

    private void saveMemo(){
        String text = etContent.getText().toString();
        if(text.isEmpty()){
            return;
        }
        Memo memo = new Memo();
        memo.setTxt(text);
        memo.setCreateDate((new Date().getTime()));
        mFirebaseDatabase.getReference("memos/"+mFirebaseUser.getUid()).push().setValue(memo).addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Snackbar.make(etContent, "메모가 저장되었습니다.", Snackbar.LENGTH_LONG).show();
                initMemo();
            }
        });
    }

    private void updateMemo(){
        String text = etContent.getText().toString();
        if(text.isEmpty()){
            return;
        }
        Memo memo = new Memo();
        memo.setTxt(text);
        memo.setCreateDate((new Date().getTime()));
        mFirebaseDatabase.getReference("memos/"+mFirebaseUser.getUid()+ "/" + selectedMemoKey).setValue(memo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Snackbar.make(etContent, "메모가 수정되었습니다.", Snackbar.LENGTH_LONG).show();
                initMemo();
            }
        });
    }

    private void profileUpdate(){
        txtEmail.setText(mFirebaseUser.getEmail());
        txtName.setText(mFirebaseUser.getDisplayName());
    }

    private void displayMemos(){
        mFirebaseDatabase.getReference("memos/"+mFirebaseUser.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Memo memo = dataSnapshot.getValue(Memo.class);  //generic는 원하는 자료형을 리턴한다.
                memo.setKey(dataSnapshot.getKey());
                displayMemoList(memo);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Memo memo = dataSnapshot.getValue(Memo.class);  //generic는 원하는 자료형을 리턴한다.
                memo.setKey(dataSnapshot.getKey());

                for(int i = 0; i < mNavigationView.getMenu().size(); i++){
                    MenuItem menuItem = mNavigationView.getMenu().getItem(i);
                    if(memo.getKey().equals(((Memo)menuItem.getActionView().getTag()).getKey())){
                        menuItem.getActionView().setTag(memo);
                        menuItem.setTitle(memo.getTitle());
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override  //Load cancel
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void displayMemoList(Memo memo){
        Menu leftMenu = mNavigationView.getMenu();
        MenuItem item = leftMenu.add(memo.getTitle());
        View view = new View(getApplication());
        view.setTag(memo);
        item.setActionView(view);
    }
}
