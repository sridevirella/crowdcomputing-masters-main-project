package com.app.service3te.TaskDivision;

import java.io.File;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

/**
 * The class provides methods to initiates Gradle tasks to generate .apk (or) .jar dynamically.
 */
public class ExecutableFileBuilder {

    public ExecutableFileBuilder(String sourceFilePath) {
        performGradleTasks(sourceFilePath, "build");
    }

    /**
     * From the given path, it picks up the android project source code (or) java project
     * to generate .apk (or) .jar depending on the worker device OS.
     */
    private void performGradleTasks(String path, String ... tasks)
    {
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(new File(path));
        ProjectConnection connection = connector.connect();

        try {
            BuildLauncher launcher = connection.newBuild();
            launcher.forTasks(tasks);
            launcher.setStandardOutput(System.out);
            launcher.setStandardError(System.err);
            launcher.run();
        } finally {
            connection.close();
        }
    }
}
