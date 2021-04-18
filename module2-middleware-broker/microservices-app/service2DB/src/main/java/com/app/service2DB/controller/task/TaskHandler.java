package com.app.service2DB.controller.task;

import com.app.service2DB.model.tables.*;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class TaskHandler {

    public TaskHandler() {}

    public Task parseJsonMessage(String payLoad) throws JSONException, IOException {

        JSONObject payLoadJsonObj = new JSONObject(payLoad);
        JSONObject fileDetails = payLoadJsonObj.getJSONObject("task").getJSONObject("executableFile");
        JSONObject taskProperties = payLoadJsonObj.getJSONObject("task").getJSONObject("taskProperties");

        return new Task(Uuids.timeBased(), fileDetails.getString("name"), fileDetails.getString("data"), fileDetails.getInt("size"),
                 fileDetails.getString("mimetype"), taskProperties.getString("shortName"), taskProperties.getString("description"),
                doDateFormat(taskProperties.getString("dueDate")), taskProperties.getString("size"), taskProperties.getString("author"), taskProperties.getString("rewards"));

    }

   public String constructPayLoad(Task task, List<Worker> workerList, boolean isItForSubTask) throws JSONException {

        JSONObject taskPayloadJson = new JSONObject();
        if(!isItForSubTask) {
            taskPayloadJson.put("workerList", getJsonFormatList(workerList));
            taskPayloadJson.put("taskId", task.getTaskId());
            taskPayloadJson.put("shortName", task.getShortName());
            taskPayloadJson.put("description", task.getDescription());
            taskPayloadJson.put("dueDate", task.getDueDate());
            taskPayloadJson.put("size", task.getSize());
            taskPayloadJson.put("author", task.getAuthor());
            taskPayloadJson.put("rewards", task.getRewards());
            System.out.println(taskPayloadJson.toString());
        } else {
            taskPayloadJson.put("taskId", task.getTaskId());
            taskPayloadJson.put("taskName", task.getShortName());
            taskPayloadJson.put("fileName", task.getFileName());
            taskPayloadJson.put("fileType", (task.getMimeType().equals("application/vnd.android.package-archive")? ".apk" : ".jar" ));
            taskPayloadJson.put("fileContent", task.getFileContent());
            taskPayloadJson.put("workerList", getJsonFormatList(workerList));
        }
        return taskPayloadJson.toString();
   }

   public List<SubTask> parseJsonForSubTaskDetails(String message) throws JSONException {

        JSONObject jsonObject = new JSONObject(message);
        JSONArray subTaskJsonArray = jsonObject.getJSONArray("WorkerAndTask");
        List<SubTask> subTaskList = new ArrayList<>();

        IntStream.range(0, subTaskJsonArray.length()).forEach(i -> {

           try {
               JSONObject subTaskJson = subTaskJsonArray.getJSONObject(i);
               System.out.println("what subtask>>"+subTaskJson.toString());

               SubTask subTask = new SubTask();
               subTask.setSubTaskKey(new TaskWorkerCompositeKey(UUID.fromString(subTaskJson.getJSONObject("task").getString("taskId")), subTaskJson.getString("workerId")));
               subTask.setSubTask(subTaskJson.getJSONObject("task").getString("fileContent"));
               subTask.setProcessed(false);
               subTaskList.add(subTask);

           } catch (JSONException e) {
               e.printStackTrace();
           }
       });
     return subTaskList;
   }

   public WorkerResponse getWorkerResponseObj(String response) throws JSONException {

        JSONObject workerResObj = new JSONObject(response);
       return new WorkerResponse(new TaskWorkerCompositeKey(UUID.fromString(workerResObj.getString("taskId")), workerResObj.getString("workerId")), workerResObj.getString("accepted"));

   }

   private List<JSONObject> getJsonFormatList(List<Worker> workerList) {

        List<JSONObject> newWorkerList = new ArrayList<>();
        workerList.forEach(worker -> {
            JSONObject jsonObject = new JSONObject(worker);
            newWorkerList.add(jsonObject);
        });
        return newWorkerList;
   }

   public String getJsonString(List<SubTask> subTaskList, Task task) throws JSONException {

        JSONObject subTaskResultsJson = new JSONObject();
        List<String> subTaskResultsList = subTaskList.stream().map(SubTask::getSubTaskResult).collect(Collectors.toList());
        subTaskResultsJson.put("taskId", task.getTaskId());
        subTaskResultsJson.put("taskName", task.getShortName());
        subTaskResultsJson.put("results", subTaskResultsList);
        return subTaskResultsJson.toString();
   }

   private String doDateFormat(String date) {

       String[] tokens = date.split(" ");
       return LocalDateTime.of(Integer.parseInt(tokens[2]) , Integer.parseInt(tokens[0] ), Integer.parseInt(tokens[1]),
               Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), 0).toString();

   }
}
