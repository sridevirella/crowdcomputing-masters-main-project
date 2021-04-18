package com.app.service3te.TaskDivision;

import com.app.service3te.util.FilePath;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

public class ExecutableSubTask {

    private final String className;
    private final int startIndex;
    private final int endIndex;
    private final UUID taskId;

    public ExecutableSubTask(String className, int startIndex, int endIndex, UUID taskId) {

        this.className = className;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.taskId = taskId;
    }

    /**
     * Invokes methods to create TaskImplementation.java, to generate executable file,
     * and to encode .apk or .jar content with base64.
     */
    public String getEncodedStringOfExecutableFile(String path, String fileType) throws IOException {

        createTaskImplementationClass(fileType);
        new ExecutableFileBuilder(path);
        return readExecutableFileDataAndEncode(fileType);
    }

    /**
     * Creates the TaskImplementation.java file with given input ranges and
     * saves the file to the corresponding android or java project folder.
     */
    private void createTaskImplementationClass(String fileType) throws IOException {

        StringBuilder sb = new StringBuilder();

        if(fileType.equals(".apk"))
            sb.append("package com.mypackage.apkfiletest;\n");
        else
            sb.append("package jarfiletest;\n");
        sb.append("import java.util.*;\npublic class TaskImplementation {\n" +
                "public TaskImplementation(Results results) { initiateTask(results);\n" +
                "    }\npublic void initiateTask(Results results) {\nMap<String, String> jsonObject = new HashMap<>();\n").append(className).append(" taskObject = new ").append(className).append("(").append(startIndex).append(",").append(endIndex).append(");\n")
                .append("jsonObject.put(\"result\", taskObject.getResult());\n").append("jsonObject.put(\"taskId\", \"").append(taskId).append("\");\n")
                .append("results.setResult(jsonObject.toString());\n}\n}");

        BufferedWriter bw;
        if(fileType.equals(".apk"))
            bw = new BufferedWriter( new FileWriter(String.valueOf(Paths.get( FilePath.getPathToAddTaskToAndroidFolder().toString(), "TaskImplementation.java"))));
        else
            bw = new BufferedWriter( new FileWriter(String.valueOf(Paths.get( FilePath.getPathToAddTaskToJarFolder().toString(), "TaskImplementation.java"))));

        bw.write(sb.toString());
        bw.flush();
        bw.close();
        System.out.println("task implementation file updated");
    }

    /**
     * Get the base64 encode string for the generated .apk (or) .jar file content.
     */
    private String readExecutableFileDataAndEncode(String fileType) throws IOException {

        byte[] executableFileContent = (fileType.equals(".apk")) ? Files.readAllBytes(FilePath.getApkFilePath()) : Files.readAllBytes(FilePath.getJarFilePath());
        return Base64.getEncoder().encodeToString(executableFileContent);
    }
}
