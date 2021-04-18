package com.app.service2DB.repository;

import com.app.service2DB.model.tables.Worker;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository abstraction to implement data access from the Cassandra database.
 * Repository provide CURD operations on "Worker" table.
 */
@Repository
public interface WorkerRepository extends CassandraRepository<Worker, String> {

    @AllowFiltering
    Worker findByWorkerId(String workerId);

    @Query("SELECT * FROM worker")
    List<Worker> findAllWorkers();

    @Query("SELECT * from worker where isAvailable=?0 allow filtering")
    List<Worker> findByWorkerAvailability(boolean value);
}
