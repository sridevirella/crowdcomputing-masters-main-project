package com.android.mqttclient.services;

import android.util.Log;

import com.android.mqttclient.model.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The class provides methods to serialize and deserialize from String to JSON payload vice versa.
 * Provides Methods to construct worker task acceptance response and executed task result response.
 */
public class TaskHandler {

    private MqttServices mqttServicesInstance;
    private String dueDate = "";

    /**
     * Constructor to initiate {@link TaskHandler} class instance to share across the application.
     * @param mqttServices : instance of class {@link MqttServices}
     */
    public TaskHandler(MqttServices mqttServices){
        if(mqttServicesInstance == null) {
            mqttServicesInstance = mqttServices;
        }
    }

    /**
     * If worker agreed to run the task on the device, send the worker acceptance JSON to middleware broker.
     * content. If task rejected do nothing.
     * @param clientID : is of type {@link String}.
     * @param response : is of type {@link String}.
     */
    public void taskResponse(String clientID, String response, String taskId){
        try {
            String  responseJsonString = getResponseJsonString( clientID, response, taskId );
            if( response.trim().equals("Yes") ) {
                //create unique response topic for the worker
                System.out.println("at worker acceptance:"+ Constants.WORKER_TASK_RESPONSE.getConstant());
                mqttServicesInstance.publishMessage( Constants.WORKER_TASK_RESPONSE.getConstant() +"/"+ clientID, responseJsonString );
                mqttServicesInstance.subscribeToTopic(Constants.WORKER_SUBTASK.getConstant() + "/" + clientID);
            }
            else if( response.trim().equals("No") ){
                // they said no, so we do not send them .apk
                Log.d("taskResponse()","worker said no to the task");
            }
            else{
                Log.d("taskResponse()","response empty");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Deserialize task description String to Json Object and parse to get all key value pairs
     * to display task description content on the UI.
     * @param taskDescriptionJsonString : Task description is of type Json serialized {@link String}
     * @return : Task description content of type {@link String}
     * @throws JSONException : throws {@link JSONException} if any.
     */
    public String getTaskDesFromJsonString( String taskDescriptionJsonString) throws JSONException {

        StringBuilder taskDescriptionString = new StringBuilder();

        if( taskDescriptionJsonString != null) {

            JSONObject taskDescriptionJsonObj = new JSONObject(taskDescriptionJsonString);
            JSONObject taskDescObject = taskDescriptionJsonObj.getJSONObject("taskDescription");

            taskDescriptionString.append("Task Id: ").append(taskDescriptionJsonObj.getString("taskId")).append(";")
                    .append("Short Name: ").append(taskDescObject.getString("shortName")).append(";")
                    .append("Description: ").append(taskDescObject.getString("description")).append(";")
                    .append("Author: ").append(taskDescObject.getString("author")).append(";")
                    .append("Size: ").append(taskDescObject.getString("size")).append(";")
                    .append("Rewards: ").append(taskDescObject.getString("rewards")).append(";")
                    .append("Due Date: ").append(taskDescObject.getString("dueDate").replace("T", " ")).append(";");

            dueDate = formatDate(taskDescriptionJsonObj.getJSONObject("taskDescription").getString("dueDate"));
            dueDate = dueDate.split(" ")[0] + " " + dueDate.split(" ")[1].trim();
            return taskDescriptionString.toString();
        }
        else{
            return taskDescriptionString.toString();
        }
    }

    private String formatDate(String dueDate) {
        return dueDate.split("T")[0]+ " "+ dueDate.split("T")[1];
    }

    /**
     * Creates task response Json String.
     * @param clientId : is of type {@link String}
     * @param res : "Yes"- response is of type {@link String}
     * @return : task response as a {@link String}
     * @throws JSONException : throws {@link JSONException} if any.
     */
    public String getResponseJsonString( String clientId, String res, String taskId ) throws JSONException {

        JSONObject responseJsonObj = new JSONObject();
        responseJsonObj.put("workerId", clientId );
        responseJsonObj.put("accepted", res);
        responseJsonObj.put("taskId", taskId.trim());
        return responseJsonObj.toString();
    }

    /**
     * Getter for due date.
     */
    public String getDueDate() {
        return dueDate;
    }
}