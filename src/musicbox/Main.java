package musicbox;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    private ImageView musicBox;
    private ImageView windupKey;

    @Override
    public void start(Stage primaryStage) {
        musicBox = createMusicBox();
        primaryStage.setScene(createScene());
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("favicon.png")));
        primaryStage.setTitle("MusicBox");
        primaryStage.show();
        primaryStage.show();
        windupKey.setOnMouseClicked(event -> setWindupKeyMouseClicked());
    }

    private Scene createScene() {
        AnchorPane root = new AnchorPane();
        windupKey = createWindupKey();
        SubScene scene = createWindupScene();
        root.getChildren().addAll(musicBox, scene);
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
        return new ImageView(new Image(getClass().getResourceAsStream("windup_key.png"), 99, 106, false, false));
    }

    private RotateTransition createRotator(Node card) {
        RotateTransition rotator = new RotateTransition(Duration.millis(10000), card);
        rotator.setAxis(Rotate.X_AXIS);
        rotator.setFromAngle(0);
        rotator.setToAngle(360);
        rotator.setInterpolator(Interpolator.LINEAR);
        rotator.setCycleCount(RotateTransition.INDEFINITE);
        return rotator;
    }

    public void setWindupKeyMouseClicked() {
        RotateTransition rotator = createRotator(windupKey);
        rotator.play();
    }

    public static void main(String[] args) {
        launch();
    }

}
