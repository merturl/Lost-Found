package com.example.jongho.newproject_1;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
