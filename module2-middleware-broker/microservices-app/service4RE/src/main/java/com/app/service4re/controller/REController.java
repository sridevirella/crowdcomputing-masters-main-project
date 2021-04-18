package com.app.service4re.controller;

import com.app.service4re.model.MessagingChannel;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * A component class that provides methods to receive and send messages from other microservices.
 */
@Component
@EnableBinding({MessagingChannel.class, Sink.class})
public class REController {

    @Autowired
    private MessagingChannel channel;
    @Autowired
    ResultAccumulator resultAccumulator;

    /**
     * accumulates the received subtask results sent by the DB microservice.
     */
    @StreamListener(target = "sub_task_Result_RA")
    public void onMessageReceived(String subTaskResults) throws JSONException {

        publishAccumulatedResults(resultAccumulator.accumulateSubTaskResults(subTaskResults));
    }

    /**
     * sends the accumulated result to DB and MQ microservices.
     */
    public void publishAccumulatedResults(String accumulatedResults) {

        Message<String> msg = MessageBuilder.withPayload(accumulatedResults).build();
        channel.sendAccumulatedResults().send(msg);
        System.out.println("Accumulated results has been sent to MQ microservice");
    }
}
