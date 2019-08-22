package com.example.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.nio.channels.AlreadyBoundException;
import java.sql.SQLTransactionRollbackException;
import java.util.List;

public class PollService extends IntentService {
    private static final String TAG = "PollService";
    //60 seconds
    private static final long POLL_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    public static final String ACTION_SHOW_NOTIFICATION = "com.example.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "com.example.photogallery.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    //SetInexactRepeating takes four parameters: a constant to
    // describe the time basis for the alarm,the time at which
    // to start the alarm,the time interval at to repeat the alarm
    // and finally a PendingIntent to fire when alarm goes off.
    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setInexactRepeating
                    (AlarmManager.ELAPSED_REALTIME,
                            SystemClock.elapsedRealtime(),
                            POLL_INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }

        QueryPrefernces.setAlarmOn(context, isOn);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public PollService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }
        String query = QueryPrefernces.getStoredQuery(this);
        String lastResultId = QueryPrefernces.getLastResultId(this);
        List<GalleryItems> items;

        if (query == null) {
            items = new FlickFetchr().fetchRecentPhotos();
        } else {
            items = new FlickFetchr().searchPhotos(query);
        }

        if (items.size() == 0) {
            return;
        }

        String resultId = items.get(0).getId();
        if (resultId.equals(lastResultId)) {
            Log.i(TAG, "Got an old result: " + resultId);
        } else {
            Log.i(TAG, "Got a new result: " + resultId);

            Resources resource = getResources();
            Intent i = MainActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resource.getString(R.string.new_picture_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resource.getString(R.string.new_picture_title))
                    .setContentText(resource.getString(R.string.new_picture_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            showBackgroundNotification(0, notification);
        }
        QueryPrefernces.setLastResultId(this, resultId);
    }

    //Toggling the background data setting to disallow downloading
    // data in the background disables the network entirely for use
    // by background services.In this case,ConnectivityManager.getActiveNetworkInfo
    // returns null,making it appears to the background service
    // as if there is no active network available,even if there really is.
    //If the network is available to your background service,it gets an
    // instance of NetworkInfo representing the current network connection.
    // The code hen checks whether the current network is fully
    // connected by calling NetworkInfo.isConnected
    //If the app does not see a network available,or the device is not fully
    // connected to a network, onHandleIntent will return
    // without executing the rest of the method.This is a good practise
    // because the app cannot download any data if it is not connected to the network
    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetwork() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent
                .getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);

        //Null PendingIntent means that alarm is not set
        return pi != null;
    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        //Parameters a :result receiver,a Handler to run the result receiver on,and then initial
        // values for the result code,result data,and result extras for the ordered broadcast
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null,
                Activity.RESULT_OK, null, null);
    }
}
