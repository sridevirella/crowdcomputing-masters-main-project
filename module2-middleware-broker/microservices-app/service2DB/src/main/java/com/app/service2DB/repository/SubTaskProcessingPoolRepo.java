package com.app.service2DB.repository;

import com.app.service2DB.model.tables.SubTaskProcessPool;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository abstraction to implement data access from the Cassandra database.
 * Repository provide CURD operations on "subTaskProcessPool" table.
 */
public interface SubTaskProcessingPoolRepo extends CassandraRepository<SubTaskProcessPool, UUID> {

    @Query("SELECT * from subTaskProcessPool")
    List<SubTaskProcessPool> findAllTasks();

    @Query("DELETE from subTaskProcessPool where taskId=?0")
    SubTaskProcessPool deleteProcessedSubTasks(UUID taskId);
}
