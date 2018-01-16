package com.example.jongho.newproject_1;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = "GeofenceTransitionsIS";
    private static final String CHANNEL_ID = "channel_01";

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.example.jongho.newproject_1.action.FOO";
    private static final String ACTION_BAZ = "com.example.jongho.newproject_1.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.jongho.newproject_1.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.jongho.newproject_1.extra.PARAM2";

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        List<Geofence> triggeringGeofences = event.getTriggeringGeofences();
        ArrayList triggeringGeofencesldsList = new ArrayList();

        for(Geofence geofence : triggeringGeofences) {
            triggeringGeofencesldsList.add(geofence.getRequestId());
        }
        String IDs = TextUtils.join(", ", triggeringGeofencesldsList);

        int transitiontype = event.getGeofenceTransition();
        sendNotification(transitiontype);
        if(transitiontype == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.e("haha", "Enter into " + IDs);
        }
        if(transitiontype == Geofence.GEOFENCE_TRANSITION_DWELL) {
            Log.e("haha", "DWELL " + IDs);
        }
        if(transitiontype == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.e("haha", "Exit from " + IDs+event.getTriggeringLocation().getLatitude() + ""+event.getTriggeringLocation().getLongitude());
        }
    }

    private void sendNotification(int transitiontype) {
        String title = null;
        String content = "메시지를 확인해주세요!";
        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        if(transitiontype == Geofence.GEOFENCE_TRANSITION_ENTER) {
            title = "주변에 메시지가 있습니다.";
            Log.e("haha", "메시지를 확인해주세요!");
        }
        if(transitiontype == Geofence.GEOFENCE_TRANSITION_DWELL) {
            title = "주변에 메시지가 있습니다.";
            Log.e("haha", "메시지를 확인해주세요! ");
        }
        if(transitiontype == Geofence.GEOFENCE_TRANSITION_EXIT) {
            title = "주변에 메시지와 멀어지고 있습니다.";
//            Log.e("haha", "Exit from " + IDs+event.getTriggeringLocation().getLatitude() + ""+event.getTriggeringLocation().getLongitude());
        }


        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_stat_ic_notification)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_stat_ic_notification))
                .setColor(Color.RED)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(notificationPendingIntent);

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
