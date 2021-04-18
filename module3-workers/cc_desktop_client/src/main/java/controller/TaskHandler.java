package controller;

import model.Constants;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;
import util.FilePaths;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The class provides methods to serialize and deserialize from String to JSON payload vice versa.
 * Provides Methods to construct worker task acceptance response and executed task result response.
 */
public class TaskHandler {

    private MqttServices mqttServicesInstance;

    public TaskHandler(MqttServices mqttServices) {
        mqttServicesInstance = mqttServices;
    }

    /**
     * If worker agreed to run the task on the device, send the worker acceptance JSON to middleware broker.
     * @param clientID worker ID is of type String.
     * @param response Worker response either "Yes" or "No"
     * @param taskDetails received task description details.
     * @throws JSONException throws any JSON related exceptions.
     * @throws MqttException throws any IO related exceptions.
     */
    public void taskResponse(String clientID, String response, String taskDetails) throws JSONException, MqttException {

        String responseJsonString = getResponseJsonString(clientID, response, taskDetails);

        if (response.trim().equals("Yes")) {
            mqttServicesInstance.publishMessage(Constants.WORKER_TASK_RESPONSE.getConstant() + "/" + clientID, responseJsonString);
            mqttServicesInstance.subscribeToTopic(Constants.WORKER_SUBTASK.getConstant() + "/" + clientID);
        } else if (response.trim().equals("No")) {
            System.out.println("worker declined the task");
        } else {
            System.out.println("Response empty");
        }
    }

    /**
     * Deserialize the message from JSON to String to construct task description string to display on the UI.
     * @param taskDescriptionJsonString task description payload received by the middleware broker.
     * @return deserialized task description string.
     * @throws JSONException throws any JSON related exceptions.
     * @throws IOException throws any IO related exceptions.
     */
    public String getTaskDesFromJsonString(String taskDescriptionJsonString) throws JSONException, IOException {

        StringBuilder taskDescriptionString = new StringBuilder();

        if (taskDescriptionJsonString != null) {

            JSONObject taskDescriptionJsonObj = new JSONObject(taskDescriptionJsonString);
            JSONObject taskDescObject = taskDescriptionJsonObj.getJSONObject("taskDescription");
            deserializeTaskDescJSON(taskDescriptionString, taskDescriptionJsonObj, taskDescObject);
            saveTaskDescriptionToTheFile(taskDescriptionString.toString());
        }
        return taskDescriptionString.toString();
    }

    /**
     * Deserialize from JSON to String.
     */
    private void deserializeTaskDescJSON(StringBuilder taskDescriptionString, JSONObject taskDescriptionJsonObj, JSONObject taskDescObject) throws JSONException {

        taskDescriptionString.append("taskId:" + taskDescriptionJsonObj.getString("taskId")).append(";");
        taskDescriptionString.append("Short Name: " + taskDescObject.getString("shortName")).append(";");
        taskDescriptionString.append("Description: " + taskDescObject.getString("description")).append(";");
        taskDescriptionString.append("Rewards: " + taskDescObject.getString("rewards")).append(";");
        taskDescriptionString.append("Size: " + taskDescObject.getString("size")).append(";");
        taskDescriptionString.append("Author: " + taskDescObject.getString("author")).append(";");
        taskDescriptionString.append("Due Date: " + taskDescObject.getString("dueDate")).append(";");
    }

    /**
     * Save the task description details in the file receivedTasks.txt.
     */
    private void saveTaskDescriptionToTheFile(String taskDescription) throws IOException {

        FileWriter fw = new FileWriter(FilePaths.getReceivedTaskFilePath().toString(), true);
        fw.append(taskDescription);
        fw.append("\n");
        fw.flush();
        fw.close();
    }

    /**
     * Construct the JSON response for the task acceptance by the worker.
     */
    public String getResponseJsonString(String clientId, String res, String taskDetails) throws JSONException {

        JSONObject responseJsonObj = new JSONObject();
        responseJsonObj.put("workerId", clientId);
        responseJsonObj.put("accepted", res);
        responseJsonObj.put("taskId", taskDetails.split(";")[0].split(":")[1].trim());

        return responseJsonObj.toString();
    }

    /**
     * Format or trim the Task ID from the task details to display rest of the details on the UI.
     */
    public List<String> formatTaskDetails(List<String> taskList) {

        List<String> taskDetailsList = new ArrayList<>();

        taskList.forEach( task -> {

            StringBuilder stringBuilder = new StringBuilder();
            String[] taskDetails = task.split(";");

            for (int i = 1; i < taskDetails.length; i++) {
                stringBuilder.append(taskDetails[i]).append("\n");
            }
            taskDetailsList.add(stringBuilder.toString());
        });

        return taskDetailsList;
    }
}
