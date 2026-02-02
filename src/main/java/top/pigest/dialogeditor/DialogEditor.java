package top.pigest.dialogeditor;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import top.pigest.dialogeditor.control.SizeChangeListener;
import top.pigest.dialogeditor.util.Utils;
import top.pigest.dialogeditor.main.MainScene;

import java.util.Objects;

public class DialogEditor extends Application {
    public static DialogEditor INSTANCE;

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        System.setProperty("javafx.animation.fullspeed", "true");
        System.setProperty("javafx.animation.pulse", "60");
        System.setProperty("prism.lcdtext", "true");
        System.setProperty("prism.text", "t2k");
        INSTANCE = this;
        this.primaryStage = primaryStage;
        //primaryStage.setResizable(false);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (getMainScene().getMainContainer() instanceof SizeChangeListener listener) {
                listener.onWidthChanged(newValue.intValue());
            }
        });
        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (getMainScene().getMainContainer() instanceof SizeChangeListener listener) {
                listener.onHeightChanged(newValue.intValue());
            }
        });
        primaryStage.setTitle("Diapig Editor by @小猪之最Thepig");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("icon.png"))));
        primaryStage.setScene(new MainScene());
        primaryStage.setOnCloseRequest(event -> {
            MainScene scene = (MainScene) primaryStage.getScene();
            if (scene.getRootDrawer().getChildren().stream().noneMatch(node -> node.getId() != null && node.getId().equals("close-confirm"))) {
                Utils.showChoosingDialog("关闭程序", "确认要关闭 Diapig Editor 吗？", "确认", "取消", event1 -> System.exit(0), event1 -> {}, scene.getRootDrawer());
            }
            event.consume();
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public MainScene getMainScene() {
        return ((MainScene) primaryStage.getScene());
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

}