package com.example.jongho.newproject_1;

import android.*;
import android.Manifest;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.health.PackageHealthStats;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JongHo on 2017-12-10.
 */

public class ContractsActivity extends AppCompatActivity {
    private ListView listView;
    private static final int PERMISSIONS_REQUEST_READ_CONTRACTS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contracts);

        this.listView = (ListView) findViewById(R.id.listView);
        showContacts();
    }

    private void showContacts() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTRACTS);
        }
        else {
            List<String> contacts = getContactNames();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts);
            listView.setAdapter(adapter);
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] permissions, int[] grantResults) {
        if(req == PERMISSIONS_REQUEST_READ_CONTRACTS) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showContacts();
            }
            else {
                Toast.makeText(this, "Fuck you", Toast.LENGTH_LONG).show();
            }
        }
    }

    private List<String> getContactNames() {
        List<String> contacts = new ArrayList<>();

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if(cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                contacts.add(name);
            } while(cursor.moveToNext());
        }
        cursor.close();

        return contacts;
    }
}
