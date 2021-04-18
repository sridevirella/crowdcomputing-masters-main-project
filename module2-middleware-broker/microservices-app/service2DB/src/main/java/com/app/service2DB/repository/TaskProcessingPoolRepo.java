package com.app.service2DB.repository;

import com.app.service2DB.model.tables.TaskProcessPool;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Spring Data repository abstraction to implement data access from the Cassandra database.
 * Repository provide CURD operations on "TaskProcessPool" table.
 */
@Repository
public interface TaskProcessingPoolRepo extends CassandraRepository<TaskProcessPool, UUID> {

    @AllowFiltering
    TaskProcessingPoolRepo findByTaskId(UUID taskId);

    @Query("SELECT * from TaskProcessPool limit 1")
    TaskProcessPool getFirstTask();

    @Query("DELETE from TaskProcessPool where taskId=?0 and dueDate=?1 and sno=?2")
    TaskProcessPool deleteProcessedTask(UUID taskId, LocalDateTime dueDate, int sno);
}