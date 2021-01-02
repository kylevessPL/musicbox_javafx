package musicbox;

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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javafx.animation.Animation.INDEFINITE;
import static javafx.animation.Animation.Status.RUNNING;
import static javafx.animation.Interpolator.LINEAR;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;

public class Main extends Application {

    private static final String AUDIO01_VALUE = "AUDIO 01";
    private static final String AUDIO02_VALUE = "AUDIO 02";

    private static final String AUDIO01_FILENAME = "D:\\java_test\\src\\musicbox\\boi.wav";
    private static final String AUDIO02_FILENAME = "";

    private static final double MAX_RATE = 20000;
    private static final double RATE_MODIFIER = 0.25;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private boolean running = false;

    private RotateTransition rotator;

    private ImageView windupKey;
    private TextField display;
    private ToggleGroup toggleGroup;
    private ToggleButton firstButton;

    private final MediaView viewer = new MediaView();

    @Override
    public void start(Stage primaryStage) {
        Scene scene = createScene();
        scene.getStylesheets().add(getClass().getResource("main.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("favicon.png")));
        primaryStage.setTitle("MusicBox");
        primaryStage.show();
        setupWindupKeyOnScroll();
        scene.addEventFilter(KEY_PRESSED, this::handleWindupKeyOnKeyUpPressed);
        scene.addEventFilter(KEY_RELEASED, this::handleWindupKeyOnKeyUpReleased);
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (toggleGroup.getSelectedToggle() == null) {
                toggleGroup.selectToggle(oldValue);
            }
            display.setText(toggleGroup.getSelectedToggle() == firstButton ? AUDIO01_VALUE : AUDIO02_VALUE);
        });
        playSound(AUDIO01_FILENAME);
    }

    public static void main(String[] args) {
        launch();
    }

    private void playSound(String filename) {
        Media media = new Media(new File(filename).toURI().toString());
        MediaPlayer player = new MediaPlayer(media);
        viewer.setMediaPlayer(player);
        player.play();
        System.out.println(player.getStatus().toString());
    }

    private Scene createScene() {
        AnchorPane root = new AnchorPane();
        ImageView musicBox = createMusicBox();
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
        SubScene scene = new SubScene(root, 140, 120, false, SceneAntialiasing.BALANCED);
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
        button.setToggleGroup(toggleGroup);
        button.setSelected(true);
        button.setFocusTraversable(false);
        return button;
    }

    private ToggleButton createSecondButton() {
        ToggleButton button = new ToggleButton();
        button.setLayoutX(484);
        button.setLayoutY(566);
        button.setToggleGroup(toggleGroup);
        button.setFocusTraversable(false);
        return button;
    }

    private RotateTransition createRotator(Node windupKey) {
        RotateTransition rotator = new RotateTransition(Duration.millis(10000000), windupKey);
        rotator.setAxis(Rotate.X_AXIS);
        rotator.setFromAngle(0);
        rotator.setToAngle(360);
        rotator.setInterpolator(LINEAR);
        rotator.setCycleCount(INDEFINITE);
        rotator.setRate(RATE_MODIFIER);
        return rotator;
    }

    private void handleWindupKeyOnKeyUpPressed(KeyEvent event) {
        if (event.getCode().equals(UP)) {
            running = true;
            if (rotator.getStatus().equals(RUNNING)) {
                rotator.pause();
            }
            double rate = rotator.getRate();
            rate = Math.min(rate + rate * RATE_MODIFIER, MAX_RATE);
            rotator.setRate(rate);
            rotator.play();
        }
    }

    private void handleWindupKeyOnKeyUpReleased(KeyEvent event) {
        if (event.getCode().equals(UP) && running) {
            running = false;
            executor.scheduleAtFixedRate(() -> {
                if (rotator.getRate() >= RATE_MODIFIER) {
                    rotator.pause();
                    double rate = rotator.getRate() - rotator.getRate() * RATE_MODIFIER;
                    rotator.setRate(rate);
                    rotator.play();
                } else {
                    Thread.currentThread().interrupt();
                }
            }, 0, 1, SECONDS);
        }
    }

    private void setupWindupKeyOnScroll() {
        rotator = createRotator(windupKey);
    }

}
