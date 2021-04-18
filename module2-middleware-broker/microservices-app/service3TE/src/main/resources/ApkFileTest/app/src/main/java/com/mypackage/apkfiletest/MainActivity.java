package com.mypackage.apkfiletest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.mypackage.apkfiletest.TaskImplementation;


public class MainActivity extends Activity {

    TaskImplementation taskImpInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        System.out.println("Task execution initiated");

        Intent broadcastIntent = new Intent("RESULT_ACTION");
        Results results = new Results();
        taskImpInstance = new TaskImplementation(results);

        broadcastIntent.putExtra("TaskResultData", results.getResult());
        broadcastIntent.setComponent(new ComponentName("com.android.mqttclient","com.android.mqttclient.LauncherActivity$MyBroadcastReceiver"));
        broadcastIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        getApplicationContext().sendBroadcast(broadcastIntent);
        finish();
    }
}