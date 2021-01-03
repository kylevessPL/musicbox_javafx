package musicbox;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javafx.animation.Animation.Status.RUNNING;
import static javafx.animation.Interpolator.LINEAR;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;

public class Main extends Application {

    private static final String AUDIO01_VALUE = "AUDIO 01";
    private static final String AUDIO02_VALUE = "AUDIO 02";

    private static final String AUDIO01_FILENAME = "audio01.mp3";
    private static final String AUDIO02_FILENAME = "audio02.mp3";

    private static final double MIN_WINDUP_RATE = 3000;
    private static final double MAX_WINDUP_RATE = 25000;
    private static final double MIN_AUDIO_RATE = 1;
    private static final double MAX_AUDIO_RATE = 1.15;
    private static final double WINDUP_RATE_MODIFIER = 0.5;
    private static final double AUDIO_RATE_MODIFIER = 0.02;

    private final SimpleObjectProperty<MediaView> mediaView = new SimpleObjectProperty<>(this, "mediaView");

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> task;

    private boolean running = false;
    private double currentAudioRate = MIN_AUDIO_RATE;

    private MediaPlayer player;
    private RotateTransition rotator;
    private ImageView windupKey;
    private TextField display;
    private ToggleGroup toggleGroup;
    private ToggleButton firstButton;

    @Override
    public void start(Stage primaryStage) {
        Scene scene = createScene();
        scene.getStylesheets().add(getClass().getResource("main.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("favicon.png")));
        primaryStage.setTitle("MusicBox");
        primaryStage.show();
        setupWindupKeyOnScroll();
        setupAudio(AUDIO01_FILENAME);
        scene.addEventFilter(KEY_PRESSED, this::handleWindupKeyOnKeyUpPressed);
        scene.addEventFilter(KEY_RELEASED, this::handleWindupKeyOnKeyUpReleased);
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (toggleGroup.getSelectedToggle() == null) {
                toggleGroup.selectToggle(oldValue);
            }
            display.setText(toggleGroup.getSelectedToggle() == firstButton ? AUDIO01_VALUE : AUDIO02_VALUE);
            player.stop();
            setupAudio(toggleGroup.getSelectedToggle() == firstButton ? AUDIO01_FILENAME : AUDIO02_FILENAME);
        });
    }

    public static void main(String[] args) {
        launch();
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
        rotator.setCycleCount(Animation.INDEFINITE);
        rotator.setRate(WINDUP_RATE_MODIFIER);
        return rotator;
    }

    private void handleWindupKeyOnKeyUpPressed(KeyEvent event) {
        if (event.getCode().equals(UP)) {
            running = true;
            if (rotator.getStatus().equals(RUNNING)) {
                rotator.pause();
            }
            double windupRate = rotator.getRate();
            windupRate = Math.min(windupRate + windupRate * WINDUP_RATE_MODIFIER, MAX_WINDUP_RATE);
            rotator.setRate(windupRate);
            rotator.play();
            currentAudioRate = Math.min(currentAudioRate + currentAudioRate * AUDIO_RATE_MODIFIER, MAX_AUDIO_RATE);
            player.setRate(currentAudioRate);
            player.play();
        }
    }

    private void handleWindupKeyOnKeyUpReleased(KeyEvent event) {
        if (event.getCode().equals(UP) && running) {
            running = false;
            task = executor.scheduleAtFixedRate(() -> {
                if (rotator.getRate() >= MIN_WINDUP_RATE) {
                    if (running) {
                        task.cancel(false);
                        return;
                    }
                    rotator.pause();
                    double windupRate = rotator.getRate() - rotator.getRate() * WINDUP_RATE_MODIFIER / 2;
                    rotator.setRate(windupRate);
                    rotator.play();
                    double tempAudioRate = currentAudioRate - currentAudioRate * AUDIO_RATE_MODIFIER;
                    currentAudioRate = Math.max(tempAudioRate, MIN_AUDIO_RATE);
                    player.setRate(currentAudioRate);
                    player.play();
                } else {
                    currentAudioRate = MIN_AUDIO_RATE;
                    player.setRate(currentAudioRate);
                    player.play();
                    task.cancel(false);
                }
            }, 0, 1, SECONDS);
        }
    }

    private void setupWindupKeyOnScroll() {
        rotator = createRotator(windupKey);
    }

    private void setupAudio(String filename) {
        Media media = new Media(getClass().getResource(filename).toExternalForm());
        player = new MediaPlayer(media);
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setRate(currentAudioRate);
        mediaView.set(new MediaView(player));
        if (rotator.getStatus() == RUNNING) {
            player.play();
        }
    }

}
