package com.example.jongho.newproject_1;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by merturl on 2017-12-06.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage.getData().size() > 0) {
            Log.d("haha", "Message data payload " + remoteMessage.getData());
        }
        if (remoteMessage.getNotification() != null){
            Log.d("haha", "Message Notification Body" + remoteMessage.getNotification().getBody());
            Map<String, String> payload = remoteMessage.getData();
            showNotification(payload);
        }
    }
    private void showNotification(Map<String, String> payload){
        if(payload != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle(payload.get("username"));
            builder.setContentText(payload.get("email"));

            Intent resultIntent = new Intent(this, MapsActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, builder.build());
        }
    }
}
