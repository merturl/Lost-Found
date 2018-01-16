package com.example.jongho.newproject_1;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by JongHo on 2017-12-11.
 */

public class SearchActivity extends AppCompatActivity {
    private EditText search;
    private Button trans;
    private ListView listView;
    private Intent intent;
    private ArrayList<Map<String, String>> mList;

    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 요소 생성
        search = (EditText) findViewById(R.id.addr_real);
        trans = (Button) findViewById(R.id.tran_real);
        listView = (ListView) findViewById(R.id.listView);
        intent = new Intent();

        final Geocoder geo = new Geocoder(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Iterator iterator = mList.get(i).entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry)iterator.next();
                    Log.e("haha", "Key : " + entry.getKey() + ", Value : " + entry.getValue());
                    if(entry.getKey().equals("addr")) {
                        intent.putExtra("addr", entry.getValue().toString());
                    }
                    else if(entry.getKey().equals("lat")) {
                        intent.putExtra("lat", entry.getValue().toString());
                    }
                    else if(entry.getKey().equals("lon")) {
                        intent.putExtra("lon", entry.getValue().toString());
                    }
                }
                setResult(444, intent);
                finish();
            }
        });

        trans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Address> list = null;
                String str = search.getText().toString();

                try {
                    list = geo.getFromLocationName(str, 10);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("HAHA", "서버에서 주소 못찾음");
                }

                if (list != null) {
                    if (list.size() == 0) {
                        Toast.makeText(SearchActivity.this, "해당 주소정보 없음", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SearchActivity.this, String.valueOf(list.get(0).getLatitude()) + ", " + String.valueOf(list.get(0).getLongitude() + "OK?"), Toast.LENGTH_LONG).show();
                        intent.putExtra("lat", list.get(0).getLatitude());
                        intent.putExtra("lon", list.get(0).getLongitude());
                        intent.putExtra("addr", search.getText().toString());
                    }
                }

                setResult(Activity.RESULT_OK, intent);
                finish();

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mList = new ArrayList<Map<String, String>>();
        mCursor = MainActivity.mDB.query("my_Geo", new String[]{"addr", "lat", "lon"}, null, null, null, null, "_id");
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put("addr", mCursor.getString(0));
                    item.put("lat", mCursor.getString(1));
                    item.put("lon", mCursor.getString(2));
                    Log.e("HEYHEY", mCursor.getString(0) + ", " + mCursor.getString(1) + ", " + mCursor.getString(2));
                    mList.add(item);
                } while (mCursor.moveToNext());
            }
        }
        SimpleAdapter adapter = new SimpleAdapter(this, mList, android.R.layout.simple_list_item_1, new String[] {"addr"}, new int[] {android.R.id.text1});
        listView.setAdapter(adapter);
    }
}
