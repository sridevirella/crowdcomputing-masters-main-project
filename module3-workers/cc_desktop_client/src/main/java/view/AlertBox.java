package view;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Create an alert box with the given title, header and attach it to the primary stage.
 */
public class AlertBox {

    private Alert alertBox;
    private String title;
    private String header;
    private Stage stage;

    AlertBox(String title, String header, Stage stage, boolean isItForSubTask) {

        this.title = title;
        this.header = header;
        this.stage = stage;
        createAlertBox(isItForSubTask);
    }

    private void createAlertBox(boolean isItForSubTask) {

        ButtonType acceptBtn = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
        ButtonType rejectBtn = new ButtonType("Reject", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType runBtn = new ButtonType("Run", ButtonBar.ButtonData.OK_DONE);

        if(!isItForSubTask)
            alertBox = new Alert(Alert.AlertType.NONE, header ,rejectBtn, acceptBtn );
        else
            alertBox = new Alert(Alert.AlertType.NONE, header , runBtn );
        alertBox.setTitle(title);
        alertBox.initOwner(stage);
    }

    public void setAlertBoxContent(String message) {
        alertBox.setContentText(message);
    }

    public Optional<ButtonType> showAndWait() {
        return alertBox.showAndWait();
    }

    public Node getAlertBox() {
        return alertBox.getGraphic();
    }
}
