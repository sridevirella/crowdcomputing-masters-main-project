package com.app.service1MQ.model;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * An interface that provides accessing INPUT, OUTPUT bound channels to send or receive data from RabbitMQ
 */
public interface MessagingChannel {

    @Output("worker_details_channel")
    MessageChannel workerDetails();

    @Output("initiator_task_channel")
    MessageChannel taskDetails();

    @Output("worker_response")
    MessageChannel workerResponse();

    @Output("sub_task_result")
    MessageChannel subTaskResult();

    @Input("assigned_task")
    SubscribableChannel receiveAssignedTask();

    @Input("assigned_sub_task")
    SubscribableChannel subTaskDetails();

    @Input("accumulated_results")
    SubscribableChannel accumulatedResults();
}
