package com.app.service2DB.repository;

import com.app.service2DB.model.tables.AccumulatedResults;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data repository abstraction to implement data access from the Cassandra database.
 * Repository provide CURD operations on "AccumulatedResults" table.
 */
@Repository
public interface AccumulatedResultRepo extends CassandraRepository<AccumulatedResults, UUID> {

    @Query("SELECT * FROM AccumulatedResults where taskId=?0")
    AccumulatedResults findByTaskId(UUID taskId);
}
