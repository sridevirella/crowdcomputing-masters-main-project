package com.app.service2DB.controller.task;

import com.app.service2DB.controller.database.DBController;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * At a given fixed rate, it schedules polling on the table.
 */
@Component
public class TaskDispatchTracker {

    @Autowired
    DBController dbController;

    /**
     * At every 5 seconds, it pickup the first task from the "taskProcessingPool" table and check for its due date.
     * If the due date of the task expires, it sends the corresponding task to the TE microservice for the task division.
     * @throws JSONException throws any JSON related exceptions.
     */
    @Scheduled(fixedRate = 5000)
    public void subTaskDispatcher() throws JSONException {
        dbController.checkDueDateAndDispatchSubTask();
    }

    /**
     * At every 5 seconds, it check the "subTaskProcessingPool" table to see if any task received all the computed subtask results.
     * If any task received all the subtask results, it sends the corresponding task to the RA microservice for the subtasks result accumulation.
     * @throws JSONException throws any JSON related exceptions.
     */
    @Scheduled(fixedRate = 5000)
    public void subTaskResultDispatcher() throws JSONException {
        dbController.checkIfAnyTaskReceivedAllResults();
    }
}