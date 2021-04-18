package com.android.mqttclient.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;

import com.android.mqttclient.LaunchViewModel;
import com.android.mqttclient.R;

/**
 * A class that handles the home page display.
 */
public class HomeFragment extends Fragment{

    private LaunchViewModel launchViewModel;

    private TextView connectionTV;
    private Button retryButton;

    /**
     * method is invoked to create fragment view object via XML layout inflation.
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        launchViewModel =
                ViewModelProviders.of(requireActivity()).get(LaunchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(root);
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * initViews() method defines all user interface views
     * findViewById is a method that finds the view from the layout resource file of current Activity.
     */
    public void initViews(View view) {
        connectionTV = view.findViewById(R.id.text_home);
        connectionTV.setText(R.string.connect_to_mqtt);
        launchViewModel.getOnConnectionLost().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                connectionTV.setText(R.string.failed_connect);
                retryButton.setVisibility(View.VISIBLE);
            }
        });
        launchViewModel.getOnConnectComplete().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean s) {
                connectionTV.setText(R.string.connection_success);
                retryButton.setVisibility(View.GONE);
            }
        });
        retryButton = view.findViewById(R.id.connect_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionTV.setText(R.string.connect_to_mqtt);
                retryButton.setVisibility(View.GONE);
                launchViewModel.getMqttServicesInstance().connectMqttClient();
            }
        });
    }
}
