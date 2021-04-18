package com.app.service2DB.repository;

import com.app.service2DB.model.tables.Task;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


/**
 * Spring Data repository abstraction to implement data access from the Cassandra database.
 * Repository provide CURD operations on "Task" table.
 */
@Repository
public interface TaskRepository extends CassandraRepository<Task, UUID> {

    @AllowFiltering
    Task findByTaskId(UUID taskId);

}
