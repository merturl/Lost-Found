package com.example.jongho.newproject_1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class TypeActivity extends AppCompatActivity {
    double lat;
    double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        // 좌표 가져오기
        double i =0;
        Intent getintent = getIntent();
        lat = getintent.getDoubleExtra("lat", i);
        lng = getintent.getDoubleExtra("lng", i);
    }

    public void moveGetItem(View view) {
        startActivity ( new Intent(this, getItemActivity.class));
        overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_left);
    }

    public void moveLostItem(View view) {
        startActivity ( new Intent(this, LostItemActivity.class));
        overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_left);

    }

}
