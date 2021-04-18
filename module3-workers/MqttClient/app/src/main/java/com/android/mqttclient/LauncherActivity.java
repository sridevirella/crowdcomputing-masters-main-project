package com.android.mqttclient;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.support.design.widget.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.android.mqttclient.model.Constants;
import com.android.mqttclient.services.MqttServices;
import com.android.mqttclient.services.ReadWriteFile;
import com.android.mqttclient.model.TaskDetails;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * LauncherActivity is app launcher activity, set in android manifest file. Defined by extending default android Activity class.
 * Provides a window to attach different fragments as views.
 * Handles navigation drawer menu, notification toggle switch, user preferences.
 */
public class LauncherActivity extends AppCompatActivity{

    private AppBarConfiguration mAppBarConfiguration;
    private static final String MyPREFERENCES = "MyPrefs" ;

    private static LaunchViewModel launchViewModel;
    private MyBroadcastReceiver myReceiver;


    /**
     * When an Activity first launched onCreate() method is responsible to create the activity.
     * invokes init method.
     * @param savedInstanceState : Default and required parameter used to handle any saved instanced data
     *                           when the activity recreates. Example: on orientation change.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        launchViewModel = ViewModelProviders.of(this).get(LaunchViewModel.class);
        init();
    }

    /**
     * Initializes SharedPreferences in private mode, toolbar and navigation drawer.
     * Calls User Interface to initialize views.
     */
    private void init() {
        launchViewModel.initMqtt(new MqttServices(this));
        launchViewModel.initReadWriteInstance(new ReadWriteFile(this));
        launchViewModel.initSharedPref(getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE));
        launchViewModel.getNewTaskReceived().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String description) {
                Toast.makeText(LauncherActivity.this, "New task received, Sending notification = "+description, Toast.LENGTH_SHORT).show();
                if(description != null) {
                    sendTaskNotification("Title", description);
                } else {
                    System.out.println("Task description is null");
                }
            }
        });
        initToolbar();
        initNavigationDrawer();
        myReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("RESULT_ACTION");
        registerReceiver(myReceiver, intentFilter);
    }

    /**
     * onRequestPermissionsResult() callback method to grant permission for onResume tasks.
     * @param requestCode The permission request identifier
     * @param permissions  The array containing Manifest permissions
     * @param grantResults The array containing if requested permissions were granted or not
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("Sri", "Permission: " + permissions[0] + "was " + grantResults[0]);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myReceiver != null) {
            unregisterReceiver(myReceiver);
        }
    }

    /**
     * Initializes LauncherActivity Toolbar and sets ActionBar.
     */
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Initializes navigation drawer.
     */
    private void initNavigationDrawer() {
        setMenuIds();
        initToggleSwitch();
    }

    /**
     * Initializes Navigation drawer menu Ids and binds them with {@link DrawerLayout}
     */
    private void setMenuIds() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_subscribe, R.id.nav_received_tasks, R.id.nav_executed_tasks)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    /**
     * Initializes {@link SwitchCompat} and set user preferred state to enable/disable receiving notifications.
     */
    private void initToggleSwitch() {
        SwitchCompat notificationToggle = findViewById(R.id.footer_toggle_button);
        notificationToggle.setChecked(launchViewModel.getNotificationPref());
        notificationToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                launchViewModel.saveToPreferences(isChecked);
            }
        });
    }

    /**
     * Overrides Activity Options menu to have custom navigation drawer menu for Subscribe, Unsubscribe, Received Tasks and Executed Tasks.
     * @param menu : menu layout xml with menu options
     * @return :
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * Controls navigation drawer animation.
     * @return :
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void sendTaskNotification(@NonNull String task, @NonNull String description) {
        if (launchViewModel.getNotificationPref()) {
            sendNotification(task, description);
        }
    }

    /**
     * Builds and send notification whenever it is invoked. Takes taskTitle and taskDetail as input parameters to display on device status bar notification.
     * @param taskTitle : received Task title  to display on status bar
     * @param taskDetail : received Task detail to display on status bar
     */
    private void sendNotification(String taskTitle, String taskDetail) {
        NotificationManager mNotificationManager;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getApplicationContext(), "notify_001");
        Intent ii = new Intent(this.getApplicationContext(), LauncherActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(taskDetail);
        bigText.setBigContentTitle("Task Received: "+taskTitle);
        bigText.setSummaryText("New task Received");

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle(taskTitle);
        mBuilder.setContentText(taskDetail);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "notification channel",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }
        mNotificationManager.notify(0, mBuilder.build());
    }

    /**
     *  MyBroadcastReceiver inner class to receive sub task result as broad cast message from sub task.
     */
    public static class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String stringIntentAction = intent.getAction();

                if (stringIntentAction != null && stringIntentAction.equals("RESULT_ACTION")) {  // checks for intent action identifier

                    try {
                        JSONObject taskResultJsonObj = getTaskResultJsonObj(Constants.WORKER_ID.getConstant());
                        JSONObject taskResult = new JSONObject(intent.getStringExtra("TaskResultData"));
                        taskResultJsonObj.accumulate("subTaskResult", taskResult.getString("result")); // append  sub task result to sub task response jsonobject
                        String taskId = taskResult.getString("taskId");
                        taskResultJsonObj.accumulate("taskId", (taskId == null) ? TaskDetails.getTaskId() : taskId);
                        launchViewModel.getRwfInstance().unInstallApk();

                        if (taskResultJsonObj != null) {
                            String responseTopic = Constants.WORKER_SUBTASK_RESPONSE.getConstant();
                            launchViewModel.onExecutedTask("Executed response topic description");
                            launchViewModel.getMqttServicesInstance().publishMessage(responseTopic, taskResultJsonObj.toString() ); // publish sub task result back to mqtt
                            launchViewModel.getRwfInstance().deleteFile(null);                   // delete sub task apk file from device internal memory
                            //rwfInstance.unInstallApk();                                        // uninstall sub task apk on the device
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    System.out.println("Something went wrong while task result BroadcastReceive");
                }
            }
        }
    }

    /**
     * Get task result JSON object.
     */
    private static JSONObject getTaskResultJsonObj(String workerId) throws JSONException {

        JSONObject taskResultJsonObj = new JSONObject();
        taskResultJsonObj.put("workerId", workerId);
        return taskResultJsonObj;
    }
}
