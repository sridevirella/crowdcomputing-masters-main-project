package model;

/**
 * A Enum class to define the constants.
 */
public enum Constants {

    MAIN_TOPIC("mcc/worker/#"),
    WORKER_SUBSCRIPTION("mcc/worker/subscribe"),
    WORKER_UN_SUBSCRIPTION("mcc/worker/unsubscribe"),
    WORKER_TASK("mcc/worker/taskDescription"),
    WORKER_ID("CCDesktop01"),
    WORKER_TASK_RESPONSE("mcc/worker/task_response"),
    WORKER_SUBTASK("mcc/worker/subtask"),
    WORKER_SUBTASK_RESPONSE("mcc/worker/subtask_response"),
    QOS("2"),
    RETAINED("false"),
    URI("tcp://localhost:1883"),
    EXECUTABLE_FILE_NAME("ExecutableFile.jar");

    private final String constant;

    Constants(String constant) {
        this.constant = constant;
    }

    public String getConstant() {
        return constant;
    }
}
