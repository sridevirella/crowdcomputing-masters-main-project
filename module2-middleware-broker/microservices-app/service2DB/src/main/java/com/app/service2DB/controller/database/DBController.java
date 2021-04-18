package com.app.service2DB.controller.database;

import com.app.service2DB.controller.task.TaskHandler;
import com.app.service2DB.model.MessagingChannel;
import com.app.service2DB.model.tables.AccumulatedResults;
import com.app.service2DB.model.tables.SubTaskResult;
import com.app.service2DB.model.tables.Task;
import com.app.service2DB.model.tables.Worker;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * A component class that provides methods to receive and send messages from other microservices.
 */
@Component
@EnableScheduling
@EnableBinding({MessagingChannel.class, Sink.class})
public class DBController {

    @Autowired
    private MessagingChannel channel;
    @Autowired
    private DBHelper dbHelper;
    @Autowired
    private TaskHandler taskHandler;


    /**
     * Inserts the subscribed workers' details that sent by the MQ microservice.
     * @param worker subscribed worker details of type Worker model object.
     */
    @StreamListener(target = "worker_details_channel")
    public void onMessageReceived(Worker worker) {
        dbHelper.insertWorkerSubscription(worker);
    }

    /**
     * Inserts the task details that sent by the MQ microservice.
     * @param message subscribed worker details of type Worker model object.
     */
    @StreamListener(target = "initiator_task_channel")
    public void onMessageReceived(String message) throws JSONException, IOException {
       Task _task = taskHandler.parseJsonMessage(message);
        dbHelper.insertTaskIntoProcessingPool(_task);
        publishTaskAndWorkerDetails(taskHandler.constructPayLoad(dbHelper.insertTaskDetails(_task), dbHelper.getAvailableWorkerDetails(), false), false);
    }

    /**
     * Continue receiving the worker responses until the task due date expires.
     * upon time expiration pick the executable file/task from the processing pool table and dispatch it to
     * TE microservice for task division
     */
    public void checkDueDateAndDispatchSubTask() throws JSONException {

          boolean timeExpired = dbHelper.checkDueDateExpiry();
          if(timeExpired) {
              Task _task = dbHelper.getPayloadAndRemoveFromProcessPool();
              publishTaskAndWorkerDetails(taskHandler.constructPayLoad(_task, dbHelper.getTaskAcceptedWorkerList(_task), true), true);
          }
    }

    /**
     * Send the task description or executable file (or) task + worker's details (who agreed to execute the task) to TE microservice.
     * @param message either task description or executable file (or) task.
     * @param isItForSubTask check if it is for the task description or for the subtask.
     */
    private void publishTaskAndWorkerDetails(String message, boolean isItForSubTask) {

            Message<String> msg = MessageBuilder.withPayload(message).build();
            if(!isItForSubTask) {
                channel.sendAvailableWorkerDetails().send(msg);
                System.out.println("worker and task details has been send to TE microservice"+ msg.toString());
            }
            else {
                channel.sendFileAndWorkerDetails().send(msg);
                System.out.println("worker and sub task details has been send to TE microservice");
            }
    }

    /**
     * Inserts worker response to the task acceptance that received from the MQ microservice.
     * @param workerResponse worker response to run the task on their device type of JSON string.
     * @throws JSONException
     */
    @StreamListener(target = "worker_response")
    public void onWorkerResponse(String workerResponse) throws JSONException {

        dbHelper.insertWorkerResponse(taskHandler.getWorkerResponseObj(workerResponse));
    }

    /**
     * Receives executable file(.jar or .apk) along with workers details from TE microservice
     * and inserts received details into the "subtask" table.
     * @param message the details of the assigned executable subtask to the workers.
     * @throws JSONException throws any json related exceptions.
     */
    @StreamListener(target = "assigned_sub_task")
    public void onSubTaskMessage(String message) throws JSONException {
        dbHelper.insertSubTaskDetails(taskHandler.parseJsonForSubTaskDetails(message));
    }

    /**
     * Receives the computed task result sent by the workers through MQ microservice and
     * update the result of subtask in the "subtask" table.
     * @param subTaskResult sub task result is of type SubTaskResult model object.
     */
    @StreamListener(target = "sub_task_result")
    public void onSubTaskResultMessage(SubTaskResult subTaskResult) {
        dbHelper.insertSubTaskResults(subTaskResult);
    }

    /**
     * Check the "subTaskProcessPool" table if any task received all the sub task results by the workers.
     * @throws JSONException throws any JSON related exceptions.
     */
    public void checkIfAnyTaskReceivedAllResults() throws JSONException {
        UUID processedTaskId = dbHelper.checkForProcessedTask();
        if(processedTaskId != null) {
            Task _task = dbHelper.getTaskDetails(processedTaskId);
            sendResultsToResultEngine(taskHandler.getJsonString(dbHelper.getSubTaskList(processedTaskId), _task));
        }
    }

    /**
     * Upon receiving all computed subtask results for a task, send all subtask results to RE microservice
     * to accumulate the results into one.
     * @param subTaskResults all subtask results of type JSON string.
     */
    public void sendResultsToResultEngine(String subTaskResults) {

        Message<String> msg = MessageBuilder.withPayload(subTaskResults).build();
        channel.sendSubtaskResults().send(msg);
    }

    /**
     * Receives the accumulated subtask details from TE microservice and
     * inserts the result to into the "AccumulatedResults" table.
     * @param accumulatedResults the accumulated result of type JSON string.
     */
    @StreamListener("accumulated_results")
    public void onMessageArrival(AccumulatedResults accumulatedResults) {
        dbHelper.saveAccumulatedResults(accumulatedResults);
    }
}
