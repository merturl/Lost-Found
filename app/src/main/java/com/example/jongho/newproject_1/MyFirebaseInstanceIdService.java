package com.example.jongho.newproject_1;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by merturl on 2017-12-06.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        Log.d("haha", "hllleoo");
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications");
//        reference.child("token").setValue(refreshedToken);
        Log.d("myFirebaseid", "Refreshed token" + refreshedToken);
    }
}
