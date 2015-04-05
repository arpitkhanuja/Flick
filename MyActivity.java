package com.example.flick.flick;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

//import com.firebase.client.Firebase;
import com.firebase.client.*;
import com.firebase.simplelogin.FirebaseSimpleLoginError;
import com.firebase.simplelogin.FirebaseSimpleLoginUser;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginAuthenticatedHandler;

import java.util.HashMap;
import java.util.Map;
//import com.firebase.simpleLogin.Firebase;


public class MyActivity extends Activity implements SensorEventListener {
    private static final String TAG = "MyActivity";
    String mostRecentLink;
    final Firebase mainRef = new Firebase("https://<FIREBASE LINK HERE>.firebaseio.com");

    ///////////////////////////////////
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 100;



    ///////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //Log.d("temp", "Pre-Service creating");
        //this.startService(new Intent(FirebaseLinkHandler.class.getName()));
    }

    public void getRecentPage() {
        // grab the most recent link
        String[] proj = new String[] { Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL };
        Uri uriCustom = Uri.parse("content://com.android.chrome.browser/bookmarks");
        String sel = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history, 1 = bookmark
        Cursor mCur = getContentResolver().query(uriCustom, proj, sel, null, null);
        mCur.moveToFirst();
        @SuppressWarnings("unused")
        String title = "";
        @SuppressWarnings("unused")
        String url = "";
        if (mCur.moveToFirst() && mCur.getCount() > 0) {
            boolean cont = true;
            while (mCur.isAfterLast() == false && cont) {
                title = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                url = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.URL));
                if(mCur.isLast()) {
                    Log.d(TAG, "Most Recent Link is:" + url);
                    mostRecentLink = url;
                }
                mCur.moveToNext();
            }
        }
    }

    public void flick() {
        Log.d(TAG, "Testing Firebase: Code Reached");
        Firebase usersRef = mainRef.child("recent");
        Map<String, Object> val = new HashMap<String, Object>();
        val.put("recent", mostRecentLink);
        //Log.d(TAG, "" + val.values());
        usersRef.setValue(val);
     //   Log.d(TAG, "Testing Firebase Val:" + usersRef);
     //   Log.d(TAG, "Testing Firebase: Code Finished");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        Sensor mySensor = event.sensor;
//        Log.d(TAG, "Testing Sensor");
        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();

            if((curTime - lastUpdate) > 100) {
                long diffTime = curTime - lastUpdate;
                lastUpdate = curTime;
               // System.out.println("z: " + z);
               // System.out.println("x: " + x);
               // System.out.println("y: " + y);
                if (x < -5) {
                    System.out.println("Preparing to flick!!");
                    getRecentPage();
                    float speed = Math.abs(x - last_x) / diffTime * 10000;
                    if (speed > SHAKE_THRESHOLD) {
                        Log.d(TAG, "Testing Sensor: You flicked!");
                        getRecentPage();
                        flick();
                    }
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    protected void onPause() {
        super.onPause();
        //senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        //senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

}
