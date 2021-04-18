package com.app.service3te.model;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * An interface that provides accessing INPUT, OUTPUT bound channels to send or receive data from RabbitMQ
 */
public interface MessagingChannel {

    @Input("available_workers")
    SubscribableChannel receiveAvailableWorkers();

    @Input("subtask_accepted_workers")
    SubscribableChannel receiveFileAndWorkerDetails();

    @Output("assigned_task")
    MessageChannel publishAssignedTask();

    @Output("assigned_sub_task")
    MessageChannel publishAssignedSubTask();
}
