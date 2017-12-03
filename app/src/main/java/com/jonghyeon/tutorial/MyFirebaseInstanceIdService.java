package com.jonghyeon.tutorial;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by merturl on 2017-12-02.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications");
        reference.child("token").setValue(refreshedToken);
        Log.d("myFirebaseid", "Refreshed token" + refreshedToken);
    }
}
