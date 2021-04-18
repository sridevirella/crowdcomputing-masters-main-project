package com.app.service2DB.model;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * An interface that provides accessing INPUT, OUTPUT bound channels to send or receive data from RabbitMQ
 */
public interface MessagingChannel {

    @Output("available_workers")
    MessageChannel sendAvailableWorkerDetails();

    @Output("subtask_accepted_workers")
    MessageChannel sendFileAndWorkerDetails();

    @Output("sub_task_Result_RA")
    MessageChannel sendSubtaskResults();

    @Input("initiator_task_channel")
    MessageChannel taskDetails();

    @Input("worker_details_channel")
    SubscribableChannel receiveWorkerDetails();

    @Input("worker_response")
    SubscribableChannel receiveWorkerResponse();

    @Input("assigned_sub_task")
    SubscribableChannel receiveSubTaskDetails();

    @Input("sub_task_result")
    SubscribableChannel subTaskResults();

    @Input("accumulated_results")
    SubscribableChannel accumulatedResults();
}