package com.android.mqttclient.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;

import com.android.mqttclient.BuildConfig;
import com.android.mqttclient.model.ReceivedHistory;
import com.android.mqttclient.model.TaskDetails;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;

/**
 * File util class to handle file data manipulations.
 * Provides util methods for file creation on internal app memory using app context, external storage,
 * parsing json and saving .apk content to file, deleting the file from the device memory after successful execution of .apk.
 */
public class ReadWriteFile {
    private Context context;
    private String fileName;
    private File fdelete;
    private byte[] fileContent;
    private String subTaskId;
    private String taskName;
    private Map<Integer, byte[]> BytesDataHashMap = new HashMap<>();
    private String external_dir_path = Environment.getExternalStorageDirectory().toString()+ "/mqtt_files";
    private static final String RECEIVED_TASK_FILE = "received_task-history.txt";
    private static final String EXECUTED_TASK_FILE = "executed_task-history.txt";
    private boolean externalMemory = false;
    private boolean fileAschunks = false;


    /**
     * Constructor to init ReadWriteFile with application context.
     * @param context: activity {@link Context}
     */
    public ReadWriteFile(Context context){
        this.context = context;
    }

    public ReadWriteFile() {}

    /**
     * Method parse json payload to create a data file.
     * Decode the file content with base64 to get actual file byte array data.
     * Receiving small file chunks: Decode each chunk with base64 will give us byte[] chunk content and store in HashMap with corresponding chunk number.
     * Check for last chunk and once all chunks are received, get each chunk from HashMap in order by its chunk number to create larger byte[] file.
     * @param payLoad :message of type byte array.
     */
    public String createFileFromJson(byte[] payLoad) {

        try {
            JSONObject payLoadjsonObj = new JSONObject(new String(payLoad));
            fileName = payLoadjsonObj.getString("fileName");
            subTaskId = payLoadjsonObj.getString("taskId");
            taskName = payLoadjsonObj.getString("taskName");

            if (fileAschunks) {                                                    // If we receive file chunks
                int fileChunkNumber = payLoadjsonObj.getInt("chunkNumber");  //get chunk number
                System.out.println("Chunk Number: " + fileChunkNumber);

                if (subTaskId.equals(payLoadjsonObj.getString("subTaskId"))) {  // did we received chunk for the same task? or is it for different task
                    storeFileChunkBytesDataInHashMap(Base64.decode(payLoadjsonObj.getString("chunkContent"), Base64.NO_WRAP), fileChunkNumber);  //decode chunk content and store byte[] data in hashmap

                    if (payLoadjsonObj.getString("lastChunk").contentEquals("Yes")) {  // did we receive last chunk?
                        fileContent = mergeFileChunks(BytesDataHashMap);             // we got all chunks so merge all chunks byte[] data to make one big byte[]
                    }
                }
                saveDataToFile(fileName, fileContent);                             // now we have actual byte[] file data, so create physical file on the device
            }
            else {
                // we got a whole file, decode file content to get actual byte[] data and store on internal memory on the device
                System.out.println("file name:"+fileName);
                saveDataToFile(fileName, Base64.decode(payLoadjsonObj.getString("fileContent"), Base64.DEFAULT));

            }
            return fileName;

        } catch (JSONException | IOException e) {
            Log.d("Json Extraction", "Excepion = " + e.getMessage());
        }
        return null;
    }

    /**
     * Reads each chunk byte[] data into larger byte[].
     * @param chunkDataBytesInHashMap receives data as chunk byte[] content value and corresponding chunk number as key
     * @return total bytes to make a large file
     */
    public byte[] mergeFileChunks( Map<Integer, byte[]> chunkDataBytesInHashMap ){

        List<Byte> byteList = new ArrayList<>();
        byte[] fileContentInBytes;

        for( int i = 0; i < chunkDataBytesInHashMap.size(); i++ ){
            byte[] chunkBytes = chunkDataBytesInHashMap.get(i);     //get each chunk by its chunk number
            for( int j = 0; j < chunkBytes.length; j++ ){          //read chunk bytes and add it to arraylist
                byteList.add( chunkBytes[j] );
            }
        }
        fileContentInBytes = new byte[ byteList.size() ];
        for( int l = 0; l < fileContentInBytes.length; l++ ) {    // once we got all bytes then copy them to static byte[]
            fileContentInBytes[l] = byteList.get(l);
        }
        return fileContentInBytes;
    }

    /**
     * HashMap to store chunks data in the order and to avoid duplicates if any.
     * @param chunkDataInBytes : data chunks as byte array
     * @param chunkNumber : chunk number as received in payload.
     */
    public void storeFileChunkBytesDataInHashMap( byte[] chunkDataInBytes, int chunkNumber){

        BytesDataHashMap.put( chunkNumber, chunkDataInBytes );
    }

    /**
     * Creates a file with given file name and byte array content
     * @param fileName : fileName as {@link String}
     * @param content : file content as byte array
     * @throws IOException : Throws {@link IOException} if any
     */
    public void saveDataToFile(String fileName, byte[] content) throws IOException {

        if( externalMemory ) {         //create file on external memory
            File tempDir = new File(external_dir_path);
            tempDir.setReadable(true);
            tempDir.setWritable(true);

            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            File file = new File(tempDir, fileName);
            if (file.exists())
                file.delete();
            try {
                if (content != null) {
                    FileOutputStream fOut = new FileOutputStream(file);
                    fOut.write(content);
                    fOut.flush();
                    fOut.close();
                }
            } catch (Exception e) {
                Log.d("Save file to external", "Exception = " + e.getMessage());
            }
        }else {                           // create file on internal memory
            System.out.println("internal memory");
            try {
                if (content != null) {
                    FileOutputStream fOut = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                    fOut.write(content);
                    fOut.flush();
                    fOut.close();
                }
            }catch(Exception e ){
                Log.d("Save file to internal", "Exception = " + e.getMessage());
            }
        }
    }

    /**
     * deletes the file from the internal memory, if exists, once the user done with the sub task.
     */
    public void deleteFile(String file) {
        if(file == null) {
            fdelete = new File("/data/data/com.android.mqttclient/files/"+fileName);
        } else {
            fdelete = new File("/data/data/com.android.mqttclient/files/"+file);
        }

        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.d("deleteFile ","file deleted");
            } else {
                Log.d("deleteFile ","file not deleted");
            }
        }
    }

    /**
     * Install .apk on the device from the given file path .
     */
  public void initiateInstallation(String downloadFile, String taskId) {
        try {
            File f1 = new File(external_dir_path+"/"+downloadFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);

            TaskDetails.setTaskId(taskId);

            if( externalMemory ) {

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {             // handle SDK versions
                    intent.setDataAndType(FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(external_dir_path + "/" + downloadFile)), "application/vnd.android.package-archive");
                } else {
                    intent.setDataAndType(Uri.fromFile(new File
                    (external_dir_path + "/" + downloadFile)), "application/vnd.android.package-archive");
                }
                } else{

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.setDataAndType(FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File("/data/data/com.android.mqttclient/files/" + downloadFile)), "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                     intent.setDataAndType(Uri.fromFile(new File
                     ("/data/data/com.android.mqttclient/files/"+downloadFile )), "application/vnd.android.package-archive");
                }
            }
            context.startActivity(intent);                     // initiates activity with the given intent
            System.out.println("after start activity");
        }catch (Exception e){
            Log.d("Apk Installation", "Exception = "+e.getMessage());
        }
    }

    /**
     * Uninstalls the apk on the device.
     */
    public void unInstallApk() {
        Uri packageURI = Uri.parse("package:"+"com.mypackage.apkfiletest");
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(uninstallIntent);
        System.out.println("Uninstalled successfully");
    }

    /**
     * Save received task details to the file on the device.
     */
    public void saveTaskListToFile(String task, String validUntil, String downloadFileName, String filename, boolean onFileReceived) {
        try {
            if (context != null) {
                String pattern = "yyyy-MM-dd HH:mm";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.US);
                Date currentDate = new Date(System.currentTimeMillis());
                String datetime = simpleDateFormat.format(currentDate);
                String history = readStringDataFromFile(filename);
                String data = task + "::"+datetime +"::"+validUntil+"::"+downloadFileName+"::"+onFileReceived+"\n"+ history;
                FileOutputStream fOut = context.openFileOutput(filename, Activity.MODE_PRIVATE);
                fOut.write(data.getBytes());
                fOut.flush();
                fOut.close();
            }
        }catch(Exception e ){
            Log.d("Save file to internal", "Exception = " + e.getMessage());
        }
    }

    /**
     * Read task details from the file.
     */
    private String readStringDataFromFile(String filename) {
        String ret = "";
        if(context != null) {
            try {
                InputStream inputStream = context.openFileInput(filename);
                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append("\n").append(receiveString);
                    }

                    inputStream.close();
                    ret = stringBuilder.toString();
                }
            } catch (FileNotFoundException e) {
                Log.e("login activity", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("login activity", "Can not read file: " + e.toString());
            }
        }
        return ret;
    }

    /**
     * Read and parse data from the file.
     * To display the user about the task expiry, check for the task due date expiration.
     */
    public List<ReceivedHistory> readFromFile(String filename) {
        List<ReceivedHistory> ret = new ArrayList<>();
        if(context != null) {
            try {
                InputStream inputStream = context.openFileInput(filename);
                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    while ((receiveString = bufferedReader.readLine()) != null) {
                        if(receiveString.contains("::")) {

                            String[] data = receiveString.split("::");
                            if(data.length >4) {
                                int duration = 0;
                                if(data[2] != null && !data[2].equals("null")) {
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                                    Date validData = formatter.parse(data[2]);
                                    Date currentDate = new Date(System.currentTimeMillis());
                                    duration = (int)((validData.getTime() - currentDate.getTime()) / (1000 * 60));
                                }
                                boolean isFileReceived = false;
                                if(data[3] != null){
                                    isFileReceived = Boolean.parseBoolean(data[4]);
                                }
                                ret.add(new ReceivedHistory(data[0], data[1], duration, data[3], isFileReceived));
                            }
                        }
                    }
                    inputStream.close();
                }
            } catch (FileNotFoundException e) {
                Log.e("login activity", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("login activity", "Can not read file: " + e.toString());
            } catch (ParseException e) {
                Log.e("File read", "Parse exception");
            }
        }
        return ret;
    }

    /**
     * Getter for received tasks file name.
     */
    public String getReceivedTaskFileName(){
        return RECEIVED_TASK_FILE;
    }

    /**
     * Getter for executed task file name.
     */
    public String getExecutedTaskFileName(){
        return EXECUTED_TASK_FILE;
    }

    /**
     * Getter for Task ID and Task Name.
     */
    public String getSubTaskId() { return subTaskId; }

    public String getTaskName() { return taskName; }
}
