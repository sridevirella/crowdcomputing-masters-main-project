package com.mypackage.apkfiletest;
import java.util.*;
public class TaskImplementation {
public TaskImplementation(Results results) { initiateTask(results);
    }
public void initiateTask(Results results) {
Map<String, String> jsonObject = new HashMap<>();
Primes taskObject = new Primes(1,1000);
jsonObject.put("result", taskObject.getResult());
jsonObject.put("taskId", "8cadfab0-9bff-11eb-a9c1-5350ba174301");
results.setResult(jsonObject.toString());
}
}