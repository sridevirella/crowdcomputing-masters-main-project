package com.android.mqttclient;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.android.mqttclient.model.Constants;
import com.android.mqttclient.model.Message;
import com.android.mqttclient.model.ReceivedHistory;
import com.android.mqttclient.services.MqttServices;
import com.android.mqttclient.services.ReadWriteFile;
import com.android.mqttclient.services.TaskHandler;

import org.json.JSONException;

import java.util.List;

/**
 * A ViewModel class that implements Interface {@link MqttServices.MqttServicesListener} to receive messages from the MQTT.
 */
public class LaunchViewModel  extends ViewModel implements MqttServices.MqttServicesListener {

    private MutableLiveData<String> newTaskReceivedEvent;
    private MutableLiveData<Boolean> onConnectComplete;
    private MutableLiveData<String> onConnectionLost;
    private MutableLiveData<Boolean> onNewtaskExecuted;

    private static final String MyPREFERENCES = "MyPrefs" ;
    private static final String NOTIFICATION_PREF_KEY = "enableNotificationKey" ;
    private static final String SUBSCRIBE_PREF_KEY = "subscribeKey" ;

    private SharedPreferences sharedpreferences;

    private MqttServices mqttServicesInstance;
    private TaskHandler taskHandlerObj;
    private ReadWriteFile rwfInstance;

    public LaunchViewModel() {
        newTaskReceivedEvent = new MutableLiveData<>();
        onConnectComplete = new MutableLiveData<>();
        onConnectionLost = new MutableLiveData<>();
        onNewtaskExecuted = new MutableLiveData<>();
    }

    public LiveData<String> getNewTaskReceived() {
        return newTaskReceivedEvent;
    }

    public LiveData<String> getOnConnectionLost() {
        return onConnectionLost;
    }
    public LiveData<Boolean> getOnConnectComplete() {
        return onConnectComplete;
    }
    public LiveData<Boolean> getOnNewtaskExecuted() {
        return onNewtaskExecuted;
    }

    /**
     * Initiate all instances and listeners.
     */
    public void initMqtt(MqttServices mqttServices) {
        mqttServicesInstance = mqttServices;
        mqttServicesInstance.initMqttClient();
        mqttServicesInstance.connectMqttClient();
        mqttServicesInstance.setListener(this);
        taskHandlerObj = new TaskHandler(mqttServicesInstance);
    }

    public void initReadWriteInstance(ReadWriteFile rwfInstance){
        this.rwfInstance = rwfInstance;
    }

    public void initSharedPref(SharedPreferences preferences){
        this.sharedpreferences = preferences;
    }

    public MqttServices getMqttServicesInstance() {
        return mqttServicesInstance;
    }

    public ReadWriteFile getRwfInstance() {
        return rwfInstance;
    }

    public TaskHandler getTaskHandlerObj() {
        return taskHandlerObj;
    }

    /**
     *Invoked when a message received by {@link MqttServices} from MQTT.
     * @param message is a Message model data object which is used to receive a incoming message from
     *                mqtt broker.
     * @throws JSONException will be thrown when there is a problem with the JSON API.
     */
    @Override
    public void onMessageReceived(Message message){

        final String taskDescriptionData ;
        try {

            taskDescriptionData = getTaskHandlerObj().getTaskDesFromJsonString(message.getMessage());

            final String validUntil = getTaskHandlerObj().getDueDate();
            final byte[] fileMessagePayload = message.getData();

            if (fileMessagePayload != null) {
                System.out.println("when file message came");
                onFileReceived(fileMessagePayload, "");
            } else {
                if (taskDescriptionData != null && taskDescriptionData.length() > 0) {
                    try {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {     // to handle touch the view while not in the UI thread.
                            @Override
                            public void run() {
                                System.out.println("validUntil:"+ validUntil);
                                onNewTaskReceived(taskDescriptionData, validUntil);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Call the method to craete the .apk file on the device memory.
     */
    private void onFileReceived(byte[] fileMessagePayload, String description) {

        String downloadfilename = rwfInstance.createFileFromJson(fileMessagePayload);
        description = rwfInstance.getTaskName();
        rwfInstance.saveTaskListToFile(rwfInstance.getSubTaskId()+";"+description, null, downloadfilename, rwfInstance.getReceivedTaskFileName(), true);
    }

    @Override
    public void onConnectComplete(boolean reconnect) {
        onConnectComplete.postValue(reconnect);
    }

    @Override
    public void onConnectionLost(String errorMessage) {
        onConnectionLost.postValue(errorMessage);
    }

    /**
     * Set MutableLiveData whenever a new task received.
     */
    private void onNewTaskReceived(String description, String validUntil) {
        rwfInstance.saveTaskListToFile(description, validUntil, null, rwfInstance.getReceivedTaskFileName(), false);
        newTaskReceivedEvent.setValue(description);
    }

    /**
     * Set MutableLiveData whenever a task executed.
     */
    public void onExecutedTask(String description) {
        rwfInstance.saveTaskListToFile(description, null, null, rwfInstance.getExecutedTaskFileName(), false);
        onNewtaskExecuted.postValue(true);
    }

    /**
     * Get all executed task details from the file.
     */
    public List<ReceivedHistory> getExecutedTasksFromFile() {
        return (rwfInstance.readFromFile(rwfInstance.getExecutedTaskFileName()));
    }

    /**
     * Get all received task details from the file.
     */
    public List<ReceivedHistory> getReceivedTasksFromFile() {
        return (rwfInstance.readFromFile(rwfInstance.getReceivedTaskFileName()));
    }

    /**
     * Gets the saved NOTIFICATION_PREF_KEY value from shared preferences.
     * @return : NOTIFICATION_PREF_KEY value
     */
    public boolean getNotificationPref() {
        return sharedpreferences.getBoolean(NOTIFICATION_PREF_KEY, false);
    }
    /**
     * Gets the saved NOTIFICATION_PREF_KEY value from shared preferences.
     * @return : NOTIFICATION_PREF_KEY value
     */
    public boolean getSubscriptionPref() {
        return sharedpreferences.getBoolean(SUBSCRIBE_PREF_KEY, false);
    }

    /**
     * Saves user notifcation preference in persistent storage.
     * @param enableNotifications : boolean value based on user selection
     */
    public void saveToPreferences(boolean enableNotifications) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(NOTIFICATION_PREF_KEY, enableNotifications);
        editor.apply();
    }

    /**
     * Saves user subscription preference in persistent storage.
     * @param subscribed : boolean value based on API success/failure
     */
    public void saveSubscriptionState(boolean subscribed) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(SUBSCRIBE_PREF_KEY, subscribed);
        editor.apply();
    }
}