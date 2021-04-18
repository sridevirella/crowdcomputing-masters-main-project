import javafx.application.Application;
import javafx.stage.Stage;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import view.GuiController;
import view.GuiElements;

//Application start point and which initials a UI controller class.
public class MqttFxApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws MqttException {
        new GuiController(primaryStage);
    }
}
