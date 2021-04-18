package com.android.mqttclient.ui.event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;

import com.android.mqttclient.LaunchViewModel;
import com.android.mqttclient.R;
import com.android.mqttclient.model.Constants;
import com.android.mqttclient.services.MqttServices;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class that handles the Subscribe/Unsubscribe page display.
 */
public class SubscribeFragment extends Fragment implements MqttServices.MqttSubscribeListener {

    private LaunchViewModel launchViewModel;
    private TextView subsciptionstatusTV;
    private Button acceptBtn, unsubscribeBtn;
    private LinearLayout infoLayout;
    private boolean isSubscribingToTopic = false;

    // method is invoked to create fragment view object via XML layout inflation.
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        launchViewModel =
                ViewModelProviders.of(requireActivity()).get(LaunchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_subscribe, container, false);
        initViews(root);
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Handle subscribe and unsubscribe buttons to receive tasks from the middleware broker.
     * initViews() method defines all user interface views
     * findViewById is a method that finds the view from the layout resource file of current Activity.
     */
    public void initViews(View view) {
        subsciptionstatusTV = view.findViewById(R.id.text_subscribe_status);
        infoLayout = view.findViewById(R.id.info_layout);
        acceptBtn = view.findViewById(R.id.subscribe_accept);
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSubscribingToTopic = true;
                acceptBtn.setEnabled(false);
                subsciptionstatusTV.setText(R.string.subscribing_please_wait);
                launchViewModel.getMqttServicesInstance().bufferOptions();
                launchViewModel.getMqttServicesInstance().subscribeToTopic(Constants.MAIN_TOPIC.getConstant());
                try {
                    launchViewModel.getMqttServicesInstance().publishMessage(Constants.WORKER_SUBSCRIPTION.getConstant(), getWorkerDetailsPayload(true));
                } catch (JSONException e) {
                    e.printStackTrace();
                }            }
        });
        unsubscribeBtn = view.findViewById(R.id.unsubscribe_button);
        unsubscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSubscribingToTopic = false;
                unsubscribeBtn.setEnabled(false);
                subsciptionstatusTV.setText(R.string.unsubscribing_please_wait);
                launchViewModel.getMqttServicesInstance().unSubscribeToTopic(Constants.MAIN_TOPIC.getConstant());
                try {
                    launchViewModel.getMqttServicesInstance().publishMessage(Constants.WORKER_UN_SUBSCRIPTION.getConstant(), getWorkerDetailsPayload(false));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        launchViewModel.getMqttServicesInstance().setSubscribeListener(this);
        if (launchViewModel.getSubscriptionPref()) {
            onSubscribeSuccess();
        }
    }

    /**
     * Save the subscription status and handle UI element visibility.
     */
    @Override
    public void onSuccess(String topic) {
        acceptBtn.setEnabled(true);
        unsubscribeBtn.setEnabled(true);
        if(isSubscribingToTopic) {
            launchViewModel.saveSubscriptionState(true);
            onSubscribeSuccess();
        } else {
            launchViewModel.saveSubscriptionState(false);
            subsciptionstatusTV.setText(R.string.unsubscribing_done);
            unsubscribeBtn.setVisibility(View.GONE);
            infoLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * On successful subscribe, display success message and give user an option to unsubscribe in the same fragment.
     */
    private void onSubscribeSuccess() {
        subsciptionstatusTV.setText(R.string.subscribing_done);
        unsubscribeBtn.setVisibility(View.VISIBLE);
        infoLayout.setVisibility(View.GONE);
    }

    /**
     * On Failure to subscribe retry.
     */
    @Override
    public void onFailure(String topic, String reason) {
        acceptBtn.setEnabled(true);
        unsubscribeBtn.setEnabled(true);
        if(isSubscribingToTopic) {
            subsciptionstatusTV.setText(R.string.subscribing_failed);
            unsubscribeBtn.setVisibility(View.GONE);
            infoLayout.setVisibility(View.VISIBLE);
        } else {
            subsciptionstatusTV.setText(R.string.unsubscribing_failed);
            unsubscribeBtn.setVisibility(View.VISIBLE);
            infoLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Construct worker availability response JSON.
     */
    private String getWorkerDetailsPayload(boolean isAvailable) throws JSONException {

        JSONObject workerDetailsObj = new JSONObject();
        workerDetailsObj.put("workerId", Constants.WORKER_ID.getConstant());
        workerDetailsObj.put("deviceOS", "Android");
        workerDetailsObj.put("isAvailable", isAvailable);
        return workerDetailsObj.toString();
    }
}
