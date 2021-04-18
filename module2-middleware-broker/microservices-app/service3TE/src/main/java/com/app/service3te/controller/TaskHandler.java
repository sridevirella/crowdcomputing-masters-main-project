package com.app.service3te.controller;

import com.app.service3te.TaskDivision.ExecutableSubTask;
import com.app.service3te.model.Topic;
import com.app.service3te.util.FilePath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

/**
 * The class handles serialization or deserialization of messages, divides the task based on the number of agreed workers,
 * and creates the task implementation java class that requires generating .jar or .apk files.
 */
@Component
public class TaskHandler {

    private String fileName;

    public TaskHandler() {}

    /**
     * Get the assigned task description payload.
     * @param message task description and subscribed worker details as JSON string.
     * @param isItForSubTask true if payload construction is for subtask.
     *                      false, if it is for sending task description to all subscribed workers
     * @return assign task description payload JSON string.
     * @throws JSONException throws any JSON related exceptions.
     * @throws IOException throws any IO related exceptions.
     */
    public String getPayLoadForWorkers(String message, boolean isItForSubTask) throws JSONException, IOException {

        return constructPayLoadForWorkers(message, isItForSubTask).toString();
    }

    /**
     * Constructs task description jSON payload or subtask payload with executable files such as .apk, .jar
     * @param message task and workers details payload that received from the DB service.
     * @param isItForSubTask if the construction of payload is for subtask then true, else false.
     * @return constructed JSON payload with task description (or) executable subtask and worker details.
     * @throws JSONException throws if any JSON related exceptions.
     * @throws IOException throws if any IO related exceptions.
     */
    private JSONObject constructPayLoadForWorkers(String message, boolean isItForSubTask) throws JSONException, IOException {

        JSONObject payLoadJsonObject = new JSONObject(message);
        List<JSONObject> workerAndTaskList = new ArrayList<>();
        JSONObject finalPayload = new JSONObject();
        JSONArray workerJsonData = payLoadJsonObject.getJSONArray("workerList");

        final String[] subTaskContent = {""};
        if(isItForSubTask)
            setFileName(payLoadJsonObject);
        System.out.println("Number of workers who agreed to do the task:" + workerJsonData.length());

        if(!isItForSubTask)
            getPayLoadForTaskDescription(getTaskJson(payLoadJsonObject, false, subTaskContent[0]), workerAndTaskList, workerJsonData);
        else
            getPayloadForSubTask(payLoadJsonObject, workerAndTaskList, workerJsonData, subTaskContent);

        finalPayload.put("WorkerAndTask", workerAndTaskList);
        return finalPayload;
    }

    /**
     * If the received file is .txt, then divide the task into subtasks and generate .apk or .jar depending on the type of worker.
     * If the received file is an executable .apk or .jar, based on the type of worker(android or desktop) assign them directly.
     */
    private void getPayloadForSubTask(JSONObject payLoadJsonObject, List<JSONObject> workerAndTaskList, JSONArray workerJsonData, String[] subTaskContent) throws JSONException, IOException {

        if(fileName.contains(".txt"))
            getPayloadForTextFile(payLoadJsonObject, workerAndTaskList, workerJsonData, subTaskContent);
        else
            getPayloadForExecutableFile(getTaskJson(payLoadJsonObject, true, subTaskContent[0]), workerAndTaskList, workerJsonData);
    }

    /**
     * Invoked when we received a executable file directly as task (.apk or .jar)
     */
    private void getPayloadForExecutableFile(JSONObject taskJson1, List<JSONObject> workerAndTaskList, JSONArray workerJsonData) {

        IntStream.range(0,workerJsonData.length()).forEach(i -> {
            try {
                JSONObject workerAndTaskObj = new JSONObject();
                JSONObject workerJson = workerJsonData.getJSONObject(i);

                if (workerJson.getString("deviceOS").equals("Android") && fileName.contains(".apk") ||
                        workerJson.getString("deviceOS").equals("Desktop") && fileName.contains(".jar")) {
                    constructTaskJsonPayload(workerAndTaskList, workerJson, workerAndTaskObj, taskJson1, Topic.WORKER_SUBTASK);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Invoked when we received a .txt file as task (containing java program as task)
     */
    private void getPayloadForTextFile(JSONObject payLoadJsonObject, List<JSONObject> workerAndTaskList, JSONArray workerJsonData, String[] subTaskContent) throws JSONException, IOException {

        JSONObject decodedFileContent = getDecodedTaskJson(getTaskJson(payLoadJsonObject, true, subTaskContent[0]));
        LinkedHashMap<String, String> inputRangeAndWorkersMap = mapInputRangeAndWorkers(workerJsonData, decodedFileContent);
        generateExecutableFile(payLoadJsonObject, workerAndTaskList, subTaskContent, decodedFileContent, inputRangeAndWorkersMap);
    }

    /**
     * For every input range, pick a worker and generate an executable file of device OS type.
     */
    private void generateExecutableFile(JSONObject payLoadJsonObject, List<JSONObject> workerAndTaskList, String[] subTaskContent, JSONObject decodedFileContent, LinkedHashMap<String, String> inputRangeAndWorkersMap) throws JSONException, IOException {

        for( Map.Entry<String,String> entry : inputRangeAndWorkersMap.entrySet()) {

            String inputRange = entry.getKey();
            JSONObject workerJson = new JSONObject(entry.getValue());
            JSONObject workerAndTaskObj = new JSONObject();

            System.out.println("workerId:: " +workerJson.getString("workerId"));
            ExecutableSubTask subTask =  new ExecutableSubTask(decodedFileContent.getString("className"), Integer.parseInt(inputRange.split(",")[0]), Integer.parseInt(inputRange.split(",")[1]), UUID.fromString(payLoadJsonObject.getString("taskId")));
            checkForTypeOfWorkerAndGenerate(subTaskContent, workerJson, subTask);

            constructTaskJsonPayload(workerAndTaskList, workerJson, workerAndTaskObj, getTaskJson(payLoadJsonObject, true, subTaskContent[0]), Topic.WORKER_SUBTASK);
        }
    }

    /**
     * Construct task payLoad for each worker.
     */
    private void constructTaskJsonPayload(List<JSONObject> workerAndTaskList, JSONObject workerJson, JSONObject workerAndTaskObj, JSONObject taskJson, Topic workerSubtask) throws JSONException {

        workerAndTaskObj.put("task", taskJson);
        workerAndTaskObj.put("workerTopic", workerSubtask.getTopicName() + "/" + workerJson.getString("workerId"));
        workerAndTaskObj.put("workerId", workerJson.getString("workerId"));
        workerAndTaskList.add(workerAndTaskObj);
    }

    /**
     * Check worker device OS and generate an executable file of that type.
     */
    private void checkForTypeOfWorkerAndGenerate(String[] subTaskContent, JSONObject workerJson, ExecutableSubTask subTask) throws JSONException, IOException {

        if(workerJson.getString("deviceOS").equals("Android")) {
            subTaskContent[0] = subTask.getEncodedStringOfExecutableFile(FilePath.getAndroidSourceCodeFolderPath().toString(), ".apk");
        }
        else if(workerJson.getString("deviceOS").equals("Desktop")) {
            subTaskContent[0] = subTask.getEncodedStringOfExecutableFile(FilePath.getJarSourceCodeFolderPath().toString(), ".jar");
        }
    }

    /**
     * Assign each task input range to each worker who agreed to run the task.
     */
    private LinkedHashMap<String, String> mapInputRangeAndWorkers(JSONArray workerJsonData, JSONObject decodedFileContent) throws JSONException, IOException {

        List<String> inputRangeList = createJavaFileAndGetInputRanges(decodedFileContent, workerJsonData.length());
        LinkedHashMap<String, String> inputRangeAndWorkersMap = new LinkedHashMap<>();

        for (int i = 0; i < inputRangeList.size(); i++) {
            inputRangeAndWorkersMap.put(inputRangeList.get(i), workerJsonData.get(i).toString());
        }
        return inputRangeAndWorkersMap;
    }

    /**
     * Construct payload to send the task description to the all subscribed workers.
     */
    private void getPayLoadForTaskDescription(JSONObject taskJson1, List<JSONObject> workerAndTaskList, JSONArray workerJsonData) {

        IntStream.range(0,workerJsonData.length()).forEach(i -> {
            try {
                JSONObject workerJson = workerJsonData.getJSONObject(i);
                JSONObject workerAndTaskObj = new JSONObject();
                constructTaskJsonPayload(workerAndTaskList, workerJson, workerAndTaskObj, taskJson1, Topic.WORKER_TASK);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Construct JSON payload.
     */
    private JSONObject getTaskJson(JSONObject jsonObject, boolean isItForSubTask, String subTaskFileContent) throws JSONException {

        JSONObject finalJson = new JSONObject();
        JSONObject taskJson = new JSONObject();

        if(!isItForSubTask) {
            taskJson.put("description", jsonObject.getString("description"));
            taskJson.put("shortName", jsonObject.getString("shortName"));
            taskJson.put("rewards", jsonObject.getString("rewards"));
            taskJson.put("size", jsonObject.getString("size"));
            taskJson.put("author", jsonObject.getString("author"));
            taskJson.put("dueDate", jsonObject.getString("dueDate"));

            finalJson.put("taskDescription", taskJson);
            finalJson.put("taskId", jsonObject.getString("taskId"));
            finalJson.put("workerResponseTopic", Topic.WORKER_TASK_RESPONSE.getTopicName());
            finalJson.put("workerSubTaskTopic", Topic.WORKER_SUBTASK.getTopicName());
        }
        else {
            finalJson.put("taskId", jsonObject.getString("taskId"));
            finalJson.put("taskName", jsonObject.getString("taskName"));
            finalJson.put("workerSubTaskRespTopic", Topic.WORKER_SUBTASK_RESPONSE.getTopicName());
            System.out.println("length:"+ subTaskFileContent.length());
            finalJson.put("fileName", (subTaskFileContent.length() == 0) ? jsonObject.getString("fileName") : "ExecutableFile");
            finalJson.put("fileContent", (subTaskFileContent.length() == 0) ? jsonObject.getString("fileContent") : subTaskFileContent);
        }
        return finalJson;
    }

    /**
     * Setter for fileName.
     */
    private void setFileName(JSONObject jsonObject) throws JSONException {

        System.out.println("fileName:"+ jsonObject.getString("fileName"));
        fileName = jsonObject.getString("fileName");
    }

    /**
     * Create the task implementation java file that requires for generation of .apk or .jar files.
     */
    private List<String> createJavaFileAndGetInputRanges(JSONObject textFileContentJson, int numberOfDivisions) throws JSONException, IOException {

        String className = textFileContentJson.getString("className");
        List<String> subTaskInputRanges= new ArrayList<>();

        createJavaFileFromTextFileContent(textFileContentJson, className);
        inputRangeDivision(textFileContentJson, numberOfDivisions, subTaskInputRanges);
        return subTaskInputRanges;
    }

    /**
     * Create TaskImplementation.java file accordingly for both .apk and .jar generation.
     */
    private void createJavaFileFromTextFileContent(JSONObject textFileContentJson, String className) {

        Arrays.asList(FilePath.getPathToAddTaskToAndroidFolder(), FilePath.getPathToAddTaskToJarFolder()).forEach(pathValue -> {

            File newFile = new File(Paths.get(pathValue.toString(), className + ".java").toString());
            try {
                if (newFile.createNewFile())
                    System.out.println("file created successfully");

                BufferedWriter bw = new BufferedWriter(new FileWriter(String.valueOf(Paths.get(pathValue.toString(), className + ".java"))));
                if (pathValue.equals(FilePath.getPathToAddTaskToAndroidFolder()))
                    bw.write("package com.mypackage.apkfiletest;\n" + textFileContentJson.getString("code"));
                else
                    bw.write("package jarfiletest;\n" + textFileContentJson.getString("code"));
                bw.flush();
                bw.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Read input range from the text file and divide the ranges based on the number of workers who agreed to run the task on their device.
     */
    private void inputRangeDivision(JSONObject textFileContentJson, int numberOfDivisions, List<String> subTaskInputRanges) throws JSONException {

        String[] inputRange = textFileContentJson.getString("inputRange").split(",");
        int divisionValue = Integer.parseInt(inputRange[1]) / numberOfDivisions;
        int previousValue = divisionValue;
        subTaskInputRanges.add(inputRange[0] +","+divisionValue);

        if(numberOfDivisions > 1) {
            for (int i = 2; i <= numberOfDivisions - 1; i++) {

                int startIndex = previousValue + 1;
                int endIndex = i * divisionValue;

                subTaskInputRanges.add(startIndex + "," + endIndex);
                previousValue = endIndex;
            }
            subTaskInputRanges.add(previousValue + 1 + "," + inputRange[1]);
        }
    }

    /**
     * Decode the file content with base64 to get the original file content byte array and construct JSON.
     */
    private JSONObject getDecodedTaskJson(JSONObject taskPayload) throws JSONException {

        byte[] byteArrayPayLoad = Base64.getDecoder().decode(taskPayload.getString("fileContent"));
        System.out.println("file content string:\n"+new String(byteArrayPayLoad));
        String[] stringPayloadArray = new String(byteArrayPayLoad).split(";;");

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("className", stringPayloadArray[0].split(":")[1]);
        jsonPayload.put("inputRange", stringPayloadArray[1].split(":")[1]);
        StringBuilder sb = new StringBuilder();
        jsonPayload.put("code", sb.append(stringPayloadArray[2].split(":")[1]).toString());
        return jsonPayload;
    }
}
