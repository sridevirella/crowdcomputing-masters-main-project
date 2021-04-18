package com.app.service3te.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A util class that provides file paths.
 */
public class FilePath {

    private FilePath() {}

    public static Path getPathToAddTaskToAndroidFolder() {
        return Paths.get(getAndroidSourceCodeFolderPath().toString(), "app", "src",
                              "main", "java", "com", "mypackage", "apkfiletest").toAbsolutePath();
    }

    public static Path getApkFilePath() {
        return Paths.get(getAndroidSourceCodeFolderPath().toString(), "app", "build", "outputs", "apk", "debug", "app-debug.apk" ).toAbsolutePath();
    }

    public static Path getAndroidSourceCodeFolderPath() {
        return Paths.get(getSourceCodeFolderPath().toString(), "ApkFileTest").toAbsolutePath();
    }

    private static Path getSourceCodeFolderPath() {
        return Paths.get("service3TE","src","main", "resources").toAbsolutePath();
    }

    public static Path getJarSourceCodeFolderPath() {
        return Paths.get(getSourceCodeFolderPath().toString(), "JarFileTest").toAbsolutePath();
    }

    public static Path getPathToAddTaskToJarFolder() {
        return Paths.get(getJarSourceCodeFolderPath().toString(), "src", "main", "java", "jarfiletest").toAbsolutePath();
    }

    public static Path getJarFilePath() {
        return Paths.get(getJarSourceCodeFolderPath().toString(), "build", "libs", "JarFileTest-1.0-SNAPSHOT.jar").toAbsolutePath();
    }
}