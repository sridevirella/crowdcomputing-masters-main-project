package util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A util class to provides file paths.
 */
public class FilePaths {

    public static Path getReceivedTaskFilePath() {
        return Paths.get("build","resources", "main", "receivedTasks.txt").toAbsolutePath();
    }

    public static Path getTaskPath() {
        return Paths.get("build","resources", "main", "task").toAbsolutePath();
    }

    public static Path getExecutedTaskFilePath() {
        return Paths.get("build","resources", "main", "executedTasks.txt").toAbsolutePath();
    }

    public static String resourcePath() {
        return Paths.get("build","resources", "main").toAbsolutePath().toString();
    }

}
