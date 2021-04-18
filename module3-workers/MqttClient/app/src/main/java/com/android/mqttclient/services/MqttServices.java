package com.android.mqttclient.services;

import android.content.Context;
import android.util.Log;

import com.android.mqttclient.model.Constants;
import com.android.mqttclient.model.Message;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link MqttServices} is a worker class which establishes an MQTT server connection using an instance of {@link MqttAndroidClient}.
 * Uses {@link android.app.Activity} {@link Context} to initialize {@link MqttAndroidClient}
 * Provides {@link MqttServicesListener} interface to handle the message call backs.
 * Used in TaskHandler to handle multiple topics/messages over MQTT server.
 */
public class MqttServices {

    private MqttServicesListener listener;
    private MqttSubscribeListener subscribeListener;
    private String taskId;
    private String taskResponseTopic;
    private String subTaskTopic;
    private String subTaskResponseTopic;
    private JSONObject SubTaskResultJsonString;

    /**
     * Interface defined to handle {@link Message} whenever a message received from MQTT.
     */
    public interface MqttServicesListener {
        void onMessageReceived(Message message) throws JSONException;
        void onConnectComplete(boolean reconnect);
        void onConnectionLost(String errorMessage);
    }

    /**
     * Setter for {@link MqttServicesListener} instance.
     * @param listener : {@link MqttServicesListener}
     */
    public void setListener(MqttServicesListener listener){
        this.listener = listener;
    }

    /**
     * Interface defined to handle {@link Message} whenever a message received from MQTT.
     */
    public interface MqttSubscribeListener {
        void onSuccess(String topic);
        void onFailure(String topic, String reason);
    }

    /**
     * Setter for {@link MqttServicesListener} instance.
     * @param listener : {@link MqttServicesListener}
     */
    public void setSubscribeListener(MqttSubscribeListener listener){
        this.subscribeListener = listener;
    }

    /**
     * {@link android.app.Activity} context required to initialize {@link MqttAndroidClient}
     */
    private Context context;

    /**
     * Initializes with {@link android.app.Activity} context when MqttServices instance is created.
     * @param context : {@link Context}
     */
    public MqttServices(Context context){
        this.context = context;
    }

    private MqttAndroidClient mqttAndroidClient;

    /**
     * Initializes MqttAndroidClient using context, URI and clientId.
     * Sets connection callbacks to handle connectComplete(), connectionLost(),messageArrived() and deliveryComplete().
     */
    public void initMqttClient() {

        mqttAndroidClient = new MqttAndroidClient(context, Constants.URI.getConstant(), Constants.WORKER_ID.getConstant());

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            /**
             * Invoked once the connection is established successfully. If there is any connection loss, returns reconnect flag for subscription retry.
             * @param reconnect : Will be true if there is any connection loss. Will be false when there is no connection loss.
             * @param URI : URI on which the connection is established.
             */
            @Override
            public void connectComplete(boolean reconnect, String URI) {
                if(listener != null){
                    listener.onConnectComplete(reconnect);
                    System.out.println("connectComplete on:"+ URI);
                }
                if (reconnect) {
                    subscribeToTopic( Constants.MAIN_TOPIC.getConstant() );   // If Clean Session is true , we need to re-subscribe upon reconnection
                } else {
                    Log.d("connectComplete()", "Connected to: " + URI);
                }
            }

            /**
             * Invoked when there is a connection loss with the mqtt broker
             */
            @Override
            public void connectionLost(Throwable cause) {
                if(listener != null){
                    listener.onConnectionLost(cause.getMessage());
                    System.out.println("connectionLost on:"+ Constants.URI.getConstant());
                }
                Log.d("connectionLost()","The Connection was lost");
            }

            /**
             * Invoked when a message received for the subscribed topics.
             */
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                if( topic.equals(Constants.WORKER_TASK.getConstant() + "/" + Constants.WORKER_ID.getConstant()) )
                {
                    if(listener != null){
                        listener.onMessageReceived(new Message(new String(message.getPayload()), null ));
                        System.out.println("Received Task Description:\n"+ new String(message.getPayload()));
                    }
                }

                else if( topic.equals(Constants.WORKER_SUBTASK.getConstant() + "/" + Constants.WORKER_ID.getConstant()) ) {
                    if(listener != null){
                        listener.onMessageReceived(new Message(null, message.getPayload() ));
                        System.out.println("Received Sub Task:\n"+ message.getPayload());
                    }
                }
            }

            /**
             * Invoked once message successfully deliver to mqtt
             * Upon successful delivery of task response, subscribe to sub task topic
             */
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d("deliveryComplete()","Message Delivered");
            }
        });
    }

    /**
     * Setup MQtt android client connection using different connect options and callbacks.
     * In case of connection lost, mqtt broker attempts for auto reconnection.
     * the mqtt broker creates a persistent session to deliver un delivered messages.
     */
    public void connectMqttClient() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {

                /**
                 * Invoked once connection successfully established with mqtt.
                 * On successful connection subscribe to default topic to receive task description.
                 */
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    if(listener != null){
                        listener.onConnectComplete(false);
                        System.out.println("connectComplete on:"+ Constants.URI.getConstant());
                    }
                }

                /**
                 * Invoked on connection failure.
                 */
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    if(listener != null){
                        listener.onConnectionLost(exception.getMessage());
                        System.out.println("connectionLost on:"+ Constants.URI.getConstant());
                    }
                    System.out.println("Failed to connect to: " + Constants.URI.getConstant());
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    /**
     * subscribes to the topic with given QOS and message retained values.
     * @param topic : Topic name as {@link String}
     */
    public void subscribeToTopic(final String topic){
        try {
            if (mqttAndroidClient.isConnected()){
                mqttAndroidClient.subscribe(topic, Integer.parseInt(Constants.QOS.getConstant()), null, new IMqttActionListener() {

                    /**
                     *  Invoked on successfully subscribing to the topic
                     */
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        if(subscribeListener != null) {
                            subscribeListener.onSuccess(topic);
                        }
                        System.out.println("Subscribed to Topic "+topic);
                    }

                    /**
                     *  Invoked on failure of topic subscription
                     */
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        if(subscribeListener != null) {
                            subscribeListener.onFailure(topic, exception.getMessage());
                        }
                        Log.d("subscribeToTopic:","Failed to subscribe");
                    }
                });
            }

            mqttAndroidClient.subscribe( Constants.WORKER_TASK.getConstant(), 0, new IMqttMessageListener() {
                /**
                 * Invoked when a message received from mqtt for subscribed topics
                 */
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    if( topic.equals(Constants.WORKER_TASK.getConstant() + "/" + Constants.WORKER_ID.getConstant()) )
                    {
                        if(listener != null){
                            listener.onMessageReceived(new Message(new String(message.getPayload()), null ));  // deserialize byte[] to string
                            System.out.println("Received Task Description:\n"+ new String(message.getPayload()));
                        }
                    }

                    else if( topic.equals(Constants.WORKER_SUBTASK.getConstant() + "/" + Constants.WORKER_ID.getConstant()) ) {
                        if(listener != null){
                            listener.onMessageReceived(new Message(null, message.getPayload() ));  // file content payload
                            System.out.println("Received Sub Task:\n"+ message.getPayload());
                        }
                    }

                }
            });

        } catch (MqttException ex){
            System.err.println("Exception while subscribing");
            ex.printStackTrace();
        }
    }

    /**
     * subscribes to the topic with given QOS and message retained values.
     * @param topic : Topic name as {@link String}
     */
    public void unSubscribeToTopic(final String topic) {
        try {
            if (mqttAndroidClient.isConnected()) {
                mqttAndroidClient.unsubscribe(topic, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        if(subscribeListener != null) {
                            subscribeListener.onSuccess(topic);
                        }
                        System.out.println("UnSubscribed to Topic "+topic);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        if(subscribeListener != null) {
                            subscribeListener.onFailure(topic, exception.getMessage());
                        }
                        Log.d("unSubscribeToTopic:","Failed to unsubscribe");
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("subscribeToTopic Exception =" + e.getMessage());
        }
    }

    /**
     *Publish a message on the topic with given QOS and retained values.
     * @param topic : Topic name as {@link String}
     * @param message : Message related to the topic as {@link String}
     */
    public void publishMessage(String topic,String message){
        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setQos(Integer.parseInt(Constants.QOS.getConstant()));
            mqttMessage.setRetained(Boolean.parseBoolean(Constants.RETAINED.getConstant()));
            mqttMessage.setPayload(message.getBytes());

            if(mqttAndroidClient.isConnected()) {
                mqttAndroidClient.publish(topic, mqttMessage);
                System.out.println("Message Published to topic: "+topic);
                System.out.println("Worker Reply: "+new String(message.getBytes()));
            }
            else{
                System.out.println(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            System.err.println("Error while Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets Offline message buffer options
     */
    public void bufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(false);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
    }
}

