package com.app.service4re.model;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * An interface that provides accessing INPUT, OUTPUT bound channels to send or receive data from RabbitMQ
 */
public interface MessagingChannel {

    @Input("sub_task_Result_RA")
    SubscribableChannel receiveSubTaskDetails();

    @Output("accumulated_results")
    MessageChannel sendAccumulatedResults();
}
