package com.app.service2DB.controller.database;

import com.app.service2DB.model.tables.*;
import com.app.service2DB.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * An helper class that provides methods to access spring data repositories.
 */
@Service
public class DBHelper {

    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private WorkerResponseRepository workerResponseRepository;
    @Autowired
    private TaskProcessingPoolRepo taskProcessingPoolRepo;
    @Autowired
    private SubTaskRepository subTaskRepository;
    @Autowired
    private SubTaskProcessingPoolRepo subTaskProcessingPoolRepo;
    @Autowired
    private AccumulatedResultRepo accumulatedResultRepo;


    /**
     * Inserts the subscribed worker details into the "worker" table
     * @param worker is of type Worker model object.
     */
    public void insertWorkerSubscription(Worker worker) {

        Worker _worker = workerRepository.save(worker);
        System.out.println("Successfully inserted worker details:\n"+_worker);
    }

    /**
     * Inserts the task details into the "task" table
     * @param task is of type Task model object.
     */
    public Task insertTaskDetails(Task task) {

        Task _task = taskRepository.save(task);
        System.out.println("Successfully inserted task details\n"+_task.toString());
        return _task;
    }

    /**
     * Check the task due date expiry with server local time.
     * @return true if time expires else return false.
     */
    public Boolean checkDueDateExpiry() {

        TaskProcessPool _task = taskProcessingPoolRepo.getFirstTask();
        if(_task != null) {
            System.out.println("checking for due date....");
            return _task.getDueDate().compareTo(LocalDateTime.now()) <= 0;
        }
        return false;
    }

    /**
     * Fetch all subscribed worker details from the "worker" table.
     * @return the list of subscribed workers.
     */
    public List<Worker> getAvailableWorkerDetails() {

        return workerRepository.findByWorkerAvailability(true);
    }

    /**
     * Inserts the details of the workers who accepted the task into the "workerResponse" table.
     * @param workerResponse is of type WorkerResponse model object.
     */
    public void insertWorkerResponse(WorkerResponse workerResponse) {

        WorkerResponse _workerResponse = workerResponseRepository.save(workerResponse);
        System.out.println("Successfully inserted worker responses:\n" +_workerResponse.toString());
    }

    /**
     * Insert taskId of the task that was received from the MQ microservice in the "taskProcessingPool" table.
     * @param task is of type Task model object.
     */
    public void insertTaskIntoProcessingPool(Task task) {

        taskProcessingPoolRepo.save( new TaskProcessPool(task.getTaskId(), getLocalDateFormat(task.getDueDate()), 0));
        System.out.println(task.getShortName() + " task successfully added to processing pool");
    }

    /**
     * Upon task due date expiration, pick and remove the task entry from the process pool to send corresponding task
     * details to the TE microservice for the task division.
     * @return task details of type Task model object.
     */
    public Task getPayloadAndRemoveFromProcessPool() {

        TaskProcessPool _task = taskProcessingPoolRepo.getFirstTask();

        try {
            taskProcessingPoolRepo.deleteProcessedTask(_task.getTaskId(), _task.getDueDate(), _task.getSno());
            subTaskProcessingPoolRepo.save(new SubTaskProcessPool(_task.getTaskId(), LocalDateTime.now()));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return taskRepository.findByTaskId(_task.getTaskId());
    }

    /**
     * Get the details of the workers who accepted the task from the "workerResponse" table.
     * @param task is of type Task model object.
     * @return list of task accepted workers.
     */
    public List<Worker> getTaskAcceptedWorkerList(Task task) {

        List<WorkerResponse> respondedWorkerList = workerResponseRepository.findByTaskAcceptance(task.getTaskId(), "Yes");
        return respondedWorkerList.stream().map(workerResponse -> workerRepository.findByWorkerId(workerResponse.getResponseKey().getWorkerId())).collect(Collectors.toList());
    }

    /**
     * Insert the subtask details into the "subTask" table.
     * @param subTaskList list of subtasks.
     */
    public void insertSubTaskDetails(List<SubTask> subTaskList) {

        subTaskRepository.saveAll(subTaskList);
        System.out.println("sub task details successfully inserted");
    }

    /**
     * Insert the subtask results into the table "subTask".
     * @param subTaskResult
     */
    public void insertSubTaskResults(SubTaskResult subTaskResult) {

        SubTask _subtaskResult = subTaskRepository.updateByTaskIdAndWorkerId(subTaskResult.getSubTaskResult(), subTaskResult.getTaskId(), subTaskResult.getWorkerId(), true);
        System.out.println("Successfully inserted the subtask result");
    }

    /**
     * Check if any task received all the computed subtask results that send by the workers.
     * @return corresponding task ID.
     */
    public UUID checkForProcessedTask() {

        List<SubTaskProcessPool> taskList = subTaskProcessingPoolRepo.findAllTasks();

        final UUID[] processedTask = {null};

        if(taskList.size() > 0) {

            taskList.forEach(task -> {
                if( subTaskRepository.getNumberOfSubTasksByTaskId(task.getTaskId()) !=0) {
                    if (subTaskRepository.getNumberOfSubTasksByTaskId(task.getTaskId()) == subTaskRepository.getNumberOfProcessedSubTasks(task.getTaskId(), true))
                        processedTask[0] = task.getTaskId();
                }
            });
        }
        return processedTask[0];
    }

    /**
     * Get subtasks list of corresponding task ID.
     * @param taskId is of type UUID.
     * @return list of subtasks.
     */
    public List<SubTask> getSubTaskList(UUID taskId) {

        subTaskProcessingPoolRepo.deleteProcessedSubTasks(taskId);
        return subTaskRepository.findByTaskId(taskId);
    }

    /**
     * Insert the accumulated results that send by the RE microservice into the "accumulatedResult" table.
     * @param accumulatedResults is of type AccumulatedResults model object.
     */
    public void saveAccumulatedResults(AccumulatedResults accumulatedResults) {

        accumulatedResultRepo.save(accumulatedResults);
        System.out.println("Successfully saved accumulated results");
    }

    /**
     * Get the task details for the corresponding task ID.
     * @param taskId is of type UUID.
     * @return task is of type Task model object.
     */
    public Task getTaskDetails(UUID taskId) {
        return taskRepository.findByTaskId(taskId);
    }

    /**
     * Format date to UTC LocalDateTime.
     * @param dateString
     * @return formatted date is of type LocalDateTime.
     */
    private LocalDateTime getLocalDateFormat(String dateString) {

        //2021-03-20T01:40:00Z
        String[] tokens = dateString.split("T");
        String[] dateTokens = tokens[0].split("-");
        String[] timeTokens = tokens[1].split(":");
        return LocalDateTime.of(Integer.parseInt(dateTokens[0]), Integer.parseInt(dateTokens[1]), Integer.parseInt(dateTokens[2]),
                Integer.parseInt(timeTokens[0]), Integer.parseInt(timeTokens[1]), 0);

    }
}