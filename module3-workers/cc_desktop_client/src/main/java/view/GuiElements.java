package view;

import controller.MqttServices;
import controller.ReadWriteFile;
import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import util.FilePaths;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Class which handles all the UI elements.
 */
public class GuiElements implements MqttServices.connectionStatusListener {

    private static BorderPane primaryPane;
    private static Button homeButton, subscribeButton, unSubscribeButton, receivedTaskButton, executedTaskButton;
    private static Button subscribeBtn, unsubscribeBtn;
    private static VBox primaryVBox;
    private final Stage primaryStage;
    private String connectionStatus;
    private final MqttServices mqttServicesInstance;


    public GuiElements(Stage stage, MqttServices mqttServicesInstance) {

        this.primaryStage = stage;
        this.mqttServicesInstance = mqttServicesInstance;
        mqttServicesInstance.setConnectionStatusListener(this);
    }

    /**
     * Initialize all UI elements such as scene, border pane, buttons, VBox, HBox.
     */
    public void initViews() {

        createPrimaryVBox();
        createPaneAndScenes();
        fireHomeButton();
    }

    /**
     * Create a scene with a border pane and add it to the scene.
     * add node VBox that contains menu buttons to the border pane.
     */
    private void createPaneAndScenes() {

        primaryPane = new BorderPane();
        primaryPane.setLeft(primaryVBox);
        Scene primaryScene = new Scene(primaryPane, 900, 650);
        primaryStage.setScene(primaryScene);
        primaryStage.setTitle("MCC Desktop Client");
        primaryStage.show();
    }

    /**
     * Initiate buttons, VBox, and action listeners on buttons.
     */
    private void createPrimaryVBox() {

        initButtons();
        setButtonProperties();
        initPrimaryVBox();
        buttonActionListener();
    }

    /**
     * Initiate all menu buttons and add CSS for each button.
     */
    private void initButtons() {

        homeButton = new Button("Home");
        subscribeButton = new Button("Subscribe");
        unSubscribeButton = new Button("Unsubscribe");
        receivedTaskButton = new Button("Received Tasks");
        executedTaskButton = new Button("Executed Tasks");
        Arrays.asList(homeButton, subscribeButton, unSubscribeButton, receivedTaskButton, executedTaskButton)
                .forEach(button -> button.setStyle("-fx-font-size:14"));
    }

    /**
     * Call action Listener for each button.
     */
    private void buttonActionListener() {

        homeButtonListener();
        subscribeButtonListener();
        unsubscribeButtonListener();
        receivedTaskButtonListener();
        executedTaskButtonListener();
    }

    /**
     * On click, set the MQTT connection status.
     */
    private void homeButtonListener() {

        homeButton.setOnMouseClicked(event -> {
            restoreButtonColors(homeButton);
            homeButton.setBackground(new Background(new BackgroundFill(Paint.valueOf("#B2E0F2"), null, null)));
            primaryPane.setStyle("-fx-font-size:14");
            if(connectionStatus != null)
                primaryPane.setCenter(new Text(connectionStatus));
        });
    }

    /**
     * On click, subscribe to the corresponding topic to receive the tasks.
     */
    private void subscribeButtonListener() {

        subscribeButton.setOnMouseClicked(event -> {
            subscribeVBoxProperties();

            subscribeBtn.setOnMouseClicked(event1 -> {
                try {
                    mqttServicesInstance.subscribeForTasks();

                } catch (JSONException | MqttException e) {
                    primaryPane.setCenter(new Text("Failed to subscribe"));
                }
                primaryPane.setCenter(new Text("Subscribed successfully"));
            });
        });
    }

    /**
     * Create SubscribeVBox with properties and add to border pane.
     */
    private void subscribeVBoxProperties() {

        restoreButtonColors(subscribeButton);
        subscribeButton.setBackground(new Background(new BackgroundFill(Paint.valueOf("#B2E0F2"), null, null)));
        subscribeBtn = new Button("Subscribe");
        VBox subscribeVBox = new VBox();
        subscribeVBox.getChildren().addAll(new Text("To receive tasks click below \"Subscribe\" button"), subscribeBtn);
        subscribeVBox.setSpacing(20);
        subscribeVBox.setPadding(new Insets(300, 0,0, 200));
        primaryPane.setCenter(subscribeVBox);
    }

    /**
     * On click, unsubscribe to the corresponding topic to stop receiving the tasks.
     */
    private void unsubscribeButtonListener() {

        unSubscribeButton.setOnMouseClicked(event -> {
            restoreButtonColors(unSubscribeButton);
            unSubscribeButton.setBackground(new Background(new BackgroundFill(Paint.valueOf("#B2E0F2"), null, null)));
            unsubscribeBtn = new Button("Unsubscribe");
            VBox unsubscribeVBox = new VBox();
            unsubscribeVBox.getChildren().addAll(new Text("To stop receiving tasks click below \"Unsubscribe\" button"), unsubscribeBtn);
            unsubscribeVBox.setSpacing(20);
            unsubscribeVBox.setPadding(new Insets(300, 0,0, 200));
            primaryPane.setCenter(unsubscribeVBox);
            unsubscribeBtn.setOnMouseClicked(event1 -> {
                try {
                    mqttServicesInstance.unsubscribe();
                    primaryPane.setCenter(new Text("Unsubscribed successfully"));
                } catch (JSONException | MqttException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     * On click, display all received tasks.
     */
    private void receivedTaskButtonListener() {

        receivedTaskButton.setOnMouseClicked(event -> {
            restoreButtonColors(receivedTaskButton);
            receivedTaskButton.setBackground(new Background(new BackgroundFill(Paint.valueOf("#B2E0F2"), null, null)));
            primaryPane.setCenter(new Text(""));
        });
    }

    /**
     * On click, display all executed tasks.
     */
    private void executedTaskButtonListener() {

        executedTaskButton.setOnMouseClicked(event -> {
            restoreButtonColors(executedTaskButton);
            executedTaskButton.setBackground(new Background(new BackgroundFill(Paint.valueOf("#B2E0F2"), null, null)));
            ListView<String> executedTaskListView = new ListView<>();
            ObservableList<String> listVewData = null;
            try {
                listVewData = FXCollections.observableArrayList(ReadWriteFile.getTaskDetailsFromReceivedTaskFile(FilePaths.getExecutedTaskFilePath().toString()));
                executedTaskListView.setItems(listVewData);
                primaryPane.setCenter(executedTaskListView);
            } catch (IOException e) {
                e.printStackTrace();
                primaryPane.setCenter(new Text(""));
            }
        });
    }

    /**
     * On application launch, to display the home menu by default.
     */
    private void fireHomeButton() {

        Event.fireEvent(homeButton, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0,
                0, 0, 0, MouseButton.PRIMARY, 1, true,
                true, true, true, true, true,
                true, true, true, true, null));
    }

    /**
     * On a button, selection restores the color of other unselected buttons.
     * @param selection A selected button of type Button.
     */
    private void restoreButtonColors(Button selection) {

        Stream.of(homeButton, subscribeButton, unSubscribeButton, receivedTaskButton, executedTaskButton)
                .filter(button -> !button.equals(selection))
                .forEach(button -> button.setBackground(new Background(new BackgroundFill(Paint.valueOf("#F0F0F0"), null, null))));
    }

    /**
     * Set button width and height properties.
     */
    private void setButtonProperties() {

        Arrays.asList(homeButton, subscribeButton, unSubscribeButton, receivedTaskButton, executedTaskButton).forEach(button -> {
            button.setMinWidth(180);
            button.setMinHeight(50);
        });
    }

    /**
     * Create VBox with properties. Add nodes image icon, and menu VBox.
     */
    private void initPrimaryVBox() {

        primaryVBox = new VBox();
        primaryVBox.setStyle("-fx-border-insets: 5; -fx-border-width: 2; -fx-border-style: solid; -fx-font-family: Courier");
        primaryVBox.setEffect(new DropShadow());
        primaryVBox.setPadding(new Insets(10,10,10,20));
        primaryVBox.getChildren().addAll(getMainIcon(), getButtonVBox());
        primaryVBox.setSpacing(10);
    }

    /**
     * Create HBox with an image.
     * @return An HBox node that contains an image.
     */
    private HBox getMainIcon() {

        ImageView ccIcon = getImageView();
        HBox imgHBox = new HBox();
        imgHBox.getChildren().add(ccIcon);
        imgHBox.setPadding(new Insets(10,30,40,20));
        return imgHBox;
    }

    /**
     * Add properties to the image.
     * @return an image of type ImageView.
     */
    private ImageView getImageView() {

        ImageView ccIcon = new ImageView(new Image(String.valueOf(Paths.get(FilePaths.resourcePath(), ("cc-icon.jpg")).toUri())));
        ccIcon.setFitHeight(100);
        ccIcon.setFitWidth(100);
        ccIcon.setEffect(new DropShadow());
        return ccIcon;
    }

    /**
     * Create VBox to hold all the menu buttons.
     * @return VBox that contains menu buttons.
     */
    private VBox getButtonVBox() {

        VBox buttonVBox = new VBox();
        buttonVBox.setSpacing(20);
        buttonVBox.getChildren().addAll(homeButton, subscribeButton, unSubscribeButton,
                receivedTaskButton, executedTaskButton, createContent());
        return buttonVBox;
    }

    /**
     * Create notification HBox with text and toggle switch.
     * @return toggleSwitchHBox of type HBox.
     */
    private HBox createContent() {

        ToggleSwitch toggle = new ToggleSwitch();
        HBox toggleSwitchHBox = new HBox();
        toggleSwitchHBox.setPadding(new Insets(70, 0,0,0));
        Text notificationText = new Text("Would you like to\nreceive a notification for every task?");
        notificationText.setEffect(new InnerShadow());
        notificationText.setStyle("-fx-font-size:14");
        toggleSwitchHBox.getChildren().addAll(notificationText, toggle);
        return toggleSwitchHBox;
    }

    /**
     * Set a connection status that received from {@link MqttServices.connectionStatusListener}.
     * @param status
     */
    @Override
    public void onStatusChange(String status) {
        connectionStatus = status;
    }

    /**
     * Inner class to implement notification toggle switch.
     */
    private static class ToggleSwitch extends Parent {

        BooleanProperty notificationOn = new SimpleBooleanProperty(false);
        TranslateTransition translate = new TranslateTransition(Duration.seconds(0.15));
        FillTransition fill = new FillTransition(Duration.seconds(0.15));
        ParallelTransition animation = new ParallelTransition(translate, fill);

        public ToggleSwitch() {

            Rectangle background = initRectangleProperties();
            Circle trigger = initCircleProperties();
            setAnimationTransition(background, trigger);
            toggleSwitchListener();
            setOnMouseClicked(event -> notificationOn.set(!notificationOn.get()));
        }
        //Action listener to set switch ON and OFF state
        private void toggleSwitchListener() {

            notificationOn.addListener((obs, oldState, newState) -> {

                boolean switchOn = onSelectTrue(newState);
                animation.play();
                System.out.println("notification selection?? "+ switchOn);
            });
        }

        //Set switch color based on the ON and OFF actions.
        private boolean onSelectTrue(Boolean newState) {

            boolean switchOn = newState;
            translate.setToX(switchOn ? 40-15 : 0);
            fill.setFromValue(switchOn ? Color.WHITE : Color.LIGHTGREEN);
            fill.setToValue(switchOn ? Color.LIGHTGREEN : Color.WHITE);
            return switchOn;
        }

        //Add animation transition to toggle switch
        private void setAnimationTransition(Rectangle background, Circle trigger) {

            translate.setNode(trigger);
            fill.setShape(background);
            getChildren().addAll(background, trigger);
        }

        //Create circle for toggle switch
        private Circle initCircleProperties() {

            Circle trigger = new Circle(8);
            trigger.setCenterX(8);
            trigger.setCenterY(8);
            trigger.setFill(Color.WHITE);
            trigger.setStroke(Color.LIGHTGRAY);
            return trigger;
        }
        //Create rectangle for toggle switch
        private Rectangle initRectangleProperties() {

            Rectangle background = new Rectangle(40, 15);
            background.setArcWidth(20);
            background.setArcHeight(20);
            background.setFill(Color.WHITE);
            background.setStroke(Color.LIGHTGRAY);
            return background;
        }
    }

    /**
     * get primary border pane.
     * @return primaryPane of type Border pane.
     */
    public BorderPane getBorderPane() {
        return primaryPane;
    }
}
