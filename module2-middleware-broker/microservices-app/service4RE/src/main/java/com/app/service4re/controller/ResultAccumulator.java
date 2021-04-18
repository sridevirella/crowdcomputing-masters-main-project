package com.app.service4re.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

/**
 * Class provides methods to accumulate all received subtask result into one.
 */
@Component
public class ResultAccumulator {

    public ResultAccumulator() {}

    public String accumulateSubTaskResults(String message) throws JSONException {

        JSONObject jsonObject = new JSONObject(message);
        JSONArray jsonArray = jsonObject.getJSONArray("results");
        JSONObject finalPayLoad = new JSONObject();

        StringBuilder stringBuilder = new StringBuilder();

        IntStream.range(0, jsonArray.length()).forEach( i -> {
            try {
                stringBuilder.append(jsonArray.get(i));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        finalPayLoad.put("taskId", jsonObject.getString("taskId"));
        finalPayLoad.put("taskName", jsonObject.getString("taskName"));
        finalPayLoad.put("result", stringBuilder);
        return finalPayLoad.toString();
    }
}
