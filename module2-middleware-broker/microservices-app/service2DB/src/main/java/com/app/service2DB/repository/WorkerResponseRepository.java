package com.app.service2DB.repository;

import com.app.service2DB.model.tables.WorkerResponse;
import com.app.service2DB.model.tables.TaskWorkerCompositeKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository abstraction to implement data access from the Cassandra database.
 * Repository provide CURD operations on "WorkerResponse" table.
 */
@Repository
public interface WorkerResponseRepository extends CassandraRepository<WorkerResponse, TaskWorkerCompositeKey> {

    @Query("SELECT * from workerResponse where taskId=?0 and accepted=?1 allow filtering")
    List<WorkerResponse> findByTaskAcceptance(UUID taskId, String accepted);

}
