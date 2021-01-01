package musicbox;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    private static final String AUDIO01_VALUE = "AUDIO 01";
    private static final String AUDIO02_VALUE = "AUDIO 02";

    private static final String AUDIO01_FILENAME = "";
    private static final String AUDIO02_FILENAME = "";

    private ImageView musicBox;
    private ImageView windupKey;
    private TextField display;
    private ToggleGroup toggleGroup;
    private ToggleButton firstButton;

    @Override
    public void start(Stage primaryStage) {
        musicBox = createMusicBox();
        Scene scene = createScene();
        scene.getStylesheets().add(getClass().getResource("main.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("favicon.png")));
        primaryStage.setTitle("MusicBox");
        primaryStage.show();
        primaryStage.show();
        windupKey.setOnMouseClicked(event -> setWindupKeyMouseClicked());
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (toggleGroup.getSelectedToggle() == null) {
                toggleGroup.selectToggle(oldValue);
            }
            display.setText(toggleGroup.getSelectedToggle() == firstButton ? AUDIO01_VALUE : AUDIO02_VALUE);
        });
    }

    public static void main(String[] args) {
        launch();
    }

    private Scene createScene() {
        AnchorPane root = new AnchorPane();
        windupKey = createWindupKey();
        display = createDisplay();
        toggleGroup = new ToggleGroup();
        firstButton = createFirstButton();
        ToggleButton secondButton = createSecondButton();
        SubScene windupScene = createWindupScene();
        root.getChildren().addAll(windupScene, musicBox, display, firstButton, secondButton);
        return new Scene(root, 750, 750);
    }

    private SubScene createWindupScene() {
        StackPane root = new StackPane();
        root.getChildren().addAll(windupKey);
        SubScene scene = new SubScene(root, 140, 120, true, SceneAntialiasing.BALANCED);
        scene.setLayoutX(560);
        scene.setLayoutY(540);
        scene.setCamera(new PerspectiveCamera());
        return scene;
    }

    private ImageView createMusicBox() {
        return new ImageView(new Image(getClass().getResourceAsStream("music_box.png")));
    }

    private ImageView createWindupKey() {
        return new ImageView(new Image(getClass().getResourceAsStream("windup_key.png"), 99, 106, true, false));
    }

    private TextField createDisplay() {
        TextField display = new TextField();
        display.setDisable(true);
        display.setText(AUDIO01_VALUE);
        display.setLayoutX(195);
        display.setLayoutY(636);
        return display;
    }

    private ToggleButton createFirstButton() {
        ToggleButton button = new ToggleButton();
        button.setLayoutX(79);
        button.setLayoutY(566);
        button.setId("firstButton");
        button.setToggleGroup(toggleGroup);
        button.setSelected(true);
        return button;
    }

    private ToggleButton createSecondButton() {
        ToggleButton button = new ToggleButton();
        button.setLayoutX(484);
        button.setLayoutY(566);
        button.setId("secondButton");
        button.setToggleGroup(toggleGroup);
        return button;
    }

    private RotateTransition createRotator(Node windupKey) {
        RotateTransition rotator = new RotateTransition(Duration.millis(10000), windupKey);
        rotator.setAxis(Rotate.X_AXIS);
        rotator.setFromAngle(0);
        rotator.setToAngle(360);
        rotator.setInterpolator(Interpolator.LINEAR);
        rotator.setCycleCount(RotateTransition.INDEFINITE);
        return rotator;
    }

    private void setWindupKeyMouseClicked() {
        RotateTransition rotator = createRotator(windupKey);
        rotator.play();
    }

}
