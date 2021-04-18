package view;

import controller.MqttServices;
import controller.ReadWriteFile;
import controller.TaskHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import model.Constants;
import model.Message;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;
import util.FilePaths;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller class that implements Interface {@link MqttServices} to messages from the MQTT.
 */
public class GuiController implements MqttServices.MqttServicesListener {

    private final TaskHandler taskHandlerObj;
    private final MqttServices mqttServicesInstance;
    private final GuiElements guiElements;
    private static AlertBox alertBoxView;
    private static  ListView<String> taskListView;
    private final Stage stage;

    public GuiController(Stage stage) throws MqttException {

        this.mqttServicesInstance = new MqttServices();
        this.stage = stage;
        this.guiElements = new GuiElements(stage, mqttServicesInstance);
        initConnectionAndListeners();
        this.taskHandlerObj = new TaskHandler(mqttServicesInstance);
    }

    /**
     * Initiate MQTT connection setup and set current instance as listener to {@link MqttServices.MqttServicesListener}.
     * Initiate all UI elements.
     * @throws MqttException throws any mqtt related exceptions.
     */
    private void initConnectionAndListeners() throws MqttException {

        mqttServicesInstance.setListener(this);
        mqttServicesInstance.initiateConnection();
        guiElements.initViews();
    }

    /**
     * Invoked when a message received by {@link MqttServices} from MQTT.
     * @param message message of type Message.
     * @throws JSONException throws any JSON related exceptions.
     * @throws IOException throws input output exceptions.
     */
    @Override
    public void onMessageReceived(Message message) throws JSONException, IOException {

        final String taskDescriptionData = taskHandlerObj.getTaskDesFromJsonString(message.getMessage());
        final byte[] fileMessagePayload = message.getData();

        //execute the updates on the JavaFX application thread.
        Platform.runLater(() -> {

            if (taskDescriptionData != null && taskDescriptionData.length() > 0)
                handleTaskDescription();

            else if (fileMessagePayload != null)
                handleExecutableFile(fileMessagePayload);
        });
    }

    /**
     * Based on user action, generate task acceptance or reject response for the user-selected task from the list view.
     */
    private void handleTaskDescription() {

        try {
            List<String> finalReceivedTasksList = addTaskDetailsToListView();

            taskListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

                Optional<ButtonType> result = getUserActionForTaskDescription();
                generateTaskResponse(result, finalReceivedTasksList);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a .jar file on the device with file payload and run the .jar
     * file dynamically after the user clicks on the run button.
     * @param fileMessagePayload .jar file data as byte[]
     */
    private void handleExecutableFile(byte[] fileMessagePayload) {

        try {
            ReadWriteFile.createFileFromJson(fileMessagePayload);
            List<String> finalReceivedTasksList = addTaskDetailsToListView();

            taskListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

                Optional<ButtonType> result = userAcceptanceToRunTask();
                runTheExecutableFile(finalReceivedTasksList, result);
            });

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read task description details from receivedTasks.txt file, format and add details to list view.
     * @return a list of task description details.
     * @throws IOException throws any Input Output exceptions.
     */
    private List<String> addTaskDetailsToListView() throws IOException {

        taskListView = new ListView<>();
        List<String> receivedTasksList = new ArrayList<>();

        receivedTasksList = addDataToListView(receivedTasksList);
        guiElements.getBorderPane().setCenter(taskListView);
        return receivedTasksList;
    }

    /**
     * If the user accepts the task, generate a response with "Yes". If he rejects the task, generate a response with "No".
     * @param result user action that specifies either be accepted or rejected the task.
     * @param finalReceivedTasksList list of all the received tasks.
     */
    private void generateTaskResponse(Optional<ButtonType> result, List<String> finalReceivedTasksList) {

        try {
            if (result.get().getButtonData().toString().equals("OK_DONE"))
                taskHandlerObj.taskResponse(Constants.WORKER_ID.getConstant(), "Yes", finalReceivedTasksList.get(taskListView.getSelectionModel().getSelectedIndex()));
            else
                taskHandlerObj.taskResponse(Constants.WORKER_ID.getConstant(), "No", finalReceivedTasksList.get(taskListView.getSelectionModel().getSelectedIndex()));  //set task response as No
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Display an alert box to the user to accept or reject the task.
     * @return A user action that return either Ok_DONE or CLOSE_CANCEL of type Optional<ButtonType>.
     */
    private Optional<ButtonType> getUserActionForTaskDescription() {

        alertBoxView = new AlertBox("Confirmation", "Accept to receive and run this task", stage, false);
        guiElements.getBorderPane().setTop(alertBoxView.getAlertBox());
        return alertBoxView.showAndWait();
    }

    /**
     * Read all task details from the file receivedTasks.txt and add each task as a string to the list view.
     * @param receivedTasksList an empty list to store all the received tasks.
     * @return a list with all received task description and executable .jar file details(such as taskId).
     * @throws IOException
     */
    private List<String> addDataToListView(List<String> receivedTasksList) throws IOException {

        try {
            receivedTasksList = ReadWriteFile.getTaskDetailsFromReceivedTaskFile(FilePaths.getReceivedTaskFilePath().toString());
            ObservableList<String> listVewData = FXCollections.observableArrayList(taskHandlerObj.formatTaskDetails(receivedTasksList));
            taskListView.setItems(listVewData);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return receivedTasksList;
    }

    /**
     * On user acceptance, run the executable task dynamically.
     * Save the executed task details to the executedTasks.txt.
     * @param finalReceivedTasksList
     * @param result
     */
    private void runTheExecutableFile(List<String> finalReceivedTasksList, Optional<ButtonType> result) {

        if (result.get().getButtonData().toString().equals("OK_DONE")) {

            try {
                ReadWriteFile.initiateTaskExecution(finalReceivedTasksList.get(taskListView.getSelectionModel().getSelectedIndex()));
                publishTaskResult();
                ReadWriteFile.saveExecutedTaskDetails(finalReceivedTasksList.get(taskListView.getSelectionModel().getSelectedIndex()));

            } catch (IOException | InterruptedException | JSONException | MqttException e) {
                e.printStackTrace();
            }

        } else
            System.out.println("don't execute the jar file");
    }

    /**
     * Display an alert box to the user to run the executable file or task.
     * @return A user action that return Ok_DONE of type Optional<ButtonType>.
     */
    private Optional<ButtonType> userAcceptanceToRunTask() {

        alertBoxView = new AlertBox("Task", "Executable Task", stage, true);
        alertBoxView.setAlertBoxContent("Received Executable file\n click OK to run");
        guiElements.getBorderPane().setTop(alertBoxView.getAlertBox());
        Optional<ButtonType> result = alertBoxView.showAndWait();
        return result;
    }

    /**
     * Create and publish JSON payload of computed results after running the executable jar file.
     * @throws JSONException
     * @throws MqttException
     */
    private void publishTaskResult() throws JSONException, MqttException {

        JSONObject taskResultJsonObj = new JSONObject();
        JSONObject subTaskResult = ReadWriteFile.getTaskResult();
        taskResultJsonObj.put("taskId", subTaskResult.getString("taskId"));
        taskResultJsonObj.put("subTaskResult", subTaskResult.getString("result"));
        taskResultJsonObj.put("workerId", Constants.WORKER_ID.getConstant());

        mqttServicesInstance.publishMessage( Constants.WORKER_SUBTASK_RESPONSE.getConstant(), taskResultJsonObj.toString() );
    }
}
