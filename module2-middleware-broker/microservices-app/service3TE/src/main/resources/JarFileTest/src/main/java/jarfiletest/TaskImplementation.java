package jarfiletest;
import java.util.*;
public class TaskImplementation {
public TaskImplementation(Results results) { initiateTask(results);
    }
public void initiateTask(Results results) {
Map<String, String> jsonObject = new HashMap<>();
Primes taskObject = new Primes(1,2000);
jsonObject.put("result", taskObject.getResult());
jsonObject.put("taskId", "de55ca60-9f4b-11eb-973a-2dd84ba8ceb4");
results.setResult(jsonObject.toString());
}
}