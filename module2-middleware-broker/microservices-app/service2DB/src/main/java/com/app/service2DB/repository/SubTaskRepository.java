package com.app.service2DB.repository;

import com.app.service2DB.model.tables.SubTask;
import com.app.service2DB.model.tables.TaskWorkerCompositeKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository abstraction to implement data access from the Cassandra database.
 * Repository provide CURD operations on "subtask" table.
 */
public interface SubTaskRepository extends CassandraRepository<SubTask, TaskWorkerCompositeKey> {

    @Query("UPDATE subTask SET subTaskResult =?0, processed=?3 WHERE taskId=?1 AND workerId=?2")
    SubTask updateByTaskIdAndWorkerId(String result, UUID taskId, String workerId, boolean processed);

    @Query("SELECT count(*) FROM subtask WHERE taskId=?0")
    int getNumberOfSubTasksByTaskId(UUID taskId);

    @Query("SELECT count(*) FROM subtask WHERE taskId=?0 AND processed=?1 allow filtering")
    int getNumberOfProcessedSubTasks(UUID taskId, boolean processed);

    @Query("SELECT * FROM subtask WHERE taskId=?0")
    List<SubTask> findByTaskId(UUID taskId);
}
