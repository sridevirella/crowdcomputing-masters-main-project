package controller;

import model.Constants;
import org.json.JSONException;
import org.json.JSONObject;
import util.FilePaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * File util class to handle file data manipulations.
 * Provides util methods for message parsing, file creation on device memory, save all received task to the file.
 * Executes the .jar file and construct the computed result in the form of JSON.
 */

public class ReadWriteFile {

    private static String fileName;
    private static JSONObject result;

    private ReadWriteFile() {}

    /**
     * Decode the base64 encoded file content to get the original byte[] of the file and create a .jar file on the device memory.
     * Example payload: {
     * 		    "taskId": "a206e620-8d85-11eb-8ca7-fd5e75e3e45e",
     * 			"taskName": "Prime numbers calculation",
     * 			"workerSubTaskRespTopic": "cc/worker/subTaskResponse",
     * 			"fileName": "TestJarFile.apk",
     * 			"fileContent": ""
     *         }
     * @param payLoad is of type byte[]
     * @throws JSONException throws any JSON related exceptions.
     * @throws IOException throws any IO related exceptions.
     */
    public static void createFileFromJson(byte[] payLoad) throws JSONException, IOException {

        JSONObject payLoadJsonObj = new JSONObject(new String(payLoad));
        fileName = payLoadJsonObj.getString("fileName");
        saveExecutableFileTaskDetails(payLoadJsonObj.getString("taskId") + ";Received executable File for : " +payLoadJsonObj.getString("taskName"),
                FilePaths.getReceivedTaskFilePath().toString());
        saveDataToFile(Constants.EXECUTABLE_FILE_NAME.getConstant() , java.util.Base64.getDecoder().decode(payLoadJsonObj.getString("fileContent")));
    }

    /**
     * Save the executable .jar file details in the receivedTasks.txt.
     * @param taskId is of type String.
     * @param path The receivedTasks.txt file location on the device.
     * @throws IOException throws any IO related exceptions.
     */
    private static void saveExecutableFileTaskDetails(String taskId, String path) throws IOException {

        FileWriter fw = new FileWriter(path, true);
        fw.append(taskId);
        fw.append("\n");
        fw.flush();
        fw.close();
    }

    /**
     * Get all the tasks from the given file path.
     * @param path The corresponding file location on the device.
     * @return List of tasks.
     * @throws IOException trows any IO related exceptions.
     */
    public static List<String> getTaskDetailsFromReceivedTaskFile(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        List<String> receivedOrExecutedTasksList = br.lines().collect(Collectors.toList());
        br.close();
        return receivedOrExecutedTasksList;
    }

    /**
     * Initiates the .jar file execution through command line arguments.
     * @param executableTaskDetails contains taskId that is of type String.
     * @throws IOException
     * @throws InterruptedException
     * @throws JSONException
     */
    public static void initiateTaskExecution(String executableTaskDetails) throws IOException, InterruptedException, JSONException {

        Process p = Runtime.getRuntime().exec("java -jar " + Paths.get(FilePaths.getTaskPath().toString(), Constants.EXECUTABLE_FILE_NAME.getConstant()).toString());
        BufferedInputStream bis = new BufferedInputStream(p.getInputStream());

        System.out.println("Jar execution initiated");
        int err = 0;
        BufferedInputStream errBis = new BufferedInputStream(p.getErrorStream());
        while ((err = errBis.read()) > 0) {
            System.out.print((char) err);
        }

        synchronized (p) {
            p.waitFor();
        }
        System.out.println(p.exitValue());
        int res = 0;
        StringBuilder subTaskResult = new StringBuilder();

        while ((res = bis.read()) > 0) {

            System.out.print((char) res);
            subTaskResult.append((char) res);
        }
        if(fileName.contains(".jar")) {
            setTaskResult(constructSubTaskResJson(subTaskResult.toString(), executableTaskDetails));
        }

        else {
            JSONObject subTaskResultJson = new JSONObject(subTaskResult.toString());
            setTaskResult(subTaskResultJson);
        }
        bis.close();
        errBis.close();
    }

    /**
     * Setter for executed task result.
     * @param taskResult is of type JSONObject.
     */
    private static void setTaskResult(JSONObject taskResult) {
        result = taskResult;
    }

    /**
     * getter for executed task result.
     * @return
     */
    public static JSONObject getTaskResult() {
        return result;
    }

    /**
     * Create .jar file on the device memory with given file content.
     * @param fileName is of type String.
     * @param fileContent is of type byte array.
     * @throws IOException throws any IO related exception.
     */
    private static void saveDataToFile(String fileName, byte[] fileContent) throws IOException {

        if (fileContent != null) {

            if (!Files.exists(Paths.get(FilePaths.getTaskPath().toString()))) {
                System.out.println("file doesn't exist");
                Files.createDirectories(FilePaths.getTaskPath());
            }
            FileOutputStream fos = new FileOutputStream(Paths.get(FilePaths.getTaskPath().toString(), fileName).toString());
            fos.write(fileContent);
            fos.flush();
            fos.close();
        }

    }

    /**
     * Construct executed task result JSON.
     * @param subTaskResult executed task result is of type string.
     * @param executableTaskDetails taskId is of type String.
     * @return A executed task result of type JSON object.
     * @throws JSONException throws any JSON related exceptions.
     */
    private static JSONObject constructSubTaskResJson(String subTaskResult, String executableTaskDetails) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("taskId", executableTaskDetails.split(";")[0].split(":")[1].trim());
        jsonObject.put("workerId", Constants.WORKER_ID.getConstant());
        jsonObject.put("subTaskResult", subTaskResult);
        return jsonObject;
    }

    /**
     * Save details of the executed task details.
     * For now we are saving executed task ID, task Name.
     * @param executedTaskDetails executed task details if of type String.
     * @throws IOException throws any IO related exception.
     */
    public static void saveExecutedTaskDetails(String executedTaskDetails) throws IOException {

        saveExecutableFileTaskDetails(executedTaskDetails.split(";")[1].split(":")[1].trim(), FilePaths.getExecutedTaskFilePath().toString());
    }
}
